package org.example.rpcVersion5.client;

import org.example.rpcVersion5.common.RPCRequest;
import org.example.rpcVersion5.common.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request) throws InterruptedException;

    void close();
}
