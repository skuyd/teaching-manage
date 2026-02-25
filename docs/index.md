# Teaching Manage - 教学管理系统文档

> 面向 IT 编程培训机构的教学管理系统
>
> 生成日期：2026-02-18
> 最后更新：2026-02-25
> 扫描模式：初始扫描 (initial_scan)

## 项目概述

本系统是一个面向 IT 编程培训机构的教学管理平台，支持 3-5 个并行学科和 30-100 名学员的管理。采用前后端分离架构，后端使用 Spring Boot 3，前端使用 Vue 3。

### 项目分类

| 属性 | 值 |
|------|-----|
| 项目类型 | 多部分全栈应用 (Multi-part Web Application) |
| 后端类型 | Java Spring Boot Backend |
| 前端类型 | Vue 3 SPA Frontend |
| 数据库 | SQLite |
| 认证方式 | JWT |

## 快速参考

### 技术栈

**后端 (backend/)**
- Spring Boot 3.2.2 + Java 17
- MyBatis-Plus 3.5.5
- SQLite 3.45.1
- JWT (jjwt 0.12.3)
- Apache POI 5.2.5 (Excel 导出)

**前端 (frontend/)**
- Vue 3.4.0 + TypeScript 5.3.3
- Vite 5.0.10
- Element Plus 2.4.4
- Monaco Editor 0.45.0 (代码查看器)
- Pinia 2.1.7 (状态管理)

### 开发命令

```bash
# 后端
cd backend
mvn spring-boot:run          # 启动服务 (端口 8080)
mvn test                     # 运行测试
mvn clean package            # 构建 JAR

# 前端
cd frontend
npm run dev                  # 启动开发服务器 (端口 5173)
npm run build               # 生产构建
npm run test                # 单元测试
npm run test:e2e            # E2E 测试
```

### 默认账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教员 | teacher | teacher123 |
| 学员 | student | student123 |

## 文档目录

### 核心文档

- [项目概述](./project-overview.md) - 业务功能和系统概述
- [架构设计](./architecture.md) - 系统架构和技术决策
- [API 接口文档](./api-contracts.md) - RESTful API 端点
- [数据模型](./data-models.md) - 数据库 Schema 和实体关系
- [开发指南](./development-guide.md) - 本地开发环境搭建

### 已有文档

- [CLAUDE.md](../CLAUDE.md) - Claude Code 工作指南
- [需求文档](./requirements.md) - 功能需求说明
- [设计文档](./des.md) - 原始设计规范

## 核心功能模块

### 0. 主题系统 (Epic 1-2 已完成)
- 三主题支持：中国红、科技蓝（默认）、自然绿
- 250ms 平滑切换动画
- localStorage 持久化 + 后端同步
- Element Plus 组件深度集成
- **登录页三主题实现** (Epic 2):
  - 现代化左右分栏布局（50/50）
  - 玻璃态卡片设计
  - 响应式适配（桌面/平板/移动端）
  - 丰富的微动效（页面加载、滑入、悬浮、脉冲）
  - 完整的可访问性支持（键盘导航、焦点管理）
  - 测试覆盖率：100% 语句/函数/行，88.88% 分支

### 1. 用户管理
- 多角色支持：管理员、教员、学员
- JWT 认证和权限控制
- 用户批量导入/导出 (Excel)

### 2. 学科管理
- 学科创建和配置
- 学员-学科关联
- 分组学科支持

### 3. 课程管理
- 课程发布和管理
- 作业截止日期设置
- 日历视图 (周视图/拖拽调整)

### 4. 小组管理
- 学员创建小组
- 申请加入/组长审批
- 小组成员管理

### 5. 作业提交
- 个人/小组作业提交
- 支持 ZIP/RAR 文件
- 代码查看器集成

### 6. 代码评审
- Monaco Editor 代码显示
- 行级代码评论
- 评分系统 (A/B/C/D/E)

### 7. 通知系统
- 多类型通知推送
- 实时未读计数
- 截止日期提醒 (定时任务)

### 8. 数据分析
- 仪表盘统计
- 成绩汇总导出
- 多维度报表

## 项目结构

```
teaching-manage/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/teaching/
│   │   ├── controller/     # REST 控制器
│   │   ├── service/        # 业务逻辑层
│   │   ├── mapper/         # MyBatis 数据访问层
│   │   ├── entity/         # 实体类
│   │   ├── dto/            # 数据传输对象
│   │   ├── config/         # 配置类
│   │   ├── security/       # 安全模块
│   │   └── exception/      # 异常处理
│   └── src/main/resources/
│       ├── application.yml # 应用配置
│       └── schema.sql      # 数据库初始化
├── frontend/               # Vue 3 前端
│   ├── src/
│   │   ├── api/           # API 服务
│   │   ├── views/         # 页面组件
│   │   ├── components/    # 通用组件
│   │   ├── stores/        # Pinia 状态管理
│   │   ├── router/        # Vue Router
│   │   └── directives/    # 自定义指令
│   └── tests/             # 测试文件
├── docs/                   # 文档目录
└── data/                   # SQLite 数据库文件
```

## AI 辅助开发指南

使用 Claude Code 或其他 AI 工具时的建议：

1. **读取 CLAUDE.md** - 包含项目约定和指南
2. **参考架构文档** - 了解分层架构和命名约定
3. **检查 API 文档** - 已有端点和数据格式
4. **遵循代码模式** - 现有代码中的模式和约定

### 常见任务

- 添加新功能 → 参考 [架构设计](./architecture.md)
- 添加 API → 参考 [API 接口文档](./api-contracts.md)
- 修改数据库 → 参考 [数据模型](./data-models.md)
- 本地开发 → 参考 [开发指南](./development-guide.md)

---

*此文档由 BMAD Document Project 工作流自动生成*
