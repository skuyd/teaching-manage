package com.teaching.init;

import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        initDefaultUsers();
    }

    private void initDefaultUsers() {
        createUserIfNotExists("admin", "admin123", "系统管理员", UserRole.ADMIN);
        createUserIfNotExists("teacher", "teacher123", "默认教师", UserRole.TEACHER);
        createUserIfNotExists("student", "student123", "默认学员", UserRole.STUDENT);
    }

    private void createUserIfNotExists(String username, String password, String name, UserRole role) {
        if (!userService.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setName(name);
            user.setRole(role);
            user.setEmail(username + "@teaching.com");
            userService.save(user);
            log.info("创建默认用户: {} ({})", username, role);
        }
    }
}
