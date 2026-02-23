package com.teaching.exception;

/**
 * 资源未找到异常（404）
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s (ID: %d) 不存在", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s (%s) 不存在", resourceName, identifier));
    }
}
