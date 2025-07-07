package org.example.rpcVersion1.server;

import jakarta.annotation.Resource;
import org.example.rpcVersion1.common.RPCRequest;
import org.example.rpcVersion1.common.RPCResponse;
import org.example.rpcVersion1.common.User;
import org.example.rpcVersion1.server.service.UserService1;
import org.springframework.stereotype.Component;
import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class RPCServer1 {

    @Resource
    private UserService1 userService1;

    public User getUserById(long id){
        return userService1.getById(id);
    }

    public boolean updateUser(User user){
        return userService1.updateById(user);
    }

    public void start(){
        try {
            ServerSocket serverSocket = new ServerSocket(8899);
            System.out.println("服务端启动了！运行在: " + serverSocket.getLocalSocketAddress());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("接收到了来自客户端：" + socket.getRemoteSocketAddress() + "的请求！");
                new Thread(() -> {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                        RPCRequest request = (RPCRequest) ois.readObject();
                        Method method = userService1.getClass().getMethod(request.getMethodName(), request.getParamTypes());
                        Object response = method.invoke(userService1, request.getParams());

                        oos.writeObject(RPCResponse.success(response));
                        oos.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("从IO数据流中读取数据错误");
                    }
                }).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
        }
    }
}
