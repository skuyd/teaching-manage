<template>
  <div class="subject-detail">
      <div class="header">
        <div class="header-left">
          <el-button text @click="goBack" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>{{ subject?.name || '课程管理' }}</h2>
        </div>
        <div class="header-actions">
          <el-button v-if="subject?.isGrouped" @click="goToGroups">
            <el-icon><User /></el-icon>
            小组管理
          </el-button>
          <el-button v-if="isTeacherOrAdmin" type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新增课程
          </el-button>
        </div>
      </div>

      <el-card class="dark-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="月视图" name="calendar">
          <div class="calendar-toolbar">
            <el-button-group>
              <el-button @click="changeMonth(-1)">上一月</el-button>
              <el-button @click="goToday">今天</el-button>
              <el-button @click="changeMonth(1)">下一月</el-button>
            </el-button-group>
            <span class="current-month">{{ currentMonthLabel }}</span>
          </div>

          <div class="calendar-grid">
            <div v-for="day in weekDays" :key="day" class="calendar-header">
              {{ day }}
            </div>
            <div
              v-for="(day, index) in calendarDays"
              :key="index"
              :class="['calendar-day', {
                'other-month': day.isOtherMonth,
                'today': day.isToday
              }]"
              @click="handleDayClick(day)"
              @dragover.prevent
              @drop="handleMonthViewDrop($event, day)"
            >
              <div class="day-number">{{ day.day }}</div>
              <div v-if="day.lessons.length > 0" class="day-lessons">
                <div
                  v-for="lesson in day.lessons.slice(0, 3)"
                  :key="lesson.id"
                  class="lesson-item"
                  :draggable="isTeacherOrAdmin"
                  @click.stop="viewLesson(lesson)"
                  @dragstart="isTeacherOrAdmin && handleDragStart($event, lesson)"
                  @dragend="handleDragEnd"
                >
                  {{ formatTime(lesson.lessonTime) }} {{ lesson.title }}
                </div>
                <div v-if="day.lessons.length > 3" class="more-lessons">
                  +{{ day.lessons.length - 3 }} 更多
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="周视图" name="week">
          <div class="week-toolbar">
            <el-button-group>
              <el-button @click="changeWeek(-1)">上一周</el-button>
              <el-button @click="goThisWeek">本周</el-button>
              <el-button @click="changeWeek(1)">下一周</el-button>
            </el-button-group>
            <span class="current-week">{{ currentWeekLabel }}</span>
          </div>

          <div class="week-grid">
            <!-- 时间轴标题行 -->
            <div class="week-time-header"></div>
            <div
              v-for="(day, index) in weekViewDays"
              :key="index"
              class="week-day-header"
              :class="{ 'is-today': day.isToday }"
            >
              <div class="week-day-name">{{ day.dayName }}</div>
              <div class="week-day-date">{{ day.dateLabel }}</div>
            </div>

            <!-- 时间轴内容 -->
            <template v-for="hour in timeSlots" :key="hour">
              <div class="week-time-label">{{ hour }}:00</div>
              <div
                v-for="(day, dayIndex) in weekViewDays"
                :key="`${hour}-${dayIndex}`"
                class="week-time-slot"
                :class="{ 'is-today': day.isToday }"
                :data-hour="hour"
                :data-date="day.dateStr"
                @click="handleTimeSlotClick(day.dateStr, hour)"
                @dragover.prevent
                @drop="handleWeekViewDrop($event, day.dateStr, hour)"
              >
                <!-- 该时间段的课程 -->
                <div
                  v-for="lesson in getLessonsAtTime(day.date, hour)"
                  :key="lesson.id"
                  class="week-lesson-item"
                  :draggable="isTeacherOrAdmin"
                  @dragstart="isTeacherOrAdmin && handleDragStart($event, lesson)"
                  @dragend="handleDragEnd"
                  @click="viewLesson(lesson)"
                >
                  <div class="lesson-time">{{ formatTime(lesson.lessonTime) }}</div>
                  <div class="lesson-title">{{ lesson.title }}</div>
                </div>
              </div>
            </template>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="isTeacherOrAdmin" label="学生管理" name="students">
          <div class="students-toolbar">
            <el-button type="primary" @click="showAddStudentDialog">
              <el-icon><Plus /></el-icon>
              添加学生
            </el-button>
            <span class="student-count">共 {{ subjectStudents.length }} 名学生</span>
          </div>

          <el-table :data="subjectStudents" v-loading="studentsLoading" class="students-table">
            <el-table-column prop="name" label="姓名" min-width="120" />
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" size="small" text @click="handleRemoveStudent(row)">
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

      <!-- 课程表单对话框 -->
      <LessonForm
        v-model="dialogVisible"
        :lesson="currentLesson"
        :subject-id="subjectId"
        :default-date="defaultLessonDate"
        @success="handleFormSuccess"
      />

      <!-- 添加学生对话框 -->
      <el-dialog
        v-model="addStudentDialogVisible"
        title="添加学生"
        width="500px"
        destroy-on-close
      >
        <el-form>
          <el-form-item label="选择学生">
            <el-select
              v-model="selectedStudentIds"
              multiple
              filterable
              placeholder="搜索并选择学生"
              style="width: 100%"
            >
              <el-option
                v-for="student in availableStudents"
                :key="student.id"
                :label="`${student.name} (${student.username})`"
                :value="student.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addStudentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddStudents" :loading="addingStudents">
            确定添加
          </el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, ArrowLeft } from '@element-plus/icons-vue'
import { getLessonsBySubject, updateLessonTime, type LessonDTO } from '@/api/lesson'
import { getSubjectById, getSubjectStudents, addStudentToSubject, removeStudentFromSubject, type SubjectDTO, type StudentDTO } from '@/api/subject'
import { listStudents } from '@/api/user'
import type { UserDTO } from '@/api/types'
import LessonForm from './LessonForm.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const subjectId = Number(route.params.id)

// 权限控制
const isTeacherOrAdmin = computed(() => userStore.isTeacher || userStore.isAdmin)

const activeTab = ref('calendar')
const loading = ref(false)
const lessons = ref<LessonDTO[]>([])
const subject = ref<SubjectDTO | null>(null)

const dialogVisible = ref(false)
const currentLesson = ref<LessonDTO | null>(null)
const defaultLessonDate = ref<string | null>(null)

// 学生管理相关
const subjectStudents = ref<StudentDTO[]>([])
const allStudents = ref<UserDTO[]>([])
const studentsLoading = ref(false)
const addStudentDialogVisible = ref(false)
const selectedStudentIds = ref<number[]>([])
const addingStudents = ref(false)

// 日历相关
const currentDate = ref(new Date())
const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// 周视图相关
const currentWeekStart = ref(getWeekStart(new Date()))
const timeSlots = Array.from({ length: 15 }, (_, i) => i + 8) // 8:00 - 22:00
const draggedLesson = ref<LessonDTO | null>(null)

function getWeekStart(date: Date): Date {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1) // 调整为周一开始
  d.setDate(diff)
  d.setHours(0, 0, 0, 0)
  return d
}

const currentWeekLabel = computed(() => {
  const start = currentWeekStart.value
  const end = new Date(start)
  end.setDate(end.getDate() + 6)
  return `${start.getMonth() + 1}月${start.getDate()}日 - ${end.getMonth() + 1}月${end.getDate()}日`
})

const weekViewDays = computed(() => {
  const days = []
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  for (let i = 0; i < 7; i++) {
    const date = new Date(currentWeekStart.value)
    date.setDate(date.getDate() + i)

    days.push({
      date,
      dateStr: formatDateStr(date),
      dayName: dayNames[i],
      dateLabel: `${date.getMonth() + 1}/${date.getDate()}`,
      isToday: date.getTime() === today.getTime()
    })
  }

  return days
})

function formatDateStr(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getLessonsAtTime(date: Date, hour: number): LessonDTO[] {
  return lessons.value.filter(lesson => {
    const lessonDate = new Date(lesson.lessonTime)
    return (
      lessonDate.getFullYear() === date.getFullYear() &&
      lessonDate.getMonth() === date.getMonth() &&
      lessonDate.getDate() === date.getDate() &&
      lessonDate.getHours() === hour
    )
  })
}

const changeWeek = (delta: number) => {
  const newStart = new Date(currentWeekStart.value)
  newStart.setDate(newStart.getDate() + delta * 7)
  currentWeekStart.value = newStart
}

const goThisWeek = () => {
  currentWeekStart.value = getWeekStart(new Date())
}

const currentMonthLabel = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1
  return `${year}年${month}月`
})

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)

  const days: Array<{
    day: number
    date: Date
    isOtherMonth: boolean
    isToday: boolean
    lessons: LessonDTO[]
  }> = []

  // 上月补齐
  const firstDayOfWeek = firstDay.getDay()
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const date = new Date(year, month, -i)
    days.push({
      day: date.getDate(),
      date,
      isOtherMonth: true,
      isToday: false,
      lessons: []
    })
  }

  // 本月
  const today = new Date()
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const date = new Date(year, month, i)
    const isToday =
      date.getFullYear() === today.getFullYear() &&
      date.getMonth() === today.getMonth() &&
      date.getDate() === today.getDate()

    const dayLessons = lessons.value.filter(lesson => {
      const lessonDate = new Date(lesson.lessonTime)
      return (
        lessonDate.getFullYear() === year &&
        lessonDate.getMonth() === month &&
        lessonDate.getDate() === i
      )
    })

    days.push({
      day: i,
      date,
      isOtherMonth: false,
      isToday,
      lessons: dayLessons
    })
  }

  // 下月补齐
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    const date = new Date(year, month + 1, i)
    days.push({
      day: i,
      date,
      isOtherMonth: true,
      isToday: false,
      lessons: []
    })
  }

  return days
})

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatTime = (dateTime: string) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadSubject = async () => {
  try {
    const res = await getSubjectById(subjectId)
    if (res.success) {
      subject.value = res.data
    }
  } catch (error) {
    console.error('加载学科信息失败:', error)
  }
}

const loadLessons = async () => {
  loading.value = true
  try {
    // 使用不分页API获取学科下所有课程，确保日历视图能显示完整课程
    const res = await getLessonsBySubject(subjectId)

    if (res.success) {
      lessons.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载课程列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  currentLesson.value = null
  defaultLessonDate.value = null  // 从按钮新增时不预填日期
  dialogVisible.value = true
}

const editLesson = (lesson: LessonDTO) => {
  currentLesson.value = lesson
  defaultLessonDate.value = null  // 编辑时使用课程原有时间
  dialogVisible.value = true
}

const viewLesson = (lesson: LessonDTO) => {
  if (userStore.isTeacher || userStore.isAdmin) {
    // 教员/管理员可以编辑
    editLesson(lesson)
  } else {
    // 学员只能查看课程信息（显示简单提示）
    ElMessage.info(`课程：${lesson.title}\n时间：${formatDateTime(lesson.lessonTime)}`)
  }
}

const handleFormSuccess = () => {
  dialogVisible.value = false
  loadLessons()
}

const changeMonth = (delta: number) => {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() + delta)
  currentDate.value = newDate
}

const goToday = () => {
  currentDate.value = new Date()
}

// 点击月视图日期 - 打开新增课程对话框（仅教员/管理员）
const handleDayClick = (day: typeof calendarDays.value[0]) => {
  if (day.isOtherMonth) return
  if (!userStore.isTeacher && !userStore.isAdmin) return // 学员不能创建课程
  // 如果点击的是课程条目，不打开对话框（由课程条目的 @click.stop 处理）
  const date = new Date(day.date)
  date.setHours(9, 0, 0, 0) // 默认 9:00
  defaultLessonDate.value = formatDateTimeForForm(date)
  currentLesson.value = null
  dialogVisible.value = true
}

// 点击周视图时间槽 - 打开新增课程对话框（仅教员/管理员）
const handleTimeSlotClick = (dateStr: string, hour: number) => {
  if (!userStore.isTeacher && !userStore.isAdmin) return // 学员不能创建课程
  const date = new Date(dateStr)
  date.setHours(hour, 0, 0, 0)
  defaultLessonDate.value = formatDateTimeForForm(date)
  currentLesson.value = null
  dialogVisible.value = true
}

// 格式化日期时间为表单所需格式
const formatDateTimeForForm = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

// 拖拽处理函数
function handleDragStart(event: DragEvent, lesson: LessonDTO) {
  draggedLesson.value = lesson
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/plain', String(lesson.id))
  }
  const target = event.target as HTMLElement
  target.classList.add('dragging')
}

function handleDragEnd(event: DragEvent) {
  draggedLesson.value = null
  const target = event.target as HTMLElement
  target.classList.remove('dragging')
}

async function handleWeekViewDrop(event: DragEvent, dateStr: string, hour: number) {
  event.preventDefault()

  if (!draggedLesson.value) return

  const lesson = draggedLesson.value
  const originalDate = new Date(lesson.lessonTime)
  const newDate = new Date(dateStr)
  newDate.setHours(hour, originalDate.getMinutes(), 0, 0)

  if (newDate.getTime() === originalDate.getTime()) {
    draggedLesson.value = null
    return
  }

  try {
    const newTimeStr = newDate.toISOString().replace('Z', '')
    await updateLessonTime(lesson.id, newTimeStr)
    ElMessage.success('课程时间已调整')
    await loadLessons()
  } catch (error) {
    ElMessage.error('调整时间失败')
    console.error(error)
  } finally {
    draggedLesson.value = null
  }
}

async function handleMonthViewDrop(event: DragEvent, day: typeof calendarDays.value[0]) {
  event.preventDefault()

  if (!draggedLesson.value || day.isOtherMonth) return

  const lesson = draggedLesson.value
  const originalDate = new Date(lesson.lessonTime)
  const newDate = new Date(day.date)
  newDate.setHours(originalDate.getHours(), originalDate.getMinutes(), 0, 0)

  if (newDate.getTime() === originalDate.getTime()) {
    draggedLesson.value = null
    return
  }

  try {
    const newTimeStr = newDate.toISOString().replace('Z', '')
    await updateLessonTime(lesson.id, newTimeStr)
    ElMessage.success('课程时间已调整')
    await loadLessons()
  } catch (error) {
    ElMessage.error('调整时间失败')
    console.error(error)
  } finally {
    draggedLesson.value = null
  }
}

const goToGroups = () => {
  router.push(`/subjects/${subjectId}/groups`)
}

const goBack = () => {
  router.push('/subjects')
}

// 学生管理相关方法
const availableStudents = computed(() => {
  const existingIds = new Set(subjectStudents.value.map(s => s.id))
  return allStudents.value.filter(s => !existingIds.has(s.id))
})

const loadSubjectStudents = async () => {
  studentsLoading.value = true
  try {
    const res = await getSubjectStudents(subjectId)
    if (res.success) {
      subjectStudents.value = res.data
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
  } finally {
    studentsLoading.value = false
  }
}

const loadAllStudents = async () => {
  try {
    const res = await listStudents()
    if (res.success) {
      allStudents.value = res.data
    }
  } catch (error) {
    console.error('加载所有学生失败:', error)
  }
}

const showAddStudentDialog = () => {
  selectedStudentIds.value = []
  addStudentDialogVisible.value = true
}

const handleAddStudents = async () => {
  if (selectedStudentIds.value.length === 0) {
    ElMessage.warning('请选择要添加的学生')
    return
  }

  addingStudents.value = true
  try {
    for (const studentId of selectedStudentIds.value) {
      await addStudentToSubject(subjectId, studentId)
    }
    ElMessage.success(`成功添加 ${selectedStudentIds.value.length} 名学生`)
    addStudentDialogVisible.value = false
    loadSubjectStudents()
  } catch (error) {
    ElMessage.error('添加学生失败')
    console.error(error)
  } finally {
    addingStudents.value = false
  }
}

const handleRemoveStudent = async (student: StudentDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要将学生"${student.name}"从该学科移除吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await removeStudentFromSubject(subjectId, student.id)
    ElMessage.success('移除成功')
    loadSubjectStudents()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
      console.error(error)
    }
  }
}

onMounted(() => {
  loadSubject()
  loadLessons()
  loadSubjectStudents()
  loadAllStudents()
})
</script>

<style scoped lang="scss">
.subject-detail {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-lg);

    .header-left {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);

      .back-btn {
        color: var(--text-secondary);

        &:hover {
          color: var(--color-primary);
        }
      }

      h2 {
        margin: 0;
        color: var(--text-primary);
        font-size: var(--text-2xl);
        font-weight: var(--font-weight-semibold);
      }
    }

    .header-actions {
      display: flex;
      gap: var(--spacing-sm);
    }
  }

  .dark-card {
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

  .search-bar {
    margin-bottom: var(--spacing-md);
    display: flex;
    align-items: center;
  }

  :deep(.el-table) {
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: var(--bg-secondary);
    --el-table-row-hover-bg-color: color-mix(in srgb, var(--color-primary) 10%, transparent);
    --el-table-text-color: var(--text-secondary);
    --el-table-header-text-color: var(--text-primary);
    --el-table-border-color: var(--border-default);
  }

  .el-pagination {
    margin-top: var(--spacing-md);
    display: flex;
    justify-content: flex-end;
  }

  .calendar-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-md);
  }

  .current-month {
    font-size: var(--text-lg);
    font-weight: var(--font-weight-bold);
    color: var(--text-primary);
  }

  .calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 1px;
    background: var(--border-default);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-md);
    overflow: hidden;
  }

  .calendar-header {
    background: var(--bg-secondary);
    padding: var(--spacing-sm);
    text-align: center;
    font-weight: var(--font-weight-bold);
    color: var(--text-primary);
  }

  .calendar-day {
    background: var(--bg-card);
    min-height: 100px;
    padding: var(--spacing-sm);
    cursor: pointer;
    transition: background-color var(--transition-fast);
  }

  .calendar-day:hover {
    background: color-mix(in srgb, var(--color-primary) 10%, transparent);
  }

  .calendar-day.other-month {
    background: var(--bg-secondary);
    color: var(--text-muted);
  }

  .calendar-day.today {
    background: color-mix(in srgb, var(--color-primary) 15%, transparent);
  }

  .day-number {
    font-size: var(--text-sm);
    font-weight: var(--font-weight-bold);
    margin-bottom: var(--spacing-xs);
    color: var(--text-primary);
  }

  .calendar-day.other-month .day-number {
    color: var(--text-muted);
  }

  .day-lessons {
    font-size: var(--text-xs);
  }

  .lesson-item {
    background: color-mix(in srgb, var(--color-primary) 20%, transparent);
    color: var(--color-primary-light);
    padding: 2px var(--spacing-xs);
    margin: 2px 0;
    border-radius: var(--radius-sm);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: pointer;
    border-left: 2px solid var(--color-primary);
  }

  .lesson-item:hover {
    background: color-mix(in srgb, var(--color-primary) 30%, transparent);
  }

  .more-lessons {
    color: var(--text-muted);
    font-size: 11px;
    margin-top: 3px;
  }

  // 周视图样式
  .week-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-md);
  }

  .current-week {
    font-size: var(--text-lg);
    font-weight: var(--font-weight-bold);
    color: var(--text-primary);
  }

  .week-grid {
    display: grid;
    grid-template-columns: 60px repeat(7, 1fr);
    gap: 1px;
    background: var(--border-default);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-md);
    overflow: hidden;
    max-height: calc(100vh - 300px);
    overflow-y: auto;
  }

  .week-time-header {
    background: var(--bg-secondary);
    padding: var(--spacing-sm) var(--spacing-sm);
  }

  .week-day-header {
    background: var(--bg-secondary);
    padding: var(--spacing-sm);
    text-align: center;

    &.is-today {
      background: color-mix(in srgb, var(--color-primary) 20%, transparent);
    }

    .week-day-name {
      font-weight: var(--font-weight-bold);
      color: var(--text-primary);
      margin-bottom: var(--spacing-xs);
    }

    .week-day-date {
      font-size: var(--text-xs);
      color: var(--text-secondary);
    }
  }

  .week-time-label {
    background: var(--bg-card);
    padding: var(--spacing-sm);
    text-align: right;
    font-size: var(--text-xs);
    color: var(--text-muted);
    border-top: 1px solid var(--border-default);
  }

  .week-time-slot {
    background: var(--bg-card);
    min-height: 60px;
    padding: var(--spacing-xs);
    border-top: 1px solid var(--border-default);
    position: relative;

    &.is-today {
      background: color-mix(in srgb, var(--color-primary) 5%, transparent);
    }

    &:hover {
      background: color-mix(in srgb, var(--color-primary) 10%, transparent);
    }
  }

  .week-lesson-item {
    background: color-mix(in srgb, var(--color-primary) 20%, transparent);
    border-left: 3px solid var(--color-primary);
    border-radius: var(--radius-sm);
    padding: var(--spacing-xs) var(--spacing-sm);
    margin-bottom: 2px;
    cursor: grab;
    transition: all var(--transition-fast);

    &:hover {
      background: color-mix(in srgb, var(--color-primary) 30%, transparent);
      transform: scale(1.02);
    }

    &.dragging {
      opacity: 0.5;
      cursor: grabbing;
    }

    .lesson-time {
      font-size: 11px;
      color: var(--text-secondary);
    }

    .lesson-title {
      font-size: var(--text-xs);
      color: var(--text-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  // 月视图拖拽样式增强
  .lesson-item {
    cursor: grab;

    &.dragging {
      opacity: 0.5;
      cursor: grabbing;
    }
  }

  .calendar-day {
    &:hover {
      background: color-mix(in srgb, var(--color-primary) 10%, transparent);
    }
  }

  // 学生管理样式
  .students-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-md);

    .student-count {
      color: var(--text-secondary);
      font-size: var(--text-sm);
    }
  }

  .students-table {
    :deep(.el-table__header) {
      th {
        background: var(--bg-secondary) !important;
      }
    }
  }
}
</style>
