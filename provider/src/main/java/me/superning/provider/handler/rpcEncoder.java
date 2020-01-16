package me.superning.provider.handler;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC请求编码，只需扩展 Netty 的MessageToByteEncoder抽象类，并且实现其encode方法即可
 *
 * @author superning
 * @date 2020年1月16日
 */
public class rpcEncoder extends MessageToByteEncoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Class<?> genericClass;

    public rpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {

        if (genericClass.isInstance(o)) {
            byte[] bytes = JSON.toJSONBytes(o);
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);

        }

    }
}
