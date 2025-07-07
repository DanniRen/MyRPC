package org.example.rpcVersion3.server;

import java.io.IOException;

public interface RPCServer3 {
    void start(int port) throws IOException, ClassNotFoundException;
    void stop();
}
