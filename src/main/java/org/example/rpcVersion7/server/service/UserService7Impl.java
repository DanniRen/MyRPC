package org.example.rpcVersion7.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rpcVersion7.common.User;
import org.example.rpcVersion7.server.mapper.UserMapper7;
import org.springframework.stereotype.Service;

@Service
public class UserService7Impl extends ServiceImpl<UserMapper7, User> implements UserService7 {
    public UserService7Impl(UserMapper7 userMapper) {
        this.baseMapper = userMapper;
    }
}
