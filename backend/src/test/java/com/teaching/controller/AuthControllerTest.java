package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.common.Result;
import com.teaching.controller.dto.LoginRequest;
import com.teaching.controller.dto.LoginResponse;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.security.JwtUtils;
import com.teaching.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Test User");
        testUser.setRole(UserRole.STUDENT);
        testUser.setEmail("test@example.com");
        userService.save(testUser);
    }

    @Test
    @DisplayName("登录成功应返回 JWT token")
    void login_withValidCredentials_shouldReturnJwtToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        String responseJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 验证返回的 token 有效
        Result<LoginResponse> result = objectMapper.readValue(responseJson,
                objectMapper.getTypeFactory().constructParametricType(Result.class, LoginResponse.class));
        String token = result.getData().getToken();
        assertThat(jwtUtils.validateToken(token, "testuser")).isTrue();
    }

    @Test
    @DisplayName("用户名不存在应返回 401")
    void login_withNonExistentUsername_shouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("密码错误应返回 401")
    void login_withWrongPassword_shouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("缺少必填字段应返回 400")
    void login_withMissingFields_shouldReturn400() throws Exception {
        LoginRequest request = new LoginRequest();
        // username 和 password 都为 null

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
