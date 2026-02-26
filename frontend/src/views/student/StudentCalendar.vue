<template>
  <MainLayout>
    <div class="student-calendar">
      <div class="header">
        <h2>课程日历</h2>
        <div class="header-info">
          <span class="subject-count">已报名 {{ enrolledSubjectCount }} 个学科</span>
        </div>
      </div>

      <el-card class="calendar-card">
        <!-- 学科筛选 -->
        <div class="filter-bar">
          <el-select
            v-model="selectedSubjectId"
            placeholder="全部学科"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
          <div class="legend" v-if="subjects.length > 1">
            <span class="legend-title">图例：</span>
            <div
              v-for="(subject, index) in subjects"
              :key="subject.id"
              class="legend-item"
            >
              <span class="legend-color" :style="{ backgroundColor: getSubjectColor(index) }"></span>
              <span class="legend-name">{{ subject.name }}</span>
            </div>
          </div>
        </div>

        <LessonCalendar
          :lessons="filteredLessons"
          :can-drag="false"
          :can-create="false"
          :show-subject-name="subjects.length > 1"
          :show-subject-colors="subjects.length > 1"
          @lesson-click="handleLessonClick"
        />
      </el-card>

      <!-- 课程详情弹窗 -->
      <LessonDetailDialog
        v-model="detailDialogVisible"
        :lesson="selectedLesson"
        :show-submit-button="true"
      />
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyLessons, type LessonDTO } from '@/api/lesson'
import { getMySubjects, type SubjectDTO } from '@/api/subject'
import MainLayout from '@/components/MainLayout.vue'
import LessonCalendar from '@/components/LessonCalendar.vue'
import LessonDetailDialog from '@/components/LessonDetailDialog.vue'

const loading = ref(false)
const lessons = ref<LessonDTO[]>([])
const subjects = ref<SubjectDTO[]>([])
const selectedSubjectId = ref<number | null>(null)
const selectedLesson = ref<LessonDTO | null>(null)
const detailDialogVisible = ref(false)

const subjectColors = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
  '#00CED1', '#FF6B6B', '#4ECDC4', '#9B59B6', '#3498DB'
]

const getSubjectColor = (index: number): string => {
  return subjectColors[index % subjectColors.length]
}

const enrolledSubjectCount = computed(() => subjects.value.length)

const filteredLessons = computed(() => {
  if (!selectedSubjectId.value) {
    return lessons.value
  }
  return lessons.value.filter(lesson => lesson.subjectId === selectedSubjectId.value)
})

const loadSubjects = async () => {
  try {
    const res = await getMySubjects()
    if (res.success) {
      subjects.value = res.data
    }
  } catch (error) {
    console.error('加载学科失败:', error)
  }
}

const loadLessons = async () => {
  loading.value = true
  try {
    const res = await getMyLessons()
    if (res.success) {
      lessons.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载课程失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleLessonClick = (lesson: LessonDTO) => {
  selectedLesson.value = lesson
  detailDialogVisible.value = true
}

onMounted(() => {
  loadSubjects()
  loadLessons()
})
</script>

<style scoped lang="scss">
.student-calendar {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-lg);

    h2 {
      margin: 0;
      color: var(--text-primary);
      font-size: var(--text-2xl);
      font-weight: var(--font-weight-semibold);
    }

    .header-info {
      .subject-count {
        color: var(--text-secondary);
        font-size: var(--text-sm);
      }
    }
  }

  .calendar-card {
    background: var(--bg-card);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-lg);

    :deep(.el-card__body) {
      padding: var(--spacing-lg);
    }

    :deep(.el-tabs__nav-wrap::after) {
      background-color: var(--border-default);
    }

    :deep(.el-tabs__item) {
      color: var(--text-secondary);
    }

    :deep(.el-tabs__item.is-active) {
      color: var(--color-primary);
    }

    :deep(.el-tabs__active-bar) {
      background-color: var(--color-primary);
    }
  }

  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-md);
    flex-wrap: wrap;
    gap: var(--spacing-md);

    .legend {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);
      flex-wrap: wrap;

      .legend-title {
        color: var(--text-secondary);
        font-size: var(--text-sm);
      }

      .legend-item {
        display: flex;
        align-items: center;
        gap: var(--spacing-xs);

        .legend-color {
          width: 12px;
          height: 12px;
          border-radius: 2px;
        }

        .legend-name {
          font-size: var(--text-xs);
          color: var(--text-primary);
        }
      }
    }
  }
}
</style>
