package com.teaching.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.dto.UpdateLessonRequest;
import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import com.teaching.mapper.LessonMapper;
import com.teaching.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonMapper lessonMapper;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        testLesson = new Lesson();
        testLesson.setId(1L);
        testLesson.setSubjectId(1L);
        testLesson.setTitle("Java基础-第一课");
        testLesson.setContent("## 课程内容");
        testLesson.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        testLesson.setHomeworkDesc("## 作业要求");
        testLesson.setSubmitType(SubmitType.PERSONAL);
        testLesson.setDeadline(LocalDateTime.of(2024, 1, 20, 23, 59));
        testLesson.setAllowLate(true);
    }

    @Test
    @DisplayName("创建课程成功")
    void createLesson_shouldSuccess() {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setSubjectId(1L);
        request.setTitle("Java基础-第一课");
        request.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        request.setSubmitType(SubmitType.PERSONAL);
        request.setAllowLate(false);

        when(lessonMapper.insert(any(Lesson.class))).thenReturn(1);

        Lesson result = lessonService.createLesson(request);

        assertNotNull(result);
        assertEquals("Java基础-第一课", result.getTitle());
        verify(lessonMapper, times(1)).insert(any(Lesson.class));
    }

    @Test
    @DisplayName("分页查询课程列表")
    void listLessons_shouldReturnPage() {
        Page<Lesson> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testLesson));
        mockPage.setTotal(1);
        when(lessonMapper.selectPage(any(), any())).thenReturn(mockPage);

        IPage<Lesson> result = lessonService.listLessons(1L, 1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(lessonMapper, times(1)).selectPage(any(), any());
    }

    @Test
    @DisplayName("根据ID查询课程")
    void getLessonById_shouldReturnLesson() {
        when(lessonMapper.selectById(1L)).thenReturn(testLesson);

        Lesson result = lessonService.getLessonById(1L);

        assertNotNull(result);
        assertEquals("Java基础-第一课", result.getTitle());
        verify(lessonMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("更新课程成功")
    void updateLesson_shouldSuccess() {
        when(lessonMapper.selectById(1L)).thenReturn(testLesson);
        when(lessonMapper.updateById(any(Lesson.class))).thenReturn(1);

        UpdateLessonRequest request = new UpdateLessonRequest();
        request.setTitle("新标题");
        request.setContent("新内容");

        Lesson result = lessonService.updateLesson(1L, request);

        assertNotNull(result);
        assertEquals("新标题", result.getTitle());
        verify(lessonMapper, times(1)).updateById(any(Lesson.class));
    }

    @Test
    @DisplayName("删除课程成功")
    void deleteLesson_shouldSuccess() {
        when(lessonMapper.deleteById(1L)).thenReturn(1);

        lessonService.deleteLesson(1L);

        verify(lessonMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("根据学科ID查询课程列表")
    void getLessonsBySubjectId_shouldReturnList() {
        when(lessonMapper.selectBySubjectId(1L)).thenReturn(List.of(testLesson));

        List<Lesson> result = lessonService.getLessonsBySubjectId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(lessonMapper, times(1)).selectBySubjectId(1L);
    }

    @Test
    @DisplayName("根据日期范围查询课程")
    void listLessons_withDateRange_shouldReturnFiltered() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 31, 23, 59);

        Page<Lesson> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(testLesson));
        mockPage.setTotal(1);
        when(lessonMapper.selectPage(any(), any())).thenReturn(mockPage);

        IPage<Lesson> result = lessonService.listLessons(1L, 1, 10, null, startTime, endTime);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(lessonMapper, times(1)).selectPage(any(), any());
    }

    @Test
    @DisplayName("查询不存在的课程应返回null")
    void getLessonById_notFound_shouldReturnNull() {
        when(lessonMapper.selectById(999L)).thenReturn(null);

        Lesson result = lessonService.getLessonById(999L);

        assertNull(result);
    }
}
