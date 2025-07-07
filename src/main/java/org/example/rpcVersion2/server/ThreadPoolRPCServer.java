package org.example.rpcVersion2.server;

import org.example.rpcVersion2.server.service.ShopService2;
import org.example.rpcVersion2.server.service.UserService2;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ThreadPoolRPCServer implements RPCServer2{
    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR;
    private ServiceProvider serviceProvider;

    @Resource
    private UserService2 userService2;

    @Resource
    private ShopService2 shopService2;


   public ThreadPoolRPCServer() {
    THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), 1000, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy()
    );

    this.serviceProvider = new ServiceProvider();
   }



    @Override
    public void start(int port){
        serviceProvider.registerService(userService2);
        serviceProvider.registerService(shopService2);
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务端启动了！运行在: " + serverSocket.getLocalSocketAddress());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("接收到了来自客户端：" + socket.getRemoteSocketAddress() + "的请求！");
                THREAD_POOL_EXECUTOR.execute(new WorkThread(socket, serviceProvider));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
        }
    }

    @Override
    public void stop() {

    }
}
