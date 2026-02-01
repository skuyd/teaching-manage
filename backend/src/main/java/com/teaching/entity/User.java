package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import com.teaching.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private UserRole role;

    private String name;

    private String email;

    private String avatar;
}
