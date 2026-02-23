package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.FileTreeNode;
import com.teaching.dto.SubmissionDTO;
import com.teaching.dto.SubmissionDetailDTO;
import com.teaching.entity.Submission;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SubmissionService extends IService<Submission> {

    /**
     * 提交作业
     */
    SubmissionDTO submitAssignment(Long lessonId, MultipartFile file, Long userId) throws IOException;

    /**
     * 重新提交作业
     */
    SubmissionDTO resubmitAssignment(Long lessonId, MultipartFile file, Long userId) throws IOException;

    /**
     * 根据课程ID获取所有提交
     */
    List<SubmissionDTO> getSubmissionsByLessonId(Long lessonId);

    /**
     * 根据ID获取提交详情
     */
    SubmissionDetailDTO getSubmissionDetail(Long id, Long userId);

    /**
     * 获取我的提交（学员视角）
     */
    SubmissionDTO getMySubmission(Long lessonId, Long userId);

    /**
     * 删除提交
     */
    void deleteSubmission(Long id, Long userId);

    /**
     * 获取文件树
     */
    FileTreeNode getFileTree(Long submissionId) throws IOException;

    /**
     * 读取文件内容
     */
    String readFileContent(Long submissionId, String relativePath) throws IOException;
}
