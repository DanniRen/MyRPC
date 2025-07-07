package org.example.rpcVersion6.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.rpcVersion6.register.NacosServiceRegister;
import org.example.rpcVersion6.server.service.ShopService6;
import org.example.rpcVersion6.server.service.UserService6;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.IOException;

@Component
public class NettyRPCServer6 implements RPCServer6 {

    private final ServiceProvider serviceProvider;

    @Resource
    private UserService6 userService6;
    @Resource
    private ShopService6 shopService6;

    public NettyRPCServer6(){
        this.serviceProvider = new ServiceProvider("localhost", 9988);
    }

    @Override
    public void start(int port) throws IOException, ClassNotFoundException {
        serviceProvider.registerService(userService6);
        serviceProvider.registerService(shopService6);

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
