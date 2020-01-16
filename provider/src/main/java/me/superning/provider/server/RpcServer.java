package me.superning.provider.server;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.naming.NamingService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jdk.nashorn.internal.ir.CallNode;
import me.superning.provider.domain.RpcRequest;
import me.superning.provider.domain.RpcResponse;
import me.superning.provider.handler.rpcDecoder;
import me.superning.provider.handler.rpcEncoder;
import me.superning.provider.handler.rpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author superning
 * @date 2020年1月15日
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String providerAdder;
    private String serviceRegistry;

    private Map<String, Object> handermap = new HashMap<String, Object>();

    public RpcServer(String providerAdder, String serviceRegistry) {
        this.providerAdder = providerAdder;
        this.serviceRegistry = serviceRegistry;
    }

    @NacosInjected
    private NamingService namingService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (beansWithAnnotation.size() > 0) {
            for (Object value : beansWithAnnotation.values()) {
                String key = beansWithAnnotation.getClass().getAnnotation(RpcService.class).value().getName();
                handermap.put(key,value);

            }
        }

    }


    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup masterGroup = new NioEventLoopGroup();
        EventLoopGroup slaverGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(masterGroup,slaverGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        .addLast(new rpcDecoder(RpcRequest.class))
                                        .addLast(new rpcEncoder(RpcResponse.class))
                                        .addLast(new rpcHandler(handermap));
                            }

                        })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        //从字符串中解析出ip地址和端口
        String[] split = providerAdder.split(":");
        String host  = split[0];
        int port  = Integer.parseInt(split[1]);
        ChannelFuture channelFuture = serverBootstrap.bind(host, port).sync();
        logger.debug("server started on port: [{}]", port);

        //
        if (namingService.getAllInstances(providerAdder)==null)
        {
            namingService.registerInstance(providerAdder,"192.168.124.133",8848);
        }

        //关闭RPC服务器
        channelFuture.channel().closeFuture().sync();

        slaverGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();
    }


}
