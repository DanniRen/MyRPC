package org.example.rpcVersion5.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion5.common.User;
import org.example.rpcVersion5.server.mapper.UserMapper5;
import org.springframework.stereotype.Service;

@Service
public class UserService5Impl extends ServiceImpl<UserMapper5, User> implements UserService5 {
    public UserService5Impl(UserMapper5 userMapper) {
        this.baseMapper = userMapper;
    }
}
