# 阶段三：成绩汇总与导出

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现成绩汇总看板、学员/小组成绩列表、Excel导出功能。

**Architecture:** RESTful API，支持按学科/课程汇总，Apache POI 生成 Excel。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Apache POI, Vue 3, Element Plus

**依赖:** 需要先完成 `02-grading.md`

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Grade Summary Page | niQ1I | 统计卡片，成绩列表，导出按钮 |

### 设计规范

- **统计卡片**: 总人数、各等级分布、平均分
- **成绩列表**: 表格形式，支持排序和筛选
- **成绩分布图**: 柱状图或饼图展示
- **导出按钮**: Excel 图标，下载提示
- **学员详情**: 点击展开查看所有课程成绩

---

## Task 1: 添加 Apache POI 依赖

**Files:**
- Update: `backend/pom.xml`

**Step 1: 添加依赖**

```xml
<!-- 在 backend/pom.xml 的 dependencies 中添加 -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

**Step 2: 刷新依赖**

```bash
cd backend && mvn dependency:resolve -q
```

**Step 3: Commit**

```bash
git add backend/pom.xml
git commit -m "feat: add Apache POI dependency for Excel export"
```

---

## Task 2: 创建成绩汇总 DTO

**Files:**
- Create: `backend/src/main/java/com/teaching/dto/GradeSummaryDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/StudentGradeSummaryDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/LessonGradeSummaryDTO.java`

**Step 1: 创建 GradeSummaryDTO（学科级汇总）**

```java
// backend/src/main/java/com/teaching/dto/GradeSummaryDTO.java
package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import lombok.Data;

import java.util.Map;

@Data
public class GradeSummaryDTO {

    private Long subjectId;
    private String subjectName;

    private Integer totalLessons;       // 总课程数
    private Integer totalSubmissions;   // 总提交数
    private Integer totalGraded;        // 已评分数
    private Integer pendingGrade;       // 待评分数

    // 各等级统计
    private Map<GradeLevel, Integer> gradeCounts;

    // 平均分（按 A=95,B=82,C=70,D=40 换算）
    private Double averageScore;
    private GradeLevel averageGrade;

    // 提交率
    private Double submissionRate;

    // 通过率（C及以上）
    private Double passRate;
}
```

**Step 2: 创建 StudentGradeSummaryDTO（学员成绩汇总）**

```java
// backend/src/main/java/com/teaching/dto/StudentGradeSummaryDTO.java
package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StudentGradeSummaryDTO {

    private Long studentId;
    private String studentName;
    private Long groupId;
    private String groupName;

    private Integer totalLessons;       // 应提交数
    private Integer submitted;          // 已提交数
    private Integer graded;             // 已评分数

    // 各等级统计
    private Map<GradeLevel, Integer> gradeCounts;

    private Double averageScore;
    private GradeLevel averageGrade;

    // 详细成绩列表
    private List<LessonGradeDTO> lessonGrades;

    @Data
    public static class LessonGradeDTO {
        private Long lessonId;
        private String lessonTitle;
        private Boolean submitted;
        private GradeLevel grade;
        private String comment;
    }
}
```

**Step 3: 创建 LessonGradeSummaryDTO（课程评分汇总）**

```java
// backend/src/main/java/com/teaching/dto/LessonGradeSummaryDTO.java
package com.teaching.dto;

import com.teaching.enums.GradeLevel;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LessonGradeSummaryDTO {

    private Long lessonId;
    private String lessonTitle;

    private Integer totalStudents;      // 应提交人数
    private Integer submitted;          // 已提交数
    private Integer graded;             // 已评分数

    // 各等级统计
    private Map<GradeLevel, Integer> gradeCounts;

    private Double submissionRate;
    private Double passRate;

    // 提交详情列表
    private List<SubmissionGradeDTO> submissions;

    @Data
    public static class SubmissionGradeDTO {
        private Long submissionId;
        private Long submitterId;
        private String submitterName;
        private Long groupId;
        private String groupName;
        private String submitTime;
        private Boolean isLate;
        private GradeLevel grade;
        private String comment;
    }
}
```

**Step 4: Commit**

```bash
git add backend/src/
git commit -m "feat: add grade summary DTOs"
```

---

## Task 3: 创建成绩汇总 Service

**Files:**
- Create: `backend/src/main/java/com/teaching/service/GradeSummaryService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/GradeSummaryServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/service/GradeSummaryServiceTest.java`

**Step 1: 编写测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/GradeSummaryServiceTest.java
package com.teaching.service;

import com.teaching.dto.GradeSummaryDTO;
import com.teaching.dto.LessonGradeSummaryDTO;
import com.teaching.dto.StudentGradeSummaryDTO;
import com.teaching.entity.*;
import com.teaching.enums.GradeLevel;
import com.teaching.mapper.*;
import com.teaching.service.impl.GradeSummaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeSummaryServiceTest {

    @Mock
    private SubjectMapper subjectMapper;
    @Mock
    private LessonMapper lessonMapper;
    @Mock
    private SubmissionMapper submissionMapper;
    @Mock
    private GradeMapper gradeMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private GroupMapper groupMapper;
    @Mock
    private SubjectStudentMapper subjectStudentMapper;

    private GradeSummaryServiceImpl gradeSummaryService;

    @BeforeEach
    void setUp() {
        gradeSummaryService = new GradeSummaryServiceImpl(
            subjectMapper, lessonMapper, submissionMapper,
            gradeMapper, userMapper, groupMapper, subjectStudentMapper
        );
    }

    @Test
    @DisplayName("获取学科成绩汇总")
    void getSubjectSummary_shouldReturnSummary() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Java基础");

        Lesson lesson1 = new Lesson();
        lesson1.setId(1L);
        Lesson lesson2 = new Lesson();
        lesson2.setId(2L);

        Grade g1 = new Grade();
        g1.setGrade(GradeLevel.A);
        Grade g2 = new Grade();
        g2.setGrade(GradeLevel.B);

        when(subjectMapper.selectById(1L)).thenReturn(subject);
        when(lessonMapper.selectBySubjectId(1L)).thenReturn(List.of(lesson1, lesson2));
        when(gradeMapper.selectBySubjectId(1L)).thenReturn(List.of(g1, g2));

        GradeSummaryDTO result = gradeSummaryService.getSubjectSummary(1L);

        assertNotNull(result);
        assertEquals("Java基础", result.getSubjectName());
        assertEquals(2, result.getTotalLessons());
        assertEquals(2, result.getTotalGraded());
    }

    @Test
    @DisplayName("获取课程评分看板")
    void getLessonSummary_shouldReturnSummary() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("第一课");
        lesson.setSubjectId(1L);

        Submission s1 = new Submission();
        s1.setId(1L);
        s1.setLessonId(1L);
        s1.setSubmitterId(10L);

        Grade g1 = new Grade();
        g1.setSubmissionId(1L);
        g1.setGrade(GradeLevel.A);

        User student = new User();
        student.setId(10L);
        student.setName("学员1");

        when(lessonMapper.selectById(1L)).thenReturn(lesson);
        when(submissionMapper.selectByLessonId(1L)).thenReturn(List.of(s1));
        when(gradeMapper.selectBySubmissionId(1L)).thenReturn(g1);
        when(userMapper.selectById(10L)).thenReturn(student);

        LessonGradeSummaryDTO result = gradeSummaryService.getLessonSummary(1L);

        assertNotNull(result);
        assertEquals("第一课", result.getLessonTitle());
        assertEquals(1, result.getSubmitted());
        assertEquals(1, result.getGraded());
    }

    @Test
    @DisplayName("获取学员成绩汇总")
    void getStudentSummary_shouldReturnSummary() {
        User student = new User();
        student.setId(10L);
        student.setName("学员1");

        Lesson lesson1 = new Lesson();
        lesson1.setId(1L);
        lesson1.setTitle("第一课");

        Submission s1 = new Submission();
        s1.setId(1L);
        s1.setLessonId(1L);
        s1.setSubmitterId(10L);

        Grade g1 = new Grade();
        g1.setSubmissionId(1L);
        g1.setGrade(GradeLevel.A);
        g1.setComment("优秀");

        when(userMapper.selectById(10L)).thenReturn(student);
        when(lessonMapper.selectBySubjectId(1L)).thenReturn(List.of(lesson1));
        when(submissionMapper.selectByLessonIdAndSubmitterId(1L, 10L)).thenReturn(s1);
        when(gradeMapper.selectBySubmissionId(1L)).thenReturn(g1);

        StudentGradeSummaryDTO result = gradeSummaryService.getStudentSummary(10L, 1L);

        assertNotNull(result);
        assertEquals("学员1", result.getStudentName());
        assertEquals(1, result.getSubmitted());
        assertEquals(1, result.getGraded());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=GradeSummaryServiceTest -q
```

**Step 3: 创建 GradeSummaryService 接口**

```java
// backend/src/main/java/com/teaching/service/GradeSummaryService.java
package com.teaching.service;

import com.teaching.dto.GradeSummaryDTO;
import com.teaching.dto.LessonGradeSummaryDTO;
import com.teaching.dto.StudentGradeSummaryDTO;

import java.util.List;

public interface GradeSummaryService {

    /**
     * 获取学科成绩汇总
     */
    GradeSummaryDTO getSubjectSummary(Long subjectId);

    /**
     * 获取课程评分看板
     */
    LessonGradeSummaryDTO getLessonSummary(Long lessonId);

    /**
     * 获取学员在某学科的成绩汇总
     */
    StudentGradeSummaryDTO getStudentSummary(Long studentId, Long subjectId);

    /**
     * 获取学科内所有学员的成绩列表
     */
    List<StudentGradeSummaryDTO> getAllStudentsSummary(Long subjectId);

    /**
     * 导出学科成绩到 Excel
     */
    byte[] exportSubjectGrades(Long subjectId);

    /**
     * 导出课程成绩到 Excel
     */
    byte[] exportLessonGrades(Long lessonId);
}
```

**Step 4: 创建 GradeSummaryServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/GradeSummaryServiceImpl.java
package com.teaching.service.impl;

import com.teaching.dto.GradeSummaryDTO;
import com.teaching.dto.LessonGradeSummaryDTO;
import com.teaching.dto.StudentGradeSummaryDTO;
import com.teaching.entity.*;
import com.teaching.enums.GradeLevel;
import com.teaching.mapper.*;
import com.teaching.service.GradeSummaryService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeSummaryServiceImpl implements GradeSummaryService {

    private final SubjectMapper subjectMapper;
    private final LessonMapper lessonMapper;
    private final SubmissionMapper submissionMapper;
    private final GradeMapper gradeMapper;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final SubjectStudentMapper subjectStudentMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public GradeSummaryDTO getSubjectSummary(Long subjectId) {
        Subject subject = subjectMapper.selectById(subjectId);
        List<Lesson> lessons = lessonMapper.selectBySubjectId(subjectId);
        List<Grade> grades = gradeMapper.selectBySubjectId(subjectId);

        GradeSummaryDTO dto = new GradeSummaryDTO();
        dto.setSubjectId(subjectId);
        dto.setSubjectName(subject.getName());
        dto.setTotalLessons(lessons.size());
        dto.setTotalGraded(grades.size());

        // 统计各等级数量
        Map<GradeLevel, Integer> gradeCounts = grades.stream()
                .collect(Collectors.groupingBy(
                        Grade::getGrade,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        dto.setGradeCounts(gradeCounts);

        // 计算平均分
        if (!grades.isEmpty()) {
            double avgScore = grades.stream()
                    .mapToInt(g -> gradeToScore(g.getGrade()))
                    .average()
                    .orElse(0);
            dto.setAverageScore(avgScore);
            dto.setAverageGrade(GradeLevel.fromScore((int) avgScore));

            // 计算通过率
            long passCount = grades.stream()
                    .filter(g -> g.getGrade() != GradeLevel.D)
                    .count();
            dto.setPassRate((double) passCount / grades.size() * 100);
        }

        return dto;
    }

    @Override
    public LessonGradeSummaryDTO getLessonSummary(Long lessonId) {
        Lesson lesson = lessonMapper.selectById(lessonId);
        List<Submission> submissions = submissionMapper.selectByLessonId(lessonId);

        LessonGradeSummaryDTO dto = new LessonGradeSummaryDTO();
        dto.setLessonId(lessonId);
        dto.setLessonTitle(lesson.getTitle());
        dto.setSubmitted(submissions.size());

        List<LessonGradeSummaryDTO.SubmissionGradeDTO> submissionGrades = new ArrayList<>();
        Map<GradeLevel, Integer> gradeCounts = new HashMap<>();
        int gradedCount = 0;

        for (Submission submission : submissions) {
            LessonGradeSummaryDTO.SubmissionGradeDTO subDto = new LessonGradeSummaryDTO.SubmissionGradeDTO();
            subDto.setSubmissionId(submission.getId());
            subDto.setSubmitterId(submission.getSubmitterId());

            User submitter = userMapper.selectById(submission.getSubmitterId());
            if (submitter != null) {
                subDto.setSubmitterName(submitter.getName());
            }

            if (submission.getGroupId() != null) {
                subDto.setGroupId(submission.getGroupId());
                Group group = groupMapper.selectById(submission.getGroupId());
                if (group != null) {
                    subDto.setGroupName(group.getName());
                }
            }

            subDto.setSubmitTime(submission.getSubmitTime().format(DATE_FORMATTER));

            if (lesson.getDeadline() != null) {
                subDto.setIsLate(submission.getSubmitTime().isAfter(lesson.getDeadline()));
            }

            Grade grade = gradeMapper.selectBySubmissionId(submission.getId());
            if (grade != null) {
                subDto.setGrade(grade.getGrade());
                subDto.setComment(grade.getComment());
                gradedCount++;
                gradeCounts.merge(grade.getGrade(), 1, Integer::sum);
            }

            submissionGrades.add(subDto);
        }

        dto.setGraded(gradedCount);
        dto.setGradeCounts(gradeCounts);
        dto.setSubmissions(submissionGrades);

        // 计算通过率
        if (gradedCount > 0) {
            long passCount = submissionGrades.stream()
                    .filter(s -> s.getGrade() != null && s.getGrade() != GradeLevel.D)
                    .count();
            dto.setPassRate((double) passCount / gradedCount * 100);
        }

        return dto;
    }

    @Override
    public StudentGradeSummaryDTO getStudentSummary(Long studentId, Long subjectId) {
        User student = userMapper.selectById(studentId);
        List<Lesson> lessons = lessonMapper.selectBySubjectId(subjectId);

        StudentGradeSummaryDTO dto = new StudentGradeSummaryDTO();
        dto.setStudentId(studentId);
        dto.setStudentName(student.getName());
        dto.setTotalLessons(lessons.size());

        List<StudentGradeSummaryDTO.LessonGradeDTO> lessonGrades = new ArrayList<>();
        Map<GradeLevel, Integer> gradeCounts = new HashMap<>();
        int submitted = 0;
        int graded = 0;

        for (Lesson lesson : lessons) {
            StudentGradeSummaryDTO.LessonGradeDTO lgDto = new StudentGradeSummaryDTO.LessonGradeDTO();
            lgDto.setLessonId(lesson.getId());
            lgDto.setLessonTitle(lesson.getTitle());

            Submission submission = submissionMapper.selectByLessonIdAndSubmitterId(lesson.getId(), studentId);
            if (submission != null) {
                lgDto.setSubmitted(true);
                submitted++;

                Grade grade = gradeMapper.selectBySubmissionId(submission.getId());
                if (grade != null) {
                    lgDto.setGrade(grade.getGrade());
                    lgDto.setComment(grade.getComment());
                    graded++;
                    gradeCounts.merge(grade.getGrade(), 1, Integer::sum);
                }
            } else {
                lgDto.setSubmitted(false);
            }

            lessonGrades.add(lgDto);
        }

        dto.setSubmitted(submitted);
        dto.setGraded(graded);
        dto.setGradeCounts(gradeCounts);
        dto.setLessonGrades(lessonGrades);

        // 计算平均分
        if (graded > 0) {
            double avgScore = lessonGrades.stream()
                    .filter(lg -> lg.getGrade() != null)
                    .mapToInt(lg -> gradeToScore(lg.getGrade()))
                    .average()
                    .orElse(0);
            dto.setAverageScore(avgScore);
            dto.setAverageGrade(GradeLevel.fromScore((int) avgScore));
        }

        return dto;
    }

    @Override
    public List<StudentGradeSummaryDTO> getAllStudentsSummary(Long subjectId) {
        List<Long> studentIds = subjectStudentMapper.selectStudentIdsBySubjectId(subjectId);
        return studentIds.stream()
                .map(id -> getStudentSummary(id, subjectId))
                .toList();
    }

    @Override
    public byte[] exportSubjectGrades(Long subjectId) {
        Subject subject = subjectMapper.selectById(subjectId);
        List<Lesson> lessons = lessonMapper.selectBySubjectId(subjectId);
        List<StudentGradeSummaryDTO> students = getAllStudentsSummary(subjectId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("成绩汇总");

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            int colIndex = 0;
            createCell(headerRow, colIndex++, "学员姓名", headerStyle);

            for (Lesson lesson : lessons) {
                createCell(headerRow, colIndex++, lesson.getTitle(), headerStyle);
            }
            createCell(headerRow, colIndex++, "平均成绩", headerStyle);

            // 填充数据
            int rowIndex = 1;
            for (StudentGradeSummaryDTO student : students) {
                Row row = sheet.createRow(rowIndex++);
                colIndex = 0;
                row.createCell(colIndex++).setCellValue(student.getStudentName());

                for (StudentGradeSummaryDTO.LessonGradeDTO lg : student.getLessonGrades()) {
                    String value = "";
                    if (lg.getSubmitted()) {
                        value = lg.getGrade() != null ? lg.getGrade().name() : "待评分";
                    } else {
                        value = "未提交";
                    }
                    row.createCell(colIndex++).setCellValue(value);
                }

                row.createCell(colIndex).setCellValue(
                    student.getAverageGrade() != null ? student.getAverageGrade().name() : "-"
                );
            }

            // 自动调整列宽
            for (int i = 0; i <= lessons.size() + 1; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("生成Excel失败", e);
        }
    }

    @Override
    public byte[] exportLessonGrades(Long lessonId) {
        LessonGradeSummaryDTO summary = getLessonSummary(lessonId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("课程成绩");

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "学员/小组", headerStyle);
            createCell(headerRow, 1, "提交时间", headerStyle);
            createCell(headerRow, 2, "是否迟交", headerStyle);
            createCell(headerRow, 3, "成绩", headerStyle);
            createCell(headerRow, 4, "评语", headerStyle);

            // 填充数据
            int rowIndex = 1;
            for (LessonGradeSummaryDTO.SubmissionGradeDTO sub : summary.getSubmissions()) {
                Row row = sheet.createRow(rowIndex++);
                String name = sub.getGroupName() != null ? sub.getGroupName() : sub.getSubmitterName();
                row.createCell(0).setCellValue(name);
                row.createCell(1).setCellValue(sub.getSubmitTime());
                row.createCell(2).setCellValue(Boolean.TRUE.equals(sub.getIsLate()) ? "是" : "否");
                row.createCell(3).setCellValue(sub.getGrade() != null ? sub.getGrade().name() : "待评分");
                row.createCell(4).setCellValue(sub.getComment() != null ? sub.getComment() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("生成Excel失败", e);
        }
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private int gradeToScore(GradeLevel grade) {
        return switch (grade) {
            case A -> 95;
            case B -> 82;
            case C -> 70;
            case D -> 40;
        };
    }
}
```

**Step 5: 添加 SubjectStudentMapper 方法**

```java
// 在 backend/src/main/java/com/teaching/mapper/SubjectStudentMapper.java 中添加
@Select("SELECT student_id FROM t_subject_student WHERE subject_id = #{subjectId} AND del_flag = 0")
List<Long> selectStudentIdsBySubjectId(Long subjectId);
```

**Step 6: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=GradeSummaryServiceTest -q
```

**Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add GradeSummaryService with Excel export"
```

---

## Task 4: 创建成绩汇总 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/GradeSummaryController.java`

**Step 1: 创建 GradeSummaryController**

```java
// backend/src/main/java/com/teaching/controller/GradeSummaryController.java
package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.GradeSummaryDTO;
import com.teaching.dto.LessonGradeSummaryDTO;
import com.teaching.dto.StudentGradeSummaryDTO;
import com.teaching.security.UserDetailsImpl;
import com.teaching.service.GradeSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/grade-summary")
@RequiredArgsConstructor
public class GradeSummaryController {

    private final GradeSummaryService gradeSummaryService;

    /**
     * 获取学科成绩汇总
     */
    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<GradeSummaryDTO> getSubjectSummary(@PathVariable Long subjectId) {
        GradeSummaryDTO summary = gradeSummaryService.getSubjectSummary(subjectId);
        return Result.success(summary);
    }

    /**
     * 获取课程评分看板
     */
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<LessonGradeSummaryDTO> getLessonSummary(@PathVariable Long lessonId) {
        LessonGradeSummaryDTO summary = gradeSummaryService.getLessonSummary(lessonId);
        return Result.success(summary);
    }

    /**
     * 获取我的成绩汇总（学员用）
     */
    @GetMapping("/my/{subjectId}")
    public Result<StudentGradeSummaryDTO> getMyGradeSummary(
            @PathVariable Long subjectId,
            @AuthenticationPrincipal UserDetailsImpl user) {
        StudentGradeSummaryDTO summary = gradeSummaryService.getStudentSummary(user.getId(), subjectId);
        return Result.success(summary);
    }

    /**
     * 获取学员成绩汇总
     */
    @GetMapping("/student/{studentId}/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<StudentGradeSummaryDTO> getStudentSummary(
            @PathVariable Long studentId,
            @PathVariable Long subjectId) {
        StudentGradeSummaryDTO summary = gradeSummaryService.getStudentSummary(studentId, subjectId);
        return Result.success(summary);
    }

    /**
     * 获取学科内所有学员的成绩列表
     */
    @GetMapping("/subject/{subjectId}/students")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public Result<List<StudentGradeSummaryDTO>> getAllStudentsSummary(@PathVariable Long subjectId) {
        List<StudentGradeSummaryDTO> summaries = gradeSummaryService.getAllStudentsSummary(subjectId);
        return Result.success(summaries);
    }

    /**
     * 导出学科成绩 Excel
     */
    @GetMapping("/subject/{subjectId}/export")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<byte[]> exportSubjectGrades(@PathVariable Long subjectId) {
        byte[] data = gradeSummaryService.exportSubjectGrades(subjectId);
        String filename = URLEncoder.encode("学科成绩汇总.xlsx", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    /**
     * 导出课程成绩 Excel
     */
    @GetMapping("/lesson/{lessonId}/export")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<byte[]> exportLessonGrades(@PathVariable Long lessonId) {
        byte[] data = gradeSummaryService.exportLessonGrades(lessonId);
        String filename = URLEncoder.encode("课程成绩.xlsx", StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
```

**Step 2: Commit**

```bash
git add backend/src/
git commit -m "feat: add GradeSummaryController with Excel export endpoints"
```

---

## Task 5: 前端成绩汇总页面

**Files:**
- Create: `frontend/src/api/gradeSummary.ts`
- Create: `frontend/src/views/grades/GradeSummary.vue`
- Create: `frontend/src/views/grades/StudentGrades.vue`
- Create: `frontend/src/views/grades/LessonGradeBoard.vue`

**Step 1: 创建 API 模块**

```typescript
// frontend/src/api/gradeSummary.ts
import request from '@/utils/request'
import type { GradeLevel } from './grade'

export interface GradeSummaryDTO {
  subjectId: number
  subjectName: string
  totalLessons: number
  totalSubmissions: number
  totalGraded: number
  pendingGrade: number
  gradeCounts: Record<GradeLevel, number>
  averageScore: number
  averageGrade: GradeLevel
  submissionRate: number
  passRate: number
}

export interface StudentGradeSummaryDTO {
  studentId: number
  studentName: string
  groupId?: number
  groupName?: string
  totalLessons: number
  submitted: number
  graded: number
  gradeCounts: Record<GradeLevel, number>
  averageScore: number
  averageGrade: GradeLevel
  lessonGrades: LessonGradeDTO[]
}

export interface LessonGradeDTO {
  lessonId: number
  lessonTitle: string
  submitted: boolean
  grade?: GradeLevel
  comment?: string
}

export interface LessonGradeSummaryDTO {
  lessonId: number
  lessonTitle: string
  totalStudents: number
  submitted: number
  graded: number
  gradeCounts: Record<GradeLevel, number>
  submissionRate: number
  passRate: number
  submissions: SubmissionGradeDTO[]
}

export interface SubmissionGradeDTO {
  submissionId: number
  submitterId: number
  submitterName: string
  groupId?: number
  groupName?: string
  submitTime: string
  isLate: boolean
  grade?: GradeLevel
  comment?: string
}

// 获取学科成绩汇总
export function getSubjectSummary(subjectId: number) {
  return request.get<GradeSummaryDTO>(`/grade-summary/subject/${subjectId}`)
}

// 获取课程评分看板
export function getLessonSummary(lessonId: number) {
  return request.get<LessonGradeSummaryDTO>(`/grade-summary/lesson/${lessonId}`)
}

// 获取我的成绩汇总
export function getMyGradeSummary(subjectId: number) {
  return request.get<StudentGradeSummaryDTO>(`/grade-summary/my/${subjectId}`)
}

// 获取学员成绩汇总
export function getStudentSummary(studentId: number, subjectId: number) {
  return request.get<StudentGradeSummaryDTO>(`/grade-summary/student/${studentId}/subject/${subjectId}`)
}

// 获取学科内所有学员成绩列表
export function getAllStudentsSummary(subjectId: number) {
  return request.get<StudentGradeSummaryDTO[]>(`/grade-summary/subject/${subjectId}/students`)
}

// 导出学科成绩 Excel
export function exportSubjectGrades(subjectId: number) {
  return request.get(`/grade-summary/subject/${subjectId}/export`, {
    responseType: 'blob'
  })
}

// 导出课程成绩 Excel
export function exportLessonGrades(lessonId: number) {
  return request.get(`/grade-summary/lesson/${lessonId}/export`, {
    responseType: 'blob'
  })
}

// 下载文件辅助函数
export function downloadFile(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
```

**Step 2: 创建成绩汇总页面**

```vue
<!-- frontend/src/views/grades/GradeSummary.vue -->
<template>
  <div class="grade-summary">
    <el-page-header @back="router.back()">
      <template #content>
        <span>{{ summary?.subjectName }} - 成绩汇总</span>
      </template>
      <template #extra>
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出Excel
        </el-button>
      </template>
    </el-page-header>

    <div v-if="summary" class="summary-content">
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stat-cards">
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="总课程数" :value="summary.totalLessons" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="已评分" :value="summary.totalGraded" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="平均成绩" :value="summary.averageGrade || '-'" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="通过率" :value="summary.passRate?.toFixed(1) + '%'" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 成绩分布 -->
      <el-card class="grade-distribution">
        <template #header>成绩分布</template>
        <el-row :gutter="20">
          <el-col v-for="level in gradeLevels" :key="level" :span="6">
            <div class="grade-item">
              <el-tag :type="getGradeType(level)" size="large">
                {{ level }}
              </el-tag>
              <span class="count">{{ summary.gradeCounts?.[level] || 0 }} 人</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 学员成绩列表 -->
      <el-card>
        <template #header>学员成绩</template>
        <el-table :data="students" stripe>
          <el-table-column prop="studentName" label="学员姓名" width="120" />
          <el-table-column prop="groupName" label="所属小组" width="120" />
          <el-table-column prop="submitted" label="提交次数" width="100" align="center" />
          <el-table-column prop="graded" label="已评分" width="100" align="center" />
          <el-table-column label="平均成绩" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.averageGrade" :type="getGradeType(row.averageGrade)">
                {{ row.averageGrade }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button size="small" link @click="viewStudentDetail(row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-skeleton v-else :rows="10" animated />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getSubjectSummary,
  getAllStudentsSummary,
  exportSubjectGrades,
  downloadFile
} from '@/api/gradeSummary'
import { getGradeType } from '@/api/grade'
import type { GradeSummaryDTO, StudentGradeSummaryDTO } from '@/api/gradeSummary'
import type { GradeLevel } from '@/api/grade'

const route = useRoute()
const router = useRouter()

const summary = ref<GradeSummaryDTO | null>(null)
const students = ref<StudentGradeSummaryDTO[]>([])

const gradeLevels: GradeLevel[] = ['A', 'B', 'C', 'D']
const subjectId = Number(route.params.subjectId)

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    const [summaryRes, studentsRes] = await Promise.all([
      getSubjectSummary(subjectId),
      getAllStudentsSummary(subjectId)
    ])
    summary.value = summaryRes.data
    students.value = studentsRes.data
  } catch (error: any) {
    ElMessage.error('加载数据失败')
  }
}

function viewStudentDetail(student: StudentGradeSummaryDTO) {
  router.push(`/grades/student/${student.studentId}/subject/${subjectId}`)
}

async function handleExport() {
  try {
    const res = await exportSubjectGrades(subjectId)
    downloadFile(res.data, `${summary.value?.subjectName}-成绩汇总.xlsx`)
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error('导出失败')
  }
}
</script>

<style scoped>
.grade-summary {
  padding: 20px;
}

.summary-content {
  margin-top: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.grade-distribution {
  margin-bottom: 20px;
}

.grade-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.count {
  font-size: 16px;
  color: #606266;
}
</style>
```

**Step 3: Commit**

```bash
git add frontend/src/
git commit -m "feat: add grade summary pages with export functionality"
```

---

## 验证清单

1. **运行后端测试**
   ```bash
   cd backend && mvn test -q
   ```

2. **功能验证**
   - 学科成绩汇总统计
   - 课程评分看板
   - 学员成绩列表
   - 成绩分布图表
   - 导出学科成绩 Excel
   - 导出课程成绩 Excel

下一步：继续 `04-summary.md` 完成阶段三总结。
