package com.teaching.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.dto.UpdateLessonRequest;
import com.teaching.entity.Lesson;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程服务接口
 */
public interface LessonService extends IService<Lesson> {
    /**
     * 创建课程
     *
     * @param request 创建请求
     * @return 创建的课程
     */
    Lesson createLesson(CreateLessonRequest request);

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
    IPage<Lesson> listLessons(Long subjectId, int page, int size, String keyword,
                               LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据ID查询课程
     *
     * @param id 课程ID
     * @return 课程
     */
    Lesson getLessonById(Long id);

    /**
     * 更新课程
     *
     * @param id      课程ID
     * @param request 更新请求
     * @return 更新后的课程
     */
    Lesson updateLesson(Long id, UpdateLessonRequest request);

    /**
     * 删除课程
     *
     * @param id 课程ID
     */
    void deleteLesson(Long id);

    /**
     * 根据学科ID查询课程列表
     *
     * @param subjectId 学科ID
     * @return 课程列表
     */
    List<Lesson> getLessonsBySubjectId(Long subjectId);

    /**
     * 调整课程时间
     *
     * @param id         课程ID
     * @param lessonTime 新的上课时间
     * @return 更新后的课程
     */
    Lesson updateLessonTime(Long id, LocalDateTime lessonTime);
}
