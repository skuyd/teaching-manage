package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import lombok.Data;

/**
 * 更新评分请求
 */
@Data
public class UpdateGradeRequest {

    /**
     * 评分等级
     */
    private GradeLevel grade;

    /**
     * 评语
     */
    private String comment;
}
