package org.example.rpcVersion4.client;

import org.example.rpcVersion4.common.Shop;
import org.example.rpcVersion4.common.User;
import org.example.rpcVersion4.server.service.ShopService4;
import org.example.rpcVersion4.server.service.UserService4;

public class TestRPCClient {
    public static void main(String[] args) {
        try {
            String host = "127.0.0.1";
            int port = 9988;
            NettyRPCClient nettyRPCClient = new NettyRPCClient(host, port);
            ClientProxy clientProxy = new ClientProxy(nettyRPCClient);

            UserService4 userService = clientProxy.createProxy(UserService4.class);

            // 根据id获取某个用户
            User user = userService.getById(1L);
            System.out.println("从服务端得到的原始的用户信息为：" + user);

            // 根据id更新某个用户
            User userNew = User.builder()
                    .id(1L)
                    .nickName("测试同学")
                    .build();
            userService.updateById(userNew);

            // 根据id获取某个用户
            user = userService.getById(1L);
            System.out.println("从服务端得到的修改后的用户信息为：" + user);

            // 根据id获取shop信息
            ShopService4 shopService = clientProxy.createProxy(ShopService4.class);
            Shop shop = shopService.getById(1L);
            System.out.println("从服务端得到的商户信息为：" + shop);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("客户端启动失败！");
        }

    }
}
