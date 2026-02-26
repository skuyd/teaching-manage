<template>
  <MainLayout>
  <div class="grade-management dark-page">
    <el-card class="dark-card">
      <template #header>
        <div class="header">
          <h2>评分管理</h2>
          <el-select
            v-model="selectedLessonId"
            placeholder="选择课程"
            @change="loadGrades"
            style="width: 300px"
          >
            <el-option
              v-for="lesson in lessons"
              :key="lesson.id"
              :label="lesson.title"
              :value="lesson.id"
            />
          </el-select>
        </div>
      </template>

      <!-- Statistics -->
      <el-row v-if="statistics" :gutter="20" class="statistics dark-statistics">
        <el-col :span="6">
          <el-statistic title="总提交数" :value="statistics.total" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="平均分" :value="statistics.averageGrade || 'N/A'" :precision="2" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="及格率" :value="statistics.passRate" suffix="%" :precision="2" />
        </el-col>
        <el-col :span="6">
          <div class="grade-distribution">
            <div class="title">等级分布</div>
            <div class="distribution-items">
              <span>A: {{ statistics.gradeDistribution.A }}</span>
              <span>B: {{ statistics.gradeDistribution.B }}</span>
              <span>C: {{ statistics.gradeDistribution.C }}</span>
              <span>D: {{ statistics.gradeDistribution.D }}</span>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- Grades Table -->
      <el-table :data="grades" v-loading="loading" style="margin-top: 20px" class="dark-table">
        <el-table-column prop="submitterName" label="提交者" width="150" />
        <el-table-column prop="lessonTitle" label="课程" width="200" />
        <el-table-column label="评分等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getGradeType(row.grade)" size="large">
              {{ row.grade }} - {{ row.gradeDescription }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评语" show-overflow-tooltip />
        <el-table-column prop="graderName" label="评分者" width="120" />
        <el-table-column label="评分时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.gradeTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
              v-if="canEdit(row)"
            >
              修改
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              v-if="canDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="editDialogVisible"
      title="修改评分"
      width="600px"
      @close="handleDialogClose"
    >
      <GradeForm
        v-if="currentGrade"
        :existing-grade="currentGrade"
        @submit="handleUpdateSubmit"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>
  </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import MainLayout from '@/components/MainLayout.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getGradesByLesson,
  getLessonStatistics,
  updateGrade,
  deleteGrade,
  type GradeDTO,
  type GradeStatistics,
  type UpdateGradeRequest,
  GradeLevel
} from '@/api/grade'
import { getLessons, type LessonDTO } from '@/api/lesson'
import { useUserStore } from '@/stores/user'
import GradeForm from '@/components/GradeForm.vue'

/**
 * Stores
 */
const userStore = useUserStore()

/**
 * Refs
 */
const loading = ref(false)
const selectedLessonId = ref<number>()
const lessons = ref<LessonDTO[]>([])
const grades = ref<GradeDTO[]>([])
const statistics = ref<GradeStatistics | null>(null)
const editDialogVisible = ref(false)
const currentGrade = ref<{ id: number; grade: GradeLevel; comment: string | null } | null>(null)

/**
 * Computed
 */
const userId = computed(() => userStore.user?.id)
const userRole = computed(() => userStore.user?.role)

/**
 * Load lessons on mount
 */
onMounted(async () => {
  try {
    const res = await getLessons()
    if (res.success && res.data) {
      lessons.value = res.data
      // 默认选择第一个课程并加载评分数据
      if (lessons.value.length > 0) {
        selectedLessonId.value = lessons.value[0].id
        loadGrades()
      }
    }
  } catch (error) {
    ElMessage.error('加载课程列表失败')
  }
})

/**
 * Load grades for selected lesson
 */
const loadGrades = async () => {
  if (!selectedLessonId.value) return

  loading.value = true
  try {
    // Load grades
    const gradesRes = await getGradesByLesson(selectedLessonId.value)
    if (gradesRes.success && gradesRes.data) {
      grades.value = gradesRes.data
    }

    // Load statistics
    const statsRes = await getLessonStatistics(selectedLessonId.value)
    if (statsRes.success && statsRes.data) {
      statistics.value = statsRes.data
    }
  } catch (error) {
    console.error('Failed to load grades:', error)
    ElMessage.error('加载评分数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * Get tag type for grade
 */
const getGradeType = (grade: GradeLevel): string => {
  const typeMap: Record<GradeLevel, string> = {
    [GradeLevel.A]: 'success',
    [GradeLevel.B]: 'primary',
    [GradeLevel.C]: 'warning',
    [GradeLevel.D]: 'danger'
  }
  return typeMap[grade]
}

/**
 * Format date
 */
const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

/**
 * Check if user can edit grade
 */
const canEdit = (grade: GradeDTO): boolean => {
  return grade.graderId === userId.value
}

/**
 * Check if user can delete grade
 */
const canDelete = (grade: GradeDTO): boolean => {
  return grade.graderId === userId.value || userRole.value === 'ADMIN'
}

/**
 * Handle edit grade
 */
const handleEdit = (grade: GradeDTO) => {
  currentGrade.value = {
    id: grade.id,
    grade: grade.grade,
    comment: grade.comment
  }
  editDialogVisible.value = true
}

/**
 * Handle update submit
 */
const handleUpdateSubmit = async (data: UpdateGradeRequest) => {
  if (!currentGrade.value) return

  try {
    const res = await updateGrade(currentGrade.value.id, data)
    if (res.success) {
      ElMessage.success('评分已更新')
      editDialogVisible.value = false
      loadGrades()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('Failed to update grade:', error)
    ElMessage.error('更新评分失败')
  }
}

/**
 * Handle delete grade
 */
const handleDelete = async (grade: GradeDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除对"${grade.submitterName}"的评分吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteGrade(grade.id)
    if (res.success) {
      ElMessage.success('评分已删除')
      loadGrades()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete grade:', error)
      ElMessage.error('删除评分失败')
    }
  }
}

/**
 * Handle dialog close
 */
const handleDialogClose = () => {
  currentGrade.value = null
}
</script>

<style scoped lang="scss">
.grade-management {
  padding: var(--spacing-lg);

  .dark-card {
    background: var(--bg-card);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-lg);

    :deep(.el-card__header) {
      background: var(--bg-secondary);
      border-bottom: 1px solid var(--border-default);
    }

    :deep(.el-card__body) {
      padding: var(--spacing-lg);
    }
  }

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
      margin: 0;
      color: var(--text-primary);
      font-size: var(--text-2xl);
      font-weight: var(--font-weight-semibold);
    }
  }

  .dark-statistics {
    margin-top: var(--spacing-lg);
    padding: var(--spacing-lg);
    background: var(--bg-secondary);
    border-radius: var(--radius-md);
    border: 1px solid var(--border-default);

    :deep(.el-statistic__head) {
      color: var(--text-muted);
    }

    :deep(.el-statistic__content) {
      color: var(--text-primary);
    }

    .grade-distribution {
      .title {
        font-size: var(--text-sm);
        color: var(--text-muted);
        margin-bottom: var(--spacing-sm);
      }

      .distribution-items {
        display: flex;
        flex-direction: column;
        gap: var(--spacing-xs);
        font-size: var(--text-xl);
        font-weight: var(--font-weight-semibold);

        span {
          font-size: var(--text-base);
          color: var(--text-primary);
        }
      }
    }
  }

  .dark-table {
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: var(--bg-secondary);
    --el-table-row-hover-bg-color: color-mix(in srgb, var(--color-primary) 10%, transparent);
    --el-table-text-color: var(--text-secondary);
    --el-table-header-text-color: var(--text-primary);
    --el-table-border-color: var(--border-default);
  }
}

@media (max-width: 768px) {
  .grade-management {
    padding: var(--spacing-sm);

    .header {
      flex-direction: column;
      gap: var(--spacing-md);
      align-items: flex-start;
    }

    .dark-statistics {
      :deep(.el-col) {
        margin-bottom: var(--spacing-md);
      }
    }
  }
}
</style>
