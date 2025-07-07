package org.example.rpcVersion5.client;

import lombok.AllArgsConstructor;
import org.example.rpcVersion5.common.RPCRequest;
import org.example.rpcVersion5.common.RPCResponse;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@AllArgsConstructor
public class ClientProxy implements InvocationHandler {
    private RPCClient rpcClient;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?>[] interfaces = AopUtils.getTargetClass(proxy).getInterfaces();
        String interfaceName = "";
        for (Class<?> iface : interfaces) {
            try {
                Method m = iface.getMethod(method.getName(), method.getParameterTypes());
                interfaceName = iface.getName();
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodException(interfaceName + "." + method.getName());
            }
        }
        RPCRequest request = RPCRequest.builder()
                .interfaceName(interfaceName)
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();
        try {
            RPCResponse response = rpcClient.sendRequest(request);
            return response.getData();
        } catch (InterruptedException e) {
            e.printStackTrace();
            rpcClient.close();
        }
        return null;
    }

    <T>T createProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
    }
}
