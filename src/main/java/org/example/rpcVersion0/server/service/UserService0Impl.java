package org.example.rpcVersion0.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion0.common.User;
import org.example.rpcVersion0.server.mapper.UserMapper0;
import org.springframework.stereotype.Service;

@Service
public class UserService0Impl extends ServiceImpl<UserMapper0, User> implements UserService0 {
    private final UserMapper0 userMapper0;
    public UserService0Impl(UserMapper0 userMapper0) {
        this.userMapper0 = userMapper0;
    }
}
