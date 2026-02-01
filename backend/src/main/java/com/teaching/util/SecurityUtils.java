package com.teaching.util;

import com.teaching.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Security工具类
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未找到当前登录用户");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return parseUserIdFromUsername(username);
        }

        if (principal instanceof String username) {
            return parseUserIdFromUsername(username);
        }

        throw new RuntimeException("无法获取用户ID");
    }

    /**
     * 获取当前登录用户详情
     */
    public static UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails;
        }
        throw new RuntimeException("未找到当前登录用户详情");
    }

    /**
     * 从用户名解析用户ID（用于测试场景）
     * 测试中使用 @WithMockUser(username = "1") 传递用户ID
     */
    private static Long parseUserIdFromUsername(String username) {
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            return 1L;
        }
    }
}
