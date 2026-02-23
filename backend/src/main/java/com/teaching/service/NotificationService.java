package com.teaching.service;

import com.teaching.dto.CreateNotificationRequest;
import com.teaching.dto.NotificationDTO;

import java.util.List;

/**
 * 通知服务
 */
public interface NotificationService {

    /**
     * 创建通知
     */
    NotificationDTO createNotification(CreateNotificationRequest request);

    /**
     * 批量创建通知（给多个用户发送相同通知）
     */
    void createNotifications(List<Long> userIds, String title, String content,
                           com.teaching.entity.NotificationType type);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记用户所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取用户通知列表
     */
    List<NotificationDTO> getUserNotifications(Long userId);

    /**
     * 获取用户未读通知列表
     */
    List<NotificationDTO> getUnreadNotifications(Long userId);

    /**
     * 获取用户未读通知数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId, Long userId);
}
