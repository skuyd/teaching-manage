# 阶段二：作业提交功能

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现作业提交功能，支持单文件和文件夹（ZIP解压）上传，按目录结构存储和展示。

**Architecture:** RESTful API，文件存储到本地文件系统，支持个人作业和小组作业。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3, Element Plus

**依赖:** 需要先完成 `03-group-management.md`

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Assignment Management Page | AlHE4 | 作业列表，提交状态，文件预览 |

### 设计规范

- **作业列表**: 按课程分组显示，状态标签区分
- **上传区域**: 拖拽上传，支持文件夹
- **文件预览**: 目录树结构，文件图标区分
- **提交状态**: 未提交/已提交/已评分 颜色区分
- **截止时间**: 倒计时显示，过期警告

---

## Task 1: 创建作业提交实体

**Files:**
- Create: `backend/src/main/java/com/teaching/entity/Submission.java`
- Create: `backend/src/main/java/com/teaching/mapper/SubmissionMapper.java`
- Test: `backend/src/test/java/com/teaching/entity/SubmissionTest.java`

**Step 1: 编写测试（红灯）**

```java
// backend/src/test/java/com/teaching/entity/SubmissionTest.java
package com.teaching.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionTest {

    @Test
    @DisplayName("创建作业提交实体")
    void createSubmission_shouldHaveAllFields() {
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setLessonId(1L);
        submission.setSubmitterId(10L);
        submission.setGroupId(null);
        submission.setFilePath("/uploads/1/1/10/");
        submission.setSubmitTime(LocalDateTime.now());

        assertEquals(1L, submission.getId());
        assertEquals(1L, submission.getLessonId());
        assertEquals(10L, submission.getSubmitterId());
        assertNull(submission.getGroupId());
        assertEquals("/uploads/1/1/10/", submission.getFilePath());
        assertNotNull(submission.getSubmitTime());
    }

    @Test
    @DisplayName("小组作业应有groupId")
    void groupSubmission_shouldHaveGroupId() {
        Submission submission = new Submission();
        submission.setSubmitterId(10L);
        submission.setGroupId(5L);

        assertEquals(5L, submission.getGroupId());
    }

    @Test
    @DisplayName("提交路径格式正确")
    void filePath_shouldFollowPattern() {
        // 路径格式: /uploads/{subjectId}/{lessonId}/{userId或groupId}/
        String personalPath = "/uploads/1/2/10/";
        String groupPath = "/uploads/1/2/group_5/";

        assertTrue(personalPath.startsWith("/uploads/"));
        assertTrue(groupPath.contains("group_"));
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=SubmissionTest -q
```

**Step 3: 创建 Submission 实体**

```java
// backend/src/main/java/com/teaching/entity/Submission.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_submission")
public class Submission extends BaseEntity {

    private Long lessonId;

    private Long submitterId;  // 提交者用户ID

    private Long groupId;  // 小组作业时的小组ID，个人作业为null

    private String filePath;  // 文件存储路径

    private LocalDateTime submitTime;  // 提交时间
}
```

**Step 4: 创建 SubmissionMapper**

```java
// backend/src/main/java/com/teaching/mapper/SubmissionMapper.java
package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

    @Select("SELECT * FROM t_submission WHERE lesson_id = #{lessonId} AND del_flag = 0 ORDER BY submit_time DESC")
    List<Submission> selectByLessonId(Long lessonId);

    @Select("SELECT * FROM t_submission WHERE lesson_id = #{lessonId} AND submitter_id = #{submitterId} AND del_flag = 0")
    Submission selectByLessonIdAndSubmitterId(Long lessonId, Long submitterId);

    @Select("SELECT * FROM t_submission WHERE lesson_id = #{lessonId} AND group_id = #{groupId} AND del_flag = 0")
    Submission selectByLessonIdAndGroupId(Long lessonId, Long groupId);

    @Select("SELECT * FROM t_submission WHERE submitter_id = #{submitterId} AND del_flag = 0 ORDER BY submit_time DESC")
    List<Submission> selectBySubmitterId(Long submitterId);
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=SubmissionTest -q
```

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add Submission entity and mapper"
```

---

## Task 2: 创建文件服务

**Files:**
- Create: `backend/src/main/java/com/teaching/service/FileService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/FileServiceImpl.java`
- Create: `backend/src/main/java/com/teaching/dto/FileNode.java`
- Test: `backend/src/test/java/com/teaching/service/FileServiceTest.java`

**Step 1: 编写 FileService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/FileServiceTest.java
package com.teaching.service;

import com.teaching.dto.FileNode;
import com.teaching.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    @TempDir
    Path tempDir;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(tempDir.toString());
    }

    @Test
    @DisplayName("保存单个文件")
    void saveFile_shouldStoreFileCorrectly() throws IOException {
        MultipartFile file = new MockMultipartFile(
            "test.java",
            "test.java",
            "text/plain",
            "public class Test {}".getBytes()
        );

        String path = fileService.saveFile(file, "1/2/10");

        assertTrue(path.contains("1/2/10"));
        assertTrue(Files.exists(Path.of(tempDir.toString(), path)));
    }

    @Test
    @DisplayName("保存并解压ZIP文件")
    void saveZipFile_shouldExtractContents() throws IOException {
        // 创建测试ZIP文件
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 添加文件
            ZipEntry entry1 = new ZipEntry("src/Main.java");
            zos.putNextEntry(entry1);
            zos.write("public class Main {}".getBytes());
            zos.closeEntry();

            ZipEntry entry2 = new ZipEntry("src/Utils.java");
            zos.putNextEntry(entry2);
            zos.write("public class Utils {}".getBytes());
            zos.closeEntry();
        }

        MultipartFile file = new MockMultipartFile(
            "project.zip",
            "project.zip",
            "application/zip",
            baos.toByteArray()
        );

        String path = fileService.saveZipFile(file, "1/2/10");

        assertTrue(Files.exists(Path.of(tempDir.toString(), path, "src/Main.java")));
        assertTrue(Files.exists(Path.of(tempDir.toString(), path, "src/Utils.java")));
    }

    @Test
    @DisplayName("列出目录结构")
    void listFiles_shouldReturnDirectoryTree() throws IOException {
        // 创建测试目录结构
        Path testDir = tempDir.resolve("1/2/10");
        Files.createDirectories(testDir.resolve("src"));
        Files.writeString(testDir.resolve("src/Main.java"), "public class Main {}");
        Files.writeString(testDir.resolve("README.md"), "# Project");

        List<FileNode> tree = fileService.listFiles("1/2/10");

        assertEquals(2, tree.size());
        assertTrue(tree.stream().anyMatch(n -> n.getName().equals("src")));
        assertTrue(tree.stream().anyMatch(n -> n.getName().equals("README.md")));
    }

    @Test
    @DisplayName("读取文件内容")
    void readFile_shouldReturnContent() throws IOException {
        Path testDir = tempDir.resolve("1/2/10");
        Files.createDirectories(testDir);
        Files.writeString(testDir.resolve("test.java"), "public class Test {}");

        String content = fileService.readFile("1/2/10/test.java");

        assertEquals("public class Test {}", content);
    }

    @Test
    @DisplayName("删除提交文件夹")
    void deleteSubmission_shouldRemoveDirectory() throws IOException {
        Path testDir = tempDir.resolve("1/2/10");
        Files.createDirectories(testDir);
        Files.writeString(testDir.resolve("test.java"), "content");

        fileService.deleteSubmission("1/2/10");

        assertFalse(Files.exists(testDir));
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=FileServiceTest -q
```

**Step 3: 创建 FileNode DTO**

```java
// backend/src/main/java/com/teaching/dto/FileNode.java
package com.teaching.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileNode {

    private String name;
    private String path;
    private String type;  // "file" or "directory"
    private Long size;
    private List<FileNode> children;

    public static FileNode file(String name, String path, Long size) {
        FileNode node = new FileNode();
        node.setName(name);
        node.setPath(path);
        node.setType("file");
        node.setSize(size);
        return node;
    }

    public static FileNode directory(String name, String path, List<FileNode> children) {
        FileNode node = new FileNode();
        node.setName(name);
        node.setPath(path);
        node.setType("directory");
        node.setChildren(children);
        return node;
    }
}
```

**Step 4: 创建 FileService 接口**

```java
// backend/src/main/java/com/teaching/service/FileService.java
package com.teaching.service;

import com.teaching.dto.FileNode;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    /**
     * 保存单个文件
     * @param file 上传的文件
     * @param relativePath 相对路径（如 "1/2/10"）
     * @return 完整的相对路径
     */
    String saveFile(MultipartFile file, String relativePath) throws IOException;

    /**
     * 保存并解压ZIP文件
     * @param file ZIP文件
     * @param relativePath 相对路径
     * @return 解压后的目录路径
     */
    String saveZipFile(MultipartFile file, String relativePath) throws IOException;

    /**
     * 列出目录下的文件结构
     * @param relativePath 相对路径
     * @return 文件树
     */
    List<FileNode> listFiles(String relativePath);

    /**
     * 读取文件内容
     * @param relativePath 文件相对路径
     * @return 文件内容
     */
    String readFile(String relativePath) throws IOException;

    /**
     * 删除提交目录
     * @param relativePath 相对路径
     */
    void deleteSubmission(String relativePath) throws IOException;
}
```

**Step 5: 创建 FileServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/FileServiceImpl.java
package com.teaching.service.impl;

import com.teaching.dto.FileNode;
import com.teaching.exception.BusinessException;
import com.teaching.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileServiceImpl implements FileService {

    private final String uploadPath;

    public FileServiceImpl(@Value("${app.upload-path:./uploads}") String uploadPath) {
        this.uploadPath = uploadPath;
    }

    @Override
    public String saveFile(MultipartFile file, String relativePath) throws IOException {
        Path targetDir = Path.of(uploadPath, relativePath);
        Files.createDirectories(targetDir);

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            filename = "uploaded_file";
        }
        // 安全处理文件名
        filename = sanitizeFilename(filename);

        Path targetFile = targetDir.resolve(filename);
        file.transferTo(targetFile);

        return relativePath + "/" + filename;
    }

    @Override
    public String saveZipFile(MultipartFile file, String relativePath) throws IOException {
        Path targetDir = Path.of(uploadPath, relativePath);

        // 先清空目标目录（如果存在）
        if (Files.exists(targetDir)) {
            deleteDirectory(targetDir);
        }
        Files.createDirectories(targetDir);

        // 解压ZIP
        try (InputStream is = file.getInputStream();
             ZipInputStream zis = new ZipInputStream(is)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                // 安全检查：防止路径遍历攻击
                if (entryName.contains("..")) {
                    throw new BusinessException(400, "不安全的ZIP文件内容");
                }

                Path entryPath = targetDir.resolve(entryName);

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }

        return relativePath;
    }

    @Override
    public List<FileNode> listFiles(String relativePath) {
        Path targetDir = Path.of(uploadPath, relativePath);
        if (!Files.exists(targetDir)) {
            return new ArrayList<>();
        }
        return listDirectory(targetDir, relativePath);
    }

    private List<FileNode> listDirectory(Path dir, String basePath) {
        List<FileNode> nodes = new ArrayList<>();

        try (var stream = Files.list(dir)) {
            stream.sorted(Comparator.comparing(p -> !Files.isDirectory(p)))
                  .forEach(path -> {
                      String name = path.getFileName().toString();
                      String fullPath = basePath + "/" + name;

                      if (Files.isDirectory(path)) {
                          List<FileNode> children = listDirectory(path, fullPath);
                          nodes.add(FileNode.directory(name, fullPath, children));
                      } else {
                          try {
                              long size = Files.size(path);
                              nodes.add(FileNode.file(name, fullPath, size));
                          } catch (IOException e) {
                              nodes.add(FileNode.file(name, fullPath, 0L));
                          }
                      }
                  });
        } catch (IOException e) {
            // 返回空列表
        }

        return nodes;
    }

    @Override
    public String readFile(String relativePath) throws IOException {
        Path filePath = Path.of(uploadPath, relativePath);

        if (!Files.exists(filePath)) {
            throw new BusinessException(404, "文件不存在");
        }

        if (Files.isDirectory(filePath)) {
            throw new BusinessException(400, "不能读取目录");
        }

        // 检查文件大小，限制最大读取10MB
        long size = Files.size(filePath);
        if (size > 10 * 1024 * 1024) {
            throw new BusinessException(400, "文件过大，无法预览");
        }

        return Files.readString(filePath);
    }

    @Override
    public void deleteSubmission(String relativePath) throws IOException {
        Path targetDir = Path.of(uploadPath, relativePath);
        if (Files.exists(targetDir)) {
            deleteDirectory(targetDir);
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private String sanitizeFilename(String filename) {
        // 移除路径分隔符和特殊字符
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
```

**Step 6: 配置上传路径（application.yml）**

```yaml
# 在 backend/src/main/resources/application.yml 中添加
app:
  upload-path: ./uploads
```

**Step 7: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=FileServiceTest -q
```

**Step 8: Commit**

```bash
git add backend/src/
git commit -m "feat: add FileService for file upload and management"
```

---

## Task 3: 创建作业提交 DTO

**Files:**
- Create: `backend/src/main/java/com/teaching/dto/SubmissionDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/SubmissionListDTO.java`

**Step 1: 创建 SubmissionDTO**

```java
// backend/src/main/java/com/teaching/dto/SubmissionDTO.java
package com.teaching.dto;

import com.teaching.entity.Submission;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubmissionDTO {

    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private Long submitterId;
    private String submitterName;
    private Long groupId;
    private String groupName;
    private String filePath;
    private List<FileNode> files;
    private LocalDateTime submitTime;
    private Boolean isLate;

    // 评分信息（如果已评分）
    private String grade;
    private String gradeComment;
    private LocalDateTime gradeTime;

    public static SubmissionDTO fromEntity(Submission submission) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(submission.getId());
        dto.setLessonId(submission.getLessonId());
        dto.setSubmitterId(submission.getSubmitterId());
        dto.setGroupId(submission.getGroupId());
        dto.setFilePath(submission.getFilePath());
        dto.setSubmitTime(submission.getSubmitTime());
        return dto;
    }
}
```

**Step 2: 创建 SubmissionListDTO（列表展示用）**

```java
// backend/src/main/java/com/teaching/dto/SubmissionListDTO.java
package com.teaching.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionListDTO {

    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private Long submitterId;
    private String submitterName;
    private Long groupId;
    private String groupName;
    private LocalDateTime submitTime;
    private Boolean isLate;
    private String grade;  // 评分结果 A/B/C/D 或 null
    private Boolean hasGrade;
}
```

**Step 3: Commit**

```bash
git add backend/src/
git commit -m "feat: add Submission DTOs"
```

---

## Task 4: 创建作业提交 Service

**Files:**
- Create: `backend/src/main/java/com/teaching/service/SubmissionService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/SubmissionServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/service/SubmissionServiceTest.java`

**Step 1: 编写 SubmissionService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/SubmissionServiceTest.java
package com.teaching.service;

import com.teaching.dto.SubmissionDTO;
import com.teaching.entity.*;
import com.teaching.enums.SubmitType;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.*;
import com.teaching.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionMapper submissionMapper;
    @Mock
    private LessonMapper lessonMapper;
    @Mock
    private GroupMapper groupMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private FileService fileService;

    private SubmissionServiceImpl submissionService;

    @BeforeEach
    void setUp() {
        submissionService = new SubmissionServiceImpl(
            submissionMapper, lessonMapper, groupMapper, userMapper, fileService
        );
    }

    @Test
    @DisplayName("个人作业提交")
    void submitPersonalHomework_shouldCreateSubmission() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubjectId(1L);
        lesson.setSubmitType(SubmitType.PERSONAL);
        lesson.setDeadline(LocalDateTime.now().plusDays(1));

        when(lessonMapper.selectById(1L)).thenReturn(lesson);
        when(submissionMapper.selectByLessonIdAndSubmitterId(1L, 10L)).thenReturn(null);
        when(fileService.saveZipFile(any(), anyString())).thenReturn("1/1/10");
        when(submissionMapper.insert(any(Submission.class))).thenReturn(1);

        MockMultipartFile file = new MockMultipartFile(
            "file", "project.zip", "application/zip", "content".getBytes()
        );

        submissionService.submit(1L, 10L, file);

        verify(submissionMapper).insert(argThat(s ->
            s.getLessonId() == 1L && s.getSubmitterId() == 10L && s.getGroupId() == null
        ));
    }

    @Test
    @DisplayName("小组作业提交")
    void submitGroupHomework_shouldCreateSubmissionWithGroupId() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubjectId(1L);
        lesson.setSubmitType(SubmitType.GROUP);
        lesson.setDeadline(LocalDateTime.now().plusDays(1));

        Group group = new Group();
        group.setId(5L);

        when(lessonMapper.selectById(1L)).thenReturn(lesson);
        when(groupMapper.selectByUserIdAndSubjectId(10L, 1L)).thenReturn(group);
        when(submissionMapper.selectByLessonIdAndGroupId(1L, 5L)).thenReturn(null);
        when(fileService.saveZipFile(any(), anyString())).thenReturn("1/1/group_5");
        when(submissionMapper.insert(any(Submission.class))).thenReturn(1);

        MockMultipartFile file = new MockMultipartFile(
            "file", "project.zip", "application/zip", "content".getBytes()
        );

        submissionService.submit(1L, 10L, file);

        verify(submissionMapper).insert(argThat(s ->
            s.getGroupId() == 5L
        ));
    }

    @Test
    @DisplayName("未加入小组不能提交小组作业")
    void submitGroupHomework_shouldFailIfNotInGroup() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubjectId(1L);
        lesson.setSubmitType(SubmitType.GROUP);

        when(lessonMapper.selectById(1L)).thenReturn(lesson);
        when(groupMapper.selectByUserIdAndSubjectId(10L, 1L)).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile(
            "file", "project.zip", "application/zip", "content".getBytes()
        );

        assertThrows(BusinessException.class, () ->
            submissionService.submit(1L, 10L, file)
        );
    }

    @Test
    @DisplayName("超过截止时间且不允许补交时拒绝提交")
    void submit_shouldRejectAfterDeadlineIfNotAllowed() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubmitType(SubmitType.PERSONAL);
        lesson.setDeadline(LocalDateTime.now().minusDays(1));
        lesson.setAllowLate(false);

        when(lessonMapper.selectById(1L)).thenReturn(lesson);

        MockMultipartFile file = new MockMultipartFile(
            "file", "project.zip", "application/zip", "content".getBytes()
        );

        assertThrows(BusinessException.class, () ->
            submissionService.submit(1L, 10L, file)
        );
    }

    @Test
    @DisplayName("允许补交时超过截止时间仍可提交")
    void submit_shouldAllowLateSubmissionIfEnabled() throws Exception {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubjectId(1L);
        lesson.setSubmitType(SubmitType.PERSONAL);
        lesson.setDeadline(LocalDateTime.now().minusDays(1));
        lesson.setAllowLate(true);

        when(lessonMapper.selectById(1L)).thenReturn(lesson);
        when(submissionMapper.selectByLessonIdAndSubmitterId(1L, 10L)).thenReturn(null);
        when(fileService.saveZipFile(any(), anyString())).thenReturn("1/1/10");
        when(submissionMapper.insert(any(Submission.class))).thenReturn(1);

        MockMultipartFile file = new MockMultipartFile(
            "file", "project.zip", "application/zip", "content".getBytes()
        );

        submissionService.submit(1L, 10L, file);

        verify(submissionMapper).insert(any(Submission.class));
    }

    @Test
    @DisplayName("获取课程的所有提交（教员用）")
    void getSubmissionsByLesson_shouldReturnAllSubmissions() {
        Submission s1 = new Submission();
        s1.setId(1L);
        s1.setLessonId(1L);
        s1.setSubmitterId(10L);

        Submission s2 = new Submission();
        s2.setId(2L);
        s2.setLessonId(1L);
        s2.setSubmitterId(20L);

        when(submissionMapper.selectByLessonId(1L)).thenReturn(List.of(s1, s2));

        List<SubmissionDTO> result = submissionService.getSubmissionsByLesson(1L);

        assertEquals(2, result.size());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=SubmissionServiceTest -q
```

**Step 3: 创建 SubmitType 枚举（如果尚未创建）**

```java
// backend/src/main/java/com/teaching/enums/SubmitType.java
package com.teaching.enums;

public enum SubmitType {
    PERSONAL,  // 个人作业
    GROUP      // 小组作业
}
```

**Step 4: 创建 SubmissionService 接口**

```java
// backend/src/main/java/com/teaching/service/SubmissionService.java
package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.FileNode;
import com.teaching.dto.SubmissionDTO;
import com.teaching.dto.SubmissionListDTO;
import com.teaching.entity.Submission;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SubmissionService extends IService<Submission> {

    /**
     * 提交作业
     */
    SubmissionDTO submit(Long lessonId, Long userId, MultipartFile file) throws IOException;

    /**
     * 重新提交作业（覆盖原有提交）
     */
    SubmissionDTO resubmit(Long submissionId, Long userId, MultipartFile file) throws IOException;

    /**
     * 获取课程的所有提交（教员用）
     */
    List<SubmissionDTO> getSubmissionsByLesson(Long lessonId);

    /**
     * 获取提交详情
     */
    SubmissionDTO getSubmission(Long id, Long userId);

    /**
     * 获取我的提交
     */
    SubmissionDTO getMySubmission(Long lessonId, Long userId);

    /**
     * 获取我的所有提交
     */
    List<SubmissionListDTO> getMySubmissions(Long userId);

    /**
     * 获取提交的文件列表
     */
    List<FileNode> getSubmissionFiles(Long submissionId);

    /**
     * 读取提交中的文件内容
     */
    String readSubmissionFile(Long submissionId, String filePath, Long userId);

    /**
     * 删除提交
     */
    void deleteSubmission(Long id, Long userId) throws IOException;
}
```

**Step 5: 创建 SubmissionServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/SubmissionServiceImpl.java
package com.teaching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.FileNode;
import com.teaching.dto.SubmissionDTO;
import com.teaching.dto.SubmissionListDTO;
import com.teaching.entity.*;
import com.teaching.enums.SubmitType;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.*;
import com.teaching.service.FileService;
import com.teaching.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final LessonMapper lessonMapper;
    private final GroupMapper groupMapper;
    private final UserMapper userMapper;
    private final FileService fileService;

    @Override
    @Transactional
    public SubmissionDTO submit(Long lessonId, Long userId, MultipartFile file) throws IOException {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课程不存在");
        }

        // 检查截止时间
        if (lesson.getDeadline() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(lesson.getDeadline()) && !lesson.getAllowLate()) {
                throw new BusinessException(400, "已超过截止时间，不允许提交");
            }
        }

        Long groupId = null;
        String pathSuffix;

        if (lesson.getSubmitType() == SubmitType.GROUP) {
            // 小组作业：检查是否在小组中
            Group group = groupMapper.selectByUserIdAndSubjectId(userId, lesson.getSubjectId());
            if (group == null) {
                throw new BusinessException(400, "您尚未加入小组，无法提交小组作业");
            }
            groupId = group.getId();
            pathSuffix = "group_" + groupId;

            // 检查是否已提交
            Submission existing = submissionMapper.selectByLessonIdAndGroupId(lessonId, groupId);
            if (existing != null) {
                throw new BusinessException(400, "小组已提交作业，如需修改请使用重新提交功能");
            }
        } else {
            // 个人作业
            pathSuffix = String.valueOf(userId);

            // 检查是否已提交
            Submission existing = submissionMapper.selectByLessonIdAndSubmitterId(lessonId, userId);
            if (existing != null) {
                throw new BusinessException(400, "您已提交作业，如需修改请使用重新提交功能");
            }
        }

        // 保存文件
        String relativePath = lesson.getSubjectId() + "/" + lessonId + "/" + pathSuffix;
        String filePath;

        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".zip")) {
            filePath = fileService.saveZipFile(file, relativePath);
        } else {
            filePath = fileService.saveFile(file, relativePath);
        }

        // 创建提交记录
        Submission submission = new Submission();
        submission.setLessonId(lessonId);
        submission.setSubmitterId(userId);
        submission.setGroupId(groupId);
        submission.setFilePath(filePath);
        submission.setSubmitTime(LocalDateTime.now());
        submissionMapper.insert(submission);

        return toDTO(submission);
    }

    @Override
    @Transactional
    public SubmissionDTO resubmit(Long submissionId, Long userId, MultipartFile file) throws IOException {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }

        // 权限检查：只有提交者或同组成员可以重新提交
        if (!canAccessSubmission(submission, userId)) {
            throw new BusinessException(403, "无权重新提交");
        }

        Lesson lesson = lessonMapper.selectById(submission.getLessonId());

        // 删除旧文件
        fileService.deleteSubmission(submission.getFilePath());

        // 保存新文件
        String relativePath;
        if (submission.getGroupId() != null) {
            relativePath = lesson.getSubjectId() + "/" + submission.getLessonId() + "/group_" + submission.getGroupId();
        } else {
            relativePath = lesson.getSubjectId() + "/" + submission.getLessonId() + "/" + submission.getSubmitterId();
        }

        String filePath;
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".zip")) {
            filePath = fileService.saveZipFile(file, relativePath);
        } else {
            filePath = fileService.saveFile(file, relativePath);
        }

        // 更新提交记录
        submission.setFilePath(filePath);
        submission.setSubmitTime(LocalDateTime.now());
        submissionMapper.updateById(submission);

        return toDTO(submission);
    }

    @Override
    public List<SubmissionDTO> getSubmissionsByLesson(Long lessonId) {
        List<Submission> submissions = submissionMapper.selectByLessonId(lessonId);
        return submissions.stream().map(this::toDTO).toList();
    }

    @Override
    public SubmissionDTO getSubmission(Long id, Long userId) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }
        return toDTO(submission);
    }

    @Override
    public SubmissionDTO getMySubmission(Long lessonId, Long userId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课程不存在");
        }

        Submission submission;
        if (lesson.getSubmitType() == SubmitType.GROUP) {
            Group group = groupMapper.selectByUserIdAndSubjectId(userId, lesson.getSubjectId());
            if (group == null) {
                return null;
            }
            submission = submissionMapper.selectByLessonIdAndGroupId(lessonId, group.getId());
        } else {
            submission = submissionMapper.selectByLessonIdAndSubmitterId(lessonId, userId);
        }

        return submission != null ? toDTO(submission) : null;
    }

    @Override
    public List<SubmissionListDTO> getMySubmissions(Long userId) {
        List<Submission> submissions = submissionMapper.selectBySubmitterId(userId);
        return submissions.stream().map(this::toListDTO).toList();
    }

    @Override
    public List<FileNode> getSubmissionFiles(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }
        return fileService.listFiles(submission.getFilePath());
    }

    @Override
    public String readSubmissionFile(Long submissionId, String filePath, Long userId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }

        // 安全检查：确保请求的文件在提交目录内
        if (!filePath.startsWith(submission.getFilePath())) {
            throw new BusinessException(400, "非法的文件路径");
        }

        try {
            return fileService.readFile(filePath);
        } catch (IOException e) {
            throw new BusinessException(500, "读取文件失败");
        }
    }

    @Override
    @Transactional
    public void deleteSubmission(Long id, Long userId) throws IOException {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }

        if (!canAccessSubmission(submission, userId)) {
            throw new BusinessException(403, "无权删除此提交");
        }

        fileService.deleteSubmission(submission.getFilePath());
        submissionMapper.deleteById(id);
    }

    private boolean canAccessSubmission(Submission submission, Long userId) {
        if (submission.getSubmitterId().equals(userId)) {
            return true;
        }
        if (submission.getGroupId() != null) {
            Lesson lesson = lessonMapper.selectById(submission.getLessonId());
            Group userGroup = groupMapper.selectByUserIdAndSubjectId(userId, lesson.getSubjectId());
            return userGroup != null && userGroup.getId().equals(submission.getGroupId());
        }
        return false;
    }

    private SubmissionDTO toDTO(Submission submission) {
        SubmissionDTO dto = SubmissionDTO.fromEntity(submission);

        // 填充提交者信息
        User submitter = userMapper.selectById(submission.getSubmitterId());
        if (submitter != null) {
            dto.setSubmitterName(submitter.getName());
        }

        // 填充小组信息
        if (submission.getGroupId() != null) {
            Group group = groupMapper.selectById(submission.getGroupId());
            if (group != null) {
                dto.setGroupName(group.getName());
            }
        }

        // 填充课程信息
        Lesson lesson = lessonMapper.selectById(submission.getLessonId());
        if (lesson != null) {
            dto.setLessonTitle(lesson.getTitle());

            // 计算是否迟交
            if (lesson.getDeadline() != null && submission.getSubmitTime() != null) {
                dto.setIsLate(submission.getSubmitTime().isAfter(lesson.getDeadline()));
            } else {
                dto.setIsLate(false);
            }
        }

        // 加载文件列表
        dto.setFiles(fileService.listFiles(submission.getFilePath()));

        return dto;
    }

    private SubmissionListDTO toListDTO(Submission submission) {
        SubmissionListDTO dto = new SubmissionListDTO();
        dto.setId(submission.getId());
        dto.setLessonId(submission.getLessonId());
        dto.setSubmitterId(submission.getSubmitterId());
        dto.setGroupId(submission.getGroupId());
        dto.setSubmitTime(submission.getSubmitTime());

        User submitter = userMapper.selectById(submission.getSubmitterId());
        if (submitter != null) {
            dto.setSubmitterName(submitter.getName());
        }

        Lesson lesson = lessonMapper.selectById(submission.getLessonId());
        if (lesson != null) {
            dto.setLessonTitle(lesson.getTitle());
            if (lesson.getDeadline() != null && submission.getSubmitTime() != null) {
                dto.setIsLate(submission.getSubmitTime().isAfter(lesson.getDeadline()));
            }
        }

        return dto;
    }
}
```

**Step 6: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=SubmissionServiceTest -q
```

**Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add SubmissionService with file upload support"
```

---

## Task 5: 创建作业提交 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/SubmissionController.java`
- Test: `backend/src/test/java/com/teaching/controller/SubmissionControllerTest.java`

**Step 1: 编写 Controller 测试（红灯）**

```java
// backend/src/test/java/com/teaching/controller/SubmissionControllerTest.java
package com.teaching.controller;

import com.teaching.dto.SubmissionDTO;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SubmissionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private SubmissionController submissionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(submissionController).build();

        // 模拟登录用户
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "student", "password", "STUDENT", "学生");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("提交作业")
    void submit_shouldReturnSubmission() throws Exception {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(1L);
        dto.setLessonId(1L);

        when(submissionService.submit(eq(1L), eq(1L), any())).thenReturn(dto);

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.zip", "application/zip", "content".getBytes()
        );

        mockMvc.perform(multipart("/api/submissions/lesson/1")
                .file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("获取课程提交列表")
    void getSubmissionsByLesson_shouldReturnList() throws Exception {
        SubmissionDTO dto1 = new SubmissionDTO();
        dto1.setId(1L);
        SubmissionDTO dto2 = new SubmissionDTO();
        dto2.setId(2L);

        when(submissionService.getSubmissionsByLesson(1L)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/submissions/lesson/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("获取我的提交")
    void getMySubmission_shouldReturnSubmission() throws Exception {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(1L);
        dto.setLessonId(1L);

        when(submissionService.getMySubmission(1L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/api/submissions/lesson/1/mine"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("获取提交文件列表")
    void getSubmissionFiles_shouldReturnFileTree() throws Exception {
        when(submissionService.getSubmissionFiles(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/submissions/1/files"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("读取提交文件内容")
    void readFile_shouldReturnContent() throws Exception {
        when(submissionService.readSubmissionFile(eq(1L), anyString(), eq(1L)))
            .thenReturn("public class Test {}");

        mockMvc.perform(get("/api/submissions/1/files/read")
                .param("path", "1/1/10/Test.java"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("public class Test {}"));
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=SubmissionControllerTest -q
```

**Step 3: 创建 SubmissionController**

```java
// backend/src/main/java/com/teaching/controller/SubmissionController.java
package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.FileNode;
import com.teaching.dto.SubmissionDTO;
import com.teaching.dto.SubmissionListDTO;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * 提交作业
     */
    @PostMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public Result<SubmissionDTO> submit(
            @PathVariable Long lessonId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl user) throws IOException {
        SubmissionDTO result = submissionService.submit(lessonId, user.getId(), file);
        return Result.success(result);
    }

    /**
     * 重新提交作业
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public Result<SubmissionDTO> resubmit(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl user) throws IOException {
        SubmissionDTO result = submissionService.resubmit(id, user.getId(), file);
        return Result.success(result);
    }

    /**
     * 获取课程的所有提交（教员用）
     */
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<List<SubmissionDTO>> getSubmissionsByLesson(@PathVariable Long lessonId) {
        List<SubmissionDTO> submissions = submissionService.getSubmissionsByLesson(lessonId);
        return Result.success(submissions);
    }

    /**
     * 获取我在某课程的提交
     */
    @GetMapping("/lesson/{lessonId}/mine")
    public Result<SubmissionDTO> getMySubmission(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetailsImpl user) {
        SubmissionDTO submission = submissionService.getMySubmission(lessonId, user.getId());
        return Result.success(submission);
    }

    /**
     * 获取我的所有提交
     */
    @GetMapping("/mine")
    public Result<List<SubmissionListDTO>> getMySubmissions(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<SubmissionListDTO> submissions = submissionService.getMySubmissions(user.getId());
        return Result.success(submissions);
    }

    /**
     * 获取提交详情
     */
    @GetMapping("/{id}")
    public Result<SubmissionDTO> getSubmission(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        SubmissionDTO submission = submissionService.getSubmission(id, user.getId());
        return Result.success(submission);
    }

    /**
     * 获取提交的文件列表
     */
    @GetMapping("/{id}/files")
    public Result<List<FileNode>> getSubmissionFiles(@PathVariable Long id) {
        List<FileNode> files = submissionService.getSubmissionFiles(id);
        return Result.success(files);
    }

    /**
     * 读取提交中的文件内容
     */
    @GetMapping("/{id}/files/read")
    public Result<String> readFile(
            @PathVariable Long id,
            @RequestParam String path,
            @AuthenticationPrincipal UserDetailsImpl user) {
        String content = submissionService.readSubmissionFile(id, path, user.getId());
        return Result.success(content);
    }

    /**
     * 删除提交
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public Result<Void> deleteSubmission(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) throws IOException {
        submissionService.deleteSubmission(id, user.getId());
        return Result.success();
    }
}
```

**Step 4: 配置文件上传大小限制（application.yml）**

```yaml
# 在 backend/src/main/resources/application.yml 中添加
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=SubmissionControllerTest -q
```

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add SubmissionController with file upload API"
```

---

## Task 6: 前端作业提交功能

**Files:**
- Create: `frontend/src/api/submission.ts`
- Create: `frontend/src/views/submissions/SubmissionUpload.vue`
- Create: `frontend/src/views/submissions/SubmissionViewer.vue`
- Create: `frontend/src/views/submissions/FileTree.vue`
- Create: `frontend/src/views/submissions/CodeViewer.vue`

**Step 1: 创建 API 模块**

```typescript
// frontend/src/api/submission.ts
import request from '@/utils/request'

export interface FileNode {
  name: string
  path: string
  type: 'file' | 'directory'
  size?: number
  children?: FileNode[]
}

export interface SubmissionDTO {
  id: number
  lessonId: number
  lessonTitle?: string
  submitterId: number
  submitterName?: string
  groupId?: number
  groupName?: string
  filePath: string
  files?: FileNode[]
  submitTime: string
  isLate?: boolean
  grade?: string
  gradeComment?: string
  gradeTime?: string
}

export interface SubmissionListDTO {
  id: number
  lessonId: number
  lessonTitle?: string
  submitterId: number
  submitterName?: string
  groupId?: number
  groupName?: string
  submitTime: string
  isLate?: boolean
  grade?: string
  hasGrade?: boolean
}

// 提交作业
export function submitHomework(lessonId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<SubmissionDTO>(`/submissions/lesson/${lessonId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 重新提交作业
export function resubmitHomework(submissionId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.put<SubmissionDTO>(`/submissions/${submissionId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取课程的所有提交（教员用）
export function getSubmissionsByLesson(lessonId: number) {
  return request.get<SubmissionDTO[]>(`/submissions/lesson/${lessonId}`)
}

// 获取我在某课程的提交
export function getMySubmission(lessonId: number) {
  return request.get<SubmissionDTO>(`/submissions/lesson/${lessonId}/mine`)
}

// 获取我的所有提交
export function getMySubmissions() {
  return request.get<SubmissionListDTO[]>('/submissions/mine')
}

// 获取提交详情
export function getSubmission(id: number) {
  return request.get<SubmissionDTO>(`/submissions/${id}`)
}

// 获取提交的文件列表
export function getSubmissionFiles(id: number) {
  return request.get<FileNode[]>(`/submissions/${id}/files`)
}

// 读取文件内容
export function readSubmissionFile(id: number, path: string) {
  return request.get<string>(`/submissions/${id}/files/read`, { params: { path } })
}

// 删除提交
export function deleteSubmission(id: number) {
  return request.delete(`/submissions/${id}`)
}
```

**Step 2: 创建文件树组件**

```vue
<!-- frontend/src/views/submissions/FileTree.vue -->
<template>
  <div class="file-tree">
    <el-tree
      :data="treeData"
      :props="defaultProps"
      node-key="path"
      highlight-current
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <span class="tree-node">
          <el-icon v-if="data.type === 'directory'">
            <Folder />
          </el-icon>
          <el-icon v-else>
            <Document />
          </el-icon>
          <span class="node-label">{{ node.label }}</span>
          <span v-if="data.type === 'file'" class="node-size">
            {{ formatSize(data.size) }}
          </span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Folder, Document } from '@element-plus/icons-vue'
import type { FileNode } from '@/api/submission'

interface Props {
  files: FileNode[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'select-file', path: string): void
}>()

const defaultProps = {
  children: 'children',
  label: 'name'
}

const treeData = computed(() => props.files)

function handleNodeClick(data: FileNode) {
  if (data.type === 'file') {
    emit('select-file', data.path)
  }
}

function formatSize(size?: number): string {
  if (!size) return ''
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}
</script>

<style scoped>
.file-tree {
  height: 100%;
  overflow: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.node-label {
  flex: 1;
}

.node-size {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}
</style>
```

**Step 3: 创建代码查看器组件**

```vue
<!-- frontend/src/views/submissions/CodeViewer.vue -->
<template>
  <div class="code-viewer">
    <div class="code-header">
      <span class="file-path">{{ filePath }}</span>
      <el-button-group size="small">
        <el-button @click="copyCode">
          <el-icon><CopyDocument /></el-icon>
        </el-button>
      </el-button-group>
    </div>
    <div ref="editorContainer" class="editor-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'
import { CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as monaco from 'monaco-editor'

interface Props {
  filePath: string
  content: string
}

const props = defineProps<Props>()

const editorContainer = ref<HTMLElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

onMounted(() => {
  if (editorContainer.value) {
    editor = monaco.editor.create(editorContainer.value, {
      value: props.content,
      language: getLanguage(props.filePath),
      readOnly: true,
      minimap: { enabled: false },
      lineNumbers: 'on',
      scrollBeyondLastLine: false,
      automaticLayout: true,
      fontSize: 14,
      theme: 'vs'
    })
  }
})

watch(() => props.content, (newContent) => {
  if (editor) {
    editor.setValue(newContent)
    monaco.editor.setModelLanguage(editor.getModel()!, getLanguage(props.filePath))
  }
})

watch(() => props.filePath, () => {
  if (editor) {
    monaco.editor.setModelLanguage(editor.getModel()!, getLanguage(props.filePath))
  }
})

onBeforeUnmount(() => {
  editor?.dispose()
})

function getLanguage(path: string): string {
  const ext = path.split('.').pop()?.toLowerCase()
  const languageMap: Record<string, string> = {
    'js': 'javascript',
    'ts': 'typescript',
    'jsx': 'javascript',
    'tsx': 'typescript',
    'vue': 'html',
    'java': 'java',
    'py': 'python',
    'go': 'go',
    'rs': 'rust',
    'c': 'c',
    'cpp': 'cpp',
    'h': 'c',
    'hpp': 'cpp',
    'cs': 'csharp',
    'rb': 'ruby',
    'php': 'php',
    'html': 'html',
    'css': 'css',
    'scss': 'scss',
    'less': 'less',
    'json': 'json',
    'xml': 'xml',
    'yaml': 'yaml',
    'yml': 'yaml',
    'md': 'markdown',
    'sql': 'sql',
    'sh': 'shell',
    'bash': 'shell'
  }
  return languageMap[ext || ''] || 'plaintext'
}

async function copyCode() {
  try {
    await navigator.clipboard.writeText(props.content)
    ElMessage.success('代码已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.code-viewer {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.file-path {
  font-family: monospace;
  font-size: 14px;
  color: #606266;
}

.editor-container {
  flex: 1;
  min-height: 0;
}
</style>
```

**Step 4: 创建作业上传组件**

```vue
<!-- frontend/src/views/submissions/SubmissionUpload.vue -->
<template>
  <el-dialog
    v-model="visible"
    :title="submission ? '重新提交作业' : '提交作业'"
    width="500px"
  >
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      drag
      :auto-upload="false"
      :limit="1"
      accept=".zip,.java,.py,.js,.ts,.html,.css,.json,.md,.txt"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        拖拽文件到此处，或 <em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持上传 ZIP 压缩包（推荐）或单个代码文件，最大 100MB
        </div>
      </template>
    </el-upload>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ submission ? '重新提交' : '提交' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { UploadInstance, UploadUserFile } from 'element-plus'
import { submitHomework, resubmitHomework } from '@/api/submission'
import type { SubmissionDTO } from '@/api/submission'

interface Props {
  modelValue: boolean
  lessonId: number
  submission?: SubmissionDTO | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = ref(props.modelValue)
const loading = ref(false)
const fileList = ref<UploadUserFile[]>([])
const uploadRef = ref<UploadInstance>()

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    fileList.value = []
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

async function handleSubmit() {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  const file = fileList.value[0].raw as File
  loading.value = true

  try {
    if (props.submission) {
      await resubmitHomework(props.submission.id, file)
      ElMessage.success('重新提交成功')
    } else {
      await submitHomework(props.lessonId, file)
      ElMessage.success('提交成功')
    }
    visible.value = false
    emit('success')
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    loading.value = false
  }
}
</script>
```

**Step 5: 创建作业查看器页面**

```vue
<!-- frontend/src/views/submissions/SubmissionViewer.vue -->
<template>
  <div class="submission-viewer">
    <el-page-header @back="router.back()">
      <template #content>
        <span>{{ submission?.lessonTitle }} - 作业详情</span>
      </template>
    </el-page-header>

    <div v-if="submission" class="viewer-content">
      <!-- 提交信息 -->
      <el-descriptions border :column="3" class="submission-info">
        <el-descriptions-item label="提交者">
          {{ submission.groupName || submission.submitterName }}
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ submission.submitTime }}
          <el-tag v-if="submission.isLate" type="danger" size="small">
            迟交
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-tag v-if="submission.grade" :type="getGradeType(submission.grade)">
            {{ submission.grade }}
          </el-tag>
          <span v-else class="text-muted">未评分</span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 代码浏览器 -->
      <div class="code-browser">
        <el-split direction="horizontal" :default-size="0.25">
          <template #start>
            <div class="file-panel">
              <div class="panel-header">文件列表</div>
              <FileTree
                :files="files"
                @select-file="selectFile"
              />
            </div>
          </template>
          <template #end>
            <div class="code-panel">
              <CodeViewer
                v-if="selectedFile"
                :file-path="selectedFile"
                :content="fileContent"
              />
              <el-empty v-else description="请选择文件查看" />
            </div>
          </template>
        </el-split>
      </div>
    </div>

    <el-skeleton v-else :rows="10" animated />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import FileTree from './FileTree.vue'
import CodeViewer from './CodeViewer.vue'
import { getSubmission, getSubmissionFiles, readSubmissionFile } from '@/api/submission'
import type { SubmissionDTO, FileNode } from '@/api/submission'

const route = useRoute()
const router = useRouter()

const submission = ref<SubmissionDTO | null>(null)
const files = ref<FileNode[]>([])
const selectedFile = ref('')
const fileContent = ref('')

const submissionId = Number(route.params.id)

onMounted(async () => {
  await loadSubmission()
  await loadFiles()
})

async function loadSubmission() {
  try {
    const res = await getSubmission(submissionId)
    submission.value = res.data
  } catch (error: any) {
    ElMessage.error('加载提交信息失败')
  }
}

async function loadFiles() {
  try {
    const res = await getSubmissionFiles(submissionId)
    files.value = res.data
  } catch (error: any) {
    ElMessage.error('加载文件列表失败')
  }
}

async function selectFile(path: string) {
  selectedFile.value = path
  try {
    const res = await readSubmissionFile(submissionId, path)
    fileContent.value = res.data
  } catch (error: any) {
    fileContent.value = '// 无法加载文件内容'
    ElMessage.error('加载文件内容失败')
  }
}

function getGradeType(grade: string): 'success' | 'warning' | 'info' | 'danger' {
  const types: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    'A': 'success',
    'B': 'info',
    'C': 'warning',
    'D': 'danger'
  }
  return types[grade] || 'info'
}
</script>

<style scoped>
.submission-viewer {
  padding: 20px;
}

.viewer-content {
  margin-top: 20px;
}

.submission-info {
  margin-bottom: 20px;
}

.code-browser {
  height: calc(100vh - 280px);
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.file-panel,
.code-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 12px 16px;
  font-weight: 500;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.text-muted {
  color: #909399;
}
</style>
```

**Step 6: 安装 Monaco Editor**

```bash
cd frontend && npm install monaco-editor @monaco-editor/loader
```

**Step 7: 添加路由配置**

```typescript
// 在 frontend/src/router/index.ts 中添加
{
  path: '/submissions/:id',
  name: 'SubmissionViewer',
  component: () => import('@/views/submissions/SubmissionViewer.vue'),
  meta: { title: '作业详情', requiresAuth: true }
}
```

**Step 8: Commit**

```bash
git add frontend/
git commit -m "feat: add submission upload and code viewer components"
```

---

## 验证清单

1. **运行后端测试**
   ```bash
   cd backend && mvn test -q
   ```

2. **运行前端测试**
   ```bash
   cd frontend && npm test
   ```

3. **功能验证**
   - 个人作业：上传单个文件
   - 个人作业：上传 ZIP 压缩包
   - 小组作业：小组成员提交
   - 小组作业：非小组成员无法提交
   - 超过截止时间：不允许补交时拒绝
   - 超过截止时间：允许补交时可提交
   - 文件浏览：目录树正确展示
   - 代码查看：Monaco Editor 语法高亮

下一步：继续 `05-summary.md` 完成阶段二总结。
