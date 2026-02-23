package com.teaching.exception;

/**
 * 请求频率超限异常（429）
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
