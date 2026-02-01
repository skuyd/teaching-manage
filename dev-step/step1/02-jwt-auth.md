# 阶段一：JWT 认证实现

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现 JWT Token 认证，包括登录、Token 生成和验证、Security 配置。

**Architecture:** Spring Security + JWT，无状态认证，Token 存储在客户端。

**Tech Stack:** Spring Security 6, JJWT 0.12, JUnit 5, Mockito

**依赖:** 需要先完成 `01-backend-setup.md`

---

## Task 1: 实现 JWT 工具类

**Files:**
- Create: `backend/src/main/java/com/teaching/security/JwtUtils.java`
- Test: `backend/src/test/java/com/teaching/security/JwtUtilsTest.java`

**Step 1: 编写 JwtUtils 测试（红灯）**

```java
// backend/src/test/java/com/teaching/security/JwtUtilsTest.java
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
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=JwtUtilsTest -q
```

Expected: FAIL - JwtUtils 类不存在

**Step 3: 创建 JwtUtils**

```java
// backend/src/main/java/com/teaching/security/JwtUtils.java
package com.teaching.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token, String username) {
        try {
            Claims claims = parseClaims(token);
            String tokenUsername = claims.getSubject();
            Date expiration = claims.getExpiration();

            return tokenUsername.equals(username) && !expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

**Step 4: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=JwtUtilsTest -q
```

Expected: PASS - 6 tests passed

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add JWT utility class with TDD"
```

---

## Task 2: 创建 UserDetails 实现

**Files:**
- Create: `backend/src/main/java/com/teaching/security/UserDetailsImpl.java`
- Create: `backend/src/main/java/com/teaching/security/UserDetailsServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/security/UserDetailsServiceImplTest.java`

**Step 1: 编写 UserDetailsService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/security/UserDetailsServiceImplTest.java
package com.teaching.security;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserService userService;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(userService);
    }

    @Test
    @DisplayName("加载存在的用户")
    void loadUserByUsername_shouldReturnUserDetails() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.STUDENT);
        user.setName("测试用户");

        when(userService.findByUsername("testuser")).thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    @DisplayName("加载不存在的用户应抛出异常")
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userService.findByUsername("nonexistent")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    @DisplayName("UserDetailsImpl应包含用户ID")
    void userDetailsImpl_shouldContainUserId() {
        User user = new User();
        user.setId(123L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.TEACHER);
        user.setName("教员");

        when(userService.findByUsername("testuser")).thenReturn(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertInstanceOf(UserDetailsImpl.class, userDetails);
        UserDetailsImpl impl = (UserDetailsImpl) userDetails;
        assertEquals(123L, impl.getId());
        assertEquals(UserRole.TEACHER, impl.getRole());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=UserDetailsServiceImplTest -q
```

Expected: FAIL - UserDetailsImpl 和 UserDetailsServiceImpl 不存在

**Step 3: 创建 UserDetailsImpl**

```java
// backend/src/main/java/com/teaching/security/UserDetailsImpl.java
package com.teaching.security;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String name;
    private final UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.name = user.getName();
        this.role = user.getRole();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

**Step 4: 创建 UserDetailsServiceImpl**

```java
// backend/src/main/java/com/teaching/security/UserDetailsServiceImpl.java
package com.teaching.security;

import com.teaching.entity.User;
import com.teaching.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new UserDetailsImpl(user);
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=UserDetailsServiceImplTest -q
```

Expected: PASS - 3 tests passed

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add UserDetailsImpl and UserDetailsServiceImpl"
```

---

## Task 3: 创建 JWT 认证过滤器

**Files:**
- Create: `backend/src/main/java/com/teaching/security/JwtAuthenticationFilter.java`
- Test: `backend/src/test/java/com/teaching/security/JwtAuthenticationFilterTest.java`

**Step 1: 编写 JwtAuthenticationFilter 测试（红灯）**

```java
// backend/src/test/java/com/teaching/security/JwtAuthenticationFilterTest.java
package com.teaching.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("没有Authorization头时应继续过滤链")
    void doFilterInternal_shouldContinue_whenNoAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Authorization头不以Bearer开头时应继续过滤链")
    void doFilterInternal_shouldContinue_whenNotBearerToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("有效Token应设置SecurityContext")
    void doFilterInternal_shouldSetAuthentication_whenValidToken() throws Exception {
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.extractUsername(token)).thenReturn("testuser");
        when(jwtUtils.validateToken(token, "testuser")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("无效Token应不设置SecurityContext")
    void doFilterInternal_shouldNotSetAuthentication_whenInvalidToken() throws Exception {
        String token = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.extractUsername(token)).thenThrow(new RuntimeException("Invalid token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=JwtAuthenticationFilterTest -q
```

Expected: FAIL - JwtAuthenticationFilter 不存在

**Step 3: 创建 JwtAuthenticationFilter**

```java
// backend/src/main/java/com/teaching/security/JwtAuthenticationFilter.java
package com.teaching.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                String username = jwtUtils.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtils.validateToken(jwt, username)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
```

**Step 4: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=JwtAuthenticationFilterTest -q
```

Expected: PASS - 4 tests passed

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add JWT authentication filter"
```

---

## Task 4: 配置 Spring Security

**Files:**
- Create: `backend/src/main/java/com/teaching/config/SecurityConfig.java`
- Test: `backend/src/test/java/com/teaching/config/SecurityConfigTest.java`

**Step 1: 编写 SecurityConfig 测试（红灯）**

```java
// backend/src/test/java/com/teaching/config/SecurityConfigTest.java
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
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=SecurityConfigTest -q
```

Expected: FAIL - SecurityConfig 不存在或配置不正确

**Step 3: 创建 SecurityConfig**

```java
// backend/src/main/java/com/teaching/config/SecurityConfig.java
package com.teaching.config;

import com.teaching.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated())
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Step 4: 创建健康检查 Controller**

```java
// backend/src/main/java/com/teaching/controller/HealthController.java
package com.teaching.controller;

import com.teaching.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=SecurityConfigTest -q
```

Expected: PASS - 3 tests passed

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add Spring Security configuration"
```

---

## Task 5: 实现认证 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/AuthController.java`
- Create: `backend/src/main/java/com/teaching/dto/LoginRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/LoginResponse.java`
- Create: `backend/src/main/java/com/teaching/dto/RegisterRequest.java`
- Test: `backend/src/test/java/com/teaching/controller/AuthControllerTest.java`

**Step 1: 编写 AuthController 测试（红灯）**

```java
// backend/src/test/java/com/teaching/controller/AuthControllerTest.java
package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.LoginRequest;
import com.teaching.dto.RegisterRequest;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.security.JwtUtils;
import com.teaching.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("登录成功应返回Token")
    void login_shouldReturnToken_whenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(UserRole.STUDENT);
        user.setName("测试用户");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(jwtUtils.generateToken("testuser")).thenReturn("generated.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("generated.jwt.token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("登录失败应返回401")
    void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("注册成功应返回用户信息")
    void register_shouldReturnUser_whenUsernameNotExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setName("新用户");
        request.setEmail("new@example.com");

        when(userService.existsByUsername("newuser")).thenReturn(false);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).createUser(any(User.class));
    }

    @Test
    @DisplayName("注册时用户名已存在应返回错误")
    void register_shouldReturnError_whenUsernameExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setName("用户");

        when(userService.existsByUsername("existinguser")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002));

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("注册时参数校验失败应返回400")
    void register_shouldReturn400_whenValidationFails() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");  // 空用户名
        request.setPassword("123");  // 密码太短

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=AuthControllerTest -q
```

Expected: FAIL - DTO 类和 AuthController 不存在

**Step 3: 创建 DTO 类**

```java
// backend/src/main/java/com/teaching/dto/LoginRequest.java
package com.teaching.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

```java
// backend/src/main/java/com/teaching/dto/LoginResponse.java
package com.teaching.dto;

import com.teaching.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String name;
    private UserRole role;
}
```

```java
// backend/src/main/java/com/teaching/dto/RegisterRequest.java
package com.teaching.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

**Step 4: 创建 AuthController**

```java
// backend/src/main/java/com/teaching/controller/AuthController.java
package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.common.ResultCode;
import com.teaching.dto.LoginRequest;
import com.teaching.dto.LoginResponse;
import com.teaching.dto.RegisterRequest;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.security.JwtUtils;
import com.teaching.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userService.findByUsername(request.getUsername());
            String token = jwtUtils.generateToken(user.getUsername());

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .role(user.getRole())
                    .build();

            return Result.success(response);
        } catch (BadCredentialsException e) {
            log.warn("登录失败: 用户名或密码错误, username={}", request.getUsername());
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return Result.error(ResultCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setEmail(request.getEmail() != null ? request.getEmail() : "");
        user.setRole(UserRole.STUDENT);  // 默认注册为学员

        userService.createUser(user);

        log.info("用户注册成功: username={}", request.getUsername());
        return Result.success();
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=AuthControllerTest -q
```

Expected: PASS - 5 tests passed

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add AuthController with login and register"
```

---

## Task 6: 创建数据初始化器

**Files:**
- Create: `backend/src/main/java/com/teaching/config/DataInitializer.java`
- Test: `backend/src/test/java/com/teaching/config/DataInitializerTest.java`

**Step 1: 编写 DataInitializer 测试（红灯）**

```java
// backend/src/test/java/com/teaching/config/DataInitializerTest.java
package com.teaching.config;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserService userService;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(userService);
    }

    @Test
    @DisplayName("管理员不存在时应创建默认管理员")
    void run_shouldCreateAdmin_whenNotExists() throws Exception {
        when(userService.existsByUsername("admin")).thenReturn(false);

        dataInitializer.run();

        verify(userService).createUser(argThat(user ->
                user.getUsername().equals("admin") &&
                user.getRole() == UserRole.ADMIN
        ));
    }

    @Test
    @DisplayName("管理员已存在时不应创建")
    void run_shouldNotCreateAdmin_whenExists() throws Exception {
        when(userService.existsByUsername("admin")).thenReturn(true);

        dataInitializer.run();

        verify(userService, never()).createUser(any(User.class));
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=DataInitializerTest -q
```

Expected: FAIL - DataInitializer 不存在

**Step 3: 创建 DataInitializer**

```java
// backend/src/main/java/com/teaching/config/DataInitializer.java
package com.teaching.config;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        initDefaultAdmin();
    }

    private void initDefaultAdmin() {
        if (!userService.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");  // 会被 UserService 加密
            admin.setName("系统管理员");
            admin.setRole(UserRole.ADMIN);
            admin.setEmail("");

            userService.createUser(admin);
            log.info("默认管理员账号创建成功: admin/admin123");
        }
    }
}
```

**Step 4: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=DataInitializerTest -q
```

Expected: PASS - 2 tests passed

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add DataInitializer with default admin"
```

---

## 验证清单

完成 Task 1-6 后，验证：

1. **所有测试通过**
   ```bash
   cd backend && mvn test -q
   ```

2. **启动应用**
   ```bash
   cd backend && mvn spring-boot:run
   ```

3. **测试登录接口**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

   Expected: 返回 token

4. **测试受保护接口**
   ```bash
   curl http://localhost:8080/api/users
   ```

   Expected: 401 Unauthorized

5. **使用 Token 访问**
   ```bash
   curl http://localhost:8080/api/users \
     -H "Authorization: Bearer <token>"
   ```

下一步：继续 `03-user-management.md` 完成用户管理功能。
