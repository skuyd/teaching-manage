package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 代码评论实体
 */
@Data
@TableName("t_code_comment")
public class CodeComment {

    @TableId(type = IdType.AUTO)
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
