package org.example.rpcVersion2.client;

import org.example.rpcVersion2.common.RPCRequest;
import org.example.rpcVersion2.common.RPCResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class IOClient {
    public static RPCResponse sendRequest(String host, int port, RPCRequest request) {

        try {
            Socket socket = new Socket(host, port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(request);
            oos.flush();

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            RPCResponse response = (RPCResponse) ois.readObject();
            System.out.println("服务端返回的信息：" + response);
            return response;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
