package com.teaching.entity;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    /**
     * 课程发布通知
     */
    LESSON_PUBLISHED("课程发布"),

    /**
     * 作业截止提醒
     */
    DEADLINE_REMINDER("截止提醒"),

    /**
     * 评分完成通知
     */
    GRADE_COMPLETED("评分完成"),

    /**
     * 小组申请通知
     */
    GROUP_APPLICATION("小组申请"),

    /**
     * 小组审批通知
     */
    GROUP_APPROVAL("小组审批");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
