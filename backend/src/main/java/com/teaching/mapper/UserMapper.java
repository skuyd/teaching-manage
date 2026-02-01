package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM t_user WHERE username = #{username} AND del_flag = 0")
    User selectByUsername(String username);

    @Select("SELECT COUNT(*) FROM t_user WHERE username = #{username} AND del_flag = 0")
    Long countByUsername(String username);
}
