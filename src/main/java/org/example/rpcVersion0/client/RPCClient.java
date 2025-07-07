package org.example.rpcVersion0.client;

import org.example.rpcVersion0.common.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RPCClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 8899);

            System.out.println("客户端运行在: " + socket.getLocalSocketAddress());

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeLong(1L);
            oos.flush();

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            User user = (User) ois.readObject();
            System.out.println("服务端返回的user信息：" + user);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("客户端启动失败！");
        }

    }
}
