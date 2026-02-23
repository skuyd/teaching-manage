package com.teaching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学科删除统计信息DTO
 * 用于删除确认对话框显示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDeleteStatsDTO {

    /**
     * 学科ID
     */
    private Long subjectId;

    /**
     * 学科名称
     */
    private String subjectName;

    /**
     * 课程数量
     */
    private Integer lessonCount;

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
     * 学员数量
     */
    private Integer studentCount;

    /**
     * 小组数量
     */
    private Integer groupCount;

    /**
     * 是否有关联数据（课程、提交等）
     */
    public boolean hasRelatedData() {
        return lessonCount > 0 || submissionCount > 0 || studentCount > 0 || groupCount > 0;
    }
}
