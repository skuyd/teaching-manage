package com.teaching.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 成绩汇总DTO（综合视图）
 */
@Data
public class GradeSummaryDTO {

    /**
     * 学科ID
     */
    private Long subjectId;

    /**
     * 学科名称
     */
    private String subjectName;

    /**
     * 总课程数
     */
    private Integer totalLessons;

    /**
     * 已评分课程数
     */
    private Integer gradedLessons;

    /**
     * 总学员数
     */
    private Integer totalStudents;

    /**
     * 已提交作业学员数
     */
    private Integer submittedStudents;

    /**
     * 按课程的成绩汇总
     */
    private List<LessonGradeSummaryDTO> lessonSummaries;

    /**
     * 按学员的成绩汇总
     */
    private List<StudentGradeSummaryDTO> studentSummaries;

    /**
     * 整体统计
     */
    private OverallStatistics overallStatistics;

    /**
     * 整体统计
     */
    @Data
    public static class OverallStatistics {
        /**
         * 总评分数
         */
        private Integer totalGrades;

        /**
         * 等级分布
         */
        private Map<String, Long> gradeDistribution;

        /**
         * 平均成绩
         */
        private Double averageGrade;

        /**
         * 及格率
         */
        private Double passRate;
    }
}
