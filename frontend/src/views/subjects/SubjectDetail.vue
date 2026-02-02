<template>
  <MainLayout>
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
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新增课程
          </el-button>
        </div>
      </div>

      <el-card class="dark-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="列表视图" name="list">
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索课程标题或内容"
              clearable
              style="width: 300px"
              @clear="loadLessons"
              @keyup.enter="loadLessons"
            >
              <template #append>
                <el-button :icon="Search" @click="loadLessons" />
              </template>
            </el-input>

            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="loadLessons"
              style="margin-left: 10px"
            />
          </div>

          <el-table :data="lessons" v-loading="loading" stripe>
            <el-table-column prop="title" label="课程标题" min-width="200" />
            <el-table-column prop="lessonTime" label="上课时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.lessonTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="submitType" label="提交类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.submitType === 'PERSONAL' ? 'primary' : 'success'">
                  {{ row.submitType === 'PERSONAL' ? '个人' : '小组' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deadline" label="截止时间" width="180">
              <template #default="{ row }">
                {{ row.deadline ? formatDateTime(row.deadline) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="allowLate" label="允许补交" width="100">
              <template #default="{ row }">
                <el-tag :type="row.allowLate ? 'success' : 'info'">
                  {{ row.allowLate ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="viewLesson(row)">
                  查看
                </el-button>
                <el-button link type="primary" size="small" @click="editLesson(row)">
                  编辑
                </el-button>
                <el-button link type="danger" size="small" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadLessons"
            @current-change="loadLessons"
          />
        </el-tab-pane>

        <el-tab-pane label="日历视图" name="calendar">
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
              @click="day.lessons.length > 0 && showDayLessons(day)"
              @dragover.prevent
              @drop="handleMonthViewDrop($event, day)"
            >
              <div class="day-number">{{ day.day }}</div>
              <div v-if="day.lessons.length > 0" class="day-lessons">
                <div
                  v-for="lesson in day.lessons.slice(0, 3)"
                  :key="lesson.id"
                  class="lesson-item"
                  draggable="true"
                  @click.stop="viewLesson(lesson)"
                  @dragstart="handleDragStart($event, lesson)"
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
                @dragover.prevent
                @drop="handleWeekViewDrop($event, day.dateStr, hour)"
              >
                <!-- 该时间段的课程 -->
                <div
                  v-for="lesson in getLessonsAtTime(day.date, hour)"
                  :key="lesson.id"
                  class="week-lesson-item"
                  draggable="true"
                  @dragstart="handleDragStart($event, lesson)"
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
      </el-tabs>
    </el-card>

      <!-- 课程表单对话框 -->
      <LessonForm
        v-model="dialogVisible"
        :lesson="currentLesson"
        :subject-id="subjectId"
        @success="handleFormSuccess"
      />
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, User, ArrowLeft } from '@element-plus/icons-vue'
import { listLessons, deleteLesson, updateLessonTime, type LessonDTO } from '@/api/lesson'
import { getSubjectById, type SubjectDTO } from '@/api/subject'
import LessonForm from './LessonForm.vue'
import MainLayout from '@/components/MainLayout.vue'

const route = useRoute()
const router = useRouter()
const subjectId = Number(route.params.id)

const activeTab = ref('list')
const searchKeyword = ref('')
const dateRange = ref<[string, string] | null>(null)
const loading = ref(false)
const lessons = ref<LessonDTO[]>([])
const subject = ref<SubjectDTO | null>(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const dialogVisible = ref(false)
const currentLesson = ref<LessonDTO | null>(null)

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
    const res = await listLessons(
      pagination.page,
      pagination.size,
      subjectId,
      searchKeyword.value || undefined,
      dateRange.value?.[0] ? `${dateRange.value[0]}T00:00:00` : undefined,
      dateRange.value?.[1] ? `${dateRange.value[1]}T23:59:59` : undefined
    )

    if (res.success) {
      lessons.value = res.data.list
      pagination.total = res.data.total
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
  dialogVisible.value = true
}

const editLesson = (lesson: LessonDTO) => {
  currentLesson.value = lesson
  dialogVisible.value = true
}

const viewLesson = (lesson: LessonDTO) => {
  // TODO: 跳转到课程详情页
  console.log('查看课程:', lesson)
}

const handleDelete = async (lesson: LessonDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程"${lesson.title}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteLesson(lesson.id)
    if (res.success) {
      ElMessage.success('删除成功')
      loadLessons()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
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

const showDayLessons = (day: typeof calendarDays.value[0]) => {
  console.log('该天课程:', day.lessons)
  // TODO: 显示该天的课程列表
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

onMounted(() => {
  loadSubject()
  loadLessons()
})
</script>

<style scoped lang="scss">
.subject-detail {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .back-btn {
        color: #ADADB0;

        &:hover {
          color: #FF5C00;
        }
      }

      h2 {
        margin: 0;
        color: #FFFFFF;
        font-size: 24px;
        font-weight: 600;
      }
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .dark-card {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid #2A2A2E;

    :deep(.el-card__body) {
      padding: 24px;
    }

    :deep(.el-tabs__nav-wrap::after) {
      background-color: #2A2A2E;
    }

    :deep(.el-tabs__item) {
      color: #ADADB0;
    }

    :deep(.el-tabs__item.is-active) {
      color: #FF5C00;
    }

    :deep(.el-tabs__active-bar) {
      background-color: #FF5C00;
    }
  }

  .search-bar {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
  }

  :deep(.el-table) {
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: rgba(255, 255, 255, 0.05);
    --el-table-row-hover-bg-color: rgba(255, 92, 0, 0.1);
    --el-table-text-color: #ADADB0;
    --el-table-header-text-color: #FFFFFF;
    --el-table-border-color: #2A2A2E;
  }

  .el-pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .calendar-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .current-month {
    font-size: 18px;
    font-weight: bold;
    color: #FFFFFF;
  }

  .calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 1px;
    background: #2A2A2E;
    border: 1px solid #2A2A2E;
    border-radius: 8px;
    overflow: hidden;
  }

  .calendar-header {
    background: rgba(255, 255, 255, 0.05);
    padding: 12px;
    text-align: center;
    font-weight: bold;
    color: #FFFFFF;
  }

  .calendar-day {
    background: rgba(255, 255, 255, 0.02);
    min-height: 100px;
    padding: 8px;
    cursor: pointer;
    transition: background-color 0.2s;
  }

  .calendar-day:hover {
    background: rgba(255, 92, 0, 0.1);
  }

  .calendar-day.other-month {
    background: rgba(0, 0, 0, 0.2);
    color: #6B6B70;
  }

  .calendar-day.today {
    background: rgba(255, 92, 0, 0.15);
  }

  .day-number {
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 5px;
    color: #FFFFFF;
  }

  .calendar-day.other-month .day-number {
    color: #6B6B70;
  }

  .day-lessons {
    font-size: 12px;
  }

  .lesson-item {
    background: rgba(255, 92, 0, 0.2);
    color: #FF8A4C;
    padding: 2px 6px;
    margin: 2px 0;
    border-radius: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: pointer;
    border-left: 2px solid #FF5C00;
  }

  .lesson-item:hover {
    background: rgba(255, 92, 0, 0.3);
  }

  .more-lessons {
    color: #6B6B70;
    font-size: 11px;
    margin-top: 3px;
  }

  // 周视图样式
  .week-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .current-week {
    font-size: 18px;
    font-weight: bold;
    color: #FFFFFF;
  }

  .week-grid {
    display: grid;
    grid-template-columns: 60px repeat(7, 1fr);
    gap: 1px;
    background: #2A2A2E;
    border: 1px solid #2A2A2E;
    border-radius: 8px;
    overflow: hidden;
    max-height: calc(100vh - 300px);
    overflow-y: auto;
  }

  .week-time-header {
    background: rgba(255, 255, 255, 0.05);
    padding: 12px 8px;
  }

  .week-day-header {
    background: rgba(255, 255, 255, 0.05);
    padding: 12px;
    text-align: center;

    &.is-today {
      background: rgba(255, 92, 0, 0.2);
    }

    .week-day-name {
      font-weight: bold;
      color: #FFFFFF;
      margin-bottom: 4px;
    }

    .week-day-date {
      font-size: 12px;
      color: #ADADB0;
    }
  }

  .week-time-label {
    background: rgba(255, 255, 255, 0.02);
    padding: 8px;
    text-align: right;
    font-size: 12px;
    color: #6B6B70;
    border-top: 1px solid #2A2A2E;
  }

  .week-time-slot {
    background: rgba(255, 255, 255, 0.02);
    min-height: 60px;
    padding: 4px;
    border-top: 1px solid #2A2A2E;
    position: relative;

    &.is-today {
      background: rgba(255, 92, 0, 0.05);
    }

    &:hover {
      background: rgba(255, 92, 0, 0.1);
    }
  }

  .week-lesson-item {
    background: rgba(255, 92, 0, 0.2);
    border-left: 3px solid #FF5C00;
    border-radius: 4px;
    padding: 4px 8px;
    margin-bottom: 2px;
    cursor: grab;
    transition: all 0.2s;

    &:hover {
      background: rgba(255, 92, 0, 0.3);
      transform: scale(1.02);
    }

    &.dragging {
      opacity: 0.5;
      cursor: grabbing;
    }

    .lesson-time {
      font-size: 11px;
      color: #ADADB0;
    }

    .lesson-title {
      font-size: 12px;
      color: #FFFFFF;
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
      background: rgba(255, 92, 0, 0.1);
    }
  }
}
</style>
