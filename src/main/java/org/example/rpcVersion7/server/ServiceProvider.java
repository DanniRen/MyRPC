package org.example.rpcVersion7.server;

import org.example.rpcVersion7.register.NacosServiceRegister;
import org.example.rpcVersion7.register.RandomLoadBalance;
import org.example.rpcVersion7.register.ServiceRegister;
import org.springframework.aop.support.AopUtils;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    /*
    包含接口和实现类的映射关系
     */
    private Map<String, Object> interfaceServices;
    private ServiceRegister serviceRegister;

    private String host;

    private int port;

    public ServiceProvider(String host, int port) {
        this.interfaceServices = new HashMap<>();
        this.serviceRegister = new NacosServiceRegister(new RandomLoadBalance());
        this.host = host;
        this.port = port;
    }

    public void registerService(Object service) {
        Class<?> targetClass = AopUtils.getTargetClass(service);
        Class<?>[] interfaces = targetClass.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            this.interfaceServices.put(interfaceClass.getName(), service);
            serviceRegister.register(interfaceClass.getName(), new InetSocketAddress(host, port));
        }
    }

    public Object getService(String interfaceName) {
        return this.interfaceServices.get(interfaceName);
    }
}
