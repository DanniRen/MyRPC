package org.example.rpcVersion3.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion3.common.User;
import org.example.rpcVersion3.server.mapper.UserMapper3;
import org.springframework.stereotype.Service;

@Service
public class UserService3Impl extends ServiceImpl<UserMapper3, User> implements UserService3 {
    public UserService3Impl(UserMapper3 userMapper) {
        this.baseMapper = userMapper;
    }
}
