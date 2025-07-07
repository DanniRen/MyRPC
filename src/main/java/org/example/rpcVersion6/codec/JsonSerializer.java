package org.example.rpcVersion6.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.example.rpcVersion6.common.RPCRequest;
import org.example.rpcVersion6.common.RPCResponse;

/**
 * 由于json序列化的方式是通过把对象转化成字符串，丢失了Data对象的类信息，所以deserialize需要
 * 了解对象对象的类信息，根据类信息把JsonObject -> 对应的对象
 */
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = JSONObject.toJSONBytes(obj);
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        switch(messageType){
            case 0:
                RPCRequest request = JSON.parseObject(bytes, RPCRequest.class);
                if(request.getParams() == null) return request;

                Object[] objects = new Object[request.getParams().length];
                for(int i = 0; i < objects.length; i ++){
                    Class<?> paramType = request.getParamTypes()[i];
                    if(!paramType.isAssignableFrom(request.getParams()[i].getClass())){
                        objects[i] = JSONObject.toJavaObject((JSONObject) request.getParams()[i], paramType);
                    }
                    else {
                        objects[i] = request.getParams()[i];
                    }
                }
                request.setParams(objects);
                obj = request;
                break;
            case 1:
                RPCResponse response = JSON.parseObject(bytes, RPCResponse.class);
                Class<?> dataType = response.getDataType();
                if(!dataType.isAssignableFrom(response.getData().getClass())){
                    response.setData(JSONObject.toJavaObject((JSONObject)response.getData(), dataType));
                }
                obj = response;
                break;
            default:
                System.out.println("暂不支持这种消息类型");
                throw new RuntimeException();
        }
        return obj;
    }

    @Override
    public int getType() {
        return 1;
    }
}
