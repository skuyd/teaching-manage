package com.teaching.service;

import com.teaching.dto.FileTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文件服务
 * 处理文件上传、ZIP解压、目录树生成
 */
@Slf4j
@Service
public class FileService {

    @Value("${file.upload.base-dir:./uploads}")
    private String baseUploadDir;

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param lessonId 课程ID
     * @param targetId 目标ID（个人作业为学员ID，小组作业为小组ID）
     * @param isGroupSubmission 是否为小组提交
     * @return 文件存储路径
     */
    public String uploadFile(MultipartFile file, Long lessonId, Long targetId, boolean isGroupSubmission) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 构建存储路径: /uploads/课程ID/小组或学员ID/
        String relativePath = String.format("%d/%s", lessonId, targetId);
        Path uploadPath = Paths.get(baseUploadDir, relativePath);

        // 创建目录
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "upload_" + System.currentTimeMillis();
        }

        Path filePath = uploadPath.resolve(originalFilename);
        file.transferTo(filePath.toFile());

        log.info("文件上传成功: {}", filePath);

        // 如果是ZIP文件，自动解压
        if (originalFilename.toLowerCase().endsWith(".zip")) {
            unzipFile(filePath.toString(), uploadPath.toString());
            // 删除原始ZIP文件
            Files.deleteIfExists(filePath);
            log.info("ZIP文件解压完成: {}", uploadPath);
        }

        return uploadPath.toString();
    }

    /**
     * 解压ZIP文件
     *
     * @param zipFilePath ZIP文件路径
     * @param destDir 目标目录
     */
    public void unzipFile(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        byte[] buffer = new byte[8192];

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());

                // 防止ZIP Slip漏洞
                String canonicalDestPath = dir.getCanonicalPath();
                String canonicalNewFilePath = newFile.getCanonicalPath();
                if (!canonicalNewFilePath.startsWith(canonicalDestPath + File.separator)) {
                    throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
                }

                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // 确保父目录存在
                    File parent = newFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    // 解压文件
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        }
    }

    /**
     * 生成目录树
     *
     * @param rootPath 根目录路径
     * @return 目录树根节点
     */
    public FileTreeNode generateDirectoryTree(String rootPath) throws IOException {
        Path path = Paths.get(rootPath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("路径不存在: " + rootPath);
        }

        return buildTree(path, path);
    }

    /**
     * 递归构建目录树
     */
    private FileTreeNode buildTree(Path rootPath, Path currentPath) throws IOException {
        File file = currentPath.toFile();
        String relativePath = rootPath.relativize(currentPath).toString();
        if (relativePath.isEmpty()) {
            relativePath = file.getName();
        }

        FileTreeNode node = new FileTreeNode(
                file.getName(),
                relativePath,
                file.isDirectory()
        );

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                List<FileTreeNode> childNodes = new ArrayList<>();
                for (File child : children) {
                    FileTreeNode childNode = buildTree(rootPath, child.toPath());
                    childNodes.add(childNode);
                }
                // 排序：文件夹在前，文件在后，同类按名称排序
                childNodes.sort((a, b) -> {
                    if (a.getIsDirectory() != b.getIsDirectory()) {
                        return a.getIsDirectory() ? -1 : 1;
                    }
                    return a.getName().compareTo(b.getName());
                });
                node.setChildren(childNodes);
            }
        } else {
            node.setSize(file.length());
        }

        return node;
    }

    /**
     * 删除目录及其内容
     *
     * @param path 目录路径
     */
    public void deleteDirectory(String path) throws IOException {
        Path directory = Paths.get(path);
        if (Files.exists(directory)) {
            Files.walk(directory)
                    .sorted((a, b) -> b.compareTo(a))  // 先删除子文件/文件夹
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.error("删除文件失败: {}", p, e);
                        }
                    });
        }
    }

    /**
     * 读取文件内容
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    public String readFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path) || Files.isDirectory(path)) {
            throw new FileNotFoundException("文件不存在或是目录: " + filePath);
        }
        return Files.readString(path);
    }
}
