package org.example;

import org.example.rpcVersion6.server.NettyRPCServer6;
import org.example.rpcVersion7.server.NettyRPCServer7;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
@MapperScan("org.example.*.server.mapper")
public class RPCServerApplication {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        ConfigurableApplicationContext context = SpringApplication.run(RPCServerApplication.class, args);
        NettyRPCServer7 rpcServer = context.getBean(NettyRPCServer7.class);
        try {
            rpcServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}