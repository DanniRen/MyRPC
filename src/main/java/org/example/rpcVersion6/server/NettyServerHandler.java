package org.example.rpcVersion6.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import org.example.rpcVersion6.common.RPCRequest;
import org.example.rpcVersion6.common.RPCResponse;
import org.example.rpcVersion6.register.NacosServiceRegister;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@AllArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<RPCRequest> {
    private ServiceProvider serviceProvider;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RPCResponse getResponse(RPCRequest request) {
        try {
            Object service = serviceProvider.getService(request.getInterfaceName());
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object response = method.invoke(service, request.getParams());
            return RPCResponse.success(response);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println(Thread.currentThread().getName() + "方法执行不成功！");
            return RPCResponse.fail();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequest rpcRequest) throws Exception {
        System.out.println("收到来自客户端的消息： " + rpcRequest);

        try {
            RPCResponse response = getResponse(rpcRequest);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
