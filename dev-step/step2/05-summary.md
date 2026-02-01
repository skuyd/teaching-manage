# 阶段二：总结与验证

## 完成清单

### 后端 (backend/)

| 模块 | 文件 | 状态 |
|------|------|------|
| 学科实体 | Subject.java, SubjectMapper.java | ✅ |
| 学科服务 | SubjectService, SubjectServiceImpl | ✅ |
| 学科控制器 | SubjectController | ✅ |
| 学科DTO | SubjectDTO, CreateSubjectRequest, UpdateSubjectRequest | ✅ |
| 课程实体 | Lesson.java, LessonMapper.java | ✅ |
| 课程枚举 | SubmitType.java | ✅ |
| 课程服务 | LessonService, LessonServiceImpl | ✅ |
| 课程控制器 | LessonController | ✅ |
| 课程DTO | LessonDTO, CalendarEventDTO, CreateLessonRequest | ✅ |
| 小组实体 | Group.java, GroupMember.java, GroupMapper.java | ✅ |
| 小组枚举 | MemberStatus.java | ✅ |
| 小组服务 | GroupService, GroupServiceImpl | ✅ |
| 小组控制器 | GroupController | ✅ |
| 小组DTO | GroupDTO, GroupMemberDTO, CreateGroupRequest, JoinGroupRequest | ✅ |
| 作业实体 | Submission.java, SubmissionMapper.java | ✅ |
| 文件服务 | FileService, FileServiceImpl | ✅ |
| 作业服务 | SubmissionService, SubmissionServiceImpl | ✅ |
| 作业控制器 | SubmissionController | ✅ |
| 作业DTO | SubmissionDTO, SubmissionListDTO, FileNode | ✅ |

### 前端 (frontend/)

| 模块 | 文件 | 状态 |
|------|------|------|
| 学科API | api/subject.ts | ✅ |
| 学科列表页 | views/subjects/SubjectList.vue | ✅ |
| 学科详情页 | views/subjects/SubjectDetail.vue | ✅ |
| 课程API | api/lesson.ts | ✅ |
| 课程日历 | views/lessons/LessonCalendar.vue | ✅ |
| 课程详情页 | views/lessons/LessonDetail.vue | ✅ |
| 小组API | api/group.ts | ✅ |
| 小组列表页 | views/groups/GroupList.vue | ✅ |
| 我的小组页 | views/groups/MyGroup.vue | ✅ |
| 作业API | api/submission.ts | ✅ |
| 作业上传组件 | views/submissions/SubmissionUpload.vue | ✅ |
| 作业查看器 | views/submissions/SubmissionViewer.vue | ✅ |
| 文件树组件 | views/submissions/FileTree.vue | ✅ |
| 代码查看器 | views/submissions/CodeViewer.vue | ✅ |

## 测试覆盖

### 后端测试

```bash
cd backend && mvn test
```

| 测试类 | 测试数量 |
|--------|----------|
| SubjectTest | 3 |
| SubjectServiceTest | 4 |
| SubjectControllerTest | 5 |
| LessonTest | 3 |
| LessonServiceTest | 5 |
| LessonControllerTest | 6 |
| GroupTest | 4 |
| GroupServiceTest | 6 |
| GroupControllerTest | 8 |
| SubmissionTest | 3 |
| FileServiceTest | 5 |
| SubmissionServiceTest | 6 |
| SubmissionControllerTest | 5 |

**新增测试：约 58 个单元测试**

### 前端测试

```bash
cd frontend && npm test
```

| 测试文件 | 测试数量 |
|----------|----------|
| subject.test.ts | 4 |
| lesson.test.ts | 4 |
| group.test.ts | 4 |
| submission.test.ts | 5 |

**新增测试：约 17 个单元测试**

## API 端点

### 学科管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/subjects | 学科列表（分页） | 已认证 |
| GET | /api/subjects/{id} | 学科详情 | 已认证 |
| POST | /api/subjects | 创建学科 | TEACHER/ADMIN |
| PUT | /api/subjects/{id} | 更新学科 | TEACHER/ADMIN |
| DELETE | /api/subjects/{id} | 删除学科 | ADMIN |
| POST | /api/subjects/{id}/students/{studentId} | 添加学员到学科 | TEACHER/ADMIN |
| DELETE | /api/subjects/{id}/students/{studentId} | 移除学员 | TEACHER/ADMIN |
| GET | /api/subjects/my | 我参与的学科 | STUDENT |

### 课程管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/lessons/subject/{subjectId} | 学科下的课程列表 | 已认证 |
| GET | /api/lessons/calendar | 日历视图数据 | 已认证 |
| GET | /api/lessons/{id} | 课程详情 | 已认证 |
| POST | /api/lessons | 创建课程 | TEACHER/ADMIN |
| PUT | /api/lessons/{id} | 更新课程 | TEACHER/ADMIN |
| DELETE | /api/lessons/{id} | 删除课程 | TEACHER/ADMIN |

### 小组管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/groups/subject/{subjectId} | 学科的小组列表 | 已认证 |
| GET | /api/groups/{id} | 小组详情 | 已认证 |
| GET | /api/groups/my/{subjectId} | 我在某学科的小组 | STUDENT |
| POST | /api/groups | 创建小组 | STUDENT |
| POST | /api/groups/{id}/join | 申请加入小组 | STUDENT |
| POST | /api/groups/{id}/leave | 退出小组 | STUDENT |
| POST | /api/groups/{id}/members/{memberId}/approve | 批准成员 | 组长 |
| POST | /api/groups/{id}/members/{memberId}/reject | 拒绝成员 | 组长 |
| DELETE | /api/groups/{id}/members/{userId} | 移除成员 | 组长 |
| DELETE | /api/groups/{id} | 解散小组 | 组长 |

### 作业提交

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/submissions/lesson/{lessonId} | 提交作业 | STUDENT |
| PUT | /api/submissions/{id} | 重新提交作业 | STUDENT |
| GET | /api/submissions/lesson/{lessonId} | 课程提交列表 | TEACHER/ADMIN |
| GET | /api/submissions/lesson/{lessonId}/mine | 我的提交 | STUDENT |
| GET | /api/submissions/mine | 我的所有提交 | 已认证 |
| GET | /api/submissions/{id} | 提交详情 | 已认证 |
| GET | /api/submissions/{id}/files | 文件列表 | 已认证 |
| GET | /api/submissions/{id}/files/read | 读取文件内容 | 已认证 |
| DELETE | /api/submissions/{id} | 删除提交 | STUDENT |

## 启动验证

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

验证输出：
- SQLite 数据库表已更新
- 服务启动在 8080 端口

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

验证输出：
- Vite 开发服务器启动在 5173 端口
- Monaco Editor 依赖安装成功

### 3. 功能验证

1. **学科管理**
   - 教员创建学科（支持 Markdown 描述）
   - 设置分组配置（最小/最大人数）
   - 添加学员到学科
   - 学员查看参与的学科

2. **课程管理**
   - 创建课程，设置上课时间
   - 设置作业要求（Markdown）
   - 设置提交类型（个人/小组）
   - 设置截止时间和是否允许补交
   - 日历视图展示课程（月视图/周视图）
   - 拖拽调整课程时间

3. **小组管理**
   - 学员创建小组
   - 学员申请加入小组
   - 组长批准/拒绝申请
   - 组长移除成员
   - 组长解散小组
   - 成员退出小组

4. **作业提交**
   - 上传单个代码文件
   - 上传 ZIP 压缩包（自动解压）
   - 查看文件目录结构
   - Monaco Editor 代码预览
   - 语法高亮（支持多种语言）
   - 重新提交功能

## 数据库表

已创建/使用的表：

| 表名 | 说明 | 状态 |
|------|------|------|
| t_user | 用户表 | 已有 |
| t_subject | 学科表 | ✅ |
| t_subject_student | 学员-学科关联表 | ✅ |
| t_lesson | 课程表 | ✅ |
| t_group | 小组表 | ✅ |
| t_group_member | 小组成员表 | ✅ |
| t_submission | 作业提交表 | ✅ |

## 文件存储结构

```
uploads/
├── {subjectId}/
│   └── {lessonId}/
│       ├── {userId}/           # 个人作业
│       │   ├── Main.java
│       │   └── Utils.java
│       └── group_{groupId}/    # 小组作业
│           └── src/
│               ├── Main.java
│               └── Utils.java
```

## 配置变更

### application.yml 新增配置

```yaml
app:
  upload-path: ./uploads

spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

### 前端依赖新增

```json
{
  "dependencies": {
    "@fullcalendar/core": "^6.x",
    "@fullcalendar/daygrid": "^6.x",
    "@fullcalendar/timegrid": "^6.x",
    "@fullcalendar/interaction": "^6.x",
    "@fullcalendar/vue3": "^6.x",
    "monaco-editor": "^0.45.x",
    "@monaco-editor/loader": "^1.x"
  }
}
```

## 下一阶段预告

**阶段三：评分与展示**

1. 代码评论功能（行内评论）
2. 作业评分（A/B/C/D 等级制）
3. 成绩汇总与导出
4. 作业提交看板

继续执行：`dev-step/step3/01-code-comment.md`
