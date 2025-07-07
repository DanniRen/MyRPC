package org.example.rpcVersion7.client;

import org.example.rpcVersion7.common.RPCRequest;
import org.example.rpcVersion7.common.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request) throws InterruptedException;

    void close();
}
