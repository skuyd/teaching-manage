package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.CreateSubjectRequest;
import com.teaching.dto.SubjectDeleteStatsDTO;
import com.teaching.dto.UpdateSubjectRequest;
import com.teaching.dto.UserDTO;
import com.teaching.entity.Subject;
import com.teaching.entity.SubjectStudent;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.*;
import com.teaching.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    private final SubjectMapper subjectMapper;
    private final SubjectStudentMapper subjectStudentMapper;
    private final UserMapper userMapper;
    private final LessonMapper lessonMapper;
    private final SubmissionMapper submissionMapper;
    private final GradeMapper gradeMapper;
    private final CodeCommentMapper codeCommentMapper;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;

    @Override
    @Transactional
    public void createSubject(CreateSubjectRequest request) {
        Subject subject = request.toEntity();
        subjectMapper.insert(subject);
    }

    @Override
    public IPage<Subject> listSubjects(int page, int size, String keyword) {
        Page<Subject> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Subject::getName, keyword);
        }
        wrapper.orderByDesc(Subject::getCreateTime);

        return subjectMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Subject getSubjectById(Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "学科不存在");
        }
        return subject;
    }

    @Override
    @Transactional
    public void updateSubject(Long id, UpdateSubjectRequest request) {
        Subject subject = getSubjectById(id);
        request.updateEntity(subject);
        subjectMapper.updateById(subject);
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = getSubjectById(id);
        log.info("开始物理删除学科: id={}, name={}", id, subject.getName());

        // 1. 删除代码评论（最底层）
        int commentCount = codeCommentMapper.physicalDeleteBySubjectId(id);
        log.info("删除代码评论: {} 条", commentCount);

        // 2. 删除评分
        int gradeCount = gradeMapper.physicalDeleteBySubjectId(id);
        log.info("删除评分: {} 条", gradeCount);

        // 3. 删除作业提交
        int submissionCount = submissionMapper.physicalDeleteBySubjectId(id);
        log.info("删除作业提交: {} 条", submissionCount);

        // 4. 删除课程
        int lessonCount = lessonMapper.physicalDeleteBySubjectId(id);
        log.info("删除课程: {} 条", lessonCount);

        // 5. 删除小组成员关系
        int memberCount = groupMemberMapper.physicalDeleteBySubjectId(id);
        log.info("删除小组成员: {} 条", memberCount);

        // 6. 删除小组
        int groupCount = groupMapper.physicalDeleteBySubjectId(id);
        log.info("删除小组: {} 条", groupCount);

        // 7. 删除学员-学科关系
        int studentCount = subjectStudentMapper.physicalDeleteBySubjectId(id);
        log.info("删除学员关系: {} 条", studentCount);

        // 8. 删除学科本身
        subjectMapper.physicalDeleteById(id);
        log.info("学科物理删除完成: id={}", id);
    }

    @Override
    @Transactional
    public void addStudentToSubject(Long subjectId, Long studentId) {
        // 检查是否已存在
        Long count = subjectStudentMapper.countBySubjectAndStudent(subjectId, studentId);
        if (count > 0) {
            return;  // 已存在，不重复添加
        }

        SubjectStudent ss = new SubjectStudent();
        ss.setSubjectId(subjectId);
        ss.setStudentId(studentId);
        subjectStudentMapper.insert(ss);
    }

    @Override
    @Transactional
    public void removeStudentFromSubject(Long subjectId, Long studentId) {
        LambdaQueryWrapper<SubjectStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectStudent::getSubjectId, subjectId)
                .eq(SubjectStudent::getStudentId, studentId);
        subjectStudentMapper.delete(wrapper);
    }

    @Override
    public List<Subject> getSubjectsByStudentId(Long studentId) {
        return subjectMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Long> getStudentIdsBySubjectId(Long subjectId) {
        LambdaQueryWrapper<SubjectStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectStudent::getSubjectId, subjectId);
        return subjectStudentMapper.selectList(wrapper).stream()
                .map(SubjectStudent::getStudentId)
                .toList();
    }

    @Override
    public List<UserDTO> getStudentsBySubjectId(Long subjectId) {
        List<Long> studentIds = subjectStudentMapper.selectStudentIdsBySubjectId(subjectId);
        if (studentIds == null || studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectBatchIds(studentIds).stream()
                .filter(Objects::nonNull)
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectDeleteStatsDTO getDeleteStats(Long id) {
        Subject subject = getSubjectById(id);

        int lessonCount = lessonMapper.countBySubjectId(id);
        int submissionCount = submissionMapper.countBySubjectId(id);
        int gradeCount = gradeMapper.countBySubjectId(id);
        int commentCount = codeCommentMapper.countBySubjectId(id);
        int studentCount = subjectStudentMapper.countBySubjectId(id);
        int groupCount = groupMapper.countBySubjectId(id);

        return SubjectDeleteStatsDTO.builder()
                .subjectId(id)
                .subjectName(subject.getName())
                .lessonCount(lessonCount)
                .submissionCount(submissionCount)
                .gradeCount(gradeCount)
                .commentCount(commentCount)
                .studentCount(studentCount)
                .groupCount(groupCount)
                .build();
    }
}
