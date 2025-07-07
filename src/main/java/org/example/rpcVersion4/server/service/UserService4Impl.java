package org.example.rpcVersion4.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion4.common.User;
import org.example.rpcVersion4.server.mapper.UserMapper4;
import org.springframework.stereotype.Service;

@Service
public class UserService4Impl extends ServiceImpl<UserMapper4, User> implements UserService4 {
    public UserService4Impl(UserMapper4 userMapper) {
        this.baseMapper = userMapper;
    }
}
