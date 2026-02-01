package com.teaching.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    @DisplayName("成功响应应返回正确的code和message")
    void success_shouldReturnCorrectCodeAndMessage() {
        Result<String> result = Result.success("test data");

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test data", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("无数据成功响应")
    void success_shouldWorkWithoutData() {
        Result<Void> result = Result.success();

        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("失败响应应返回正确的code和message")
    void error_shouldReturnCorrectCodeAndMessage() {
        Result<Void> result = Result.error(400, "参数错误");

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("使用ResultCode创建响应")
    void error_shouldWorkWithResultCode() {
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED);

        assertEquals(401, result.getCode());
        assertEquals("未授权", result.getMessage());
    }
}
