package com.teaching.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代码评论DTO
 */
@Data
public class CodeCommentDTO {

    private Long id;

    /**
     * 作业提交ID
     */
    private Long submissionId;

    /**
     * 文件路径（相对路径）
     */
    private String filePath;

    /**
     * 行号
     */
    private Integer lineNumber;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者ID
     */
    private Long commenterId;

    /**
     * 评论者姓名
     */
    private String commenterName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
