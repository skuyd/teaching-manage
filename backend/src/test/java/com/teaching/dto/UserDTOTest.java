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
