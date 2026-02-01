package com.teaching.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        // 测试用的 secret 和 expiration
        jwtUtils = new JwtUtils(
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
            3600000L  // 1 hour
        );
    }

    @Test
    @DisplayName("生成的Token不为空")
    void generateToken_shouldReturnNonEmptyToken() {
        String token = jwtUtils.generateToken("testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("从Token中提取用户名")
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtUtils.generateToken("testuser");

        String username = jwtUtils.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("验证有效Token")
    void validateToken_shouldReturnTrue_forValidToken() {
        String token = jwtUtils.generateToken("testuser");

        boolean isValid = jwtUtils.validateToken(token, "testuser");

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Token用户名不匹配时验证失败")
    void validateToken_shouldReturnFalse_forWrongUsername() {
        String token = jwtUtils.generateToken("testuser");

        boolean isValid = jwtUtils.validateToken(token, "wronguser");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("无效Token验证失败")
    void validateToken_shouldReturnFalse_forInvalidToken() {
        boolean isValid = jwtUtils.validateToken("invalid.token.here", "testuser");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("过期Token验证失败")
    void validateToken_shouldReturnFalse_forExpiredToken() {
        // 创建一个立即过期的 JwtUtils
        JwtUtils expiredJwtUtils = new JwtUtils(
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
            -1000L  // 已过期
        );

        String token = expiredJwtUtils.generateToken("testuser");

        boolean isValid = expiredJwtUtils.validateToken(token, "testuser");

        assertFalse(isValid);
    }
}
