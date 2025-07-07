package org.example.rpcVersion7.register;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

public class NacosServiceRegister implements ServiceRegister {
    NamingService naming;
    LoadBalance loadBalancer;

    public NacosServiceRegister(LoadBalance loadBalancer) {
        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, "127.0.0.1:8848");
            properties.put(PropertyKeyConst.USERNAME, "nacos");  // 用户名
            properties.put(PropertyKeyConst.PASSWORD, "nacos");  // 密码
            this.naming = NacosFactory.createNamingService(properties);
            this.loadBalancer = loadBalancer;
        } catch (NacosException e) {
            e.printStackTrace();
            System.out.println("连接Nacos失败！");
        }
    }

    @Override
    public void register(String serviceName, InetSocketAddress serverAddr) {
        Instance instance = new Instance();
        instance.setIp(serverAddr.getHostName());
        instance.setPort(serverAddr.getPort());
        instance.setClusterName("DEFAULT");
        try {
            naming.registerInstance(serviceName, instance);
        } catch (NacosException e) {
            e.printStackTrace();
            System.out.println("注册服务: " + serviceName + "失败!");
        }
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            List<Instance> instanceList = naming.getAllInstances(serviceName);
            Instance instance = loadBalancer.Select(instanceList);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            e.printStackTrace();
            System.out.println("没有" + serviceName + "服务的实例！");
        }
        return null;
    }
}
