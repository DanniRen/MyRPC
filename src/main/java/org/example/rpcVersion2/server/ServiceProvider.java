package org.example.rpcVersion2.server;

import org.springframework.aop.support.AopUtils;

import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    /*
    包含接口和实现类的映射关系
     */
    private Map<String, Object> interfaceServices;

    public ServiceProvider() {
        this.interfaceServices = new HashMap<>();
    }

    public void registerService(Object service) {
        Class<?> targetClass = AopUtils.getTargetClass(service);
        Class<?>[] interfaces = targetClass.getInterfaces();

        for (Class<?> interfaceClass : interfaces) {
            this.interfaceServices.put(interfaceClass.getName(), service);
        }
    }

    public Object getService(String interfaceName) {
        interfaceServices.forEach((k, v) -> {
            System.out.println(k + ":" + v);
        });
        return this.interfaceServices.get(interfaceName);
    }
}
