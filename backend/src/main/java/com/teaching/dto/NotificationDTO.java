package com.teaching.dto;

import com.teaching.entity.NotificationType;
import lombok.Data;

/**
 * 通知DTO
 */
@Data
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private NotificationType type;
    private String typeDescription;
    private Boolean isRead;
    private String createTime;
}
