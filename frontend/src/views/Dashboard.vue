<template>
  <div class="dashboard-container">
    <!-- 移动端遮罩 -->
    <div v-if="sidebarVisible" class="sidebar-overlay" @click="sidebarVisible = false"></div>

    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'sidebar-open': sidebarVisible, 'sidebar-collapsed': sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">📚</span>
          <span v-if="!sidebarCollapsed" class="logo-text">教学管理系统</span>
        </div>
        <button v-if="!isMobile" class="collapse-btn" @click="toggleSidebar">
          <el-icon :size="18">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </button>
      </div>

      <nav class="sidebar-nav">
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :collapse-transition="false"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <template #title>仪表板</template>
          </el-menu-item>
          <el-menu-item index="/subjects" v-if="userStore.isTeacher || userStore.isAdmin">
            <el-icon><Reading /></el-icon>
            <template #title>学科管理</template>
          </el-menu-item>
          <el-menu-item index="/submissions">
            <el-icon><Document /></el-icon>
            <template #title>作业管理</template>
          </el-menu-item>
          <el-menu-item index="/grades" v-if="userStore.isTeacher || userStore.isAdmin">
            <el-icon><EditPen /></el-icon>
            <template #title>评分管理</template>
          </el-menu-item>
          <el-menu-item index="/grade-summary">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>成绩汇总</template>
          </el-menu-item>
          <el-menu-item index="/admin/users" v-if="userStore.isAdmin">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><Setting /></el-icon>
            <template #title>个人设置</template>
          </el-menu-item>
        </el-menu>
      </nav>

      <div v-if="!sidebarCollapsed" class="sidebar-footer">
        <div class="user-mini-card">
          <el-avatar :size="32" class="user-avatar">
            {{ userStore.user?.name?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="user-mini-info">
            <span class="user-mini-name">{{ userStore.user?.name }}</span>
            <el-tag :type="roleTagType" size="small">{{ roleText }}</el-tag>
          </div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-wrapper">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <button class="menu-toggle" @click="sidebarVisible = !sidebarVisible">
            <el-icon :size="20"><Expand /></el-icon>
          </button>
          <div class="search-box">
            <el-icon class="search-icon"><Search /></el-icon>
            <input type="text" placeholder="搜索..." class="search-input" />
          </div>
        </div>

        <div class="topbar-right">
          <ThemeSwitcher mode="dropdown" />
          <NotificationBell />
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-dropdown-trigger">
              <el-avatar :size="36" class="user-avatar">
                {{ userStore.user?.name?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.user?.name }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><Setting /></el-icon>
                  个人设置
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 主内容 -->
      <main class="main-content">
        <!-- 欢迎卡片 -->
        <section class="welcome-section">
          <div class="welcome-card">
            <div class="welcome-content">
              <h1 class="welcome-title">
                {{ greetingEmoji }} 欢迎回来，{{ userStore.user?.name }}
              </h1>
              <p class="welcome-subtitle">{{ roleDescription }}</p>
            </div>
            <div class="welcome-decoration"></div>
          </div>
        </section>

        <!-- 统计卡片区 -->
        <section class="stats-section">
          <h2 class="section-title">数据概览</h2>
          <div class="stats-grid">
            <!-- 管理员视图 -->
            <template v-if="userStore.isAdmin">
              <StatCard
                title="总用户数"
                :value="adminStats.totalUsers"
                icon="User"
                color="danger"
                :loading="statsLoading"
              />
              <StatCard
                title="总学科数"
                :value="adminStats.totalSubjects"
                icon="Reading"
                color="primary"
                :loading="statsLoading"
              />
              <StatCard
                title="总课程数"
                :value="adminStats.totalLessons"
                icon="Document"
                color="warning"
                :loading="statsLoading"
              />
              <StatCard
                title="总提交数"
                :value="adminStats.totalSubmissions"
                icon="Folder"
                color="success"
                :loading="statsLoading"
              />
            </template>

            <!-- 教师视图 -->
            <template v-else-if="userStore.isTeacher">
              <StatCard
                title="我的学科数"
                :value="teacherStats.mySubjects"
                icon="Reading"
                color="primary"
                :loading="statsLoading"
              />
              <StatCard
                title="待评分作业"
                :value="teacherStats.pendingGrades"
                icon="Clock"
                color="warning"
                trend="up"
                :trendValue="pendingTrendText"
                :loading="statsLoading"
              />
              <StatCard
                title="已评分作业"
                :value="teacherStats.completedGrades"
                icon="CircleCheck"
                color="success"
                :loading="statsLoading"
              />
            </template>

            <!-- 学员视图 -->
            <template v-else>
              <StatCard
                title="我的学科数"
                :value="studentStats.mySubjects"
                icon="Reading"
                color="primary"
                :loading="statsLoading"
              />
              <StatCard
                title="已提交作业"
                :value="studentStats.submittedAssignments"
                icon="Upload"
                color="success"
                :loading="statsLoading"
              />
              <StatCard
                title="已评分作业"
                :value="studentStats.gradedAssignments"
                icon="Medal"
                color="warning"
                :loading="statsLoading"
              />
              <StatCard
                title="平均成绩"
                :value="gradeText"
                icon="TrendCharts"
                color="info"
                :loading="statsLoading"
              />
            </template>
          </div>
        </section>

        <!-- 快捷操作区 -->
        <section class="quick-actions-section">
          <h2 class="section-title">快捷操作</h2>
          <div class="quick-actions">
            <router-link v-if="userStore.isTeacher || userStore.isAdmin" to="/subjects" class="quick-action-card">
              <el-icon class="action-icon"><Reading /></el-icon>
              <span class="action-label">管理学科</span>
            </router-link>
            <router-link to="/submissions" class="quick-action-card">
              <el-icon class="action-icon"><Document /></el-icon>
              <span class="action-label">查看作业</span>
            </router-link>
            <router-link to="/grade-summary" class="quick-action-card">
              <el-icon class="action-icon"><DataAnalysis /></el-icon>
              <span class="action-label">成绩汇总</span>
            </router-link>
            <router-link to="/profile" class="quick-action-card">
              <el-icon class="action-icon"><Setting /></el-icon>
              <span class="action-label">个人设置</span>
            </router-link>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  House, User, Reading, Document, EditPen, DataAnalysis,
  Expand, Fold, Setting, ArrowDown, SwitchButton, Search
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/NotificationBell.vue'
import ThemeSwitcher from '@/components/common/ThemeSwitcher.vue'
import StatCard from '@/components/common/StatCard.vue'
import { getAdminStats, getTeacherStats, getStudentStats } from '@/api/dashboard'
import type { AdminStats, TeacherStats, StudentStats } from '@/api/dashboard'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏状态
const sidebarVisible = ref(false)
const sidebarCollapsed = ref(false)
const isMobile = ref(false)

// 统计数据
const statsLoading = ref(false)
const adminStats = ref<AdminStats>({
  totalUsers: 0,
  totalSubjects: 0,
  totalLessons: 0,
  totalSubmissions: 0
})

const teacherStats = ref<TeacherStats>({
  mySubjects: 0,
  pendingGrades: 0,
  completedGrades: 0
})

const studentStats = ref<StudentStats>({
  mySubjects: 0,
  submittedAssignments: 0,
  gradedAssignments: 0,
  averageGrade: 0
})

// 计算属性
const activeMenu = computed(() => route.path)

const greetingEmoji = computed(() => {
  const role = userStore.user?.role
  switch (role) {
    case 'ADMIN': return '👋'
    case 'TEACHER': return '📚'
    case 'STUDENT': return '🎓'
    default: return '👋'
  }
})

const roleText = computed(() => {
  const role = userStore.user?.role
  switch (role) {
    case 'ADMIN': return '管理员'
    case 'TEACHER': return '教员'
    case 'STUDENT': return '学员'
    default: return '未知'
  }
})

const roleDescription = computed(() => {
  const role = userStore.user?.role
  switch (role) {
    case 'ADMIN': return '您具有系统管理员权限，可以管理用户和所有系统设置。'
    case 'TEACHER': return '您具有教员权限，可以管理课程、评分和查看学生作业。'
    case 'STUDENT': return '您是学员，可以查看课程、提交作业和查看成绩。'
    default: return ''
  }
})

const roleTagType = computed(() => {
  const role = userStore.user?.role
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'TEACHER': return 'warning'
    case 'STUDENT': return 'success'
    default: return 'info'
  }
})

const gradeText = computed(() => {
  const avg = studentStats.value.averageGrade
  if (avg === 0) return '-'
  if (avg >= 3.5) return 'A'
  if (avg >= 2.5) return 'B'
  if (avg >= 1.5) return 'C'
  return 'D'
})

const pendingTrendText = computed(() => {
  const pending = teacherStats.value.pendingGrades
  if (pending > 0) return `${pending} 份待评`
  return '已完成'
})

// 方法
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleMenuSelect = (index: string) => {
  router.push(index)
  if (isMobile.value) {
    sidebarVisible.value = false
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    sidebarCollapsed.value = false
  }
}

const loadStats = async () => {
  if (!userStore.user) {
    console.warn('User not loaded, skipping stats fetch')
    return
  }

  statsLoading.value = true
  try {
    if (userStore.isAdmin) {
      const res = await getAdminStats()
      if (res.success) {
        adminStats.value = res.data
      }
    } else if (userStore.isTeacher) {
      const res = await getTeacherStats()
      if (res.success) {
        teacherStats.value = res.data
      }
    } else if (userStore.isStudent) {
      const res = await getStudentStats()
      if (res.success) {
        studentStats.value = res.data
      }
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  } finally {
    statsLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  loadStats()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

watch(
  () => userStore.user,
  (newUser, oldUser) => {
    if (newUser && !oldUser) {
      loadStats()
    }
  }
)
</script>

<style scoped>
.dashboard-container {
  display: flex;
  min-height: 100vh;
  background: var(--bg-primary);
  color: var(--text-primary);
}

/* ========================================
   侧边栏样式
   ======================================== */
.sidebar {
  width: var(--sidebar-width);
  background: var(--bg-sidebar);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-normal), transform var(--transition-normal);
  border-right: 1px solid var(--border-default);
  position: relative;
  z-index: var(--z-fixed);
}

.sidebar-collapsed {
  width: var(--sidebar-width-collapsed);
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--border-default);
  min-height: var(--topbar-height);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.logo-icon {
  font-size: var(--text-2xl);
}

.logo-text {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  background: var(--bg-gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-sm) 0;
}

.sidebar-menu {
  border-right: none;
  background: transparent !important;
}

:deep(.el-menu) {
  background: transparent !important;
  border-right: none !important;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: var(--radius-md);
  color: var(--text-secondary) !important;
  transition: all var(--transition-fast);
}

:deep(.el-menu-item:hover),
:deep(.el-menu-item:focus) {
  background: var(--bg-secondary) !important;
  color: var(--text-primary) !important;
}

:deep(.el-menu-item.is-active) {
  background: var(--color-primary) !important;
  color: var(--text-on-primary) !important;
}

:deep(.el-menu--collapse .el-menu-item) {
  padding: 0 !important;
  justify-content: center;
}

.sidebar-footer {
  padding: var(--spacing-md);
  border-top: 1px solid var(--border-default);
}

.user-mini-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
}

.user-mini-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-mini-name {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
}

/* ========================================
   主内容区样式
   ======================================== */
.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶栏 */
.topbar {
  height: var(--topbar-height);
  background: var(--bg-topbar);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  border-bottom: 1px solid var(--border-default);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.menu-toggle {
  display: none;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
}

.menu-toggle:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.search-box {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-input);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-default);
  transition: all var(--transition-fast);
}

.search-box:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--btn-shadow-primary);
}

.search-icon {
  color: var(--text-muted);
}

.search-input {
  width: 200px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-size: var(--text-sm);
  outline: none;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.user-dropdown-trigger:hover {
  background: var(--bg-secondary);
}

.user-avatar {
  background: var(--color-primary);
  color: var(--text-on-primary);
  font-weight: var(--font-weight-semibold);
}

.user-name {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
}

.dropdown-arrow {
  color: var(--text-muted);
  transition: transform var(--transition-fast);
}

/* 主内容 */
.main-content {
  flex: 1;
  padding: var(--spacing-lg);
  overflow-y: auto;
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: var(--spacing-xl);
}

.welcome-card {
  position: relative;
  padding: var(--spacing-xl);
  background: var(--bg-gradient-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.welcome-content {
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-on-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.welcome-subtitle {
  font-size: var(--text-base);
  color: var(--text-on-primary);
  opacity: 0.9;
  margin: 0;
  line-height: var(--line-height-normal);
}

.welcome-decoration {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

/* 统计区域 */
.stats-section {
  margin-bottom: var(--spacing-xl);
}

.section-title {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-md) 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--spacing-md);
}

/* 快捷操作 */
.quick-actions-section {
  margin-bottom: var(--spacing-xl);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--spacing-md);
}

.quick-action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg);
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-normal);
}

.quick-action-card:hover {
  border-color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.action-icon {
  font-size: 32px;
  color: var(--color-primary);
}

.action-label {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
}

/* ========================================
   响应式布局
   ======================================== */
.sidebar-overlay {
  display: none;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    transform: translateX(-100%);
    z-index: var(--z-modal);
  }

  .sidebar.sidebar-open {
    transform: translateX(0);
  }

  .sidebar-overlay {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: var(--bg-overlay);
    z-index: var(--z-modal-backdrop);
  }

  .menu-toggle {
    display: flex;
  }

  .search-box {
    display: none;
  }

  .user-name {
    display: none;
  }

  .main-content {
    padding: var(--spacing-md);
  }

  .welcome-title {
    font-size: var(--text-xl);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .search-input {
    width: 150px;
  }
}

@media (min-width: 1440px) {
  .main-content {
    padding: var(--spacing-xl);
  }

  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
