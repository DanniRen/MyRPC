package org.example.rpcVersion4.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import org.example.rpcVersion4.server.service.ShopService4;
import org.example.rpcVersion4.server.service.UserService4;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.IOException;

@Component
public class NettyRPCServer implements RPCServer4{

    private final ServiceProvider serviceProvider;

    @Resource
    private UserService4 userService4;
    @Resource
    private ShopService4 shopService4;

    public NettyRPCServer(){
        this.serviceProvider = new ServiceProvider();
    }

    @Override
    public void start(int port) throws IOException, ClassNotFoundException {
        serviceProvider.registerService(userService4);
        serviceProvider.registerService(shopService4);

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
