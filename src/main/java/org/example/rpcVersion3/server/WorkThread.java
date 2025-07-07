package org.example.rpcVersion3.server;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rpcVersion3.common.RPCRequest;
import org.example.rpcVersion3.common.RPCResponse;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

@Slf4j
@AllArgsConstructor
public class WorkThread implements Runnable {

    private SocketChannel clientChannel;
    private ServiceProvider serviceProvider;

    @Override
    public void run() {
        try {
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            while(lengthBuffer.hasRemaining()) {
                int read = clientChannel.read(lengthBuffer);
                if(read == -1) {
                    clientChannel.close();
                    System.out.println(Thread.currentThread().getName() + "Client connection closed");
                    return;
                }
            }
            lengthBuffer.flip();
            int length = lengthBuffer.getInt();

            ByteBuffer readBuffer = ByteBuffer.allocate(length);
            while(readBuffer.hasRemaining()) {
                int read = clientChannel.read(readBuffer);
                if(read == -1) {
                    clientChannel.close();
                    System.out.println(Thread.currentThread().getName() + "Client connection closed");
                    return;
                }
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(readBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);
            RPCRequest request = (RPCRequest) ois.readObject();
            System.out.println(Thread.currentThread().getName() + "读取到来自客户端的请求信息：" + request);

            // 获取调用结果
            RPCResponse response = getResponse(request);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(response);
            oos.flush();

            byte[] byteArray = baos.toByteArray();
            ByteBuffer writeBuffer = ByteBuffer.allocate(4 + byteArray.length);
            writeBuffer.putInt(byteArray.length);
            writeBuffer.put(byteArray);
            writeBuffer.flip();

            clientChannel.write(writeBuffer);
            System.out.println(Thread.currentThread().getName() + "已经将response返回给客户端！");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(Thread.currentThread().getName() + "从IO数据流中读取数据错误");
            try {
                clientChannel.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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
            System.out.println(Thread.currentThread().getName() + "方法执行不成功！");
            return RPCResponse.fail();
        }
    }
}
