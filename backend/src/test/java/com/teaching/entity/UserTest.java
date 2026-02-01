package com.teaching.entity;

import com.teaching.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("创建用户实体应包含所有必需字段")
    void createUser_shouldHaveAllRequiredFields() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded_password");
        user.setRole(UserRole.STUDENT);
        user.setName("测试用户");
        user.setEmail("test@example.com");

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("encoded_password", user.getPassword());
        assertEquals(UserRole.STUDENT, user.getRole());
        assertEquals("测试用户", user.getName());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    @DisplayName("用户角色枚举应包含三种角色")
    void userRole_shouldHaveThreeRoles() {
        assertEquals(3, UserRole.values().length);
        assertNotNull(UserRole.ADMIN);
        assertNotNull(UserRole.TEACHER);
        assertNotNull(UserRole.STUDENT);
    }

    @Test
    @DisplayName("新用户默认删除标志应为0")
    void newUser_shouldHaveDefaultDelFlag() {
        User user = new User();
        assertEquals(0, user.getDelFlag());
    }

    @Test
    @DisplayName("新用户默认版本号应为0")
    void newUser_shouldHaveDefaultVersion() {
        User user = new User();
        assertEquals(0, user.getVersion());
    }
}
