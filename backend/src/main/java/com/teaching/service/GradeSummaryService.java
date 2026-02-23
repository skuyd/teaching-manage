package com.teaching.service;

import com.teaching.dto.GradeSummaryDTO;
import com.teaching.dto.LessonGradeSummaryDTO;
import com.teaching.dto.StudentGradeSummaryDTO;

import java.io.IOException;

/**
 * 成绩汇总服务
 */
public interface GradeSummaryService {

    /**
     * 获取学科成绩汇总
     */
    GradeSummaryDTO getSubjectGradeSummary(Long subjectId);

    /**
     * 获取课程成绩汇总
     */
    LessonGradeSummaryDTO getLessonGradeSummary(Long lessonId);

    /**
     * 获取学员成绩汇总
     */
    StudentGradeSummaryDTO getStudentGradeSummary(Long studentId);

    /**
     * 导出学科成绩Excel
     */
    byte[] exportSubjectGradesToExcel(Long subjectId) throws IOException;

    /**
     * 导出课程成绩Excel
     */
    byte[] exportLessonGradesToExcel(Long lessonId) throws IOException;

    /**
     * 导出学员成绩Excel
     */
    byte[] exportStudentGradesToExcel(Long studentId) throws IOException;
}
