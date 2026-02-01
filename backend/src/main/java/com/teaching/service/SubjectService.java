package com.teaching.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.CreateSubjectRequest;
import com.teaching.dto.UpdateSubjectRequest;
import com.teaching.entity.Subject;

import java.util.List;

public interface SubjectService extends IService<Subject> {

    void createSubject(CreateSubjectRequest request);

    IPage<Subject> listSubjects(int page, int size, String keyword);

    Subject getSubjectById(Long id);

    void updateSubject(Long id, UpdateSubjectRequest request);

    void deleteSubject(Long id);

    void addStudentToSubject(Long subjectId, Long studentId);

    void removeStudentFromSubject(Long subjectId, Long studentId);

    List<Subject> getSubjectsByStudentId(Long studentId);

    List<Long> getStudentIdsBySubjectId(Long subjectId);
}
