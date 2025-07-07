package org.example.rpcVersion6.codec;

import java.io.IOException;

public interface Serializer {
    // 主要有两个功能：
    // 一是将对象序列化成字节数组
    byte[] serialize(Object obj) throws IOException;
    // 二是将字节数组反序列化成消息
    Object deserialize(byte[] bytes, int messageType);

    // 返回使用的序列化器
    int getType();

    static Serializer getSerializerByCode(int code){
        switch(code){
            case 0:
                return new ObjectSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }
}
