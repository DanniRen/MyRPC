package org.example.rpcVersion4.server;

import java.io.IOException;

public interface RPCServer4 {
    void start(int port) throws IOException, ClassNotFoundException;
    void stop();
}
