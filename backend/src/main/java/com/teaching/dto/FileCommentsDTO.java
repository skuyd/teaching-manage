package com.teaching.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 文件评论DTO（按文件和行号分组）
 */
@Data
public class FileCommentsDTO {

    /**
     * 文件路径（相对路径）
     */
    private String filePath;

    /**
     * 该文件的评论，按行号分组
     * Key: 行号
     * Value: 该行的评论列表
     */
    private Map<Integer, List<CodeCommentDTO>> commentsByLine;

    /**
     * 该文件的总评论数
     */
    private Integer totalComments;
}
