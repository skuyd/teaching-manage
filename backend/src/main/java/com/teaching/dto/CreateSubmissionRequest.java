package com.teaching.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建作业提交请求
 */
@Data
public class CreateSubmissionRequest {

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long lessonId;

    /**
     * 小组ID（小组作业时必填）
     */
    private Long groupId;

    /**
     * 备注说明
     */
    private String notes;
}
