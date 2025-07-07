package org.example.rpcVersion2.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.rpcVersion2.common.User;

@Mapper
public interface UserMapper2 extends BaseMapper<User> {
    
}
