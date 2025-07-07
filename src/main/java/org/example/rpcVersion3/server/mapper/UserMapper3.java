package org.example.rpcVersion3.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.rpcVersion3.common.User;

@Mapper
public interface UserMapper3 extends BaseMapper<User> {
    
}
