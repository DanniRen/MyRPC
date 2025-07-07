package org.example.rpcVersion3.client;

import org.example.rpcVersion3.common.RPCRequest;
import org.example.rpcVersion3.common.RPCResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOClient {
    public static RPCResponse sendRequest(String host, int port, RPCRequest request) {

        try {
            // 连接服务端，new一个socketChannel
            SocketChannel socketChannel = SocketChannel.open();
//            socketChannel.configureBlocking(false);
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socketChannel.connect(socketAddress);

            // 将request请求体序列化
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(request);
            oos.flush();

            // 发送请求体长度 + 具体数据
            byte[] writeByteArray = baos.toByteArray();
            ByteBuffer writeBuffer = ByteBuffer.allocate(4 + writeByteArray.length);
            writeBuffer.putInt(writeByteArray.length);
            writeBuffer.put(writeByteArray);
            writeBuffer.flip();

            int totalWritten = 0;
            while (writeBuffer.hasRemaining()) {
                int written = socketChannel.write(writeBuffer);
                System.out.println("本次写入字节数：" + written);
                if (written <= 0) {
                    System.out.println("写入被阻塞或连接关闭");
                    break;
                }
                totalWritten += written;
            }
            System.out.println("写入完成，总字节数：" + totalWritten);

            // 接收响应体长度 + 具体数据
            // 先读取响应体长度
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            while (lengthBuffer.hasRemaining()) {
                int read = socketChannel.read(lengthBuffer);
                if (read == -1) {
                    throw new IOException("Server Socket closed");
                }
            }
            lengthBuffer.flip();
            int length = lengthBuffer.getInt();
            if (length <= 0) {
                throw new IOException("Invalid response length: " + length);
            }

            ByteBuffer readBuffer = ByteBuffer.allocate(length);
            while (readBuffer.hasRemaining()) {
                int read = socketChannel.read(readBuffer);
                if (read == -1) {
                    throw new IOException("Server Socket closed");
                }
            }
            readBuffer.flip();
            ByteArrayInputStream bais = new ByteArrayInputStream(readBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);
            RPCResponse response = (RPCResponse) ois.readObject();
            System.out.println("服务端返回的信息：" + response);

            socketChannel.close();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
