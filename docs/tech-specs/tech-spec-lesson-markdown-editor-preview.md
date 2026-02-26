---
title: '课程内容 Markdown 编辑与预览系统'
slug: 'lesson-markdown-editor-preview'
created: '2026-02-26'
status: 'ready-for-dev'
stepsCompleted: [1, 2, 3, 4]
tech_stack:
  - Vue 3.4 + Composition API + <script setup>
  - Element Plus 2.4
  - Vditor (Markdown 编辑器)
  - Spring Boot 3.2.2
  - MyBatis-Plus
files_to_modify:
  - frontend/src/views/subjects/LessonForm.vue
  - frontend/src/views/subjects/SubjectDetail.vue
  - frontend/src/components/MainLayout.vue
  - frontend/src/router/index.ts
  - frontend/package.json
  - backend/src/main/java/com/teaching/service/FileService.java
  - backend/src/main/java/com/teaching/controller/LessonController.java
  - frontend/src/api/lesson.ts
files_to_create:
  - frontend/src/components/common/MarkdownEditor.vue
  - frontend/src/components/common/MarkdownPreview.vue
  - frontend/src/components/lessons/LessonCalendar.vue
  - frontend/src/components/lessons/LessonDetailDialog.vue
  - frontend/src/views/student/StudentCalendar.vue
  - frontend/src/api/file.ts
  - backend/src/main/java/com/teaching/controller/FileController.java
code_patterns:
  - 组件使用 <script setup lang="ts"> 语法
  - 对话框使用 v-model 双向绑定 + defineEmits
  - API 调用使用 @/utils/request 封装的 axios
  - 表单验证使用 Element Plus FormRules
  - 权限控制使用 userStore.isTeacher/isAdmin/isStudent
test_patterns:
  - Vitest 单元测试
  - Vue Test Utils 组件测试
  - Playwright E2E 测试
---

# Tech-Spec: 课程内容 Markdown 编辑与预览系统

**Created:** 2026-02-26

## Overview

### Problem Statement

当前教学管理系统存在以下问题：

1. **教师/管理员端**：课程内容 (`content`) 和作业要求 (`homework_desc`) 字段使用普通 `el-input textarea`，虽然提示"支持 Markdown 格式"，但没有专用编辑器和实时预览功能，用户体验差。

2. **学员端**：
   - 没有独立的日历视图来查看所参加学科的全部课程
   - 点击课程只显示简单的 `ElMessage.info()` 提示（见 SubjectDetail.vue:455-456）
   - 学员无法方便地预览课程内容和作业要求的 Markdown 渲染结果

### Solution

1. **集成 Vditor Markdown 编辑器**：替换 LessonForm.vue 中的普通文本框
2. **实现图片上传功能**：后端新增 `/api/files/images` 接口
3. **提取日历组件**：将 SubjectDetail.vue 中的日历逻辑提取为独立组件
4. **创建学员日历页面**：复用日历组件，聚合显示学员所有已加入学科的课程
5. **创建课程详情预览弹窗**：学员点击课程时展示渲染后的 Markdown 内容

### Scope

**In Scope:**
- Vditor Markdown 编辑器集成（`content` 和 `homeworkDesc` 字段）
- 图片上传接口和 Vditor 集成
- 代码块语法高亮（Vditor 内置）
- 日历组件提取（LessonCalendar.vue）
- 学员日历视图页面（聚合多学科课程）
- 课程详情预览对话框（LessonDetailDialog.vue）
- 路由和侧边栏菜单配置

**Out of Scope:**
- LaTeX 数学公式渲染
- 实时协作编辑
- 版本历史/修订记录
- Markdown 模板功能
- 附件上传（仅支持图片）

## Context for Development

### Codebase Patterns

**组件模式：**
```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
const props = defineProps<{ ... }>()
const emit = defineEmits<{ ... }>()
</script>
```

**对话框模式：**
```vue
<!-- 父组件 -->
<LessonForm v-model="dialogVisible" :lesson="currentLesson" @success="handleSuccess" />

<!-- 子组件 -->
const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [boolean], success: [] }>()
```

**API 调用模式：**
```typescript
import request from '@/utils/request'
export const createXxx = (data: CreateRequest) => request.post<Result<XxxDTO>>('/api/xxx', data)
```

### Files to Reference

| File | Purpose | 行数 |
| ---- | ------- | ---- |
| `frontend/src/views/subjects/LessonForm.vue` | 课程表单，需集成 Vditor | 197 |
| `frontend/src/views/subjects/SubjectDetail.vue` | 日历视图源码，需提取组件 | 998 |
| `frontend/src/components/MainLayout.vue` | 侧边栏菜单，需添加学员入口 | 318 |
| `backend/src/main/java/com/teaching/service/FileService.java` | 文件上传逻辑，参考 uploadAvatar() | 300 |

### Technical Decisions

| 决策 | 选择 | 理由 |
|------|------|------|
| Markdown 编辑器 | Vditor | 功能全面、中文友好、支持即时渲染/分屏预览 |
| 编辑器模式 | ir (即时渲染) | 平衡编辑体验和预览效果 |
| 图片存储路径 | `/uploads/images/{yyyy-MM}/{uuid}.{ext}` | 按月份组织，避免单目录文件过多 |
| 日历组件提取 | 保留原有逻辑，通过 props 控制只读模式 | 最小化代码变更 |

## Implementation Plan

### Tasks

#### Phase 1: 基础组件

- [ ] **Task 1.1**: 安装 Vditor 依赖
  - File: `frontend/package.json`
  - Action: 运行 `cd frontend && npm install vditor`
  - Notes: Vditor 版本使用最新稳定版

- [ ] **Task 1.2**: 创建 MarkdownEditor 组件
  - File: `frontend/src/components/common/MarkdownEditor.vue` (新建)
  - Action: 封装 Vditor 编辑器
  - Props: `modelValue: string`, `height?: number`, `placeholder?: string`
  - Emits: `update:modelValue`
  - Features:
    - 使用 `ir` 模式（即时渲染）
    - 配置工具栏：标题、粗体、斜体、列表、代码块、表格、链接、图片
    - 实现图片上传回调 `upload.handler`
    - 监听内容变化通过 `input` 事件触发 emit

- [ ] **Task 1.3**: 创建 MarkdownPreview 组件
  - File: `frontend/src/components/common/MarkdownPreview.vue` (新建)
  - Action: 使用 Vditor.preview() 静态方法渲染 Markdown
  - Props: `content: string`
  - Notes:
    - 使用 `watch` 监听 content 变化重新渲染
    - 添加 `.markdown-body` 样式类

#### Phase 2: 图片上传接口

- [ ] **Task 2.1**: 后端新增图片上传方法
  - File: `backend/src/main/java/com/teaching/service/FileService.java`
  - Action: 添加 `uploadImage(MultipartFile file)` 方法
  - Logic:
    ```java
    // 1. 验证文件类型 (jpg/jpeg/png/gif/webp)
    // 2. 验证文件大小 (<=5MB)
    // 3. 生成路径: /uploads/images/{yyyy-MM}/{UUID}.{ext}
    // 4. 保存文件
    // 5. 返回访问 URL
    ```

- [ ] **Task 2.2**: 后端新增图片上传控制器
  - File: `backend/src/main/java/com/teaching/controller/FileController.java` (新建)
  - Endpoint: `POST /api/files/images`
  - Request: `@RequestParam("file") MultipartFile file`
  - Response: `Result<Map<String, String>>` 包含 `{ "url": "/uploads/images/..." }`
  - Security: `@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")`

- [ ] **Task 2.3**: 前端新增文件 API
  - File: `frontend/src/api/file.ts` (新建)
  - Action: 封装 `uploadImage(file: File): Promise<Result<{ url: string }>>`
  - Notes: 使用 FormData 上传

#### Phase 3: 编辑器集成

- [ ] **Task 3.1**: 改造 LessonForm 课程内容字段
  - File: `frontend/src/views/subjects/LessonForm.vue`
  - Action:
    - 导入 MarkdownEditor 组件
    - 替换第 18-25 行的 `el-input textarea` 为 `<MarkdownEditor v-model="form.content" :height="250" placeholder="请输入课程内容，支持 Markdown 格式" />`
  - Notes: 保持表单验证逻辑不变

- [ ] **Task 3.2**: 改造 LessonForm 作业要求字段
  - File: `frontend/src/views/subjects/LessonForm.vue`
  - Action: 替换第 37-44 行的 `el-input textarea` 为 `<MarkdownEditor v-model="form.homeworkDesc" :height="200" placeholder="请输入作业要求，支持 Markdown 格式" />`

- [ ] **Task 3.3**: 调整对话框宽度
  - File: `frontend/src/views/subjects/LessonForm.vue`
  - Action: 将 `width="700px"` 改为 `width="900px"` 以适应编辑器

#### Phase 4: 课程详情预览

- [ ] **Task 4.1**: 创建 LessonDetailDialog 组件
  - File: `frontend/src/components/lessons/LessonDetailDialog.vue` (新建)
  - Props: `modelValue: boolean`, `lesson: LessonDTO | null`
  - Template 结构:
    ```
    el-dialog
      ├── 头部：学科名称 + 课程标题
      ├── 元信息：上课时间、截止时间、提交类型
      ├── 课程内容：MarkdownPreview
      ├── 作业要求：MarkdownPreview
      └── 底部按钮：关闭、去提交作业
    ```

- [ ] **Task 4.2**: 集成到 SubjectDetail
  - File: `frontend/src/views/subjects/SubjectDetail.vue`
  - Action:
    - 导入 LessonDetailDialog 组件
    - 添加 `detailDialogVisible` 和 `detailLesson` 状态
    - 修改 `viewLesson()` 方法（第 449-457 行）：学员点击时打开 LessonDetailDialog 而非 ElMessage

#### Phase 5: 日历组件提取

- [ ] **Task 5.1**: 创建 LessonCalendar 组件
  - File: `frontend/src/components/lessons/LessonCalendar.vue` (新建)
  - Props:
    - `lessons: LessonDTO[]` - 课程列表
    - `readonly?: boolean` - 只读模式（禁用拖拽和新增）
    - `multiSubject?: boolean` - 多学科模式（显示学科名称标签）
  - Emits:
    - `lesson-click: [lesson: LessonDTO]` - 点击课程
    - `lesson-drag: [lessonId: number, newTime: string]` - 拖拽更新
    - `day-click: [date: string]` - 点击日期（非只读模式）
  - Action: 从 SubjectDetail.vue 提取以下内容：
    - 第 236-242 行：日历状态定义
    - 第 244-309 行：周视图计算属性
    - 第 311-386 行：月视图计算属性
    - 第 388-407 行：格式化函数
    - 第 508-578 行：拖拽处理函数
    - 第 677-996 行：日历相关样式

- [ ] **Task 5.2**: 重构 SubjectDetail 使用 LessonCalendar
  - File: `frontend/src/views/subjects/SubjectDetail.vue`
  - Action:
    - 导入 LessonCalendar 组件
    - 替换内联日历代码为 `<LessonCalendar :lessons="lessons" @lesson-click="viewLesson" @lesson-drag="handleLessonDrag" @day-click="handleDayClick" />`
    - 删除已提取的日历相关代码
    - 保留学生管理 Tab 和课程表单对话框

#### Phase 6: 学员日历页面

- [ ] **Task 6.1**: 后端新增学员课程 API
  - File: `backend/src/main/java/com/teaching/controller/LessonController.java`
  - Endpoint: `GET /api/lessons/my`
  - Logic:
    - 获取当前用户 ID
    - 查询用户已加入的所有学科 ID
    - 查询这些学科下的所有课程
    - 返回包含 `subjectName` 的课程列表
  - Response: `Result<List<LessonDTO>>` (LessonDTO 需包含 subjectName)

- [ ] **Task 6.2**: 前端新增 API 方法
  - File: `frontend/src/api/lesson.ts`
  - Action: 添加 `export const getMyLessons = () => request.get<Result<LessonDTO[]>>('/api/lessons/my')`

- [ ] **Task 6.3**: 创建 StudentCalendar 页面
  - File: `frontend/src/views/student/StudentCalendar.vue` (新建)
  - Template:
    ```
    MainLayout
      ├── 头部：标题 "我的课程日历"
      ├── LessonCalendar :lessons="lessons" :readonly="true" :multi-subject="true" @lesson-click="showDetail"
      └── LessonDetailDialog v-model="dialogVisible" :lesson="currentLesson"
    ```
  - Script:
    - `onMounted` 调用 `getMyLessons()` 加载数据
    - 处理课程点击事件显示详情

- [ ] **Task 6.4**: 添加路由配置
  - File: `frontend/src/router/index.ts`
  - Action: 在 `/profile` 路由前添加：
    ```typescript
    {
      path: '/student/calendar',
      name: 'StudentCalendar',
      component: () => import('@/views/student/StudentCalendar.vue'),
      meta: { requiresAuth: true, roles: ['STUDENT'] }
    }
    ```

- [ ] **Task 6.5**: 添加侧边栏菜单
  - File: `frontend/src/components/MainLayout.vue`
  - Action: 在第 23 行 `作业管理` 菜单项后添加：
    ```vue
    <el-menu-item index="/student/calendar" v-if="userStore.isStudent">
      <el-icon><Calendar /></el-icon>
      <span>课程日历</span>
    </el-menu-item>
    ```
  - Notes: 需要导入 `Calendar` 图标

### Acceptance Criteria

#### AC1: Markdown 编辑器显示
- [ ] **Given** 教员/管理员打开"新增课程"或"编辑课程"对话框
- [ ] **When** 对话框加载完成
- [ ] **Then** "课程内容"和"作业要求"字段显示 Vditor 编辑器，带有工具栏

#### AC2: Markdown 编辑功能
- [ ] **Given** 教员在课程内容编辑器中
- [ ] **When** 输入 `# 标题` 或 `**粗体**` 等 Markdown 语法
- [ ] **Then** 编辑器即时渲染显示效果（标题变大、文字加粗）

#### AC3: 代码高亮
- [ ] **Given** 教员在编辑器中
- [ ] **When** 输入 ` ```java\nSystem.out.println("Hello");\n``` `
- [ ] **Then** 代码块显示语法高亮

#### AC4: 图片上传
- [ ] **Given** 教员在 Markdown 编辑器中
- [ ] **When** 粘贴剪贴板图片或拖拽图片到编辑器
- [ ] **Then** 图片自动上传，上传完成后插入 `![](url)` 格式的链接

#### AC5: 图片上传失败处理
- [ ] **Given** 教员尝试上传超过 5MB 的图片
- [ ] **When** 上传请求发送
- [ ] **Then** 显示错误提示 "图片大小不能超过 5MB"

#### AC6: 课程保存
- [ ] **Given** 教员在编辑器中输入了 Markdown 内容
- [ ] **When** 点击"确定"保存课程
- [ ] **Then** Markdown 源码正确保存到数据库，再次编辑时内容回显正确

#### AC7: 学员日历入口
- [ ] **Given** 学员登录系统
- [ ] **When** 查看侧边栏菜单
- [ ] **Then** 显示"课程日历"菜单项（教员/管理员不显示此项）

#### AC8: 学员日历显示
- [ ] **Given** 学员点击"课程日历"菜单
- [ ] **When** 页面加载完成
- [ ] **Then** 显示所有已加入学科的课程，课程卡片显示学科名称

#### AC9: 学员日历只读
- [ ] **Given** 学员在课程日历页面
- [ ] **When** 尝试拖拽课程卡片
- [ ] **Then** 拖拽无效，课程位置不变

#### AC10: 学员日历点击空白
- [ ] **Given** 学员在课程日历页面
- [ ] **When** 点击日历空白区域
- [ ] **Then** 无任何响应（不弹出新增对话框）

#### AC11: 课程详情预览
- [ ] **Given** 学员在日历中看到课程
- [ ] **When** 点击课程卡片
- [ ] **Then** 弹出详情对话框，显示渲染后的 HTML 内容（非 Markdown 源码）

#### AC12: 详情对话框内容
- [ ] **Given** 学员打开课程详情对话框
- [ ] **When** 查看对话框内容
- [ ] **Then** 显示：课程标题、上课时间、截止时间、提交类型、课程内容（渲染）、作业要求（渲染）

#### AC13: 跳转提交作业
- [ ] **Given** 学员在课程详情对话框中
- [ ] **When** 点击"去提交作业"按钮
- [ ] **Then** 跳转到作业提交页面，并带上课程 ID 参数

## Additional Context

### Dependencies

**前端新增依赖：**
```bash
npm install vditor
```

**后端依赖：** 无需新增（复用现有 Spring Boot multipart 配置）

### File Size Limits

- 图片上传限制：5MB
- 支持格式：jpg, jpeg, png, gif, webp
- 存储路径：`/uploads/images/{yyyy-MM}/{uuid}.{ext}`

### Color Scheme for Multi-Subject Calendar

学科颜色映射（按学科 ID 取模分配）：
```typescript
const subjectColors = [
  'var(--el-color-primary)',   // 蓝色
  'var(--el-color-success)',   // 绿色
  'var(--el-color-warning)',   // 橙色
  'var(--el-color-danger)',    // 红色
  'var(--el-color-info)'       // 灰色
]
const getSubjectColor = (subjectId: number) => subjectColors[subjectId % 5]
```

### Testing Strategy

**单元测试：**
- MarkdownEditor 组件：v-model 双向绑定测试
- MarkdownPreview 组件：内容渲染测试
- 图片上传 API：文件类型/大小验证测试

**集成测试：**
- FileController 图片上传接口测试
- LessonController `/api/lessons/my` 接口测试

**E2E 测试：**
- 教员创建带 Markdown 内容的课程
- 学员查看课程日历
- 学员点击课程查看详情

### Risk Assessment

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Vditor CDN 加载慢 | 编辑器初始化延迟 | 配置本地资源或使用 npm 包内置资源 |
| 图片上传失败 | 用户体验差 | 添加重试机制和友好错误提示 |
| 日历组件提取遗漏 | 功能回归 | 提取后对比测试原有功能 |

### Notes

- Vditor 初始化需要在 `onMounted` 中进行，确保 DOM 已渲染
- 学员日历需要一次性加载所有学科课程，数据量大时考虑分页或懒加载
- 日历组件提取时保持与原有样式完全一致，避免视觉回归
