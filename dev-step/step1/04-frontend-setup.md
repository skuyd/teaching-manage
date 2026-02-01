# 阶段一：前端基础框架搭建

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 搭建 Vue 3 前端项目，配置路由、状态管理、HTTP 客户端，实现登录注册页面。

**Architecture:** Vue 3 Composition API + Pinia 状态管理 + Vue Router + Axios HTTP 客户端

**Tech Stack:** Vue 3, Vite, Element Plus, Pinia, Vue Router, Axios, Vitest

**依赖:** 需要先完成后端 `03-user-management.md`

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Login Page - Tech Style | aI0Hp | 深色科技风格，左侧品牌区 + 右侧登录表单 |
| Dashboard - Main Layout | M2eRL | 260px 侧边栏 + 主内容区布局 |

### 设计规范

- **主背景色**: #0A0A0B, #111113
- **强调色**: #FF5C00 (橙色渐变)
- **文字颜色**: #FFFFFF (主), #6B6B70 (辅助), #ADADB0 (标签)
- **输入框**: #1A1A1D 背景, #2A2A2E 边框, 8px 圆角
- **按钮**: 橙色渐变 (#FF5C00 → #FF8A4C), 10px 圆角, 发光阴影
- **字体**: Inter (正文), DM Mono (代码/Logo)
- **侧边栏**: 260px 宽度, #111113 背景

---

## Task 1: 初始化 Vue 3 项目

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/index.html`

**Step 1: 创建项目目录**

```bash
mkdir -p frontend/src/{views,components,stores,router,api,utils,assets}
```

**Step 2: 创建 package.json**

```json
{
  "name": "teaching-manage-frontend",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "lint": "eslint src --ext .vue,.js,.ts,.jsx,.tsx"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "axios": "^1.6.2",
    "element-plus": "^2.4.4",
    "@element-plus/icons-vue": "^2.3.1"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.5.2",
    "vite": "^5.0.10",
    "typescript": "^5.3.3",
    "vue-tsc": "^1.8.25",
    "vitest": "^1.1.0",
    "@vue/test-utils": "^2.4.3",
    "jsdom": "^23.0.1",
    "sass": "^1.69.5"
  }
}
```

**Step 3: 创建 vite.config.ts**

```typescript
// frontend/vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  test: {
    globals: true,
    environment: 'jsdom'
  }
})
```

**Step 4: 创建 tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "paths": {
      "@/*": ["./src/*"]
    },
    "types": ["vitest/globals"]
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

**Step 5: 创建 tsconfig.node.json**

```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
}
```

**Step 6: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>教学管理系统</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

**Step 7: 创建 main.ts**

```typescript
// frontend/src/main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
```

**Step 8: 创建 App.vue**

```vue
<!-- frontend/src/App.vue -->
<template>
  <router-view />
</template>

<script setup lang="ts">
// App root component
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}
</style>
```

**Step 9: 安装依赖并验证**

```bash
cd frontend && npm install
```

**Step 10: Commit**

```bash
git add frontend/
git commit -m "chore: initialize Vue 3 frontend project"
```

---

## Task 2: 配置 Vue Router

**Files:**
- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/views/Login.vue`
- Create: `frontend/src/views/Dashboard.vue`
- Create: `frontend/src/views/NotFound.vue`
- Test: `frontend/src/router/__tests__/router.test.ts`

**Step 1: 编写路由测试（红灯）**

```typescript
// frontend/src/router/__tests__/router.test.ts
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from '../index'
import { setActivePinia, createPinia } from 'pinia'

describe('Router', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should have login route', () => {
    const loginRoute = routes.find(r => r.path === '/login')
    expect(loginRoute).toBeDefined()
    expect(loginRoute?.name).toBe('Login')
  })

  it('should have dashboard route', () => {
    const dashboardRoute = routes.find(r => r.path === '/')
    expect(dashboardRoute).toBeDefined()
  })

  it('should have 404 route', () => {
    const notFoundRoute = routes.find(r => r.path === '/:pathMatch(.*)*')
    expect(notFoundRoute).toBeDefined()
    expect(notFoundRoute?.name).toBe('NotFound')
  })

  it('should redirect unauthenticated users to login', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes
    })

    // 模拟未登录状态
    localStorage.removeItem('token')

    await router.push('/')
    await router.isReady()

    // 应该被重定向到登录页
    expect(router.currentRoute.value.path).toBe('/login')
  })
})
```

**Step 2: 运行测试验证失败**

```bash
cd frontend && npm test -- router.test.ts
```

Expected: FAIL - 路由模块不存在

**Step 3: 创建路由配置**

```typescript
// frontend/src/router/index.ts
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
```

**Step 4: 创建 Login.vue 占位**

```vue
<!-- frontend/src/views/Login.vue -->
<template>
  <div class="login-page">
    <h1>登录页面</h1>
  </div>
</template>

<script setup lang="ts">
// Login page placeholder
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style>
```

**Step 5: 创建 Dashboard.vue 占位**

```vue
<!-- frontend/src/views/Dashboard.vue -->
<template>
  <div class="dashboard">
    <h1>仪表板</h1>
  </div>
</template>

<script setup lang="ts">
// Dashboard placeholder
</script>
```

**Step 6: 创建 NotFound.vue**

```vue
<!-- frontend/src/views/NotFound.vue -->
<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  gap: 16px;
}

.not-found h1 {
  font-size: 72px;
  color: #409eff;
}
</style>
```

**Step 7: 运行测试验证通过**

```bash
cd frontend && npm test -- router.test.ts
```

Expected: PASS - 4 tests passed

**Step 8: Commit**

```bash
git add frontend/src/
git commit -m "feat: add Vue Router configuration with auth guard"
```

---

## Task 3: 创建 HTTP 客户端和 API 模块

**Files:**
- Create: `frontend/src/utils/request.ts`
- Create: `frontend/src/api/auth.ts`
- Create: `frontend/src/api/types.ts`
- Test: `frontend/src/utils/__tests__/request.test.ts`

**Step 1: 编写 request 测试（红灯）**

```typescript
// frontend/src/utils/__tests__/request.test.ts
import { describe, it, expect, beforeEach, vi } from 'vitest'
import axios from 'axios'
import { request, setToken, getToken, removeToken } from '../request'

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() }
      },
      get: vi.fn(),
      post: vi.fn()
    }))
  }
}))

describe('Request Utils', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('should set token to localStorage', () => {
    setToken('test-token')
    expect(localStorage.getItem('token')).toBe('test-token')
  })

  it('should get token from localStorage', () => {
    localStorage.setItem('token', 'stored-token')
    expect(getToken()).toBe('stored-token')
  })

  it('should remove token from localStorage', () => {
    localStorage.setItem('token', 'to-remove')
    removeToken()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('should return null when no token', () => {
    expect(getToken()).toBeNull()
  })
})
```

**Step 2: 运行测试验证失败**

```bash
cd frontend && npm test -- request.test.ts
```

Expected: FAIL - request 模块不存在

**Step 3: 创建类型定义**

```typescript
// frontend/src/api/types.ts
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  name: string
  role: UserRole
}

export interface RegisterRequest {
  username: string
  password: string
  name: string
  email?: string
}

export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT'

export interface UserDTO {
  id: number
  username: string
  role: UserRole
  name: string
  email: string
  avatar: string
  createTime: string
}
```

**Step 4: 创建 request.ts**

```typescript
// frontend/src/utils/request.ts
import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { Result } from '@/api/types'

const TOKEN_KEY = 'token'

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res: Result = response.data

    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // Token 过期或无效
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error: AxiosError) => {
    console.error('Request error:', error)

    if (error.response?.status === 401) {
      removeToken()
      router.push('/login')
    }

    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export { request }
export default request
```

**Step 5: 创建 auth API**

```typescript
// frontend/src/api/auth.ts
import request from '@/utils/request'
import type { Result, LoginRequest, LoginResponse, RegisterRequest } from './types'

export function login(data: LoginRequest): Promise<Result<LoginResponse>> {
  return request.post('/auth/login', data)
}

export function register(data: RegisterRequest): Promise<Result<void>> {
  return request.post('/auth/register', data)
}

export function getCurrentUser(): Promise<Result<LoginResponse>> {
  return request.get('/users/me')
}
```

**Step 6: 运行测试验证通过**

```bash
cd frontend && npm test -- request.test.ts
```

Expected: PASS - 4 tests passed

**Step 7: Commit**

```bash
git add frontend/src/
git commit -m "feat: add HTTP client and auth API"
```

---

## Task 4: 创建用户状态管理 Store

**Files:**
- Create: `frontend/src/stores/user.ts`
- Test: `frontend/src/stores/__tests__/user.test.ts`

**Step 1: 编写 user store 测试（红灯）**

```typescript
// frontend/src/stores/__tests__/user.test.ts
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../user'
import * as authApi from '@/api/auth'
import * as requestUtils from '@/utils/request'

vi.mock('@/api/auth')
vi.mock('@/utils/request', () => ({
  setToken: vi.fn(),
  getToken: vi.fn(),
  removeToken: vi.fn()
}))

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should have initial state', () => {
    const store = useUserStore()

    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isLoggedIn).toBe(false)
  })

  it('should login and set user info', async () => {
    const store = useUserStore()
    const mockResponse = {
      code: 200,
      message: 'success',
      data: {
        token: 'test-token',
        userId: 1,
        username: 'testuser',
        name: '测试用户',
        role: 'STUDENT' as const
      }
    }

    vi.mocked(authApi.login).mockResolvedValue(mockResponse)

    await store.login({ username: 'testuser', password: 'password' })

    expect(store.token).toBe('test-token')
    expect(store.user?.username).toBe('testuser')
    expect(store.isLoggedIn).toBe(true)
    expect(requestUtils.setToken).toHaveBeenCalledWith('test-token')
  })

  it('should logout and clear user info', () => {
    const store = useUserStore()
    store.token = 'some-token'
    store.user = {
      userId: 1,
      username: 'testuser',
      name: '测试',
      role: 'STUDENT',
      token: 'some-token'
    }

    store.logout()

    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(requestUtils.removeToken).toHaveBeenCalled()
  })

  it('should check if user is admin', () => {
    const store = useUserStore()
    store.user = {
      userId: 1,
      username: 'admin',
      name: '管理员',
      role: 'ADMIN',
      token: 'token'
    }

    expect(store.isAdmin).toBe(true)
  })

  it('should check if user is teacher', () => {
    const store = useUserStore()
    store.user = {
      userId: 1,
      username: 'teacher',
      name: '教员',
      role: 'TEACHER',
      token: 'token'
    }

    expect(store.isTeacher).toBe(true)
  })
})
```

**Step 2: 运行测试验证失败**

```bash
cd frontend && npm test -- user.test.ts
```

Expected: FAIL - user store 不存在

**Step 3: 创建 user store**

```typescript
// frontend/src/stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { setToken, getToken, removeToken } from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, UserRole } from '@/api/types'

export interface UserInfo {
  userId: number
  username: string
  name: string
  role: UserRole
  token: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isTeacher = computed(() => user.value?.role === 'TEACHER')
  const isStudent = computed(() => user.value?.role === 'STUDENT')

  async function login(credentials: LoginRequest): Promise<void> {
    const res = await loginApi(credentials)
    const data = res.data

    token.value = data.token
    user.value = {
      userId: data.userId,
      username: data.username,
      name: data.name,
      role: data.role,
      token: data.token
    }

    setToken(data.token)
  }

  async function register(data: RegisterRequest): Promise<void> {
    await registerApi(data)
  }

  function logout(): void {
    token.value = null
    user.value = null
    removeToken()
  }

  function initFromStorage(): void {
    const storedToken = getToken()
    if (storedToken) {
      token.value = storedToken
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
    login,
    register,
    logout,
    initFromStorage
  }
})
```

**Step 4: 运行测试验证通过**

```bash
cd frontend && npm test -- user.test.ts
```

Expected: PASS - 5 tests passed

**Step 5: Commit**

```bash
git add frontend/src/
git commit -m "feat: add user store with Pinia"
```

---

## Task 5: 实现登录页面

**UI 设计参考:** `UI/pencil-new.pen` → Login Page - Tech Style (ID: aI0Hp)

**设计要点:**
- 左侧品牌区 (800px): 深色渐变背景, Logo, 标语, 特性图标, 装饰元素
- 右侧登录表单: 表单容器 380px 宽度, 深色输入框, 橙色渐变登录按钮
- 整体深色科技风格, 橙色强调色

**Files:**
- Modify: `frontend/src/views/Login.vue`
- Test: `frontend/src/views/__tests__/Login.test.ts`

**Step 1: 编写登录页面测试（红灯）**

```typescript
// frontend/src/views/__tests__/Login.test.ts
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ElMessage } from 'element-plus'
import Login from '../Login.vue'
import { useUserStore } from '@/stores/user'

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

describe('Login.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should render login form', () => {
    const wrapper = mount(Login, {
      global: {
        stubs: ['el-form', 'el-form-item', 'el-input', 'el-button', 'el-tabs', 'el-tab-pane']
      }
    })

    expect(wrapper.find('.login-container').exists()).toBe(true)
  })

  it('should have username and password fields', () => {
    const wrapper = mount(Login, {
      global: {
        stubs: ['el-form', 'el-form-item', 'el-input', 'el-button', 'el-tabs', 'el-tab-pane']
      }
    })

    expect(wrapper.html()).toContain('用户名')
    expect(wrapper.html()).toContain('密码')
  })

  it('should have login and register tabs', () => {
    const wrapper = mount(Login, {
      global: {
        stubs: ['el-form', 'el-form-item', 'el-input', 'el-button', 'el-tabs', 'el-tab-pane']
      }
    })

    expect(wrapper.html()).toContain('登录')
    expect(wrapper.html()).toContain('注册')
  })
})
```

**Step 2: 运行测试验证失败**

```bash
cd frontend && npm test -- Login.test.ts
```

Expected: FAIL - 登录表单未实现

**Step 3: 实现完整的登录页面**

```vue
<!-- frontend/src/views/Login.vue -->
<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">教学管理系统</h1>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-width="0"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="submit-btn"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-width="0"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>

            <el-form-item prop="name">
              <el-input
                v-model="registerForm.name"
                placeholder="姓名"
                prefix-icon="UserFilled"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱（选填）"
                prefix-icon="Message"
                size="large"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="submit-btn"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  email: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.login(loginForm)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error: any) {
      // Error already handled by interceptor
    } finally {
      loading.value = false
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.register({
        username: registerForm.username,
        password: registerForm.password,
        name: registerForm.name,
        email: registerForm.email || undefined
      })
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      loginForm.username = registerForm.username
      loginForm.password = ''
    } catch (error: any) {
      // Error already handled by interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
  font-size: 24px;
}

.tabs {
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }
}

.submit-btn {
  width: 100%;
}
</style>
```

**Step 4: 添加 Element Plus 图标**

```typescript
// 在 frontend/src/main.ts 中添加图标导入
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
```

**Step 5: 运行测试验证通过**

```bash
cd frontend && npm test -- Login.test.ts
```

Expected: PASS - 3 tests passed

**Step 6: Commit**

```bash
git add frontend/src/
git commit -m "feat: implement login page with form validation"
```

---

## Task 6: 创建布局组件

**UI 设计参考:** `UI/pencil-new.pen` → Dashboard - Main Layout (ID: M2eRL)

**设计要点:**
- 侧边栏 (260px): #111113 背景, Logo 区域, 导航菜单, 橙色选中高亮
- 主内容区: #0A0A0B 背景, 顶部导航栏
- 菜单项: lucide 图标, 悬停和选中状态

**Files:**
- Create: `frontend/src/components/Layout.vue`
- Create: `frontend/src/components/Sidebar.vue`
- Create: `frontend/src/components/Navbar.vue`
- Modify: `frontend/src/router/index.ts`

**Step 1: 创建 Layout 组件**

```vue
<!-- frontend/src/components/Layout.vue -->
<template>
  <el-container class="layout-container">
    <Sidebar />
    <el-container>
      <Navbar />
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
```

**Step 2: 创建 Sidebar 组件**

```vue
<!-- frontend/src/components/Sidebar.vue -->
<template>
  <el-aside width="220px" class="sidebar">
    <div class="logo">
      <h2>教学管理</h2>
    </div>
    <el-menu
      :default-active="activeMenu"
      router
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
    >
      <el-menu-item index="/">
        <el-icon><HomeFilled /></el-icon>
        <span>工作台</span>
      </el-menu-item>

      <el-sub-menu index="subjects" v-if="canManageSubjects">
        <template #title>
          <el-icon><Collection /></el-icon>
          <span>学科管理</span>
        </template>
        <el-menu-item index="/subjects">学科列表</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/my-courses" v-if="isStudent">
        <el-icon><Reading /></el-icon>
        <span>我的课程</span>
      </el-menu-item>

      <el-menu-item index="/groups" v-if="isStudent">
        <el-icon><UserFilled /></el-icon>
        <span>我的小组</span>
      </el-menu-item>

      <el-menu-item index="/submissions">
        <el-icon><Document /></el-icon>
        <span>作业管理</span>
      </el-menu-item>

      <el-menu-item index="/grades">
        <el-icon><DataAnalysis /></el-icon>
        <span>成绩汇总</span>
      </el-menu-item>

      <el-menu-item index="/users" v-if="isAdmin">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </el-menu-item>

      <el-menu-item index="/notifications">
        <el-icon><Bell /></el-icon>
        <span>消息中心</span>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => userStore.isAdmin)
const isTeacher = computed(() => userStore.isTeacher)
const isStudent = computed(() => userStore.isStudent)
const canManageSubjects = computed(() => isAdmin.value || isTeacher.value)
</script>

<style scoped lang="scss">
.sidebar {
  background-color: #304156;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;

  h2 {
    color: #fff;
    font-size: 18px;
    margin: 0;
  }
}

.el-menu {
  border-right: none;
}
</style>
```

**Step 3: 创建 Navbar 组件**

```vue
<!-- frontend/src/components/Navbar.vue -->
<template>
  <el-header class="navbar">
    <div class="navbar-left">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentRoute">{{ currentRoute }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="navbar-right">
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="32" icon="UserFilled" />
          <span class="username">{{ userStore.user?.name || '用户' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const routeNames: Record<string, string> = {
  '/': '工作台',
  '/subjects': '学科管理',
  '/users': '用户管理',
  '/submissions': '作业管理',
  '/grades': '成绩汇总',
  '/groups': '我的小组',
  '/notifications': '消息中心'
}

const currentRoute = computed(() => routeNames[route.path] || '')

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    })
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped lang="scss">
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.navbar-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  .username {
    color: #606266;
  }
}
</style>
```

**Step 4: 更新路由配置**

```typescript
// frontend/src/router/index.ts - 更新后的版本
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/components/Layout.vue'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/users/UserList.vue'),
        meta: { roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
```

**Step 5: 创建占位视图**

```vue
<!-- frontend/src/views/users/UserList.vue -->
<template>
  <div class="user-list">
    <h2>用户管理</h2>
    <!-- 待实现 -->
  </div>
</template>

<script setup lang="ts">
// User list placeholder
</script>
```

```vue
<!-- frontend/src/views/Profile.vue -->
<template>
  <div class="profile">
    <h2>个人信息</h2>
    <!-- 待实现 -->
  </div>
</template>

<script setup lang="ts">
// Profile placeholder
</script>
```

**Step 6: 更新 Dashboard**

```vue
<!-- frontend/src/views/Dashboard.vue -->
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <span>欢迎回来</span>
          </template>
          <div class="welcome">
            <el-avatar :size="64" icon="UserFilled" />
            <div class="info">
              <h3>{{ userStore.user?.name }}</h3>
              <el-tag :type="roleTagType">{{ roleText }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" v-if="canManageSubjects">管理学科</el-button>
            <el-button v-if="isStudent">我的作业</el-button>
            <el-button>消息中心</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <span>系统公告</span>
          </template>
          <p class="announcement">欢迎使用教学管理系统！</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const isAdmin = computed(() => userStore.isAdmin)
const isTeacher = computed(() => userStore.isTeacher)
const isStudent = computed(() => userStore.isStudent)
const canManageSubjects = computed(() => isAdmin.value || isTeacher.value)

const roleText = computed(() => {
  if (isAdmin.value) return '系统管理员'
  if (isTeacher.value) return '教员'
  return '学员'
})

const roleTagType = computed(() => {
  if (isAdmin.value) return 'danger'
  if (isTeacher.value) return 'warning'
  return 'success'
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card {
    margin-bottom: 20px;
  }

  .welcome {
    display: flex;
    align-items: center;
    gap: 16px;

    .info {
      h3 {
        margin: 0 0 8px 0;
      }
    }
  }

  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .announcement {
    color: #606266;
  }
}
</style>
```

**Step 7: Commit**

```bash
git add frontend/src/
git commit -m "feat: add layout components and update routing"
```

---

## 验证清单

完成 Task 1-6 后，验证：

1. **安装依赖**
   ```bash
   cd frontend && npm install
   ```

2. **运行测试**
   ```bash
   cd frontend && npm test
   ```

3. **启动开发服务器**
   ```bash
   cd frontend && npm run dev
   ```

4. **访问页面**
   - 登录页：http://localhost:5173/login
   - 使用 admin/admin123 登录
   - 验证跳转到仪表板
   - 验证侧边栏和导航栏显示正确

5. **验证路由守卫**
   - 退出登录后访问首页应跳转到登录页
   - 登录后访问登录页应跳转到首页

下一步：继续 `05-summary.md` 查看阶段一总结。
