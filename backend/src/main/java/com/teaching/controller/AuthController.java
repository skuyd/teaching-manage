package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.controller.dto.LoginRequest;
import com.teaching.controller.dto.LoginResponse;
import com.teaching.security.JwtUtils;
import com.teaching.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails.getUsername());

            LoginResponse response = LoginResponse.builder()
                    .id(userDetails.getId())
                    .token(token)
                    .username(userDetails.getUsername())
                    .name(userDetails.getName())
                    .role(userDetails.getRole())
                    .build();

            log.info("用户登录成功: {}", userDetails.getUsername());
            return ResponseEntity.ok(Result.success(response));
        } catch (AuthenticationException e) {
            log.warn("登录失败: username={}, error={}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, "用户名或密码错误"));
        }
    }
}
