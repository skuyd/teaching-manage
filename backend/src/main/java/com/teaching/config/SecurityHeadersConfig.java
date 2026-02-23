package com.teaching.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeadersConfig implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 防止MIME类型嗅探
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // 防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // XSS保护
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // 强制HTTPS（如果使用HTTPS）
        // httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // 控制引用信息
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // 内容安全策略
        httpResponse.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';");

        // 权限策略
        httpResponse.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

        chain.doFilter(request, response);
    }
}
