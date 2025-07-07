package org.example.rpcVersion2.common;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RPCResponse implements Serializable {
    /*
    包含一些状态信息和数据
     */
    int code;
    String message;

    Object data;

    public static RPCResponse success(Object data) {
        return RPCResponse.builder().code(200).message("success").data(data).build();
    }
    public static RPCResponse fail() {
        return RPCResponse.builder().code(500).message("fail").build();
    }
}
