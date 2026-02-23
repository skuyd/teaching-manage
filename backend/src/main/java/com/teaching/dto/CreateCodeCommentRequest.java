package com.teaching.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建代码评论请求
 */
@Data
public class CreateCodeCommentRequest {

    /**
     * 作业提交ID
     */
    @NotNull(message = "提交ID不能为空")
    private Long submissionId;

    /**
     * 文件路径（相对路径）
     */
    @NotBlank(message = "文件路径不能为空")
    private String filePath;

    /**
     * 行号
     */
    @NotNull(message = "行号不能为空")
    @Min(value = 1, message = "行号必须大于0")
    private Integer lineNumber;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
