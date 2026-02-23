package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.CreateGradeRequest;
import com.teaching.dto.GradeDTO;
import com.teaching.dto.UpdateGradeRequest;
import com.teaching.enums.UserRole;
import com.teaching.service.GradeService;
import com.teaching.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评分控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    /**
     * 评分
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<GradeDTO> gradeSubmission(@Validated @RequestBody CreateGradeRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        GradeDTO dto = gradeService.gradeSubmission(request, userId);
        log.info("评分已添加: id={}, submissionId={}, grade={}", dto.getId(), request.getSubmissionId(), request.getGrade());
        return Result.success(dto);
    }

    /**
     * 更新评分
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<GradeDTO> updateGrade(
            @PathVariable Long id,
            @Validated @RequestBody UpdateGradeRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        GradeDTO dto = gradeService.updateGrade(id, request, userId);
        log.info("评分已更新: id={}, userId={}", id, userId);
        return Result.success(dto);
    }

    /**
     * 删除评分
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Void> deleteGrade(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        gradeService.deleteGrade(id, userId);
        log.info("评分已删除: id={}, userId={}", id, userId);
        return Result.success();
    }

    /**
     * 根据提交ID获取评分
     */
    @GetMapping("/submission/{submissionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<GradeDTO> getGradeBySubmission(@PathVariable Long submissionId) {
        GradeDTO grade = gradeService.getGradeBySubmission(submissionId);
        return Result.success(grade);
    }

    /**
     * 根据课程ID获取所有评分
     */
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<List<GradeDTO>> getGradesByLesson(@PathVariable Long lessonId) {
        List<GradeDTO> grades = gradeService.getGradesByLesson(lessonId);
        return Result.success(grades);
    }

    /**
     * 根据学员ID获取所有评分
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<List<GradeDTO>> getGradesByStudent(@PathVariable Long studentId) {
        // 学员只能查看自己的评分
        Long userId = SecurityUtils.getCurrentUserId();
        UserRole role = SecurityUtils.getCurrentUserRole();

        if (UserRole.STUDENT.equals(role) && !studentId.equals(userId)) {
            return Result.error(403, "无权查看他人成绩");
        }

        List<GradeDTO> grades = gradeService.getGradesByStudent(studentId);
        return Result.success(grades);
    }

    /**
     * 获取课程评分统计
     */
    @GetMapping("/lesson/{lessonId}/statistics")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Map<String, Object>> getLessonStatistics(@PathVariable Long lessonId) {
        Map<String, Object> stats = gradeService.getLessonGradeStatistics(lessonId);
        return Result.success(stats);
    }

    /**
     * 获取学员评分统计
     */
    @GetMapping("/student/{studentId}/statistics")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<Map<String, Object>> getStudentStatistics(@PathVariable Long studentId) {
        // 学员只能查看自己的统计
        Long userId = SecurityUtils.getCurrentUserId();
        UserRole role = SecurityUtils.getCurrentUserRole();

        if (UserRole.STUDENT.equals(role) && !studentId.equals(userId)) {
            return Result.error(403, "无权查看他人统计");
        }

        Map<String, Object> stats = gradeService.getStudentGradeStatistics(studentId);
        return Result.success(stats);
    }
}
