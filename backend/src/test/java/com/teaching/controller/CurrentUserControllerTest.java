package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User currentUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setUsername("currentuser");
        currentUser.setPassword(passwordEncoder.encode("password123"));
        currentUser.setRole(UserRole.STUDENT);
        currentUser.setName("当前用户");
        currentUser.setEmail("current@test.com");
        userService.save(currentUser);

        userDetails = new UserDetailsImpl(currentUser);
    }

    @Test
    @DisplayName("获取当前用户信息")
    void getCurrentUser_shouldReturnUserInfo() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("currentuser"))
                .andExpect(jsonPath("$.data.name").value("当前用户"));
    }

    @Test
    @DisplayName("更新当前用户信息（不能修改角色）")
    void updateCurrentUser_shouldUpdateWithoutRole() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("新名字");
        request.setEmail("new@example.com");
        // 即使设置了 role，也不应被修改
        request.setRole(UserRole.ADMIN);

        mockMvc.perform(put("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证用户信息已更新但角色未改变
        User updatedUser = userService.getUserById(currentUser.getId());
        assert updatedUser.getName().equals("新名字");
        assert updatedUser.getEmail().equals("new@example.com");
        assert updatedUser.getRole() == UserRole.STUDENT;  // 角色未被修改
    }

    @Test
    @DisplayName("未认证用户不能访问当前用户接口")
    void getCurrentUser_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
