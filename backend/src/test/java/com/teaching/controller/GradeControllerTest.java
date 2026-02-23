package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CreateGradeRequest;
import com.teaching.entity.User;
import com.teaching.entity.Subject;
import com.teaching.entity.Lesson;
import com.teaching.entity.Submission;
import com.teaching.enums.UserRole;
import com.teaching.enums.GradeLevel;
import com.teaching.enums.SubmitType;
import com.teaching.service.UserService;
import com.teaching.service.SubjectService;
import com.teaching.service.LessonService;
import com.teaching.service.SubmissionService;
import com.teaching.service.GradeService;
import com.teaching.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private User teacher;
    private User student;
    private Subject subject;
    private Lesson lesson;
    private Submission submission;
    private String teacherToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        // 创建教师
        teacher = new User();
        teacher.setUsername("grade_teacher");
        teacher.setPassword(passwordEncoder.encode("password123"));
        teacher.setName("Grade Teacher");
        teacher.setRole(UserRole.TEACHER);
        teacher.setEmail("grade_teacher@test.com");
        userService.save(teacher);
        teacherToken = jwtUtils.generateToken(teacher.getUsername());

        // 创建学生
        student = new User();
        student.setUsername("grade_student");
        student.setPassword(passwordEncoder.encode("password123"));
        student.setName("Grade Student");
        student.setRole(UserRole.STUDENT);
        student.setEmail("grade_student@test.com");
        userService.save(student);
        studentToken = jwtUtils.generateToken(student.getUsername());

        // 创建学科
        subject = new Subject();
        subject.setName("Test Subject");
        subject.setDescription("Test Description");
        subject.setCreateBy(teacher.getId().toString());
        subjectService.save(subject);

        // 创建课程
        lesson = new Lesson();
        lesson.setSubjectId(subject.getId());
        lesson.setTitle("Test Lesson");
        lesson.setContent("Test Content");
        lesson.setLessonTime(LocalDateTime.now());
        lesson.setSubmitType(SubmitType.PERSONAL);
        lesson.setDeadline(LocalDateTime.now().plusDays(7));
        lesson.setCreateBy(teacher.getId().toString());
        lessonService.save(lesson);

        // 创建提交
        submission = new Submission();
        submission.setLessonId(lesson.getId());
        submission.setSubmitterId(student.getId());
        submission.setFilePath("/test/path");
        submission.setSubmitTime(LocalDateTime.now());
        submission.setCreateBy(student.getId().toString());
        submissionService.save(submission);
    }

    @Test
    @DisplayName("教师可以评分")
    void gradeSubmission_asTeacher_shouldSucceed() throws Exception {
        CreateGradeRequest request = new CreateGradeRequest();
        request.setSubmissionId(submission.getId());
        request.setGrade(GradeLevel.A);
        request.setComment("Excellent work!");

        mockMvc.perform(post("/api/grades")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.grade").value("A"))
                .andExpect(jsonPath("$.data.comment").value("Excellent work!"));
    }

    @Test
    @DisplayName("学生不能评分")
    void gradeSubmission_asStudent_shouldFail() throws Exception {
        CreateGradeRequest request = new CreateGradeRequest();
        request.setSubmissionId(submission.getId());
        request.setGrade(GradeLevel.A);

        mockMvc.perform(post("/api/grades")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("根据提交ID获取评分")
    void getGradeBySubmission_shouldReturnGrade() throws Exception {
        // 先创建评分
        CreateGradeRequest request = new CreateGradeRequest();
        request.setSubmissionId(submission.getId());
        request.setGrade(GradeLevel.B);
        request.setComment("Good work");
        gradeService.gradeSubmission(request, teacher.getId());

        mockMvc.perform(get("/api/grades/submission/" + submission.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.grade").value("B"));
    }

    @Test
    @DisplayName("根据课程ID获取所有评分")
    void getGradesByLesson_asTeacher_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/grades/lesson/" + lesson.getId())
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("学生只能查看自己的评分")
    void getGradesByStudent_ownGrades_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/grades/student/" + student.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("学生不能查看他人评分")
    void getGradesByStudent_otherStudentGrades_shouldFail() throws Exception {
        // 创建另一个学生
        User otherStudent = new User();
        otherStudent.setUsername("other_student");
        otherStudent.setPassword(passwordEncoder.encode("password123"));
        otherStudent.setName("Other Student");
        otherStudent.setRole(UserRole.STUDENT);
        otherStudent.setEmail("other@test.com");
        userService.save(otherStudent);

        mockMvc.perform(get("/api/grades/student/" + otherStudent.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("获取课程评分统计")
    void getLessonStatistics_asTeacher_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/grades/lesson/" + lesson.getId() + "/statistics")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未认证用户无法访问评分API")
    void gradeEndpoints_withoutAuth_shouldFail() throws Exception {
        mockMvc.perform(get("/api/grades/lesson/" + lesson.getId()))
                .andExpect(status().isUnauthorized());
    }
}
