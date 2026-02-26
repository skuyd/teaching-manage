package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传图片（用于Markdown编辑器）
     *
     * @param file 图片文件
     * @return 图片信息
     */
    @PostMapping("/image")
    public ResponseEntity<Result<Map<String, Object>>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            FileService.ImageUploadResult result = fileService.uploadImage(file);
            log.info("图片上传成功: url={}, originalName={}", result.url(), result.originalName());
            return ResponseEntity.ok(Result.success(Map.of(
                    "url", result.url(),
                    "originalName", result.originalName() != null ? result.originalName() : "image",
                    "size", result.size()
            )));
        } catch (IllegalArgumentException e) {
            log.warn("图片上传参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "图片上传失败"));
        }
    }
}
