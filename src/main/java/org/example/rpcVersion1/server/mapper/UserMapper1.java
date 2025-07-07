package org.example.rpcVersion1.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.rpcVersion1.common.User;

@Mapper
public interface UserMapper1 extends BaseMapper<User> {
    
}
