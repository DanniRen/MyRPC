package org.example.rpcVersion5.server;

import java.io.IOException;

public interface RPCServer5 {
    void start(int port) throws IOException, ClassNotFoundException;
    void stop();
}
