<template>
  <MainLayout>
  <div class="submission-board dark-page">
    <div class="header dark-header">
      <h2>提交状态看板</h2>
      <div class="filters dark-filters">
        <div class="filter-item">
          <span class="filter-label">学科</span>
          <el-select
            v-model="selectedSubjectId"
            placeholder="请选择学科"
            @change="handleSubjectChange"
            clearable
            size="large"
            class="filter-select"
          >
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </div>
        <div class="filter-item">
          <span class="filter-label">课程</span>
          <el-select
            v-model="selectedLessonId"
            placeholder="请选择课程"
            :disabled="!selectedSubjectId"
            clearable
            size="large"
            class="filter-select"
          >
            <el-option
              v-for="lesson in lessons"
              :key="lesson.id"
              :label="lesson.title"
              :value="lesson.id"
            />
          </el-select>
        </div>
      </div>
    </div>

    <div class="board dark-board" v-loading="loading">
      <!-- 未提交列 -->
      <div class="column not-submitted dark-column">
        <div class="column-header">
          <span class="status-dot warning"></span>
          <span>未提交{{ isGroupAssignment ? '（小组）' : '' }}</span>
          <el-tag size="small">{{ notSubmittedStudents.length }}</el-tag>
        </div>
        <div class="column-content">
          <div
            v-for="item in notSubmittedStudents"
            :key="item.id"
            class="card"
          >
            <div class="card-avatar">
              <el-avatar :size="32">{{ item.name.charAt(0) }}</el-avatar>
            </div>
            <div class="card-info">
              <div class="card-name">{{ item.name }}</div>
              <div v-if="item.isGroup" class="card-type">
                <el-tag size="small" type="info">小组</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-if="notSubmittedStudents.length === 0 && selectedLessonId" :description="isGroupAssignment ? '所有小组都已提交' : '所有学员都已提交'" :image-size="60" />
        </div>
      </div>

      <!-- 已提交列 -->
      <div class="column submitted dark-column">
        <div class="column-header">
          <span class="status-dot info"></span>
          <span>已提交待评分</span>
          <el-tag size="small" type="info">{{ submittedNotGraded.length }}</el-tag>
        </div>
        <div class="column-content">
          <div
            v-for="submission in submittedNotGraded"
            :key="submission.id"
            class="card clickable"
            @click="goToDetail(submission.id)"
          >
            <div class="card-avatar">
              <el-avatar :size="32">{{ submission.submitterName?.charAt(0) || '?' }}</el-avatar>
            </div>
            <div class="card-info">
              <div class="card-name">{{ submission.submitterName }}</div>
              <div class="card-time">{{ formatTime(submission.submitTime) }}</div>
            </div>
          </div>
          <el-empty v-if="submittedNotGraded.length === 0 && selectedLessonId" description="暂无待评分提交" :image-size="60" />
        </div>
      </div>

      <!-- 已评分列 -->
      <div class="column graded dark-column">
        <div class="column-header">
          <span class="status-dot success"></span>
          <span>已评分</span>
          <el-tag size="small" type="success">{{ gradedSubmissions.length }}</el-tag>
        </div>
        <div class="column-content">
          <div
            v-for="submission in gradedSubmissions"
            :key="submission.id"
            class="card clickable"
            @click="goToDetail(submission.id)"
          >
            <div class="card-avatar">
              <el-avatar :size="32">{{ submission.submitterName?.charAt(0) || '?' }}</el-avatar>
            </div>
            <div class="card-info">
              <div class="card-name">{{ submission.submitterName }}</div>
              <div class="card-grade">
                <el-tag :type="getGradeType(submission.grade)" size="small">
                  {{ submission.grade }}
                </el-tag>
              </div>
            </div>
          </div>
          <el-empty v-if="gradedSubmissions.length === 0 && selectedLessonId" description="暂无已评分提交" :image-size="60" />
        </div>
      </div>
    </div>

    <el-empty v-if="!selectedLessonId" description="请选择学科和课程查看提交状态" :image-size="120" class="empty-hint" />
  </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '@/components/MainLayout.vue'
import { listSubjects, getSubjectStudents, type SubjectDTO, type StudentDTO } from '@/api/subject'
import { getLessonsBySubject, type LessonDTO, SubmitType } from '@/api/lesson'
import { getSubmissionsByLesson, type SubmissionDTO } from '@/api/submission'
import { listGroupsBySubject, type GroupDTO } from '@/api/group'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const subjects = ref<SubjectDTO[]>([])
const lessons = ref<LessonDTO[]>([])
const selectedSubjectId = ref<number | null>(null)
const selectedLessonId = ref<number | null>(null)
const submissions = ref<SubmissionDTO[]>([])
const students = ref<StudentDTO[]>([])
const groups = ref<GroupDTO[]>([])
const currentLesson = ref<LessonDTO | null>(null)

// 计算属性：是否为小组作业
const isGroupAssignment = computed(() => {
  return currentLesson.value?.submitType === SubmitType.GROUP
})

// 计算属性：未提交的学员或小组
const notSubmittedStudents = computed(() => {
  if (!selectedLessonId.value) return []

  if (isGroupAssignment.value) {
    // 小组作业：返回未提交的小组
    const submittedGroupIds = new Set(submissions.value.map(s => s.groupId).filter(id => id !== null && id !== undefined))
    return groups.value.filter(g => !submittedGroupIds.has(g.id)).map(g => ({
      id: g.id,
      name: g.name,
      isGroup: true
    }))
  } else {
    // 个人作业：返回未提交的学员
    const submittedIds = new Set(submissions.value.map(s => s.submitterId))
    return students.value.filter(s => !submittedIds.has(s.id)).map(s => ({
      id: s.id,
      name: s.name,
      isGroup: false
    }))
  }
})

// 计算属性：已提交但未评分
const submittedNotGraded = computed(() => {
  return submissions.value.filter(s => !s.graded)
})

// 计算属性：已评分
const gradedSubmissions = computed(() => {
  return submissions.value.filter(s => s.graded)
})

const handleSubjectChange = async () => {
  selectedLessonId.value = null
  lessons.value = []
  submissions.value = []
  students.value = []
  groups.value = []
  currentLesson.value = null

  if (selectedSubjectId.value) {
    try {
      // 并行加载课程、学员和小组列表
      const [lessonsRes, studentsRes, groupsRes] = await Promise.all([
        getLessonsBySubject(selectedSubjectId.value),
        getSubjectStudents(selectedSubjectId.value),
        listGroupsBySubject(selectedSubjectId.value)
      ])

      if (lessonsRes.success) {
        lessons.value = lessonsRes.data
      }
      if (studentsRes.success) {
        students.value = studentsRes.data
      }
      if (groupsRes.success) {
        groups.value = groupsRes.data
      }
    } catch (error) {
      console.error('Failed to load data:', error)
      ElMessage.error('加载数据失败')
    }
  }
}

watch(selectedLessonId, async (newVal) => {
  if (newVal) {
    // 设置当前课程信息
    currentLesson.value = lessons.value.find(l => l.id === newVal) || null
    await loadSubmissions()
  } else {
    submissions.value = []
    currentLesson.value = null
  }
})

const loadSubmissions = async () => {
  if (!selectedLessonId.value) return

  loading.value = true
  try {
    const res = await getSubmissionsByLesson(selectedLessonId.value)
    if (res.success) {
      submissions.value = res.data
    }
  } catch (error) {
    console.error('Failed to load submissions:', error)
    ElMessage.error('加载提交列表失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm')
}

const getGradeType = (grade?: string) => {
  if (!grade) return 'info'
  const typeMap: Record<string, 'success' | 'primary' | 'warning' | 'danger' | 'info'> = {
    'A': 'success',
    'B': 'primary',
    'C': 'warning',
    'D': 'danger'
  }
  return typeMap[grade] || 'info'
}

const goToDetail = (id: number) => {
  router.push(`/submissions/${id}`)
}

onMounted(async () => {
  try {
    const res = await listSubjects(1, 100)
    if (res.success) {
      subjects.value = res.data.list
    }
  } catch (error) {
    console.error('Failed to load subjects:', error)
    ElMessage.error('加载学科列表失败')
  }
})
</script>

<style scoped lang="scss">
.submission-board {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  .filters {
    display: flex;
    gap: 20px;
  }

  .filter-item {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .filter-label {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-regular);
    white-space: nowrap;
  }

  .filter-select {
    width: 200px;
  }
}

.board {
  display: flex;
  gap: 20px;
  flex: 1;
  overflow-x: auto;
  min-height: 400px;
}

.column {
  flex: 1;
  min-width: 280px;
  max-width: 320px;
  background: var(--el-bg-color-page);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--el-border-color-light);
}

.column-header {
  padding: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
  border-radius: 8px 8px 0 0;

  .status-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;

    &.warning { background-color: var(--el-color-warning); }
    &.info { background-color: var(--el-color-info); }
    &.success { background-color: var(--el-color-success); }
  }
}

.column-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 12px;

  &.clickable {
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
      border-color: var(--el-color-primary-light-5);
    }
  }
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.card-grade {
  margin-top: 4px;
}

.card-type {
  margin-top: 4px;
}

.empty-hint {
  margin-top: 60px;
}
</style>
