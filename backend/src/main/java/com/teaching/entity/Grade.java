package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.teaching.enums.GradeLevel;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评分实体
 */
@Data
@TableName("t_grade")
public class Grade {

    @TableId(type = IdType.AUTO)
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
     * 评语
     */
    private String comment;

    /**
     * 评分者ID
     */
    private Long graderId;

    /**
     * 评分时间
     */
    private LocalDateTime gradeTime;

    /**
     * 乐观锁
     */
    @Version
    private Integer version;

    /**
     * 删除标志(0存在 1删除)
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
