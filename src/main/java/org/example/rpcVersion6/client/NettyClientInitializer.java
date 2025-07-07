package org.example.rpcVersion6.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.example.rpcVersion6.codec.MyDecoder;
import org.example.rpcVersion6.codec.MyEncoder;
import org.example.rpcVersion6.codec.ObjectSerializer;

public class NettyClientInitializer extends ChannelInitializer<NioSocketChannel> {

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder(new ObjectSerializer()));
    }
}
