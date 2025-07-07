package org.example.rpcVersion7.server;

import java.io.IOException;

public interface RPCServer7 {
    void start() throws IOException, ClassNotFoundException;
    void stop();
}
