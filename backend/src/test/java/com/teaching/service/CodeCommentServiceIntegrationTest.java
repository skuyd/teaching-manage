package com.teaching.service;

import com.teaching.dto.CodeCommentDTO;
import com.teaching.dto.CreateCodeCommentRequest;
import com.teaching.dto.FileCommentsDTO;
import com.teaching.entity.*;
import com.teaching.enums.UserRole;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("代码评论服务集成测试")
class CodeCommentServiceIntegrationTest {

    @Autowired
    private CodeCommentService codeCommentService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private LessonMapper lessonMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private CodeCommentMapper codeCommentMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User teacher;
    private User student;
    private User admin;
    private Subject subject;
    private Lesson lesson;
    private Submission submission;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        teacher = new User();
        teacher.setUsername("teacher1");
        teacher.setPassword(passwordEncoder.encode("password"));
        teacher.setName("Teacher One");
        teacher.setRole(UserRole.TEACHER);
        teacher.setEmail("teacher1@test.com");
        userMapper.insert(teacher);

        student = new User();
        student.setUsername("student1");
        student.setPassword(passwordEncoder.encode("password"));
        student.setName("Student One");
        student.setRole(UserRole.STUDENT);
        student.setEmail("student1@test.com");
        userMapper.insert(student);

        admin = new User();
        admin.setUsername("admin1");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setName("Admin One");
        admin.setRole(UserRole.ADMIN);
        admin.setEmail("admin1@test.com");
        userMapper.insert(admin);

        // 创建测试学科
        subject = new Subject();
        subject.setName("测试学科");
        subject.setDescription("集成测试学科");
        subject.setIsGrouped(false);
        subject.setMinMembers(1);
        subject.setMaxMembers(1);
        subjectMapper.insert(subject);

        // 创建测试课程
        lesson = new Lesson();
        lesson.setTitle("测试课程");
        lesson.setContent("测试内容");
        lesson.setSubjectId(subject.getId());
        lesson.setLessonTime(LocalDateTime.now());
        lesson.setDeadline(LocalDateTime.now().plusDays(7));
        lessonMapper.insert(lesson);

        // 创建测试提交
        submission = new Submission();
        submission.setLessonId(lesson.getId());
        submission.setSubmitterId(student.getId());
        submission.setFilePath("/test/Main.java");
        submission.setSubmitTime(LocalDateTime.now());
        submissionMapper.insert(submission);
    }

    @Test
    @DisplayName("添加代码评论成功")
    void addComment_shouldCreateCommentSuccessfully() {
        // Given
        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(submission.getId());
        request.setFilePath("Main.java");
        request.setLineNumber(10);
        request.setContent("这里应该添加输入验证");

        // When
        CodeCommentDTO result = codeCommentService.addComment(request, teacher.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getContent()).isEqualTo("这里应该添加输入验证");
        assertThat(result.getFilePath()).isEqualTo("Main.java");
        assertThat(result.getLineNumber()).isEqualTo(10);
        assertThat(result.getCommenterId()).isEqualTo(teacher.getId());

        // 验证数据库中的记录
        CodeComment savedComment = codeCommentMapper.selectById(result.getId());
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getContent()).isEqualTo("这里应该添加输入验证");
    }

    @Test
    @DisplayName("添加评论到不存在的提交应抛出异常")
    void addComment_withNonExistentSubmission_shouldThrowException() {
        // Given
        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(99999L);
        request.setFilePath("Main.java");
        request.setLineNumber(10);
        request.setContent("测试评论");

        // When & Then
        assertThatThrownBy(() -> codeCommentService.addComment(request, teacher.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("提交记录不存在");
    }

    @Test
    @DisplayName("更新评论成功")
    void updateComment_byOwner_shouldUpdateSuccessfully() {
        // Given
        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(submission.getId());
        request.setFilePath("Main.java");
        request.setLineNumber(10);
        request.setContent("原始评论");

        CodeCommentDTO created = codeCommentService.addComment(request, teacher.getId());

        // When
        CodeCommentDTO updated = codeCommentService.updateComment(
                created.getId(),
                "更新后的评论",
                teacher.getId()
        );

        // Then
        assertThat(updated.getContent()).isEqualTo("更新后的评论");

        // 验证数据库中的记录
        CodeComment savedComment = codeCommentMapper.selectById(created.getId());
        assertThat(savedComment.getContent()).isEqualTo("更新后的评论");
    }

    @Test
    @DisplayName("非评论所有者更新评论应抛出异常")
    void updateComment_byNonOwner_shouldThrowException() {
        // Given
        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(submission.getId());
        request.setFilePath("Main.java");
        request.setLineNumber(10);
        request.setContent("原始评论");

        CodeCommentDTO created = codeCommentService.addComment(request, teacher.getId());

        // When & Then
        assertThatThrownBy(() -> codeCommentService.updateComment(
                created.getId(),
                "非法更新",
                student.getId()
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权编辑该评论");
    }

    @Test
    @DisplayName("更新不存在的评论应抛出异常")
    void updateComment_withNonExistentComment_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> codeCommentService.updateComment(99999L, "测试", teacher.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("评论不存在");
    }

    @Test
    @DisplayName("评论所有者删除评论成功")
    void deleteComment_byOwner_shouldDeleteSuccessfully() {
        // Given
        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(submission.getId());
        request.setFilePath("Main.java");
        request.setLineNumber(10);
        request.setContent("要删除的评论");

        CodeCommentDTO created = codeCommentService.addComment(request, teacher.getId());

        // When
        codeCommentService.deleteComment(created.getId(), teacher.getId());

        // Then
        CodeComment deleted = codeCommentMapper.selectById(created.getId());
        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("管理员可以删除他人评论")
    void deleteComment_byAdmin_shouldDeleteSuccessfully() {
        // Given
        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(submission.getId());
        request.setFilePath("Main.java");
        request.setLineNumber(10);
        request.setContent("要删除的评论");

        CodeCommentDTO created = codeCommentService.addComment(request, teacher.getId());

        // When
        codeCommentService.deleteComment(created.getId(), admin.getId());

        // Then
        CodeComment deleted = codeCommentMapper.selectById(created.getId());
        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("学生不能删除他人评论")
    void deleteComment_byNonOwnerStudent_shouldThrowException() {
        // Given
        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(submission.getId());
        request.setFilePath("Main.java");
        request.setLineNumber(10);
        request.setContent("要删除的评论");

        CodeCommentDTO created = codeCommentService.addComment(request, teacher.getId());

        // When & Then
        assertThatThrownBy(() -> codeCommentService.deleteComment(created.getId(), student.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权删除该评论");
    }

    @Test
    @DisplayName("删除不存在的评论应抛出异常")
    void deleteComment_withNonExistentComment_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> codeCommentService.deleteComment(99999L, teacher.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("评论不存在");
    }

    @Test
    @DisplayName("按提交ID获取评论")
    void getCommentsBySubmission_shouldReturnAllComments() {
        // Given
        CreateCodeCommentRequest request1 = new CreateCodeCommentRequest();
        request1.setSubmissionId(submission.getId());
        request1.setFilePath("Main.java");
        request1.setLineNumber(10);
        request1.setContent("评论1");
        codeCommentService.addComment(request1, teacher.getId());

        CreateCodeCommentRequest request2 = new CreateCodeCommentRequest();
        request2.setSubmissionId(submission.getId());
        request2.setFilePath("Utils.java");
        request2.setLineNumber(20);
        request2.setContent("评论2");
        codeCommentService.addComment(request2, teacher.getId());

        // When
        List<CodeCommentDTO> comments = codeCommentService.getCommentsBySubmission(submission.getId());

        // Then
        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(CodeCommentDTO::getContent)
                .containsExactlyInAnyOrder("评论1", "评论2");
    }

    @Test
    @DisplayName("按文件路径获取评论")
    void getCommentsByFile_shouldReturnFileComments() {
        // Given
        CreateCodeCommentRequest request1 = new CreateCodeCommentRequest();
        request1.setSubmissionId(submission.getId());
        request1.setFilePath("Main.java");
        request1.setLineNumber(10);
        request1.setContent("Main评论1");
        codeCommentService.addComment(request1, teacher.getId());

        CreateCodeCommentRequest request2 = new CreateCodeCommentRequest();
        request2.setSubmissionId(submission.getId());
        request2.setFilePath("Main.java");
        request2.setLineNumber(20);
        request2.setContent("Main评论2");
        codeCommentService.addComment(request2, teacher.getId());

        CreateCodeCommentRequest request3 = new CreateCodeCommentRequest();
        request3.setSubmissionId(submission.getId());
        request3.setFilePath("Utils.java");
        request3.setLineNumber(10);
        request3.setContent("Utils评论");
        codeCommentService.addComment(request3, teacher.getId());

        // When
        List<CodeCommentDTO> mainComments = codeCommentService.getCommentsByFile(
                submission.getId(), "Main.java");

        // Then
        assertThat(mainComments).hasSize(2);
        assertThat(mainComments).extracting(CodeCommentDTO::getContent)
                .containsExactlyInAnyOrder("Main评论1", "Main评论2");
    }

    @Test
    @DisplayName("按行号获取评论")
    void getCommentsByLine_shouldReturnLineComments() {
        // Given
        CreateCodeCommentRequest request1 = new CreateCodeCommentRequest();
        request1.setSubmissionId(submission.getId());
        request1.setFilePath("Main.java");
        request1.setLineNumber(10);
        request1.setContent("第10行评论1");
        codeCommentService.addComment(request1, teacher.getId());

        CreateCodeCommentRequest request2 = new CreateCodeCommentRequest();
        request2.setSubmissionId(submission.getId());
        request2.setFilePath("Main.java");
        request2.setLineNumber(10);
        request2.setContent("第10行评论2");
        codeCommentService.addComment(request2, teacher.getId());

        CreateCodeCommentRequest request3 = new CreateCodeCommentRequest();
        request3.setSubmissionId(submission.getId());
        request3.setFilePath("Main.java");
        request3.setLineNumber(20);
        request3.setContent("第20行评论");
        codeCommentService.addComment(request3, teacher.getId());

        // When
        List<CodeCommentDTO> line10Comments = codeCommentService.getCommentsByLine(
                submission.getId(), "Main.java", 10);

        // Then
        assertThat(line10Comments).hasSize(2);
        assertThat(line10Comments).extracting(CodeCommentDTO::getContent)
                .containsExactlyInAnyOrder("第10行评论1", "第10行评论2");
    }

    @Test
    @DisplayName("获取按文件分组的评论")
    void getCommentsGroupedByFile_shouldReturnGroupedComments() {
        // Given
        CreateCodeCommentRequest request1 = new CreateCodeCommentRequest();
        request1.setSubmissionId(submission.getId());
        request1.setFilePath("Main.java");
        request1.setLineNumber(10);
        request1.setContent("Main第10行");
        codeCommentService.addComment(request1, teacher.getId());

        CreateCodeCommentRequest request2 = new CreateCodeCommentRequest();
        request2.setSubmissionId(submission.getId());
        request2.setFilePath("Main.java");
        request2.setLineNumber(20);
        request2.setContent("Main第20行");
        codeCommentService.addComment(request2, teacher.getId());

        CreateCodeCommentRequest request3 = new CreateCodeCommentRequest();
        request3.setSubmissionId(submission.getId());
        request3.setFilePath("Utils.java");
        request3.setLineNumber(5);
        request3.setContent("Utils第5行");
        codeCommentService.addComment(request3, teacher.getId());

        // When
        List<FileCommentsDTO> grouped = codeCommentService.getCommentsGroupedByFile(submission.getId());

        // Then
        assertThat(grouped).hasSize(2);

        FileCommentsDTO mainFile = grouped.stream()
                .filter(f -> f.getFilePath().equals("Main.java"))
                .findFirst()
                .orElseThrow();
        assertThat(mainFile.getTotalComments()).isEqualTo(2);
        assertThat(mainFile.getCommentsByLine()).containsKeys(10, 20);

        FileCommentsDTO utilsFile = grouped.stream()
                .filter(f -> f.getFilePath().equals("Utils.java"))
                .findFirst()
                .orElseThrow();
        assertThat(utilsFile.getTotalComments()).isEqualTo(1);
        assertThat(utilsFile.getCommentsByLine()).containsKey(5);
    }

    @Test
    @DisplayName("空提交没有评论应返回空列表")
    void getCommentsBySubmission_withNoComments_shouldReturnEmptyList() {
        // When
        List<CodeCommentDTO> comments = codeCommentService.getCommentsBySubmission(submission.getId());

        // Then
        assertThat(comments).isEmpty();
    }
}
