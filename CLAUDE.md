# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。
请始终使用简体中文与我对话，并在回答时保持专业、简洁。

## 项目概述

面向 IT 编程培训机构的教学管理系统。支持 3-5 个并行学科，30-100 名学员。全栈应用，后端使用 Spring Boot 3，前端使用 Vue 3。

**技术栈：**
- 后端：Spring Boot 3.2.2, MyBatis-Plus, SQLite, JWT, Java 17
- 前端：Vue 3, Vite, TypeScript, Element Plus, Monaco Editor, Pinia

## 构建与运行命令

### 后端 (Maven)
```bash
cd backend

# 运行应用
mvn spring-boot:run

# 构建 JAR 包
mvn clean package

# 运行测试
mvn test

# 运行指定测试
mvn test -Dtest=ClassName#methodName

# 跳过测试构建
mvn clean package -DskipTests
```

### 前端 (npm)
```bash
cd frontend

# 开发服务器 (http://localhost:5173)
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview

# 运行测试
npm run test

# 运行测试 UI
npm run test:ui

# 代码检查
npm run lint
```

### 数据库
- **位置**：`./data/teaching.db` (SQLite)
- **Schema**：启动时从 `backend/src/main/resources/schema.sql` 自动初始化
- **注意**：首次运行后设置 `spring.sql.init.mode=never` 防止重复初始化

## 架构概览

### 认证与安全流程

**JWT 认证：**
1. 用户登录 → 后端验证凭证 → 返回 JWT token（24 小时有效期）
2. 前端将 token 存储在 localStorage
3. 请求拦截器（`frontend/src/utils/request.ts`）添加 `Authorization: Bearer {token}` 头部
4. `JwtAuthenticationFilter` 验证 token → 设置 SecurityContext
5. 控制器使用 `@PreAuthorize("hasRole('...')")` 进行授权

**安全层级：**
- `CorsConfig`：支持凭证的 CORS 配置
- `RateLimitFilter`：每 IP 每分钟 100 次请求限制（内存存储，每分钟重置）
- `SecurityHeadersConfig`：XSS、点击劫持、MIME 嗅探防护
- `GlobalExceptionHandler`：统一的 401/403/404/429/500 错误响应

**角色：** ADMIN（管理员）, TEACHER（教员）, STUDENT（学员）

### 数据访问模式

**后端使用 MyBatis-Plus 及自定义约定：**
- 所有实体扩展基础字段（version, delFlag, createBy, createTime, updateBy, updateTime）
- 通过 `delFlag` 实现逻辑删除（0=存在，1=删除）- 在 MyBatis-Plus 中全局配置
- 通过 `version` 字段实现乐观锁
- 数据库蛇形命名自动映射到 Java 驼峰命名

**服务层模式：**
```
Controller → Service（业务逻辑）→ Mapper（MyBatis-Plus）→ Database
                ↓
         NotificationService（处理副作用）
```

**示例：评分流程**
1. `GradeController.createGrade()` 接收请求
2. `GradeService` 验证提交存在，创建评分记录
3. **副作用**：`NotificationService.notifyGradeCompleted()` 为学员创建通知
4. 返回 DTO 到控制器

### 文件上传架构

**上传流程：**
1. `SubmissionController` 接收 multipart 文件
2. `FileService.handleFileUpload()`：
   - 保存到 `/uploads/{subjectId}/{lessonId}/{groupId 或 studentId}/`
   - ZIP 文件自动解压并保持目录结构
   - 生成文件树 JSON 供前端展示
3. 文件路径存储在 `t_submission.file_path`

**代码查看器集成：**
- 前端从后端获取文件树
- Monaco Editor 显示选中文件并带语法高亮
- 行级评论存储在 `t_code_comment`（file_path + line_number）

### 前端状态管理

**Pinia Store 模式（`stores/user.ts`）：**
- 集中化用户状态（用户信息、token、角色）
- 计算属性：`isAdmin`、`isTeacher`、`isStudent`
- 操作方法：`login()`、`logout()`、`fetchUserInfo()`

**权限控制：**
- 路由守卫检查 `meta.requiresAuth` 和 `meta.roles`
- 模板中的自定义指令：`v-role`、`v-not-role`、`v-permission`
- 权限检查失败时元素从 DOM 移除

### 通知系统

**5 种通知类型：**
1. `LESSON_PUBLISHED` - 新课程创建
2. `DEADLINE_REMINDER` - 截止日期前 1 天（定时任务：每天上午 9 点）
3. `GRADE_COMPLETED` - 作业已评分
4. `GROUP_APPLICATION` - 学员申请加入小组
5. `GROUP_APPROVED` - 组长批准申请

**定时任务：**
- `DeadlineReminderTask` 在 `0 0 9 * * ?` 运行（每天上午 9 点）
- 查询截止日期为明天的课程
- 为未提交作业的学员创建通知

**前端轮询：**
- `NotificationBell.vue` 每 30 秒轮询 `/api/notifications/unread`
- 徽章显示未读数量
- 下拉菜单显示最近通知

### 小组管理业务规则

**多步骤工作流：**
1. 学员创建小组 → 成为组长
2. 其他学员申请 → 状态 = PENDING
3. 组长批准/拒绝 → 状态 = APPROVED/REJECTED
4. 组长可移除成员

**验证规则：**
- 小组人数必须在学科的 `min_members` 到 `max_members` 范围内
- 对于分组学科，学员必须在首次提交前加入小组
- 提交类型（PERSONAL vs GROUP）决定是否需要 group_id

## 关键配置

### 后端（`application.yml`）

**数据库自动初始化：**
```yaml
spring.sql.init.mode: always  # 首次运行后改为 'never'
```

**JWT 密钥：**
```yaml
jwt.secret: teaching-manage-secret-key-must-be-at-least-256-bits-long
```
⚠️ **生产环境：** 使用环境变量，不要硬编码

**文件存储：**
```yaml
file.upload-dir: ./uploads
```

### 前端代理

前端将 `/api` 请求代理到后端。开发环境中，Vite 配置应代理到 `http://localhost:8080`。

## 数据库 Schema 约定

**表命名：** `t_{实体名}` (例如：`t_user`, `t_subject`, `t_lesson`)

**标准字段（所有表）：**
- `id` - 自增主键
- `version` - 乐观锁
- `del_flag` - 逻辑删除（0/1）
- `create_by`, `create_time`, `update_by`, `update_time` - 审计字段

**外键命名：** `{父表}_id`（例如：`subject_id`, `lesson_id`）

**枚举存储：** 使用 VARCHAR 存储类型枚举（例如：`UserRole`, `NotificationType`）

## 常见模式

### 添加新的 API 端点

**后端：**
1. 在 `entity/` 中创建实体（扩展基础字段）
2. 在 `mapper/` 中创建 mapper 接口（继承 `BaseMapper<Entity>`）
3. 在 `dto/` 中创建 DTOs（Request, Response 等）
4. 在 `service/` 中实现 service，使用 `@Service` 注解
5. 在 `controller/` 中创建 controller，使用 `@RestController` 和 `@RequestMapping("/api/...")`
6. 添加 `@PreAuthorize` 实现基于角色的访问控制

**前端：**
1. 在 `api/types.ts` 中定义类型
2. 在 `api/{模块}.ts` 中创建 API 服务，使用 `request` 工具
3. 在 `views/{模块}/` 中创建视图，使用 Composition API
4. 在 `router/index.ts` 中添加路由，设置 `meta.requiresAuth` 和可选的 `meta.roles`

### 使用 MyBatis-Plus

**简单 CRUD：**
```java
// Service 继承 IService<Entity>
// Mapper 继承 BaseMapper<Entity>

// 可用的内置方法：
save(entity)           // INSERT
updateById(entity)     // UPDATE
removeById(id)         // 逻辑删除
getById(id)            // 根据 ID 查询
list()                 // 查询所有
page(page, wrapper)    // 分页查询
```

**自定义查询：**
- 在 Mapper 接口中添加方法
- 在 `resources/mapper/` 中创建对应的 XML（可选）
- 或使用 QueryWrapper/LambdaQueryWrapper 进行动态查询

### 异常处理

**后端抛出自定义异常：**
- `BusinessException(code, message)` → 400 Bad Request
- `ResourceNotFoundException(resource, id)` → 404 Not Found
- `RateLimitExceededException(message)` → 429 Too Many Requests

`GlobalExceptionHandler` 捕获并转换为统一的 `Result<T>` 响应。

## 已知问题

**代码库存在构建失败（来自之前的会话）：**
1. 某些 DTO 中缺少验证约束导入
2. Notification 实体引用的 BaseEntity 类缺失
3. 前端构建中的 vue-tsc 工具链错误

这些错误与最近的安全和错误处理实现（任务 37-38）无关。

## 开发备注

**API 响应格式：**
所有后端端点返回：
```json
{
  "code": 200,
  "success": true,
  "data": {...},
  "message": "Success"
}
```

**前端期望此格式** - `request.ts` 拦截器检查 `code !== 200`。

**日期/时间处理：**
- 后端使用 `LocalDateTime`（Java 8+ Time API）
- SQLite 以 ISO-8601 格式的 TEXT 存储
- 前端接收 ISO 字符串，使用 Element Plus 日期格式化器显示

**文件上传限制：**
- Spring Boot 默认 multipart 限制：1MB
- 通过以下配置增加：`spring.servlet.multipart.max-file-size` 和 `max-request-size`

**生产环境 CORS：**
- 在 `CorsConfig.java` 中将 `allowedOriginPatterns("*")` 改为具体域名
