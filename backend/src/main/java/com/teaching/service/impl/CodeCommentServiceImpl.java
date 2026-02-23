package com.teaching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.dto.CodeCommentDTO;
import com.teaching.dto.CreateCodeCommentRequest;
import com.teaching.dto.FileCommentsDTO;
import com.teaching.entity.CodeComment;
import com.teaching.entity.Submission;
import com.teaching.entity.User;
import com.teaching.enums.UserRole;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.CodeCommentMapper;
import com.teaching.mapper.SubmissionMapper;
import com.teaching.mapper.UserMapper;
import com.teaching.service.CodeCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeCommentServiceImpl extends ServiceImpl<CodeCommentMapper, CodeComment> implements CodeCommentService {

    private final CodeCommentMapper codeCommentMapper;
    private final SubmissionMapper submissionMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CodeCommentDTO addComment(CreateCodeCommentRequest request, Long commenterId) {
        // 验证提交是否存在
        Submission submission = submissionMapper.selectById(request.getSubmissionId());
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }

        // 创建评论
        CodeComment comment = new CodeComment();
        comment.setSubmissionId(request.getSubmissionId());
        comment.setFilePath(request.getFilePath());
        comment.setLineNumber(request.getLineNumber());
        comment.setContent(request.getContent());
        comment.setCommenterId(commenterId);

        codeCommentMapper.insert(comment);
        log.info("代码评论已添加: id={}, submissionId={}, commenterId={}",
                comment.getId(), request.getSubmissionId(), commenterId);

        return convertToDTO(comment);
    }

    @Override
    @Transactional
    public CodeCommentDTO updateComment(Long id, String content, Long userId) {
        CodeComment comment = codeCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }

        // 只有评论者本人可以编辑
        if (!comment.getCommenterId().equals(userId)) {
            throw new BusinessException(403, "无权编辑该评论");
        }

        comment.setContent(content);
        codeCommentMapper.updateById(comment);
        log.info("代码评论已更新: id={}, userId={}", id, userId);

        return convertToDTO(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id, Long userId) {
        CodeComment comment = codeCommentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }

        // 验证权限：只有评论者本人或管理员/教师可以删除
        if (!comment.getCommenterId().equals(userId)) {
            User user = userMapper.selectById(userId);
            if (user == null || (user.getRole() != UserRole.TEACHER && user.getRole() != UserRole.ADMIN)) {
                throw new BusinessException(403, "无权删除该评论");
            }
        }

        codeCommentMapper.deleteById(id);
        log.info("代码评论已删除: id={}, userId={}", id, userId);
    }

    @Override
    public List<CodeCommentDTO> getCommentsBySubmission(Long submissionId) {
        List<CodeComment> comments = codeCommentMapper.findBySubmissionId(submissionId);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeCommentDTO> getCommentsByFile(Long submissionId, String filePath) {
        List<CodeComment> comments = codeCommentMapper.findBySubmissionIdAndFilePath(submissionId, filePath);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeCommentDTO> getCommentsByLine(Long submissionId, String filePath, Integer lineNumber) {
        List<CodeComment> comments = codeCommentMapper.findBySubmissionIdAndFilePathAndLineNumber(
                submissionId, filePath, lineNumber);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FileCommentsDTO> getCommentsGroupedByFile(Long submissionId) {
        List<CodeComment> allComments = codeCommentMapper.findBySubmissionId(submissionId);

        // 按文件路径分组
        Map<String, List<CodeComment>> commentsByFile = allComments.stream()
                .collect(Collectors.groupingBy(CodeComment::getFilePath));

        List<FileCommentsDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<CodeComment>> fileEntry : commentsByFile.entrySet()) {
            String filePath = fileEntry.getKey();
            List<CodeComment> fileComments = fileEntry.getValue();

            // 按行号分组
            Map<Integer, List<CodeCommentDTO>> commentsByLine = fileComments.stream()
                    .collect(Collectors.groupingBy(
                            CodeComment::getLineNumber,
                            Collectors.mapping(this::convertToDTO, Collectors.toList())
                    ));

            FileCommentsDTO fileCommentsDTO = new FileCommentsDTO();
            fileCommentsDTO.setFilePath(filePath);
            fileCommentsDTO.setCommentsByLine(commentsByLine);
            fileCommentsDTO.setTotalComments(fileComments.size());

            result.add(fileCommentsDTO);
        }

        // 按文件路径排序
        result.sort(Comparator.comparing(FileCommentsDTO::getFilePath));

        return result;
    }

    private CodeCommentDTO convertToDTO(CodeComment comment) {
        CodeCommentDTO dto = new CodeCommentDTO();
        dto.setId(comment.getId());
        dto.setSubmissionId(comment.getSubmissionId());
        dto.setFilePath(comment.getFilePath());
        dto.setLineNumber(comment.getLineNumber());
        dto.setContent(comment.getContent());
        dto.setCommenterId(comment.getCommenterId());
        dto.setCreateTime(comment.getCreateTime());
        dto.setUpdateTime(comment.getUpdateTime());

        // 加载评论者信息
        User commenter = userMapper.selectById(comment.getCommenterId());
        if (commenter != null) {
            dto.setCommenterName(commenter.getName());
        }

        return dto;
    }
}
