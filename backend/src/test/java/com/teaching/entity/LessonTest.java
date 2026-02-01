package com.teaching.entity;

import com.teaching.enums.SubmitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LessonTest {

    @Test
    @DisplayName("创建课程实体应包含所有字段")
    void createLesson_shouldHaveAllFields() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubjectId(1L);
        lesson.setTitle("Java基础-第一课");
        lesson.setContent("## 课程内容\n- 变量\n- 数据类型");
        lesson.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        lesson.setHomeworkDesc("## 作业要求\n完成练习1-5");
        lesson.setSubmitType(SubmitType.PERSONAL);
        lesson.setDeadline(LocalDateTime.of(2024, 1, 20, 23, 59));
        lesson.setAllowLate(true);

        assertEquals(1L, lesson.getId());
        assertEquals("Java基础-第一课", lesson.getTitle());
        assertEquals(SubmitType.PERSONAL, lesson.getSubmitType());
        assertTrue(lesson.getAllowLate());
    }

    @Test
    @DisplayName("新课程默认为个人提交")
    void newLesson_shouldHavePersonalSubmitTypeByDefault() {
        Lesson lesson = new Lesson();
        assertEquals(SubmitType.PERSONAL, lesson.getSubmitType());
    }

    @Test
    @DisplayName("新课程默认不允许补交")
    void newLesson_shouldNotAllowLateByDefault() {
        Lesson lesson = new Lesson();
        assertFalse(lesson.getAllowLate());
    }

    @Test
    @DisplayName("提交类型枚举应有两种值")
    void submitType_shouldHaveTwoValues() {
        assertEquals(2, SubmitType.values().length);
        assertNotNull(SubmitType.PERSONAL);
        assertNotNull(SubmitType.GROUP);
    }
}
