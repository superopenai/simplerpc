package me.superning.provider.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.superning.provider.domain.RpcRequest;
import me.superning.provider.domain.RpcResponse;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.util.XMLEventConsumer;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
/**
 * 处理RPC请求, 只需扩展 Netty 的SimpleChannelInboundHandler抽象类即可
 * @author superning
 * @date 2020年1月16日
 */
public class rpcHandler  extends SimpleChannelInboundHandler<RpcRequest> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Object> handlerMap;

    public rpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }



    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

        RpcResponse rpcResponse = new RpcResponse();

        rpcResponse.setReponseId(rpcRequest.getRequestId());
        Object reflectResult = reflect(rpcRequest);
        rpcResponse.setResult(reflectResult);

        channelHandlerContext.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);


    }


    /**
     *处理RPC请求
      * @param request
     * @return
     * @throws InvocationTargetException
     */
    private Object reflect(RpcRequest request) throws InvocationTargetException {
        String className = request.getClassName();
        Object bean = handlerMap.get(className);

        //获取反射所需要的参数
        Class<?> reflectClass = bean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        //cglib反射，可以改善java原生的反射性能*
        FastClass fastClass = FastClass.create(reflectClass);
        FastMethod method = fastClass.getMethod(methodName, parameterTypes);
        return method.invoke(bean,parameters);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server caught exception ",cause);
        ctx.close();

    }
}
