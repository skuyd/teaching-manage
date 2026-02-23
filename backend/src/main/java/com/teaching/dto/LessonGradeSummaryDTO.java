package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 课程成绩汇总DTO
 */
@Data
public class LessonGradeSummaryDTO {

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
     * 作业截止时间
     */
    private String deadline;

    /**
     * 学科ID
     */
    private Long subjectId;

    /**
     * 学科名称
     */
    private String subjectName;

    /**
     * 总学员数
     */
    private Integer totalStudents;

    /**
     * 已提交数
     */
    private Integer submittedCount;

    /**
     * 已评分数
     */
    private Integer gradedCount;

    /**
     * 未提交数
     */
    private Integer unsubmittedCount;

    /**
     * 未评分数
     */
    private Integer ungradedCount;

    /**
     * 提交率
     */
    private Double submissionRate;

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
     * 学员成绩详情列表
     */
    private List<StudentGradeDetail> studentGrades;

    /**
     * 学员成绩详情
     */
    @Data
    public static class StudentGradeDetail {
        /**
         * 学员ID
         */
        private Long studentId;

        /**
         * 学员姓名
         */
        private String studentName;

        /**
         * 小组ID
         */
        private Long groupId;

        /**
         * 小组名称
         */
        private String groupName;

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
         * 提交时间
         */
        private String submitTime;

        /**
         * 评分时间
         */
        private String gradeTime;

        /**
         * 是否已提交
         */
        private Boolean submitted;

        /**
         * 是否已评分
         */
        private Boolean graded;
    }
}
