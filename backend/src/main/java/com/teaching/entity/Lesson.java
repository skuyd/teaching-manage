package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import com.teaching.enums.SubmitType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_lesson")
public class Lesson extends BaseEntity {
    /**
     * 学科ID
     */
    private Long subjectId;

    /**
     * 课程标题
     */
    private String title;

    /**
     * 课程内容（Markdown）
     */
    private String content;

    /**
     * 上课时间
     */
    private LocalDateTime lessonTime;

    /**
     * 作业描述（Markdown）
     */
    private String homeworkDesc;

    /**
     * 提交类型（PERSONAL/GROUP）
     */
    private SubmitType submitType = SubmitType.PERSONAL;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 是否允许补交
     */
    private Boolean allowLate = false;
}
