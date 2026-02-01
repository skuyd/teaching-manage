package com.teaching.dto;

import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新课程请求
 */
@Data
public class UpdateLessonRequest {
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
    private SubmitType submitType;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 是否允许补交
     */
    private Boolean allowLate;

    /**
     * 更新实体
     *
     * @param lesson 课程实体
     */
    public void updateEntity(Lesson lesson) {
        if (this.title != null) {
            lesson.setTitle(this.title);
        }
        if (this.content != null) {
            lesson.setContent(this.content);
        }
        if (this.lessonTime != null) {
            lesson.setLessonTime(this.lessonTime);
        }
        if (this.homeworkDesc != null) {
            lesson.setHomeworkDesc(this.homeworkDesc);
        }
        if (this.submitType != null) {
            lesson.setSubmitType(this.submitType);
        }
        if (this.deadline != null) {
            lesson.setDeadline(this.deadline);
        }
        if (this.allowLate != null) {
            lesson.setAllowLate(this.allowLate);
        }
    }
}
