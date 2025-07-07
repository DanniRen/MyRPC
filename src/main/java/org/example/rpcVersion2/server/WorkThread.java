package org.example.rpcVersion2.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.rpcVersion2.common.RPCRequest;
import org.example.rpcVersion2.common.RPCResponse;
import org.springframework.validation.ObjectError;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

@Slf4j
@AllArgsConstructor
public class WorkThread implements Runnable {

    private Socket socket;
    private ServiceProvider serviceProvider;

    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            RPCRequest request = (RPCRequest) ois.readObject();
            System.out.println("读取到来自客户端的请求信息：" + request);
            RPCResponse response = getResponse(request);

            oos.writeObject(response);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("从IO数据流中读取数据错误");
        }
    }

    public RPCResponse getResponse(RPCRequest request) {
        try {
            Object service = serviceProvider.getService(request.getInterfaceName());
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object response = method.invoke(service, request.getParams());
            return RPCResponse.success(response);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行不成功！");
            return RPCResponse.fail();
        }
    }
}
