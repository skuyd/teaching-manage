package com.teaching.dto;

import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建课程请求
 */
@Data
public class CreateLessonRequest {
    /**
     * 学科ID
     */
    @NotNull(message = "学科ID不能为空")
    private Long subjectId;

    /**
     * 课程标题
     */
    @NotBlank(message = "课程标题不能为空")
    private String title;

    /**
     * 课程内容（Markdown）
     */
    private String content;

    /**
     * 上课时间
     */
    @NotNull(message = "上课时间不能为空")
    private LocalDateTime lessonTime;

    /**
     * 作业描述（Markdown）
     */
    private String homeworkDesc;

    /**
     * 提交类型（PERSONAL/GROUP）
     */
    @NotNull(message = "提交类型不能为空")
    private SubmitType submitType = SubmitType.PERSONAL;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 是否允许补交
     */
    @NotNull(message = "是否允许补交不能为空")
    private Boolean allowLate = false;

    /**
     * 转换为实体
     *
     * @return 课程实体
     */
    public Lesson toEntity() {
        Lesson lesson = new Lesson();
        lesson.setSubjectId(this.subjectId);
        lesson.setTitle(this.title);
        lesson.setContent(this.content);
        lesson.setLessonTime(this.lessonTime);
        lesson.setHomeworkDesc(this.homeworkDesc);
        lesson.setSubmitType(this.submitType);
        lesson.setDeadline(this.deadline);
        lesson.setAllowLate(this.allowLate);
        return lesson;
    }
}
