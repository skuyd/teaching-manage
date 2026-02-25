package com.teaching.dto;

import com.teaching.entity.User;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户偏好设置请求 DTO
 */
@Data
public class UpdateUserPreferencesRequest {

    /**
     * 主题偏好
     * 可选值: tech-blue, chinese-red, nature-green
     */
    @Pattern(
        regexp = "tech-blue|chinese-red|nature-green",
        message = "主题必须是以下值之一: tech-blue, chinese-red, nature-green"
    )
    private String theme;

    /**
     * 更新用户实体
     *
     * @param user 用户实体
     */
    public void updateEntity(User user) {
        if (this.theme != null) {
            user.setTheme(this.theme);
        }
    }
}
