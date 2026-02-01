# 阶段二：课程管理功能

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现课程的完整 CRUD 功能，包括课程创建、编辑、删除，以及日历视图展示。

**Architecture:** RESTful API，课程属于学科，支持 Markdown 内容，日历组件展示。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3, Element Plus, FullCalendar

**依赖:** 需要先完成 `01-subject-management.md`

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Lesson Management Page | FAwmG | 日历视图，课程列表，深色主题 |

### 设计规范

- **日历组件**: 深色背景适配，橙色高亮当前日期
- **课程卡片**: 按学科颜色区分，悬停显示详情
- **Markdown 编辑器**: 深色主题，代码高亮
- **表单弹窗**: 居中显示，深色背景
- **时间选择器**: 适配深色主题

---

## Task 1: 创建课程实体和 Mapper

**Files:**
- Create: `backend/src/main/java/com/teaching/entity/Lesson.java`
- Create: `backend/src/main/java/com/teaching/enums/SubmitType.java`
- Create: `backend/src/main/java/com/teaching/mapper/LessonMapper.java`
- Test: `backend/src/test/java/com/teaching/entity/LessonTest.java`

**Step 1: 编写 Lesson 实体测试（红灯）**

```java
// backend/src/test/java/com/teaching/entity/LessonTest.java
package com.teaching.entity;

import com.teaching.enums.SubmitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LessonTest {

    @Test
    @DisplayName("创建课程实体应包含所有字段")
    void createLesson_shouldHaveAllFields() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubjectId(1L);
        lesson.setTitle("Java基础-第一课");
        lesson.setContent("## 课程内容\n- 变量\n- 数据类型");
        lesson.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        lesson.setHomeworkDesc("## 作业要求\n完成练习1-5");
        lesson.setSubmitType(SubmitType.PERSONAL);
        lesson.setDeadline(LocalDateTime.of(2024, 1, 20, 23, 59));
        lesson.setAllowLate(true);

        assertEquals(1L, lesson.getId());
        assertEquals("Java基础-第一课", lesson.getTitle());
        assertEquals(SubmitType.PERSONAL, lesson.getSubmitType());
        assertTrue(lesson.getAllowLate());
    }

    @Test
    @DisplayName("新课程默认为个人提交")
    void newLesson_shouldHavePersonalSubmitTypeByDefault() {
        Lesson lesson = new Lesson();
        assertEquals(SubmitType.PERSONAL, lesson.getSubmitType());
    }

    @Test
    @DisplayName("新课程默认不允许补交")
    void newLesson_shouldNotAllowLateByDefault() {
        Lesson lesson = new Lesson();
        assertFalse(lesson.getAllowLate());
    }

    @Test
    @DisplayName("提交类型枚举应有两种值")
    void submitType_shouldHaveTwoValues() {
        assertEquals(2, SubmitType.values().length);
        assertNotNull(SubmitType.PERSONAL);
        assertNotNull(SubmitType.GROUP);
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=LessonTest -q
```

Expected: FAIL - Lesson 类不存在

**Step 3: 创建 SubmitType 枚举**

```java
// backend/src/main/java/com/teaching/enums/SubmitType.java
package com.teaching.enums;

public enum SubmitType {
    PERSONAL,  // 个人提交
    GROUP      // 小组提交
}
```

**Step 4: 创建 Lesson 实体**

```java
// backend/src/main/java/com/teaching/entity/Lesson.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import com.teaching.enums.SubmitType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_lesson")
public class Lesson extends BaseEntity {

    private Long subjectId;

    private String title;

    private String content;  // Markdown 格式

    private LocalDateTime lessonTime;

    private String homeworkDesc;  // Markdown 格式

    private SubmitType submitType = SubmitType.PERSONAL;

    private LocalDateTime deadline;

    private Boolean allowLate = false;
}
```

**Step 5: 创建 LessonMapper**

```java
// backend/src/main/java/com/teaching/mapper/LessonMapper.java
package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Lesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LessonMapper extends BaseMapper<Lesson> {

    @Select("SELECT * FROM t_lesson WHERE subject_id = #{subjectId} AND del_flag = 0 ORDER BY lesson_time ASC")
    List<Lesson> selectBySubjectId(Long subjectId);

    @Select("SELECT * FROM t_lesson WHERE subject_id = #{subjectId} " +
            "AND lesson_time >= #{startTime} AND lesson_time <= #{endTime} " +
            "AND del_flag = 0 ORDER BY lesson_time ASC")
    List<Lesson> selectBySubjectIdAndTimeRange(Long subjectId, LocalDateTime startTime, LocalDateTime endTime);

    @Select("SELECT COUNT(*) FROM t_lesson WHERE subject_id = #{subjectId} AND del_flag = 0")
    Long countBySubjectId(Long subjectId);
}
```

**Step 6: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=LessonTest -q
```

Expected: PASS - 4 tests passed

**Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add Lesson entity and mapper"
```

---

## Task 2: 创建课程 DTO

**Files:**
- Create: `backend/src/main/java/com/teaching/dto/LessonDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/CreateLessonRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/UpdateLessonRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/CalendarEventDTO.java`
- Test: `backend/src/test/java/com/teaching/dto/LessonDTOTest.java`

**Step 1: 编写 LessonDTO 测试（红灯）**

```java
// backend/src/test/java/com/teaching/dto/LessonDTOTest.java
package com.teaching.dto;

import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LessonDTOTest {

    @Test
    @DisplayName("从Lesson实体转换为LessonDTO")
    void fromEntity_shouldConvertCorrectly() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setSubjectId(1L);
        lesson.setTitle("第一课");
        lesson.setContent("课程内容");
        lesson.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        lesson.setHomeworkDesc("作业要求");
        lesson.setSubmitType(SubmitType.GROUP);

        LessonDTO dto = LessonDTO.fromEntity(lesson);

        assertEquals(1L, dto.getId());
        assertEquals("第一课", dto.getTitle());
        assertEquals(SubmitType.GROUP, dto.getSubmitType());
    }

    @Test
    @DisplayName("转换为日历事件")
    void toCalendarEvent_shouldConvertCorrectly() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Java基础");
        lesson.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));

        CalendarEventDTO event = CalendarEventDTO.fromLesson(lesson);

        assertEquals(1L, event.getId());
        assertEquals("Java基础", event.getTitle());
        assertEquals("2024-01-15T09:00:00", event.getStart());
    }

    @Test
    @DisplayName("CreateLessonRequest转换为Lesson实体")
    void createLessonRequest_shouldConvertToEntity() {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setSubjectId(1L);
        request.setTitle("新课程");
        request.setContent("内容");
        request.setLessonTime(LocalDateTime.of(2024, 2, 1, 14, 0));
        request.setSubmitType(SubmitType.PERSONAL);

        Lesson lesson = request.toEntity();

        assertEquals(1L, lesson.getSubjectId());
        assertEquals("新课程", lesson.getTitle());
        assertEquals(SubmitType.PERSONAL, lesson.getSubmitType());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=LessonDTOTest -q
```

Expected: FAIL - DTO 类不存在

**Step 3: 创建 LessonDTO**

```java
// backend/src/main/java/com/teaching/dto/LessonDTO.java
package com.teaching.dto;

import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonDTO {

    private Long id;
    private Long subjectId;
    private String title;
    private String content;
    private LocalDateTime lessonTime;
    private String homeworkDesc;
    private SubmitType submitType;
    private LocalDateTime deadline;
    private Boolean allowLate;
    private LocalDateTime createTime;

    // 额外字段
    private String subjectName;
    private Integer submissionCount;  // 作业提交数

    public static LessonDTO fromEntity(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setSubjectId(lesson.getSubjectId());
        dto.setTitle(lesson.getTitle());
        dto.setContent(lesson.getContent());
        dto.setLessonTime(lesson.getLessonTime());
        dto.setHomeworkDesc(lesson.getHomeworkDesc());
        dto.setSubmitType(lesson.getSubmitType());
        dto.setDeadline(lesson.getDeadline());
        dto.setAllowLate(lesson.getAllowLate());
        dto.setCreateTime(lesson.getCreateTime());
        return dto;
    }
}
```

**Step 4: 创建 CalendarEventDTO**

```java
// backend/src/main/java/com/teaching/dto/CalendarEventDTO.java
package com.teaching.dto;

import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class CalendarEventDTO {

    private Long id;
    private String title;
    private String start;  // ISO 8601 格式
    private String end;    // ISO 8601 格式
    private String color;
    private Boolean allDay = false;

    // 扩展属性
    private Long subjectId;
    private SubmitType submitType;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static CalendarEventDTO fromLesson(Lesson lesson) {
        CalendarEventDTO event = new CalendarEventDTO();
        event.setId(lesson.getId());
        event.setTitle(lesson.getTitle());
        event.setStart(lesson.getLessonTime().format(FORMATTER));
        // 假设每节课2小时
        event.setEnd(lesson.getLessonTime().plusHours(2).format(FORMATTER));
        event.setSubjectId(lesson.getSubjectId());
        event.setSubmitType(lesson.getSubmitType());

        // 根据提交类型设置颜色
        event.setColor(lesson.getSubmitType() == SubmitType.GROUP ? "#67C23A" : "#409EFF");

        return event;
    }
}
```

**Step 5: 创建 CreateLessonRequest**

```java
// backend/src/main/java/com/teaching/dto/CreateLessonRequest.java
package com.teaching.dto;

import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateLessonRequest {

    @NotNull(message = "学科ID不能为空")
    private Long subjectId;

    @NotBlank(message = "课程标题不能为空")
    private String title;

    private String content;

    @NotNull(message = "上课时间不能为空")
    private LocalDateTime lessonTime;

    private String homeworkDesc;

    @NotNull(message = "提交类型不能为空")
    private SubmitType submitType = SubmitType.PERSONAL;

    private LocalDateTime deadline;

    private Boolean allowLate = false;

    public Lesson toEntity() {
        Lesson lesson = new Lesson();
        lesson.setSubjectId(this.subjectId);
        lesson.setTitle(this.title);
        lesson.setContent(this.content != null ? this.content : "");
        lesson.setLessonTime(this.lessonTime);
        lesson.setHomeworkDesc(this.homeworkDesc != null ? this.homeworkDesc : "");
        lesson.setSubmitType(this.submitType);
        lesson.setDeadline(this.deadline);
        lesson.setAllowLate(this.allowLate);
        return lesson;
    }
}
```

**Step 6: 创建 UpdateLessonRequest**

```java
// backend/src/main/java/com/teaching/dto/UpdateLessonRequest.java
package com.teaching.dto;

import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateLessonRequest {

    private String title;
    private String content;
    private LocalDateTime lessonTime;
    private String homeworkDesc;
    private SubmitType submitType;
    private LocalDateTime deadline;
    private Boolean allowLate;

    public void updateEntity(Lesson lesson) {
        if (this.title != null) {
            lesson.setTitle(this.title);
        }
        if (this.content != null) {
            lesson.setContent(this.content);
        }
        if (this.lessonTime != null) {
            lesson.setLessonTime(this.lessonTime);
        }
        if (this.homeworkDesc != null) {
            lesson.setHomeworkDesc(this.homeworkDesc);
        }
        if (this.submitType != null) {
            lesson.setSubmitType(this.submitType);
        }
        if (this.deadline != null) {
            lesson.setDeadline(this.deadline);
        }
        if (this.allowLate != null) {
            lesson.setAllowLate(this.allowLate);
        }
    }
}
```

**Step 7: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=LessonDTOTest -q
```

Expected: PASS - 3 tests passed

**Step 8: Commit**

```bash
git add backend/src/
git commit -m "feat: add Lesson DTOs with calendar event support"
```

---

## Task 3: 创建课程 Service

**Files:**
- Create: `backend/src/main/java/com/teaching/service/LessonService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/LessonServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/service/LessonServiceTest.java`

**Step 1: 编写 LessonService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/LessonServiceTest.java
package com.teaching.service;

import com.teaching.dto.CalendarEventDTO;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.dto.UpdateLessonRequest;
import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import com.teaching.mapper.LessonMapper;
import com.teaching.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonMapper lessonMapper;

    private LessonServiceImpl lessonService;

    @BeforeEach
    void setUp() {
        lessonService = new LessonServiceImpl(lessonMapper);
    }

    @Test
    @DisplayName("创建课程")
    void createLesson_shouldInsertLesson() {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setSubjectId(1L);
        request.setTitle("第一课");
        request.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        request.setSubmitType(SubmitType.PERSONAL);

        when(lessonMapper.insert(any(Lesson.class))).thenReturn(1);

        lessonService.createLesson(request);

        verify(lessonMapper).insert(argThat(lesson ->
                lesson.getTitle().equals("第一课") &&
                lesson.getSubmitType() == SubmitType.PERSONAL
        ));
    }

    @Test
    @DisplayName("获取学科的所有课程")
    void getLessonsBySubjectId_shouldReturnLessons() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("测试课程");

        when(lessonMapper.selectBySubjectId(1L)).thenReturn(List.of(lesson));

        List<Lesson> result = lessonService.getLessonsBySubjectId(1L);

        assertEquals(1, result.size());
        assertEquals("测试课程", result.get(0).getTitle());
    }

    @Test
    @DisplayName("获取学科的日历事件")
    void getCalendarEvents_shouldReturnEvents() {
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("日历课程");
        lesson.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        lesson.setSubmitType(SubmitType.PERSONAL);

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(lessonMapper.selectBySubjectIdAndTimeRange(1L, start, end))
                .thenReturn(List.of(lesson));

        List<CalendarEventDTO> events = lessonService.getCalendarEvents(1L, start, end);

        assertEquals(1, events.size());
        assertEquals("日历课程", events.get(0).getTitle());
    }

    @Test
    @DisplayName("更新课程")
    void updateLesson_shouldUpdateFields() {
        Lesson existingLesson = new Lesson();
        existingLesson.setId(1L);
        existingLesson.setTitle("旧标题");

        UpdateLessonRequest request = new UpdateLessonRequest();
        request.setTitle("新标题");

        when(lessonMapper.selectById(1L)).thenReturn(existingLesson);
        when(lessonMapper.updateById(any(Lesson.class))).thenReturn(1);

        lessonService.updateLesson(1L, request);

        verify(lessonMapper).updateById(argThat(lesson ->
                lesson.getTitle().equals("新标题")
        ));
    }

    @Test
    @DisplayName("删除课程")
    void deleteLesson_shouldDeleteLesson() {
        when(lessonMapper.deleteById(1L)).thenReturn(1);

        lessonService.deleteLesson(1L);

        verify(lessonMapper).deleteById(1L);
    }

    @Test
    @DisplayName("更新课程时间（拖拽调整）")
    void updateLessonTime_shouldUpdateTime() {
        Lesson existingLesson = new Lesson();
        existingLesson.setId(1L);
        existingLesson.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));

        LocalDateTime newTime = LocalDateTime.of(2024, 1, 16, 14, 0);

        when(lessonMapper.selectById(1L)).thenReturn(existingLesson);
        when(lessonMapper.updateById(any(Lesson.class))).thenReturn(1);

        lessonService.updateLessonTime(1L, newTime);

        verify(lessonMapper).updateById(argThat(lesson ->
                lesson.getLessonTime().equals(newTime)
        ));
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=LessonServiceTest -q
```

Expected: FAIL - LessonService 不存在

**Step 3: 创建 LessonService 接口**

```java
// backend/src/main/java/com/teaching/service/LessonService.java
package com.teaching.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.CalendarEventDTO;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.dto.UpdateLessonRequest;
import com.teaching.entity.Lesson;

import java.time.LocalDateTime;
import java.util.List;

public interface LessonService extends IService<Lesson> {

    void createLesson(CreateLessonRequest request);

    List<Lesson> getLessonsBySubjectId(Long subjectId);

    Lesson getLessonById(Long id);

    void updateLesson(Long id, UpdateLessonRequest request);

    void deleteLesson(Long id);

    List<CalendarEventDTO> getCalendarEvents(Long subjectId, LocalDateTime start, LocalDateTime end);

    void updateLessonTime(Long id, LocalDateTime newTime);
}
```

**Step 4: 创建 LessonServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/LessonServiceImpl.java
package com.teaching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.CalendarEventDTO;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.dto.UpdateLessonRequest;
import com.teaching.entity.Lesson;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.LessonMapper;
import com.teaching.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl extends ServiceImpl<LessonMapper, Lesson> implements LessonService {

    private final LessonMapper lessonMapper;

    @Override
    @Transactional
    public void createLesson(CreateLessonRequest request) {
        Lesson lesson = request.toEntity();
        lessonMapper.insert(lesson);
    }

    @Override
    public List<Lesson> getLessonsBySubjectId(Long subjectId) {
        return lessonMapper.selectBySubjectId(subjectId);
    }

    @Override
    public Lesson getLessonById(Long id) {
        Lesson lesson = lessonMapper.selectById(id);
        if (lesson == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "课程不存在");
        }
        return lesson;
    }

    @Override
    @Transactional
    public void updateLesson(Long id, UpdateLessonRequest request) {
        Lesson lesson = getLessonById(id);
        request.updateEntity(lesson);
        lessonMapper.updateById(lesson);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        lessonMapper.deleteById(id);
    }

    @Override
    public List<CalendarEventDTO> getCalendarEvents(Long subjectId, LocalDateTime start, LocalDateTime end) {
        List<Lesson> lessons = lessonMapper.selectBySubjectIdAndTimeRange(subjectId, start, end);
        return lessons.stream()
                .map(CalendarEventDTO::fromLesson)
                .toList();
    }

    @Override
    @Transactional
    public void updateLessonTime(Long id, LocalDateTime newTime) {
        Lesson lesson = getLessonById(id);
        lesson.setLessonTime(newTime);
        lessonMapper.updateById(lesson);
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=LessonServiceTest -q
```

Expected: PASS - 6 tests passed

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add LessonService with calendar support"
```

---

## Task 4: 创建课程 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/LessonController.java`
- Test: `backend/src/test/java/com/teaching/controller/LessonControllerTest.java`

**Step 1: 编写 LessonController 测试（红灯）**

```java
// backend/src/test/java/com/teaching/controller/LessonControllerTest.java
package com.teaching.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CalendarEventDTO;
import com.teaching.dto.CreateLessonRequest;
import com.teaching.entity.Lesson;
import com.teaching.enums.SubmitType;
import com.teaching.security.JwtUtils;
import com.teaching.security.UserDetailsServiceImpl;
import com.teaching.service.LessonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        testLesson = new Lesson();
        testLesson.setId(1L);
        testLesson.setSubjectId(1L);
        testLesson.setTitle("Java基础-第一课");
        testLesson.setLessonTime(LocalDateTime.of(2024, 1, 15, 9, 0));
        testLesson.setSubmitType(SubmitType.PERSONAL);
    }

    @Test
    @DisplayName("获取学科的课程列表")
    @WithMockUser(roles = "TEACHER")
    void getLessonsBySubjectId_shouldReturnLessons() throws Exception {
        when(lessonService.getLessonsBySubjectId(1L)).thenReturn(List.of(testLesson));

        mockMvc.perform(get("/api/lessons/subject/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("Java基础-第一课"));
    }

    @Test
    @DisplayName("获取日历事件")
    @WithMockUser(roles = "TEACHER")
    void getCalendarEvents_shouldReturnEvents() throws Exception {
        CalendarEventDTO event = CalendarEventDTO.fromLesson(testLesson);

        when(lessonService.getCalendarEvents(eq(1L), any(), any()))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/api/lessons/calendar/1")
                .param("start", "2024-01-01T00:00:00")
                .param("end", "2024-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Java基础-第一课"));
    }

    @Test
    @DisplayName("创建课程")
    @WithMockUser(roles = "TEACHER")
    void createLesson_shouldSucceed() throws Exception {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setSubjectId(1L);
        request.setTitle("新课程");
        request.setLessonTime(LocalDateTime.of(2024, 2, 1, 14, 0));
        request.setSubmitType(SubmitType.PERSONAL);

        mockMvc.perform(post("/api/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(lessonService).createLesson(any(CreateLessonRequest.class));
    }

    @Test
    @DisplayName("获取课程详情")
    @WithMockUser(roles = "STUDENT")
    void getLessonById_shouldReturnLesson() throws Exception {
        when(lessonService.getLessonById(1L)).thenReturn(testLesson);

        mockMvc.perform(get("/api/lessons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Java基础-第一课"));
    }

    @Test
    @DisplayName("更新课程时间（拖拽）")
    @WithMockUser(roles = "TEACHER")
    void updateLessonTime_shouldSucceed() throws Exception {
        mockMvc.perform(patch("/api/lessons/1/time")
                .param("newTime", "2024-01-16T14:00:00"))
                .andExpect(status().isOk());

        verify(lessonService).updateLessonTime(eq(1L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("删除课程")
    @WithMockUser(roles = "ADMIN")
    void deleteLesson_shouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/lessons/1"))
                .andExpect(status().isOk());

        verify(lessonService).deleteLesson(1L);
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=LessonControllerTest -q
```

Expected: FAIL - LessonController 不存在

**Step 3: 创建 LessonController**

```java
// backend/src/main/java/com/teaching/controller/LessonController.java
package com.teaching.controller;

import com.teaching.common.Result;
import com.teaching.dto.*;
import com.teaching.entity.Lesson;
import com.teaching.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<List<LessonDTO>> getLessonsBySubjectId(@PathVariable Long subjectId) {
        List<Lesson> lessons = lessonService.getLessonsBySubjectId(subjectId);
        List<LessonDTO> dtos = lessons.stream()
                .map(LessonDTO::fromEntity)
                .toList();
        return Result.success(dtos);
    }

    @GetMapping("/calendar/{subjectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<List<CalendarEventDTO>> getCalendarEvents(
            @PathVariable Long subjectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<CalendarEventDTO> events = lessonService.getCalendarEvents(subjectId, start, end);
        return Result.success(events);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<LessonDTO> getLessonById(@PathVariable Long id) {
        Lesson lesson = lessonService.getLessonById(id);
        return Result.success(LessonDTO.fromEntity(lesson));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> createLesson(@Valid @RequestBody CreateLessonRequest request) {
        lessonService.createLesson(request);
        log.info("创建课程: title={}, subjectId={}", request.getTitle(), request.getSubjectId());
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> updateLesson(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLessonRequest request) {
        lessonService.updateLesson(id, request);
        log.info("更新课程: id={}", id);
        return Result.success();
    }

    @PatchMapping("/{id}/time")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> updateLessonTime(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newTime) {
        lessonService.updateLessonTime(id, newTime);
        log.info("更新课程时间: id={}, newTime={}", id, newTime);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        log.info("删除课程: id={}", id);
        return Result.success();
    }
}
```

**Step 4: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=LessonControllerTest -q
```

Expected: PASS - 6 tests passed

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add LessonController with calendar API"
```

---

## Task 5: 前端课程管理和日历展示

**Files:**
- Add dependency: `@fullcalendar/vue3` in package.json
- Create: `frontend/src/api/lesson.ts`
- Modify: `frontend/src/views/subjects/SubjectDetail.vue`
- Create: `frontend/src/components/LessonCalendar.vue`
- Create: `frontend/src/components/LessonForm.vue`

**Step 1: 添加 FullCalendar 依赖**

```bash
cd frontend && npm install @fullcalendar/vue3 @fullcalendar/core @fullcalendar/daygrid @fullcalendar/timegrid @fullcalendar/interaction
```

**Step 2: 创建课程 API**

```typescript
// frontend/src/api/lesson.ts
import request from '@/utils/request'
import type { Result } from './types'

export type SubmitType = 'PERSONAL' | 'GROUP'

export interface LessonDTO {
  id: number
  subjectId: number
  title: string
  content: string
  lessonTime: string
  homeworkDesc: string
  submitType: SubmitType
  deadline: string
  allowLate: boolean
  createTime: string
}

export interface CalendarEventDTO {
  id: number
  title: string
  start: string
  end: string
  color: string
  subjectId: number
  submitType: SubmitType
}

export interface CreateLessonRequest {
  subjectId: number
  title: string
  content?: string
  lessonTime: string
  homeworkDesc?: string
  submitType: SubmitType
  deadline?: string
  allowLate?: boolean
}

export interface UpdateLessonRequest {
  title?: string
  content?: string
  lessonTime?: string
  homeworkDesc?: string
  submitType?: SubmitType
  deadline?: string
  allowLate?: boolean
}

export function getLessonsBySubjectId(subjectId: number): Promise<Result<LessonDTO[]>> {
  return request.get(`/lessons/subject/${subjectId}`)
}

export function getCalendarEvents(
  subjectId: number,
  start: string,
  end: string
): Promise<Result<CalendarEventDTO[]>> {
  return request.get(`/lessons/calendar/${subjectId}`, { params: { start, end } })
}

export function getLessonById(id: number): Promise<Result<LessonDTO>> {
  return request.get(`/lessons/${id}`)
}

export function createLesson(data: CreateLessonRequest): Promise<Result<void>> {
  return request.post('/lessons', data)
}

export function updateLesson(id: number, data: UpdateLessonRequest): Promise<Result<void>> {
  return request.put(`/lessons/${id}`, data)
}

export function updateLessonTime(id: number, newTime: string): Promise<Result<void>> {
  return request.patch(`/lessons/${id}/time`, null, { params: { newTime } })
}

export function deleteLesson(id: number): Promise<Result<void>> {
  return request.delete(`/lessons/${id}`)
}
```

**Step 3: 创建日历组件**

```vue
<!-- frontend/src/components/LessonCalendar.vue -->
<template>
  <div class="lesson-calendar">
    <FullCalendar
      ref="calendarRef"
      :options="calendarOptions"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import type { CalendarOptions, EventClickArg, DateSelectArg, EventDropArg } from '@fullcalendar/core'
import { getCalendarEvents, updateLessonTime, type CalendarEventDTO } from '@/api/lesson'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  subjectId: number
}>()

const emit = defineEmits<{
  eventClick: [lessonId: number]
  dateSelect: [start: Date, end: Date]
}>()

const calendarRef = ref()
const events = ref<CalendarEventDTO[]>([])

const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: 'zh-cn',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek'
  },
  buttonText: {
    today: '今天',
    month: '月',
    week: '周'
  },
  events: events.value,
  editable: true,
  selectable: true,
  selectMirror: true,
  dayMaxEvents: 3,
  eventClick: handleEventClick,
  select: handleDateSelect,
  eventDrop: handleEventDrop,
  datesSet: handleDatesSet
}))

const handleEventClick = (info: EventClickArg) => {
  const lessonId = Number(info.event.id)
  emit('eventClick', lessonId)
}

const handleDateSelect = (info: DateSelectArg) => {
  emit('dateSelect', info.start, info.end)
  const calendar = calendarRef.value?.getApi()
  calendar?.unselect()
}

const handleEventDrop = async (info: EventDropArg) => {
  const lessonId = Number(info.event.id)
  const newTime = info.event.start?.toISOString()

  if (!newTime) return

  try {
    await updateLessonTime(lessonId, newTime)
    ElMessage.success('课程时间已更新')
  } catch (error) {
    info.revert()
  }
}

const handleDatesSet = async (info: { start: Date; end: Date }) => {
  await loadEvents(info.start, info.end)
}

const loadEvents = async (start: Date, end: Date) => {
  try {
    const res = await getCalendarEvents(
      props.subjectId,
      start.toISOString(),
      end.toISOString()
    )
    events.value = res.data
  } catch (error) {
    console.error('Failed to load calendar events:', error)
  }
}

watch(() => props.subjectId, () => {
  const calendar = calendarRef.value?.getApi()
  if (calendar) {
    const view = calendar.view
    loadEvents(view.activeStart, view.activeEnd)
  }
})

defineExpose({
  refresh: () => {
    const calendar = calendarRef.value?.getApi()
    if (calendar) {
      const view = calendar.view
      loadEvents(view.activeStart, view.activeEnd)
    }
  }
})
</script>

<style scoped lang="scss">
.lesson-calendar {
  :deep(.fc) {
    .fc-toolbar-title {
      font-size: 1.2em;
    }

    .fc-button {
      background-color: #409eff;
      border-color: #409eff;

      &:hover {
        background-color: #66b1ff;
      }
    }

    .fc-event {
      cursor: pointer;
    }
  }
}
</style>
```

**Step 4: 创建课程表单组件**

```vue
<!-- frontend/src/components/LessonForm.vue -->
<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑课程' : '新增课程'"
    width="700px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="课程标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入课程标题" />
      </el-form-item>

      <el-form-item label="上课时间" prop="lessonTime">
        <el-date-picker
          v-model="form.lessonTime"
          type="datetime"
          placeholder="选择上课时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
        />
      </el-form-item>

      <el-form-item label="教学内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="支持 Markdown 格式"
        />
      </el-form-item>

      <el-form-item label="作业要求" prop="homeworkDesc">
        <el-input
          v-model="form.homeworkDesc"
          type="textarea"
          :rows="4"
          placeholder="支持 Markdown 格式"
        />
      </el-form-item>

      <el-form-item label="提交类型" prop="submitType">
        <el-radio-group v-model="form.submitType">
          <el-radio value="PERSONAL">个人提交</el-radio>
          <el-radio value="GROUP">小组提交</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="截止时间">
        <el-date-picker
          v-model="form.deadline"
          type="datetime"
          placeholder="选择截止时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DDTHH:mm:ss"
        />
      </el-form-item>

      <el-form-item label="允许补交">
        <el-switch v-model="form.allowLate" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createLesson, updateLesson, type LessonDTO, type SubmitType } from '@/api/lesson'

const props = defineProps<{
  modelValue: boolean
  subjectId: number
  lesson: LessonDTO | null
  defaultDate?: Date
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  title: '',
  lessonTime: '',
  content: '',
  homeworkDesc: '',
  submitType: 'PERSONAL' as SubmitType,
  deadline: '',
  allowLate: false
})

const isEdit = computed(() => !!props.lesson?.id)

const rules: FormRules = {
  title: [{ required: true, message: '请输入课程标题', trigger: 'blur' }],
  lessonTime: [{ required: true, message: '请选择上课时间', trigger: 'change' }],
  submitType: [{ required: true, message: '请选择提交类型', trigger: 'change' }]
}

watch(() => props.lesson, (lesson) => {
  if (lesson) {
    form.title = lesson.title
    form.lessonTime = lesson.lessonTime
    form.content = lesson.content
    form.homeworkDesc = lesson.homeworkDesc
    form.submitType = lesson.submitType
    form.deadline = lesson.deadline || ''
    form.allowLate = lesson.allowLate
  } else {
    resetForm()
  }
}, { immediate: true })

watch(() => props.defaultDate, (date) => {
  if (date && !props.lesson) {
    form.lessonTime = date.toISOString().slice(0, 19)
  }
})

const resetForm = () => {
  form.title = ''
  form.lessonTime = ''
  form.content = ''
  form.homeworkDesc = ''
  form.submitType = 'PERSONAL'
  form.deadline = ''
  form.allowLate = false
}

const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value && props.lesson) {
        await updateLesson(props.lesson.id, form)
        ElMessage.success('更新成功')
      } else {
        await createLesson({
          ...form,
          subjectId: props.subjectId
        })
        ElMessage.success('创建成功')
      }

      emit('success')
    } finally {
      loading.value = false
    }
  })
}
</script>
```

**Step 5: 完善学科详情页面**

```vue
<!-- frontend/src/views/subjects/SubjectDetail.vue -->
<template>
  <div class="subject-detail">
    <div class="header">
      <div class="title-section">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>{{ subject?.name }}</h2>
        <el-tag v-if="subject?.isGrouped" type="success">
          分组 ({{ subject.minMembers }}-{{ subject.maxMembers }}人)
        </el-tag>
      </div>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        新增课程
      </el-button>
    </div>

    <el-card v-if="subject">
      <template #header>
        <div class="card-header">
          <span>学科信息</span>
        </div>
      </template>
      <el-descriptions :column="2">
        <el-descriptions-item label="培训时间">
          {{ subject.startDate }} ~ {{ subject.endDate }}
        </el-descriptions-item>
        <el-descriptions-item label="学科描述" :span="2">
          {{ subject.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="calendar-card">
      <template #header>
        <div class="card-header">
          <span>课程日历</span>
        </div>
      </template>
      <LessonCalendar
        v-if="subjectId"
        ref="calendarRef"
        :subject-id="subjectId"
        @event-click="handleEventClick"
        @date-select="handleDateSelect"
      />
    </el-card>

    <!-- 课程表单对话框 -->
    <LessonForm
      v-model="dialogVisible"
      :subject-id="subjectId"
      :lesson="currentLesson"
      :default-date="selectedDate"
      @success="handleFormSuccess"
    />

    <!-- 课程详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="课程详情"
      width="600px"
    >
      <div v-if="viewLesson" class="lesson-detail">
        <h3>{{ viewLesson.title }}</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="上课时间">
            {{ formatDateTime(viewLesson.lessonTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="提交类型">
            <el-tag :type="viewLesson.submitType === 'GROUP' ? 'success' : 'primary'">
              {{ viewLesson.submitType === 'GROUP' ? '小组提交' : '个人提交' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="截止时间">
            {{ viewLesson.deadline ? formatDateTime(viewLesson.deadline) : '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="允许补交">
            {{ viewLesson.allowLate ? '是' : '否' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="section" v-if="viewLesson.content">
          <h4>教学内容</h4>
          <div class="markdown-content" v-html="renderMarkdown(viewLesson.content)"></div>
        </div>

        <div class="section" v-if="viewLesson.homeworkDesc">
          <h4>作业要求</h4>
          <div class="markdown-content" v-html="renderMarkdown(viewLesson.homeworkDesc)"></div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="editLesson">编辑</el-button>
        <el-button type="danger" @click="handleDeleteLesson">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSubjectById, type SubjectDTO } from '@/api/subject'
import { getLessonById, deleteLesson, type LessonDTO } from '@/api/lesson'
import LessonCalendar from '@/components/LessonCalendar.vue'
import LessonForm from '@/components/LessonForm.vue'

const route = useRoute()
const router = useRouter()

const subjectId = computed(() => Number(route.params.id))
const subject = ref<SubjectDTO | null>(null)
const calendarRef = ref()
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentLesson = ref<LessonDTO | null>(null)
const viewLesson = ref<LessonDTO | null>(null)
const selectedDate = ref<Date | undefined>()

const loadSubject = async () => {
  const res = await getSubjectById(subjectId.value)
  subject.value = res.data
}

const goBack = () => {
  router.push('/subjects')
}

const showCreateDialog = () => {
  currentLesson.value = null
  selectedDate.value = undefined
  dialogVisible.value = true
}

const handleDateSelect = (start: Date) => {
  currentLesson.value = null
  selectedDate.value = start
  dialogVisible.value = true
}

const handleEventClick = async (lessonId: number) => {
  const res = await getLessonById(lessonId)
  viewLesson.value = res.data
  detailDialogVisible.value = true
}

const editLesson = () => {
  if (viewLesson.value) {
    currentLesson.value = { ...viewLesson.value }
    detailDialogVisible.value = false
    dialogVisible.value = true
  }
}

const handleDeleteLesson = async () => {
  if (!viewLesson.value) return

  try {
    await ElMessageBox.confirm('确定要删除这个课程吗？', '提示', { type: 'warning' })
    await deleteLesson(viewLesson.value.id)
    ElMessage.success('删除成功')
    detailDialogVisible.value = false
    calendarRef.value?.refresh()
  } catch (error) {
    // 用户取消
  }
}

const handleFormSuccess = () => {
  dialogVisible.value = false
  calendarRef.value?.refresh()
}

const formatDateTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const renderMarkdown = (text: string) => {
  // 简单的 Markdown 渲染，实际项目可使用 marked 等库
  return text
    .replace(/^## (.+)$/gm, '<h4>$1</h4>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/\n/g, '<br>')
}

onMounted(() => {
  loadSubject()
})
</script>

<style scoped lang="scss">
.subject-detail {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .title-section {
      display: flex;
      align-items: center;
      gap: 16px;

      h2 {
        margin: 0;
      }
    }
  }

  .calendar-card {
    margin-top: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .lesson-detail {
    h3 {
      margin-top: 0;
      margin-bottom: 20px;
    }

    .section {
      margin-top: 20px;

      h4 {
        margin-bottom: 10px;
        color: #303133;
      }

      .markdown-content {
        background: #f5f7fa;
        padding: 15px;
        border-radius: 4px;
      }
    }
  }
}
</style>
```

**Step 6: Commit**

```bash
git add frontend/
git commit -m "feat: add lesson calendar and management pages"
```

---

## 验证清单

1. **运行后端测试**
   ```bash
   cd backend && mvn test -q
   ```

2. **启动服务并验证**
   - 创建学科
   - 进入学科详情页
   - 在日历上新增课程
   - 拖拽调整课程时间
   - 查看、编辑、删除课程

下一步：继续 `03-group-management.md` 完成小组管理功能。
