package org.example.rpcVersion6.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import org.example.rpcVersion6.common.RPCRequest;
import org.example.rpcVersion6.common.RPCResponse;

/**
 * 协议编码器，设计格式为
 * +--------+--------+--------+----------------+
 * | 消息类型 | 序列化方式 | 数据长度 | 序列化数据内容 |
 * +--------+--------+--------+----------------+
 * |  2字节   |  2字节    |  4字节  |    N字节       |
 * +--------+--------+--------+----------------+
 */
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {
    private Serializer serializer;
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        // 写入消息类型
        if(msg instanceof RPCRequest){
            byteBuf.writeShort(MessageType.REQUEST.getCode());
        } else if (msg instanceof RPCResponse) {
            byteBuf.writeShort(MessageType.RESPONSE.getCode());
        }
        // 写入序列化方式
        byteBuf.writeShort(serializer.getType());

        byte[] serialize = serializer.serialize(msg);
        // 写入消息体长度
        byteBuf.writeInt(serialize.length);
        // 写入序列化的数据内容
        byteBuf.writeBytes(serialize);
    }
}
