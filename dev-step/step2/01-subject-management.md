# 阶段二：学科管理功能

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现学科的完整 CRUD 功能，包括学科创建、编辑、删除、列表查询，以及学科分组设置。

**Architecture:** RESTful API，学科与学员多对多关联，支持分组教学设置。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3, Element Plus

**依赖:** 需要先完成阶段一

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Subject Management Page | hjlph | 学科卡片列表，深色主题，橙色强调 |

### 设计规范

- **页面布局**: 260px 侧边栏 + 主内容区
- **卡片样式**: #111113 背景, #2A2A2E 边框, 8px 圆角
- **按钮**: 橙色渐变主按钮, 深色次要按钮
- **表单**: 深色输入框, 橙色聚焦边框
- **状态标签**: 使用对比色区分不同状态

---

## Task 1: 创建学科实体和 Mapper

**Files:**
- Create: `backend/src/main/java/com/teaching/entity/Subject.java`
- Create: `backend/src/main/java/com/teaching/entity/SubjectStudent.java`
- Create: `backend/src/main/java/com/teaching/mapper/SubjectMapper.java`
- Create: `backend/src/main/java/com/teaching/mapper/SubjectStudentMapper.java`
- Test: `backend/src/test/java/com/teaching/entity/SubjectTest.java`

**Step 1: 编写 Subject 实体测试（红灯）**

```java
// backend/src/test/java/com/teaching/entity/SubjectTest.java
package com.teaching.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SubjectTest {

    @Test
    @DisplayName("创建学科实体应包含所有字段")
    void createSubject_shouldHaveAllFields() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Java高级编程");
        subject.setDescription("Java高级特性学习");
        subject.setIsGrouped(true);
        subject.setMinMembers(3);
        subject.setMaxMembers(5);
        subject.setStartDate(LocalDate.of(2024, 1, 1));
        subject.setEndDate(LocalDate.of(2024, 3, 31));

        assertEquals(1L, subject.getId());
        assertEquals("Java高级编程", subject.getName());
        assertTrue(subject.getIsGrouped());
        assertEquals(3, subject.getMinMembers());
        assertEquals(5, subject.getMaxMembers());
    }

    @Test
    @DisplayName("新学科默认不分组")
    void newSubject_shouldNotBeGroupedByDefault() {
        Subject subject = new Subject();
        assertFalse(subject.getIsGrouped());
    }

    @Test
    @DisplayName("新学科默认成员数为1")
    void newSubject_shouldHaveDefaultMemberCount() {
        Subject subject = new Subject();
        assertEquals(1, subject.getMinMembers());
        assertEquals(1, subject.getMaxMembers());
    }

    @Test
    @DisplayName("学科可设置固定小组人数")
    void subject_shouldSupportFixedGroupSize() {
        Subject subject = new Subject();
        subject.setMinMembers(4);
        subject.setMaxMembers(4);  // 最小等于最大表示固定人数

        assertEquals(subject.getMinMembers(), subject.getMaxMembers());
    }

    @Test
    @DisplayName("学科可设置小组人数范围")
    void subject_shouldSupportGroupSizeRange() {
        Subject subject = new Subject();
        subject.setMinMembers(3);
        subject.setMaxMembers(5);

        assertTrue(subject.getMaxMembers() > subject.getMinMembers());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=SubjectTest -q
```

Expected: FAIL - Subject 类不存在

**Step 3: 创建 Subject 实体**

```java
// backend/src/main/java/com/teaching/entity/Subject.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_subject")
public class Subject extends BaseEntity {

    private String name;

    private String description;

    private Boolean isGrouped = false;

    private Integer minMembers = 1;

    private Integer maxMembers = 1;

    private LocalDate startDate;

    private LocalDate endDate;
}
```

**Step 4: 创建 SubjectStudent 关联实体**

```java
// backend/src/main/java/com/teaching/entity/SubjectStudent.java
package com.teaching.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teaching.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_subject_student")
public class SubjectStudent extends BaseEntity {

    private Long subjectId;

    private Long studentId;
}
```

**Step 5: 创建 SubjectMapper**

```java
// backend/src/main/java/com/teaching/mapper/SubjectMapper.java
package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.Subject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {

    @Select("SELECT s.* FROM t_subject s " +
            "INNER JOIN t_subject_student ss ON s.id = ss.subject_id " +
            "WHERE ss.student_id = #{studentId} AND s.del_flag = 0 AND ss.del_flag = 0")
    List<Subject> selectByStudentId(Long studentId);
}
```

**Step 6: 创建 SubjectStudentMapper**

```java
// backend/src/main/java/com/teaching/mapper/SubjectStudentMapper.java
package com.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teaching.entity.SubjectStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubjectStudentMapper extends BaseMapper<SubjectStudent> {

    @Select("SELECT COUNT(*) FROM t_subject_student " +
            "WHERE subject_id = #{subjectId} AND student_id = #{studentId} AND del_flag = 0")
    Long countBySubjectAndStudent(Long subjectId, Long studentId);
}
```

**Step 7: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=SubjectTest -q
```

Expected: PASS - 5 tests passed

**Step 8: Commit**

```bash
git add backend/src/
git commit -m "feat: add Subject entity and mappers"
```

---

## Task 2: 创建学科 DTO

**Files:**
- Create: `backend/src/main/java/com/teaching/dto/SubjectDTO.java`
- Create: `backend/src/main/java/com/teaching/dto/CreateSubjectRequest.java`
- Create: `backend/src/main/java/com/teaching/dto/UpdateSubjectRequest.java`
- Test: `backend/src/test/java/com/teaching/dto/SubjectDTOTest.java`

**Step 1: 编写 SubjectDTO 测试（红灯）**

```java
// backend/src/test/java/com/teaching/dto/SubjectDTOTest.java
package com.teaching.dto;

import com.teaching.entity.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SubjectDTOTest {

    @Test
    @DisplayName("从Subject实体转换为SubjectDTO")
    void fromEntity_shouldConvertCorrectly() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("Java高级编程");
        subject.setDescription("学习Java高级特性");
        subject.setIsGrouped(true);
        subject.setMinMembers(3);
        subject.setMaxMembers(5);
        subject.setStartDate(LocalDate.of(2024, 1, 1));
        subject.setEndDate(LocalDate.of(2024, 3, 31));

        SubjectDTO dto = SubjectDTO.fromEntity(subject);

        assertEquals(1L, dto.getId());
        assertEquals("Java高级编程", dto.getName());
        assertTrue(dto.getIsGrouped());
        assertEquals(3, dto.getMinMembers());
        assertEquals(5, dto.getMaxMembers());
    }

    @Test
    @DisplayName("CreateSubjectRequest转换为Subject实体")
    void createSubjectRequest_shouldConvertToEntity() {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Python基础");
        request.setDescription("Python入门课程");
        request.setIsGrouped(false);
        request.setStartDate(LocalDate.of(2024, 2, 1));
        request.setEndDate(LocalDate.of(2024, 4, 30));

        Subject subject = request.toEntity();

        assertEquals("Python基础", subject.getName());
        assertEquals("Python入门课程", subject.getDescription());
        assertFalse(subject.getIsGrouped());
    }

    @Test
    @DisplayName("UpdateSubjectRequest更新Subject实体")
    void updateSubjectRequest_shouldUpdateEntity() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("旧名称");
        subject.setDescription("旧描述");

        UpdateSubjectRequest request = new UpdateSubjectRequest();
        request.setName("新名称");
        request.setDescription("新描述");
        request.setIsGrouped(true);
        request.setMinMembers(2);
        request.setMaxMembers(4);

        request.updateEntity(subject);

        assertEquals("新名称", subject.getName());
        assertEquals("新描述", subject.getDescription());
        assertTrue(subject.getIsGrouped());
        assertEquals(2, subject.getMinMembers());
        assertEquals(4, subject.getMaxMembers());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=SubjectDTOTest -q
```

Expected: FAIL - DTO 类不存在

**Step 3: 创建 SubjectDTO**

```java
// backend/src/main/java/com/teaching/dto/SubjectDTO.java
package com.teaching.dto;

import com.teaching.entity.Subject;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SubjectDTO {

    private Long id;
    private String name;
    private String description;
    private Boolean isGrouped;
    private Integer minMembers;
    private Integer maxMembers;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createTime;

    // 额外字段
    private Integer studentCount;  // 学员数量
    private Integer lessonCount;   // 课程数量

    public static SubjectDTO fromEntity(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDescription(subject.getDescription());
        dto.setIsGrouped(subject.getIsGrouped());
        dto.setMinMembers(subject.getMinMembers());
        dto.setMaxMembers(subject.getMaxMembers());
        dto.setStartDate(subject.getStartDate());
        dto.setEndDate(subject.getEndDate());
        dto.setCreateTime(subject.getCreateTime());
        return dto;
    }
}
```

**Step 4: 创建 CreateSubjectRequest**

```java
// backend/src/main/java/com/teaching/dto/CreateSubjectRequest.java
package com.teaching.dto;

import com.teaching.entity.Subject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateSubjectRequest {

    @NotBlank(message = "学科名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "是否分组不能为空")
    private Boolean isGrouped = false;

    @Min(value = 1, message = "最小人数不能小于1")
    private Integer minMembers = 1;

    @Min(value = 1, message = "最大人数不能小于1")
    private Integer maxMembers = 1;

    private LocalDate startDate;

    private LocalDate endDate;

    public Subject toEntity() {
        Subject subject = new Subject();
        subject.setName(this.name);
        subject.setDescription(this.description != null ? this.description : "");
        subject.setIsGrouped(this.isGrouped);
        subject.setMinMembers(this.minMembers);
        subject.setMaxMembers(this.maxMembers);
        subject.setStartDate(this.startDate);
        subject.setEndDate(this.endDate);
        return subject;
    }
}
```

**Step 5: 创建 UpdateSubjectRequest**

```java
// backend/src/main/java/com/teaching/dto/UpdateSubjectRequest.java
package com.teaching.dto;

import com.teaching.entity.Subject;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSubjectRequest {

    private String name;

    private String description;

    private Boolean isGrouped;

    @Min(value = 1, message = "最小人数不能小于1")
    private Integer minMembers;

    @Min(value = 1, message = "最大人数不能小于1")
    private Integer maxMembers;

    private LocalDate startDate;

    private LocalDate endDate;

    public void updateEntity(Subject subject) {
        if (this.name != null) {
            subject.setName(this.name);
        }
        if (this.description != null) {
            subject.setDescription(this.description);
        }
        if (this.isGrouped != null) {
            subject.setIsGrouped(this.isGrouped);
        }
        if (this.minMembers != null) {
            subject.setMinMembers(this.minMembers);
        }
        if (this.maxMembers != null) {
            subject.setMaxMembers(this.maxMembers);
        }
        if (this.startDate != null) {
            subject.setStartDate(this.startDate);
        }
        if (this.endDate != null) {
            subject.setEndDate(this.endDate);
        }
    }
}
```

**Step 6: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=SubjectDTOTest -q
```

Expected: PASS - 3 tests passed

**Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add Subject DTOs"
```

---

## Task 3: 创建学科 Service

**Files:**
- Create: `backend/src/main/java/com/teaching/service/SubjectService.java`
- Create: `backend/src/main/java/com/teaching/service/impl/SubjectServiceImpl.java`
- Test: `backend/src/test/java/com/teaching/service/SubjectServiceTest.java`

**Step 1: 编写 SubjectService 测试（红灯）**

```java
// backend/src/test/java/com/teaching/service/SubjectServiceTest.java
package com.teaching.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teaching.dto.CreateSubjectRequest;
import com.teaching.dto.UpdateSubjectRequest;
import com.teaching.entity.Subject;
import com.teaching.entity.SubjectStudent;
import com.teaching.mapper.SubjectMapper;
import com.teaching.mapper.SubjectStudentMapper;
import com.teaching.service.impl.SubjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private SubjectStudentMapper subjectStudentMapper;

    private SubjectServiceImpl subjectService;

    @BeforeEach
    void setUp() {
        subjectService = new SubjectServiceImpl(subjectMapper, subjectStudentMapper);
    }

    @Test
    @DisplayName("创建学科")
    void createSubject_shouldInsertSubject() {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Java高级编程");
        request.setDescription("学习Java高级特性");
        request.setIsGrouped(true);
        request.setMinMembers(3);
        request.setMaxMembers(5);

        when(subjectMapper.insert(any(Subject.class))).thenReturn(1);

        subjectService.createSubject(request);

        verify(subjectMapper).insert(argThat(subject ->
                subject.getName().equals("Java高级编程") &&
                subject.getIsGrouped() &&
                subject.getMinMembers() == 3
        ));
    }

    @Test
    @DisplayName("分页查询学科列表")
    void listSubjects_shouldReturnPagedSubjects() {
        IPage<Subject> mockPage = new Page<>(1, 10);
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("测试学科");
        mockPage.setRecords(List.of(subject));
        mockPage.setTotal(1);

        when(subjectMapper.selectPage(any(Page.class), any())).thenReturn(mockPage);

        IPage<Subject> result = subjectService.listSubjects(1, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("根据ID查询学科")
    void getSubjectById_shouldReturnSubject() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("测试学科");

        when(subjectMapper.selectById(1L)).thenReturn(subject);

        Subject result = subjectService.getSubjectById(1L);

        assertNotNull(result);
        assertEquals("测试学科", result.getName());
    }

    @Test
    @DisplayName("更新学科")
    void updateSubject_shouldUpdateFields() {
        Subject existingSubject = new Subject();
        existingSubject.setId(1L);
        existingSubject.setName("旧名称");

        UpdateSubjectRequest request = new UpdateSubjectRequest();
        request.setName("新名称");
        request.setIsGrouped(true);

        when(subjectMapper.selectById(1L)).thenReturn(existingSubject);
        when(subjectMapper.updateById(any(Subject.class))).thenReturn(1);

        subjectService.updateSubject(1L, request);

        verify(subjectMapper).updateById(argThat(subject ->
                subject.getName().equals("新名称") &&
                subject.getIsGrouped()
        ));
    }

    @Test
    @DisplayName("删除学科")
    void deleteSubject_shouldDeleteSubject() {
        when(subjectMapper.deleteById(1L)).thenReturn(1);

        subjectService.deleteSubject(1L);

        verify(subjectMapper).deleteById(1L);
    }

    @Test
    @DisplayName("添加学员到学科")
    void addStudentToSubject_shouldInsertRelation() {
        when(subjectStudentMapper.countBySubjectAndStudent(1L, 2L)).thenReturn(0L);
        when(subjectStudentMapper.insert(any(SubjectStudent.class))).thenReturn(1);

        subjectService.addStudentToSubject(1L, 2L);

        verify(subjectStudentMapper).insert(argThat(ss ->
                ss.getSubjectId() == 1L && ss.getStudentId() == 2L
        ));
    }

    @Test
    @DisplayName("学员已在学科中不重复添加")
    void addStudentToSubject_shouldNotDuplicate() {
        when(subjectStudentMapper.countBySubjectAndStudent(1L, 2L)).thenReturn(1L);

        subjectService.addStudentToSubject(1L, 2L);

        verify(subjectStudentMapper, never()).insert(any());
    }

    @Test
    @DisplayName("查询学员的学科列表")
    void getSubjectsByStudentId_shouldReturnSubjects() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("学员的学科");

        when(subjectMapper.selectByStudentId(2L)).thenReturn(List.of(subject));

        List<Subject> result = subjectService.getSubjectsByStudentId(2L);

        assertEquals(1, result.size());
        assertEquals("学员的学科", result.get(0).getName());
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=SubjectServiceTest -q
```

Expected: FAIL - SubjectService 不存在

**Step 3: 创建 SubjectService 接口**

```java
// backend/src/main/java/com/teaching/service/SubjectService.java
package com.teaching.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teaching.dto.CreateSubjectRequest;
import com.teaching.dto.UpdateSubjectRequest;
import com.teaching.entity.Subject;

import java.util.List;

public interface SubjectService extends IService<Subject> {

    void createSubject(CreateSubjectRequest request);

    IPage<Subject> listSubjects(int page, int size, String keyword);

    Subject getSubjectById(Long id);

    void updateSubject(Long id, UpdateSubjectRequest request);

    void deleteSubject(Long id);

    void addStudentToSubject(Long subjectId, Long studentId);

    void removeStudentFromSubject(Long subjectId, Long studentId);

    List<Subject> getSubjectsByStudentId(Long studentId);

    List<Long> getStudentIdsBySubjectId(Long subjectId);
}
```

**Step 4: 创建 SubjectServiceImpl**

```java
// backend/src/main/java/com/teaching/service/impl/SubjectServiceImpl.java
package com.teaching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teaching.common.ResultCode;
import com.teaching.dto.CreateSubjectRequest;
import com.teaching.dto.UpdateSubjectRequest;
import com.teaching.entity.Subject;
import com.teaching.entity.SubjectStudent;
import com.teaching.exception.BusinessException;
import com.teaching.mapper.SubjectMapper;
import com.teaching.mapper.SubjectStudentMapper;
import com.teaching.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    private final SubjectMapper subjectMapper;
    private final SubjectStudentMapper subjectStudentMapper;

    @Override
    @Transactional
    public void createSubject(CreateSubjectRequest request) {
        Subject subject = request.toEntity();
        subjectMapper.insert(subject);
    }

    @Override
    public IPage<Subject> listSubjects(int page, int size, String keyword) {
        Page<Subject> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Subject::getName, keyword);
        }
        wrapper.orderByDesc(Subject::getCreateTime);

        return subjectMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Subject getSubjectById(Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "学科不存在");
        }
        return subject;
    }

    @Override
    @Transactional
    public void updateSubject(Long id, UpdateSubjectRequest request) {
        Subject subject = getSubjectById(id);
        request.updateEntity(subject);
        subjectMapper.updateById(subject);
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        subjectMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void addStudentToSubject(Long subjectId, Long studentId) {
        // 检查是否已存在
        Long count = subjectStudentMapper.countBySubjectAndStudent(subjectId, studentId);
        if (count > 0) {
            return;  // 已存在，不重复添加
        }

        SubjectStudent ss = new SubjectStudent();
        ss.setSubjectId(subjectId);
        ss.setStudentId(studentId);
        subjectStudentMapper.insert(ss);
    }

    @Override
    @Transactional
    public void removeStudentFromSubject(Long subjectId, Long studentId) {
        LambdaQueryWrapper<SubjectStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectStudent::getSubjectId, subjectId)
                .eq(SubjectStudent::getStudentId, studentId);
        subjectStudentMapper.delete(wrapper);
    }

    @Override
    public List<Subject> getSubjectsByStudentId(Long studentId) {
        return subjectMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Long> getStudentIdsBySubjectId(Long subjectId) {
        LambdaQueryWrapper<SubjectStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubjectStudent::getSubjectId, subjectId);
        return subjectStudentMapper.selectList(wrapper).stream()
                .map(SubjectStudent::getStudentId)
                .toList();
    }
}
```

**Step 5: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=SubjectServiceTest -q
```

Expected: PASS - 8 tests passed

**Step 6: Commit**

```bash
git add backend/src/
git commit -m "feat: add SubjectService with CRUD operations"
```

---

## Task 4: 创建学科 Controller

**Files:**
- Create: `backend/src/main/java/com/teaching/controller/SubjectController.java`
- Test: `backend/src/test/java/com/teaching/controller/SubjectControllerTest.java`

**Step 1: 编写 SubjectController 测试（红灯）**

```java
// backend/src/test/java/com/teaching/controller/SubjectControllerTest.java
package com.teaching.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teaching.dto.CreateSubjectRequest;
import com.teaching.dto.UpdateSubjectRequest;
import com.teaching.entity.Subject;
import com.teaching.security.JwtUtils;
import com.teaching.security.UserDetailsServiceImpl;
import com.teaching.service.SubjectService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubjectService subjectService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private Subject testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setName("Java高级编程");
        testSubject.setDescription("学习Java高级特性");
        testSubject.setIsGrouped(true);
        testSubject.setMinMembers(3);
        testSubject.setMaxMembers(5);
    }

    @Test
    @DisplayName("管理员可以获取学科列表")
    @WithMockUser(roles = "ADMIN")
    void listSubjects_shouldReturnPagedSubjects() throws Exception {
        IPage<Subject> page = new Page<>(1, 10);
        page.setRecords(List.of(testSubject));
        page.setTotal(1);

        when(subjectService.listSubjects(1, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/subjects")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].name").value("Java高级编程"));
    }

    @Test
    @DisplayName("教员可以获取学科列表")
    @WithMockUser(roles = "TEACHER")
    void listSubjects_shouldAllowTeacher() throws Exception {
        IPage<Subject> page = new Page<>(1, 10);
        page.setRecords(List.of(testSubject));
        page.setTotal(1);

        when(subjectService.listSubjects(1, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/subjects"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("管理员可以创建学科")
    @WithMockUser(roles = "ADMIN")
    void createSubject_shouldSucceed() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Python基础");
        request.setDescription("Python入门课程");
        request.setIsGrouped(false);

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(subjectService).createSubject(any(CreateSubjectRequest.class));
    }

    @Test
    @DisplayName("教员可以创建学科")
    @WithMockUser(roles = "TEACHER")
    void createSubject_shouldAllowTeacher() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Python基础");
        request.setIsGrouped(false);

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("学员不能创建学科")
    @WithMockUser(roles = "STUDENT")
    void createSubject_shouldForbidStudent() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("Python基础");
        request.setIsGrouped(false);

        mockMvc.perform(post("/api/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("获取学科详情")
    @WithMockUser(roles = "ADMIN")
    void getSubjectById_shouldReturnSubject() throws Exception {
        when(subjectService.getSubjectById(1L)).thenReturn(testSubject);

        mockMvc.perform(get("/api/subjects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Java高级编程"));
    }

    @Test
    @DisplayName("更新学科")
    @WithMockUser(roles = "ADMIN")
    void updateSubject_shouldSucceed() throws Exception {
        UpdateSubjectRequest request = new UpdateSubjectRequest();
        request.setName("Java高级编程（更新）");

        mockMvc.perform(put("/api/subjects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(subjectService).updateSubject(eq(1L), any(UpdateSubjectRequest.class));
    }

    @Test
    @DisplayName("删除学科")
    @WithMockUser(roles = "ADMIN")
    void deleteSubject_shouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/subjects/1"))
                .andExpect(status().isOk());

        verify(subjectService).deleteSubject(1L);
    }

    @Test
    @DisplayName("添加学员到学科")
    @WithMockUser(roles = "ADMIN")
    void addStudentToSubject_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/subjects/1/students/2"))
                .andExpect(status().isOk());

        verify(subjectService).addStudentToSubject(1L, 2L);
    }
}
```

**Step 2: 运行测试验证失败**

```bash
cd backend && mvn test -Dtest=SubjectControllerTest -q
```

Expected: FAIL - SubjectController 不存在

**Step 3: 创建 SubjectController**

```java
// backend/src/main/java/com/teaching/controller/SubjectController.java
package com.teaching.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.teaching.common.Result;
import com.teaching.dto.*;
import com.teaching.entity.Subject;
import com.teaching.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<PageResponse<SubjectDTO>> listSubjects(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        IPage<Subject> subjectPage = subjectService.listSubjects(page, size, keyword);

        List<SubjectDTO> subjectDTOs = subjectPage.getRecords().stream()
                .map(SubjectDTO::fromEntity)
                .toList();

        PageResponse<SubjectDTO> response = PageResponse.of(
                subjectDTOs,
                subjectPage.getTotal(),
                page,
                size
        );

        return Result.success(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<SubjectDTO> getSubjectById(@PathVariable Long id) {
        Subject subject = subjectService.getSubjectById(id);
        return Result.success(SubjectDTO.fromEntity(subject));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        subjectService.createSubject(request);
        log.info("创建学科: name={}", request.getName());
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        subjectService.updateSubject(id, request);
        log.info("更新学科: id={}", id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        log.info("删除学科: id={}", id);
        return Result.success();
    }

    @PostMapping("/{subjectId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> addStudentToSubject(
            @PathVariable Long subjectId,
            @PathVariable Long studentId) {
        subjectService.addStudentToSubject(subjectId, studentId);
        log.info("添加学员到学科: subjectId={}, studentId={}", subjectId, studentId);
        return Result.success();
    }

    @DeleteMapping("/{subjectId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> removeStudentFromSubject(
            @PathVariable Long subjectId,
            @PathVariable Long studentId) {
        subjectService.removeStudentFromSubject(subjectId, studentId);
        log.info("从学科移除学员: subjectId={}, studentId={}", subjectId, studentId);
        return Result.success();
    }

    @GetMapping("/{subjectId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<List<Long>> getSubjectStudents(@PathVariable Long subjectId) {
        List<Long> studentIds = subjectService.getStudentIdsBySubjectId(subjectId);
        return Result.success(studentIds);
    }
}
```

**Step 4: 运行测试验证通过**

```bash
cd backend && mvn test -Dtest=SubjectControllerTest -q
```

Expected: PASS - 9 tests passed

**Step 5: Commit**

```bash
git add backend/src/
git commit -m "feat: add SubjectController with CRUD endpoints"
```

---

## Task 5: 前端学科管理页面

**Files:**
- Create: `frontend/src/api/subject.ts`
- Create: `frontend/src/views/subjects/SubjectList.vue`
- Create: `frontend/src/views/subjects/SubjectForm.vue`
- Modify: `frontend/src/router/index.ts`

**Step 1: 创建学科 API**

```typescript
// frontend/src/api/subject.ts
import request from '@/utils/request'
import type { Result, PageResponse } from './types'

export interface SubjectDTO {
  id: number
  name: string
  description: string
  isGrouped: boolean
  minMembers: number
  maxMembers: number
  startDate: string
  endDate: string
  createTime: string
  studentCount?: number
  lessonCount?: number
}

export interface CreateSubjectRequest {
  name: string
  description?: string
  isGrouped: boolean
  minMembers?: number
  maxMembers?: number
  startDate?: string
  endDate?: string
}

export interface UpdateSubjectRequest {
  name?: string
  description?: string
  isGrouped?: boolean
  minMembers?: number
  maxMembers?: number
  startDate?: string
  endDate?: string
}

export function listSubjects(
  page: number,
  size: number,
  keyword?: string
): Promise<Result<PageResponse<SubjectDTO>>> {
  return request.get('/subjects', { params: { page, size, keyword } })
}

export function getSubjectById(id: number): Promise<Result<SubjectDTO>> {
  return request.get(`/subjects/${id}`)
}

export function createSubject(data: CreateSubjectRequest): Promise<Result<void>> {
  return request.post('/subjects', data)
}

export function updateSubject(id: number, data: UpdateSubjectRequest): Promise<Result<void>> {
  return request.put(`/subjects/${id}`, data)
}

export function deleteSubject(id: number): Promise<Result<void>> {
  return request.delete(`/subjects/${id}`)
}

export function addStudentToSubject(subjectId: number, studentId: number): Promise<Result<void>> {
  return request.post(`/subjects/${subjectId}/students/${studentId}`)
}

export function removeStudentFromSubject(subjectId: number, studentId: number): Promise<Result<void>> {
  return request.delete(`/subjects/${subjectId}/students/${studentId}`)
}
```

**Step 2: 创建学科列表页面**

```vue
<!-- frontend/src/views/subjects/SubjectList.vue -->
<template>
  <div class="subject-list">
    <div class="header">
      <h2>学科管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        新增学科
      </el-button>
    </div>

    <el-card>
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学科名称"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <el-table :data="subjects" v-loading="loading" stripe>
        <el-table-column prop="name" label="学科名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="分组设置" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.isGrouped" type="success">
              分组 ({{ row.minMembers }}-{{ row.maxMembers }}人)
            </el-tag>
            <el-tag v-else type="info">不分组</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间范围" width="200">
          <template #default="{ row }">
            <span v-if="row.startDate && row.endDate">
              {{ row.startDate }} ~ {{ row.endDate }}
            </span>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button type="primary" link @click="goToDetail(row.id)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadSubjects"
          @current-change="loadSubjects"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <SubjectForm
      v-model="dialogVisible"
      :subject="currentSubject"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSubjects, deleteSubject, type SubjectDTO } from '@/api/subject'
import SubjectForm from './SubjectForm.vue'

const router = useRouter()

const loading = ref(false)
const subjects = ref<SubjectDTO[]>([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const currentSubject = ref<SubjectDTO | null>(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadSubjects = async () => {
  loading.value = true
  try {
    const res = await listSubjects(pagination.page, pagination.size, searchKeyword.value || undefined)
    subjects.value = res.data.list
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadSubjects()
}

const showCreateDialog = () => {
  currentSubject.value = null
  dialogVisible.value = true
}

const showEditDialog = (subject: SubjectDTO) => {
  currentSubject.value = { ...subject }
  dialogVisible.value = true
}

const goToDetail = (id: number) => {
  router.push(`/subjects/${id}`)
}

const handleDelete = async (subject: SubjectDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学科「${subject.name}」吗？`,
      '提示',
      { type: 'warning' }
    )
    await deleteSubject(subject.id)
    ElMessage.success('删除成功')
    loadSubjects()
  } catch (error) {
    // 用户取消或删除失败
  }
}

const handleFormSuccess = () => {
  dialogVisible.value = false
  loadSubjects()
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped lang="scss">
.subject-list {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
    }
  }

  .search-bar {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .text-muted {
    color: #909399;
  }
}
</style>
```

**Step 3: 创建学科表单组件**

```vue
<!-- frontend/src/views/subjects/SubjectForm.vue -->
<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑学科' : '新增学科'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="学科名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入学科名称" />
      </el-form-item>

      <el-form-item label="学科描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入学科描述"
        />
      </el-form-item>

      <el-form-item label="是否分组" prop="isGrouped">
        <el-switch v-model="form.isGrouped" />
      </el-form-item>

      <template v-if="form.isGrouped">
        <el-form-item label="小组人数">
          <el-col :span="11">
            <el-form-item prop="minMembers">
              <el-input-number
                v-model="form.minMembers"
                :min="1"
                :max="form.maxMembers"
                placeholder="最小人数"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" class="text-center">~</el-col>
          <el-col :span="11">
            <el-form-item prop="maxMembers">
              <el-input-number
                v-model="form.maxMembers"
                :min="form.minMembers"
                placeholder="最大人数"
              />
            </el-form-item>
          </el-col>
        </el-form-item>
      </template>

      <el-form-item label="培训时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
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
import { createSubject, updateSubject, type SubjectDTO } from '@/api/subject'

const props = defineProps<{
  modelValue: boolean
  subject: SubjectDTO | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  name: '',
  description: '',
  isGrouped: false,
  minMembers: 1,
  maxMembers: 1
})

const dateRange = ref<[string, string] | null>(null)

const isEdit = computed(() => !!props.subject?.id)

const rules: FormRules = {
  name: [
    { required: true, message: '请输入学科名称', trigger: 'blur' }
  ],
  minMembers: [
    { type: 'number', min: 1, message: '最小人数不能小于1', trigger: 'change' }
  ],
  maxMembers: [
    { type: 'number', min: 1, message: '最大人数不能小于1', trigger: 'change' }
  ]
}

watch(() => props.subject, (subject) => {
  if (subject) {
    form.name = subject.name
    form.description = subject.description
    form.isGrouped = subject.isGrouped
    form.minMembers = subject.minMembers
    form.maxMembers = subject.maxMembers
    if (subject.startDate && subject.endDate) {
      dateRange.value = [subject.startDate, subject.endDate]
    } else {
      dateRange.value = null
    }
  } else {
    resetForm()
  }
}, { immediate: true })

const resetForm = () => {
  form.name = ''
  form.description = ''
  form.isGrouped = false
  form.minMembers = 1
  form.maxMembers = 1
  dateRange.value = null
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
      const data = {
        name: form.name,
        description: form.description,
        isGrouped: form.isGrouped,
        minMembers: form.isGrouped ? form.minMembers : 1,
        maxMembers: form.isGrouped ? form.maxMembers : 1,
        startDate: dateRange.value?.[0],
        endDate: dateRange.value?.[1]
      }

      if (isEdit.value && props.subject) {
        await updateSubject(props.subject.id, data)
        ElMessage.success('更新成功')
      } else {
        await createSubject(data)
        ElMessage.success('创建成功')
      }

      emit('success')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.text-center {
  text-align: center;
  line-height: 32px;
}
</style>
```

**Step 4: 更新路由配置**

```typescript
// 在 frontend/src/router/index.ts 的 children 数组中添加：
{
  path: 'subjects',
  name: 'Subjects',
  component: () => import('@/views/subjects/SubjectList.vue'),
  meta: { roles: ['ADMIN', 'TEACHER'] }
},
{
  path: 'subjects/:id',
  name: 'SubjectDetail',
  component: () => import('@/views/subjects/SubjectDetail.vue'),
  meta: { roles: ['ADMIN', 'TEACHER'] }
}
```

**Step 5: 创建学科详情页面占位**

```vue
<!-- frontend/src/views/subjects/SubjectDetail.vue -->
<template>
  <div class="subject-detail">
    <h2>学科详情</h2>
    <p>学科 ID: {{ $route.params.id }}</p>
    <!-- 课程管理、学员管理等功能将在后续实现 -->
  </div>
</template>

<script setup lang="ts">
// Subject detail placeholder
</script>
```

**Step 6: Commit**

```bash
git add frontend/src/
git commit -m "feat: add subject management pages"
```

---

## 验证清单

完成 Task 1-5 后，验证：

1. **运行后端测试**
   ```bash
   cd backend && mvn test -q
   ```

2. **启动后端服务**
   ```bash
   cd backend && mvn spring-boot:run
   ```

3. **启动前端服务**
   ```bash
   cd frontend && npm run dev
   ```

4. **功能验证**
   - 管理员登录后访问学科管理
   - 创建新学科（带分组设置）
   - 编辑学科信息
   - 删除学科
   - 搜索学科

下一步：继续 `02-lesson-management.md` 完成课程管理功能。
