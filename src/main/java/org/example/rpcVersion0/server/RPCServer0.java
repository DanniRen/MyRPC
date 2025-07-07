package org.example.rpcVersion0.server;

import jakarta.annotation.Resource;
import org.example.rpcVersion0.common.User;
import org.example.rpcVersion0.server.service.UserService0;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class RPCServer0 {

    @Resource
    private UserService0 userService ;

    public User getUserById(long id){
        return userService.getById(id);
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

                        Long id = ois.readLong();
                        User userById = getUserById(id);
                        oos.writeObject(userById);
                        oos.flush();
                    } catch (IOException e) {
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
