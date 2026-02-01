package com.teaching.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业提交详情DTO（包含文件树）
 */
@Data
public class SubmissionDetailDTO {

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
     * 课程作业要求
     */
    private String homeworkDesc;

    /**
     * 提交者ID
     */
    private Long submitterId;

    /**
     * 提交者姓名
     */
    private String submitterName;

    /**
     * 小组ID
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
     * 文件目录树
     */
    private FileTreeNode fileTree;
}
