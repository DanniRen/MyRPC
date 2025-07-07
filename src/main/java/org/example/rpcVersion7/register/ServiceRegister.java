package org.example.rpcVersion7.register;

import java.net.InetSocketAddress;

public interface ServiceRegister {
    void register(String serviceName, InetSocketAddress serverAddr);

    InetSocketAddress serviceDiscovery(String serviceName);
}
