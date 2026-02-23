package com.teaching.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.teaching.common.Result;
import com.teaching.dto.*;
import com.teaching.entity.Subject;
import com.teaching.service.SubjectService;
import com.teaching.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<PageResponse<SubjectDTO>> listSubjects(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        IPage<Subject> subjectPage = subjectService.listSubjects(page, size, keyword);

        List<SubjectDTO> subjectDTOs = subjectPage.getRecords().stream()
                .map(SubjectDTO::fromEntity)
                .toList();

        PageResponse<SubjectDTO> response = PageResponse.of(
                subjectDTOs,
                subjectPage.getTotal(),
                page,
                size
        );

        return Result.success(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<SubjectDTO> getSubjectById(@PathVariable Long id) {
        Subject subject = subjectService.getSubjectById(id);
        return Result.success(SubjectDTO.fromEntity(subject));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        subjectService.createSubject(request);
        log.info("创建学科: name={}", request.getName());
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        subjectService.updateSubject(id, request);
        log.info("更新学科: id={}", id);
        return Result.success();
    }

    @GetMapping("/{id}/delete-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<SubjectDeleteStatsDTO> getDeleteStats(@PathVariable Long id) {
        SubjectDeleteStatsDTO stats = subjectService.getDeleteStats(id);
        return Result.success(stats);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        log.info("删除学科: id={}", id);
        return Result.success();
    }

    @PostMapping("/{subjectId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> addStudentToSubject(
            @PathVariable Long subjectId,
            @PathVariable Long studentId) {
        subjectService.addStudentToSubject(subjectId, studentId);
        log.info("添加学员到学科: subjectId={}, studentId={}", subjectId, studentId);
        return Result.success();
    }

    @DeleteMapping("/{subjectId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> removeStudentFromSubject(
            @PathVariable Long subjectId,
            @PathVariable Long studentId) {
        subjectService.removeStudentFromSubject(subjectId, studentId);
        log.info("从学科移除学员: subjectId={}, studentId={}", subjectId, studentId);
        return Result.success();
    }

    @GetMapping("/{subjectId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<List<UserDTO>> getSubjectStudents(@PathVariable Long subjectId) {
        List<UserDTO> students = subjectService.getStudentsBySubjectId(subjectId);
        return Result.success(students);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<SubjectDTO>> getMySubjects() {
        Long studentId = SecurityUtils.getCurrentUserId();
        List<Subject> subjects = subjectService.getSubjectsByStudentId(studentId);
        List<SubjectDTO> subjectDTOs = subjects.stream()
                .map(SubjectDTO::fromEntity)
                .toList();
        return Result.success(subjectDTOs);
    }
}
