# 阶段四：UI优化与安全加固

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 优化系统UI体验，完善权限控制，加强安全防护。

**Tech Stack:** Vue 3, Element Plus, Spring Security

**依赖:** 需要先完成 `01-notification.md`

---

## UI 设计参考

**重要：前端页面开发必须严格参照 `UI/pencil-new.pen` 设计稿实现。**

### 本阶段相关设计页面

| 页面 | 设计稿 ID | 设计要点 |
|------|-----------|----------|
| Dashboard - Main Layout | M2eRL | 仪表板统计卡片，快捷操作 |
| Profile Page | VcjCT | 个人资料编辑表单 |
| User Management Page | VrQIh | 用户列表，角色管理 |

### 设计规范

- **统计卡片**: 深色背景，数字大字体，图标装饰
- **仪表板**: 按角色显示不同内容
- **个人资料**: 头像上传，信息编辑表单
- **用户管理**: 表格列表，批量操作，搜索筛选
- **响应式**: 移动端适配，抽屉式侧边栏

---

## Task 1: 仪表板优化

**Files:**
- Update: `frontend/src/views/Dashboard.vue`

**Step 1: 创建角色化仪表板**

```vue
<!-- frontend/src/views/Dashboard.vue -->
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <!-- 欢迎信息 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>{{ greeting }}，{{ userStore.userInfo?.name }}</h2>
              <p>{{ getWelcomeMessage() }}</p>
            </div>
            <div class="welcome-avatar">
              <el-avatar :size="64">
                {{ userStore.userInfo?.name?.charAt(0) }}
              </el-avatar>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 管理员/教员视图 -->
    <template v-if="userStore.isAdmin || userStore.isTeacher">
      <el-row :gutter="20" class="stat-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="学科数量" :value="stats.subjectCount" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="课程数量" :value="stats.lessonCount" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="待评分作业" :value="stats.pendingGrades">
              <template #suffix>
                <el-button v-if="stats.pendingGrades > 0" type="primary" link size="small">
                  去评分
                </el-button>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="学员总数" :value="stats.studentCount" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 待处理事项 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>待评分作业</template>
            <el-table :data="pendingSubmissions" stripe size="small">
              <el-table-column prop="lessonTitle" label="课程" />
              <el-table-column prop="submitterName" label="提交者" width="100" />
              <el-table-column prop="submitTime" label="提交时间" width="160" />
              <el-table-column label="操作" width="80">
                <template #default="{ row }">
                  <el-button size="small" link @click="goToGrade(row)">评分</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>最近课程</template>
            <el-timeline>
              <el-timeline-item
                v-for="lesson in recentLessons"
                :key="lesson.id"
                :timestamp="lesson.lessonTime"
                placement="top"
              >
                <el-card shadow="never">
                  <h4>{{ lesson.title }}</h4>
                  <p>{{ lesson.subjectName }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 学员视图 -->
    <template v-if="userStore.isStudent">
      <el-row :gutter="20" class="stat-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="参与学科" :value="stats.mySubjectCount" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="已提交作业" :value="stats.mySubmissionCount" />
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="待提交作业" :value="stats.myPendingCount">
              <template #suffix>
                <el-tag v-if="stats.myPendingCount > 0" type="warning" size="small">
                  待完成
                </el-tag>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="平均成绩" :value="stats.myAverageGrade || '-'" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 我的作业 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>待提交作业</template>
            <el-table :data="myPendingHomework" stripe size="small">
              <el-table-column prop="lessonTitle" label="课程" />
              <el-table-column prop="deadline" label="截止时间" width="160" />
              <el-table-column label="操作" width="80">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="goToSubmit(row)">
                    提交
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>最新成绩</template>
            <el-table :data="myRecentGrades" stripe size="small">
              <el-table-column prop="lessonTitle" label="课程" />
              <el-table-column label="成绩" width="80">
                <template #default="{ row }">
                  <el-tag :type="getGradeType(row.grade)">{{ row.grade }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="gradeTime" label="评分时间" width="160" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getGradeType } from '@/api/grade'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({
  subjectCount: 0,
  lessonCount: 0,
  pendingGrades: 0,
  studentCount: 0,
  mySubjectCount: 0,
  mySubmissionCount: 0,
  myPendingCount: 0,
  myAverageGrade: null
})

const pendingSubmissions = ref([])
const recentLessons = ref([])
const myPendingHomework = ref([])
const myRecentGrades = ref([])

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

function getWelcomeMessage() {
  if (userStore.isAdmin) return '系统运行正常，今天也要加油！'
  if (userStore.isTeacher) return '今天有新的作业等待评分'
  return '继续学习，保持进步！'
}

function goToGrade(row: any) {
  router.push(`/submissions/${row.id}`)
}

function goToSubmit(row: any) {
  router.push(`/lessons/${row.lessonId}`)
}

onMounted(async () => {
  // 加载仪表板数据
  // TODO: 实现具体的API调用
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.welcome-text p {
  margin: 0;
  opacity: 0.9;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}
</style>
```

**Step 2: Commit**

```bash
git add frontend/src/
git commit -m "feat: enhance dashboard with role-based views"
```

---

## Task 2: 响应式布局优化

**Files:**
- Create: `frontend/src/styles/responsive.scss`
- Update: `frontend/src/App.vue`

**Step 1: 创建响应式样式**

```scss
// frontend/src/styles/responsive.scss

// 断点定义
$breakpoints: (
  'xs': 0,
  'sm': 576px,
  'md': 768px,
  'lg': 992px,
  'xl': 1200px,
  'xxl': 1400px
);

// 媒体查询 mixin
@mixin respond-to($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media (min-width: map-get($breakpoints, $breakpoint)) {
      @content;
    }
  }
}

@mixin respond-below($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media (max-width: map-get($breakpoints, $breakpoint) - 1px) {
      @content;
    }
  }
}

// 响应式工具类
.hidden-xs {
  @include respond-below('sm') {
    display: none !important;
  }
}

.hidden-sm {
  @include respond-below('md') {
    display: none !important;
  }
}

.visible-xs {
  display: none !important;
  @include respond-below('sm') {
    display: block !important;
  }
}

// 响应式间距
.px-responsive {
  padding-left: 16px;
  padding-right: 16px;

  @include respond-to('md') {
    padding-left: 24px;
    padding-right: 24px;
  }

  @include respond-to('lg') {
    padding-left: 32px;
    padding-right: 32px;
  }
}

// 响应式字体
.text-responsive {
  font-size: 14px;

  @include respond-to('md') {
    font-size: 16px;
  }
}

// 移动端侧边栏
.layout-container {
  @include respond-below('md') {
    .sidebar {
      position: fixed;
      left: -240px;
      z-index: 1000;
      transition: left 0.3s;

      &.is-open {
        left: 0;
      }
    }

    .main-content {
      margin-left: 0 !important;
    }

    .sidebar-overlay {
      display: block;
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      z-index: 999;
    }
  }
}

// 表格响应式
.responsive-table {
  @include respond-below('md') {
    .el-table__header-wrapper {
      display: none;
    }

    .el-table__body-wrapper {
      .el-table__row {
        display: flex;
        flex-wrap: wrap;
        padding: 12px;
        border-bottom: 1px solid #ebeef5;

        .el-table__cell {
          border: none !important;
          padding: 4px 0;

          &:before {
            content: attr(data-label);
            font-weight: bold;
            margin-right: 8px;
          }
        }
      }
    }
  }
}

// 卡片响应式
.responsive-cards {
  display: grid;
  gap: 16px;
  grid-template-columns: 1fr;

  @include respond-to('sm') {
    grid-template-columns: repeat(2, 1fr);
  }

  @include respond-to('lg') {
    grid-template-columns: repeat(4, 1fr);
  }
}
```

**Step 2: Commit**

```bash
git add frontend/src/
git commit -m "feat: add responsive layout styles"
```

---

## Task 3: 权限控制细化

**Files:**
- Create: `frontend/src/directives/permission.ts`
- Update: `frontend/src/main.ts`

**Step 1: 创建权限指令**

```typescript
// frontend/src/directives/permission.ts
import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

export interface PermissionDirectiveBinding {
  roles?: string[]
  permissions?: string[]
}

export const permission: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const userStore = useUserStore()
    const requiredRoles = Array.isArray(binding.value) ? binding.value : [binding.value]

    const hasRole = requiredRoles.some(role => {
      if (role === 'admin') return userStore.isAdmin
      if (role === 'teacher') return userStore.isTeacher
      if (role === 'student') return userStore.isStudent
      return false
    })

    if (!hasRole) {
      el.parentNode?.removeChild(el)
    }
  }
}

// v-role 指令：只有指定角色可见
export const role: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const userStore = useUserStore()
    const allowedRoles = Array.isArray(binding.value) ? binding.value : [binding.value]

    const userRole = userStore.userInfo?.role?.toLowerCase()
    if (!userRole || !allowedRoles.includes(userRole)) {
      el.parentNode?.removeChild(el)
    }
  }
}

// v-not-role 指令：指定角色不可见
export const notRole: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const userStore = useUserStore()
    const excludedRoles = Array.isArray(binding.value) ? binding.value : [binding.value]

    const userRole = userStore.userInfo?.role?.toLowerCase()
    if (userRole && excludedRoles.includes(userRole)) {
      el.parentNode?.removeChild(el)
    }
  }
}
```

**Step 2: 注册指令（main.ts）**

```typescript
// frontend/src/main.ts
import { permission, role, notRole } from '@/directives/permission'

app.directive('permission', permission)
app.directive('role', role)
app.directive('not-role', notRole)
```

**Step 3: 使用示例**

```vue
<!-- 只有管理员可见 -->
<el-button v-role="'admin'">管理员操作</el-button>

<!-- 教员和管理员可见 -->
<el-button v-role="['admin', 'teacher']">教员操作</el-button>

<!-- 学员不可见 -->
<div v-not-role="'student'">非学员内容</div>
```

**Step 4: Commit**

```bash
git add frontend/src/
git commit -m "feat: add permission directives for role-based access control"
```

---

## Task 4: 安全加固

**Files:**
- Update: `backend/src/main/java/com/teaching/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/teaching/config/CorsConfig.java`
- Create: `backend/src/main/java/com/teaching/filter/RateLimitFilter.java`

**Step 1: 更新安全配置**

```java
// backend/src/main/java/com/teaching/config/SecurityConfig.java
// 添加安全响应头配置
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // ... 其他配置 ...
        .headers(headers -> headers
            .contentTypeOptions(Customizer.withDefaults())
            .xssProtection(Customizer.withDefaults())
            .frameOptions(frame -> frame.deny())
            .cacheControl(Customizer.withDefaults())
        );
    return http.build();
}
```

**Step 2: 创建 CORS 配置**

```java
// backend/src/main/java/com/teaching/config/CorsConfig.java
package com.teaching.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));  // 前端地址
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

**Step 3: 创建简单的请求限流过滤器**

```java
// backend/src/main/java/com/teaching/filter/RateLimitFilter.java
package com.teaching.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastResetTime = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);
        long currentTime = System.currentTimeMillis();

        // 每分钟重置计数
        lastResetTime.compute(clientIp, (key, lastTime) -> {
            if (lastTime == null || currentTime - lastTime > 60000) {
                requestCounts.put(clientIp, new AtomicInteger(0));
                return currentTime;
            }
            return lastTime;
        });

        AtomicInteger count = requestCounts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));

        if (count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

**Step 4: Commit**

```bash
git add backend/src/
git commit -m "feat: add security hardening (CORS, rate limiting, security headers)"
```

---

## Task 5: 全局错误处理优化

**Files:**
- Create: `frontend/src/utils/errorHandler.ts`
- Update: `frontend/src/utils/request.ts`

**Step 1: 创建错误处理器**

```typescript
// frontend/src/utils/errorHandler.ts
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

export interface ApiError {
  code: number
  message: string
  data?: any
}

export function handleApiError(error: ApiError) {
  const userStore = useUserStore()

  switch (error.code) {
    case 401:
      // 未认证或 token 过期
      ElMessageBox.alert('登录已过期，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        callback: () => {
          userStore.logout()
          router.push('/login')
        }
      })
      break

    case 403:
      // 无权限
      ElMessage.error('您没有权限进行此操作')
      break

    case 404:
      // 资源不存在
      ElMessage.error('请求的资源不存在')
      break

    case 429:
      // 请求过于频繁
      ElMessage.warning('请求过于频繁，请稍后再试')
      break

    case 500:
      // 服务器错误
      ElMessage.error('服务器错误，请稍后再试')
      break

    default:
      ElMessage.error(error.message || '操作失败')
  }
}

// 全局 Vue 错误处理
export function setupErrorHandler(app: any) {
  app.config.errorHandler = (err: Error, instance: any, info: string) => {
    console.error('Vue Error:', err, info)
    ElMessage.error('页面发生错误，请刷新重试')
  }

  // 未捕获的 Promise 错误
  window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled Promise Rejection:', event.reason)
  })
}
```

**Step 2: 更新请求工具**

```typescript
// frontend/src/utils/request.ts（更新错误处理部分）
import { handleApiError } from './errorHandler'

// 响应拦截器中
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      handleApiError(res)
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    const apiError = error.response?.data || { code: 500, message: '网络错误' }
    handleApiError(apiError)
    return Promise.reject(error)
  }
)
```

**Step 3: Commit**

```bash
git add frontend/src/
git commit -m "feat: add global error handling"
```

---

## 验证清单

1. **UI验证**
   - 仪表板根据角色显示不同内容
   - 移动端布局正常
   - 表格在小屏幕上可用

2. **权限验证**
   - v-role 指令正确控制元素显示
   - 权限不足时显示友好提示

3. **安全验证**
   - CORS 配置正确
   - 请求限流生效
   - 安全响应头正确设置

下一步：继续 `03-summary.md` 完成阶段四总结。
