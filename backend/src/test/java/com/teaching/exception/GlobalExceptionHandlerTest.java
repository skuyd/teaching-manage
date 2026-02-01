package com.teaching.exception;

import com.teaching.common.Result;
import com.teaching.common.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("处理业务异常")
    void handleBusinessException_shouldReturnCorrectResult() {
        BusinessException ex = new BusinessException(ResultCode.USER_NOT_FOUND);

        ResponseEntity<Result<Void>> response = handler.handleBusinessException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1001, response.getBody().getCode());
        assertEquals("用户不存在", response.getBody().getMessage());
    }

    @Test
    @DisplayName("处理自定义消息的业务异常")
    void handleBusinessException_shouldReturnCustomMessage() {
        BusinessException ex = new BusinessException(400, "自定义错误消息");

        ResponseEntity<Result<Void>> response = handler.handleBusinessException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertEquals("自定义错误消息", response.getBody().getMessage());
    }

    @Test
    @DisplayName("处理参数校验异常")
    void handleValidationException_shouldReturnFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "username", "用户名不能为空");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Result<Void>> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("用户名不能为空"));
    }

    @Test
    @DisplayName("处理通用异常")
    void handleException_shouldReturnInternalError() {
        Exception ex = new RuntimeException("未知错误");

        ResponseEntity<Result<Void>> response = handler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getCode());
    }
}
