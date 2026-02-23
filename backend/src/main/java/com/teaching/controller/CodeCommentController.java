package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.CodeCommentDTO;
import com.teaching.dto.CreateCodeCommentRequest;
import com.teaching.dto.FileCommentsDTO;
import com.teaching.service.CodeCommentService;
import com.teaching.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 代码评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CodeCommentController {

    private final CodeCommentService codeCommentService;

    /**
     * 添加代码评论
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<CodeCommentDTO> addComment(@Validated @RequestBody CreateCodeCommentRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        CodeCommentDTO dto = codeCommentService.addComment(request, userId);
        log.info("代码评论已添加: id={}, userId={}", dto.getId(), userId);
        return Result.success(dto);
    }

    /**
     * 更新评论内容
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<CodeCommentDTO> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "评论内容不能为空");
        }
        CodeCommentDTO dto = codeCommentService.updateComment(id, content, userId);
        log.info("代码评论已更新: id={}, userId={}", id, userId);
        return Result.success(dto);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        codeCommentService.deleteComment(id, userId);
        log.info("代码评论已删除: id={}, userId={}", id, userId);
        return Result.success();
    }

    /**
     * 获取提交的所有评论
     */
    @GetMapping("/submission/{submissionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<List<CodeCommentDTO>> getCommentsBySubmission(@PathVariable Long submissionId) {
        List<CodeCommentDTO> comments = codeCommentService.getCommentsBySubmission(submissionId);
        return Result.success(comments);
    }

    /**
     * 获取文件的所有评论
     */
    @GetMapping("/submission/{submissionId}/file")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<List<CodeCommentDTO>> getCommentsByFile(
            @PathVariable Long submissionId,
            @RequestParam String filePath) {
        List<CodeCommentDTO> comments = codeCommentService.getCommentsByFile(submissionId, filePath);
        return Result.success(comments);
    }

    /**
     * 获取特定行的评论
     */
    @GetMapping("/submission/{submissionId}/line")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<List<CodeCommentDTO>> getCommentsByLine(
            @PathVariable Long submissionId,
            @RequestParam String filePath,
            @RequestParam Integer lineNumber) {
        List<CodeCommentDTO> comments = codeCommentService.getCommentsByLine(submissionId, filePath, lineNumber);
        return Result.success(comments);
    }

    /**
     * 获取提交的评论（按文件和行号分组）
     */
    @GetMapping("/submission/{submissionId}/grouped")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<List<FileCommentsDTO>> getCommentsGrouped(@PathVariable Long submissionId) {
        List<FileCommentsDTO> grouped = codeCommentService.getCommentsGroupedByFile(submissionId);
        return Result.success(grouped);
    }
}
