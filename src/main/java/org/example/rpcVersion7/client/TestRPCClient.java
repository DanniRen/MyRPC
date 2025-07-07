package org.example.rpcVersion7.client;

import org.example.rpcVersion7.common.Shop;
import org.example.rpcVersion7.common.User;
import org.example.rpcVersion7.server.service.ShopService7;
import org.example.rpcVersion7.server.service.UserService7;

public class TestRPCClient {
    public static void main(String[] args) {
        try {
            NettyRPCClient nettyRPCClient = new NettyRPCClient();
            ClientProxy clientProxy = new ClientProxy(nettyRPCClient);

            UserService7 userService = clientProxy.createProxy(UserService7.class);

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
            ShopService7 shopService = clientProxy.createProxy(ShopService7.class);
            Shop shop = shopService.getById(1L);
            System.out.println("从服务端得到的商户信息为：" + shop);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("客户端启动失败！");
        }

    }
}
