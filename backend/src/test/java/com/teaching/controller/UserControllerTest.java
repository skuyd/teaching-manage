package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CreateUserRequest;
import com.teaching.dto.PageResponse;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.dto.UserDTO;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testStudent;
    private User testTeacher;

    @BeforeEach
    void setUp() {
        // Create test student
        testStudent = new User();
        testStudent.setUsername("teststudent");
        testStudent.setPassword(passwordEncoder.encode("password123"));
        testStudent.setName("Test Student");
        testStudent.setRole(UserRole.STUDENT);
        testStudent.setEmail("student@test.com");
        userService.save(testStudent);

        // Create test teacher
        testTeacher = new User();
        testTeacher.setUsername("testteacher");
        testTeacher.setPassword(passwordEncoder.encode("password123"));
        testTeacher.setName("Test Teacher");
        testTeacher.setRole(UserRole.TEACHER);
        testTeacher.setEmail("teacher@test.com");
        userService.save(testTeacher);
    }

    @Test
    @DisplayName("管理员可以查询用户列表")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void listUsers_asAdmin_shouldReturnPagedUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "10")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("管理员可以根据ID查询用户")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_asAdmin_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/" + testStudent.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("teststudent"))
                .andExpect(jsonPath("$.data.name").value("Test Student"));
    }

    @Test
    @DisplayName("管理员可以创建用户")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUser_asAdmin_shouldCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setName("New User");
        request.setRole(UserRole.STUDENT);
        request.setEmail("newuser@test.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify user was created
        User createdUser = userService.findByUsername("newuser");
        assert createdUser != null;
        assert createdUser.getName().equals("New User");
    }

    @Test
    @DisplayName("创建用户时用户名已存在应返回400")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUser_withDuplicateUsername_shouldReturn400() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("teststudent");  // Already exists
        request.setPassword("password123");
        request.setName("Duplicate User");
        request.setRole(UserRole.STUDENT);
        request.setEmail("duplicate@test.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @DisplayName("管理员可以更新用户")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUser_asAdmin_shouldUpdateUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setRole(UserRole.TEACHER);

        mockMvc.perform(put("/api/users/" + testStudent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify user was updated
        User updatedUser = userService.getUserById(testStudent.getId());
        assert updatedUser.getName().equals("Updated Name");
        assert updatedUser.getRole() == UserRole.TEACHER;
    }

    @Test
    @DisplayName("管理员可以删除用户")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_asAdmin_shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + testStudent.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify user was deleted (should throw exception)
        try {
            userService.getUserById(testStudent.getId());
            assert false : "User should have been deleted";
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    @DisplayName("非管理员访问用户管理接口应返回403")
    @WithMockUser(username = "student", roles = "STUDENT")
    void accessUserManagement_asNonAdmin_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("未认证用户访问应返回401")
    void accessUserManagement_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
