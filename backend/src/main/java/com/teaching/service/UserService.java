package com.teaching.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.dto.UpdateUserPreferencesRequest;
import com.teaching.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    User findByUsername(String username);

    List<User> listStudents();

    void createUser(User user);

    boolean existsByUsername(String username);

    IPage<User> listUsers(int page, int size, String keyword);

    User getUserById(Long id);

    void updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void updateUserAvatar(Long id, String avatarUrl);

    List<User> listAllUsers(String keyword);

    void updateUserPreferences(Long id, UpdateUserPreferencesRequest request);
}
