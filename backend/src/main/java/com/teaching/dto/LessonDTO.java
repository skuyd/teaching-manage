package com.teaching.dto;

import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程DTO
 */
@Data
public class LessonDTO {
    private Long id;
    private Long subjectId;
    private String title;
    private String content;
    private LocalDateTime lessonTime;
    private String homeworkDesc;
    private SubmitType submitType;
    private LocalDateTime deadline;
    private Boolean allowLate;
    private LocalDateTime createTime;

    /**
     * 学科名称（关联查询）
     */
    private String subjectName;

    /**
     * 提交数量（统计）
     */
    private Integer submissionCount;

    /**
     * 从实体转换为DTO
     *
     * @param lesson 课程实体
     * @return 课程DTO
     */
    public static LessonDTO fromEntity(Lesson lesson) {
        if (lesson == null) {
            return null;
        }

        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setSubjectId(lesson.getSubjectId());
        dto.setTitle(lesson.getTitle());
        dto.setContent(lesson.getContent());
        dto.setLessonTime(lesson.getLessonTime());
        dto.setHomeworkDesc(lesson.getHomeworkDesc());
        dto.setSubmitType(lesson.getSubmitType());
        dto.setDeadline(lesson.getDeadline());
        dto.setAllowLate(lesson.getAllowLate());
        dto.setCreateTime(lesson.getCreateTime());

        return dto;
    }
}
