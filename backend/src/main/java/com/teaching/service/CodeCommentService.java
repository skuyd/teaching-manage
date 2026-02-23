package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.CodeCommentDTO;
import com.teaching.dto.CreateCodeCommentRequest;
import com.teaching.dto.FileCommentsDTO;
import com.teaching.entity.CodeComment;

import java.util.List;

public interface CodeCommentService extends IService<CodeComment> {

    /**
     * 添加代码评论
     */
    CodeCommentDTO addComment(CreateCodeCommentRequest request, Long commenterId);

    /**
     * 更新评论内容
     */
    CodeCommentDTO updateComment(Long id, String content, Long userId);

    /**
     * 删除评论
     */
    void deleteComment(Long id, Long userId);

    /**
     * 获取提交的所有评论
     */
    List<CodeCommentDTO> getCommentsBySubmission(Long submissionId);

    /**
     * 获取文件的所有评论
     */
    List<CodeCommentDTO> getCommentsByFile(Long submissionId, String filePath);

    /**
     * 获取特定行的评论
     */
    List<CodeCommentDTO> getCommentsByLine(Long submissionId, String filePath, Integer lineNumber);

    /**
     * 获取提交的评论，按文件和行号分组
     */
    List<FileCommentsDTO> getCommentsGroupedByFile(Long submissionId);
}
