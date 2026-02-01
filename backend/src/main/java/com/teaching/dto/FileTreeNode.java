package com.teaching.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件树节点DTO
 */
@Data
public class FileTreeNode {

    /**
     * 文件/文件夹名称
     */
    private String name;

    /**
     * 完整路径
     */
    private String path;

    /**
     * 是否为文件夹
     */
    private Boolean isDirectory;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 子节点
     */
    private List<FileTreeNode> children = new ArrayList<>();

    public FileTreeNode() {
    }

    public FileTreeNode(String name, String path, Boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
    }
}
