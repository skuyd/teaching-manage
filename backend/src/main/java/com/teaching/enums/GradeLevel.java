package com.teaching.enums;

/**
 * 评分等级枚举
 */
public enum GradeLevel {

    /**
     * 优秀
     */
    A("优秀"),

    /**
     * 良好
     */
    B("良好"),

    /**
     * 及格
     */
    C("及格"),

    /**
     * 不及格
     */
    D("不及格");

    private final String description;

    GradeLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
