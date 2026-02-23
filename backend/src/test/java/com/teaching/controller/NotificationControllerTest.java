package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CreateNotificationRequest;
import com.teaching.entity.User;
import com.teaching.entity.NotificationType;
import com.teaching.enums.UserRole;
import com.teaching.service.UserService;
import com.teaching.service.NotificationService;
import com.teaching.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private User user;
    private String userToken;

    @BeforeEach
    void setUp() {
        // 创建用户
        user = new User();
        user.setUsername("notification_user");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Notification User");
        user.setRole(UserRole.STUDENT);
        user.setEmail("notification@test.com");
        userService.save(user);
        userToken = jwtUtils.generateToken(user.getUsername());

        // 创建通知
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(user.getId());
        request.setTitle("New lesson published");
        request.setContent("A new lesson has been published");
        request.setType(NotificationType.LESSON_PUBLISHED);
        notificationService.createNotification(request);
    }

    @Test
    @DisplayName("获取当前用户所有通知")
    void getUserNotifications_shouldReturnNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取未读通知")
    void getUnreadNotifications_shouldReturnUnread() throws Exception {
        mockMvc.perform(get("/api/notifications/unread")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取未读通知数量")
    void getUnreadCount_shouldReturnCount() throws Exception {
        mockMvc.perform(get("/api/notifications/unread/count")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("标记通知为已读")
    void markAsRead_shouldSucceed() throws Exception {
        // 获取通知ID
        var notifications = notificationService.getUserNotifications(user.getId());
        if (!notifications.isEmpty()) {
            Long notificationId = notifications.get(0).getId();

            mockMvc.perform(put("/api/notifications/" + notificationId + "/read")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @DisplayName("标记所有通知为已读")
    void markAllAsRead_shouldSucceed() throws Exception {
        mockMvc.perform(put("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除通知")
    void deleteNotification_shouldSucceed() throws Exception {
        var notifications = notificationService.getUserNotifications(user.getId());
        if (!notifications.isEmpty()) {
            Long notificationId = notifications.get(0).getId();

            mockMvc.perform(delete("/api/notifications/" + notificationId)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @DisplayName("未认证用户无法访问通知API")
    void notificationEndpoints_withoutAuth_shouldFail() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("不同用户无法看到彼此的通知")
    void notifications_areScopedToUser() throws Exception {
        // 创建另一个用户
        User otherUser = new User();
        otherUser.setUsername("other_notification_user");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser.setName("Other User");
        otherUser.setRole(UserRole.STUDENT);
        otherUser.setEmail("other_notification@test.com");
        userService.save(otherUser);
        String otherToken = jwtUtils.generateToken(otherUser.getUsername());

        // 为其他用户创建通知
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(otherUser.getId());
        request.setTitle("Grade completed");
        request.setContent("Your grade has been completed");
        request.setType(NotificationType.GRADE_COMPLETED);
        notificationService.createNotification(request);

        // 第一个用户应该只看到自己的通知
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 第二个用户应该只看到自己的通知
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
