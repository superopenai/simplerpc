package me.superning.consumer.client;

import me.superning.consumer.domain.RpcRequest;
import me.superning.consumer.domain.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC代理
 * @date 2020年1月16日
 * @author superning
 */
public class rpcProxy {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String serverAddress;

    public rpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public <T> T create(Class<?> interFaceClass) {
        return (T) Proxy.newProxyInstance(
                interFaceClass.getClassLoader(),
                new Class<?>[] {interFaceClass},
                (proxy, method, args) -> {
                    //创建并且初始化RPC请求，并设置请求参数
                    RpcRequest request = new RpcRequest();
                    request.setRequestId(UUID.randomUUID().toString());
                    request.setClassName(method.getDeclaringClass().getName());
                    request.setMethodName(method.getName());
                    request.setParameterTypes(method.getParameterTypes());
                    request.setParameters(args);


                    //解析主机名和端口
                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);

                    //初始化RPC客户端
                    rpcClient client = new rpcClient(host, port);

                    long startTime = System.currentTimeMillis();
                    //通过RPC客户端发送rpc请求并且获取rpc响应
                    RpcResponse response = client.send(request);
                    logger.debug("send rpc request elapsed time: {}ms...", System.currentTimeMillis() - startTime);

                    if (response == null) {
                        throw new RuntimeException("response is null...");
                    }
                    //返回RPC响应结果
                    if(response.getError()!=null){
                        throw response.getError();
                    }else {
                        return response.getResult();
                    }
                }
        );





    }


}
