package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.GradeSummaryDTO;
import com.teaching.dto.LessonGradeSummaryDTO;
import com.teaching.dto.StudentGradeSummaryDTO;
import com.teaching.enums.UserRole;
import com.teaching.service.GradeSummaryService;
import com.teaching.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 成绩汇总控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/grade-summary")
@RequiredArgsConstructor
public class GradeSummaryController {

    private final GradeSummaryService gradeSummaryService;

    /**
     * 获取学科成绩汇总
     */
    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<GradeSummaryDTO> getSubjectGradeSummary(@PathVariable Long subjectId) {
        GradeSummaryDTO summary = gradeSummaryService.getSubjectGradeSummary(subjectId);
        return Result.success(summary);
    }

    /**
     * 获取课程成绩汇总
     */
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<LessonGradeSummaryDTO> getLessonGradeSummary(@PathVariable Long lessonId) {
        LessonGradeSummaryDTO summary = gradeSummaryService.getLessonGradeSummary(lessonId);
        return Result.success(summary);
    }

    /**
     * 获取学员成绩汇总
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public Result<StudentGradeSummaryDTO> getStudentGradeSummary(@PathVariable Long studentId) {
        // 学员只能查看自己的成绩汇总
        Long userId = SecurityUtils.getCurrentUserId();
        UserRole role = SecurityUtils.getCurrentUserRole();

        if (UserRole.STUDENT.equals(role) && !studentId.equals(userId)) {
            return Result.error(403, "无权查看他人成绩汇总");
        }

        StudentGradeSummaryDTO summary = gradeSummaryService.getStudentGradeSummary(studentId);
        return Result.success(summary);
    }

    /**
     * 导出学科成绩Excel
     */
    @GetMapping("/subject/{subjectId}/export")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<byte[]> exportSubjectGrades(@PathVariable Long subjectId) {
        try {
            byte[] excelData = gradeSummaryService.exportSubjectGradesToExcel(subjectId);

            String filename = generateFilename("subject_" + subjectId + "_grades");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("学科成绩Excel已导出: subjectId={}, fileSize={}", subjectId, excelData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (IOException e) {
            log.error("导出学科成绩Excel失败: subjectId={}", subjectId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出课程成绩Excel
     */
    @GetMapping("/lesson/{lessonId}/export")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<byte[]> exportLessonGrades(@PathVariable Long lessonId) {
        try {
            byte[] excelData = gradeSummaryService.exportLessonGradesToExcel(lessonId);

            String filename = generateFilename("lesson_" + lessonId + "_grades");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("课程成绩Excel已导出: lessonId={}, fileSize={}", lessonId, excelData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (IOException e) {
            log.error("导出课程成绩Excel失败: lessonId={}", lessonId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出学员成绩Excel
     */
    @GetMapping("/student/{studentId}/export")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<byte[]> exportStudentGrades(@PathVariable Long studentId) {
        // 学员只能导出自己的成绩
        Long userId = SecurityUtils.getCurrentUserId();
        UserRole role = SecurityUtils.getCurrentUserRole();

        if (UserRole.STUDENT.equals(role) && !studentId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        try {
            byte[] excelData = gradeSummaryService.exportStudentGradesToExcel(studentId);

            String filename = generateFilename("student_" + studentId + "_grades");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            log.info("学员成绩Excel已导出: studentId={}, fileSize={}", studentId, excelData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (IOException e) {
            log.error("导出学员成绩Excel失败: studentId={}", studentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate Excel filename with timestamp
     */
    private String generateFilename(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + "_" + timestamp + ".xlsx";
    }
}
