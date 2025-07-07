package org.example.rpcVersion1.client;

import org.example.rpcVersion1.common.User;
import org.example.rpcVersion1.server.service.UserService1;

public class RPCClient {
    public static void main(String[] args) {
        try {
            ClientProxy clientProxy = new ClientProxy("127.0.0.1", 8899);

            UserService1 proxy = clientProxy.createProxy(UserService1.class);

            // 根据id获取某个用户
            User user = proxy.getById(1L);
            System.out.println("从服务端得到的原始的用户信息为：" + user);

            // 根据id更新某个用户
            User userNew = User.builder()
                    .id(1L)
                    .nickName("测试用户")
                    .build();
            boolean b = proxy.updateById(userNew);

            // 根据id获取某个用户
            user = proxy.getById(1L);
            System.out.println("从服务端得到的修改后的用户信息为：" + user);


        } catch (Exception e) {
            System.out.println("客户端启动失败！");
        }

    }
}
