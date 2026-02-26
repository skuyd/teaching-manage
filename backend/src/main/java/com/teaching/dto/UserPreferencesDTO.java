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
     * 可选值: tech-blue, sky-blue, nature-green
     * 如果用户未设置主题，返回 null，前端将使用 localStorage 中的值
     */
    private String theme;

    public static UserPreferencesDTO fromEntity(User user) {
        UserPreferencesDTO dto = new UserPreferencesDTO();
        // 返回用户实际设置的主题，如果未设置则返回 null
        // 前端会根据 null 值决定是否使用 localStorage 中的主题
        dto.setTheme(user.getTheme());
        return dto;
    }
}
