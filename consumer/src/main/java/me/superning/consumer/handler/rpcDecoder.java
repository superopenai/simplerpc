package me.superning.consumer.handler;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * RPC请求解码，解码二进制内容,只需扩展Netty的ByteToMessageDecoder，并且实现其decode方法即可
 * @author superning
 * @date 2020年1月16日
 */
public class rpcDecoder extends ByteToMessageDecoder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Class<?> genericClass;
    public rpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        if (byteBuf.readableBytes()<4)
        {
            return;
        }
        byteBuf.markReaderIndex();
        int datalength = byteBuf.readInt();
        if (datalength<0)
        {
            channelHandlerContext.close();

        }

        if (byteBuf.readableBytes()<datalength)
        {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] data = new byte[datalength];
        byteBuf.readBytes(data);
        Object o = JSON.parseObject(data, genericClass);
        logger.info("this object is [{}]",o);
        list.add(o);
    }
}
