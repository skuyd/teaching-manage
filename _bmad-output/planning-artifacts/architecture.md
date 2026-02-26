---
stepsCompleted: ['step-01-init', 'step-02-context', 'step-03-starter', 'step-04-decisions', 'step-05-patterns', 'step-06-structure', 'step-07-validation', 'step-08-complete']
status: 'complete'
completedAt: '2026-02-23'
inputDocuments:
  - path: '_bmad-output/planning-artifacts/prd.md'
    type: 'prd'
    description: 'Product Requirements Document'
  - path: 'docs/architecture.md'
    type: 'existing-architecture'
    description: '现有架构设计文档'
  - path: 'docs/project-overview.md'
    type: 'overview'
    description: '项目概述'
  - path: 'docs/api-contracts.md'
    type: 'api'
    description: 'API接口文档'
  - path: 'docs/data-models.md'
    type: 'data-model'
    description: '数据模型'
  - path: 'docs/development-guide.md'
    type: 'dev-guide'
    description: '开发指南'
  - path: 'docs/index.md'
    type: 'index'
    description: '文档索引'
workflowType: 'architecture'
workflowGoal: 'system-improvement'
project_name: 'teaching-manage'
user_name: 'Prola'
date: '2026-02-23'
classification:
  projectType: 'Web App (SPA + REST API)'
  domain: 'EdTech'
  complexity: 'medium'
  projectContext: 'brownfield'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

47 项功能需求，涵盖用户认证、仪表盘、学科管理、课程管理、作业提交、评分评论、小组协作、通知系统、UI/UX 一致性。

核心修复需求：
- FR8: Dashboard 正常加载（修复 404）
- FR14: 学科详情页正常显示
- FR31: 评分保存正常（修复 500）
- FR44-47: UI 一致性要求

**Non-Functional Requirements:**

26 项非功能需求，关键指标：
- 性能：API < 500ms, FCP < 3s
- 安全：JWT 24h, 限流 100/min/IP
- 可靠性：零 500/404 错误
- 兼容性：Chrome 90+, 桌面优先

**Scale & Complexity:**

| 维度 | 评估 |
|------|------|
| Primary domain | Full-stack Web (SPA + REST API) |
| Complexity level | Medium (brownfield improvement) |
| Architectural components | 无新增，现有组件修复与优化 |

### Technical Constraints & Dependencies

| 约束 | 说明 |
|------|------|
| 数据库 | SQLite，不可更换，适合 30-100 用户 |
| 后端 | Spring Boot 3.2.2 + MyBatis-Plus + Java 17 |
| 前端 | Vue 3 + Element Plus + Pinia + TypeScript |
| 部署 | 单机，本地文件存储 (./uploads/) |
| 兼容性 | 必须保持现有 API 契约向后兼容 |

### Cross-Cutting Concerns Identified

| 关注点 | 影响范围 | 优先级 |
|--------|----------|--------|
| **错误处理一致性** | 前后端统一错误处理机制 | P0 |
| **UI 主题系统** | 全局 CSS 变量，组件一致性 | P1 |
| **调试与日志** | 改进错误追踪能力 | P1 |
| **数据流验证** | API 响应与前端渲染的数据契约 | P0 |

### Known Issues Architecture Impact

| 问题 | 架构层 | 可能根因 |
|------|--------|----------|
| Dashboard 404 | 前端路由 / API | 路由配置或端点缺失 |
| 学科详情无法显示 | 数据流 | API 数据结构或前端状态管理 |
| 评分功能 500 | 后端服务层 | Service 异常处理 |
| UI 不一致 | 前端样式 | 缺少全局 CSS 变量体系 |

## Starter Template Evaluation

### Primary Technology Domain

Full-stack Web Application (SPA + REST API) - **Brownfield Project**

### Existing Technology Stack (Established)

本项目已有完整的技术栈，无需引入新的 Starter Template。

**Backend:**

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行时 |
| Spring Boot | 3.2.2 | 应用框架 |
| MyBatis-Plus | 3.5.5 | ORM |
| SQLite | 3.45.1 | 数据库 |
| jjwt | 0.12.3 | JWT 认证 |
| Apache POI | 5.2.5 | Excel 导出 |

**Frontend:**

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4.0 | UI 框架 |
| TypeScript | 5.3.3 | 类型系统 |
| Vite | 5.0.10 | 构建工具 |
| Element Plus | 2.4.4 | UI 组件库 |
| Pinia | 2.1.7 | 状态管理 |
| Monaco Editor | 0.45.0 | 代码编辑器 |

### Architectural Decisions Already Made

| 决策领域 | 已确立方案 |
|----------|------------|
| **语言配置** | Java 17 (后端), TypeScript 5.3.3 (前端) |
| **样式方案** | Element Plus 组件库 + 自定义 CSS |
| **测试框架** | JUnit (后端), Vitest (前端) |
| **构建工具** | Maven (后端), Vite (前端) |
| **代码组织** | 分层架构 (Controller/Service/Mapper) |
| **状态管理** | Pinia Store |
| **API 通信** | Axios + JWT Bearer Token |
| **路由** | Vue Router + 路由守卫 |
| **权限控制** | 后端 @PreAuthorize + 前端 v-role 指令 |

### Improvement Focus

本次架构工作聚焦于：

1. **错误处理机制完善** - 统一前后端错误处理
2. **UI 主题系统规范化** - 全局 CSS 变量体系
3. **数据流一致性验证** - API 契约与前端渲染

## Core Architectural Decisions

### Decision 1: Error Handling Strategy

**选择：B - 增强现有机制**

**决策理由：**
- 现有 `GlobalExceptionHandler` 已提供基础错误处理
- 增量改进风险最低，向后兼容
- 可在现有架构上快速迭代

**实现方案：**

| 层级 | 增强内容 |
|------|----------|
| **后端** | 扩展 `GlobalExceptionHandler`，增加错误码映射表，统一异常分类 |
| **前端** | 增强 `request.ts` 拦截器，添加错误码到消息的映射，统一 ElMessage 提示 |
| **契约** | 定义标准错误响应结构：`{ code, success, message, data?, errors? }` |

**错误码规范：**

| 范围 | 含义 |
|------|------|
| 200 | 成功 |
| 400-499 | 客户端错误（验证失败、权限不足等） |
| 500-599 | 服务端错误（内部异常、数据库错误等） |

**关键文件：**
- `backend/src/main/java/com/teaching/exception/GlobalExceptionHandler.java`
- `frontend/src/utils/request.ts`

---

### Decision 2: UI Consistency Implementation

**选择：A - CSS 变量方案**

**决策理由：**
- 原生 CSS 特性，无额外依赖
- 运行时可切换主题（未来扩展性）
- 与 Element Plus 变量系统兼容
- 浏览器支持良好（Chrome 90+）

**实现方案：**

**1. 创建全局 CSS 变量文件：**

```css
/* frontend/src/styles/variables.css */
:root {
  /* 主色调 */
  --color-primary: #409eff;
  --color-success: #67c23a;
  --color-warning: #e6a23c;
  --color-danger: #f56c6c;
  --color-info: #909399;

  /* 文字颜色 */
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #c0c4cc;

  /* 边框 */
  --border-color: #dcdfe6;
  --border-radius: 4px;

  /* 间距 */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;

  /* 阴影 */
  --shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  --shadow-dark: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
}
```

**2. 组件样式统一规则：**

| 组件类型 | 样式规范 |
|----------|----------|
| 页面容器 | `padding: var(--spacing-lg)` |
| 卡片 | `border-radius: var(--border-radius)`, `box-shadow: var(--shadow-light)` |
| 表格 | 使用 Element Plus 默认样式 |
| 表单 | 统一标签宽度 `100px`，间距 `var(--spacing-md)` |
| 按钮组 | 间距 `var(--spacing-sm)` |

**关键文件：**
- `frontend/src/styles/variables.css` (新建)
- `frontend/src/styles/common.css` (新建)
- `frontend/src/main.ts` (导入样式)

---

### Decision 3: Debugging & Logging Improvement

**选择：B - 增强日志**

**决策理由：**
- 无需引入新依赖
- 利用现有 SLF4J/Logback 基础设施
- 前端 Error Boundary 提升用户体验
- 结构化日志便于问题排查

**实现方案：**

**后端日志增强：**

```yaml
# application.yml
logging:
  level:
    com.teaching: DEBUG
    com.teaching.controller: INFO
    com.teaching.service: DEBUG
    com.teaching.exception: WARN
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**日志规范：**

| 场景 | 日志级别 | 内容 |
|------|----------|------|
| API 请求入口 | INFO | `[{method}] {path} - userId: {id}` |
| 业务异常 | WARN | `{exceptionType}: {message}` |
| 系统异常 | ERROR | 完整堆栈 + 请求上下文 |
| 关键业务操作 | INFO | `{action} - entityId: {id}, operator: {user}` |

**前端错误边界：**

```typescript
// ErrorBoundary.vue - 捕获组件渲染错误
// 显示友好错误页面，记录错误到控制台
// 提供"重试"按钮
```

**关键文件：**
- `backend/src/main/resources/application.yml`
- `frontend/src/components/ErrorBoundary.vue` (新建)

---

### Decision Summary

| 决策 | 选择 | 工作量 | 风险 |
|------|------|--------|------|
| 错误处理 | B - 增强现有 | 中等 | 低 |
| UI 一致性 | A - CSS 变量 | 中等 | 低 |
| 调试/日志 | B - 增强日志 | 低 | 极低 |

**总体评估：** 所有决策均采用增量改进策略，保持向后兼容，风险可控。

## Implementation Patterns & Consistency Rules

### 命名模式

**数据库命名：**

| 元素 | 规则 | 示例 |
|------|------|------|
| 表名 | `t_{entity}` 小写下划线 | `t_user`, `t_subject` |
| 列名 | snake_case | `create_time`, `del_flag` |
| 外键 | `{parent}_id` | `subject_id`, `lesson_id` |
| 索引 | `idx_{table}_{column}` | `idx_user_username` |

**API 命名：**

| 元素 | 规则 | 示例 |
|------|------|------|
| 基础路径 | `/api/{resources}` 复数 | `/api/users`, `/api/subjects` |
| 资源操作 | RESTful 动词 | `GET /api/users/{id}` |
| 子资源 | `/{parent}/{parentId}/{child}` | `/api/subjects/{id}/lessons` |
| 查询参数 | camelCase | `?subjectId=1&startDate=...` |
| 版本策略 | 暂不版本化，保持简洁 | `/api/` (非 `/api/v1/`) |

**DTO 命名：**

| 用途 | 命名规则 | 示例 |
|------|----------|------|
| 创建请求 | `CreateXxxRequest` | `CreateSubjectRequest` |
| 更新请求 | `UpdateXxxRequest` | `UpdateGradeRequest` |
| 响应对象 | `XxxDTO` | `SubjectDTO`, `LessonDTO` |
| 统计对象 | `XxxStatsDTO` | `GradeSummaryDTO` |

**代码命名：**

| 语言 | 元素 | 规则 | 示例 |
|------|------|------|------|
| Java | 类 | PascalCase | `UserService`, `GradeDTO` |
| Java | 方法 | camelCase | `getById()`, `createGrade()` |
| Java | 常量 | UPPER_SNAKE | `MAX_FILE_SIZE` |
| TS | 组件 | PascalCase | `SubjectList.vue` |
| TS | 函数 | camelCase | `useUserStore()` |
| TS | 接口 | PascalCase + DTO 后缀 | `UserDTO`, `CreateSubjectRequest` |

### 结构模式

**后端组织：**

```
backend/src/main/java/com/teaching/
├── controller/     # REST 控制器
├── service/        # 业务接口
│   └── impl/       # 业务实现
├── mapper/         # MyBatis-Plus Mapper
├── entity/         # 数据库实体
├── dto/            # 数据传输对象
├── config/         # 配置类
├── exception/      # 异常处理
└── util/           # 工具类
```

**前端组织：**

```
frontend/src/
├── api/            # API 服务（按模块）
├── views/          # 页面组件（按功能）
├── components/     # 通用组件
├── stores/         # Pinia Store
├── router/         # 路由配置
├── styles/         # 全局样式
├── directives/     # 自定义指令
└── utils/          # 工具函数
```

### 格式模式

**API 响应格式（必须遵守）：**

```json
{
  "code": 200,
  "success": true,
  "data": { /* 业务数据 */ },
  "message": "Success"
}
```

**错误响应格式：**

```json
{
  "code": 400,
  "success": false,
  "data": null,
  "message": "用户名已存在"
}
```

**日期时间格式：**

| 场景 | 格式 | 示例 |
|------|------|------|
| API 传输 | ISO-8601 | `2026-03-01T10:00:00` |
| 数据库存储 | TEXT (ISO) | `2026-03-01 10:00:00` |
| 前端显示 | YYYY-MM-DD HH:mm | `2026-03-01 10:00` |

### 状态管理模式

**Pinia Store 结构：**

```typescript
export const useXxxStore = defineStore('xxx', () => {
  const items = ref<XxxDTO[]>([])
  const loading = ref(false)

  const filteredItems = computed(() => ...)

  async function fetchItems() { ... }

  return { items, loading, filteredItems, fetchItems }
})
```

**数据刷新时机：**

| 场景 | 策略 |
|------|------|
| 页面挂载 | `onMounted` 中调用 `fetchXxx()` |
| 数据变更 | 操作成功后立即刷新列表 |
| 路由切换 | 不自动清空 Store，由页面决定 |

### CSS 变量使用模式

**全局变量引用：**

```css
/* 正确 */
.card {
  padding: var(--spacing-lg);
  border-radius: var(--border-radius);
  color: var(--text-primary);
}

/* 错误：硬编码值 */
.card {
  padding: 24px;
  border-radius: 4px;
  color: #303133;
}
```

**组件私有变量：**

```css
/* 命名格式：--{component}-{property} */
.subject-card {
  --subject-card-header-bg: var(--color-primary);
  --subject-card-padding: var(--spacing-md);
}
```

### 测试模式

**测试文件命名：**

| 类型 | 后端 | 前端 |
|------|------|------|
| 单元测试 | `{Class}Test.java` | `{module}.test.ts` |
| 集成测试 | `{Class}IntegrationTest.java` | `{module}.integration.test.ts` |
| E2E 测试 | - | `{feature}.e2e.ts` |

**E2E 测试选择器：**

```html
<!-- 格式：data-testid="{module}-{element}" -->
<el-button data-testid="subject-create-btn">创建学科</el-button>
<el-table data-testid="lesson-list-table">...</el-table>
<el-input data-testid="login-username-input" />
```

**测试数据隔离：**
- 每个测试用例独立准备测试数据
- 测试后清理创建的数据
- 不依赖其他测试的执行顺序

### 错误处理模式

**后端异常抛出：**

```java
// 业务异常
throw new BusinessException(400, "用户名已存在");

// 资源不存在
throw new ResourceNotFoundException("User", id);
```

**前端错误处理：**

```typescript
try {
  const res = await api.create(data)
  ElMessage.success('创建成功')
} catch (error) {
  // request.ts 拦截器已处理 ElMessage.error
  console.error('Create failed:', error)
}
```

### 执行规则

**所有 AI 代理必须：**

1. 遵循现有文件命名约定，不创建新的命名风格
2. 使用统一的 API 响应格式，不修改 Result<T> 结构
3. 新增 CSS 样式必须使用变量，不允许硬编码颜色/间距
4. 错误处理必须通过现有异常体系，不抛出原始 Exception
5. 前端组件必须使用 Composition API，不使用 Options API
6. E2E 相关元素添加 `data-testid` 属性
7. DTO 命名遵循 `CreateXxxRequest/UpdateXxxRequest/XxxDTO` 规范

**模式验证：**

| 检查项 | 验证方式 |
|--------|----------|
| API 响应格式 | 后端测试 + Postman 验证 |
| CSS 变量使用 | 代码审查 |
| 命名规范 | IDE 检查 + 代码审查 |
| 异常处理 | 单元测试覆盖 |
| E2E 选择器 | Playwright 测试验证 |

## Project Structure & Boundaries

### 完整项目目录结构

```
teaching-manage/
├── README.md
├── CLAUDE.md                          # AI 代理指导文档
├── docs/                              # 项目文档
│   ├── index.md
│   ├── architecture.md
│   ├── api-contracts.md
│   ├── data-models.md
│   ├── development-guide.md
│   └── project-overview.md
│
├── backend/                           # Spring Boot 后端
│   ├── pom.xml
│   ├── src/main/java/com/teaching/
│   │   ├── Application.java
│   │   ├── controller/                 # REST 控制器层
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java
│   │   │   ├── SubjectController.java
│   │   │   ├── LessonController.java
│   │   │   ├── GroupController.java
│   │   │   ├── SubmissionController.java
│   │   │   ├── GradeController.java    # FR31 修复点
│   │   │   ├── CodeCommentController.java
│   │   │   ├── NotificationController.java
│   │   │   ├── DashboardController.java # FR8 修复点
│   │   │   └── GradeSummaryController.java
│   │   ├── service/                    # 业务接口层
│   │   │   ├── impl/                   # 业务实现层
│   │   │   │   └── GradeServiceImpl.java  # FR31 主要修复点
│   │   │   └── SubjectService.java     # FR14 相关
│   │   ├── mapper/                     # MyBatis-Plus Mapper
│   │   ├── entity/                     # 数据库实体
│   │   ├── dto/                        # 数据传输对象
│   │   ├── enums/                      # 枚举类型
│   │   ├── config/                     # 配置类
│   │   ├── security/                   # 安全模块
│   │   ├── exception/                  # 异常处理
│   │   │   └── GlobalExceptionHandler.java  # 错误处理增强点
│   │   ├── task/                       # 定时任务
│   │   └── util/                       # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── schema.sql
│   ├── src/test/java/com/teaching/service/
│   └── uploads/
│
├── frontend/                          # Vue 3 前端
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── playwright.config.ts
│   ├── src/
│   │   ├── main.ts                     # 入口（导入全局样式）
│   │   ├── App.vue
│   │   ├── api/                        # API 服务
│   │   │   ├── types.ts
│   │   │   ├── dashboard.ts            # FR8 相关
│   │   │   ├── subject.ts              # FR14 相关
│   │   │   └── grade.ts                # FR31 相关
│   │   ├── views/                      # 页面组件
│   │   │   ├── Dashboard.vue           # FR8 修复点
│   │   │   ├── subjects/
│   │   │   │   └── SubjectDetail.vue   # FR14 修复点
│   │   │   └── grades/
│   │   │       └── GradeManagement.vue # FR31 前端相关
│   │   ├── components/                 # 通用组件
│   │   │   └── ErrorBoundary.vue       # 新建：错误边界
│   │   ├── stores/                     # Pinia 状态
│   │   ├── router/                     # FR8 路由修复点
│   │   │   └── index.ts
│   │   ├── styles/                     # 全局样式（新建）
│   │   │   ├── variables.css           # FR44-47: CSS 变量
│   │   │   └── common.css              # FR44-47: 通用样式
│   │   ├── directives/
│   │   └── utils/
│   │       └── request.ts              # 请求拦截器增强点
│   └── tests/
│
└── data/
    └── teaching.db                    # SQLite 数据库
```

### 架构边界

**API 边界：**

| 模块 | 端点前缀 | 权限 |
|------|----------|------|
| 认证 | `/api/auth` | 公开 |
| 用户 | `/api/users` | ADMIN |
| 学科 | `/api/subjects` | 已认证 |
| 课程 | `/api/lessons` | 已认证 |
| 小组 | `/api/groups` | 已认证 |
| 提交 | `/api/submissions` | 已认证 |
| 评分 | `/api/grades` | ADMIN/TEACHER |
| 评论 | `/api/comments` | ADMIN/TEACHER |
| 通知 | `/api/notifications` | 已认证 |
| 仪表盘 | `/api/dashboard` | 按角色 |
| 成绩汇总 | `/api/grade-summary` | 已认证 |

**数据流边界：**

```
Controller → Service → Mapper → SQLite
    ↓           ↓
   DTO       Entity
```

**前端状态边界：**

```
Component → Pinia Store → API Service → Backend
    ↓
  v-loading / ElMessage
```

### 修复需求映射

**FR8 - Dashboard 404：**

| 层级 | 文件 | 检查项 |
|------|------|--------|
| 路由 | `frontend/src/router/index.ts` | `/dashboard` 路由配置 |
| 视图 | `frontend/src/views/Dashboard.vue` | 组件加载逻辑 |
| API | `frontend/src/api/dashboard.ts` | API 调用 |
| 后端 | `backend/.../DashboardController.java` | 端点实现 |

**FR14 - 学科详情显示：**

| 层级 | 文件 | 检查项 |
|------|------|--------|
| 视图 | `frontend/src/views/subjects/SubjectDetail.vue` | 数据渲染 |
| API | `frontend/src/api/subject.ts` | `getSubjectById()` |
| 后端 | `backend/.../SubjectController.java` | `GET /{id}` |
| 服务 | `backend/.../SubjectServiceImpl.java` | 数据组装 |

**FR31 - 评分保存 500：**

| 层级 | 文件 | 检查项 |
|------|------|--------|
| 控制器 | `backend/.../GradeController.java` | 请求处理 |
| 服务 | `backend/.../GradeServiceImpl.java` | 业务逻辑 |
| 异常 | `backend/.../GlobalExceptionHandler.java` | 错误处理 |
| 数据 | `t_grade` 表 | 外键约束 |

**FR44-47 - UI 一致性：**

| 步骤 | 文件 | 操作 |
|------|------|------|
| 1 | `frontend/src/styles/variables.css` | 新建 CSS 变量 |
| 2 | `frontend/src/styles/common.css` | 新建通用样式 |
| 3 | `frontend/src/main.ts` | 导入样式文件 |
| 4 | 各 `.vue` 组件 | 替换硬编码为变量 |

### 新增文件清单

| 文件 | 用途 | 决策来源 |
|------|------|----------|
| `frontend/src/styles/variables.css` | CSS 变量定义 | Decision 2 |
| `frontend/src/styles/common.css` | 通用样式类 | Decision 2 |
| `frontend/src/components/ErrorBoundary.vue` | 错误边界组件 | Decision 3 |

## Architecture Validation Results

### Coherence Validation ✅

**决策兼容性：** 所有技术决策相互兼容
- Spring Boot 3.2.2 + Vue 3.4.0 + SQLite 组合稳定
- 三项新决策（错误处理/CSS变量/增强日志）均为增量改进
- 无矛盾或冲突的架构选择

**模式一致性：** 实现模式支持架构决策
- 命名约定覆盖数据库、API、代码、测试
- DTO 命名规范明确
- 测试选择器格式统一

**结构对齐：** 项目结构支持所有决策
- 新增 `styles/` 目录支持 CSS 变量
- 新增 `ErrorBoundary.vue` 支持错误边界
- 修复需求映射到具体文件

### Requirements Coverage ✅

**功能需求覆盖：**

| 需求 | 覆盖状态 | 实现路径 |
|------|----------|----------|
| FR8 Dashboard | ✅ 完全覆盖 | 路由 → 视图 → API 诊断链 |
| FR14 学科详情 | ✅ 完全覆盖 | 前后端数据流修复 |
| FR31 评分保存 | ✅ 完全覆盖 | Service 异常处理增强 |
| FR44-47 UI 一致性 | ✅ 完全覆盖 | CSS 变量体系 |

**非功能需求覆盖：**

| NFR | 覆盖状态 |
|-----|----------|
| 性能 API < 500ms | ✅ 现有架构支持 |
| 安全 JWT 24h | ✅ 已实现 |
| 限流 100/min/IP | ✅ 已实现 |
| 零 500/404 错误 | ✅ 错误处理增强 |

### Implementation Readiness ✅

**AI 代理可直接使用此文档进行实现：**

1. 所有修复需求有明确的文件映射
2. 实现模式有代码示例
3. 命名约定覆盖全场景
4. 新增文件位置和内容已定义

### Architecture Completeness Checklist

**✅ 需求分析**
- [x] 项目上下文分析完成
- [x] 规模复杂度评估（中等/Brownfield）
- [x] 技术约束识别
- [x] 横切关注点映射

**✅ 架构决策**
- [x] 错误处理策略：增强现有机制
- [x] UI 一致性方案：CSS 变量
- [x] 日志增强策略：结构化日志 + 错误边界

**✅ 实现模式**
- [x] 命名约定（数据库/API/代码/测试）
- [x] 结构模式（后端/前端目录）
- [x] 格式模式（API 响应/日期时间）
- [x] 状态管理模式（Pinia Store）

**✅ 项目结构**
- [x] 完整目录结构
- [x] 修复需求映射
- [x] 新增文件清单
- [x] 架构边界定义

### Architecture Readiness Assessment

**Overall Status:** ✅ READY FOR IMPLEMENTATION

**Confidence Level:** HIGH

**Key Strengths:**
- Brownfield 项目，现有架构稳定
- 增量改进策略，风险可控
- 修复需求清晰映射到具体文件
- 模式完善，AI 代理可一致实现

**Implementation Priority:**
1. FR8 Dashboard 404 修复
2. FR14 学科详情修复
3. FR31 评分保存修复
4. FR44-47 CSS 变量体系建立

