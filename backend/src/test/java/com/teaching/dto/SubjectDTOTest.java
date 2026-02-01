package com.teaching.dto;

import com.teaching.entity.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SubjectDTOTest {

    @Test
    @DisplayName("从Subject实体转换为SubjectDTO")
    void fromEntity_shouldConvertCorrectly() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Java高级编程");
        subject.setDescription("学习Java高级特性");
        subject.setIsGrouped(true);
        subject.setMinMembers(3);
        subject.setMaxMembers(5);
        subject.setStartDate(LocalDate.of(2024, 1, 1));
        subject.setEndDate(LocalDate.of(2024, 3, 31));

        SubjectDTO dto = SubjectDTO.fromEntity(subject);

        assertEquals(1L, dto.getId());
        assertEquals("Java高级编程", dto.getName());
        assertTrue(dto.getIsGrouped());
        assertEquals(3, dto.getMinMembers());
        assertEquals(5, dto.getMaxMembers());
    }

    @Test
    @DisplayName("CreateSubjectRequest转换为Subject实体")
    void createSubjectRequest_shouldConvertToEntity() {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Python基础");
        request.setDescription("Python入门课程");
        request.setIsGrouped(false);
        request.setStartDate(LocalDate.of(2024, 2, 1));
        request.setEndDate(LocalDate.of(2024, 4, 30));

        Subject subject = request.toEntity();

        assertEquals("Python基础", subject.getName());
        assertEquals("Python入门课程", subject.getDescription());
        assertFalse(subject.getIsGrouped());
    }

    @Test
    @DisplayName("UpdateSubjectRequest更新Subject实体")
    void updateSubjectRequest_shouldUpdateEntity() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("旧名称");
        subject.setDescription("旧描述");

        UpdateSubjectRequest request = new UpdateSubjectRequest();
        request.setName("新名称");
        request.setDescription("新描述");
        request.setIsGrouped(true);
        request.setMinMembers(2);
        request.setMaxMembers(4);

        request.updateEntity(subject);

        assertEquals("新名称", subject.getName());
        assertEquals("新描述", subject.getDescription());
        assertTrue(subject.getIsGrouped());
        assertEquals(2, subject.getMinMembers());
        assertEquals(4, subject.getMaxMembers());
    }
}
