package com.teaching.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交DTO
 */
@Data
public class SubmissionDTO {

    private Long id;

    /**
     * 课程ID
     */
    private Long lessonId;

    /**
     * 课程标题
     */
    private String lessonTitle;

    /**
     * 提交者ID
     */
    private Long submitterId;

    /**
     * 提交者姓名
     */
    private String submitterName;

    /**
     * 小组ID（小组作业时有值）
     */
    private Long groupId;

    /**
     * 小组名称
     */
    private String groupName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 是否为小组提交
     */
    private Boolean isGroupSubmission;

    /**
     * 评分等级
     */
    private String grade;

    /**
     * 是否已评分
     */
    private Boolean graded;
}
