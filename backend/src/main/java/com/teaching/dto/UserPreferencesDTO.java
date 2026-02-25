package com.teaching.dto;

import com.teaching.entity.User;
import lombok.Data;

/**
 * 用户偏好设置响应 DTO
 */
@Data
public class UserPreferencesDTO {

    /**
     * 主题偏好
     * 可选值: tech-blue, chinese-red, nature-green
     */
    private String theme;

    public static UserPreferencesDTO fromEntity(User user) {
        UserPreferencesDTO dto = new UserPreferencesDTO();
        dto.setTheme(user.getTheme() != null ? user.getTheme() : "tech-blue");
        return dto;
    }
}
