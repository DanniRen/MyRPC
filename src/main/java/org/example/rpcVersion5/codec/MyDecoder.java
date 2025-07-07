package org.example.rpcVersion5.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MyDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        short messageType = byteBuf.readShort();
        if(messageType != MessageType.REQUEST.getCode() &&
        messageType != MessageType.RESPONSE.getCode()){
            System.out.println("暂时不支持这种消息类型的解码");
            return;
        }

        short serializerType = byteBuf.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if(serializer == null) {
            System.out.println("不存在对应的序列化器");
        }
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Object deserialize = serializer.deserialize(bytes, messageType);
        list.add(deserialize);
    }
}
