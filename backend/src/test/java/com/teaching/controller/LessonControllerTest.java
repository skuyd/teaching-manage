package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.dto.UpdateLessonRequest;
import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import com.teaching.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LessonService lessonService;

    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        // 创建测试课程
        CreateLessonRequest request = new CreateLessonRequest();
        request.setSubjectId(1L);
        request.setTitle("Java基础-第一课");
        request.setContent("## 课程内容\n- 变量\n- 数据类型");
        request.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        request.setHomeworkDesc("## 作业要求\n完成练习1-5");
        request.setSubmitType(SubmitType.PERSONAL);
        request.setDeadline(LocalDateTime.of(2024, 1, 20, 23, 59));
        request.setAllowLate(false);

        testLesson = lessonService.createLesson(request);
    }

    @Test
    @DisplayName("教师查询课程列表成功")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void listLessons_asTeacher_shouldSuccess() throws Exception {
        mockMvc.perform(get("/api/lessons")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("学员查询课程列表成功")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void listLessons_asStudent_shouldSuccess() throws Exception {
        mockMvc.perform(get("/api/lessons")
                        .param("subjectId", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("根据ID查询课程成功")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void getLessonById_shouldSuccess() throws Exception {
        mockMvc.perform(get("/api/lessons/{id}", testLesson.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testLesson.getId()))
                .andExpect(jsonPath("$.data.title").value("Java基础-第一课"));
    }

    @Test
    @DisplayName("教师创建课程成功")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void createLesson_asTeacher_shouldSuccess() throws Exception {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setSubjectId(1L);
        request.setTitle("Java基础-第二课");
        request.setContent("## 课程内容");
        request.setLessonTime(LocalDateTime.of(2024, 1, 16, 9, 0));
        request.setSubmitType(SubmitType.PERSONAL);
        request.setAllowLate(false);

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Java基础-第二课"));
    }

    @Test
    @DisplayName("学员创建课程应失败")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void createLesson_asStudent_shouldFail() throws Exception {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setSubjectId(1L);
        request.setTitle("Test Lesson");
        request.setLessonTime(LocalDateTime.now());
        request.setSubmitType(SubmitType.PERSONAL);
        request.setAllowLate(false);

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("教师更新课程成功")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void updateLesson_asTeacher_shouldSuccess() throws Exception {
        UpdateLessonRequest request = new UpdateLessonRequest();
        request.setTitle("更新后的标题");
        request.setContent("更新后的内容");

        mockMvc.perform(put("/api/lessons/{id}", testLesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("更新后的标题"));
    }

    @Test
    @DisplayName("学员更新课程应失败")
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void updateLesson_asStudent_shouldFail() throws Exception {
        UpdateLessonRequest request = new UpdateLessonRequest();
        request.setTitle("Test Update");

        mockMvc.perform(put("/api/lessons/{id}", testLesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("管理员删除课程成功")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteLesson_asAdmin_shouldSuccess() throws Exception {
        mockMvc.perform(delete("/api/lessons/{id}", testLesson.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("教师删除课程应失败")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void deleteLesson_asTeacher_shouldFail() throws Exception {
        mockMvc.perform(delete("/api/lessons/{id}", testLesson.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("创建课程时缺少必填字段应失败")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void createLesson_withMissingFields_shouldFail() throws Exception {
        CreateLessonRequest request = new CreateLessonRequest();
        // 缺少必填字段

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("根据学科ID查询课程列表")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void listLessons_bySubjectId_shouldSuccess() throws Exception {
        mockMvc.perform(get("/api/lessons")
                        .param("subjectId", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("根据日期范围查询课程")
    @WithMockUser(username = "teacher", roles = {"TEACHER"})
    void listLessons_byDateRange_shouldSuccess() throws Exception {
        mockMvc.perform(get("/api/lessons")
                        .param("startTime", "2024-01-01T00:00:00")
                        .param("endTime", "2024-01-31T23:59:59")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
