<template>
  <div class="layout-container">
    <!-- 移动端遮罩 -->
    <div v-if="sidebarVisible" class="sidebar-overlay" @click="sidebarVisible = false"></div>

    <div class="sidebar" :class="{ 'sidebar-open': sidebarVisible }">
      <div class="logo">
        <h2>教学管理系统</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>仪表板</span>
        </el-menu-item>
        <el-menu-item index="/student/calendar" v-if="userStore.isStudent">
          <el-icon><Calendar /></el-icon>
          <span>课程日历</span>
        </el-menu-item>
        <el-menu-item index="/subjects" v-if="userStore.isTeacher || userStore.isAdmin">
          <el-icon><Reading /></el-icon>
          <span>学科管理</span>
        </el-menu-item>
        <el-menu-item index="/submissions">
          <el-icon><Document /></el-icon>
          <span>作业管理</span>
        </el-menu-item>
        <el-menu-item index="/submissions/board" v-if="userStore.isTeacher || userStore.isAdmin">
          <el-icon><Grid /></el-icon>
          <span>提交看板</span>
        </el-menu-item>
        <el-menu-item index="/grades" v-if="userStore.isTeacher || userStore.isAdmin">
          <el-icon><EditPen /></el-icon>
          <span>评分管理</span>
        </el-menu-item>
        <el-menu-item index="/grade-summary">
          <el-icon><DataAnalysis /></el-icon>
          <span>成绩汇总</span>
        </el-menu-item>
        <el-menu-item index="/admin/users" v-if="userStore.isAdmin">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <span>个人设置</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="main-content">
      <div class="navbar">
        <el-button
          class="menu-toggle"
          link
          @click="sidebarVisible = !sidebarVisible"
        >
          <el-icon size="24"><Expand /></el-icon>
        </el-button>
        <div class="user-info">
          <ThemeSwitcher mode="dropdown" :show-message="false" />
          <NotificationBell />
          <span class="welcome">欢迎, {{ userStore.user?.name }}</span>
          <el-tag :type="roleTagType" class="role-tag">{{ roleText }}</el-tag>
          <el-dropdown trigger="click" @command="handleCommand">
            <el-button type="primary" size="small">
              <el-icon><User /></el-icon>
              <span class="dropdown-text">{{ userStore.user?.name }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
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
      </div>

      <div class="content">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  House, User, Reading, Document, EditPen, DataAnalysis,
  Expand, Setting, ArrowDown, SwitchButton, Grid, Calendar
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/NotificationBell.vue'
import ThemeSwitcher from '@/components/common/ThemeSwitcher.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarVisible = ref(false)
const activeMenu = computed(() => route.path)

const roleText = computed(() => {
  const role = userStore.user?.role
  switch (role) {
    case 'ADMIN':
      return '管理员'
    case 'TEACHER':
      return '教员'
    case 'STUDENT':
      return '学员'
    default:
      return '未知'
  }
})

const roleTagType = computed(() => {
  const role = userStore.user?.role
  switch (role) {
    case 'ADMIN':
      return 'danger'
    case 'TEACHER':
      return 'warning'
    case 'STUDENT':
      return 'success'
    default:
      return 'info'
  }
})

const handleMenuSelect = (index: string) => {
  router.push(index)
  sidebarVisible.value = false
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
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background: var(--bg-primary);
}

.sidebar {
  width: 260px;
  background: var(--bg-sidebar);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.logo {
  padding: 24px;
  text-align: center;
  border-bottom: 1px solid var(--border-default);
  background: var(--sidebar-logo-bg);
}

.logo h2 {
  color: var(--text-on-primary);
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

:deep(.el-menu) {
  background: transparent !important;
  border-right: none !important;
}

:deep(.el-menu-item) {
  color: var(--sidebar-text) !important;
}

:deep(.el-menu-item:hover),
:deep(.el-menu-item:focus) {
  background: rgba(255, 255, 255, 0.15) !important;
  color: var(--sidebar-text-active) !important;
}

:deep(.el-menu-item.is-active) {
  background: var(--sidebar-item-active-bg) !important;
  color: var(--sidebar-text-active) !important;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 60px;
  background: var(--bg-topbar);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
  border-bottom: 1px solid var(--border-default);
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome {
  color: var(--text-primary);
  font-size: 14px;
}

.role-tag {
  font-size: 12px;
}

.dropdown-text {
  margin-left: 4px;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--bg-primary);
}

/* 响应式布局 */
.sidebar-overlay {
  display: none;
}

.menu-toggle {
  display: none;
  color: var(--text-primary);
  margin-right: auto;
}

@media (max-width: 768px) {
  .layout-container {
    position: relative;
  }

  .sidebar {
    position: fixed;
    left: -260px;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transition: left 0.3s ease;
  }

  .sidebar.sidebar-open {
    left: 0;
  }

  .sidebar-overlay {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: var(--bg-overlay);
    z-index: 999;
  }

  .menu-toggle {
    display: block;
  }

  .main-content {
    margin-left: 0;
  }

  .navbar {
    justify-content: space-between;
  }

  .welcome {
    display: none;
  }
}
</style>
