package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 创建评分请求
 */
@Data
public class CreateGradeRequest {

    /**
     * 作业提交ID
     */
    @NotNull(message = "提交ID不能为空")
    private Long submissionId;

    /**
     * 评分等级
     */
    @NotNull(message = "评分等级不能为空")
    private GradeLevel grade;

    /**
     * 评语
     */
    private String comment;
}
