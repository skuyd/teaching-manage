package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.dto.UpdateUserPreferencesRequest;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.UserMapper;
import com.teaching.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

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

    @Override
    public void updateUserAvatar(Long id, String avatarUrl) {
        User user = getUserById(id);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    @Override
    public List<User> listStudents() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, UserRole.STUDENT)
                .orderByAsc(User::getName);
        return userMapper.selectList(wrapper);
    }

    @Override
    public List<User> listAllUsers(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getName, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectList(wrapper);
    }

    @Override
    public void updateUserPreferences(Long id, UpdateUserPreferencesRequest request) {
        User user = getUserById(id);
        request.updateEntity(user);
        userMapper.updateById(user);
    }
}
