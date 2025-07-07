package org.example.rpcVersion5.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.rpcVersion5.server.service.ShopService5;
import org.example.rpcVersion5.server.service.UserService5;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.IOException;

@Component
public class NettyRPCServer5 implements RPCServer5 {

    private final ServiceProvider serviceProvider;

    @Resource
    private UserService5 userService5;
    @Resource
    private ShopService5 shopService5;

    public NettyRPCServer5(){
        this.serviceProvider = new ServiceProvider();
    }

    @Override
    public void start(int port) throws IOException, ClassNotFoundException {
        serviceProvider.registerService(userService5);
        serviceProvider.registerService(shopService5);

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("Netty服务端启动了");
            // 死循环监听，一直到通道关闭连接才结束
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {

    }
}
