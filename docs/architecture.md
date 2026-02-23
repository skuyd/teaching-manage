# 架构设计 - Teaching Manage

## 系统架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端层                              │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  Vue 3 SPA                                              ││
│  │  - Element Plus UI                                      ││
│  │  - Pinia 状态管理                                       ││
│  │  - Vue Router                                           ││
│  │  - Monaco Editor                                        ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP/REST (JSON)
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        服务端层                              │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  Spring Boot 3.2.2                                      ││
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     ││
│  │  │ Controller  │→ │  Service    │→ │   Mapper    │     ││
│  │  │   (REST)    │  │(Business)   │  │ (MyBatis)   │     ││
│  │  └─────────────┘  └─────────────┘  └─────────────┘     ││
│  │         │                │                │             ││
│  │         ▼                ▼                ▼             ││
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     ││
│  │  │  Security   │  │Notification │  │   File      │     ││
│  │  │   (JWT)     │  │  Service    │  │  Service    │     ││
│  │  └─────────────┘  └─────────────┘  └─────────────┘     ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        数据层                                │
│  ┌─────────────────────┐  ┌─────────────────────┐          │
│  │     SQLite          │  │   文件系统           │          │
│  │   (./data/*.db)     │  │  (./uploads/)       │          │
│  └─────────────────────┘  └─────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

## 后端架构

### 分层架构

```
com.teaching/
├── controller/         # 控制器层 - REST API 端点
│   └── dto/           # 请求/响应 DTO
├── service/           # 服务层 - 业务逻辑
│   └── impl/          # 服务实现
├── mapper/            # 数据访问层 - MyBatis Mapper
├── entity/            # 实体层 - 数据库映射
├── dto/               # 公共 DTO
├── enums/             # 枚举类型
├── config/            # 配置类
├── security/          # 安全模块
├── exception/         # 异常处理
├── task/              # 定时任务
├── util/              # 工具类
├── common/            # 公共类
└── init/              # 初始化器
```

### 核心组件

#### 1. 安全模块 (security/)

```java
// JWT 认证流程
JwtAuthenticationFilter
    ↓ 验证 Token
    ↓ 解析用户信息
    ↓ 设置 SecurityContext
```

| 类名 | 职责 |
|------|------|
| JwtUtils | JWT 生成和验证 |
| JwtAuthenticationFilter | 请求过滤器 |
| UserDetailsServiceImpl | 用户详情服务 |
| UserDetailsImpl | 用户详情实现 |

#### 2. 配置模块 (config/)

| 类名 | 职责 |
|------|------|
| SecurityConfig | Spring Security 配置 |
| CorsConfig | CORS 跨域配置 |
| MybatisPlusConfig | MyBatis-Plus 配置 |
| RateLimitFilter | 速率限制过滤器 |
| SecurityHeadersConfig | 安全响应头 |
| WebConfig | Web 配置（静态资源） |

#### 3. 异常处理 (exception/)

```java
GlobalExceptionHandler
    ├── BusinessException      → 400 Bad Request
    ├── ResourceNotFoundException → 404 Not Found
    ├── RateLimitExceededException → 429 Too Many Requests
    ├── AccessDeniedException  → 403 Forbidden
    └── Exception              → 500 Internal Server Error
```

### 数据访问模式

使用 MyBatis-Plus 简化 CRUD：

```java
// Mapper 继承 BaseMapper
public interface UserMapper extends BaseMapper<User> {
    // 自定义方法
    @Select("...")
    List<User> findByRole(String role);
}

// Service 使用 QueryWrapper
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getRole, role)
       .eq(User::getDelFlag, 0);
return userMapper.selectList(wrapper);
```

### 实体设计约定

所有实体继承基础字段：

```java
// 基础字段
private Long id;            // 主键
private Integer version;    // 乐观锁
private Integer delFlag;    // 逻辑删除 (0=存在, 1=删除)
private String createBy;    // 创建者
private LocalDateTime createTime;
private String updateBy;    // 更新者
private LocalDateTime updateTime;
```

## 前端架构

### 目录结构

```
frontend/src/
├── api/                # API 服务层
│   ├── types.ts       # 类型定义
│   ├── auth.ts        # 认证 API
│   ├── user.ts        # 用户 API
│   ├── subject.ts     # 学科 API
│   ├── lesson.ts      # 课程 API
│   ├── group.ts       # 小组 API
│   ├── submission.ts  # 提交 API
│   ├── grade.ts       # 评分 API
│   ├── comment.ts     # 评论 API
│   └── notification.ts# 通知 API
├── views/             # 页面组件
│   ├── Dashboard.vue  # 仪表盘
│   ├── Login.vue      # 登录
│   ├── users/         # 用户管理
│   ├── subjects/      # 学科管理
│   ├── groups/        # 小组管理
│   ├── submissions/   # 提交管理
│   ├── grades/        # 成绩管理
│   ├── notifications/ # 通知中心
│   └── profile/       # 个人设置
├── components/        # 通用组件
│   ├── MainLayout.vue # 主布局
│   ├── FileTree.vue   # 文件树
│   ├── CodeComments.vue # 代码评论
│   ├── GradeForm.vue  # 评分表单
│   └── NotificationBell.vue # 通知铃铛
├── stores/            # Pinia 状态管理
│   └── user.ts        # 用户状态
├── router/            # 路由配置
│   └── index.ts
├── directives/        # 自定义指令
│   └── permission.ts  # 权限指令
├── utils/             # 工具函数
│   └── request.ts     # Axios 封装
└── styles/            # 样式文件
```

### 状态管理

使用 Pinia 管理用户状态：

```typescript
// stores/user.ts
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null as User | null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isTeacher: (state) => state.user?.role === 'TEACHER',
    isStudent: (state) => state.user?.role === 'STUDENT'
  },

  actions: {
    async login(credentials) { ... },
    async logout() { ... },
    async fetchUserInfo() { ... }
  }
})
```

### 路由权限控制

```typescript
// router/index.ts
{
  path: '/users',
  component: UserList,
  meta: {
    requiresAuth: true,
    roles: ['ADMIN']
  }
}

// 路由守卫
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (to.meta.roles && !hasRole(to.meta.roles)) {
    next('/403')
  } else {
    next()
  }
})
```

### 权限指令

```vue
<!-- 仅管理员可见 -->
<el-button v-role="'ADMIN'">管理用户</el-button>

<!-- 非学员可见 -->
<el-button v-not-role="'STUDENT'">评分</el-button>

<!-- 多角色 -->
<el-button v-role="['ADMIN', 'TEACHER']">编辑</el-button>
```

## API 设计

### 响应格式

```json
{
  "code": 200,
  "success": true,
  "data": { ... },
  "message": "Success"
}
```

### 错误响应

```json
{
  "code": 400,
  "success": false,
  "data": null,
  "message": "用户名已存在"
}
```

### 分页请求

```
GET /api/users?page=1&size=10&role=STUDENT
```

### 分页响应

```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

## 安全架构

### 认证流程

```
1. POST /api/auth/login
   ├── 验证用户名密码
   ├── 生成 JWT Token (24h)
   └── 返回 Token 和用户信息

2. 后续请求
   ├── Header: Authorization: Bearer {token}
   ├── JwtAuthenticationFilter 验证
   └── 设置 SecurityContext
```

### 授权配置

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/health").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/subjects/**").authenticated()
            .requestMatchers("/api/subjects/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        );
    }
}
```

### 方法级权限

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { ... }

@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public void createLesson(CreateLessonRequest request) { ... }
```

## 文件处理架构

### 上传流程

```
1. 前端上传 multipart/form-data
   ↓
2. SubmissionController 接收
   ↓
3. FileService.handleFileUpload()
   ├── 验证文件类型
   ├── 保存到 uploads/{subjectId}/{lessonId}/{submitterId}/
   ├── 解压 ZIP/RAR
   └── 生成文件树 JSON
   ↓
4. 保存 Submission 记录
```

### 文件存储结构

```
uploads/
└── {subjectId}/
    └── {lessonId}/
        └── {submitterId 或 groupId}/
            ├── original.zip           # 原始文件
            └── extracted/             # 解压目录
                ├── src/
                │   └── main.java
                └── README.md
```

## 通知架构

### 通知触发点

| 触发点 | 通知类型 | 实现位置 |
|--------|----------|----------|
| 创建课程 | LESSON_PUBLISHED | LessonServiceImpl |
| 评分完成 | GRADE_COMPLETED | GradeServiceImpl |
| 申请加入 | GROUP_APPLICATION | GroupServiceImpl |
| 批准申请 | GROUP_APPROVED | GroupServiceImpl |
| 定时任务 | DEADLINE_REMINDER | DeadlineReminderTask |

### 定时任务

```java
@Scheduled(cron = "0 0 9 * * ?")  // 每天 9:00
public void sendDeadlineReminders() {
    // 1. 查询明天截止的课程
    // 2. 查询未提交的学员
    // 3. 创建通知记录
}
```

## 技术决策记录

### 选择 SQLite

**原因：**
- 简化部署（无需独立数据库服务）
- 适合 30-100 用户规模
- 单文件存储便于备份

**权衡：**
- 写入性能受限（单写者锁）
- 不支持复杂并发场景

### 选择 MyBatis-Plus

**原因：**
- 简化 CRUD 操作
- 内置分页、逻辑删除、乐观锁
- 与 Spring Boot 3 兼容

### 选择 Monaco Editor

**原因：**
- VS Code 同款编辑器
- 丰富的语法高亮支持
- 支持行级定位和标记

### 选择 Element Plus

**原因：**
- Vue 3 原生支持
- 丰富的组件库
- 良好的中文文档
