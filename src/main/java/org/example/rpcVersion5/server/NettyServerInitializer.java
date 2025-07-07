package org.example.rpcVersion5.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import org.example.rpcVersion5.codec.JsonSerializer;
import org.example.rpcVersion5.codec.MyDecoder;
import org.example.rpcVersion5.codec.MyEncoder;
import org.example.rpcVersion5.codec.ObjectSerializer;

@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<NioSocketChannel> {
    private ServiceProvider serviceProvider;
    @Override
    protected void initChannel(NioSocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder(new ObjectSerializer()));
        pipeline.addLast(new NettyServerHandler(serviceProvider));
    }
}
