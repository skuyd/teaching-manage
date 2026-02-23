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
import com.teaching.dto.UserImportResultDTO;
import com.teaching.service.FileService;
import com.teaching.service.UserExcelService;
import com.teaching.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;
    private final UserExcelService userExcelService;

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

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Result<List<UserDTO>>> listStudents() {
        List<UserDTO> students = userService.listStudents().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Result.success(students));
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

    @PostMapping("/avatar")
    public ResponseEntity<Result<Map<String, String>>> uploadAvatar(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            String avatarUrl = fileService.uploadAvatar(file, userDetails.getId());
            userService.updateUserAvatar(userDetails.getId(), avatarUrl);
            log.info("用户上传头像: userId={}, avatarUrl={}", userDetails.getId(), avatarUrl);
            return ResponseEntity.ok(Result.success(Map.of("url", avatarUrl)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "头像上传失败"));
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportUsers(@RequestParam(required = false) String keyword) {
        try {
            List<User> users = userService.listAllUsers(keyword);
            var outputStream = userExcelService.exportUsers(users);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "users.xlsx");

            log.info("导出用户列表: count={}", users.size());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("导出用户列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            var outputStream = userExcelService.generateTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "user_import_template.xlsx");

            log.info("下载用户导入模板");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("生成导入模板失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<UserImportResultDTO>> importUsers(@RequestParam("file") MultipartFile file) {
        // 验证文件类型
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "请上传 Excel 文件 (.xlsx 或 .xls)"));
        }

        UserImportResultDTO result = userExcelService.importUsers(file);
        log.info("导入用户完成: total={}, success={}, fail={}",
                result.getTotalCount(), result.getSuccessCount(), result.getFailCount());

        return ResponseEntity.ok(Result.success(result));
    }
}
