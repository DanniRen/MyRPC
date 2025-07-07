package org.example.rpcVersion1.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion1.common.User;
import org.example.rpcVersion1.server.mapper.UserMapper1;
import org.springframework.stereotype.Service;

@Service
public class UserService1Impl extends ServiceImpl<UserMapper1, User> implements UserService1 {
    public UserService1Impl(UserMapper1 userMapper) {
        this.baseMapper = userMapper;
    }
}
