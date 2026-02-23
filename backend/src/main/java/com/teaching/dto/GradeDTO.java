package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评分DTO
 */
@Data
public class GradeDTO {

    private Long id;

    /**
     * 作业提交ID
     */
    private Long submissionId;

    /**
     * 评分等级
     */
    private GradeLevel grade;

    /**
     * 评分等级描述
     */
    private String gradeDescription;

    /**
     * 评语
     */
    private String comment;

    /**
     * 评分者ID
     */
    private Long graderId;

    /**
     * 评分者姓名
     */
    private String graderName;

    /**
     * 评分时间
     */
    private LocalDateTime gradeTime;

    /**
     * 课程标题
     */
    private String lessonTitle;

    /**
     * 提交者姓名
     */
    private String submitterName;
}
