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
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        Page<User> mockPage = new Page<>(1, 10);
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
