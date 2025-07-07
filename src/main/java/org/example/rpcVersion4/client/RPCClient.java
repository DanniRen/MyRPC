package org.example.rpcVersion4.client;

import org.example.rpcVersion4.common.RPCRequest;
import org.example.rpcVersion4.common.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request) throws InterruptedException;

    void close();
}
