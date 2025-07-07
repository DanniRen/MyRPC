package org.example.rpcVersion5.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import org.example.rpcVersion5.common.RPCRequest;
import org.example.rpcVersion5.common.RPCResponse;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class NettyRPCClient implements RPCClient {

    private static final Bootstrap bootstrap;
    private static final NioEventLoopGroup eventLoopGroup;

    private String host;
    private int port;

    public NettyRPCClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    static {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    @Override
    public RPCResponse sendRequest(RPCRequest request) throws InterruptedException {
        try {
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            Channel channel = channelFuture.channel();
            Promise<RPCResponse> promise = new DefaultPromise<>(channel.eventLoop());

            channel.pipeline().addLast(new NettyClientHandler(promise));
            channel.writeAndFlush(request).addListener(future -> {
                if(!future.isSuccess()) {
                    promise.setFailure(future.cause());
                }
            });

            RPCResponse response = promise.await(5, TimeUnit.SECONDS) ? promise.getNow() : null;
            System.out.println("收到来自服务端返回的相应结果：" + response);
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
}
