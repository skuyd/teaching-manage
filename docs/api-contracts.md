# API 接口文档 - Teaching Manage

## 概述

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: JWT Bearer Token
- **请求格式**: JSON (除文件上传外)
- **响应格式**: 统一 JSON 格式

### 统一响应格式

```json
{
  "code": 200,
  "success": true,
  "data": { ... },
  "message": "Success"
}
```

### 错误码

| 错误码 | 含义 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器错误 |

---

## 认证模块 `/api/auth`

### POST /login
用户登录

**请求体:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "name": "管理员",
      "role": "ADMIN",
      "email": "admin@example.com",
      "avatar": ""
    }
  }
}
```

---

## 用户模块 `/api/users`

### GET /
获取用户列表 (分页)

**权限**: ADMIN

**参数:**
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页数量，默认 10 |
| role | string | 否 | 角色筛选 |
| keyword | string | 否 | 搜索关键词 |

### GET /{id}
获取用户详情

**权限**: ADMIN

### POST /
创建用户

**权限**: ADMIN

**请求体:**
```json
{
  "username": "newuser",
  "password": "password123",
  "name": "新用户",
  "role": "STUDENT",
  "email": "user@example.com"
}
```

### PUT /{id}
更新用户

**权限**: ADMIN

### DELETE /{id}
删除用户

**权限**: ADMIN

### GET /me
获取当前用户信息

**权限**: 已认证

### PUT /me
更新当前用户信息

**权限**: 已认证

### POST /avatar
上传头像

**权限**: 已认证

**Content-Type**: multipart/form-data

### GET /students
获取学员列表

**权限**: ADMIN, TEACHER

### GET /export
导出用户列表 (Excel)

**权限**: ADMIN

### GET /template
下载导入模板 (Excel)

**权限**: ADMIN

### POST /import
批量导入用户 (Excel)

**权限**: ADMIN

**Content-Type**: multipart/form-data

---

## 学科模块 `/api/subjects`

### GET /
获取学科列表

**权限**: 已认证

**参数:**
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| all | boolean | 否 | 是否获取所有学科 |

### GET /my
获取我的学科列表 (学员)

**权限**: STUDENT

### GET /{id}
获取学科详情

**权限**: 已认证

### POST /
创建学科

**权限**: ADMIN

**请求体:**
```json
{
  "name": "Java 基础",
  "description": "Java 编程入门课程",
  "isGrouped": false,
  "minMembers": 1,
  "maxMembers": 1,
  "startDate": "2026-03-01",
  "endDate": "2026-06-30"
}
```

### PUT /{id}
更新学科

**权限**: ADMIN

### GET /{id}/delete-stats
获取学科删除统计

**权限**: ADMIN

**响应:**
```json
{
  "code": 200,
  "data": {
    "subjectId": 1,
    "subjectName": "Java 基础",
    "lessonCount": 10,
    "submissionCount": 50,
    "gradeCount": 45,
    "commentCount": 120,
    "studentCount": 30,
    "groupCount": 0
  }
}
```

### DELETE /{id}
物理删除学科 (级联删除所有关联数据)

**权限**: ADMIN

### POST /{subjectId}/students/{studentId}
添加学员到学科

**权限**: ADMIN

### DELETE /{subjectId}/students/{studentId}
从学科移除学员

**权限**: ADMIN

### GET /{subjectId}/students
获取学科学员列表

**权限**: 已认证

---

## 课程模块 `/api/lessons`

### GET /
获取课程列表 (带日期范围)

**权限**: 已认证

**参数:**
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| subjectId | long | 否 | 学科 ID |
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期 |

### GET /all
获取所有课程

**权限**: 已认证

### GET /subject/{subjectId}
获取学科下的课程

**权限**: 已认证

### GET /{id}
获取课程详情

**权限**: 已认证

### POST /
创建课程

**权限**: ADMIN, TEACHER

**请求体:**
```json
{
  "subjectId": 1,
  "title": "第一课：Hello World",
  "content": "课程内容...",
  "lessonTime": "2026-03-01T10:00:00",
  "homeworkDesc": "作业描述...",
  "submitType": "PERSONAL",
  "deadline": "2026-03-08T23:59:59",
  "allowLate": false
}
```

### PUT /{id}
更新课程

**权限**: ADMIN, TEACHER

### PUT /{id}/time
更新课程时间 (日历拖拽)

**权限**: ADMIN, TEACHER

**请求体:**
```json
{
  "lessonTime": "2026-03-02T14:00:00"
}
```

### GET /{id}/delete-stats
获取课程删除统计

**权限**: ADMIN, TEACHER

### DELETE /{id}
物理删除课程 (级联删除)

**权限**: ADMIN, TEACHER

---

## 小组模块 `/api/groups`

### GET /subject/{subjectId}
获取学科下的小组列表

**权限**: 已认证

### GET /{id}
获取小组详情

**权限**: 已认证

### GET /my/{subjectId}
获取我在学科中的小组

**权限**: STUDENT

### POST /
创建小组

**权限**: STUDENT

**请求体:**
```json
{
  "subjectId": 1,
  "name": "第一小组"
}
```

### POST /{groupId}/join
申请加入小组

**权限**: STUDENT

### POST /{groupId}/leave
退出小组

**权限**: STUDENT

### POST /{groupId}/members/{memberId}/approve
批准加入申请

**权限**: STUDENT (组长)

### POST /{groupId}/members/{memberId}/reject
拒绝加入申请

**权限**: STUDENT (组长)

### DELETE /{groupId}/members/{userId}
移除成员

**权限**: STUDENT (组长)

### DELETE /{groupId}
解散小组

**权限**: STUDENT (组长)

---

## 作业提交模块 `/api/submissions`

### POST /submit
提交作业

**权限**: STUDENT

**Content-Type**: multipart/form-data

**参数:**
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| lessonId | long | 是 | 课程 ID |
| groupId | long | 否 | 小组 ID (分组作业) |
| file | file | 是 | 作业文件 (ZIP/RAR) |

### POST /resubmit
重新提交

**权限**: STUDENT

### GET /lesson/{lessonId}
获取课程的提交列表

**权限**: 已认证

### GET /{id}
获取提交详情

**权限**: 已认证

### GET /my/{lessonId}
获取我的提交

**权限**: STUDENT

### DELETE /{id}
删除提交

**权限**: ADMIN, TEACHER

### GET /{id}/file-tree
获取文件树

**权限**: 已认证

### GET /{id}/file-content
获取文件内容

**权限**: 已认证

**参数:**
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| path | string | 是 | 文件相对路径 |

### GET /{id}/download
下载原始文件

**权限**: ADMIN, TEACHER

---

## 代码评论模块 `/api/comments`

### POST /
添加评论

**权限**: ADMIN, TEACHER

**请求体:**
```json
{
  "submissionId": 1,
  "filePath": "src/main/java/Main.java",
  "lineNumber": 10,
  "content": "这里可以优化..."
}
```

### PUT /{id}
更新评论

**权限**: ADMIN, TEACHER

### DELETE /{id}
删除评论

**权限**: ADMIN, TEACHER

### GET /submission/{submissionId}
获取提交的所有评论

**权限**: 已认证

### GET /submission/{submissionId}/file
获取文件的评论

**权限**: 已认证

**参数:**
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| filePath | string | 是 | 文件路径 |

### GET /submission/{submissionId}/line
获取指定行的评论

**权限**: 已认证

**参数:**
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| filePath | string | 是 | 文件路径 |
| lineNumber | int | 是 | 行号 |

### GET /submission/{submissionId}/grouped
获取评论 (按文件分组)

**权限**: 已认证

---

## 评分模块 `/api/grades`

### POST /
创建评分

**权限**: ADMIN, TEACHER

**请求体:**
```json
{
  "submissionId": 1,
  "grade": "A",
  "comment": "完成得很好！"
}
```

### PUT /{id}
更新评分

**权限**: ADMIN, TEACHER

### DELETE /{id}
删除评分

**权限**: ADMIN, TEACHER

### GET /submission/{submissionId}
获取提交的评分

**权限**: 已认证

### GET /lesson/{lessonId}
获取课程的评分列表

**权限**: ADMIN, TEACHER

### GET /student/{studentId}
获取学员的评分列表

**权限**: 已认证

### GET /lesson/{lessonId}/statistics
获取课程评分统计

**权限**: ADMIN, TEACHER

### GET /student/{studentId}/statistics
获取学员评分统计

**权限**: 已认证

---

## 成绩汇总模块 `/api/grade-summary`

### GET /subject/{subjectId}
获取学科成绩汇总

**权限**: ADMIN, TEACHER

### GET /lesson/{lessonId}
获取课程成绩汇总

**权限**: ADMIN, TEACHER

### GET /student/{studentId}
获取学员成绩汇总

**权限**: 已认证

### GET /subject/{subjectId}/export
导出学科成绩 (Excel)

**权限**: ADMIN, TEACHER

### GET /lesson/{lessonId}/export
导出课程成绩 (Excel)

**权限**: ADMIN, TEACHER

### GET /student/{studentId}/export
导出学员成绩 (Excel)

**权限**: 已认证

---

## 通知模块 `/api/notifications`

### GET /
获取通知列表

**权限**: 已认证

### GET /unread
获取未读通知

**权限**: 已认证

### GET /unread/count
获取未读通知数量

**权限**: 已认证

### PUT /{id}/read
标记为已读

**权限**: 已认证

### PUT /read-all
全部标记为已读

**权限**: 已认证

### DELETE /{id}
删除通知

**权限**: 已认证

---

## 仪表盘模块 `/api/dashboard`

### GET /admin/stats
管理员统计数据

**权限**: ADMIN

**响应:**
```json
{
  "code": 200,
  "data": {
    "userCount": 100,
    "subjectCount": 5,
    "lessonCount": 50,
    "submissionCount": 200
  }
}
```

### GET /teacher/stats
教员统计数据

**权限**: TEACHER

### GET /student/stats
学员统计数据

**权限**: STUDENT

---

## 健康检查 `/api/health`

### GET /
健康检查

**权限**: 无

**响应:**
```json
{
  "status": "UP"
}
```
