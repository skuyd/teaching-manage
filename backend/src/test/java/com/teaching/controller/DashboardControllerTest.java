package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.service.UserService;
import com.teaching.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private User admin;
    private User teacher;
    private User student;
    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        // 创建管理员
        admin = new User();
        admin.setUsername("dashboard_admin");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setName("Dashboard Admin");
        admin.setRole(UserRole.ADMIN);
        admin.setEmail("dashboard_admin@test.com");
        userService.save(admin);
        adminToken = jwtUtils.generateToken(admin.getUsername());

        // 创建教师
        teacher = new User();
        teacher.setUsername("dashboard_teacher");
        teacher.setPassword(passwordEncoder.encode("password123"));
        teacher.setName("Dashboard Teacher");
        teacher.setRole(UserRole.TEACHER);
        teacher.setEmail("dashboard_teacher@test.com");
        userService.save(teacher);
        teacherToken = jwtUtils.generateToken(teacher.getUsername());

        // 创建学生
        student = new User();
        student.setUsername("dashboard_student");
        student.setPassword(passwordEncoder.encode("password123"));
        student.setName("Dashboard Student");
        student.setRole(UserRole.STUDENT);
        student.setEmail("dashboard_student@test.com");
        userService.save(student);
        studentToken = jwtUtils.generateToken(student.getUsername());
    }

    @Test
    @DisplayName("管理员可以获取管理员统计数据")
    void getAdminStats_asAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").isNumber())
                .andExpect(jsonPath("$.data.totalSubjects").isNumber())
                .andExpect(jsonPath("$.data.totalLessons").isNumber())
                .andExpect(jsonPath("$.data.totalSubmissions").isNumber());
    }

    @Test
    @DisplayName("教师不能访问管理员统计数据")
    void getAdminStats_asTeacher_shouldFail() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin/stats")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("学生不能访问管理员统计数据")
    void getAdminStats_asStudent_shouldFail() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin/stats")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("教师可以获取教师统计数据")
    void getTeacherStats_asTeacher_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/dashboard/teacher/stats")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.mySubjects").isNumber())
                .andExpect(jsonPath("$.data.pendingGrades").isNumber())
                .andExpect(jsonPath("$.data.completedGrades").isNumber());
    }

    @Test
    @DisplayName("学生不能访问教师统计数据")
    void getTeacherStats_asStudent_shouldFail() throws Exception {
        mockMvc.perform(get("/api/dashboard/teacher/stats")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("学生可以获取学生统计数据")
    void getStudentStats_asStudent_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/dashboard/student/stats")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.mySubjects").isNumber())
                .andExpect(jsonPath("$.data.submittedAssignments").isNumber())
                .andExpect(jsonPath("$.data.gradedAssignments").isNumber())
                .andExpect(jsonPath("$.data.averageGrade").isNumber());
    }

    @Test
    @DisplayName("教师不能访问学生统计数据")
    void getStudentStats_asTeacher_shouldFail() throws Exception {
        mockMvc.perform(get("/api/dashboard/student/stats")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("未认证用户无法访问Dashboard API")
    void dashboardEndpoints_withoutAuth_shouldFail() throws Exception {
        mockMvc.perform(get("/api/dashboard/admin/stats"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/dashboard/teacher/stats"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/dashboard/student/stats"))
                .andExpect(status().isUnauthorized());
    }
}
