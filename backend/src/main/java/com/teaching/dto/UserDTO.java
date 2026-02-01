package com.teaching.dto;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {

    private Long id;
    private String username;
    private String password;  // 仅用于转换，不会返回给前端
    private UserRole role;
    private String name;
    private String email;
    private String avatar;
    private LocalDateTime createTime;

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        // 不设置密码，安全考虑
        dto.setRole(user.getRole());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}
