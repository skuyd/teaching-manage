package com.teaching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.dto.FileTreeNode;
import com.teaching.dto.SubmissionDTO;
import com.teaching.dto.SubmissionDetailDTO;
import com.teaching.entity.Group;
import com.teaching.entity.GroupMember;
import com.teaching.entity.Lesson;
import com.teaching.entity.Submission;
import com.teaching.entity.User;
import com.teaching.enums.MemberStatus;
import com.teaching.enums.SubmitType;
import com.teaching.enums.UserRole;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.GradeMapper;
import com.teaching.mapper.GroupMapper;
import com.teaching.mapper.GroupMemberMapper;
import com.teaching.mapper.LessonMapper;
import com.teaching.mapper.SubmissionMapper;
import com.teaching.mapper.UserMapper;
import com.teaching.service.FileService;
import com.teaching.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final LessonMapper lessonMapper;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GradeMapper gradeMapper;
    private final FileService fileService;

    @Override
    @Transactional
    public SubmissionDTO submitAssignment(Long lessonId, MultipartFile file, Long userId) throws IOException {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 检查是否已提交
        Submission existing = getExistingSubmission(lessonId, userId, lesson.getSubmitType());
        if (existing != null) {
            throw new BusinessException(400, "已提交过作业，请使用重新提交");
        }

        return doSubmit(lesson, file, userId);
    }

    @Override
    @Transactional
    public SubmissionDTO resubmitAssignment(Long lessonId, MultipartFile file, Long userId) throws IOException {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 检查是否已提交
        Submission existing = getExistingSubmission(lessonId, userId, lesson.getSubmitType());
        if (existing == null) {
            throw new BusinessException(400, "尚未提交过作业，请先提交");
        }

        // 检查是否允许补交
        if (!lesson.getAllowLate() && lesson.getDeadline() != null && LocalDateTime.now().isAfter(lesson.getDeadline())) {
            throw new BusinessException(400, "已超过截止时间且不允许补交");
        }

        // 删除旧文件
        try {
            fileService.deleteDirectory(existing.getFilePath());
        } catch (IOException e) {
            log.warn("删除旧文件失败: {}", existing.getFilePath(), e);
        }

        // 删除旧提交记录
        submissionMapper.deleteById(existing.getId());

        return doSubmit(lesson, file, userId);
    }

    private Submission getExistingSubmission(Long lessonId, Long userId, SubmitType submitType) {
        if (submitType == SubmitType.PERSONAL) {
            return submissionMapper.findByLessonAndSubmitter(lessonId, userId);
        } else {
            // 小组作业：查找用户所在小组
            Group group = groupMapper.selectByUserIdAndSubjectId(userId, getLessonSubjectId(lessonId));
            if (group == null) {
                throw new BusinessException(400, "小组作业需要先加入小组");
            }
            return submissionMapper.findByLessonAndGroup(lessonId, group.getId());
        }
    }

    private SubmissionDTO doSubmit(Lesson lesson, MultipartFile file, Long userId) throws IOException {
        Long targetId;
        Long groupId = null;
        boolean isGroupSubmission = lesson.getSubmitType() == SubmitType.GROUP;

        if (isGroupSubmission) {
            Group group = groupMapper.selectByUserIdAndSubjectId(userId, lesson.getSubjectId());
            if (group == null) {
                throw new BusinessException(400, "小组作业需要先加入小组");
            }

            // 验证小组成员状态
            GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(group.getId(), userId);
            if (member == null || member.getStatus() != MemberStatus.APPROVED) {
                throw new BusinessException(400, "您不是小组的正式成员，无法提交");
            }

            targetId = group.getId();
            groupId = group.getId();
        } else {
            targetId = userId;
        }

        // 上传文件
        String filePath = fileService.uploadFile(file, lesson.getId(), targetId, isGroupSubmission);

        // 创建提交记录
        Submission submission = new Submission();
        submission.setLessonId(lesson.getId());
        submission.setSubmitterId(userId);
        submission.setGroupId(groupId);
        submission.setFilePath(filePath);
        submission.setSubmitTime(LocalDateTime.now());
        submissionMapper.insert(submission);

        log.info("作业提交成功: lessonId={}, userId={}, groupId={}", lesson.getId(), userId, groupId);

        return convertToDTO(submission);
    }

    @Override
    public List<SubmissionDTO> getSubmissionsByLessonId(Long lessonId) {
        List<Submission> submissions = submissionMapper.findByLessonId(lessonId);
        List<SubmissionDTO> dtos = new ArrayList<>();
        for (Submission submission : submissions) {
            dtos.add(convertToDTO(submission));
        }
        return dtos;
    }

    @Override
    public SubmissionDetailDTO getSubmissionDetail(Long id, Long userId) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }

        // 权限验证：教师/管理员/本人/同组成员可查看
        if (!canViewSubmission(submission, userId)) {
            throw new BusinessException(403, "无权查看该提交");
        }

        return convertToDetailDTO(submission);
    }

    @Override
    public SubmissionDTO getMySubmission(Long lessonId, Long userId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) {
            throw new BusinessException(404, "课程不存在");
        }

        Submission submission = getExistingSubmission(lessonId, userId, lesson.getSubmitType());
        if (submission == null) {
            return null;
        }

        return convertToDTO(submission);
    }

    @Override
    @Transactional
    public void deleteSubmission(Long id, Long userId) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }

        // 仅提交者本人可删除
        if (!submission.getSubmitterId().equals(userId)) {
            throw new BusinessException(403, "无权删除该提交");
        }

        // 删除文件
        try {
            fileService.deleteDirectory(submission.getFilePath());
        } catch (IOException e) {
            log.error("删除文件失败: {}", submission.getFilePath(), e);
        }

        submissionMapper.deleteById(id);
        log.info("作业提交已删除: id={}, userId={}", id, userId);
    }

    @Override
    public FileTreeNode getFileTree(Long submissionId) throws IOException {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }
        return fileService.generateDirectoryTree(submission.getFilePath());
    }

    @Override
    public String readFileContent(Long submissionId, String relativePath) throws IOException {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }

        String fullPath = Paths.get(submission.getFilePath(), relativePath).toString();
        return fileService.readFileContent(fullPath);
    }

    private boolean canViewSubmission(Submission submission, Long userId) {
        // 提交者本人
        if (submission.getSubmitterId().equals(userId)) {
            return true;
        }

        // 同组成员
        if (submission.getGroupId() != null) {
            GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(submission.getGroupId(), userId);
            if (member != null && member.getStatus() == MemberStatus.APPROVED) {
                return true;
            }
        }

        // 教师/管理员（通过用户角色判断）
        User user = userMapper.selectById(userId);
        return user != null && (user.getRole() == UserRole.TEACHER || user.getRole() == UserRole.ADMIN);
    }

    private Long getLessonSubjectId(Long lessonId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        return lesson != null ? lesson.getSubjectId() : null;
    }

    private SubmissionDTO convertToDTO(Submission submission) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(submission.getId());
        dto.setLessonId(submission.getLessonId());
        dto.setSubmitterId(submission.getSubmitterId());
        dto.setGroupId(submission.getGroupId());
        dto.setFilePath(submission.getFilePath());
        dto.setSubmitTime(submission.getSubmitTime());
        dto.setIsGroupSubmission(submission.getGroupId() != null);

        // 加载课程信息
        Lesson lesson = lessonMapper.selectById(submission.getLessonId());
        if (lesson != null) {
            dto.setLessonTitle(lesson.getTitle());
        }

        // 加载提交者信息
        User submitter = userMapper.selectById(submission.getSubmitterId());
        if (submitter != null) {
            dto.setSubmitterName(submitter.getName());
        }

        // 加载小组信息
        if (submission.getGroupId() != null) {
            Group group = groupMapper.selectById(submission.getGroupId());
            if (group != null) {
                dto.setGroupName(group.getName());
            }
        }

        // 查询是否已评分
        dto.setGraded(gradeMapper.findBySubmissionId(submission.getId()) != null);

        return dto;
    }

    private SubmissionDetailDTO convertToDetailDTO(Submission submission) {
        SubmissionDetailDTO dto = new SubmissionDetailDTO();
        dto.setId(submission.getId());
        dto.setLessonId(submission.getLessonId());
        dto.setSubmitterId(submission.getSubmitterId());
        dto.setGroupId(submission.getGroupId());
        dto.setFilePath(submission.getFilePath());
        dto.setSubmitTime(submission.getSubmitTime());

        // 加载课程信息
        Lesson lesson = lessonMapper.selectById(submission.getLessonId());
        if (lesson != null) {
            dto.setLessonTitle(lesson.getTitle());
            dto.setHomeworkDesc(lesson.getHomeworkDesc());
        }

        // 加载提交者信息
        User submitter = userMapper.selectById(submission.getSubmitterId());
        if (submitter != null) {
            dto.setSubmitterName(submitter.getName());
        }

        // 加载小组信息
        if (submission.getGroupId() != null) {
            Group group = groupMapper.selectById(submission.getGroupId());
            if (group != null) {
                dto.setGroupName(group.getName());
            }
        }

        // 加载文件树
        try {
            dto.setFileTree(fileService.generateDirectoryTree(submission.getFilePath()));
        } catch (IOException e) {
            log.error("生成文件树失败: {}", submission.getFilePath(), e);
        }

        return dto;
    }
}
