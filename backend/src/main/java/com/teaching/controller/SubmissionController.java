package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.FileTreeNode;
import com.teaching.dto.SubmissionDTO;
import com.teaching.dto.SubmissionDetailDTO;
import com.teaching.entity.Submission;
import com.teaching.exception.ResourceNotFoundException;
import com.teaching.service.SubmissionService;
import com.teaching.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 作业提交控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 提交作业
     */
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<SubmissionDTO> submitAssignment(
            @RequestParam("lessonId") Long lessonId,
            @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = SecurityUtils.getCurrentUserId();
        SubmissionDTO dto = submissionService.submitAssignment(lessonId, file, userId);
        log.info("作业提交成功: userId={}, lessonId={}", userId, lessonId);
        return Result.success(dto);
    }

    /**
     * 重新提交作业
     */
    @PostMapping(value = "/resubmit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<SubmissionDTO> resubmitAssignment(
            @RequestParam("lessonId") Long lessonId,
            @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = SecurityUtils.getCurrentUserId();
        SubmissionDTO dto = submissionService.resubmitAssignment(lessonId, file, userId);
        log.info("作业重新提交成功: userId={}, lessonId={}", userId, lessonId);
        return Result.success(dto);
    }

    /**
     * 根据课程ID获取所有提交
     */
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<List<SubmissionDTO>> getSubmissionsByLesson(@PathVariable Long lessonId) {
        List<SubmissionDTO> submissions = submissionService.getSubmissionsByLessonId(lessonId);
        return Result.success(submissions);
    }

    /**
     * 获取提交详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<SubmissionDetailDTO> getSubmissionDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        SubmissionDetailDTO detail = submissionService.getSubmissionDetail(id, userId);
        return Result.success(detail);
    }

    /**
     * 获取我的提交
     */
    @GetMapping("/my/{lessonId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<SubmissionDTO> getMySubmission(@PathVariable Long lessonId) {
        Long userId = SecurityUtils.getCurrentUserId();
        SubmissionDTO submission = submissionService.getMySubmission(lessonId, userId);
        return Result.success(submission);
    }

    /**
     * 删除提交
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<Void> deleteSubmission(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        submissionService.deleteSubmission(id, userId);
        log.info("作业提交已删除: id={}, userId={}", id, userId);
        return Result.success();
    }

    /**
     * 获取文件树
     */
    @GetMapping("/{id}/file-tree")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<FileTreeNode> getFileTree(@PathVariable Long id) throws IOException {
        FileTreeNode tree = submissionService.getFileTree(id);
        return Result.success(tree);
    }

    /**
     * 读取文件内容
     */
    @GetMapping("/{id}/file-content")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<String> readFileContent(
            @PathVariable Long id,
            @RequestParam String path) throws IOException {
        String content = submissionService.readFileContent(id, path);
        return Result.success(content);
    }

    /**
     * 下载提交文件（打包为ZIP）
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<byte[]> downloadSubmission(@PathVariable Long id) throws IOException {
        // 获取提交记录
        Submission submission = submissionService.getById(id);
        if (submission == null) {
            throw new ResourceNotFoundException("提交记录", id);
        }

        // 获取文件路径
        String filePath = submission.getFilePath();
        Path sourcePath = Paths.get(filePath);

        // 如果路径不存在，尝试使用 uploadDir 前缀
        if (!Files.exists(sourcePath)) {
            sourcePath = Paths.get(uploadDir, filePath);
        }

        if (!Files.exists(sourcePath)) {
            throw new ResourceNotFoundException("提交文件", filePath);
        }

        // 打包为 ZIP
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            if (Files.isDirectory(sourcePath)) {
                // 递归打包目录
                Path finalSourcePath = sourcePath;
                Files.walk(sourcePath)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            try {
                                String entryName = finalSourcePath.relativize(path).toString();
                                zos.putNextEntry(new ZipEntry(entryName));
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                log.error("打包文件失败: {}", path, e);
                            }
                        });
            } else {
                // 单文件打包
                zos.putNextEntry(new ZipEntry(sourcePath.getFileName().toString()));
                Files.copy(sourcePath, zos);
                zos.closeEntry();
            }
        }

        byte[] zipBytes = baos.toByteArray();
        String zipFileName = "submission_" + id + ".zip";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", zipFileName);
        headers.setContentLength(zipBytes.length);

        log.info("下载提交文件: id={}, fileName={}", id, zipFileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(zipBytes);
    }
}
