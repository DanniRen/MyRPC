package org.example.rpcVersion6.client;

import org.example.rpcVersion6.common.RPCRequest;
import org.example.rpcVersion6.common.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request) throws InterruptedException;

    void close();
}
