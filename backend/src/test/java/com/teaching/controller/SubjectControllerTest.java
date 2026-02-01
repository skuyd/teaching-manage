package com.teaching.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CreateSubjectRequest;
import com.teaching.dto.UpdateSubjectRequest;
import com.teaching.entity.Subject;
import com.teaching.security.JwtUtils;
import com.teaching.security.UserDetailsServiceImpl;
import com.teaching.service.SubjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private Subject testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setName("Java高级编程");
        testSubject.setDescription("学习Java高级特性");
        testSubject.setIsGrouped(true);
        testSubject.setMinMembers(3);
        testSubject.setMaxMembers(5);
    }

    @Test
    @DisplayName("管理员可以获取学科列表")
    @WithMockUser(roles = "ADMIN")
    void listSubjects_shouldReturnPagedSubjects() throws Exception {
        IPage<Subject> page = new Page<>(1, 10);
        page.setRecords(List.of(testSubject));
        page.setTotal(1);

        when(subjectService.listSubjects(1, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/subjects")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].name").value("Java高级编程"));
    }

    @Test
    @DisplayName("教员可以获取学科列表")
    @WithMockUser(roles = "TEACHER")
    void listSubjects_shouldAllowTeacher() throws Exception {
        IPage<Subject> page = new Page<>(1, 10);
        page.setRecords(List.of(testSubject));
        page.setTotal(1);

        when(subjectService.listSubjects(1, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/subjects"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("管理员可以创建学科")
    @WithMockUser(roles = "ADMIN")
    void createSubject_shouldSucceed() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Python基础");
        request.setDescription("Python入门课程");
        request.setIsGrouped(false);

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(subjectService).createSubject(any(CreateSubjectRequest.class));
    }

    @Test
    @DisplayName("教员可以创建学科")
    @WithMockUser(roles = "TEACHER")
    void createSubject_shouldAllowTeacher() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Python基础");
        request.setIsGrouped(false);

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("学员不能创建学科")
    @WithMockUser(roles = "STUDENT")
    void createSubject_shouldForbidStudent() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Python基础");
        request.setIsGrouped(false);

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("获取学科详情")
    @WithMockUser(roles = "ADMIN")
    void getSubjectById_shouldReturnSubject() throws Exception {
        when(subjectService.getSubjectById(1L)).thenReturn(testSubject);

        mockMvc.perform(get("/api/subjects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Java高级编程"));
    }

    @Test
    @DisplayName("更新学科")
    @WithMockUser(roles = "ADMIN")
    void updateSubject_shouldSucceed() throws Exception {
        UpdateSubjectRequest request = new UpdateSubjectRequest();
        request.setName("Java高级编程（更新）");

        mockMvc.perform(put("/api/subjects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(subjectService).updateSubject(eq(1L), any(UpdateSubjectRequest.class));
    }

    @Test
    @DisplayName("删除学科")
    @WithMockUser(roles = "ADMIN")
    void deleteSubject_shouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/subjects/1"))
                .andExpect(status().isOk());

        verify(subjectService).deleteSubject(1L);
    }

    @Test
    @DisplayName("添加学员到学科")
    @WithMockUser(roles = "ADMIN")
    void addStudentToSubject_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/subjects/1/students/2"))
                .andExpect(status().isOk());

        verify(subjectService).addStudentToSubject(1L, 2L);
    }
}
