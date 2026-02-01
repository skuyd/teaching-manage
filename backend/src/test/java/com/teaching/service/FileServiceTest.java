package com.teaching.service;

import com.teaching.dto.FileTreeNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceTest {

    @Autowired
    private FileService fileService;

    private final String testUploadDir = "./test-uploads";

    @BeforeEach
    void setUp() throws IOException {
        // 创建测试上传目录
        Path testPath = Paths.get(testUploadDir);
        if (!Files.exists(testPath)) {
            Files.createDirectories(testPath);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // 清理测试目录
        Path testPath = Paths.get(testUploadDir);
        if (Files.exists(testPath)) {
            fileService.deleteDirectory(testUploadDir);
        }
    }

    @Test
    @DisplayName("上传普通文件成功")
    void uploadFile_withNormalFile_shouldSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        String path = fileService.uploadFile(file, 1L, 100L, false);

        assertNotNull(path);
        assertTrue(Files.exists(Paths.get(path)));

        // 清理
        fileService.deleteDirectory(path);
    }

    @Test
    @DisplayName("上传ZIP文件并自动解压")
    void uploadFile_withZipFile_shouldUnzipAutomatically() throws IOException {
        // 创建测试ZIP文件
        File zipFile = createTestZipFile();
        MockMultipartFile mockZipFile = new MockMultipartFile(
                "file",
                "test.zip",
                "application/zip",
                Files.readAllBytes(zipFile.toPath())
        );

        String uploadPath = fileService.uploadFile(mockZipFile, 1L, 100L, false);

        // 验证文件已解压
        assertNotNull(uploadPath);
        assertTrue(Files.exists(Paths.get(uploadPath, "file1.txt")));
        assertTrue(Files.exists(Paths.get(uploadPath, "folder", "file2.txt")));

        // 验证ZIP文件已删除
        assertFalse(Files.exists(Paths.get(uploadPath, "test.zip")));

        // 清理
        fileService.deleteDirectory(uploadPath);
        zipFile.delete();
    }

    @Test
    @DisplayName("生成目录树成功")
    void generateDirectoryTree_shouldSuccess() throws IOException {
        // 创建测试目录结构
        Path rootPath = Paths.get(testUploadDir, "tree-test");
        Files.createDirectories(rootPath);
        Files.createDirectories(rootPath.resolve("folder1"));
        Files.createDirectories(rootPath.resolve("folder2"));
        Files.writeString(rootPath.resolve("file1.txt"), "content1");
        Files.writeString(rootPath.resolve("folder1/file2.txt"), "content2");

        FileTreeNode tree = fileService.generateDirectoryTree(rootPath.toString());

        assertNotNull(tree);
        assertTrue(tree.getIsDirectory());
        assertEquals(3, tree.getChildren().size());

        // 验证排序：文件夹在前
        assertEquals("folder1", tree.getChildren().get(0).getName());
        assertTrue(tree.getChildren().get(0).getIsDirectory());
        assertEquals("folder2", tree.getChildren().get(1).getName());
        assertTrue(tree.getChildren().get(1).getIsDirectory());
        assertEquals("file1.txt", tree.getChildren().get(2).getName());
        assertFalse(tree.getChildren().get(2).getIsDirectory());

        // 验证子文件夹
        FileTreeNode folder1 = tree.getChildren().get(0);
        assertEquals(1, folder1.getChildren().size());
        assertEquals("file2.txt", folder1.getChildren().get(0).getName());

        // 清理
        fileService.deleteDirectory(rootPath.toString());
    }

    @Test
    @DisplayName("读取文件内容成功")
    void readFileContent_shouldSuccess() throws IOException {
        Path testFile = Paths.get(testUploadDir, "content-test.txt");
        String content = "Test Content 测试内容";
        Files.writeString(testFile, content);

        String result = fileService.readFileContent(testFile.toString());

        assertEquals(content, result);

        // 清理
        Files.delete(testFile);
    }

    @Test
    @DisplayName("上传空文件应抛出异常")
    void uploadFile_withEmptyFile_shouldThrowException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadFile(emptyFile, 1L, 100L, false);
        });
    }

    @Test
    @DisplayName("读取不存在的文件应抛出异常")
    void readFileContent_withNonExistentFile_shouldThrowException() {
        assertThrows(Exception.class, () -> {
            fileService.readFileContent("non-existent-file.txt");
        });
    }

    /**
     * 创建测试用的ZIP文件
     */
    private File createTestZipFile() throws IOException {
        File zipFile = new File(testUploadDir, "test-temp.zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // 添加文件1
            ZipEntry entry1 = new ZipEntry("file1.txt");
            zos.putNextEntry(entry1);
            zos.write("Content 1".getBytes());
            zos.closeEntry();

            // 添加文件夹和文件2
            ZipEntry entry2 = new ZipEntry("folder/");
            zos.putNextEntry(entry2);
            zos.closeEntry();

            ZipEntry entry3 = new ZipEntry("folder/file2.txt");
            zos.putNextEntry(entry3);
            zos.write("Content 2".getBytes());
            zos.closeEntry();
        }

        return zipFile;
    }
}
