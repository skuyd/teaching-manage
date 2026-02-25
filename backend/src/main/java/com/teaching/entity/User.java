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

    /**
     * 用户主题偏好
     * 可选值: tech-blue, chinese-red, nature-green
     * 默认值: tech-blue
     */
    private String theme;
}
