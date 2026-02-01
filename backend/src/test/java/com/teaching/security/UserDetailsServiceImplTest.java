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
