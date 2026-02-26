<template>
  <div class="dashboard-content">
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
            :onClick="() => router.push('/admin/users')"
          />
          <StatCard
            title="总学科数"
            :value="adminStats.totalSubjects"
            icon="Reading"
            color="primary"
            :loading="statsLoading"
            :onClick="() => router.push('/subjects')"
          />
          <StatCard
            title="总课程数"
            :value="adminStats.totalLessons"
            icon="Document"
            color="warning"
            :loading="statsLoading"
            :onClick="() => router.push('/subjects')"
          />
          <StatCard
            title="总提交数"
            :value="adminStats.totalSubmissions"
            icon="Folder"
            color="success"
            :loading="statsLoading"
            :onClick="() => router.push('/submissions')"
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
            :onClick="() => router.push('/subjects')"
          />
          <StatCard
            title="待评分作业"
            :value="teacherStats.pendingGrades"
            icon="Clock"
            color="warning"
            trend="up"
            :trendValue="pendingTrendText"
            :loading="statsLoading"
            :onClick="() => router.push('/submissions/board')"
          />
          <StatCard
            title="已评分作业"
            :value="teacherStats.completedGrades"
            icon="CircleCheck"
            color="success"
            :loading="statsLoading"
            :onClick="() => router.push('/grades')"
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
            :onClick="() => router.push('/student/calendar')"
          />
          <StatCard
            title="已提交作业"
            :value="studentStats.submittedAssignments"
            icon="Upload"
            color="success"
            :loading="statsLoading"
            :onClick="() => router.push('/submissions')"
          />
          <StatCard
            title="已评分作业"
            :value="studentStats.gradedAssignments"
            icon="Medal"
            color="warning"
            :loading="statsLoading"
            :onClick="() => router.push('/grade-summary')"
          />
          <StatCard
            title="平均成绩"
            :value="gradeText"
            icon="TrendCharts"
            color="info"
            :loading="statsLoading"
            :onClick="() => router.push('/grade-summary')"
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, Document, DataAnalysis, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import StatCard from '@/components/common/StatCard.vue'
import { getAdminStats, getTeacherStats, getStudentStats } from '@/api/dashboard'
import type { AdminStats, TeacherStats, StudentStats } from '@/api/dashboard'

const router = useRouter()
const userStore = useUserStore()

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
const greetingEmoji = computed(() => {
  const role = userStore.user?.role
  switch (role) {
    case 'ADMIN': return '👋'
    case 'TEACHER': return '📚'
    case 'STUDENT': return '🎓'
    default: return '👋'
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
const loadStats = async () => {
  if (!userStore.user) {
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
  loadStats()
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
.dashboard-content {
  color: var(--text-primary);
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
@media (max-width: 768px) {
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

@media (min-width: 1440px) {
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
