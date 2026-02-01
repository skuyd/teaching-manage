package com.teaching.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teaching.dto.CreateSubjectRequest;
import com.teaching.dto.UpdateSubjectRequest;
import com.teaching.entity.Subject;
import com.teaching.entity.SubjectStudent;
import com.teaching.mapper.SubjectMapper;
import com.teaching.mapper.SubjectStudentMapper;
import com.teaching.service.impl.SubjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private SubjectStudentMapper subjectStudentMapper;

    private SubjectServiceImpl subjectService;

    @BeforeEach
    void setUp() {
        subjectService = new SubjectServiceImpl(subjectMapper, subjectStudentMapper);
    }

    @Test
    @DisplayName("创建学科")
    void createSubject_shouldInsertSubject() {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Java高级编程");
        request.setDescription("学习Java高级特性");
        request.setIsGrouped(true);
        request.setMinMembers(3);
        request.setMaxMembers(5);

        when(subjectMapper.insert(any(Subject.class))).thenReturn(1);

        subjectService.createSubject(request);

        verify(subjectMapper).insert(argThat(subject ->
                subject.getName().equals("Java高级编程") &&
                subject.getIsGrouped() &&
                subject.getMinMembers() == 3
        ));
    }

    @Test
    @DisplayName("分页查询学科列表")
    void listSubjects_shouldReturnPagedSubjects() {
        Page<Subject> mockPage = new Page<>(1, 10);
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("测试学科");
        mockPage.setRecords(List.of(subject));
        mockPage.setTotal(1);

        when(subjectMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        IPage<Subject> result = subjectService.listSubjects(1, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("根据ID查询学科")
    void getSubjectById_shouldReturnSubject() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("测试学科");

        when(subjectMapper.selectById(1L)).thenReturn(subject);

        Subject result = subjectService.getSubjectById(1L);

        assertNotNull(result);
        assertEquals("测试学科", result.getName());
    }

    @Test
    @DisplayName("更新学科")
    void updateSubject_shouldUpdateFields() {
        Subject existingSubject = new Subject();
        existingSubject.setId(1L);
        existingSubject.setName("旧名称");

        UpdateSubjectRequest request = new UpdateSubjectRequest();
        request.setName("新名称");
        request.setIsGrouped(true);

        when(subjectMapper.selectById(1L)).thenReturn(existingSubject);
        when(subjectMapper.updateById(any(Subject.class))).thenReturn(1);

        subjectService.updateSubject(1L, request);

        verify(subjectMapper).updateById(argThat(subject ->
                subject.getName().equals("新名称") &&
                subject.getIsGrouped()
        ));
    }

    @Test
    @DisplayName("删除学科")
    void deleteSubject_shouldDeleteSubject() {
        when(subjectMapper.deleteById(1L)).thenReturn(1);

        subjectService.deleteSubject(1L);

        verify(subjectMapper).deleteById(1L);
    }

    @Test
    @DisplayName("添加学员到学科")
    void addStudentToSubject_shouldInsertRelation() {
        when(subjectStudentMapper.countBySubjectAndStudent(1L, 2L)).thenReturn(0L);
        when(subjectStudentMapper.insert(any(SubjectStudent.class))).thenReturn(1);

        subjectService.addStudentToSubject(1L, 2L);

        verify(subjectStudentMapper).insert(argThat(ss ->
                ss.getSubjectId() == 1L && ss.getStudentId() == 2L
        ));
    }

    @Test
    @DisplayName("学员已在学科中不重复添加")
    void addStudentToSubject_shouldNotDuplicate() {
        when(subjectStudentMapper.countBySubjectAndStudent(1L, 2L)).thenReturn(1L);

        subjectService.addStudentToSubject(1L, 2L);

        verify(subjectStudentMapper, never()).insert(any());
    }

    @Test
    @DisplayName("查询学员的学科列表")
    void getSubjectsByStudentId_shouldReturnSubjects() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("学员的学科");

        when(subjectMapper.selectByStudentId(2L)).thenReturn(List.of(subject));

        List<Subject> result = subjectService.getSubjectsByStudentId(2L);

        assertEquals(1, result.size());
        assertEquals("学员的学科", result.get(0).getName());
    }
}
