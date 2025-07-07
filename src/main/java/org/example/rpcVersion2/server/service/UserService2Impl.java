package org.example.rpcVersion2.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion2.common.User;
import org.example.rpcVersion2.server.mapper.UserMapper2;
import org.springframework.stereotype.Service;

@Service
public class UserService2Impl extends ServiceImpl<UserMapper2, User> implements UserService2 {
    public UserService2Impl(UserMapper2 userMapper) {
        this.baseMapper = userMapper;
    }
}
