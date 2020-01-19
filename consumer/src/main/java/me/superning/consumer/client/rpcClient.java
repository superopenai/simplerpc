package me.superning.consumer.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.superning.consumer.domain.RpcRequest;
import me.superning.consumer.domain.RpcResponse;
import me.superning.consumer.handler.rpcDecoder;
import me.superning.consumer.handler.rpcEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC客户端
 *
 * @author superning
 * @date 2020年1月16日
 */
public class rpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String host;

    private int port;

    private RpcResponse response;


    private final Object obj = new Object();

    public rpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {

        this.response = rpcResponse;

        synchronized (obj) {
            //收到响应，唤醒线程
            obj.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("client caught exception...", cause);
        ctx.close();
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                //发送请求（encode）
                                .addLast(new rpcEncoder(RpcRequest.class))
                                //返回相应（decode）
                                .addLast(new rpcDecoder(RpcResponse.class))
                                .addLast(rpcClient.this);
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
        channelFuture.channel().writeAndFlush(request).sync();
        synchronized (obj){
            //未收到响应，使线程继续等待
            obj.wait();
        }
        if (null!=response)
        {
            channelFuture.channel().closeFuture().sync();

        }

        group.shutdownGracefully();

        return  response;


    }

}
