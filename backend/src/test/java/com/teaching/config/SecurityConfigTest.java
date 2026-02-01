package com.teaching.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("登录接口应允许匿名访问")
    void loginEndpoint_shouldBeAccessibleAnonymously() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isUnauthorized()); // 401 因为用户不存在，但不是 403
    }

    @Test
    @DisplayName("受保护接口应拒绝未认证访问")
    void protectedEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("健康检查接口应允许匿名访问")
    void healthEndpoint_shouldBeAccessibleAnonymously() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }
}
