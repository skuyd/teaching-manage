package com.teaching.service;

import com.teaching.dto.SubmissionDTO;
import com.teaching.entity.Group;
import com.teaching.entity.GroupMember;
import com.teaching.entity.Lesson;
import com.teaching.entity.Subject;
import com.teaching.enums.MemberStatus;
import com.teaching.enums.SubmitType;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.GroupMapper;
import com.teaching.mapper.GroupMemberMapper;
import com.teaching.mapper.LessonMapper;
import com.teaching.mapper.SubjectMapper;
import com.teaching.mapper.SubmissionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SubmissionServiceTest {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private LessonMapper lessonMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private FileService fileService;

    private Lesson personalLesson;
    private Lesson groupLesson;
    private Subject testSubject;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        // 创建测试学科
        testSubject = new Subject();
        testSubject.setName("测试学科");
        testSubject.setDescription("测试描述");
        testSubject.setIsGrouped(true);
        testSubject.setMinMembers(3);
        testSubject.setMaxMembers(5);
        testSubject.setStartDate(LocalDate.now());
        testSubject.setEndDate(LocalDate.now().plusMonths(3));
        subjectMapper.insert(testSubject);

        // 创建个人作业课程
        personalLesson = new Lesson();
        personalLesson.setSubjectId(testSubject.getId());
        personalLesson.setTitle("个人作业课程");
        personalLesson.setContent("课程内容");
        personalLesson.setLessonTime(LocalDateTime.now());
        personalLesson.setHomeworkDesc("作业要求");
        personalLesson.setSubmitType(SubmitType.PERSONAL);
        personalLesson.setDeadline(LocalDateTime.now().plusDays(7));
        personalLesson.setAllowLate(true);
        lessonMapper.insert(personalLesson);

        // 创建小组作业课程
        groupLesson = new Lesson();
        groupLesson.setSubjectId(testSubject.getId());
        groupLesson.setTitle("小组作业课程");
        groupLesson.setContent("课程内容");
        groupLesson.setLessonTime(LocalDateTime.now());
        groupLesson.setHomeworkDesc("小组作业要求");
        groupLesson.setSubmitType(SubmitType.GROUP);
        groupLesson.setDeadline(LocalDateTime.now().plusDays(7));
        groupLesson.setAllowLate(true);
        lessonMapper.insert(groupLesson);

        // 创建测试小组
        testGroup = new Group();
        testGroup.setSubjectId(testSubject.getId());
        testGroup.setName("测试小组");
        testGroup.setLeaderId(2L);
        groupMapper.insert(testGroup);

        // 添加小组成员
        GroupMember leader = new GroupMember();
        leader.setGroupId(testGroup.getId());
        leader.setUserId(2L);
        leader.setStatus(MemberStatus.APPROVED);
        groupMemberMapper.insert(leader);
    }

    @Test
    @DisplayName("提交个人作业成功")
    void submitAssignment_personal_shouldSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "homework.txt",
                "text/plain",
                "My homework content".getBytes()
        );

        SubmissionDTO result = submissionService.submitAssignment(personalLesson.getId(), file, 1L);

        assertNotNull(result);
        assertEquals(personalLesson.getId(), result.getLessonId());
        assertEquals(1L, result.getSubmitterId());
        assertNull(result.getGroupId());
        assertFalse(result.getIsGroupSubmission());

        // 清理
        if (result.getFilePath() != null) {
            fileService.deleteDirectory(result.getFilePath());
        }
    }

    @Test
    @DisplayName("提交小组作业成功")
    void submitAssignment_group_shouldSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "group-homework.txt",
                "text/plain",
                "Group homework content".getBytes()
        );

        SubmissionDTO result = submissionService.submitAssignment(groupLesson.getId(), file, 2L);

        assertNotNull(result);
        assertEquals(groupLesson.getId(), result.getLessonId());
        assertEquals(2L, result.getSubmitterId());
        assertEquals(testGroup.getId(), result.getGroupId());
        assertTrue(result.getIsGroupSubmission());

        // 清理
        if (result.getFilePath() != null) {
            fileService.deleteDirectory(result.getFilePath());
        }
    }

    @Test
    @DisplayName("重复提交应抛出异常")
    void submitAssignment_duplicate_shouldThrowException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "homework.txt",
                "text/plain",
                "My homework".getBytes()
        );

        // 第一次提交
        SubmissionDTO first = submissionService.submitAssignment(personalLesson.getId(), file, 1L);

        // 第二次提交应抛出异常
        assertThrows(BusinessException.class, () -> {
            submissionService.submitAssignment(personalLesson.getId(), file, 1L);
        });

        // 清理
        if (first.getFilePath() != null) {
            fileService.deleteDirectory(first.getFilePath());
        }
    }

    @Test
    @DisplayName("小组作业未加入小组应抛出异常")
    void submitAssignment_groupWithoutMembership_shouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "homework.txt",
                "text/plain",
                "My homework".getBytes()
        );

        // 用户1未加入小组
        assertThrows(BusinessException.class, () -> {
            submissionService.submitAssignment(groupLesson.getId(), file, 1L);
        });
    }

    @Test
    @DisplayName("重新提交作业成功")
    void resubmitAssignment_shouldSuccess() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "homework-v1.txt",
                "text/plain",
                "Version 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "homework-v2.txt",
                "text/plain",
                "Version 2".getBytes()
        );

        // 首次提交
        SubmissionDTO first = submissionService.submitAssignment(personalLesson.getId(), file1, 1L);
        Long firstId = first.getId();

        // 重新提交
        SubmissionDTO second = submissionService.resubmitAssignment(personalLesson.getId(), file2, 1L);

        assertNotNull(second);
        assertNotEquals(firstId, second.getId());  // 新的提交ID

        // 清理
        if (second.getFilePath() != null) {
            fileService.deleteDirectory(second.getFilePath());
        }
    }

    @Test
    @DisplayName("获取我的提交")
    void getMySubmission_shouldSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "homework.txt",
                "text/plain",
                "My homework".getBytes()
        );

        submissionService.submitAssignment(personalLesson.getId(), file, 1L);

        SubmissionDTO result = submissionService.getMySubmission(personalLesson.getId(), 1L);

        assertNotNull(result);
        assertEquals(1L, result.getSubmitterId());

        // 清理
        if (result.getFilePath() != null) {
            fileService.deleteDirectory(result.getFilePath());
        }
    }

    @Test
    @DisplayName("获取不存在的提交返回null")
    void getMySubmission_notExists_shouldReturnNull() {
        SubmissionDTO result = submissionService.getMySubmission(personalLesson.getId(), 1L);
        assertNull(result);
    }
}
