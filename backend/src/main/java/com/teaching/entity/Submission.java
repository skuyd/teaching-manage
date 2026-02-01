package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 作业提交实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_submission")
public class Submission extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long lessonId;

    /**
     * 提交者ID
     */
    private Long submitterId;

    /**
     * 小组ID（小组作业时有值）
     */
    private Long groupId;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
}
