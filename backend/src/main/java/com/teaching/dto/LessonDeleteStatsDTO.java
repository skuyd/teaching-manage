package com.teaching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程删除统计信息DTO
 * 用于删除确认对话框显示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDeleteStatsDTO {

    /**
     * 课程ID
     */
    private Long lessonId;

    /**
     * 课程标题
     */
    private String lessonTitle;

    /**
     * 所属学科名称
     */
    private String subjectName;

    /**
     * 作业提交数量
     */
    private Integer submissionCount;

    /**
     * 评分数量
     */
    private Integer gradeCount;

    /**
     * 代码评论数量
     */
    private Integer commentCount;

    /**
     * 是否有关联数据
     */
    public boolean hasRelatedData() {
        return submissionCount > 0;
    }
}
