package com.teaching.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.teaching.common.Result;
import com.teaching.dto.PageResponse;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.dto.LessonDTO;
import com.teaching.dto.UpdateLessonRequest;
import com.teaching.entity.Lesson;
import com.teaching.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    /**
     * 分页查询课程列表
     *
     * @param subjectId 学科ID（可选）
     * @param page      页码
     * @param size      每页大小
     * @param keyword   关键词（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 课程分页列表
     */
    @GetMapping
    public Result<PageResponse<LessonDTO>> listLessons(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        IPage<Lesson> lessonPage = lessonService.listLessons(subjectId, page, size, keyword, startTime, endTime);

        List<LessonDTO> lessonDTOs = lessonPage.getRecords().stream()
                .map(LessonDTO::fromEntity)
                .toList();

        PageResponse<LessonDTO> response = PageResponse.of(
                lessonDTOs, lessonPage.getTotal(), page, size);

        return Result.success(response);
    }

    /**
     * 根据ID查询课程
     *
     * @param id 课程ID
     * @return 课程详情
     */
    @GetMapping("/{id}")
    public Result<LessonDTO> getLessonById(@PathVariable Long id) {
        Lesson lesson = lessonService.getLessonById(id);
        if (lesson == null) {
            return Result.error(404, "课程不存在");
        }
        return Result.success(LessonDTO.fromEntity(lesson));
    }

    /**
     * 创建课程
     *
     * @param request 创建请求
     * @return 创建的课程
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<LessonDTO> createLesson(@Valid @RequestBody CreateLessonRequest request) {
        Lesson lesson = lessonService.createLesson(request);
        log.info("创建课程: subjectId={}, title={}", lesson.getSubjectId(), lesson.getTitle());
        return Result.success(LessonDTO.fromEntity(lesson));
    }

    /**
     * 更新课程
     *
     * @param id      课程ID
     * @param request 更新请求
     * @return 更新后的课程
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<LessonDTO> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLessonRequest request) {
        Lesson lesson = lessonService.updateLesson(id, request);
        log.info("更新课程: id={}, title={}", id, lesson.getTitle());
        return Result.success(LessonDTO.fromEntity(lesson));
    }

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        log.info("删除课程: id={}", id);
        return Result.success();
    }
}
