package com.teaching.init;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationArguments args;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    }

    @Test
    @DisplayName("当所有用户不存在时应创建3个默认用户")
    void run_whenAllUsersNotExist_shouldCreateAllDefaultUsers() throws Exception {
        when(userService.existsByUsername(anyString())).thenReturn(false);

        dataInitializer.run(args);

        verify(userService, times(3)).existsByUsername(anyString());
        verify(userService, times(3)).save(userCaptor.capture());

        List<User> createdUsers = userCaptor.getAllValues();
        assertThat(createdUsers).hasSize(3);
        assertThat(createdUsers).extracting(User::getUsername)
                .containsExactlyInAnyOrder("admin", "teacher", "student");
        assertThat(createdUsers).extracting(User::getRole)
                .containsExactlyInAnyOrder(UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT);
    }

    @Test
    @DisplayName("当部分用户已存在时只创建不存在的用户")
    void run_whenSomeUsersExist_shouldCreateOnlyMissingUsers() throws Exception {
        when(userService.existsByUsername("admin")).thenReturn(true);
        when(userService.existsByUsername("teacher")).thenReturn(false);
        when(userService.existsByUsername("student")).thenReturn(false);

        dataInitializer.run(args);

        verify(userService, times(3)).existsByUsername(anyString());
        verify(userService, times(2)).save(userCaptor.capture());

        List<User> createdUsers = userCaptor.getAllValues();
        assertThat(createdUsers).hasSize(2);
        assertThat(createdUsers).extracting(User::getUsername)
                .containsExactlyInAnyOrder("teacher", "student");
    }

    @Test
    @DisplayName("当所有用户已存在时不应创建任何用户")
    void run_whenAllUsersExist_shouldNotCreateAnyUser() throws Exception {
        when(userService.existsByUsername(anyString())).thenReturn(true);

        dataInitializer.run(args);

        verify(userService, times(3)).existsByUsername(anyString());
        verify(userService, never()).save(any(User.class));
    }

    @Test
    @DisplayName("创建的用户应包含正确的默认值")
    void run_shouldCreateUsersWithCorrectDefaults() throws Exception {
        when(userService.existsByUsername(anyString())).thenReturn(false);

        dataInitializer.run(args);

        verify(userService, times(3)).save(userCaptor.capture());

        List<User> createdUsers = userCaptor.getAllValues();
        for (User user : createdUsers) {
            assertThat(user.getPassword()).isEqualTo("encoded_password");
            assertThat(user.getName()).isNotBlank();
            assertThat(user.getEmail()).endsWith("@teaching.com");
        }
    }
}
