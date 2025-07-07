package org.example.rpcVersion6.server;

import java.io.IOException;

public interface RPCServer6 {
    void start(int port) throws IOException, ClassNotFoundException;
    void stop();
}
