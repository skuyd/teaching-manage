package com.teaching.service;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("根据用户名查询用户")
    void findByUsername_shouldReturnUser() {
        User expected = new User();
        expected.setId(1L);
        expected.setUsername("testuser");
        expected.setRole(UserRole.STUDENT);

        when(userMapper.selectByUsername("testuser")).thenReturn(expected);

        User result = userService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper).selectByUsername("testuser");
    }

    @Test
    @DisplayName("用户名不存在时返回null")
    void findByUsername_shouldReturnNull_whenNotFound() {
        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        User result = userService.findByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    @DisplayName("创建用户时密码应被加密")
    void createUser_shouldEncodePassword() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("plainPassword");
        user.setRole(UserRole.STUDENT);
        user.setName("新用户");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        userService.createUser(user);

        assertEquals("encodedPassword", user.getPassword());
        verify(passwordEncoder).encode("plainPassword");
        verify(userMapper).insert(user);
    }

    @Test
    @DisplayName("检查用户名是否存在")
    void existsByUsername_shouldReturnTrue_whenExists() {
        when(userMapper.countByUsername("existinguser")).thenReturn(1L);

        boolean result = userService.existsByUsername("existinguser");

        assertTrue(result);
    }

    @Test
    @DisplayName("检查用户名不存在")
    void existsByUsername_shouldReturnFalse_whenNotExists() {
        when(userMapper.countByUsername("newuser")).thenReturn(0L);

        boolean result = userService.existsByUsername("newuser");

        assertFalse(result);
    }
}
