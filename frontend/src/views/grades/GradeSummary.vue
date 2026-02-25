<template>
  <MainLayout>
  <div class="grade-summary dark-page">
    <el-card class="dark-card">
      <template #header>
        <div class="header">
          <h2>成绩汇总</h2>
          <div class="filters">
            <el-select
              v-model="viewType"
              placeholder="选择视图类型"
              style="width: 150px; margin-right: 12px"
              @change="handleViewTypeChange"
            >
              <el-option label="按课程" value="lesson" />
              <el-option label="按学员" value="student" />
            </el-select>

            <el-select
              v-if="viewType === 'lesson'"
              v-model="selectedLessonId"
              placeholder="选择课程"
              style="width: 300px"
              @change="loadLessonSummary"
            >
              <el-option
                v-for="lesson in lessons"
                :key="lesson.id"
                :label="lesson.title"
                :value="lesson.id"
              />
            </el-select>

            <el-select
              v-if="viewType === 'student'"
              v-model="selectedStudentId"
              placeholder="选择学员"
              style="width: 300px"
              @change="loadStudentSummary"
            >
              <el-option
                v-for="student in students"
                :key="student.id"
                :label="student.name"
                :value="student.id"
              />
            </el-select>
          </div>
        </div>
      </template>

      <!-- Statistics Cards -->
      <div v-if="summary" class="statistics dark-statistics">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic
              :value="lessonSummary?.gradedCount || studentSummary?.gradedCount || 0"
              title="已评分数"
            >
              <template #suffix>
                <span class="statistic-suffix">份</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic
              :value="summary.averageGrade || 0"
              :precision="2"
              title="平均成绩"
            />
          </el-col>
          <el-col :span="6">
            <el-statistic
              :value="summary.passRate || 0"
              :precision="2"
              title="及格率"
            >
              <template #suffix>
                <span class="statistic-suffix">%</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-button
              type="primary"
              :icon="Download"
              @click="handleExport"
              :loading="exporting"
            >
              导出Excel
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- Grade Distribution Chart -->
      <div v-if="summary" class="grade-distribution">
        <h3>等级分布</h3>
        <div class="distribution-chart">
          <div
            v-for="(value, grade) in summary.gradeDistribution"
            :key="grade"
            class="grade-bar"
          >
            <div class="grade-label">
              <span class="grade-name">{{ grade }}</span>
              <span class="grade-count">{{ value }}</span>
            </div>
            <div class="bar-container">
              <div
                class="bar-fill"
                :class="`grade-${grade.toLowerCase()}`"
                :style="{ width: getPercentage(value) + '%' }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Lesson Summary Table -->
      <div v-if="viewType === 'lesson' && lessonSummary" class="data-table">
        <h3>学员成绩明细</h3>
        <el-table :data="lessonSummary.studentGrades" border class="dark-table">
          <el-table-column prop="studentName" label="学员姓名" width="150" />
          <el-table-column label="小组" width="150">
            <template #default="{ row }">
              {{ row.groupName || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="评分等级" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.grade" :type="getGradeTagType(row.grade)" size="large">
                {{ row.grade }} - {{ row.gradeDescription }}
              </el-tag>
              <el-tag v-else type="info">未评分</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="评语" show-overflow-tooltip />
          <el-table-column label="提交时间" width="180">
            <template #default="{ row }">
              {{ row.submitTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="评分时间" width="180">
            <template #default="{ row }">
              {{ row.gradeTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="!row.submitted" type="warning">未提交</el-tag>
              <el-tag v-else-if="!row.graded" type="info">待评分</el-tag>
              <el-tag v-else type="success">已评分</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Student Summary Table -->
      <div v-if="viewType === 'student' && studentSummary" class="data-table">
        <h3>课程成绩明细</h3>
        <el-table :data="studentSummary.lessonGrades" border class="dark-table">
          <el-table-column prop="lessonTitle" label="课程名称" width="200" />
          <el-table-column prop="lessonTime" label="课程时间" width="180" />
          <el-table-column label="评分等级" width="150">
            <template #default="{ row }">
              <el-tag :type="getGradeTagType(row.grade)" size="large">
                {{ row.grade }} - {{ row.gradeDescription }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="评语" show-overflow-tooltip />
          <el-table-column prop="graderName" label="评分者" width="120" />
          <el-table-column prop="submitTime" label="提交时间" width="180" />
          <el-table-column prop="gradeTime" label="评分时间" width="180" />
        </el-table>
      </div>
    </el-card>
  </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import MainLayout from '@/components/MainLayout.vue'
import { Download } from '@element-plus/icons-vue'
import {
  getLessonGradeSummary,
  getStudentGradeSummary,
  exportLessonGrades,
  exportStudentGrades,
  type LessonGradeSummaryDTO,
  type StudentGradeSummaryDTO
} from '@/api/gradeSummary'
import { getLessons, type LessonDTO } from '@/api/lesson'
import { useUserStore } from '@/stores/user'

/**
 * Refs
 */
const viewType = ref<'lesson' | 'student'>('lesson')
const selectedLessonId = ref<number>()
const selectedStudentId = ref<number>()
const lessons = ref<LessonDTO[]>([])
const students = ref<any[]>([])
const lessonSummary = ref<LessonGradeSummaryDTO | null>(null)
const studentSummary = ref<StudentGradeSummaryDTO | null>(null)
const exporting = ref(false)

const userStore = useUserStore()

/**
 * Computed
 */
const summary = computed(() => {
  if (viewType.value === 'lesson' && lessonSummary.value) {
    return {
      averageGrade: lessonSummary.value.averageGrade,
      passRate: lessonSummary.value.passRate,
      gradeDistribution: lessonSummary.value.gradeDistribution
    }
  } else if (viewType.value === 'student' && studentSummary.value) {
    return {
      averageGrade: studentSummary.value.averageGrade,
      passRate: studentSummary.value.passRate,
      gradeDistribution: studentSummary.value.gradeDistribution
    }
  }
  return null
})

const totalGrades = computed(() => {
  if (!summary.value) return 0
  const dist = summary.value.gradeDistribution
  return dist.A + dist.B + dist.C + dist.D
})

/**
 * Load lessons on mount
 */
onMounted(async () => {
  try {
    const res = await getLessons()
    if (res.success && res.data) {
      lessons.value = res.data
    }
  } catch (error) {
    console.error('Failed to load lessons:', error)
    ElMessage.error('加载课程列表失败')
  }

  // If student, auto-select current user
  if (userStore.user?.role === 'STUDENT') {
    viewType.value = 'student'
    selectedStudentId.value = userStore.user.id
    loadStudentSummary()
  }
})

/**
 * Handle view type change
 */
const handleViewTypeChange = () => {
  lessonSummary.value = null
  studentSummary.value = null
  selectedLessonId.value = undefined
  selectedStudentId.value = undefined
}

/**
 * Load lesson summary
 */
const loadLessonSummary = async () => {
  if (!selectedLessonId.value) return

  try {
    const res = await getLessonGradeSummary(selectedLessonId.value)
    if (res.success && res.data) {
      lessonSummary.value = res.data
    }
  } catch (error) {
    console.error('Failed to load lesson summary:', error)
    ElMessage.error('加载课程成绩汇总失败')
  }
}

/**
 * Load student summary
 */
const loadStudentSummary = async () => {
  if (!selectedStudentId.value) return

  try {
    const res = await getStudentGradeSummary(selectedStudentId.value)
    if (res.success && res.data) {
      studentSummary.value = res.data
    }
  } catch (error) {
    console.error('Failed to load student summary:', error)
    ElMessage.error('加载学员成绩汇总失败')
  }
}

/**
 * Get percentage for distribution chart
 */
const getPercentage = (value: number): number => {
  if (totalGrades.value === 0) return 0
  return (value / totalGrades.value) * 100
}

/**
 * Get tag type for grade
 */
const getGradeTagType = (grade: string): string => {
  const typeMap: Record<string, string> = {
    'A': 'success',
    'B': 'primary',
    'C': 'warning',
    'D': 'danger'
  }
  return typeMap[grade] || 'info'
}

/**
 * Handle export
 */
const handleExport = async () => {
  exporting.value = true

  try {
    let response
    if (viewType.value === 'lesson' && selectedLessonId.value) {
      response = await exportLessonGrades(selectedLessonId.value)
    } else if (viewType.value === 'student' && selectedStudentId.value) {
      response = await exportStudentGrades(selectedStudentId.value)
    } else {
      ElMessage.warning('请先选择要导出的数据')
      return
    }

    // Create download link
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `grades_export_${Date.now()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('Export failed:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped lang="scss">
.grade-summary {
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

    .filters {
      display: flex;
      gap: var(--spacing-sm);
    }
  }

  .dark-statistics {
    margin: var(--spacing-lg) 0;
    padding: var(--spacing-lg);
    background: var(--bg-secondary);
    border-radius: var(--radius-md);
    border: 1px solid var(--border-default);

    .statistic-suffix {
      font-size: var(--text-sm);
      margin-left: var(--spacing-xs);
    }

    :deep(.el-statistic__head) {
      color: var(--text-muted);
    }

    :deep(.el-statistic__content) {
      color: var(--text-primary);
    }
  }

  .grade-distribution {
    margin: var(--spacing-xl) 0;

    h3 {
      margin-bottom: var(--spacing-lg);
      font-size: var(--text-lg);
      font-weight: var(--font-weight-semibold);
      color: var(--text-primary);
    }

    .distribution-chart {
      .grade-bar {
        margin-bottom: var(--spacing-md);

        .grade-label {
          display: flex;
          justify-content: space-between;
          margin-bottom: var(--spacing-sm);
          font-size: var(--text-sm);

          .grade-name {
            font-weight: var(--font-weight-semibold);
            color: var(--text-primary);
          }

          .grade-count {
            color: var(--text-muted);
          }
        }

        .bar-container {
          width: 100%;
          height: 24px;
          background: var(--bg-tertiary);
          border-radius: var(--radius-sm);
          overflow: hidden;

          .bar-fill {
            height: 100%;
            transition: width var(--transition-normal);
            border-radius: var(--radius-sm);

            &.grade-a {
              background: linear-gradient(90deg, var(--color-success), color-mix(in srgb, var(--color-success) 70%, white));
            }

            &.grade-b {
              background: linear-gradient(90deg, var(--color-info), color-mix(in srgb, var(--color-info) 70%, white));
            }

            &.grade-c {
              background: linear-gradient(90deg, var(--color-warning), color-mix(in srgb, var(--color-warning) 70%, white));
            }

            &.grade-d {
              background: linear-gradient(90deg, var(--color-danger), color-mix(in srgb, var(--color-danger) 70%, white));
            }
          }
        }
      }
    }
  }

  .data-table {
    margin-top: var(--spacing-xl);

    h3 {
      margin-bottom: var(--spacing-md);
      font-size: var(--text-lg);
      font-weight: var(--font-weight-semibold);
      color: var(--text-primary);
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
  .grade-summary {
    padding: var(--spacing-sm);

    .header {
      flex-direction: column;
      gap: var(--spacing-md);
      align-items: flex-start;

      .filters {
        width: 100%;
        flex-direction: column;

        :deep(.el-select) {
          width: 100% !important;
        }
      }
    }

    .dark-statistics {
      :deep(.el-col) {
        margin-bottom: var(--spacing-md);
      }
    }
  }
}
</style>
