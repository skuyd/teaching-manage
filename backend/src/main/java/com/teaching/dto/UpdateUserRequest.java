package com.teaching.dto;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String name;

    private UserRole role;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    // 可选：修改密码
    private String password;

    public void updateEntity(User user) {
        if (this.name != null) {
            user.setName(this.name);
        }
        if (this.role != null) {
            user.setRole(this.role);
        }
        if (this.email != null) {
            user.setEmail(this.email);
        }
        if (this.avatar != null) {
            user.setAvatar(this.avatar);
        }
        // password 需要在 service 层加密处理
    }
}
