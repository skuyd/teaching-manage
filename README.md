# 教学管理系统

面向 IT 编程培训机构的教学管理系统，支持 3-5 个并行学科，30-100 名学员的全流程教学管理。

## 功能特性

### 主题系统
- 三种主题：中国红、科技蓝（默认）、自然绿
- 一键切换，250ms 平滑过渡动画
- 本地持久化 + 后端同步
- Element Plus 组件深度集成

### 登录页
- 现代化左右分栏布局（品牌区 + 表单区）
- 三主题视觉效果完整实现
- 玻璃态卡片设计
- 响应式适配（桌面/平板/移动端）
- 丰富的微动效（页面加载、滑入、悬浮、脉冲）
- 完整的可访问性支持

### 用户管理
- 三种角色：管理员、教员、学员
- JWT 认证（24小时有效期）
- 个人信息管理、头像上传、密码修改

### 学科管理
- 学科创建、编辑、删除
- 学员分配与管理
- 支持个人作业和小组作业模式

### 课程管理
- 课程创建与发布
- 日历视图（月视图/周视图）
- 拖拽调整课程时间
- 作业截止时间设置

## 核心概念

### 学科与课程的关系

```
学科 (Subject)
├── 课程 1 (Lesson)
│   └── 作业提交 (Submissions)
├── 课程 2 (Lesson)
│   └── 作业提交 (Submissions)
└── 课程 N (Lesson)
    └── 作业提交 (Submissions)
```

- **学科 (Subject)**：代表一门完整的课程体系，如"Java 基础"、"前端开发"等
- **课程 (Lesson)**：学科下的具体教学单元，每次上课对应一个课程
- **关系**：一个学科包含多个课程，每个课程必须属于某个学科

### 学员与学科的关系

```
学员加入流程：
1. 管理员/教员 在学科详情页 → 学生管理 → 添加学员到学科
2. 学员登录后即可看到已加入的学科及其全部课程
3. 学员可以提交该学科下任意课程的作业
```

**重要说明**：
- 学员只能加入**学科**，不能单独加入某个课程
- 加入学科后，自动参与该学科下的**全部课程**
- 学员必须先被添加到学科中，才能：
  - 在"作业管理"中看到该学科
  - 提交该学科下课程的作业
  - 查看课程内容和作业要求

### 作业提交类型

| 类型 | 说明 | 要求 |
|------|------|------|
| 个人作业 | 每个学员独立提交 | 学员已加入学科 |
| 小组作业 | 以小组为单位提交 | 学员已加入学科且已加入小组 |

### 小组管理
- 学员创建小组
- 入组申请与审批
- 组长权限管理

### 作业提交
- 文件上传（支持 ZIP 自动解压）
- 重新提交
- 提交状态看板

### 代码查看器
- Monaco Editor 代码预览
- 语法高亮（支持多种语言）
- 行级代码评论

### 评分系统
- 作业评分与评语
- 成绩汇总统计
- 成绩导出 Excel

### 通知系统
- 课程发布通知
- 截止日期提醒（每日定时任务）
- 评分完成通知
- 小组申请通知

## 技术栈

### 后端
- **框架**：Spring Boot 3.2.2
- **ORM**：MyBatis-Plus
- **数据库**：SQLite
- **认证**：JWT + Spring Security
- **语言**：Java 17

### 前端
- **框架**：Vue 3 + TypeScript
- **构建工具**：Vite
- **UI 组件库**：Element Plus
- **状态管理**：Pinia
- **代码编辑器**：Monaco Editor
- **主题系统**：CSS Variables + SCSS

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- Maven 3.8+

### 后端启动

```bash
cd backend

# 运行应用（开发模式）
mvn spring-boot:run

# 或构建后运行
mvn clean package -DskipTests
java -jar target/teaching-manage-0.0.1-SNAPSHOT.jar
```

后端服务运行在 http://localhost:8080

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务运行在 http://localhost:5173

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教员 | teacher | teacher123 |
| 学员 | student | student123 |

## 项目结构

```
teaching-manage/
├── backend/                    # 后端项目
│   ├── src/main/java/com/teaching/
│   │   ├── config/            # 配置类
│   │   ├── controller/        # 控制器
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # 实体类
│   │   ├── exception/         # 异常处理
│   │   ├── mapper/            # MyBatis Mapper
│   │   ├── security/          # 安全配置
│   │   ├── service/           # 业务逻辑
│   │   └── util/              # 工具类
│   └── src/main/resources/
│       ├── application.yml    # 应用配置
│       └── schema.sql         # 数据库初始化
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API 接口
│   │   ├── components/        # 公共组件
│   │   │   └── common/        # 通用组件（ThemeSwitcher 等）
│   │   ├── composables/       # 组合式函数（useTheme 等）
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # Pinia 状态（theme、user 等）
│   │   ├── styles/            # 样式文件
│   │   │   └── themes/        # 主题 CSS 变量
│   │   └── views/             # 页面视图
│   └── vite.config.ts         # Vite 配置
├── data/                       # SQLite 数据库
└── uploads/                    # 上传文件存储
```

## API 文档

### 认证接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/logout | 用户登出 |
| GET | /api/auth/me | 获取当前用户信息 |

### 用户管理

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/users | 获取用户列表 | 管理员 |
| POST | /api/users | 创建用户 | 管理员 |
| PUT | /api/users/{id} | 更新用户 | 管理员 |
| DELETE | /api/users/{id} | 删除用户 | 管理员 |

### 用户偏好

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/users/me/preferences | 获取用户偏好 | 已认证 |
| PATCH | /api/users/me/preferences | 更新用户偏好 | 已认证 |

### 学科管理

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/subjects | 获取学科列表 | 全部 |
| POST | /api/subjects | 创建学科 | 管理员/教员 |
| GET | /api/subjects/{id} | 获取学科详情 | 全部 |
| PUT | /api/subjects/{id} | 更新学科 | 管理员/教员 |
| DELETE | /api/subjects/{id} | 删除学科 | 管理员/教员 |

### 课程管理

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/lessons | 获取课程列表 | 全部 |
| POST | /api/lessons | 创建课程 | 管理员/教员 |
| PUT | /api/lessons/{id} | 更新课程 | 管理员/教员 |
| DELETE | /api/lessons/{id} | 删除课程 | 管理员/教员 |

### 作业提交

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/submissions | 获取提交列表 | 全部 |
| POST | /api/submissions | 提交作业 | 学员 |
| GET | /api/submissions/{id}/files | 获取文件列表 | 全部 |

### 评分管理

| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| POST | /api/grades | 创建评分 | 管理员/教员 |
| PUT | /api/grades/{id} | 更新评分 | 管理员/教员 |
| GET | /api/grade-summary/student | 学员成绩汇总 | 学员 |
| GET | /api/grade-summary/subject/{id} | 学科成绩汇总 | 管理员/教员 |

## 配置说明

### 后端配置 (application.yml)

```yaml
# 服务端口
server:
  port: 8080

# 数据库配置
spring:
  datasource:
    url: jdbc:sqlite:./data/teaching.db

# JWT 配置
jwt:
  secret: your-secret-key  # 生产环境使用环境变量
  expiration: 86400000     # 24小时

# 文件上传
file:
  upload-dir: ./uploads
```

### 前端配置 (vite.config.ts)

```typescript
// API 代理配置
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 开发指南

### 添加新 API

1. **后端**
   - 创建 Entity → Mapper → Service → Controller
   - 添加 `@PreAuthorize` 注解控制权限

2. **前端**
   - 在 `api/types.ts` 定义类型
   - 在 `api/` 目录创建 API 服务
   - 在 `views/` 创建页面组件
   - 在 `router/` 添加路由

### 运行测试

```bash
# 后端测试
cd backend
mvn test

# 前端单元测试
cd frontend
npm run test

# 前端 E2E 测试（需要先启动前后端服务）
cd frontend
npx playwright test --project=chromium --workers=1
```

## 部署

### Docker 部署（推荐）

```bash
# 构建镜像
docker build -t teaching-manage .

# 运行容器
docker run -d -p 8080:8080 -v ./data:/app/data teaching-manage
```

### 手动部署

1. 构建后端 JAR 包
2. 构建前端静态文件
3. 配置 Nginx 反向代理
4. 使用 systemd 管理服务

## 安全特性

- JWT Token 认证
- 密码 BCrypt 加密
- CORS 跨域保护
- 请求速率限制（每 IP 每分钟 100 次）
- XSS/点击劫持/MIME 嗅探防护
- SQL 注入防护

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
