package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.NotificationDTO;
import com.teaching.service.NotificationService;
import com.teaching.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取当前用户所有通知
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<NotificationDTO>> getUserNotifications() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return Result.success(notifications);
    }

    /**
     * 获取当前用户未读通知
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public Result<List<NotificationDTO>> getUnreadNotifications() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
        return Result.success(notifications);
    }

    /**
     * 获取当前用户未读通知数量
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public Result<Integer> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return Result.success();
    }
}
