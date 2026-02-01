package com.teaching.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SubjectTest {

    @Test
    @DisplayName("创建学科实体应包含所有字段")
    void createSubject_shouldHaveAllFields() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Java高级编程");
        subject.setDescription("Java高级特性学习");
        subject.setIsGrouped(true);
        subject.setMinMembers(3);
        subject.setMaxMembers(5);
        subject.setStartDate(LocalDate.of(2024, 1, 1));
        subject.setEndDate(LocalDate.of(2024, 3, 31));

        assertEquals(1L, subject.getId());
        assertEquals("Java高级编程", subject.getName());
        assertTrue(subject.getIsGrouped());
        assertEquals(3, subject.getMinMembers());
        assertEquals(5, subject.getMaxMembers());
    }

    @Test
    @DisplayName("新学科默认不分组")
    void newSubject_shouldNotBeGroupedByDefault() {
        Subject subject = new Subject();
        assertFalse(subject.getIsGrouped());
    }

    @Test
    @DisplayName("新学科默认成员数为1")
    void newSubject_shouldHaveDefaultMemberCount() {
        Subject subject = new Subject();
        assertEquals(1, subject.getMinMembers());
        assertEquals(1, subject.getMaxMembers());
    }

    @Test
    @DisplayName("学科可设置固定小组人数")
    void subject_shouldSupportFixedGroupSize() {
        Subject subject = new Subject();
        subject.setMinMembers(4);
        subject.setMaxMembers(4);  // 最小等于最大表示固定人数

        assertEquals(subject.getMinMembers(), subject.getMaxMembers());
    }

    @Test
    @DisplayName("学科可设置小组人数范围")
    void subject_shouldSupportGroupSizeRange() {
        Subject subject = new Subject();
        subject.setMinMembers(3);
        subject.setMaxMembers(5);

        assertTrue(subject.getMaxMembers() > subject.getMinMembers());
    }
}
