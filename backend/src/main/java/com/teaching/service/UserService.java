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
