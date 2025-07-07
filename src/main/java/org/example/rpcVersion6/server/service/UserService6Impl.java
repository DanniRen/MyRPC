package org.example.rpcVersion6.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion6.common.User;
import org.example.rpcVersion6.server.mapper.UserMapper6;
import org.springframework.stereotype.Service;

@Service
public class UserService6Impl extends ServiceImpl<UserMapper6, User> implements UserService6 {
    public UserService6Impl(UserMapper6 userMapper) {
        this.baseMapper = userMapper;
    }
}
