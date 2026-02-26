package com.teaching.service;

import com.teaching.dto.FileTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.junrar.Junrar;
import com.github.junrar.exception.RarException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 文件服务
 * 处理文件上传、ZIP解压、目录树生成
 */
@Slf4j
@Service
public class FileService {

    @Value("${file.upload-dir:./uploads}")
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
        log.info("开始上传文件: lessonId={}, targetId={}, isGroup={}", lessonId, targetId, isGroupSubmission);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        log.info("文件信息: name={}, size={}, contentType={}",
            file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 构建存储路径: /uploads/课程ID/小组或学员ID/
        String relativePath = String.format("%d/%s", lessonId, targetId);
        Path uploadPath = Paths.get(baseUploadDir, relativePath).toAbsolutePath();
        log.info("上传目录: {}", uploadPath);

        // 创建目录
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("创建目录成功: {}", uploadPath);
        }

        // 保存文件
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "upload_" + System.currentTimeMillis();
        }

        Path filePath = uploadPath.resolve(originalFilename);
        log.info("保存文件到: {}", filePath);

        // 使用 Files.copy 替代 transferTo，避免路径问题
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, filePath, REPLACE_EXISTING);
        }

        log.info("文件上传成功: {}", filePath);

        // 如果是压缩文件，自动解压
        String lowerFilename = originalFilename.toLowerCase();
        if (lowerFilename.endsWith(".zip")) {
            unzipFile(filePath.toString(), uploadPath.toString());
            Files.deleteIfExists(filePath);
            log.info("ZIP文件解压完成: {}", uploadPath);
        } else if (lowerFilename.endsWith(".rar")) {
            unrarFile(filePath.toString(), uploadPath.toString());
            Files.deleteIfExists(filePath);
            log.info("RAR文件解压完成: {}", uploadPath);
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
     * 解压RAR文件
     *
     * @param rarFilePath RAR文件路径
     * @param destDir 目标目录
     */
    public void unrarFile(String rarFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            Junrar.extract(new File(rarFilePath), dir);
        } catch (RarException e) {
            throw new IOException("解压RAR文件失败: " + e.getMessage(), e);
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

    /**
     * 上传用户头像
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 头像访问URL
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只允许上传图片文件");
        }

        // 验证文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过2MB");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成唯一文件名
        String filename = "avatar_" + userId + "_" + System.currentTimeMillis() + extension;

        // 创建头像目录
        Path avatarDir = Paths.get(baseUploadDir, "avatars");
        if (!Files.exists(avatarDir)) {
            Files.createDirectories(avatarDir);
        }

        // 保存文件
        Path filePath = avatarDir.resolve(filename);
        file.transferTo(filePath.toFile());

        log.info("头像上传成功: userId={}, path={}", userId, filePath);

        // 返回访问URL
        return "/uploads/avatars/" + filename;
    }

    /**
     * 上传图片（用于Markdown编辑器）
     *
     * @param file 图片文件
     * @return 图片信息（包含URL和原始文件名）
     */
    public ImageUploadResult uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只允许上传图片文件");
        }

        // 验证文件大小（最大10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 按月份组织图片目录
        String monthFolder = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

        // 生成唯一文件名
        String filename = "img_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + extension;

        // 创建图片目录
        Path imageDir = Paths.get(baseUploadDir, "images", monthFolder);
        if (!Files.exists(imageDir)) {
            Files.createDirectories(imageDir);
        }

        // 保存文件
        Path filePath = imageDir.resolve(filename);
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, filePath, REPLACE_EXISTING);
        }

        log.info("图片上传成功: originalName={}, path={}", originalFilename, filePath);

        // 返回图片信息
        String url = "/uploads/images/" + monthFolder + "/" + filename;
        return new ImageUploadResult(url, originalFilename, file.getSize());
    }

    /**
     * 图片上传结果
     */
    public record ImageUploadResult(String url, String originalName, long size) {}
}
