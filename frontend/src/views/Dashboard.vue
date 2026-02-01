<template>
  <div class="dashboard-container">
    <div class="sidebar">
      <div class="logo">
        <h2>教学管理系统</h2>
      </div>
      <el-menu
        default-active="1"
        class="sidebar-menu"
        background-color="#111113"
        text-color="#ADADB0"
        active-text-color="#FF5C00"
      >
        <el-menu-item index="1">
          <el-icon><House /></el-icon>
          <span>仪表板</span>
        </el-menu-item>
        <el-menu-item index="2" v-if="userStore.isAdmin">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="main-content">
      <div class="navbar">
        <div class="user-info">
          <span class="welcome">欢迎, {{ userStore.user?.name }}</span>
          <el-tag :type="roleTagType" class="role-tag">{{ roleText }}</el-tag>
          <el-button type="danger" size="small" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </div>

      <div class="content">
        <el-card class="stats-card">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <div class="stats-content">
            <div class="stat-item">
              <div class="stat-label">用户名</div>
              <div class="stat-value">{{ userStore.user?.username }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">姓名</div>
              <div class="stat-value">{{ userStore.user?.name }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">角色</div>
              <div class="stat-value">{{ roleText }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>快速开始</span>
            </div>
          </template>
          <p>欢迎使用教学管理系统！</p>
          <p v-if="userStore.isAdmin">您具有管理员权限，可以管理用户和系统设置。</p>
          <p v-else-if="userStore.isTeacher">您具有教员权限，可以管理课程和评分。</p>
          <p v-else>您是学员，可以查看课程和提交作业。</p>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

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

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.dashboard-container {
  display: flex;
  height: 100vh;
  background: #0A0A0B;
}

.sidebar {
  width: 260px;
  background: #111113;
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 24px;
  text-align: center;
  border-bottom: 1px solid #2A2A2E;
}

.logo h2 {
  color: #FFFFFF;
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF5C00 0%, #FF8A4C 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 60px;
  background: #111113;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
  border-bottom: 1px solid #2A2A2E;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome {
  color: #FFFFFF;
  font-size: 14px;
}

.role-tag {
  font-size: 12px;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  align-content: start;
}

.stats-card,
.info-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid #2A2A2E;
  color: #FFFFFF;
}

:deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid #2A2A2E;
}

.card-header {
  color: #FFFFFF;
  font-weight: 600;
}

.stats-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  color: #6B6B70;
  font-size: 12px;
  margin-bottom: 8px;
}

.stat-value {
  color: #FFFFFF;
  font-size: 18px;
  font-weight: 600;
}

.info-card p {
  color: #ADADB0;
  line-height: 1.6;
  margin: 8px 0;
}
</style>
