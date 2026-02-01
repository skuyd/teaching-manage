# 阶段一：用户管理功能

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现用户 CRUD 操作，包括用户列表、创建、修改、删除功能，支持角色权限控制。

**Architecture:** RESTful API，基于角色的访问控制（RBAC），只有管理员可以管理用户。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Spring Security, JUnit 5

**依赖:** 需要先完成 `02-jwt-auth.md`

---

## Task 1: 创建用户管理 DTO

**Files:**
- Create: `backend/src/main/java/com/teaching/dto/UserDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/CreateUserRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/UpdateUserRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/PageRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/PageResponse.java`
- Test: `backend/src/test/java/com/teaching/dto/UserDTOTest.java`

**Step 1: 编写 UserDTO 测试（红灯）**

```java
// backend/src/test/java/com/teaching/dto/UserDTOTest.java
package com.teaching.dto;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    @DisplayName("从User实体转换为UserDTO")
    void fromEntity_shouldConvertCorrectly() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(UserRole.STUDENT);
        user.setName("测试用户");
        user.setEmail("test@example.com");
        user.setAvatar("avatar.png");

        UserDTO dto = UserDTO.fromEntity(user);

        assertEquals(1L, dto.getId());
        assertEquals("testuser", dto.getUsername());
        assertEquals(UserRole.STUDENT, dto.getRole());
        assertEquals("测试用户", dto.getName());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("avatar.png", dto.getAvatar());
        // 不应包含密码
        assertNull(dto.getPassword());
    }

    @Test
    @DisplayName("CreateUserRequest转换为User实体")
    void createUserRequest_shouldConvertToEntity() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRole(UserRole.TEACHER);
        request.setName("新教员");
        request.setEmail("teacher@example.com");

        User user = request.toEntity();

        assertEquals("newuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals(UserRole.TEACHER, user.getRole());
        assertEquals("新教员", user.getName());
        assertEquals("teacher@example.com", user.getEmail());
    }

    @Test
    @DisplayName("UpdateUserRequest更新User实体")
    void updateUserRequest_shouldUpdateEntity() {
        User user = new User();
        user.setId(1L);
        user.setUsername("olduser");
        user.setName("旧名字");
        user.setRole(UserRole.STUDENT);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("新名字");
        request.setRole(UserRole.TEACHER);
        request.setEmail("new@example.com");

        request.updateEntity(user);

        assertEquals("新名字", user.getName());
        assertEquals(UserRole.TEACHER, user.getRole());
        assertEquals("new@example.com", user.getEmail());
        // username不应被修改
        assertEquals("olduser", user.getUsername());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=UserDTOTest -q
```

Expected: FAIL - DTO 类不存在

**Step 3: 创建 UserDTO**

```java
// backend/src/main/java/com/teaching/dto/UserDTO.java
package com.teaching.dto;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {

    private Long id;
    private String username;
    private String password;  // 仅用于转换，不会返回给前端
    private UserRole role;
    private String name;
    private String email;
    private String avatar;
    private LocalDateTime createTime;

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        // 不设置密码，安全考虑
        dto.setRole(user.getRole());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}
```

**Step 4: 创建 CreateUserRequest**

```java
// backend/src/main/java/com/teaching/dto/CreateUserRequest.java
package com.teaching.dto;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    private String password;

    @NotNull(message = "角色不能为空")
    private UserRole role;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @Email(message = "邮箱格式不正确")
    private String email;

    public User toEntity() {
        User user = new User();
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setRole(this.role);
        user.setName(this.name);
        user.setEmail(this.email != null ? this.email : "");
        user.setAvatar("");
        return user;
    }
}
```

**Step 5: 创建 UpdateUserRequest**

```java
// backend/src/main/java/com/teaching/dto/UpdateUserRequest.java
package com.teaching.dto;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String name;

    private UserRole role;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    // 可选：修改密码
    private String password;

    public void updateEntity(User user) {
        if (this.name != null) {
            user.setName(this.name);
        }
        if (this.role != null) {
            user.setRole(this.role);
        }
        if (this.email != null) {
            user.setEmail(this.email);
        }
        if (this.avatar != null) {
            user.setAvatar(this.avatar);
        }
        // password 需要在 service 层加密处理
    }
}
```

**Step 6: 创建分页相关 DTO**

```java
// backend/src/main/java/com/teaching/dto/PageRequest.java
package com.teaching.dto;

import lombok.Data;

@Data
public class PageRequest {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;

    public int getOffset() {
        return (page - 1) * size;
    }
}
```

```java
// backend/src/main/java/com/teaching/dto/PageResponse.java
package com.teaching.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> list;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPages;

    public static <T> PageResponse<T> of(List<T> list, Long total, Integer page, Integer size) {
        PageResponse<T> response = new PageResponse<>();
        response.setList(list);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages((int) Math.ceil((double) total / size));
        return response;
    }
}
```

**Step 7: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=UserDTOTest -q
```

Expected: PASS - 3 tests passed

**Step 8: Commit**

```bash
git add backend/src/
git commit -m "feat: add user management DTOs"
```

---

## Task 2: 扩展 UserService 功能

**Files:**
- Modify: `backend/src/main/java/com/teaching/service/UserService.java`
- Modify: `backend/src/main/java/com/teaching/service/impl/UserServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/service/UserServiceExtendedTest.java`

**Step 1: 编写扩展 UserService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/UserServiceExtendedTest.java
package com.teaching.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.mapper.UserMapper;
import com.teaching.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceExtendedTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("分页查询用户列表")
    void listUsers_shouldReturnPagedUsers() {
        Page<User> page = new Page<>(1, 10);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        IPage<User> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(user));
        mockPage.setTotal(1);

        when(userMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        IPage<User> result = userService.listUsers(1, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("根据ID查询用户")
    void getUserById_shouldReturnUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userMapper.selectById(1L)).thenReturn(user);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("更新用户信息")
    void updateUser_shouldUpdateFields() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setName("旧名字");
        existingUser.setRole(UserRole.STUDENT);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("新名字");
        request.setRole(UserRole.TEACHER);

        when(userMapper.selectById(1L)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updateUser(1L, request);

        verify(userMapper).updateById(argThat(user ->
                user.getName().equals("新名字") &&
                user.getRole() == UserRole.TEACHER
        ));
    }

    @Test
    @DisplayName("更新用户时修改密码应加密")
    void updateUser_shouldEncodePassword_whenPasswordProvided() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPassword("oldEncodedPassword");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("newPassword");

        when(userMapper.selectById(1L)).thenReturn(existingUser);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updateUser(1L, request);

        verify(passwordEncoder).encode("newPassword");
        verify(userMapper).updateById(argThat(user ->
                user.getPassword().equals("newEncodedPassword")
        ));
    }

    @Test
    @DisplayName("删除用户（逻辑删除）")
    void deleteUser_shouldLogicallyDelete() {
        when(userMapper.deleteById(1L)).thenReturn(1);

        userService.deleteUser(1L);

        verify(userMapper).deleteById(1L);
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=UserServiceExtendedTest -q
```

Expected: FAIL - 新方法不存在

**Step 3: 扩展 UserService 接口**

```java
// backend/src/main/java/com/teaching/service/UserService.java
package com.teaching.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.entity.User;

public interface UserService extends IService<User> {

    User findByUsername(String username);

    void createUser(User user);

    boolean existsByUsername(String username);

    IPage<User> listUsers(int page, int size, String keyword);

    User getUserById(Long id);

    void updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
```

**Step 4: 扩展 UserServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/UserServiceImpl.java
package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.entity.User;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.UserMapper;
import com.teaching.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public void createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    @Override
    public IPage<User> listUsers(int page, int size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getName, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);

        return userMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public void updateUser(Long id, UpdateUserRequest request) {
        User user = getUserById(id);

        request.updateEntity(user);

        // 如果提供了新密码，需要加密
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=UserServiceExtendedTest -q
```

Expected: PASS - 5 tests passed

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: extend UserService with CRUD operations"
```

---

## Task 3: 创建用户管理 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/UserController.java`
- Test: `backend/src/test/java/com/teaching/controller/UserControllerTest.java`

**Step 1: 编写 UserController 测试（红灯）**

```java
// backend/src/test/java/com/teaching/controller/UserControllerTest.java
package com.teaching.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CreateUserRequest;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.security.JwtUtils;
import com.teaching.security.UserDetailsImpl;
import com.teaching.security.UserDetailsServiceImpl;
import com.teaching.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(UserRole.STUDENT);
        testUser.setName("测试用户");
    }

    @Test
    @DisplayName("管理员可以获取用户列表")
    @WithMockUser(roles = "ADMIN")
    void listUsers_shouldReturnPagedUsers_whenAdmin() throws Exception {
        IPage<User> page = new Page<>(1, 10);
        page.setRecords(List.of(testUser));
        page.setTotal(1);

        when(userService.listUsers(1, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/users")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("非管理员不能获取用户列表")
    @WithMockUser(roles = "STUDENT")
    void listUsers_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("管理员可以根据ID获取用户")
    @WithMockUser(roles = "ADMIN")
    void getUserById_shouldReturnUser_whenAdmin() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("管理员可以创建用户")
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldSucceed_whenAdmin() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRole(UserRole.STUDENT);
        request.setName("新用户");

        when(userService.existsByUsername("newuser")).thenReturn(false);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).createUser(any(User.class));
    }

    @Test
    @DisplayName("创建用户时用户名已存在应返回错误")
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturnError_whenUsernameExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setRole(UserRole.STUDENT);
        request.setName("用户");

        when(userService.existsByUsername("existinguser")).thenReturn(true);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002));
    }

    @Test
    @DisplayName("管理员可以更新用户")
    @WithMockUser(roles = "ADMIN")
    void updateUser_shouldSucceed_whenAdmin() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("新名字");
        request.setRole(UserRole.TEACHER);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("管理员可以删除用户")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldSucceed_whenAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("非管理员不能删除用户")
    @WithMockUser(roles = "TEACHER")
    void deleteUser_shouldReturn403_whenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=UserControllerTest -q
```

Expected: FAIL - UserController 不存在

**Step 3: 创建 UserController**

```java
// backend/src/main/java/com/teaching/controller/UserController.java
package com.teaching.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.teaching.common.Result;
import com.teaching.common.ResultCode;
import com.teaching.dto.*;
import com.teaching.entity.User;
import com.teaching.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResponse<UserDTO>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        IPage<User> userPage = userService.listUsers(page, size, keyword);

        List<UserDTO> userDTOs = userPage.getRecords().stream()
                .map(UserDTO::fromEntity)
                .toList();

        PageResponse<UserDTO> response = PageResponse.of(
                userDTOs,
                userPage.getTotal(),
                page,
                size
        );

        return Result.success(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(UserDTO.fromEntity(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return Result.error(ResultCode.USERNAME_EXISTS);
        }

        User user = request.toEntity();
        userService.createUser(user);

        log.info("管理员创建用户: username={}, role={}", request.getUsername(), request.getRole());
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        userService.updateUser(id, request);

        log.info("管理员更新用户: id={}", id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        log.info("管理员删除用户: id={}", id);
        return Result.success();
    }
}
```

**Step 4: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=UserControllerTest -q
```

Expected: PASS - 8 tests passed

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add UserController with CRUD endpoints"
```

---

## Task 4: 添加当前用户信息接口

**Files:**
- Modify: `backend/src/main/java/com/teaching/controller/UserController.java`
- Test: `backend/src/test/java/com/teaching/controller/CurrentUserControllerTest.java`

**Step 1: 编写当前用户接口测试（红灯）**

```java
// backend/src/test/java/com/teaching/controller/CurrentUserControllerTest.java
package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.security.JwtUtils;
import com.teaching.security.UserDetailsImpl;
import com.teaching.security.UserDetailsServiceImpl;
import com.teaching.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private User currentUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("currentuser");
        currentUser.setPassword("encoded");
        currentUser.setRole(UserRole.STUDENT);
        currentUser.setName("当前用户");

        userDetails = new UserDetailsImpl(currentUser);
    }

    @Test
    @DisplayName("获取当前用户信息")
    void getCurrentUser_shouldReturnUserInfo() throws Exception {
        when(userService.findByUsername("currentuser")).thenReturn(currentUser);

        mockMvc.perform(get("/api/users/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("currentuser"))
                .andExpect(jsonPath("$.data.name").value("当前用户"));
    }

    @Test
    @DisplayName("更新当前用户信息（不能修改角色）")
    void updateCurrentUser_shouldUpdateWithoutRole() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("新名字");
        request.setEmail("new@example.com");
        // 即使设置了 role，也不应被修改
        request.setRole(UserRole.ADMIN);

        when(userService.findByUsername("currentuser")).thenReturn(currentUser);

        mockMvc.perform(put("/api/users/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证更新时不传递 role
        verify(userService).updateUser(eq(1L), argThat(req ->
                req.getName().equals("新名字") && req.getRole() == null
        ));
    }

    @Test
    @DisplayName("未认证用户不能访问当前用户接口")
    void getCurrentUser_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=CurrentUserControllerTest -q
```

Expected: FAIL - 新端点不存在

**Step 3: 在 UserController 中添加当前用户接口**

```java
// 在 UserController.java 中添加以下方法

    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return Result.success(UserDTO.fromEntity(user));
    }

    @PutMapping("/me")
    public Result<Void> updateCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateUserRequest request) {

        // 普通用户不能修改自己的角色
        request.setRole(null);

        userService.updateUser(userDetails.getId(), request);

        log.info("用户更新个人信息: username={}", userDetails.getUsername());
        return Result.success();
    }
```

需要添加导入：
```java
import com.teaching.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
```

**Step 4: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=CurrentUserControllerTest -q
```

Expected: PASS - 3 tests passed

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add current user info endpoints"
```

---

## 验证清单

完成 Task 1-4 后，验证：

1. **所有测试通过**
   ```bash
   cd backend && mvn test -q
   ```

2. **启动应用**
   ```bash
   cd backend && mvn spring-boot:run
   ```

3. **获取管理员 Token**
   ```bash
   TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')
   ```

4. **测试用户列表接口**
   ```bash
   curl http://localhost:8080/api/users \
     -H "Authorization: Bearer $TOKEN"
   ```

5. **测试创建用户**
   ```bash
   curl -X POST http://localhost:8080/api/users \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"username":"teacher1","password":"123456","role":"TEACHER","name":"教员1"}'
   ```

6. **测试当前用户信息**
   ```bash
   curl http://localhost:8080/api/users/me \
     -H "Authorization: Bearer $TOKEN"
   ```

下一步：继续 `04-frontend-setup.md` 开始前端搭建。
