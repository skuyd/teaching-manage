package com.teaching.dto;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    private String password;

    @NotNull(message = "角色不能为空")
    private UserRole role;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @Email(message = "邮箱格式不正确")
    private String email;

    public User toEntity() {
        User user = new User();
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setRole(this.role);
        user.setName(this.name);
        user.setEmail(this.email != null ? this.email : "");
        user.setAvatar("");
        return user;
    }
}
