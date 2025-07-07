package org.example.rpcVersion3.client;

import org.example.rpcVersion3.common.Shop;
import org.example.rpcVersion3.common.User;
import org.example.rpcVersion3.server.service.ShopService3;
import org.example.rpcVersion3.server.service.UserService3;

public class TestRPCClient {
    public static void main(String[] args) {
        try {
            ClientProxy clientProxy = new ClientProxy("127.0.0.1", 8899);

            UserService3 userService = clientProxy.createProxy(UserService3.class);

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
            ShopService3 shopService = clientProxy.createProxy(ShopService3.class);
            Shop shop = shopService.getById(1L);
            System.out.println("从服务端得到的商户信息为：" + shop);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("客户端启动失败！");
        }

    }
}
