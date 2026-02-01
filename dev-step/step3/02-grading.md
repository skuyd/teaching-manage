# 阶段三：作业评分功能

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现 A/B/C/D 等级制评分功能，支持评语和评分时间记录。

**Architecture:** RESTful API，评分关联到作业提交，支持更新评分。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3, Element Plus

**依赖:** 需要先完成 `01-code-comment.md`

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Grade Summary Page | niQ1I | 评分表单，等级选择，评语输入 |

### 设计规范

- **等级选择**: A/B/C/D 按钮组，选中高亮
- **等级颜色**: A-绿色, B-蓝色, C-橙色, D-红色
- **评语输入**: 深色文本域，支持快捷评语
- **评分按钮**: 橙色渐变提交按钮
- **评分状态**: 显示评分时间和评分人

---

## Task 1: 创建评分实体和枚举

**Files:**
- Create: `backend/src/main/java/com/teaching/entity/Grade.java`
- Create: `backend/src/main/java/com/teaching/enums/GradeLevel.java`
- Create: `backend/src/main/java/com/teaching/mapper/GradeMapper.java`
- Test: `backend/src/test/java/com/teaching/entity/GradeTest.java`

**Step 1: 编写测试（红灯）**

```java
// backend/src/test/java/com/teaching/entity/GradeTest.java
package com.teaching.entity;

import com.teaching.enums.GradeLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {

    @Test
    @DisplayName("创建评分实体")
    void createGrade_shouldHaveAllFields() {
        Grade grade = new Grade();
        grade.setId(1L);
        grade.setSubmissionId(1L);
        grade.setGrade(GradeLevel.A);
        grade.setComment("代码结构清晰，命名规范");
        grade.setGraderId(5L);
        grade.setGradeTime(LocalDateTime.now());

        assertEquals(1L, grade.getId());
        assertEquals(1L, grade.getSubmissionId());
        assertEquals(GradeLevel.A, grade.getGrade());
        assertNotNull(grade.getComment());
        assertEquals(5L, grade.getGraderId());
        assertNotNull(grade.getGradeTime());
    }

    @Test
    @DisplayName("评分等级枚举应有四种值")
    void gradeLevel_shouldHaveFourValues() {
        assertEquals(4, GradeLevel.values().length);
        assertNotNull(GradeLevel.A);
        assertNotNull(GradeLevel.B);
        assertNotNull(GradeLevel.C);
        assertNotNull(GradeLevel.D);
    }

    @Test
    @DisplayName("评分等级应有正确的描述")
    void gradeLevel_shouldHaveCorrectDescription() {
        assertEquals("优秀", GradeLevel.A.getDescription());
        assertEquals("良好", GradeLevel.B.getDescription());
        assertEquals("及格", GradeLevel.C.getDescription());
        assertEquals("不及格", GradeLevel.D.getDescription());
    }

    @Test
    @DisplayName("评分等级应有正确的分数范围")
    void gradeLevel_shouldHaveCorrectScoreRange() {
        assertTrue(GradeLevel.A.getMinScore() >= 90);
        assertTrue(GradeLevel.B.getMinScore() >= 75);
        assertTrue(GradeLevel.C.getMinScore() >= 60);
        assertTrue(GradeLevel.D.getMinScore() < 60);
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=GradeTest -q
```

**Step 3: 创建 GradeLevel 枚举**

```java
// backend/src/main/java/com/teaching/enums/GradeLevel.java
package com.teaching.enums;

import lombok.Getter;

@Getter
public enum GradeLevel {
    A("优秀", 90),
    B("良好", 75),
    C("及格", 60),
    D("不及格", 0);

    private final String description;
    private final int minScore;

    GradeLevel(String description, int minScore) {
        this.description = description;
        this.minScore = minScore;
    }

    public static GradeLevel fromScore(int score) {
        if (score >= 90) return A;
        if (score >= 75) return B;
        if (score >= 60) return C;
        return D;
    }
}
```

**Step 4: 创建 Grade 实体**

```java
// backend/src/main/java/com/teaching/entity/Grade.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import com.teaching.enums.GradeLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_grade")
public class Grade extends BaseEntity {

    private Long submissionId;    // 关联的提交ID

    private GradeLevel grade;     // 评分等级

    private String comment;       // 评语

    private Long graderId;        // 评分人ID

    private LocalDateTime gradeTime;  // 评分时间
}
```

**Step 5: 创建 GradeMapper**

```java
// backend/src/main/java/com/teaching/mapper/GradeMapper.java
package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Grade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GradeMapper extends BaseMapper<Grade> {

    @Select("SELECT * FROM t_grade WHERE submission_id = #{submissionId} AND del_flag = 0")
    Grade selectBySubmissionId(Long submissionId);

    @Select("SELECT g.* FROM t_grade g " +
            "INNER JOIN t_submission s ON g.submission_id = s.id " +
            "WHERE s.lesson_id = #{lessonId} AND g.del_flag = 0 AND s.del_flag = 0")
    List<Grade> selectByLessonId(Long lessonId);

    @Select("SELECT g.* FROM t_grade g " +
            "INNER JOIN t_submission s ON g.submission_id = s.id " +
            "INNER JOIN t_lesson l ON s.lesson_id = l.id " +
            "WHERE l.subject_id = #{subjectId} AND g.del_flag = 0 AND s.del_flag = 0 AND l.del_flag = 0")
    List<Grade> selectBySubjectId(Long subjectId);
}
```

**Step 6: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=GradeTest -q
```

**Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add Grade entity and GradeLevel enum"
```

---

## Task 2: 创建评分 DTO

**Files:**
- Create: `backend/src/main/java/com/teaching/dto/GradeDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/CreateGradeRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/UpdateGradeRequest.java`

**Step 1: 创建 GradeDTO**

```java
// backend/src/main/java/com/teaching/dto/GradeDTO.java
package com.teaching.dto;

import com.teaching.entity.Grade;
import com.teaching.enums.GradeLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GradeDTO {

    private Long id;
    private Long submissionId;
    private GradeLevel grade;
    private String gradeDescription;
    private String comment;
    private Long graderId;
    private String graderName;
    private LocalDateTime gradeTime;

    // 关联的提交信息
    private Long lessonId;
    private String lessonTitle;
    private Long submitterId;
    private String submitterName;
    private Long groupId;
    private String groupName;

    public static GradeDTO fromEntity(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setSubmissionId(grade.getSubmissionId());
        dto.setGrade(grade.getGrade());
        dto.setGradeDescription(grade.getGrade().getDescription());
        dto.setComment(grade.getComment());
        dto.setGraderId(grade.getGraderId());
        dto.setGradeTime(grade.getGradeTime());
        return dto;
    }
}
```

**Step 2: 创建 CreateGradeRequest**

```java
// backend/src/main/java/com/teaching/dto/CreateGradeRequest.java
package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGradeRequest {

    @NotNull(message = "提交ID不能为空")
    private Long submissionId;

    @NotNull(message = "评分等级不能为空")
    private GradeLevel grade;

    private String comment;  // 评语，可选
}
```

**Step 3: 创建 UpdateGradeRequest**

```java
// backend/src/main/java/com/teaching/dto/UpdateGradeRequest.java
package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateGradeRequest {

    @NotNull(message = "评分等级不能为空")
    private GradeLevel grade;

    private String comment;
}
```

**Step 4: Commit**

```bash
git add backend/src/
git commit -m "feat: add Grade DTOs"
```

---

## Task 3: 创建评分 Service

**Files:**
- Create: `backend/src/main/java/com/teaching/service/GradeService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/GradeServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/service/GradeServiceTest.java`

**Step 1: 编写 GradeService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/GradeServiceTest.java
package com.teaching.service;

import com.teaching.dto.CreateGradeRequest;
import com.teaching.dto.GradeDTO;
import com.teaching.dto.UpdateGradeRequest;
import com.teaching.entity.Grade;
import com.teaching.entity.Lesson;
import com.teaching.entity.Submission;
import com.teaching.entity.User;
import com.teaching.enums.GradeLevel;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.*;
import com.teaching.service.impl.GradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private SubmissionMapper submissionMapper;

    @Mock
    private LessonMapper lessonMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private GroupMapper groupMapper;

    private GradeServiceImpl gradeService;

    @BeforeEach
    void setUp() {
        gradeService = new GradeServiceImpl(gradeMapper, submissionMapper, lessonMapper, userMapper, groupMapper);
    }

    @Test
    @DisplayName("创建评分")
    void gradeSubmission_shouldCreateGrade() {
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setLessonId(1L);

        when(submissionMapper.selectById(1L)).thenReturn(submission);
        when(gradeMapper.selectBySubmissionId(1L)).thenReturn(null);
        when(gradeMapper.insert(any(Grade.class))).thenReturn(1);

        CreateGradeRequest request = new CreateGradeRequest();
        request.setSubmissionId(1L);
        request.setGrade(GradeLevel.A);
        request.setComment("优秀的作业");

        gradeService.gradeSubmission(request, 5L);

        verify(gradeMapper).insert(argThat(g ->
            g.getSubmissionId() == 1L &&
            g.getGrade() == GradeLevel.A &&
            g.getGraderId() == 5L
        ));
    }

    @Test
    @DisplayName("提交不存在时不能评分")
    void gradeSubmission_shouldFailIfSubmissionNotFound() {
        when(submissionMapper.selectById(1L)).thenReturn(null);

        CreateGradeRequest request = new CreateGradeRequest();
        request.setSubmissionId(1L);
        request.setGrade(GradeLevel.A);

        assertThrows(BusinessException.class, () -> gradeService.gradeSubmission(request, 5L));
    }

    @Test
    @DisplayName("不能重复评分")
    void gradeSubmission_shouldFailIfAlreadyGraded() {
        Submission submission = new Submission();
        submission.setId(1L);

        Grade existingGrade = new Grade();
        existingGrade.setId(1L);

        when(submissionMapper.selectById(1L)).thenReturn(submission);
        when(gradeMapper.selectBySubmissionId(1L)).thenReturn(existingGrade);

        CreateGradeRequest request = new CreateGradeRequest();
        request.setSubmissionId(1L);
        request.setGrade(GradeLevel.A);

        assertThrows(BusinessException.class, () -> gradeService.gradeSubmission(request, 5L));
    }

    @Test
    @DisplayName("更新评分")
    void updateGrade_shouldModifyGrade() {
        Grade grade = new Grade();
        grade.setId(1L);
        grade.setGrade(GradeLevel.B);

        when(gradeMapper.selectById(1L)).thenReturn(grade);
        when(gradeMapper.updateById(any(Grade.class))).thenReturn(1);

        UpdateGradeRequest request = new UpdateGradeRequest();
        request.setGrade(GradeLevel.A);
        request.setComment("修正评分");

        gradeService.updateGrade(1L, request, 5L);

        verify(gradeMapper).updateById(argThat(g ->
            g.getGrade() == GradeLevel.A
        ));
    }

    @Test
    @DisplayName("获取提交的评分")
    void getGradeBySubmission_shouldReturnGrade() {
        Grade grade = new Grade();
        grade.setId(1L);
        grade.setSubmissionId(1L);
        grade.setGrade(GradeLevel.A);
        grade.setGraderId(5L);

        Submission submission = new Submission();
        submission.setId(1L);
        submission.setLessonId(1L);
        submission.setSubmitterId(10L);

        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("课程1");

        User grader = new User();
        grader.setId(5L);
        grader.setName("教员");

        User submitter = new User();
        submitter.setId(10L);
        submitter.setName("学员");

        when(gradeMapper.selectBySubmissionId(1L)).thenReturn(grade);
        when(submissionMapper.selectById(1L)).thenReturn(submission);
        when(lessonMapper.selectById(1L)).thenReturn(lesson);
        when(userMapper.selectById(5L)).thenReturn(grader);
        when(userMapper.selectById(10L)).thenReturn(submitter);

        GradeDTO result = gradeService.getGradeBySubmission(1L);

        assertNotNull(result);
        assertEquals(GradeLevel.A, result.getGrade());
        assertEquals("教员", result.getGraderName());
    }

    @Test
    @DisplayName("获取课程的所有评分")
    void getGradesByLesson_shouldReturnList() {
        Grade g1 = new Grade();
        g1.setId(1L);
        g1.setSubmissionId(1L);
        g1.setGrade(GradeLevel.A);
        g1.setGraderId(5L);

        Grade g2 = new Grade();
        g2.setId(2L);
        g2.setSubmissionId(2L);
        g2.setGrade(GradeLevel.B);
        g2.setGraderId(5L);

        when(gradeMapper.selectByLessonId(1L)).thenReturn(List.of(g1, g2));

        List<GradeDTO> result = gradeService.getGradesByLesson(1L);

        assertEquals(2, result.size());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=GradeServiceTest -q
```

**Step 3: 创建 GradeService 接口**

```java
// backend/src/main/java/com/teaching/service/GradeService.java
package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.CreateGradeRequest;
import com.teaching.dto.GradeDTO;
import com.teaching.dto.UpdateGradeRequest;
import com.teaching.entity.Grade;

import java.util.List;
import java.util.Map;

public interface GradeService extends IService<Grade> {

    /**
     * 对提交进行评分
     */
    GradeDTO gradeSubmission(CreateGradeRequest request, Long graderId);

    /**
     * 更新评分
     */
    GradeDTO updateGrade(Long gradeId, UpdateGradeRequest request, Long graderId);

    /**
     * 获取提交的评分
     */
    GradeDTO getGradeBySubmission(Long submissionId);

    /**
     * 获取课程的所有评分
     */
    List<GradeDTO> getGradesByLesson(Long lessonId);

    /**
     * 获取学科的所有评分
     */
    List<GradeDTO> getGradesBySubject(Long subjectId);

    /**
     * 获取学员的成绩统计
     */
    Map<String, Object> getStudentGradeStats(Long studentId, Long subjectId);

    /**
     * 删除评分
     */
    void deleteGrade(Long gradeId);
}
```

**Step 4: 创建 GradeServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/GradeServiceImpl.java
package com.teaching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.CreateGradeRequest;
import com.teaching.dto.GradeDTO;
import com.teaching.dto.UpdateGradeRequest;
import com.teaching.entity.*;
import com.teaching.enums.GradeLevel;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.*;
import com.teaching.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements GradeService {

    private final GradeMapper gradeMapper;
    private final SubmissionMapper submissionMapper;
    private final LessonMapper lessonMapper;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;

    @Override
    @Transactional
    public GradeDTO gradeSubmission(CreateGradeRequest request, Long graderId) {
        // 验证提交是否存在
        Submission submission = submissionMapper.selectById(request.getSubmissionId());
        if (submission == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "提交记录不存在");
        }

        // 检查是否已评分
        Grade existingGrade = gradeMapper.selectBySubmissionId(request.getSubmissionId());
        if (existingGrade != null) {
            throw new BusinessException(400, "该作业已评分，如需修改请使用更新功能");
        }

        // 创建评分
        Grade grade = new Grade();
        grade.setSubmissionId(request.getSubmissionId());
        grade.setGrade(request.getGrade());
        grade.setComment(request.getComment());
        grade.setGraderId(graderId);
        grade.setGradeTime(LocalDateTime.now());

        gradeMapper.insert(grade);

        return toDTO(grade);
    }

    @Override
    @Transactional
    public GradeDTO updateGrade(Long gradeId, UpdateGradeRequest request, Long graderId) {
        Grade grade = gradeMapper.selectById(gradeId);
        if (grade == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "评分记录不存在");
        }

        grade.setGrade(request.getGrade());
        grade.setComment(request.getComment());
        grade.setGraderId(graderId);  // 记录修改人
        grade.setGradeTime(LocalDateTime.now());  // 更新评分时间

        gradeMapper.updateById(grade);

        return toDTO(grade);
    }

    @Override
    public GradeDTO getGradeBySubmission(Long submissionId) {
        Grade grade = gradeMapper.selectBySubmissionId(submissionId);
        if (grade == null) {
            return null;
        }
        return toDTO(grade);
    }

    @Override
    public List<GradeDTO> getGradesByLesson(Long lessonId) {
        List<Grade> grades = gradeMapper.selectByLessonId(lessonId);
        return grades.stream().map(this::toDTO).toList();
    }

    @Override
    public List<GradeDTO> getGradesBySubject(Long subjectId) {
        List<Grade> grades = gradeMapper.selectBySubjectId(subjectId);
        return grades.stream().map(this::toDTO).toList();
    }

    @Override
    public Map<String, Object> getStudentGradeStats(Long studentId, Long subjectId) {
        List<Grade> grades = gradeMapper.selectBySubjectId(subjectId);

        // 筛选该学员的成绩
        List<Grade> studentGrades = grades.stream()
                .filter(g -> {
                    Submission submission = submissionMapper.selectById(g.getSubmissionId());
                    return submission != null && submission.getSubmitterId().equals(studentId);
                })
                .toList();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", studentGrades.size());

        // 统计各等级数量
        Map<GradeLevel, Long> levelCounts = studentGrades.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Grade::getGrade,
                        java.util.stream.Collectors.counting()
                ));
        stats.put("levelCounts", levelCounts);

        // 计算平均分（A=95, B=82, C=70, D=40）
        if (!studentGrades.isEmpty()) {
            double avgScore = studentGrades.stream()
                    .mapToInt(g -> {
                        return switch (g.getGrade()) {
                            case A -> 95;
                            case B -> 82;
                            case C -> 70;
                            case D -> 40;
                        };
                    })
                    .average()
                    .orElse(0);
            stats.put("averageScore", avgScore);
            stats.put("averageGrade", GradeLevel.fromScore((int) avgScore));
        }

        return stats;
    }

    @Override
    @Transactional
    public void deleteGrade(Long gradeId) {
        Grade grade = gradeMapper.selectById(gradeId);
        if (grade == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "评分记录不存在");
        }
        gradeMapper.deleteById(gradeId);
    }

    private GradeDTO toDTO(Grade grade) {
        GradeDTO dto = GradeDTO.fromEntity(grade);

        // 填充评分人信息
        User grader = userMapper.selectById(grade.getGraderId());
        if (grader != null) {
            dto.setGraderName(grader.getName());
        }

        // 填充提交信息
        Submission submission = submissionMapper.selectById(grade.getSubmissionId());
        if (submission != null) {
            dto.setLessonId(submission.getLessonId());
            dto.setSubmitterId(submission.getSubmitterId());
            dto.setGroupId(submission.getGroupId());

            // 填充课程信息
            Lesson lesson = lessonMapper.selectById(submission.getLessonId());
            if (lesson != null) {
                dto.setLessonTitle(lesson.getTitle());
            }

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
        }

        return dto;
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=GradeServiceTest -q
```

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add GradeService"
```

---

## Task 4: 创建评分 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/GradeController.java`
- Test: `backend/src/test/java/com/teaching/controller/GradeControllerTest.java`

**Step 1: 编写 Controller 测试（红灯）**

```java
// backend/src/test/java/com/teaching/controller/GradeControllerTest.java
package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CreateGradeRequest;
import com.teaching.dto.GradeDTO;
import com.teaching.dto.UpdateGradeRequest;
import com.teaching.enums.GradeLevel;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.GradeService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GradeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GradeService gradeService;

    @InjectMocks
    private GradeController gradeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gradeController).build();

        // 模拟教员登录
        UserDetailsImpl userDetails = new UserDetailsImpl(5L, "teacher", "password", "TEACHER", "教员");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("创建评分")
    void gradeSubmission_shouldReturnGrade() throws Exception {
        GradeDTO dto = new GradeDTO();
        dto.setId(1L);
        dto.setSubmissionId(1L);
        dto.setGrade(GradeLevel.A);

        when(gradeService.gradeSubmission(any(CreateGradeRequest.class), eq(5L))).thenReturn(dto);

        CreateGradeRequest request = new CreateGradeRequest();
        request.setSubmissionId(1L);
        request.setGrade(GradeLevel.A);
        request.setComment("优秀");

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.grade").value("A"));
    }

    @Test
    @DisplayName("更新评分")
    void updateGrade_shouldReturnUpdatedGrade() throws Exception {
        GradeDTO dto = new GradeDTO();
        dto.setId(1L);
        dto.setGrade(GradeLevel.B);

        when(gradeService.updateGrade(eq(1L), any(UpdateGradeRequest.class), eq(5L))).thenReturn(dto);

        UpdateGradeRequest request = new UpdateGradeRequest();
        request.setGrade(GradeLevel.B);
        request.setComment("修正评分");

        mockMvc.perform(put("/api/grades/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.grade").value("B"));
    }

    @Test
    @DisplayName("获取提交的评分")
    void getGradeBySubmission_shouldReturnGrade() throws Exception {
        GradeDTO dto = new GradeDTO();
        dto.setId(1L);
        dto.setGrade(GradeLevel.A);

        when(gradeService.getGradeBySubmission(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/grades/submission/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.grade").value("A"));
    }

    @Test
    @DisplayName("获取课程的评分列表")
    void getGradesByLesson_shouldReturnList() throws Exception {
        GradeDTO dto1 = new GradeDTO();
        dto1.setId(1L);
        dto1.setGrade(GradeLevel.A);

        GradeDTO dto2 = new GradeDTO();
        dto2.setId(2L);
        dto2.setGrade(GradeLevel.B);

        when(gradeService.getGradesByLesson(1L)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/grades/lesson/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2));
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=GradeControllerTest -q
```

**Step 3: 创建 GradeController**

```java
// backend/src/main/java/com/teaching/controller/GradeController.java
package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.CreateGradeRequest;
import com.teaching.dto.GradeDTO;
import com.teaching.dto.UpdateGradeRequest;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    /**
     * 对提交进行评分
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<GradeDTO> gradeSubmission(
            @Valid @RequestBody CreateGradeRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        GradeDTO result = gradeService.gradeSubmission(request, user.getId());
        return Result.success(result);
    }

    /**
     * 更新评分
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<GradeDTO> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGradeRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        GradeDTO result = gradeService.updateGrade(id, request, user.getId());
        return Result.success(result);
    }

    /**
     * 获取提交的评分
     */
    @GetMapping("/submission/{submissionId}")
    public Result<GradeDTO> getGradeBySubmission(@PathVariable Long submissionId) {
        GradeDTO grade = gradeService.getGradeBySubmission(submissionId);
        return Result.success(grade);
    }

    /**
     * 获取课程的所有评分
     */
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<List<GradeDTO>> getGradesByLesson(@PathVariable Long lessonId) {
        List<GradeDTO> grades = gradeService.getGradesByLesson(lessonId);
        return Result.success(grades);
    }

    /**
     * 获取学科的所有评分
     */
    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<List<GradeDTO>> getGradesBySubject(@PathVariable Long subjectId) {
        List<GradeDTO> grades = gradeService.getGradesBySubject(subjectId);
        return Result.success(grades);
    }

    /**
     * 获取学员的成绩统计
     */
    @GetMapping("/stats/student/{studentId}/subject/{subjectId}")
    public Result<Map<String, Object>> getStudentGradeStats(
            @PathVariable Long studentId,
            @PathVariable Long subjectId) {
        Map<String, Object> stats = gradeService.getStudentGradeStats(studentId, subjectId);
        return Result.success(stats);
    }

    /**
     * 删除评分
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return Result.success();
    }
}
```

**Step 4: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=GradeControllerTest -q
```

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add GradeController"
```

---

## Task 5: 前端评分功能

**Files:**
- Create: `frontend/src/api/grade.ts`
- Create: `frontend/src/views/grades/GradeForm.vue`
- Create: `frontend/src/views/grades/GradeDisplay.vue`
- Update: `frontend/src/views/submissions/SubmissionViewer.vue`

**Step 1: 创建评分 API**

```typescript
// frontend/src/api/grade.ts
import request from '@/utils/request'

export type GradeLevel = 'A' | 'B' | 'C' | 'D'

export interface GradeDTO {
  id: number
  submissionId: number
  grade: GradeLevel
  gradeDescription: string
  comment?: string
  graderId: number
  graderName?: string
  gradeTime: string
  lessonId?: number
  lessonTitle?: string
  submitterId?: number
  submitterName?: string
  groupId?: number
  groupName?: string
}

export interface CreateGradeRequest {
  submissionId: number
  grade: GradeLevel
  comment?: string
}

export interface UpdateGradeRequest {
  grade: GradeLevel
  comment?: string
}

// 评分
export function gradeSubmission(data: CreateGradeRequest) {
  return request.post<GradeDTO>('/grades', data)
}

// 更新评分
export function updateGrade(id: number, data: UpdateGradeRequest) {
  return request.put<GradeDTO>(`/grades/${id}`, data)
}

// 获取提交的评分
export function getGradeBySubmission(submissionId: number) {
  return request.get<GradeDTO>(`/grades/submission/${submissionId}`)
}

// 获取课程的评分列表
export function getGradesByLesson(lessonId: number) {
  return request.get<GradeDTO[]>(`/grades/lesson/${lessonId}`)
}

// 获取学科的评分列表
export function getGradesBySubject(subjectId: number) {
  return request.get<GradeDTO[]>(`/grades/subject/${subjectId}`)
}

// 获取学员的成绩统计
export function getStudentGradeStats(studentId: number, subjectId: number) {
  return request.get<Record<string, unknown>>(`/grades/stats/student/${studentId}/subject/${subjectId}`)
}

// 删除评分
export function deleteGrade(id: number) {
  return request.delete(`/grades/${id}`)
}

// 工具函数：获取评分颜色
export function getGradeColor(grade: GradeLevel): string {
  const colors: Record<GradeLevel, string> = {
    'A': '#67c23a',
    'B': '#409eff',
    'C': '#e6a23c',
    'D': '#f56c6c'
  }
  return colors[grade]
}

// 工具函数：获取评分类型
export function getGradeType(grade: GradeLevel): 'success' | 'primary' | 'warning' | 'danger' {
  const types: Record<GradeLevel, 'success' | 'primary' | 'warning' | 'danger'> = {
    'A': 'success',
    'B': 'primary',
    'C': 'warning',
    'D': 'danger'
  }
  return types[grade]
}
```

**Step 2: 创建评分表单组件**

```vue
<!-- frontend/src/views/grades/GradeForm.vue -->
<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '修改评分' : '作业评分'"
    width="500px"
    @closed="resetForm"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="评分等级" prop="grade">
        <el-radio-group v-model="form.grade" size="large">
          <el-radio-button
            v-for="level in gradeLevels"
            :key="level.value"
            :value="level.value"
          >
            <span :style="{ color: level.color }">
              {{ level.value }} - {{ level.label }}
            </span>
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="评语" prop="comment">
        <el-input
          v-model="form.comment"
          type="textarea"
          :rows="4"
          placeholder="请输入评语（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 快捷评语 -->
      <el-form-item label="快捷评语">
        <div class="quick-comments">
          <el-tag
            v-for="(comment, index) in quickComments"
            :key="index"
            class="quick-comment"
            @click="appendComment(comment)"
          >
            {{ comment }}
          </el-tag>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ isEdit ? '更新' : '提交' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { gradeSubmission, updateGrade, getGradeColor } from '@/api/grade'
import type { GradeDTO, GradeLevel } from '@/api/grade'

interface Props {
  modelValue: boolean
  submissionId: number
  existingGrade?: GradeDTO | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success', grade: GradeDTO): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => !!props.existingGrade)

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  grade: '' as GradeLevel | '',
  comment: ''
})

const rules: FormRules = {
  grade: [{ required: true, message: '请选择评分等级', trigger: 'change' }]
}

const gradeLevels = [
  { value: 'A' as GradeLevel, label: '优秀', color: getGradeColor('A') },
  { value: 'B' as GradeLevel, label: '良好', color: getGradeColor('B') },
  { value: 'C' as GradeLevel, label: '及格', color: getGradeColor('C') },
  { value: 'D' as GradeLevel, label: '不及格', color: getGradeColor('D') }
]

const quickComments = [
  '代码结构清晰',
  '命名规范',
  '逻辑正确',
  '需要添加注释',
  '代码可以优化',
  '有语法错误',
  '功能未完整实现',
  '未按要求完成'
]

watch(() => props.existingGrade, (grade) => {
  if (grade) {
    form.grade = grade.grade
    form.comment = grade.comment || ''
  }
}, { immediate: true })

function appendComment(comment: string) {
  if (form.comment) {
    form.comment += '；' + comment
  } else {
    form.comment = comment
  }
}

function resetForm() {
  form.grade = ''
  form.comment = ''
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    let result: GradeDTO

    if (isEdit.value && props.existingGrade) {
      const res = await updateGrade(props.existingGrade.id, {
        grade: form.grade as GradeLevel,
        comment: form.comment || undefined
      })
      result = res.data
      ElMessage.success('评分已更新')
    } else {
      const res = await gradeSubmission({
        submissionId: props.submissionId,
        grade: form.grade as GradeLevel,
        comment: form.comment || undefined
      })
      result = res.data
      ElMessage.success('评分成功')
    }

    visible.value = false
    emit('success', result)
  } catch (error: any) {
    ElMessage.error(error.message || '评分失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.quick-comments {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-comment {
  cursor: pointer;
}

.quick-comment:hover {
  opacity: 0.8;
}
</style>
```

**Step 3: 创建评分展示组件**

```vue
<!-- frontend/src/views/grades/GradeDisplay.vue -->
<template>
  <div class="grade-display">
    <div v-if="grade" class="grade-info">
      <div class="grade-badge">
        <el-tag :type="getGradeType(grade.grade)" size="large" effect="dark">
          {{ grade.grade }}
        </el-tag>
        <span class="grade-desc">{{ grade.gradeDescription }}</span>
      </div>

      <div v-if="grade.comment" class="grade-comment">
        <div class="comment-label">评语：</div>
        <div class="comment-content">{{ grade.comment }}</div>
      </div>

      <div class="grade-meta">
        <span>评分人：{{ grade.graderName }}</span>
        <span>评分时间：{{ formatTime(grade.gradeTime) }}</span>
      </div>

      <div v-if="canEdit" class="grade-actions">
        <el-button size="small" @click="emit('edit')">修改评分</el-button>
      </div>
    </div>

    <div v-else class="no-grade">
      <el-empty description="暂未评分" :image-size="60">
        <el-button v-if="canGrade" type="primary" @click="emit('grade')">
          立即评分
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getGradeType } from '@/api/grade'
import type { GradeDTO } from '@/api/grade'

interface Props {
  grade?: GradeDTO | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'grade'): void
  (e: 'edit'): void
}>()

const userStore = useUserStore()

const canGrade = computed(() => userStore.isTeacher || userStore.isAdmin)
const canEdit = computed(() => canGrade.value && props.grade)

function formatTime(time: string): string {
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.grade-display {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.grade-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.grade-badge {
  display: flex;
  align-items: center;
  gap: 12px;
}

.grade-desc {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.grade-comment {
  padding: 12px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.comment-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.grade-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.grade-actions {
  margin-top: 8px;
}

.no-grade {
  text-align: center;
  padding: 20px;
}
</style>
```

**Step 4: Commit**

```bash
git add frontend/src/
git commit -m "feat: add grade form and display components"
```

---

## 验证清单

1. **运行后端测试**
   ```bash
   cd backend && mvn test -Dtest=*Grade* -q
   ```

2. **功能验证**
   - 教员对作业评分（A/B/C/D）
   - 添加评语
   - 使用快捷评语
   - 修改已有评分
   - 学员查看自己的评分
   - 按课程/学科查看评分列表

下一步：继续 `03-grade-summary.md` 完成成绩汇总功能。
