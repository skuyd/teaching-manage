package com.teaching.dto;

import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LessonDTOTest {

    @Test
    @DisplayName("从Lesson实体转换为DTO")
    void fromEntity_shouldConvertLessonToDTO() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubjectId(1L);
        lesson.setTitle("Java基础-第一课");
        lesson.setContent("## 课程内容");
        lesson.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        lesson.setHomeworkDesc("## 作业要求");
        lesson.setSubmitType(SubmitType.PERSONAL);
        lesson.setDeadline(LocalDateTime.of(2024, 1, 20, 23, 59));
        lesson.setAllowLate(true);
        lesson.setCreateTime(LocalDateTime.of(2024, 1, 1, 10, 0));

        LessonDTO dto = LessonDTO.fromEntity(lesson);

        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getSubjectId());
        assertEquals("Java基础-第一课", dto.getTitle());
        assertEquals("## 课程内容", dto.getContent());
        assertEquals(SubmitType.PERSONAL, dto.getSubmitType());
        assertTrue(dto.getAllowLate());
    }

    @Test
    @DisplayName("CreateLessonRequest转换为Lesson实体")
    void createRequest_toEntity_shouldConvertToLesson() {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setSubjectId(1L);
        request.setTitle("Java基础-第一课");
        request.setContent("## 课程内容");
        request.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        request.setHomeworkDesc("## 作业要求");
        request.setSubmitType(SubmitType.GROUP);
        request.setDeadline(LocalDateTime.of(2024, 1, 20, 23, 59));
        request.setAllowLate(true);

        Lesson lesson = request.toEntity();

        assertEquals(1L, lesson.getSubjectId());
        assertEquals("Java基础-第一课", lesson.getTitle());
        assertEquals(SubmitType.GROUP, lesson.getSubmitType());
        assertTrue(lesson.getAllowLate());
    }

    @Test
    @DisplayName("UpdateLessonRequest更新Lesson实体")
    void updateRequest_updateEntity_shouldUpdateLesson() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("旧标题");
        lesson.setContent("旧内容");
        lesson.setAllowLate(false);

        UpdateLessonRequest request = new UpdateLessonRequest();
        request.setTitle("新标题");
        request.setContent("新内容");
        request.setAllowLate(true);

        request.updateEntity(lesson);

        assertEquals(1L, lesson.getId());
        assertEquals("新标题", lesson.getTitle());
        assertEquals("新内容", lesson.getContent());
        assertTrue(lesson.getAllowLate());
    }

    @Test
    @DisplayName("CreateLessonRequest验证必填字段")
    void createRequest_shouldValidateRequiredFields() {
        CreateLessonRequest request = new CreateLessonRequest();

        assertNull(request.getSubjectId());
        assertNull(request.getTitle());
        assertNull(request.getLessonTime());
        assertEquals(SubmitType.PERSONAL, request.getSubmitType());
        assertFalse(request.getAllowLate());
    }
}
