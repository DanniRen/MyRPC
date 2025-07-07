package org.example.rpcVersion3.server;

import org.example.rpcVersion3.server.service.ShopService3;
import org.example.rpcVersion3.server.service.UserService3;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class NIORPCServer implements RPCServer3 {

    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR;
    private final ServiceProvider serviceProvider;

    @Resource
    private UserService3 userService3;

    @Resource
    private ShopService3 shopService3;

    public NIORPCServer() {
        THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                1000, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());
        this.serviceProvider = new ServiceProvider();
    }

    @Override
    public void start(int port) throws IOException, ClassNotFoundException {
        serviceProvider.registerService(userService3);
        serviceProvider.registerService(shopService3);

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        SocketAddress socketAddress = new InetSocketAddress(port);
        serverSocketChannel.socket().bind(socketAddress);

        Selector selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 判断是否有事件发生
            if (selector.select(1000) == 0) {
                System.out.println(Thread.currentThread().getName() + "等待客户端连接ing");
                continue;
            }
            // 如果有事件发生，则获取到所有的selectionKey
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // 当发生的事件是连接事件，则获取连接的客户端channel，并且注册到selector中
                if(key.isAcceptable()) {
                    // 获取关联的socketChannel
                    SocketChannel clientChannel = serverSocketChannel.accept();
                    // 这里注意调用register之前，必须将socketChannel设置为非阻塞状态
                    clientChannel.configureBlocking(false);

                    // 注册读事件，并绑定一个AtomicBoolean标志，初始为false
                    AtomicBoolean processing = new AtomicBoolean(false);
                    clientChannel.register(selector, SelectionKey.OP_READ, processing);
                    System.out.println(Thread.currentThread().getName() + "客户端连接：" + clientChannel.getRemoteAddress());

                } else if (key.isReadable()) {
                    // 当通道内有数据可读时，先检查一下是否有线程已经在处理这个通道了
                    AtomicBoolean processing = (AtomicBoolean) key.attachment();
                    // processing为false时，新建一个线程去处理
                    if(processing.compareAndSet(false, true)) {
                        // 当发生的事件为读取数据事件时，就反向获取关联的socketChannel
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        THREAD_POOL_EXECUTOR.execute(() -> {
                            System.out.println(Thread.currentThread().getName() + "start to process clientChannel");
                            try {
                                new WorkThread(clientChannel, serviceProvider).run();
                                key.cancel();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            } finally {
                                processing.set(false);
                            }
                        });
                    }
                    else {
                        System.out.println(Thread.currentThread().getName() + "clientChannel is processing");
                    }
                }
                iterator.remove();
            }

        }
    }

    @Override
    public void stop() {

    }
}
