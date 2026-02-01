# 教学管理系统需求规格说明书

## 文档信息

| 项目 | 内容 |
|------|------|
| 项目名称 | 教学管理系统 (Teaching Management System) |
| 版本 | 1.0 |
| 编写日期 | 2026-01-31 |
| 目标用户 | IT 编程培训小型机构 |
| 预计规模 | 3-5 个并行学科、30-100 名学员 |

---

## 1. 项目概述

### 1.1 项目背景

本系统面向 IT 编程培训的小型机构，提供完整的教学管理解决方案。系统支持学科管理、课程安排、作业提交与批改、小组协作等核心功能，帮助教育机构高效管理教学流程。

### 1.2 项目目标

1. 实现学科和课程的数字化管理
2. 支持作业的在线提交、代码审查和评分
3. 提供小组协作功能，支持团队项目
4. 建立完善的通知机制，确保信息及时传达
5. 提供直观的教学进度跟踪和成绩汇总

### 1.3 技术栈

| 层级 | 技术选型 | 说明 |
|------|----------|------|
| 前端框架 | Vue 3 + Vite | 现代化前端构建 |
| UI 组件库 | Element Plus | 企业级 UI 组件 |
| 代码编辑器 | Monaco Editor | VS Code 同款编辑器 |
| 后端框架 | Java 17 + Spring Boot 3 | 企业级后端框架 |
| ORM 框架 | MyBatis-Plus | 简化数据库操作 |
| 数据库 | SQLite | 轻量级嵌入式数据库 |
| 认证方式 | JWT Token | 无状态认证 |
| 文件存储 | 本地文件系统 | 简单可靠 |

---

## 2. 用户角色与权限

### 2.1 角色定义

系统包含三种用户角色：

| 角色 | 标识符 | 说明 |
|------|--------|------|
| 系统管理员 | ADMIN | 拥有最高权限，负责系统管理 |
| 教员 | TEACHER | 负责教学和评分工作 |
| 学员 | STUDENT | 学习课程、提交作业 |

### 2.2 权限矩阵

#### 用户管理

| 功能 | 系统管理员 | 教员 | 学员 |
|------|:----------:|:----:|:----:|
| 创建/编辑/删除用户 | ✓ | ✗ | ✗ |
| 查看用户列表 | ✓ | ✓ | ✗ |
| 修改个人信息 | ✓ | ✓ | ✓ |

#### 学科管理

| 功能 | 系统管理员 | 教员 | 学员 |
|------|:----------:|:----:|:----:|
| 创建/编辑/删除学科 | ✓ | ✓ | ✗ |
| 查看学科列表 | ✓ | ✓ | ✓ |
| 分配学员到学科 | ✓ | ✓ | ✗ |

#### 课程管理

| 功能 | 系统管理员 | 教员 | 学员 |
|------|:----------:|:----:|:----:|
| 创建/编辑/删除课程 | ✓ | ✓ | ✗ |
| 查看课程内容 | ✓ | ✓ | ✓ |
| 查看课程日历 | ✓ | ✓ | ✓ |

#### 小组管理

| 功能 | 系统管理员 | 教员 | 学员 |
|------|:----------:|:----:|:----:|
| 创建小组 | ✗ | ✗ | ✓ |
| 申请加入小组 | ✗ | ✗ | ✓ |
| 审批小组申请 | ✗ | ✗ | ✓(组长) |
| 查看所有小组 | ✓ | ✓ | ✓(本学科) |

#### 作业管理

| 功能 | 系统管理员 | 教员 | 学员 |
|------|:----------:|:----:|:----:|
| 提交作业 | ✗ | ✗ | ✓ |
| 查看自己/小组作业 | ✗ | ✗ | ✓ |
| 查看所有学员作业 | ✓ | ✓ | ✗ |

#### 评分管理

| 功能 | 系统管理员 | 教员 | 学员 |
|------|:----------:|:----:|:----:|
| 评分和评论 | ✓ | ✓ | ✗ |
| 添加代码行评论 | ✓ | ✓ | ✗ |
| 查看自己成绩 | ✓ | ✓ | ✓ |
| 查看所有成绩 | ✓ | ✓ | ✗ |
| 导出成绩 | ✓ | ✓ | ✗ |

#### 通知管理

| 功能 | 系统管理员 | 教员 | 学员 |
|------|:----------:|:----:|:----:|
| 接收通知 | ✓ | ✓ | ✓ |
| 标记已读 | ✓ | ✓ | ✓ |

---

## 3. 功能模块详细说明

### 3.1 用户管理模块

#### 3.1.1 功能描述

管理系统用户的注册、登录、信息维护等功能。

#### 3.1.2 功能列表

| 功能编号 | 功能名称 | 描述 | 操作角色 |
|----------|----------|------|----------|
| U-001 | 用户登录 | 用户通过用户名和密码登录系统 | 所有角色 |
| U-002 | 用户登出 | 用户退出登录状态 | 所有角色 |
| U-003 | 创建用户 | 管理员创建新用户账号 | 管理员 |
| U-004 | 编辑用户 | 管理员修改用户信息 | 管理员 |
| U-005 | 删除用户 | 管理员删除用户（逻辑删除） | 管理员 |
| U-006 | 查看用户列表 | 分页查看所有用户 | 管理员、教员 |
| U-007 | 修改个人信息 | 用户修改自己的基本信息 | 所有角色 |
| U-008 | 修改密码 | 用户修改自己的登录密码 | 所有角色 |
| U-009 | 上传头像 | 用户上传个人头像 | 所有角色 |

#### 3.1.3 用户属性

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| username | String | ✓ | 登录用户名，唯一 |
| password | String | ✓ | 登录密码，加密存储 |
| role | String | ✓ | 角色：ADMIN/TEACHER/STUDENT |
| name | String | ✓ | 真实姓名 |
| email | String | ✗ | 电子邮箱 |
| avatar | String | ✗ | 头像图片路径 |

#### 3.1.4 用户列表显示

用户管理页面表格列：
| 列名 | 说明 |
|------|------|
| 姓名 | 显示真实姓名（name）和头像 |
| 用户名 | 显示登录用户名（username） |
| 角色 | 教师/学员/管理员，不同颜色标识 |
| 状态 | 正常（绿色）/已禁用（红色） |
| 注册时间 | 用户创建时间 |
| 操作 | 编辑、禁用/启用、删除 |

#### 3.1.5 业务规则

1. 用户名长度 3-50 个字符，只允许字母、数字、下划线
2. 密码长度至少 6 个字符
3. 密码使用 BCrypt 加密存储
4. JWT Token 有效期 24 小时
5. 删除用户为逻辑删除，不影响历史数据

---

### 3.2 学科管理模块

#### 3.2.1 功能描述

管理培训学科的基本信息、分组设置和学员分配。

#### 3.2.2 功能列表

| 功能编号 | 功能名称 | 描述 | 操作角色 |
|----------|----------|------|----------|
| S-001 | 创建学科 | 创建新的培训学科 | 管理员、教员 |
| S-002 | 编辑学科 | 修改学科基本信息 | 管理员、教员 |
| S-003 | 删除学科 | 删除学科（逻辑删除） | 管理员、教员 |
| S-004 | 查看学科列表 | 分页查看学科列表 | 所有角色 |
| S-005 | 查看学科详情 | 查看学科完整信息 | 所有角色 |
| S-006 | 分配学员 | 将学员添加到学科 | 管理员、教员 |
| S-007 | 移除学员 | 从学科移除学员 | 管理员、教员 |
| S-008 | 查看学科学员 | 查看学科下所有学员 | 管理员、教员 |

#### 3.2.3 学科属性

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| name | String | ✓ | 学科名称，最长 100 字符 |
| description | Text | ✗ | 学科介绍，支持 Markdown |
| isGrouped | Boolean | ✓ | 是否启用分组，默认否 |
| minMembers | Integer | ✗ | 小组最少人数，默认 1 |
| maxMembers | Integer | ✗ | 小组最多人数，默认 1 |
| startDate | Date | ✗ | 学科开始日期 |
| endDate | Date | ✗ | 学科结束日期 |

#### 3.2.4 业务规则

1. 学科名称不能重复
2. 启用分组时，必须设置人数范围（minMembers ≤ maxMembers）
3. 分组人数范围建议 2-10 人
4. 学科删除后，关联的课程和小组一并逻辑删除
5. 学员只能查看自己被分配的学科

---

### 3.3 课程管理模块

#### 3.3.1 功能描述

管理学科下的具体课程，包括教学内容、作业设置和日历展示。

#### 3.3.2 功能列表

| 功能编号 | 功能名称 | 描述 | 操作角色 |
|----------|----------|------|----------|
| L-001 | 创建课程 | 在学科下创建新课程 | 管理员、教员 |
| L-002 | 编辑课程 | 修改课程信息 | 管理员、教员 |
| L-003 | 删除课程 | 删除课程（逻辑删除） | 管理员、教员 |
| L-004 | 查看课程列表 | 分页查看学科下的课程 | 所有角色 |
| L-005 | 查看课程详情 | 查看课程完整信息 | 所有角色 |
| L-006 | 日历月视图 | 按月展示课程安排 | 所有角色 |
| L-007 | 日历周视图 | 按周展示课程安排 | 所有角色 |
| L-008 | 拖拽调整时间 | 在日历上拖拽调整课程时间 | 管理员、教员 |

#### 3.3.3 课程属性

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| subjectId | Long | ✓ | 所属学科 ID |
| title | String | ✓ | 课程标题，最长 200 字符 |
| content | Text | ✗ | 教学内容，支持 Markdown |
| lessonTime | DateTime | ✓ | 上课时间 |
| homeworkDesc | Text | ✗ | 作业要求，支持 Markdown |
| submitType | String | ✓ | 提交类型：PERSONAL/GROUP |
| deadline | DateTime | ✗ | 作业截止时间 |
| allowLate | Boolean | ✓ | 是否允许补交，默认否 |

#### 3.3.4 业务规则

1. 课程必须关联到某个学科
2. 课程时间不能早于学科开始日期或晚于结束日期（如设置）
3. 作业截止时间必须晚于上课时间
4. 小组提交类型只有在学科启用分组时才可选
5. 课程发布后自动发送通知给相关学员

#### 3.3.5 日历功能详细说明

**月视图：**
- 每个日期格子显示当天的课程标题
- 多个课程时叠加显示，超出部分显示 "+N 更多"
- 不同学科用不同颜色区分
- 点击日期可快速创建课程
- 点击课程可跳转到详情页

**周视图：**
- 横轴为一周七天，纵轴为时间段（如 8:00-22:00）
- 显示课程时间块，包含标题和时间
- 支持拖拽调整课程时间
- 支持拖拽调整课程时长

---

### 3.4 小组管理模块

#### 3.4.1 功能描述

管理学科内的学员小组，支持学员自主建组和申请加入。

#### 3.4.2 功能列表

| 功能编号 | 功能名称 | 描述 | 操作角色 |
|----------|----------|------|----------|
| G-001 | 创建小组 | 学员创建小组并成为组长 | 学员 |
| G-002 | 申请加入 | 学员申请加入现有小组 | 学员 |
| G-003 | 审批申请 | 组长审批入组申请 | 学员(组长) |
| G-004 | 移除成员 | 组长移除小组成员 | 学员(组长) |
| G-005 | 退出小组 | 成员主动退出小组 | 学员 |
| G-006 | 解散小组 | 组长解散整个小组 | 学员(组长) |
| G-007 | 查看小组列表 | 查看学科下所有小组 | 所有角色 |
| G-008 | 查看小组详情 | 查看小组成员信息 | 所有角色 |
| G-009 | 查看我的小组 | 学员查看自己所在的小组 | 学员 |

#### 3.4.3 小组属性

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| subjectId | Long | ✓ | 所属学科 ID |
| name | String | ✓ | 小组名称，最长 100 字符 |
| leaderId | Long | ✓ | 组长用户 ID |

#### 3.4.4 小组成员属性

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| groupId | Long | ✓ | 所属小组 ID |
| userId | Long | ✓ | 成员用户 ID |
| status | String | ✓ | 状态：PENDING/APPROVED/REJECTED |

#### 3.4.5 业务规则

1. 只有学科启用分组，学员才能创建/加入小组
2. 学员在同一学科只能属于一个小组
3. 创建小组时，创建者自动成为组长和成员（状态为 APPROVED）
4. 小组人数不能超过学科设置的 maxMembers
5. 小组人数不能少于学科设置的 minMembers（解散时除外）
6. 组长不能退出小组，必须先转让组长或解散小组
7. 解散小组需要组长确认，解散后所有成员状态变为离开
8. 未加入小组的学员无法查看和提交小组作业
9. 申请被拒绝后可以重新申请

---

### 3.5 作业提交模块

#### 3.5.1 功能描述

学员提交作业文件，支持个人作业和小组作业两种模式。

#### 3.5.2 功能列表

| 功能编号 | 功能名称 | 描述 | 操作角色 |
|----------|----------|------|----------|
| H-001 | 提交作业 | 学员上传作业文件 | 学员 |
| H-002 | 重新提交 | 学员覆盖之前的提交 | 学员 |
| H-003 | 查看提交 | 查看作业提交详情 | 所有角色 |
| H-004 | 查看提交列表 | 教员查看所有提交 | 管理员、教员 |
| H-005 | 下载作业 | 下载提交的作业文件 | 管理员、教员 |
| H-006 | 查看提交状态 | 看板形式展示提交情况 | 管理员、教员 |

#### 3.5.3 作业提交属性

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| lessonId | Long | ✓ | 关联的课程 ID |
| submitterId | Long | ✓ | 提交者用户 ID |
| groupId | Long | ✗ | 小组 ID（小组作业时） |
| filePath | String | ✓ | 文件存储路径 |
| submitTime | DateTime | ✓ | 提交时间 |

#### 3.5.4 业务规则

1. **个人作业**：每个学员独立提交
2. **小组作业**：小组任一成员提交，组内共享查看
3. 截止时间后：
   - 允许补交：可以继续提交，标记为"迟交"
   - 不允许补交：无法提交
4. 重新提交会覆盖之前的文件，保留最新一次
5. 文件大小限制：单文件 100MB

#### 3.5.5 文件上传规则

**支持的上传方式：**
1. 单文件上传：直接上传单个文件
2. 多文件上传：同时选择多个文件
3. 文件夹上传：上传整个文件夹（保持目录结构）
4. ZIP 压缩包：自动解压并保持目录结构

**文件存储结构：**
```
/uploads/
  └── {学科ID}/
      └── {课程ID}/
          └── {小组ID 或 学员ID}/
              └── [作业文件和目录]
```

**支持的文件类型：**
- 代码文件：.java, .js, .ts, .py, .go, .c, .cpp, .h, .cs, .rb, .php, .swift, .kt 等
- 配置文件：.json, .xml, .yml, .yaml, .properties, .ini, .toml 等
- 文档文件：.md, .txt, .pdf, .doc, .docx
- 压缩文件：.zip（自动解压）

---

### 3.6 代码查看器模块

#### 3.6.1 功能描述

类似 GitHub 的代码浏览体验，支持语法高亮和行内评论。

#### 3.6.2 功能列表

| 功能编号 | 功能名称 | 描述 | 操作角色 |
|----------|----------|------|----------|
| C-001 | 目录树展示 | 左侧展示文件目录结构 | 所有角色 |
| C-002 | 代码预览 | 右侧展示代码内容 | 所有角色 |
| C-003 | 语法高亮 | 根据文件类型显示语法高亮 | 所有角色 |
| C-004 | 行号显示 | 显示代码行号 | 所有角色 |
| C-005 | 代码搜索 | 在当前文件中搜索 | 所有角色 |
| C-006 | 添加行评论 | 在指定行添加评论 | 管理员、教员 |
| C-007 | 查看行评论 | 查看代码行的评论 | 所有角色 |
| C-008 | 删除行评论 | 删除自己添加的评论 | 管理员、教员 |

#### 3.6.3 代码评论属性

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| submissionId | Long | ✓ | 关联的作业提交 ID |
| filePath | String | ✓ | 文件相对路径 |
| lineNumber | Integer | ✓ | 评论所在行号 |
| content | Text | ✓ | 评论内容 |
| commenterId | Long | ✓ | 评论者用户 ID |

#### 3.6.4 界面布局

```
+----------------+------------------+----------------------------------------+
|                |                  |                                        |
|  待批改作业    |   目录树         |              代码编辑器                 |
|  (可折叠)      |   (可折叠)       |                                        |
|                |                  |   1 | public class Main {              |
|  📋 张三       |   📁 src/        |   2 |     public static void main... |  💬
|  📋 李四       |     📄 Main.java |   3 |         System.out.println...  |
|  📋 王五       |     📄 Utils.java|   4 |     }                          |
|  ...           |   📁 test/       |   5 | }                              |
|                |                  |                                        |
+----------------+------------------+----------------------------------------+
|  等级: [A] [B] [C] [D]    评语: [________________]    [提交评分] [退回修改] |
+----------------------------------------------------------------------------+
```

**说明：**
- 待批改作业列表支持折叠/展开，折叠后可获得更大的代码查看空间
- 学员上传的作业按目录结构展示，支持多文件浏览
- 评分使用 A/B/C/D 等级按钮选择，而非数值输入
- 「提交评分」为主操作按钮，「退回修改」为次要操作

#### 3.6.5 功能特性

1. **Monaco Editor 集成**
   - VS Code 同款编辑器体验
   - 自动识别编程语言
   - 代码折叠功能
   - 小地图导航

2. **目录树功能**
   - 树形结构展示
   - 文件图标区分类型
   - 单击选择，双击打开
   - 支持展开/折叠文件夹

3. **行内评论**
   - 点击行号区域添加评论
   - 有评论的行显示标记图标
   - 支持 Markdown 格式
   - 评论时间和评论者显示

---

### 3.7 评分管理模块

#### 3.7.1 功能描述

教员对学员提交的作业进行评分和评价。

#### 3.7.2 功能列表

| 功能编号 | 功能名称 | 描述 | 操作角色 |
|----------|----------|------|----------|
| E-001 | 作业评分 | 对提交的作业打分 | 管理员、教员 |
| E-002 | 添加评语 | 添加文字评价 | 管理员、教员 |
| E-003 | 修改评分 | 修改已有评分 | 管理员、教员 |
| E-004 | 查看评分 | 学员查看自己的评分 | 所有角色 |
| E-005 | 成绩汇总-学员 | 学员查看所有课程成绩 | 学员 |
| E-006 | 成绩汇总-教员 | 按学员/小组汇总成绩 | 管理员、教员 |
| E-007 | 导出成绩 | 导出成绩为 Excel | 管理员、教员 |

#### 3.7.3 评分属性

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| submissionId | Long | ✓ | 关联的作业提交 ID |
| grade | String | ✓ | 等级：A/B/C/D |
| comment | Text | ✗ | 文字评语 |
| graderId | Long | ✓ | 评分者用户 ID |
| gradeTime | DateTime | ✓ | 评分时间 |

#### 3.7.4 等级定义

| 等级 | 含义 | 说明 |
|:----:|------|------|
| A | 优秀 | 完成度高，代码质量好 |
| B | 良好 | 基本完成，有小问题 |
| C | 及格 | 勉强完成，问题较多 |
| D | 不及格 | 未完成或质量很差 |

#### 3.7.5 业务规则

1. 每个提交只能有一个评分记录
2. 可以修改评分，保留最新结果
3. 小组作业评分对组内所有成员可见
4. 评分完成后自动发送通知给学员
5. 成绩导出包含：学员姓名、学科、课程、等级、评语、提交时间、评分时间

#### 3.7.6 成绩汇总视图

**说明：** 每次培训只有一门课程，因此成绩汇总页面无需学科选择器。

**学员视角：**
```
+------------------+------------+--------+----------+
| 课程             | 提交时间    | 等级   | 评语     |
+------------------+------------+--------+----------+
| Java基础 第1课   | 2024-01-15 | A      | 很好     |
| Java基础 第2课   | 2024-01-22 | B      | 需改进   |
| ...              | ...        | ...    | ...      |
+------------------+------------+--------+----------+
```

**教员视角：**

页面顶部显示统计卡片：参训人数、小组数量、平均分、完成率

支持视图切换：「按小组」/「按学员」

```
+----------+--------+--------+--------+--------+--------+--------+----------+
| 小组/学员 | 第1课  | 第2课  | 第3课  | 第4课  | 第5课  | 总分   | 平均分   |
+----------+--------+--------+--------+--------+--------+--------+----------+
| 第一组   | A      | A      | B      | A      | A      | 优秀   | 4.6      |
| 第二组   | B      | B      | C      | B      | A      | 良好   | 3.6      |
| 第三组   | A      | A      | A      | A      | A      | 优秀   | 5.0      |
| 第四组   | D      | C      | C      | D      | C      | 及格   | 2.0      |
| ...      | ...    | ...    | ...    | ...    | ...    | ...    | ...      |
+----------+--------+--------+--------+--------+--------+--------+----------+
```

分数颜色说明：
- 绿色：A/B（优良）
- 黄色：C（及格）
- 红色：D（不及格）

---

### 3.8 通知管理模块

#### 3.8.1 功能描述

系统自动发送站内通知，确保用户及时获取重要信息。

#### 3.8.2 功能列表

| 功能编号 | 功能名称 | 描述 | 操作角色 |
|----------|----------|------|----------|
| N-001 | 查看通知列表 | 查看所有通知 | 所有角色 |
| N-002 | 查看通知详情 | 查看单条通知内容 | 所有角色 |
| N-003 | 标记已读 | 将通知标记为已读 | 所有角色 |
| N-004 | 全部标记已读 | 批量标记所有为已读 | 所有角色 |
| N-005 | 删除通知 | 删除单条通知 | 所有角色 |
| N-006 | 获取未读数量 | 获取未读通知数量 | 所有角色 |

#### 3.8.3 通知属性

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| userId | Long | ✓ | 接收者用户 ID |
| title | String | ✓ | 通知标题 |
| content | Text | ✗ | 通知详细内容 |
| type | String | ✓ | 通知类型 |
| isRead | Boolean | ✓ | 是否已读，默认否 |

#### 3.8.4 通知类型

| 类型 | 说明 | 接收者 | 触发时机 |
|------|------|--------|----------|
| LESSON_PUBLISHED | 新课程发布 | 学科内学员 | 课程创建时 |
| HOMEWORK_DEADLINE | 作业截止提醒 | 未提交学员 | 截止前 24 小时 |
| GRADE_COMPLETED | 评分完成 | 提交者/小组成员 | 评分保存时 |
| GROUP_APPLICATION | 入组申请 | 组长 | 有人申请时 |
| GROUP_APPROVED | 申请通过 | 申请者 | 组长批准时 |
| GROUP_REJECTED | 申请拒绝 | 申请者 | 组长拒绝时 |

#### 3.8.5 通知模板

**新课程发布：**
```
标题：[{学科名称}] 新课程发布
内容：课程《{课程标题}》已发布，上课时间：{lessonTime}
```

**作业截止提醒：**
```
标题：作业即将截止
内容：课程《{课程标题}》的作业将于 {deadline} 截止，请及时提交。
```

**评分完成：**
```
标题：作业已批改
内容：您提交的《{课程标题}》作业已批改，等级：{grade}
```

**入组申请：**
```
标题：新的入组申请
内容：{申请者姓名} 申请加入小组「{小组名称}」，请及时处理。
```

---

## 4. 页面结构与路由

### 4.1 路由设计

```
/ (首页/登录页)
│
├── /dashboard                    # 工作台（按角色展示不同内容）
│
├── /subjects                     # 学科列表
│   ├── /subjects/create          # 创建学科
│   ├── /subjects/:id             # 学科详情（含日历视图）
│   ├── /subjects/:id/edit        # 编辑学科
│   └── /subjects/:id/students    # 学科学员管理
│
├── /lessons                      # 课程管理
│   ├── /lessons/create           # 创建课程
│   ├── /lessons/:id              # 课程详情
│   └── /lessons/:id/edit         # 编辑课程
│
├── /submissions                  # 作业管理
│   ├── /submissions/board        # 提交状态看板
│   └── /submissions/:id          # 作业详情（代码查看器）
│
├── /groups                       # 小组管理（学员）
│   ├── /groups/create            # 创建小组
│   └── /groups/:id               # 小组详情
│
├── /grades                       # 成绩汇总
│
├── /notifications                # 消息中心
│
├── /profile                      # 个人中心
│   └── /profile/password         # 修改密码
│
└── /admin                        # 系统管理（仅管理员）
    └── /admin/users              # 用户管理
        ├── /admin/users/create   # 创建用户
        └── /admin/users/:id/edit # 编辑用户
```

### 4.2 页面布局

**整体布局：**
```
+----------------------------------------------------------+
|                        Header                             |
|  Logo   学科下拉   搜索框          通知图标  用户头像下拉  |
+----------------------------------------------------------+
|        |                                                  |
|        |                                                  |
|  侧边  |                    主内容区                       |
|  导航  |                                                  |
|        |                                                  |
|        |                                                  |
+--------+--------------------------------------------------+
```

**侧边导航菜单：**
- 工作台
- 学科管理（教员/管理员）/ 我的学科（学员）
- 作业管理（教员/管理员）/ 我的作业（学员）
- 小组管理（学员）
- 成绩查询
- 消息中心
- 系统管理（仅管理员）
  - 用户管理

---

## 5. 数据模型

### 5.1 ER 图

```
                    +------------+
                    |   t_user   |
                    +------------+
                          |
          +---------------+---------------+
          |               |               |
          v               v               v
    +----------+    +---------+    +-------------+
    | t_subject|<---| t_lesson|    | t_notification|
    +----------+    +---------+    +-------------+
          |               |
          |               v
          |         +------------+
          |         | t_submission|
          |         +------------+
          |               |
          v               v
    +---------+     +---------+     +-------------+
    | t_group |     | t_grade |     |t_code_comment|
    +---------+     +---------+     +-------------+
          |
          v
    +---------------+
    | t_group_member|
    +---------------+

    +------------------+
    | t_subject_student|
    +------------------+
```

### 5.2 表结构定义

#### 5.2.1 用户表 (t_user)

```sql
CREATE TABLE t_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,           -- 登录用户名
    password VARCHAR(100) NOT NULL,                 -- 加密后的密码
    role VARCHAR(20) NOT NULL,                      -- 角色：ADMIN/TEACHER/STUDENT
    name VARCHAR(50) NOT NULL,                      -- 真实姓名
    email VARCHAR(100),                             -- 电子邮箱
    avatar VARCHAR(200),                            -- 头像路径
    version INT NOT NULL DEFAULT 0,                 -- 乐观锁
    del_flag TINYINT NOT NULL DEFAULT 0,            -- 删除标志
    create_by VARCHAR(64) NOT NULL DEFAULT '',      -- 创建者
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',      -- 更新者
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE UNIQUE INDEX UK_user_username ON t_user(username);
```

#### 5.2.2 学科表 (t_subject)

```sql
CREATE TABLE t_subject (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,                     -- 学科名称
    description TEXT,                               -- 学科介绍（Markdown）
    is_grouped TINYINT NOT NULL DEFAULT 0,          -- 是否启用分组
    min_members INT NOT NULL DEFAULT 1,             -- 小组最少人数
    max_members INT NOT NULL DEFAULT 1,             -- 小组最多人数
    start_date DATE,                                -- 开始日期
    end_date DATE,                                  -- 结束日期
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE INDEX IDX_subject_name ON t_subject(name);
```

#### 5.2.3 课程表 (t_lesson)

```sql
CREATE TABLE t_lesson (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,                    -- 所属学科ID
    title VARCHAR(200) NOT NULL,                    -- 课程标题
    content TEXT,                                   -- 教学内容（Markdown）
    lesson_time DATETIME NOT NULL,                  -- 上课时间
    homework_desc TEXT,                             -- 作业要求（Markdown）
    submit_type VARCHAR(20) NOT NULL DEFAULT 'PERSONAL', -- PERSONAL/GROUP
    deadline DATETIME,                              -- 作业截止时间
    allow_late TINYINT NOT NULL DEFAULT 0,          -- 是否允许补交
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE INDEX IDX_lesson_subject ON t_lesson(subject_id);
CREATE INDEX IDX_lesson_time ON t_lesson(lesson_time);
```

#### 5.2.4 小组表 (t_group)

```sql
CREATE TABLE t_group (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,                    -- 所属学科ID
    name VARCHAR(100) NOT NULL,                     -- 小组名称
    leader_id INTEGER NOT NULL,                     -- 组长用户ID
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE INDEX IDX_group_subject ON t_group(subject_id);
CREATE INDEX IDX_group_leader ON t_group(leader_id);
```

#### 5.2.5 小组成员表 (t_group_member)

```sql
CREATE TABLE t_group_member (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id INTEGER NOT NULL,                      -- 所属小组ID
    user_id INTEGER NOT NULL,                       -- 成员用户ID
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING/APPROVED/REJECTED
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE INDEX IDX_member_group ON t_group_member(group_id);
CREATE INDEX IDX_member_user ON t_group_member(user_id);
CREATE UNIQUE INDEX UK_member_group_user ON t_group_member(group_id, user_id);
```

#### 5.2.6 作业提交表 (t_submission)

```sql
CREATE TABLE t_submission (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    lesson_id INTEGER NOT NULL,                     -- 关联课程ID
    submitter_id INTEGER NOT NULL,                  -- 提交者用户ID
    group_id INTEGER,                               -- 小组ID（小组作业时）
    file_path VARCHAR(500) NOT NULL,                -- 文件存储路径
    submit_time DATETIME NOT NULL,                  -- 提交时间
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE INDEX IDX_submission_lesson ON t_submission(lesson_id);
CREATE INDEX IDX_submission_submitter ON t_submission(submitter_id);
CREATE INDEX IDX_submission_group ON t_submission(group_id);
```

#### 5.2.7 评分表 (t_grade)

```sql
CREATE TABLE t_grade (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL,                 -- 关联提交ID
    grade VARCHAR(10) NOT NULL,                     -- 等级：A/B/C/D
    comment TEXT,                                   -- 文字评语
    grader_id INTEGER NOT NULL,                     -- 评分者用户ID
    grade_time DATETIME NOT NULL,                   -- 评分时间
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE UNIQUE INDEX UK_grade_submission ON t_grade(submission_id);
CREATE INDEX IDX_grade_grader ON t_grade(grader_id);
```

#### 5.2.8 代码评论表 (t_code_comment)

```sql
CREATE TABLE t_code_comment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL,                 -- 关联提交ID
    file_path VARCHAR(500) NOT NULL,                -- 文件相对路径
    line_number INT NOT NULL,                       -- 评论所在行号
    content TEXT NOT NULL,                          -- 评论内容
    commenter_id INTEGER NOT NULL,                  -- 评论者用户ID
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE INDEX IDX_comment_submission ON t_code_comment(submission_id);
CREATE INDEX IDX_comment_file ON t_code_comment(submission_id, file_path);
```

#### 5.2.9 通知表 (t_notification)

```sql
CREATE TABLE t_notification (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,                       -- 接收者用户ID
    title VARCHAR(200) NOT NULL,                    -- 通知标题
    content TEXT,                                   -- 通知内容
    type VARCHAR(50) NOT NULL,                      -- 通知类型
    is_read TINYINT NOT NULL DEFAULT 0,             -- 是否已读
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE INDEX IDX_notification_user ON t_notification(user_id);
CREATE INDEX IDX_notification_read ON t_notification(user_id, is_read);
```

#### 5.2.10 学员-学科关联表 (t_subject_student)

```sql
CREATE TABLE t_subject_student (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER NOT NULL,                    -- 学科ID
    student_id INTEGER NOT NULL,                    -- 学员用户ID
    version INT NOT NULL DEFAULT 0,
    del_flag TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01',
    update_by VARCHAR(64) NOT NULL DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT '1970-01-01 08:00:01'
);

-- 索引
CREATE INDEX IDX_ss_subject ON t_subject_student(subject_id);
CREATE INDEX IDX_ss_student ON t_subject_student(student_id);
CREATE UNIQUE INDEX UK_ss_subject_student ON t_subject_student(subject_id, student_id);
```

---

## 6. API 接口设计

### 6.1 接口规范

**基础路径：** `/api`

**请求格式：** JSON

**响应格式：**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "data": { ... }
}
```

**错误响应：**
```json
{
    "success": false,
    "code": 400,
    "message": "错误描述",
    "data": null
}
```

**分页响应：**
```json
{
    "success": true,
    "code": 200,
    "message": "success",
    "data": {
        "records": [...],
        "total": 100,
        "current": 1,
        "size": 10,
        "pages": 10
    }
}
```

**认证方式：** Bearer Token
```
Authorization: Bearer <jwt_token>
```

### 6.2 接口列表

#### 6.2.1 认证接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|:----:|
| POST | /auth/login | 用户登录 | ✗ |
| POST | /auth/logout | 用户登出 | ✓ |
| GET | /auth/me | 获取当前用户信息 | ✓ |

#### 6.2.2 用户管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /users | 用户列表（分页） | ADMIN |
| GET | /users/{id} | 用户详情 | ADMIN |
| POST | /users | 创建用户 | ADMIN |
| PUT | /users/{id} | 更新用户 | ADMIN |
| DELETE | /users/{id} | 删除用户 | ADMIN |
| PUT | /users/profile | 更新个人信息 | ALL |
| PUT | /users/password | 修改密码 | ALL |
| POST | /users/avatar | 上传头像 | ALL |

#### 6.2.3 学科管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /subjects | 学科列表 | ALL |
| GET | /subjects/{id} | 学科详情 | ALL |
| POST | /subjects | 创建学科 | ADMIN/TEACHER |
| PUT | /subjects/{id} | 更新学科 | ADMIN/TEACHER |
| DELETE | /subjects/{id} | 删除学科 | ADMIN/TEACHER |
| GET | /subjects/{id}/students | 学科学员列表 | ADMIN/TEACHER |
| POST | /subjects/{id}/students | 添加学员 | ADMIN/TEACHER |
| DELETE | /subjects/{id}/students/{studentId} | 移除学员 | ADMIN/TEACHER |

#### 6.2.4 课程管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /lessons | 课程列表 | ALL |
| GET | /lessons/{id} | 课程详情 | ALL |
| POST | /lessons | 创建课程 | ADMIN/TEACHER |
| PUT | /lessons/{id} | 更新课程 | ADMIN/TEACHER |
| DELETE | /lessons/{id} | 删除课程 | ADMIN/TEACHER |
| GET | /lessons/calendar | 日历视图数据 | ALL |
| PUT | /lessons/{id}/time | 调整课程时间 | ADMIN/TEACHER |

#### 6.2.5 小组管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /groups | 小组列表 | ALL |
| GET | /groups/{id} | 小组详情 | ALL |
| POST | /groups | 创建小组 | STUDENT |
| DELETE | /groups/{id} | 解散小组 | STUDENT(组长) |
| POST | /groups/{id}/apply | 申请加入 | STUDENT |
| PUT | /groups/{id}/approve/{memberId} | 批准申请 | STUDENT(组长) |
| PUT | /groups/{id}/reject/{memberId} | 拒绝申请 | STUDENT(组长) |
| DELETE | /groups/{id}/members/{memberId} | 移除成员 | STUDENT(组长) |
| DELETE | /groups/{id}/leave | 退出小组 | STUDENT |
| GET | /groups/my | 我的小组 | STUDENT |

#### 6.2.6 作业提交接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /submissions | 提交列表 | ALL |
| GET | /submissions/{id} | 提交详情 | ALL |
| POST | /submissions | 提交作业 | STUDENT |
| GET | /submissions/{id}/files | 获取文件列表 | ALL |
| GET | /submissions/{id}/files/** | 获取文件内容 | ALL |
| GET | /submissions/{id}/download | 下载作业 | ADMIN/TEACHER |
| GET | /submissions/board | 提交状态看板 | ADMIN/TEACHER |

#### 6.2.7 评分管理接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /grades | 添加/更新评分 | ADMIN/TEACHER |
| GET | /grades/submission/{submissionId} | 获取提交的评分 | ALL |
| GET | /grades/my | 我的成绩列表 | STUDENT |
| GET | /grades/summary | 成绩汇总 | ADMIN/TEACHER |
| GET | /grades/export | 导出成绩Excel | ADMIN/TEACHER |

#### 6.2.8 代码评论接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /code-comments/submission/{submissionId} | 获取提交的所有评论 | ALL |
| POST | /code-comments | 添加评论 | ADMIN/TEACHER |
| PUT | /code-comments/{id} | 更新评论 | ADMIN/TEACHER |
| DELETE | /code-comments/{id} | 删除评论 | ADMIN/TEACHER |

#### 6.2.9 通知接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /notifications | 通知列表 | ALL |
| GET | /notifications/unread-count | 未读数量 | ALL |
| PUT | /notifications/{id}/read | 标记已读 | ALL |
| PUT | /notifications/read-all | 全部标记已读 | ALL |
| DELETE | /notifications/{id} | 删除通知 | ALL |

---

## 7. 非功能性需求

### 7.1 性能需求

| 指标 | 要求 |
|------|------|
| 页面加载时间 | < 3 秒 |
| API 响应时间 | < 500ms (95th percentile) |
| 并发用户数 | 支持 50 用户同时在线 |
| 文件上传 | 单文件最大 100MB |

### 7.2 安全需求

1. **认证安全**
   - JWT Token 有效期 24 小时
   - 密码使用 BCrypt 加密（cost factor = 10）
   - 登录失败 5 次后锁定账号 15 分钟

2. **接口安全**
   - 所有接口（除登录外）需要认证
   - 基于角色的访问控制 (RBAC)
   - 防止 SQL 注入、XSS 攻击

3. **文件安全**
   - 限制上传文件类型
   - 文件名 sanitization
   - 防止路径遍历攻击

### 7.3 可用性需求

1. 系统支持 Windows/Linux 部署
2. 支持主流浏览器：Chrome、Firefox、Edge、Safari
3. 响应式设计，支持 1280px 以上分辨率

### 7.4 数据需求

1. 使用 SQLite 单文件数据库，便于备份
2. 支持数据导出（成绩 Excel）
3. 逻辑删除，保留历史数据

---

## 8. 实施计划

### 阶段一：基础框架（Phase 1）

**目标：** 搭建项目基础架构，完成用户认证

**交付物：**
- 后端 Spring Boot 项目框架
- 前端 Vue 3 项目框架
- SQLite 数据库配置
- 用户登录/登出功能
- 基础用户管理 CRUD

### 阶段二：核心业务（Phase 2）

**目标：** 完成学科、课程、小组核心功能

**交付物：**
- 学科 CRUD + 学员分配
- 课程 CRUD + 日历视图
- 小组创建、申请、审批功能
- 作业文件上传

### 阶段三：评分与展示（Phase 3）

**目标：** 完成代码查看器和评分功能

**交付物：**
- Monaco Editor 代码查看器
- 行内评论功能
- 作业评分功能
- 成绩汇总与导出

### 阶段四：完善功能（Phase 4）

**目标：** 完成通知系统和细节优化

**交付物：**
- 站内通知系统
- 定时任务（截止提醒）
- 权限细化
- UI/UX 优化

---

## 9. 验收标准

### 9.1 功能验收

1. [ ] 管理员可以创建、编辑、删除用户
2. [ ] 教员可以创建学科和课程
3. [ ] 学员可以创建小组、申请加入小组
4. [ ] 组长可以审批入组申请
5. [ ] 学员可以提交作业（文件上传）
6. [ ] 教员可以查看代码、添加评论、评分
7. [ ] 学员可以查看自己的成绩
8. [ ] 用户可以收到相关通知

### 9.2 技术验收

1. [ ] 代码符合编码规范
2. [ ] 单元测试覆盖率 > 80%
3. [ ] API 文档完整
4. [ ] 无已知安全漏洞
5. [ ] 性能满足需求指标

---

## 附录

### A. 术语表

| 术语 | 说明 |
|------|------|
| 学科 (Subject) | 一个完整的培训课程，如 "Java 基础班" |
| 课程 (Lesson) | 学科下的单次上课，如 "第1课：Java 环境配置" |
| 小组 (Group) | 学员组成的学习小组 |
| 组长 (Leader) | 小组的创建者和管理者 |
| 提交 (Submission) | 学员提交的作业 |
| 评分 (Grade) | 教员对作业的评价 |

### B. 参考资料

1. Vue 3 官方文档：https://vuejs.org/
2. Element Plus：https://element-plus.org/
3. Monaco Editor：https://microsoft.github.io/monaco-editor/
4. Spring Boot 3：https://spring.io/projects/spring-boot
5. MyBatis-Plus：https://baomidou.com/
