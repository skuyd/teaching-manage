# 阶段三：代码评论功能

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现类似 GitHub Code Review 的代码行内评论功能，教员可在学员提交的代码特定行添加评论。

**Architecture:** RESTful API，评论关联到提交的文件路径和行号。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3, Element Plus, Monaco Editor

**依赖:** 需要先完成 `step2/04-submission-management.md`

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Assignment Management Page | AlHE4 | 代码查看器区域，评论展示 |

### 设计规范

- **代码查看器**: Monaco Editor 深色主题 (vs-dark)
- **目录树**: 左侧 250px，文件图标，展开/折叠
- **行号区域**: 可点击添加评论
- **评论气泡**: 橙色边框，显示评论者和内容
- **评论列表**: 按文件和行号分组显示

---

## Task 1: 创建代码评论实体

**Files:**
- Create: `backend/src/main/java/com/teaching/entity/CodeComment.java`
- Create: `backend/src/main/java/com/teaching/mapper/CodeCommentMapper.java`
- Test: `backend/src/test/java/com/teaching/entity/CodeCommentTest.java`

**Step 1: 编写测试（红灯）**

```java
// backend/src/test/java/com/teaching/entity/CodeCommentTest.java
package com.teaching.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeCommentTest {

    @Test
    @DisplayName("创建代码评论实体")
    void createCodeComment_shouldHaveAllFields() {
        CodeComment comment = new CodeComment();
        comment.setId(1L);
        comment.setSubmissionId(1L);
        comment.setFilePath("src/Main.java");
        comment.setLineNumber(10);
        comment.setContent("这里可以优化，建议使用 StringBuilder");
        comment.setCommenterId(5L);

        assertEquals(1L, comment.getId());
        assertEquals(1L, comment.getSubmissionId());
        assertEquals("src/Main.java", comment.getFilePath());
        assertEquals(10, comment.getLineNumber());
        assertNotNull(comment.getContent());
        assertEquals(5L, comment.getCommenterId());
    }

    @Test
    @DisplayName("评论内容不能为空")
    void commentContent_shouldNotBeEmpty() {
        CodeComment comment = new CodeComment();
        comment.setContent("有效评论");

        assertFalse(comment.getContent().isEmpty());
    }

    @Test
    @DisplayName("行号必须是正数")
    void lineNumber_shouldBePositive() {
        CodeComment comment = new CodeComment();
        comment.setLineNumber(1);

        assertTrue(comment.getLineNumber() > 0);
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=CodeCommentTest -q
```

**Step 3: 创建 CodeComment 实体**

```java
// backend/src/main/java/com/teaching/entity/CodeComment.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_code_comment")
public class CodeComment extends BaseEntity {

    private Long submissionId;   // 关联的提交ID

    private String filePath;     // 文件相对路径（相对于提交根目录）

    private Integer lineNumber;  // 评论所在行号

    private String content;      // 评论内容

    private Long commenterId;    // 评论者用户ID
}
```

**Step 4: 创建 CodeCommentMapper**

```java
// backend/src/main/java/com/teaching/mapper/CodeCommentMapper.java
package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.CodeComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CodeCommentMapper extends BaseMapper<CodeComment> {

    @Select("SELECT * FROM t_code_comment WHERE submission_id = #{submissionId} AND del_flag = 0 ORDER BY file_path, line_number")
    List<CodeComment> selectBySubmissionId(Long submissionId);

    @Select("SELECT * FROM t_code_comment WHERE submission_id = #{submissionId} AND file_path = #{filePath} AND del_flag = 0 ORDER BY line_number")
    List<CodeComment> selectBySubmissionIdAndFilePath(Long submissionId, String filePath);

    @Select("SELECT COUNT(*) FROM t_code_comment WHERE submission_id = #{submissionId} AND del_flag = 0")
    Long countBySubmissionId(Long submissionId);
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=CodeCommentTest -q
```

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add CodeComment entity and mapper"
```

---

## Task 2: 创建代码评论 DTO

**Files:**
- Create: `backend/src/main/java/com/teaching/dto/CodeCommentDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/CreateCodeCommentRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/FileCommentsDTO.java`

**Step 1: 创建 CodeCommentDTO**

```java
// backend/src/main/java/com/teaching/dto/CodeCommentDTO.java
package com.teaching.dto;

import com.teaching.entity.CodeComment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeCommentDTO {

    private Long id;
    private Long submissionId;
    private String filePath;
    private Integer lineNumber;
    private String content;
    private Long commenterId;
    private String commenterName;
    private String commenterAvatar;
    private LocalDateTime createTime;

    public static CodeCommentDTO fromEntity(CodeComment comment) {
        CodeCommentDTO dto = new CodeCommentDTO();
        dto.setId(comment.getId());
        dto.setSubmissionId(comment.getSubmissionId());
        dto.setFilePath(comment.getFilePath());
        dto.setLineNumber(comment.getLineNumber());
        dto.setContent(comment.getContent());
        dto.setCommenterId(comment.getCommenterId());
        dto.setCreateTime(comment.getCreateTime());
        return dto;
    }
}
```

**Step 2: 创建 CreateCodeCommentRequest**

```java
// backend/src/main/java/com/teaching/dto/CreateCodeCommentRequest.java
package com.teaching.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCodeCommentRequest {

    @NotNull(message = "提交ID不能为空")
    private Long submissionId;

    @NotBlank(message = "文件路径不能为空")
    private String filePath;

    @NotNull(message = "行号不能为空")
    @Min(value = 1, message = "行号必须大于0")
    private Integer lineNumber;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
```

**Step 3: 创建 FileCommentsDTO（按文件聚合评论）**

```java
// backend/src/main/java/com/teaching/dto/FileCommentsDTO.java
package com.teaching.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FileCommentsDTO {

    private String filePath;
    private Integer commentCount;
    private Map<Integer, List<CodeCommentDTO>> lineComments;  // key: 行号, value: 该行的评论列表
}
```

**Step 4: Commit**

```bash
git add backend/src/
git commit -m "feat: add CodeComment DTOs"
```

---

## Task 3: 创建代码评论 Service

**Files:**
- Create: `backend/src/main/java/com/teaching/service/CodeCommentService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/CodeCommentServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/service/CodeCommentServiceTest.java`

**Step 1: 编写 CodeCommentService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/CodeCommentServiceTest.java
package com.teaching.service;

import com.teaching.dto.CodeCommentDTO;
import com.teaching.dto.CreateCodeCommentRequest;
import com.teaching.dto.FileCommentsDTO;
import com.teaching.entity.CodeComment;
import com.teaching.entity.Submission;
import com.teaching.entity.User;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.CodeCommentMapper;
import com.teaching.mapper.SubmissionMapper;
import com.teaching.mapper.UserMapper;
import com.teaching.service.impl.CodeCommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeCommentServiceTest {

    @Mock
    private CodeCommentMapper codeCommentMapper;

    @Mock
    private SubmissionMapper submissionMapper;

    @Mock
    private UserMapper userMapper;

    private CodeCommentServiceImpl codeCommentService;

    @BeforeEach
    void setUp() {
        codeCommentService = new CodeCommentServiceImpl(codeCommentMapper, submissionMapper, userMapper);
    }

    @Test
    @DisplayName("添加代码评论")
    void addComment_shouldCreateComment() {
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setFilePath("1/1/10");

        when(submissionMapper.selectById(1L)).thenReturn(submission);
        when(codeCommentMapper.insert(any(CodeComment.class))).thenReturn(1);

        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(1L);
        request.setFilePath("src/Main.java");
        request.setLineNumber(10);
        request.setContent("这里需要优化");

        codeCommentService.addComment(request, 5L);

        verify(codeCommentMapper).insert(argThat(c ->
            c.getSubmissionId() == 1L &&
            c.getFilePath().equals("src/Main.java") &&
            c.getLineNumber() == 10 &&
            c.getCommenterId() == 5L
        ));
    }

    @Test
    @DisplayName("提交不存在时不能添加评论")
    void addComment_shouldFailIfSubmissionNotFound() {
        when(submissionMapper.selectById(1L)).thenReturn(null);

        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(1L);
        request.setFilePath("src/Main.java");
        request.setLineNumber(10);
        request.setContent("评论");

        assertThrows(BusinessException.class, () -> codeCommentService.addComment(request, 5L));
    }

    @Test
    @DisplayName("获取提交的所有评论")
    void getCommentsBySubmission_shouldReturnComments() {
        CodeComment c1 = new CodeComment();
        c1.setId(1L);
        c1.setSubmissionId(1L);
        c1.setFilePath("src/Main.java");
        c1.setLineNumber(10);
        c1.setContent("评论1");
        c1.setCommenterId(5L);

        CodeComment c2 = new CodeComment();
        c2.setId(2L);
        c2.setSubmissionId(1L);
        c2.setFilePath("src/Main.java");
        c2.setLineNumber(20);
        c2.setContent("评论2");
        c2.setCommenterId(5L);

        User teacher = new User();
        teacher.setId(5L);
        teacher.setName("教员");

        when(codeCommentMapper.selectBySubmissionId(1L)).thenReturn(List.of(c1, c2));
        when(userMapper.selectById(5L)).thenReturn(teacher);

        List<CodeCommentDTO> result = codeCommentService.getCommentsBySubmission(1L);

        assertEquals(2, result.size());
        assertEquals("教员", result.get(0).getCommenterName());
    }

    @Test
    @DisplayName("按文件聚合评论")
    void getCommentsByFile_shouldGroupByLine() {
        CodeComment c1 = new CodeComment();
        c1.setId(1L);
        c1.setFilePath("src/Main.java");
        c1.setLineNumber(10);
        c1.setCommenterId(5L);

        CodeComment c2 = new CodeComment();
        c2.setId(2L);
        c2.setFilePath("src/Main.java");
        c2.setLineNumber(10);  // 同一行有两条评论
        c2.setCommenterId(5L);

        User teacher = new User();
        teacher.setId(5L);
        teacher.setName("教员");

        when(codeCommentMapper.selectBySubmissionIdAndFilePath(1L, "src/Main.java")).thenReturn(List.of(c1, c2));
        when(userMapper.selectById(5L)).thenReturn(teacher);

        FileCommentsDTO result = codeCommentService.getCommentsByFile(1L, "src/Main.java");

        assertEquals(2, result.getCommentCount());
        assertEquals(2, result.getLineComments().get(10).size());
    }

    @Test
    @DisplayName("只有评论者本人可以删除评论")
    void deleteComment_shouldCheckOwnership() {
        CodeComment comment = new CodeComment();
        comment.setId(1L);
        comment.setCommenterId(5L);

        when(codeCommentMapper.selectById(1L)).thenReturn(comment);

        // 非评论者删除应失败
        assertThrows(BusinessException.class, () -> codeCommentService.deleteComment(1L, 10L));

        // 评论者本人可以删除
        when(codeCommentMapper.deleteById(1L)).thenReturn(1);
        assertDoesNotThrow(() -> codeCommentService.deleteComment(1L, 5L));
    }

    @Test
    @DisplayName("更新评论内容")
    void updateComment_shouldModifyContent() {
        CodeComment comment = new CodeComment();
        comment.setId(1L);
        comment.setCommenterId(5L);
        comment.setContent("原评论");

        when(codeCommentMapper.selectById(1L)).thenReturn(comment);
        when(codeCommentMapper.updateById(any(CodeComment.class))).thenReturn(1);

        codeCommentService.updateComment(1L, "新评论内容", 5L);

        verify(codeCommentMapper).updateById(argThat(c ->
            c.getContent().equals("新评论内容")
        ));
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=CodeCommentServiceTest -q
```

**Step 3: 创建 CodeCommentService 接口**

```java
// backend/src/main/java/com/teaching/service/CodeCommentService.java
package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.CodeCommentDTO;
import com.teaching.dto.CreateCodeCommentRequest;
import com.teaching.dto.FileCommentsDTO;
import com.teaching.entity.CodeComment;

import java.util.List;
import java.util.Map;

public interface CodeCommentService extends IService<CodeComment> {

    /**
     * 添加代码评论
     */
    CodeCommentDTO addComment(CreateCodeCommentRequest request, Long userId);

    /**
     * 获取提交的所有评论
     */
    List<CodeCommentDTO> getCommentsBySubmission(Long submissionId);

    /**
     * 获取某文件的所有评论（按行聚合）
     */
    FileCommentsDTO getCommentsByFile(Long submissionId, String filePath);

    /**
     * 获取提交的评论统计（按文件）
     */
    Map<String, Integer> getCommentCountByFile(Long submissionId);

    /**
     * 更新评论
     */
    void updateComment(Long commentId, String content, Long userId);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId, Long userId);
}
```

**Step 4: 创建 CodeCommentServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/CodeCommentServiceImpl.java
package com.teaching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.CodeCommentDTO;
import com.teaching.dto.CreateCodeCommentRequest;
import com.teaching.dto.FileCommentsDTO;
import com.teaching.entity.CodeComment;
import com.teaching.entity.Submission;
import com.teaching.entity.User;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.CodeCommentMapper;
import com.teaching.mapper.SubmissionMapper;
import com.teaching.mapper.UserMapper;
import com.teaching.service.CodeCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeCommentServiceImpl extends ServiceImpl<CodeCommentMapper, CodeComment> implements CodeCommentService {

    private final CodeCommentMapper codeCommentMapper;
    private final SubmissionMapper submissionMapper;
    private final UserMapper userMapper;

    @Override
    public CodeCommentDTO addComment(CreateCodeCommentRequest request, Long userId) {
        // 验证提交是否存在
        Submission submission = submissionMapper.selectById(request.getSubmissionId());
        if (submission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }

        // 创建评论
        CodeComment comment = new CodeComment();
        comment.setSubmissionId(request.getSubmissionId());
        comment.setFilePath(request.getFilePath());
        comment.setLineNumber(request.getLineNumber());
        comment.setContent(request.getContent());
        comment.setCommenterId(userId);

        codeCommentMapper.insert(comment);

        return toDTO(comment);
    }

    @Override
    public List<CodeCommentDTO> getCommentsBySubmission(Long submissionId) {
        List<CodeComment> comments = codeCommentMapper.selectBySubmissionId(submissionId);
        return comments.stream().map(this::toDTO).toList();
    }

    @Override
    public FileCommentsDTO getCommentsByFile(Long submissionId, String filePath) {
        List<CodeComment> comments = codeCommentMapper.selectBySubmissionIdAndFilePath(submissionId, filePath);

        FileCommentsDTO dto = new FileCommentsDTO();
        dto.setFilePath(filePath);
        dto.setCommentCount(comments.size());

        // 按行号分组
        Map<Integer, List<CodeCommentDTO>> lineComments = comments.stream()
                .map(this::toDTO)
                .collect(Collectors.groupingBy(CodeCommentDTO::getLineNumber));

        dto.setLineComments(lineComments);
        return dto;
    }

    @Override
    public Map<String, Integer> getCommentCountByFile(Long submissionId) {
        List<CodeComment> comments = codeCommentMapper.selectBySubmissionId(submissionId);

        return comments.stream()
                .collect(Collectors.groupingBy(
                        CodeComment::getFilePath,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    @Override
    public void updateComment(Long commentId, String content, Long userId) {
        CodeComment comment = codeCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "评论不存在");
        }

        if (!comment.getCommenterId().equals(userId)) {
            throw new BusinessException(403, "只能修改自己的评论");
        }

        comment.setContent(content);
        codeCommentMapper.updateById(comment);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        CodeComment comment = codeCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "评论不存在");
        }

        if (!comment.getCommenterId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的评论");
        }

        codeCommentMapper.deleteById(commentId);
    }

    private CodeCommentDTO toDTO(CodeComment comment) {
        CodeCommentDTO dto = CodeCommentDTO.fromEntity(comment);

        // 填充评论者信息
        User commenter = userMapper.selectById(comment.getCommenterId());
        if (commenter != null) {
            dto.setCommenterName(commenter.getName());
            dto.setCommenterAvatar(commenter.getAvatar());
        }

        return dto;
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=CodeCommentServiceTest -q
```

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add CodeCommentService"
```

---

## Task 4: 创建代码评论 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/CodeCommentController.java`
- Test: `backend/src/test/java/com/teaching/controller/CodeCommentControllerTest.java`

**Step 1: 编写 Controller 测试（红灯）**

```java
// backend/src/test/java/com/teaching/controller/CodeCommentControllerTest.java
package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CodeCommentDTO;
import com.teaching.dto.CreateCodeCommentRequest;
import com.teaching.dto.FileCommentsDTO;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.CodeCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CodeCommentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CodeCommentService codeCommentService;

    @InjectMocks
    private CodeCommentController codeCommentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(codeCommentController).build();

        // 模拟教员登录
        UserDetailsImpl userDetails = new UserDetailsImpl(5L, "teacher", "password", "TEACHER", "教员");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("添加代码评论")
    void addComment_shouldReturnCreatedComment() throws Exception {
        CodeCommentDTO dto = new CodeCommentDTO();
        dto.setId(1L);
        dto.setSubmissionId(1L);
        dto.setFilePath("src/Main.java");
        dto.setLineNumber(10);
        dto.setContent("评论内容");

        when(codeCommentService.addComment(any(CreateCodeCommentRequest.class), eq(5L))).thenReturn(dto);

        CreateCodeCommentRequest request = new CreateCodeCommentRequest();
        request.setSubmissionId(1L);
        request.setFilePath("src/Main.java");
        request.setLineNumber(10);
        request.setContent("评论内容");

        mockMvc.perform(post("/api/code-comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("获取提交的所有评论")
    void getCommentsBySubmission_shouldReturnList() throws Exception {
        CodeCommentDTO dto1 = new CodeCommentDTO();
        dto1.setId(1L);
        CodeCommentDTO dto2 = new CodeCommentDTO();
        dto2.setId(2L);

        when(codeCommentService.getCommentsBySubmission(1L)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/code-comments/submission/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("获取文件的评论（按行聚合）")
    void getCommentsByFile_shouldReturnGroupedComments() throws Exception {
        FileCommentsDTO dto = new FileCommentsDTO();
        dto.setFilePath("src/Main.java");
        dto.setCommentCount(2);

        when(codeCommentService.getCommentsByFile(1L, "src/Main.java")).thenReturn(dto);

        mockMvc.perform(get("/api/code-comments/submission/1/file")
                .param("path", "src/Main.java"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.filePath").value("src/Main.java"));
    }

    @Test
    @DisplayName("删除评论")
    void deleteComment_shouldCallService() throws Exception {
        doNothing().when(codeCommentService).deleteComment(1L, 5L);

        mockMvc.perform(delete("/api/code-comments/1"))
            .andExpect(status().isOk());

        verify(codeCommentService).deleteComment(1L, 5L);
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=CodeCommentControllerTest -q
```

**Step 3: 创建 CodeCommentController**

```java
// backend/src/main/java/com/teaching/controller/CodeCommentController.java
package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.CodeCommentDTO;
import com.teaching.dto.CreateCodeCommentRequest;
import com.teaching.dto.FileCommentsDTO;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.CodeCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/code-comments")
@RequiredArgsConstructor
public class CodeCommentController {

    private final CodeCommentService codeCommentService;

    /**
     * 添加代码评论
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<CodeCommentDTO> addComment(
            @Valid @RequestBody CreateCodeCommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        CodeCommentDTO result = codeCommentService.addComment(request, user.getId());
        return Result.success(result);
    }

    /**
     * 获取提交的所有评论
     */
    @GetMapping("/submission/{submissionId}")
    public Result<List<CodeCommentDTO>> getCommentsBySubmission(@PathVariable Long submissionId) {
        List<CodeCommentDTO> comments = codeCommentService.getCommentsBySubmission(submissionId);
        return Result.success(comments);
    }

    /**
     * 获取某文件的评论（按行聚合）
     */
    @GetMapping("/submission/{submissionId}/file")
    public Result<FileCommentsDTO> getCommentsByFile(
            @PathVariable Long submissionId,
            @RequestParam String path) {
        FileCommentsDTO comments = codeCommentService.getCommentsByFile(submissionId, path);
        return Result.success(comments);
    }

    /**
     * 获取提交的评论数统计（按文件）
     */
    @GetMapping("/submission/{submissionId}/count")
    public Result<Map<String, Integer>> getCommentCountByFile(@PathVariable Long submissionId) {
        Map<String, Integer> counts = codeCommentService.getCommentCountByFile(submissionId);
        return Result.success(counts);
    }

    /**
     * 更新评论
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Void> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetailsImpl user) {
        codeCommentService.updateComment(id, body.get("content"), user.getId());
        return Result.success();
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        codeCommentService.deleteComment(id, user.getId());
        return Result.success();
    }
}
```

**Step 4: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=CodeCommentControllerTest -q
```

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add CodeCommentController"
```

---

## Task 5: 前端代码评论功能

**Files:**
- Create: `frontend/src/api/codeComment.ts`
- Update: `frontend/src/views/submissions/CodeViewer.vue`
- Create: `frontend/src/views/submissions/CommentPopover.vue`

**Step 1: 创建代码评论 API**

```typescript
// frontend/src/api/codeComment.ts
import request from '@/utils/request'

export interface CodeCommentDTO {
  id: number
  submissionId: number
  filePath: string
  lineNumber: number
  content: string
  commenterId: number
  commenterName?: string
  commenterAvatar?: string
  createTime: string
}

export interface FileCommentsDTO {
  filePath: string
  commentCount: number
  lineComments: Record<number, CodeCommentDTO[]>
}

export interface CreateCodeCommentRequest {
  submissionId: number
  filePath: string
  lineNumber: number
  content: string
}

// 添加评论
export function addCodeComment(data: CreateCodeCommentRequest) {
  return request.post<CodeCommentDTO>('/code-comments', data)
}

// 获取提交的所有评论
export function getCommentsBySubmission(submissionId: number) {
  return request.get<CodeCommentDTO[]>(`/code-comments/submission/${submissionId}`)
}

// 获取文件的评论
export function getCommentsByFile(submissionId: number, filePath: string) {
  return request.get<FileCommentsDTO>(`/code-comments/submission/${submissionId}/file`, {
    params: { path: filePath }
  })
}

// 获取评论数统计
export function getCommentCountByFile(submissionId: number) {
  return request.get<Record<string, number>>(`/code-comments/submission/${submissionId}/count`)
}

// 更新评论
export function updateCodeComment(id: number, content: string) {
  return request.put(`/code-comments/${id}`, { content })
}

// 删除评论
export function deleteCodeComment(id: number) {
  return request.delete(`/code-comments/${id}`)
}
```

**Step 2: 创建评论气泡组件**

```vue
<!-- frontend/src/views/submissions/CommentPopover.vue -->
<template>
  <el-popover
    :visible="visible"
    placement="right"
    :width="320"
    trigger="click"
  >
    <template #reference>
      <slot></slot>
    </template>

    <div class="comment-popover">
      <div class="comment-header">
        <span>第 {{ lineNumber }} 行评论</span>
        <el-button
          v-if="canAddComment"
          type="primary"
          size="small"
          text
          @click="showAddForm = true"
        >
          添加评论
        </el-button>
      </div>

      <!-- 评论列表 -->
      <div v-if="comments.length > 0" class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-meta">
            <el-avatar :size="24">
              {{ comment.commenterName?.charAt(0) }}
            </el-avatar>
            <span class="commenter-name">{{ comment.commenterName }}</span>
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div v-if="canEditComment(comment)" class="comment-actions">
            <el-button size="small" text @click="editComment(comment)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(comment)">删除</el-button>
          </div>
        </div>
      </div>

      <!-- 添加评论表单 -->
      <div v-if="showAddForm" class="add-comment-form">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="输入评论内容..."
        />
        <div class="form-actions">
          <el-button size="small" @click="cancelAdd">取消</el-button>
          <el-button size="small" type="primary" :loading="submitting" @click="submitComment">
            提交
          </el-button>
        </div>
      </div>

      <el-empty v-if="comments.length === 0 && !showAddForm" description="暂无评论" />
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { addCodeComment, deleteCodeComment } from '@/api/codeComment'
import type { CodeCommentDTO } from '@/api/codeComment'

interface Props {
  submissionId: number
  filePath: string
  lineNumber: number
  comments: CodeCommentDTO[]
  visible: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'comment-added'): void
  (e: 'comment-deleted'): void
}>()

const userStore = useUserStore()

const showAddForm = ref(false)
const newComment = ref('')
const submitting = ref(false)

const canAddComment = computed(() => {
  return userStore.isTeacher || userStore.isAdmin
})

function canEditComment(comment: CodeCommentDTO): boolean {
  return comment.commenterId === userStore.userInfo?.id
}

async function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await addCodeComment({
      submissionId: props.submissionId,
      filePath: props.filePath,
      lineNumber: props.lineNumber,
      content: newComment.value.trim()
    })
    ElMessage.success('评论添加成功')
    newComment.value = ''
    showAddForm.value = false
    emit('comment-added')
  } catch (error: any) {
    ElMessage.error(error.message || '添加评论失败')
  } finally {
    submitting.value = false
  }
}

function cancelAdd() {
  showAddForm.value = false
  newComment.value = ''
}

function editComment(comment: CodeCommentDTO) {
  // TODO: 实现编辑功能
  ElMessage.info('编辑功能开发中')
}

async function handleDelete(comment: CodeCommentDTO) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除')
    await deleteCodeComment(comment.id)
    ElMessage.success('删除成功')
    emit('comment-deleted')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

function formatTime(time: string): string {
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.comment-popover {
  max-height: 400px;
  overflow-y: auto;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.comment-list {
  margin-bottom: 12px;
}

.comment-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.commenter-name {
  font-weight: 500;
  font-size: 13px;
}

.comment-time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  margin-left: 32px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.comment-actions {
  margin-left: 32px;
  margin-top: 4px;
}

.add-comment-form {
  margin-top: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
```

**Step 3: 更新代码查看器（集成评论功能）**

```vue
<!-- frontend/src/views/submissions/CodeViewer.vue （更新版）-->
<template>
  <div class="code-viewer">
    <div class="code-header">
      <span class="file-path">{{ filePath }}</span>
      <div class="header-actions">
        <el-badge v-if="commentCount > 0" :value="commentCount" type="warning">
          <el-button size="small" @click="showAllComments = !showAllComments">
            <el-icon><ChatDotRound /></el-icon>
            评论
          </el-button>
        </el-badge>
        <el-button size="small" @click="copyCode">
          <el-icon><CopyDocument /></el-icon>
        </el-button>
      </div>
    </div>
    <div ref="editorContainer" class="editor-container"></div>

    <!-- 行内评论指示器 -->
    <div class="line-comments-overlay">
      <div
        v-for="(comments, lineNum) in lineCommentsMap"
        :key="lineNum"
        :style="{ top: getLineTop(Number(lineNum)) + 'px' }"
        class="line-comment-indicator"
        @click="openCommentPopover(Number(lineNum))"
      >
        <el-badge :value="comments.length" type="warning">
          <el-icon><ChatDotRound /></el-icon>
        </el-badge>
      </div>
    </div>

    <!-- 评论弹窗 -->
    <CommentPopover
      v-if="selectedLine !== null"
      :visible="commentPopoverVisible"
      :submission-id="submissionId"
      :file-path="filePath"
      :line-number="selectedLine"
      :comments="lineCommentsMap[selectedLine] || []"
      @update:visible="commentPopoverVisible = $event"
      @comment-added="loadComments"
      @comment-deleted="loadComments"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount, computed } from 'vue'
import { CopyDocument, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as monaco from 'monaco-editor'
import CommentPopover from './CommentPopover.vue'
import { getCommentsByFile } from '@/api/codeComment'
import type { CodeCommentDTO } from '@/api/codeComment'

interface Props {
  submissionId: number
  filePath: string
  content: string
}

const props = defineProps<Props>()

const editorContainer = ref<HTMLElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

const lineCommentsMap = ref<Record<number, CodeCommentDTO[]>>({})
const commentPopoverVisible = ref(false)
const selectedLine = ref<number | null>(null)
const showAllComments = ref(false)

const commentCount = computed(() => {
  return Object.values(lineCommentsMap.value).flat().length
})

onMounted(async () => {
  initEditor()
  await loadComments()
})

function initEditor() {
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
      theme: 'vs',
      glyphMargin: true,  // 启用边距图标
      lineDecorationsWidth: 20
    })

    // 监听行号点击事件
    editor.onMouseDown((e) => {
      if (e.target.type === monaco.editor.MouseTargetType.GUTTER_LINE_NUMBERS) {
        const lineNumber = e.target.position?.lineNumber
        if (lineNumber) {
          openCommentPopover(lineNumber)
        }
      }
    })
  }
}

async function loadComments() {
  try {
    const res = await getCommentsByFile(props.submissionId, props.filePath)
    lineCommentsMap.value = res.data.lineComments || {}
    updateDecorations()
  } catch (error) {
    // 静默处理错误
  }
}

function updateDecorations() {
  if (!editor) return

  const decorations: monaco.editor.IModelDeltaDecoration[] = []

  for (const lineNum of Object.keys(lineCommentsMap.value)) {
    decorations.push({
      range: new monaco.Range(Number(lineNum), 1, Number(lineNum), 1),
      options: {
        isWholeLine: true,
        linesDecorationsClassName: 'line-has-comment',
        glyphMarginClassName: 'comment-glyph',
        glyphMarginHoverMessage: { value: '查看评论' }
      }
    })
  }

  editor.createDecorationsCollection(decorations)
}

function openCommentPopover(lineNumber: number) {
  selectedLine.value = lineNumber
  commentPopoverVisible.value = true
}

function getLineTop(lineNumber: number): number {
  if (!editor) return 0
  const lineTop = editor.getTopForLineNumber(lineNumber)
  return lineTop - editor.getScrollTop()
}

watch(() => props.content, (newContent) => {
  if (editor) {
    editor.setValue(newContent)
    monaco.editor.setModelLanguage(editor.getModel()!, getLanguage(props.filePath))
  }
})

watch(() => props.filePath, async () => {
  if (editor) {
    monaco.editor.setModelLanguage(editor.getModel()!, getLanguage(props.filePath))
  }
  await loadComments()
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
  position: relative;
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

.header-actions {
  display: flex;
  gap: 8px;
}

.editor-container {
  flex: 1;
  min-height: 0;
}

.line-comments-overlay {
  position: absolute;
  left: 0;
  top: 40px;
  width: 30px;
  pointer-events: none;
}

.line-comment-indicator {
  position: absolute;
  left: 5px;
  cursor: pointer;
  pointer-events: auto;
}

:deep(.line-has-comment) {
  background-color: rgba(255, 193, 7, 0.1);
}

:deep(.comment-glyph) {
  background-color: #ffc107;
  border-radius: 50%;
  width: 8px !important;
  height: 8px !important;
  margin-left: 4px;
  margin-top: 6px;
}
</style>
```

**Step 4: Commit**

```bash
git add frontend/src/
git commit -m "feat: add code comment feature with Monaco Editor integration"
```

---

## 验证清单

1. **运行后端测试**
   ```bash
   cd backend && mvn test -Dtest=*CodeComment* -q
   ```

2. **功能验证**
   - 教员在代码行上添加评论
   - 查看评论列表
   - 按行聚合显示评论
   - 编辑自己的评论
   - 删除自己的评论
   - 学员可查看但不能添加评论

下一步：继续 `02-grading.md` 完成作业评分功能。
