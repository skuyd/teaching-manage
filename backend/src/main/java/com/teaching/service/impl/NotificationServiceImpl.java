package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.teaching.dto.CreateNotificationRequest;
import com.teaching.dto.NotificationDTO;
import com.teaching.entity.Notification;
import com.teaching.entity.NotificationType;
import com.teaching.mapper.NotificationMapper;
import com.teaching.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public NotificationDTO createNotification(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setType(request.getType());
        notification.setIsRead(false);

        notificationMapper.insert(notification);

        log.info("通知已创建: userId={}, type={}, title={}",
                request.getUserId(), request.getType(), request.getTitle());

        return convertToDTO(notification);
    }

    @Override
    @Transactional
    public void createNotifications(List<Long> userIds, String title, String content, NotificationType type) {
        List<Notification> notifications = new ArrayList<>();

        for (Long userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type);
            notification.setIsRead(false);
            notifications.add(notification);
        }

        // 批量插入
        for (Notification notification : notifications) {
            notificationMapper.insert(notification);
        }

        log.info("批量通知已创建: userCount={}, type={}, title={}",
                userIds.size(), type, title);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, true);

        int updated = notificationMapper.update(null, updateWrapper);
        if (updated == 0) {
            throw new RuntimeException("通知不存在或无权操作");
        }

        log.info("通知已标记为已读: notificationId={}, userId={}", notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .set(Notification::getIsRead, true);

        notificationMapper.update(null, updateWrapper);

        log.info("用户所有通知已标记为已读: userId={}", userId);
    }

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);

        List<Notification> notifications = notificationMapper.selectList(queryWrapper);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .orderByDesc(Notification::getCreateTime);

        List<Notification> notifications = notificationMapper.selectList(queryWrapper);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId);

        int deleted = notificationMapper.delete(queryWrapper);
        if (deleted == 0) {
            throw new RuntimeException("通知不存在或无权删除");
        }

        log.info("通知已删除: notificationId={}, userId={}", notificationId, userId);
    }

    /**
     * 转换为DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(notification, dto);
        dto.setTypeDescription(notification.getType().getDescription());

        if (notification.getCreateTime() != null) {
            dto.setCreateTime(notification.getCreateTime().format(DATE_FORMATTER));
        }

        return dto;
    }
}
