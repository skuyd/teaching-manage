package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.CreateGradeRequest;
import com.teaching.dto.GradeDTO;
import com.teaching.dto.UpdateGradeRequest;
import com.teaching.entity.Grade;

import java.util.List;
import java.util.Map;

public interface GradeService extends IService<Grade> {

    /**
     * 评分
     */
    GradeDTO gradeSubmission(CreateGradeRequest request, Long graderId);

    /**
     * 更新评分
     */
    GradeDTO updateGrade(Long id, UpdateGradeRequest request, Long graderId);

    /**
     * 删除评分
     */
    void deleteGrade(Long id, Long userId);

    /**
     * 根据提交ID获取评分
     */
    GradeDTO getGradeBySubmission(Long submissionId);

    /**
     * 根据课程ID获取所有评分
     */
    List<GradeDTO> getGradesByLesson(Long lessonId);

    /**
     * 根据学员ID获取所有评分
     */
    List<GradeDTO> getGradesByStudent(Long studentId);

    /**
     * 获取课程评分统计
     */
    Map<String, Object> getLessonGradeStatistics(Long lessonId);

    /**
     * 获取学员评分统计
     */
    Map<String, Object> getStudentGradeStatistics(Long studentId);
}
