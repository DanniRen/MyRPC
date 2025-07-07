package org.example.rpcVersion6.register;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public interface ServiceRegister {
    void register(String serviceName, InetSocketAddress serverAddr);

    InetSocketAddress serviceDiscovery(String serviceName);
}
