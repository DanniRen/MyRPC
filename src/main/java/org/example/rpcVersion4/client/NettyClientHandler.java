package org.example.rpcVersion4.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Promise;
import org.example.rpcVersion4.common.RPCResponse;

public class NettyClientHandler extends SimpleChannelInboundHandler<RPCResponse> {

    private final Promise<RPCResponse> promise;

    public NettyClientHandler(Promise<RPCResponse> promise){
        this.promise = promise;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse rpcResponse) throws Exception {
        promise.setSuccess(rpcResponse);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        promise.setFailure(cause);
        cause.printStackTrace();
        ctx.close();
    }
}
