# 阶段三：总结与验证

## 完成清单

### 后端 (backend/)

| 模块 | 文件 | 状态 |
|------|------|------|
| 代码评论实体 | CodeComment.java, CodeCommentMapper.java | ✅ |
| 代码评论服务 | CodeCommentService, CodeCommentServiceImpl | ✅ |
| 代码评论控制器 | CodeCommentController | ✅ |
| 代码评论DTO | CodeCommentDTO, CreateCodeCommentRequest, FileCommentsDTO | ✅ |
| 评分实体 | Grade.java, GradeMapper.java | ✅ |
| 评分枚举 | GradeLevel.java | ✅ |
| 评分服务 | GradeService, GradeServiceImpl | ✅ |
| 评分控制器 | GradeController | ✅ |
| 评分DTO | GradeDTO, CreateGradeRequest, UpdateGradeRequest | ✅ |
| 成绩汇总服务 | GradeSummaryService, GradeSummaryServiceImpl | ✅ |
| 成绩汇总控制器 | GradeSummaryController | ✅ |
| 成绩汇总DTO | GradeSummaryDTO, StudentGradeSummaryDTO, LessonGradeSummaryDTO | ✅ |
| Excel导出 | Apache POI 集成 | ✅ |

### 前端 (frontend/)

| 模块 | 文件 | 状态 |
|------|------|------|
| 代码评论API | api/codeComment.ts | ✅ |
| 评论气泡组件 | views/submissions/CommentPopover.vue | ✅ |
| 代码查看器更新 | views/submissions/CodeViewer.vue | ✅ |
| 评分API | api/grade.ts | ✅ |
| 评分表单 | views/grades/GradeForm.vue | ✅ |
| 评分展示 | views/grades/GradeDisplay.vue | ✅ |
| 成绩汇总API | api/gradeSummary.ts | ✅ |
| 成绩汇总页 | views/grades/GradeSummary.vue | ✅ |
| 学员成绩页 | views/grades/StudentGrades.vue | ✅ |
| 课程评分看板 | views/grades/LessonGradeBoard.vue | ✅ |

## 测试覆盖

### 后端测试

```bash
cd backend && mvn test
```

| 测试类 | 测试数量 |
|--------|----------|
| CodeCommentTest | 3 |
| CodeCommentServiceTest | 6 |
| CodeCommentControllerTest | 4 |
| GradeTest | 4 |
| GradeServiceTest | 6 |
| GradeControllerTest | 4 |
| GradeSummaryServiceTest | 3 |

**新增测试：约 30 个单元测试**

## API 端点

### 代码评论

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/code-comments | 添加代码评论 | TEACHER/ADMIN |
| GET | /api/code-comments/submission/{id} | 获取提交的所有评论 | 已认证 |
| GET | /api/code-comments/submission/{id}/file | 获取文件评论（按行聚合） | 已认证 |
| GET | /api/code-comments/submission/{id}/count | 获取评论数统计 | 已认证 |
| PUT | /api/code-comments/{id} | 更新评论 | TEACHER/ADMIN |
| DELETE | /api/code-comments/{id} | 删除评论 | TEACHER/ADMIN |

### 评分管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/grades | 对提交评分 | TEACHER/ADMIN |
| PUT | /api/grades/{id} | 更新评分 | TEACHER/ADMIN |
| GET | /api/grades/submission/{id} | 获取提交的评分 | 已认证 |
| GET | /api/grades/lesson/{lessonId} | 获取课程的评分列表 | TEACHER/ADMIN |
| GET | /api/grades/subject/{subjectId} | 获取学科的评分列表 | TEACHER/ADMIN |
| GET | /api/grades/stats/student/{id}/subject/{id} | 学员成绩统计 | 已认证 |
| DELETE | /api/grades/{id} | 删除评分 | TEACHER/ADMIN |

### 成绩汇总

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/grade-summary/subject/{id} | 学科成绩汇总 | TEACHER/ADMIN |
| GET | /api/grade-summary/lesson/{id} | 课程评分看板 | TEACHER/ADMIN |
| GET | /api/grade-summary/my/{subjectId} | 我的成绩汇总 | STUDENT |
| GET | /api/grade-summary/student/{id}/subject/{id} | 学员成绩汇总 | TEACHER/ADMIN |
| GET | /api/grade-summary/subject/{id}/students | 学科内所有学员成绩 | TEACHER/ADMIN |
| GET | /api/grade-summary/subject/{id}/export | 导出学科成绩Excel | TEACHER/ADMIN |
| GET | /api/grade-summary/lesson/{id}/export | 导出课程成绩Excel | TEACHER/ADMIN |

## 启动验证

### 功能验证

1. **代码评论**
   - 教员在代码行上点击添加评论
   - 评论显示在对应行旁边
   - 按行聚合多条评论
   - 编辑/删除自己的评论
   - 学员可查看评论但不能添加

2. **作业评分**
   - 教员选择等级（A/B/C/D）
   - 添加评语
   - 使用快捷评语
   - 修改已有评分
   - 学员查看自己的评分

3. **成绩汇总**
   - 学科成绩统计卡片
   - 成绩分布图
   - 学员成绩列表
   - 课程评分看板
   - 导出 Excel 文件

## 数据库表

已创建/使用的表：

| 表名 | 说明 | 状态 |
|------|------|------|
| t_code_comment | 代码评论表 | ✅ |
| t_grade | 评分表 | ✅ |

## 依赖变更

### 后端新增依赖

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

## 下一阶段预告

**阶段四：完善功能**

1. 站内通知系统
2. 权限细化
3. UI优化与响应式适配
4. 系统配置与安全加固

继续执行：`dev-step/step4/01-notification.md`
