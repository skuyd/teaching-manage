# 数据模型 - Teaching Manage

## 概述

本系统使用 SQLite 数据库，采用 MyBatis-Plus 作为 ORM 框架。所有表遵循统一的设计约定。

## 数据库配置

- **数据库类型**: SQLite 3.45.1
- **文件位置**: `./data/teaching.db`
- **字符集**: UTF-8
- **Schema 初始化**: `backend/src/main/resources/schema.sql`

## 设计约定

### 基础字段

所有表包含以下基础字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INTEGER | 主键，自增 |
| version | INT | 乐观锁版本号，默认 0 |
| del_flag | TINYINT | 逻辑删除标志 (0=存在, 1=删除) |
| create_by | VARCHAR(64) | 创建者用户名 |
| create_time | DATETIME | 创建时间 |
| update_by | VARCHAR(64) | 更新者用户名 |
| update_time | DATETIME | 更新时间 |

### 命名约定

- **表名**: `t_{实体名}` (小写下划线)
- **字段名**: 小写下划线命名
- **索引名**: `idx_{表名}_{字段名}`

---

## 实体关系图

```
┌─────────────┐     ┌─────────────┐
│   t_user    │     │  t_subject  │
└──────┬──────┘     └──────┬──────┘
       │                   │
       │    ┌──────────────┼──────────────┐
       │    │              │              │
       ▼    ▼              ▼              ▼
┌───────────────┐   ┌───────────┐   ┌───────────┐
│t_subject_student│ │  t_lesson │   │  t_group  │
└───────────────┘   └─────┬─────┘   └─────┬─────┘
                          │               │
                          │               ▼
                          │         ┌───────────────┐
                          │         │t_group_member │
                          ▼         └───────────────┘
                    ┌───────────────┐
                    │ t_submission  │
                    └───────┬───────┘
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
        ┌──────────┐ ┌─────────────┐ ┌──────────────┐
        │ t_grade  │ │t_code_comment│ │t_notification│
        └──────────┘ └─────────────┘ └──────────────┘
```

---

## 表结构详解

### t_user (用户表)

```sql
CREATE TABLE IF NOT EXISTS t_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) DEFAULT '',
    avatar VARCHAR(200) DEFAULT '',
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

CREATE INDEX IF NOT EXISTS idx_user_username ON t_user(username);
```

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| username | VARCHAR(50) | UNIQUE, NOT NULL | 登录用户名 |
| password | VARCHAR(100) | NOT NULL | BCrypt 加密密码 |
| role | VARCHAR(20) | NOT NULL | 角色: ADMIN/TEACHER/STUDENT |
| name | VARCHAR(50) | NOT NULL | 显示名称 |
| email | VARCHAR(100) | - | 邮箱地址 |
| avatar | VARCHAR(200) | - | 头像路径 |

**角色枚举 (UserRole)**:
- `ADMIN` - 管理员
- `TEACHER` - 教员
- `STUDENT` - 学员

---

### t_subject (学科表)

```sql
CREATE TABLE IF NOT EXISTS t_subject (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT DEFAULT '',
    is_grouped TINYINT NOT NULL DEFAULT 0,
    min_members INT NOT NULL DEFAULT 1,
    max_members INT NOT NULL DEFAULT 1,
    start_date DATE,
    end_date DATE,
    -- 基础字段...
);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| name | VARCHAR(100) | 学科名称 |
| description | TEXT | 学科描述 |
| is_grouped | TINYINT | 是否分组学科 (0=个人, 1=小组) |
| min_members | INT | 小组最小人数 |
| max_members | INT | 小组最大人数 |
| start_date | DATE | 开课日期 |
| end_date | DATE | 结课日期 |

---

### t_lesson (课程表)

```sql
CREATE TABLE IF NOT EXISTS t_lesson (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT DEFAULT '',
    lesson_time DATETIME NOT NULL,
    homework_desc TEXT DEFAULT '',
    submit_type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
    deadline DATETIME,
    allow_late TINYINT NOT NULL DEFAULT 0,
    -- 基础字段...
);

CREATE INDEX IF NOT EXISTS idx_lesson_subject_id ON t_lesson(subject_id);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| subject_id | INTEGER | 所属学科 ID |
| title | VARCHAR(200) | 课程标题 |
| content | TEXT | 课程内容 |
| lesson_time | DATETIME | 上课时间 |
| homework_desc | TEXT | 作业描述 |
| submit_type | VARCHAR(20) | 提交类型: PERSONAL/GROUP |
| deadline | DATETIME | 作业截止时间 |
| allow_late | TINYINT | 是否允许迟交 |

**提交类型枚举 (SubmitType)**:
- `PERSONAL` - 个人提交
- `GROUP` - 小组提交

---

### t_group (小组表)

```sql
CREATE TABLE IF NOT EXISTS t_group (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    leader_id INTEGER NOT NULL,
    -- 基础字段...
);

CREATE INDEX IF NOT EXISTS idx_group_subject_id ON t_group(subject_id);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| subject_id | INTEGER | 所属学科 ID |
| name | VARCHAR(100) | 小组名称 |
| leader_id | INTEGER | 组长用户 ID |

---

### t_group_member (小组成员表)

```sql
CREATE TABLE IF NOT EXISTS t_group_member (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- 基础字段...
);

CREATE INDEX IF NOT EXISTS idx_group_member_group_id ON t_group_member(group_id);
CREATE INDEX IF NOT EXISTS idx_group_member_user_id ON t_group_member(user_id);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| group_id | INTEGER | 小组 ID |
| user_id | INTEGER | 成员用户 ID |
| status | VARCHAR(20) | 成员状态 |

**成员状态枚举 (MemberStatus)**:
- `PENDING` - 待审批
- `APPROVED` - 已批准
- `REJECTED` - 已拒绝

---

### t_submission (作业提交表)

```sql
CREATE TABLE IF NOT EXISTS t_submission (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    lesson_id INTEGER NOT NULL,
    submitter_id INTEGER NOT NULL,
    group_id INTEGER,
    file_path VARCHAR(500) NOT NULL,
    submit_time DATETIME NOT NULL,
    -- 基础字段...
);

CREATE INDEX IF NOT EXISTS idx_submission_lesson_id ON t_submission(lesson_id);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| lesson_id | INTEGER | 课程 ID |
| submitter_id | INTEGER | 提交者用户 ID |
| group_id | INTEGER | 小组 ID (小组作业时) |
| file_path | VARCHAR(500) | 文件存储路径 |
| submit_time | DATETIME | 提交时间 |

---

### t_grade (评分表)

```sql
CREATE TABLE IF NOT EXISTS t_grade (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL,
    grade VARCHAR(10) NOT NULL,
    comment TEXT DEFAULT '',
    grader_id INTEGER NOT NULL,
    grade_time DATETIME NOT NULL,
    -- 基础字段...
);

CREATE INDEX IF NOT EXISTS idx_grade_submission_id ON t_grade(submission_id);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| submission_id | INTEGER | 提交 ID |
| grade | VARCHAR(10) | 评分等级 |
| comment | TEXT | 评语 |
| grader_id | INTEGER | 评分者用户 ID |
| grade_time | DATETIME | 评分时间 |

**评分等级枚举 (GradeLevel)**:
- `A` - 优秀 (90-100)
- `B` - 良好 (80-89)
- `C` - 中等 (70-79)
- `D` - 及格 (60-69)
- `E` - 不及格 (<60)

---

### t_code_comment (代码评论表)

```sql
CREATE TABLE IF NOT EXISTS t_code_comment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    line_number INT NOT NULL,
    content TEXT NOT NULL,
    commenter_id INTEGER NOT NULL,
    -- 基础字段...
);

CREATE INDEX IF NOT EXISTS idx_code_comment_submission_id ON t_code_comment(submission_id);
CREATE INDEX IF NOT EXISTS idx_code_comment_file_path ON t_code_comment(submission_id, file_path);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| submission_id | INTEGER | 提交 ID |
| file_path | VARCHAR(500) | 文件相对路径 |
| line_number | INT | 评论所在行号 |
| content | TEXT | 评论内容 |
| commenter_id | INTEGER | 评论者用户 ID |

---

### t_notification (通知表)

```sql
CREATE TABLE IF NOT EXISTS t_notification (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT DEFAULT '',
    type VARCHAR(50) NOT NULL,
    is_read TINYINT NOT NULL DEFAULT 0,
    -- 基础字段...
);

CREATE INDEX IF NOT EXISTS idx_notification_user_id ON t_notification(user_id);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | INTEGER | 接收者用户 ID |
| title | VARCHAR(200) | 通知标题 |
| content | TEXT | 通知内容 |
| type | VARCHAR(50) | 通知类型 |
| is_read | TINYINT | 是否已读 (0=未读, 1=已读) |

**通知类型枚举 (NotificationType)**:
- `LESSON_PUBLISHED` - 新课程发布
- `DEADLINE_REMINDER` - 截止日期提醒
- `GRADE_COMPLETED` - 评分完成
- `GROUP_APPLICATION` - 小组申请
- `GROUP_APPROVED` - 申请已批准

---

### t_subject_student (学员-学科关联表)

```sql
CREATE TABLE IF NOT EXISTS t_subject_student (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    -- 基础字段...
);

CREATE INDEX IF NOT EXISTS idx_subject_student_subject_id ON t_subject_student(subject_id);
CREATE INDEX IF NOT EXISTS idx_subject_student_student_id ON t_subject_student(student_id);
```

| 字段 | 类型 | 说明 |
|------|------|------|
| subject_id | INTEGER | 学科 ID |
| student_id | INTEGER | 学员用户 ID |

---

## 实体类映射

### Java 实体示例

```java
@Data
@TableName("t_user")
public class User extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;

    @EnumValue
    private UserRole role;

    private String name;
    private String email;
    private String avatar;
}
```

### 基础实体类

```java
@Data
public abstract class BaseEntity {
    @Version
    private Integer version;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

---

## 查询优化建议

### 已有索引

1. `idx_user_username` - 用户名查询
2. `idx_lesson_subject_id` - 按学科查询课程
3. `idx_group_subject_id` - 按学科查询小组
4. `idx_group_member_group_id` - 按小组查询成员
5. `idx_group_member_user_id` - 按用户查询所属小组
6. `idx_submission_lesson_id` - 按课程查询提交
7. `idx_grade_submission_id` - 按提交查询评分
8. `idx_code_comment_submission_id` - 按提交查询评论
9. `idx_code_comment_file_path` - 按文件查询评论
10. `idx_notification_user_id` - 按用户查询通知
11. `idx_subject_student_subject_id` - 按学科查询学员
12. `idx_subject_student_student_id` - 按学员查询学科

### 常用查询场景

1. **获取学科下的所有课程**: 使用 `idx_lesson_subject_id`
2. **获取课程的所有提交**: 使用 `idx_submission_lesson_id`
3. **获取用户的未读通知**: 使用 `idx_notification_user_id` + `is_read = 0`
4. **获取文件的评论**: 使用 `idx_code_comment_file_path`
