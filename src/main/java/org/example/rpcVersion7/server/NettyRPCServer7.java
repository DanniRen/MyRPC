package org.example.rpcVersion7.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.example.rpcVersion7.server.service.ShopService7;
import org.example.rpcVersion7.server.service.UserService7;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Data
public class NettyRPCServer7 implements RPCServer7 {

    private final ServiceProvider serviceProvider;

    private final int port;

    @Resource
    private UserService7 userService7;
    @Resource
    private ShopService7 shopService7;

    @Autowired
    public NettyRPCServer7(@Value("${rpc.server.port}") int port){
        this.port = port;
        this.serviceProvider = new ServiceProvider("localhost", this.port);
    }

    @Override
    public void start() throws IOException, ClassNotFoundException {
        serviceProvider.registerService(userService7);
        serviceProvider.registerService(shopService7);

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(serviceProvider));
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("Netty服务端启动了，端口号为" + port);
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
