package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.CreateSubjectRequest;
import com.teaching.dto.UpdateSubjectRequest;
import com.teaching.entity.Subject;
import com.teaching.entity.SubjectStudent;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.SubjectMapper;
import com.teaching.mapper.SubjectStudentMapper;
import com.teaching.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    private final SubjectMapper subjectMapper;
    private final SubjectStudentMapper subjectStudentMapper;

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
        subjectMapper.deleteById(id);
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
}
