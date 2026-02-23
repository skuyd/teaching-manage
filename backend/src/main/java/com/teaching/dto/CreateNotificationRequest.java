package com.teaching.dto;

import com.teaching.entity.NotificationType;
import lombok.Data;

/**
 * 创建通知请求
 */
@Data
public class CreateNotificationRequest {
    private Long userId;
    private String title;
    private String content;
    private NotificationType type;
}
