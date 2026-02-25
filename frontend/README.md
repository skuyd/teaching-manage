# 教学管理系统 - 前端

基于 Vue 3 + TypeScript + Element Plus 的现代化前端应用。

## 技术栈

- **Vue 3** - Composition API
- **TypeScript** - 类型安全
- **Vite** - 构建工具
- **Element Plus** - UI 组件库
- **Pinia** - 状态管理
- **Monaco Editor** - 代码编辑器
- **Vitest** - 单元测试

## 快速开始

```bash
# 安装依赖
npm install

# 开发服务器
npm run dev

# 生产构建
npm run build

# 运行测试
npm run test

# 代码检查
npm run lint
```

## 项目结构

```
src/
├── api/                    # API 接口定义
├── components/             # 公共组件
│   └── common/            # 通用组件
│       └── ThemeSwitcher.vue
├── composables/           # 组合式函数
│   └── useTheme.ts       # 主题管理 composable
├── router/                # 路由配置
├── stores/                # Pinia 状态管理
│   ├── theme.ts          # 主题 Store
│   └── user.ts           # 用户 Store
├── styles/                # 样式文件
│   └── themes/           # 主题相关样式
│       ├── variables.css         # 通用设计 Token
│       ├── theme-chinese-red.css # 中国红主题
│       ├── theme-tech-blue.css   # 科技蓝主题
│       ├── theme-nature-green.css # 自然绿主题
│       └── transitions.css       # 过渡动画
├── views/                 # 页面视图
│   └── Login.vue         # 登录页（三主题支持）
└── utils/                 # 工具函数
```

## 主题系统

### 概述

系统支持三种主题：

| 主题 | 名称 | 主色调 | 描述 |
|------|------|--------|------|
| `chinese-red` | 中国红 | #C41E3A | 温暖喜庆的传统风格 |
| `tech-blue` | 科技蓝 | #3B82F6 | 现代专业的科技风格（默认） |
| `nature-green` | 自然绿 | #10B981 | 清新护眼的自然风格 |

### 使用 ThemeSwitcher 组件

```vue
<template>
  <!-- 下拉菜单模式（默认） -->
  <ThemeSwitcher />

  <!-- 按钮组模式 -->
  <ThemeSwitcher mode="buttons" />

  <!-- 循环切换模式 -->
  <ThemeSwitcher mode="toggle" />

  <!-- 卡片选择模式 -->
  <ThemeSwitcher mode="cards" />

  <!-- 自定义选项 -->
  <ThemeSwitcher
    mode="cards"
    :show-message="false"
    :enable-transition="true"
    @change="handleThemeChange"
  />
</template>

<script setup lang="ts">
import ThemeSwitcher from '@/components/common/ThemeSwitcher.vue'
import type { ThemeName } from '@/stores/theme'

function handleThemeChange(theme: ThemeName) {
  console.log('Theme changed to:', theme)
}
</script>
```

#### Props

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `mode` | `'dropdown' \| 'buttons' \| 'toggle' \| 'cards'` | `'dropdown'` | 显示模式 |
| `showMessage` | `boolean` | `true` | 切换成功时是否显示消息 |
| `enableTransition` | `boolean` | `true` | 是否启用过渡动画 |

#### Events

| 事件 | 参数 | 描述 |
|------|------|------|
| `change` | `(theme: ThemeName)` | 主题切换时触发 |

### 使用 useTheme Composable

```typescript
import { useTheme, useThemeAutoInit, useThemeWatcher } from '@/composables/useTheme'

// 基础用法
const {
  currentTheme,       // 当前主题名称
  themeMetadata,      // 当前主题元数据
  availableThemes,    // 所有可用主题
  isTransitioning,    // 是否正在过渡中
  isSyncing,          // 是否正在同步到后端
  setTheme,           // 设置主题
  cycleTheme,         // 循环切换
  resetTheme,         // 重置为默认主题
  syncThemeToBackend, // 同步到后端
} = useTheme()

// 设置主题
await setTheme('chinese-red', {
  persist: true,      // 是否持久化到 localStorage
  sync: true,         // 是否同步到后端
  transition: true    // 是否启用过渡动画
})

// 循环切换主题
await cycleTheme({ sync: true, transition: true })

// 在 App.vue 中自动初始化主题
useThemeAutoInit()

// 监听主题变化
useThemeWatcher((newTheme, oldTheme) => {
  console.log(`Theme changed: ${oldTheme} -> ${newTheme}`)
})
```

### 主题开发指南

#### 1. CSS 变量使用

在组件中使用主题变量：

```scss
.my-component {
  // 颜色
  color: var(--text-primary);
  background-color: var(--bg-card);
  border-color: var(--border-default);

  // 主题色
  .primary-button {
    background-color: var(--color-primary);
    color: var(--color-on-primary);
  }

  // 过渡动画
  transition: all var(--transition-normal);
}
```

#### 2. 主要 CSS 变量

```css
/* 主题色 */
--color-primary           /* 主色调 */
--color-primary-light     /* 主色调浅色 */
--color-primary-dark      /* 主色调深色 */
--color-primary-bg        /* 主色调背景 */
--color-on-primary        /* 主色调上的文字 */

/* 文字颜色 */
--text-primary            /* 主要文字 */
--text-secondary          /* 次要文字 */
--text-placeholder        /* 占位符文字 */
--text-disabled           /* 禁用文字 */

/* 背景颜色 */
--bg-page                 /* 页面背景 */
--bg-card                 /* 卡片背景 */
--bg-overlay              /* 遮罩背景 */

/* 边框 */
--border-default          /* 默认边框 */
--border-light            /* 浅色边框 */

/* 圆角 */
--radius-sm               /* 小圆角 (4px) */
--radius-md               /* 中圆角 (8px) */
--radius-lg               /* 大圆角 (12px) */
--radius-full             /* 完全圆角 */

/* 阴影 */
--shadow-sm               /* 小阴影 */
--shadow-md               /* 中阴影 */
--shadow-lg               /* 大阴影 */

/* 过渡 */
--transition-fast         /* 快速过渡 (150ms) */
--transition-normal       /* 正常过渡 (250ms) */
--transition-slow         /* 慢速过渡 (350ms) */
```

#### 3. 新增主题

1. 创建主题 CSS 文件 `src/styles/themes/theme-{name}.css`
2. 在 `src/stores/theme.ts` 中添加主题配置
3. 在 `src/styles/main.scss` 中导入新主题

## 测试

```bash
# 运行所有测试
npm run test

# 运行测试并显示 UI
npm run test:ui

# 运行测试并生成覆盖率报告
npm run test -- --coverage
```

### 测试覆盖率

主题系统测试覆盖率：**99.41%**

| 文件 | 语句 | 分支 | 函数 | 行 |
|------|------|------|------|------|
| theme.ts (Store) | 100% | 100% | 100% | 100% |
| useTheme.ts (Composable) | 98.59% | 94.44% | 100% | 98.59% |
| ThemeSwitcher.vue | 100% | 100% | 100% | 100% |

## API 集成

### 用户偏好 API

```typescript
// 获取用户偏好
GET /api/users/me/preferences

// 响应
{
  "code": 200,
  "data": {
    "theme": "tech-blue"
  }
}

// 更新用户偏好
PATCH /api/users/me/preferences
Content-Type: application/json

{
  "theme": "chinese-red"
}
```

### 前端 API 调用

```typescript
import { getUserPreferences, updateUserPreferences } from '@/api/user'

// 获取偏好
const response = await getUserPreferences()
console.log(response.data.theme)

// 更新偏好
await updateUserPreferences({ theme: 'nature-green' })
```

## 登录页

登录页采用现代化设计，支持三种主题切换。

### 布局结构

**桌面端 (>=1280px)：**
```
+---------------------------+---------------------------+
|                           |                           |
|      品牌展示区            |      登录表单区            |
|                           |                           |
|   Logo + 标题 + 标语       |     玻璃态登录卡片          |
|   装饰性动画图形           |     用户名/密码输入         |
|                           |     登录按钮               |
|                           |                           |
+---------------------------+---------------------------+
     50% 宽度                      50% 宽度
```

**平板端 (768-1279px)：** 垂直堆叠（品牌区在上，表单区在下）

**移动端 (<768px)：** 仅显示表单区，品牌区隐藏

### 主题效果

| 主题 | 背景渐变 | 强调色 | 按钮样式 |
|------|----------|--------|----------|
| 中国红 | 深红渐变 | 金色 | 红金渐变 |
| 科技蓝 | 深蓝渐变 | 青色 | 蓝色渐变 |
| 自然绿 | 深绿渐变 | 翠绿 | 绿色渐变 |

### 动画效果

- **页面加载**：0.6s 淡入
- **品牌内容**：0.8s 从左滑入
- **登录卡片**：0.8s 从右滑入
- **Logo**：3s 无限悬浮动画
- **装饰图形**：6-8s 无限脉冲动画
- **输入框聚焦**：缩放 + 阴影变化
- **按钮悬停**：上移 + 阴影增强

### 使用示例

```vue
<template>
  <Login />
</template>

<script setup>
import Login from '@/views/Login.vue'
</script>
```

### 测试覆盖

登录页测试覆盖率：**100%**（语句/函数/行）、**88.88%**（分支）

| 测试类别 | 测试数量 | 覆盖内容 |
|----------|----------|----------|
| 组件渲染 | 15 | 结构元素、输入框、按钮 |
| 主题集成 | 6 | useThemeAutoInit、CSS 变量 |
| 响应式 | 4 | 桌面/平板/移动端布局 |
| 动画 | 7 | 页面加载、滑入、悬浮效果 |
| 表单功能 | 22 | 验证、提交、错误处理 |
| 可访问性 | 6 | ARIA、键盘导航、焦点管理 |
| 边界情况 | 6 | 特殊字符、网络错误、快速点击 |

## 最佳实践

1. **使用 CSS 变量** - 不要硬编码颜色值
2. **启用过渡动画** - 提升用户体验
3. **同步到后端** - 登录用户应同步主题偏好
4. **测试覆盖** - 新功能需要添加测试

## 相关文档

- [主项目 README](../README.md)
- [API 文档](../docs/api-contracts.md)
- [UX 设计规范](../_bmad-output/planning-artifacts/ux-design-specification.md)
