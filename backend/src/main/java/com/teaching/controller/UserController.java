package com.teaching.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.teaching.common.Result;
import com.teaching.dto.CreateUserRequest;
import com.teaching.dto.PageRequest;
import com.teaching.dto.PageResponse;
import com.teaching.dto.UpdateUserRequest;
import com.teaching.dto.UserDTO;
import com.teaching.entity.User;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<PageResponse<UserDTO>>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        IPage<User> userPage = userService.listUsers(page, size, keyword);
        List<UserDTO> userDTOs = userPage.getRecords().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        PageResponse<UserDTO> pageResponse = PageResponse.of(
                userDTOs,
                userPage.getTotal(),
                page,
                size
        );
        return ResponseEntity.ok(Result.success(pageResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<UserDTO>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserDTO userDTO = UserDTO.fromEntity(user);
        return ResponseEntity.ok(Result.success(userDTO));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(400, "用户名已存在"));
        }
        User user = request.toEntity();
        userService.createUser(user);
        return ResponseEntity.ok(Result.success());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        userService.updateUser(id, request);
        return ResponseEntity.ok(Result.success());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Result.success());
    }

    @GetMapping("/me")
    public ResponseEntity<Result<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(Result.success(UserDTO.fromEntity(user)));
    }

    @PutMapping("/me")
    public ResponseEntity<Result<Void>> updateCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateUserRequest request) {

        // 普通用户不能修改自己的角色
        request.setRole(null);

        userService.updateUser(userDetails.getId(), request);

        log.info("用户更新个人信息: username={}", userDetails.getUsername());
        return ResponseEntity.ok(Result.success());
    }
}
