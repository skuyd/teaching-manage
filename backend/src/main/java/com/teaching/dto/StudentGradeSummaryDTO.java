package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 学员成绩汇总DTO
 */
@Data
public class StudentGradeSummaryDTO {

    /**
     * 学员ID
     */
    private Long studentId;

    /**
     * 学员姓名
     */
    private String studentName;

    /**
     * 学员邮箱
     */
    private String studentEmail;

    /**
     * 总课程数
     */
    private Integer totalLessons;

    /**
     * 已提交作业数
     */
    private Integer submittedCount;

    /**
     * 已评分数
     */
    private Integer gradedCount;

    /**
     * 未评分数
     */
    private Integer ungradedCount;

    /**
     * 平均成绩
     */
    private Double averageGrade;

    /**
     * 及格率
     */
    private Double passRate;

    /**
     * 等级分布
     */
    private Map<String, Long> gradeDistribution;

    /**
     * 详细成绩列表
     */
    private List<LessonGradeDetail> lessonGrades;

    /**
     * 课程成绩详情
     */
    @Data
    public static class LessonGradeDetail {
        /**
         * 课程ID
         */
        private Long lessonId;

        /**
         * 课程标题
         */
        private String lessonTitle;

        /**
         * 课程时间
         */
        private String lessonTime;

        /**
         * 评分等级
         */
        private GradeLevel grade;

        /**
         * 等级描述
         */
        private String gradeDescription;

        /**
         * 评语
         */
        private String comment;

        /**
         * 评分者
         */
        private String graderName;

        /**
         * 评分时间
         */
        private String gradeTime;

        /**
         * 是否已提交
         */
        private Boolean submitted;

        /**
         * 提交时间
         */
        private String submitTime;
    }
}
