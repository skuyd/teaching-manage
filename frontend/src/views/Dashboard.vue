<template>
  <div class="dashboard-container">
    <!-- 移动端遮罩 -->
    <div v-if="sidebarVisible" class="sidebar-overlay" @click="sidebarVisible = false"></div>

    <div class="sidebar" :class="{ 'sidebar-open': sidebarVisible }">
      <div class="logo">
        <h2>教学管理系统</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#111113"
        text-color="#ADADB0"
        active-text-color="#FF5C00"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>仪表板</span>
        </el-menu-item>
        <el-menu-item index="/subjects" v-if="userStore.isTeacher || userStore.isAdmin">
          <el-icon><Reading /></el-icon>
          <span>学科管理</span>
        </el-menu-item>
        <el-menu-item index="/submissions">
          <el-icon><Document /></el-icon>
          <span>作业管理</span>
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
          type="text"
          @click="sidebarVisible = !sidebarVisible"
        >
          <el-icon size="24"><Expand /></el-icon>
        </el-button>
        <div class="user-info">
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
        <!-- 管理员视图 -->
        <template v-if="userStore.isAdmin">
          <el-card class="welcome-card">
            <h2>👋 欢迎，{{ userStore.user?.name }}</h2>
            <p>您具有系统管理员权限，可以管理用户和所有系统设置。</p>
          </el-card>

          <div class="stats-grid">
            <el-card class="stat-card">
              <div class="stat-icon admin">
                <el-icon size="32"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ adminStats.totalUsers }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-icon subject">
                <el-icon size="32"><Reading /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ adminStats.totalSubjects }}</div>
                <div class="stat-label">总学科数</div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-icon lesson">
                <el-icon size="32"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ adminStats.totalLessons }}</div>
                <div class="stat-label">总课程数</div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-icon submission">
                <el-icon size="32"><Folder /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ adminStats.totalSubmissions }}</div>
                <div class="stat-label">总提交数</div>
              </div>
            </el-card>
          </div>
        </template>

        <!-- 教师视图 -->
        <template v-else-if="userStore.isTeacher">
          <el-card class="welcome-card">
            <h2>📚 欢迎，{{ userStore.user?.name }}</h2>
            <p>您具有教员权限，可以管理课程、评分和查看学生作业。</p>
          </el-card>

          <div class="stats-grid">
            <el-card class="stat-card">
              <div class="stat-icon subject">
                <el-icon size="32"><Reading /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ teacherStats.mySubjects }}</div>
                <div class="stat-label">我的学科数</div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-icon pending">
                <el-icon size="32"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ teacherStats.pendingGrades }}</div>
                <div class="stat-label">待评分作业</div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-icon completed">
                <el-icon size="32"><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ teacherStats.completedGrades }}</div>
                <div class="stat-label">已评分作业</div>
              </div>
            </el-card>
          </div>
        </template>

        <!-- 学员视图 -->
        <template v-else>
          <el-card class="welcome-card">
            <h2>🎓 欢迎，{{ userStore.user?.name }}</h2>
            <p>您是学员，可以查看课程、提交作业和查看成绩。</p>
          </el-card>

          <div class="stats-grid">
            <el-card class="stat-card">
              <div class="stat-icon subject">
                <el-icon size="32"><Reading /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ studentStats.mySubjects }}</div>
                <div class="stat-label">我的学科数</div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-icon submission">
                <el-icon size="32"><Upload /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ studentStats.submittedAssignments }}</div>
                <div class="stat-label">已提交作业</div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-icon grade">
                <el-icon size="32"><Medal /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ studentStats.gradedAssignments }}</div>
                <div class="stat-label">已评分作业</div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-icon average">
                <el-icon size="32"><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ gradeText }}</div>
                <div class="stat-label">平均成绩</div>
              </div>
            </el-card>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  House, User, Reading, Document, EditPen, DataAnalysis, Folder,
  Clock, CircleCheck, Upload, Medal, TrendCharts, Expand, Setting,
  ArrowDown, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/NotificationBell.vue'
import { getAdminStats, getTeacherStats, getStudentStats } from '@/api/dashboard'
import type { AdminStats, TeacherStats, StudentStats } from '@/api/dashboard'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarVisible = ref(false)
const activeMenu = computed(() => route.path)

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

const gradeText = computed(() => {
  const avg = studentStats.value.averageGrade
  if (avg === 0) return '-'
  if (avg >= 3.5) return 'A'
  if (avg >= 2.5) return 'B'
  if (avg >= 1.5) return 'C'
  return 'D'
})

const handleMenuSelect = (index: string) => {
  router.push(index)
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

const loadStats = async () => {
  // 防御性检查：确保用户状态已加载
  if (!userStore.user) {
    console.warn('User not loaded, skipping stats fetch')
    return
  }

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
    } else {
      console.warn('Unknown user role:', userStore.user?.role)
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

onMounted(() => {
  loadStats()
})

// 当用户状态变化时重新加载统计数据
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

.dropdown-text {
  margin-left: 4px;
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.welcome-card {
  background: linear-gradient(135deg, rgba(255, 92, 0, 0.1) 0%, rgba(255, 138, 76, 0.05) 100%);
  border: 1px solid rgba(255, 92, 0, 0.3);
  margin-bottom: 24px;
}

.welcome-card h2 {
  color: #FFFFFF;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-card p {
  color: #ADADB0;
  font-size: 14px;
  margin: 0;
  line-height: 1.6;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid #2A2A2E;
  transition: all 0.3s;
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: #FF5C00;
  transform: translateY(-2px);
}

:deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.admin {
  background: rgba(231, 76, 60, 0.2);
  color: #E74C3C;
}

.stat-icon.subject {
  background: rgba(52, 152, 219, 0.2);
  color: #3498DB;
}

.stat-icon.lesson {
  background: rgba(155, 89, 182, 0.2);
  color: #9B59B6;
}

.stat-icon.submission {
  background: rgba(241, 196, 15, 0.2);
  color: #F1C40F;
}

.stat-icon.pending {
  background: rgba(230, 126, 34, 0.2);
  color: #E67E22;
}

.stat-icon.completed {
  background: rgba(46, 204, 113, 0.2);
  color: #2ECC71;
}

.stat-icon.grade {
  background: rgba(52, 152, 219, 0.2);
  color: #3498DB;
}

.stat-icon.average {
  background: rgba(155, 89, 182, 0.2);
  color: #9B59B6;
}

.stat-info {
  flex: 1;
}

.stat-value {
  color: #FFFFFF;
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  color: #6B6B70;
  font-size: 14px;
}

/* 响应式布局 */
.sidebar-overlay {
  display: none;
}

.menu-toggle {
  display: none;
  color: #FFFFFF;
  margin-right: auto;
}

@media (max-width: 768px) {
  .dashboard-container {
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
    background: rgba(0, 0, 0, 0.5);
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

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stat-card {
    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-icon :deep(.el-icon) {
    font-size: 24px;
  }

  .stat-value {
    font-size: 24px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
