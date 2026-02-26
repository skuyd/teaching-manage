<template>
  <div class="lesson-calendar">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="月视图" name="month">
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
            @drop="canDrag && handleMonthViewDrop($event, day)"
          >
            <div class="day-number">{{ day.day }}</div>
            <div v-if="day.lessons.length > 0" class="day-lessons">
              <div
                v-for="lesson in day.lessons.slice(0, 3)"
                :key="lesson.id"
                :class="['lesson-item', { 'subject-colored': showSubjectColors }]"
                :style="showSubjectColors ? { borderLeftColor: getSubjectColor(lesson.subjectId) } : {}"
                :draggable="canDrag"
                @click.stop="handleLessonClick(lesson)"
                @dragstart="canDrag && handleDragStart($event, lesson)"
                @dragend="handleDragEnd"
              >
                <span v-if="showSubjectName" class="lesson-subject">{{ lesson.subjectName }}</span>
                <span class="lesson-time">{{ formatTime(lesson.lessonTime) }}</span>
                <span class="lesson-title">{{ lesson.title }}</span>
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

          <template v-for="hour in timeSlots" :key="hour">
            <div class="week-time-label">{{ hour }}:00</div>
            <div
              v-for="(day, dayIndex) in weekViewDays"
              :key="`${hour}-${dayIndex}`"
              class="week-time-slot"
              :class="{ 'is-today': day.isToday }"
              @click="handleTimeSlotClick(day.dateStr, hour)"
              @dragover.prevent
              @drop="canDrag && handleWeekViewDrop($event, day.dateStr, hour)"
            >
              <div
                v-for="lesson in getLessonsAtTime(day.date, hour)"
                :key="lesson.id"
                :class="['week-lesson-item', { 'subject-colored': showSubjectColors }]"
                :style="showSubjectColors ? { borderLeftColor: getSubjectColor(lesson.subjectId) } : {}"
                :draggable="canDrag"
                @dragstart="canDrag && handleDragStart($event, lesson)"
                @dragend="handleDragEnd"
                @click.stop="handleLessonClick(lesson)"
              >
                <div v-if="showSubjectName" class="lesson-subject">{{ lesson.subjectName }}</div>
                <div class="lesson-time">{{ formatTime(lesson.lessonTime) }}</div>
                <div class="lesson-title">{{ lesson.title }}</div>
              </div>
            </div>
          </template>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { LessonDTO } from '@/api/lesson'

interface Props {
  lessons: LessonDTO[]
  canDrag?: boolean
  canCreate?: boolean
  showSubjectName?: boolean
  showSubjectColors?: boolean
}

interface CalendarDay {
  day: number
  date: Date
  isOtherMonth: boolean
  isToday: boolean
  lessons: LessonDTO[]
}

const props = withDefaults(defineProps<Props>(), {
  canDrag: false,
  canCreate: false,
  showSubjectName: false,
  showSubjectColors: false
})

const emit = defineEmits<{
  (e: 'lesson-click', lesson: LessonDTO): void
  (e: 'day-click', date: Date): void
  (e: 'time-slot-click', date: Date, hour: number): void
  (e: 'lesson-drop', lesson: LessonDTO, newDate: Date): void
}>()

const activeTab = ref('month')
const currentDate = ref(new Date())
const currentWeekStart = ref(getWeekStart(new Date()))
const draggedLesson = ref<LessonDTO | null>(null)

const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const timeSlots = Array.from({ length: 15 }, (_, i) => i + 8)

const subjectColors = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
  '#00CED1', '#FF6B6B', '#4ECDC4', '#9B59B6', '#3498DB'
]

function getSubjectColor(subjectId?: number): string {
  if (!subjectId) return subjectColors[0]
  return subjectColors[subjectId % subjectColors.length]
}

function getWeekStart(date: Date): Date {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  d.setDate(diff)
  d.setHours(0, 0, 0, 0)
  return d
}

const currentMonthLabel = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1
  return `${year}年${month}月`
})

const currentWeekLabel = computed(() => {
  const start = currentWeekStart.value
  const end = new Date(start)
  end.setDate(end.getDate() + 6)
  return `${start.getMonth() + 1}月${start.getDate()}日 - ${end.getMonth() + 1}月${end.getDate()}日`
})

const calendarDays = computed((): CalendarDay[] => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)

  const days: CalendarDay[] = []

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

  const today = new Date()
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const date = new Date(year, month, i)
    const isToday =
      date.getFullYear() === today.getFullYear() &&
      date.getMonth() === today.getMonth() &&
      date.getDate() === today.getDate()

    const dayLessons = props.lessons.filter(lesson => {
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

function formatTime(dateTime: string): string {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getLessonsAtTime(date: Date, hour: number): LessonDTO[] {
  return props.lessons.filter(lesson => {
    const lessonDate = new Date(lesson.lessonTime)
    return (
      lessonDate.getFullYear() === date.getFullYear() &&
      lessonDate.getMonth() === date.getMonth() &&
      lessonDate.getDate() === date.getDate() &&
      lessonDate.getHours() === hour
    )
  })
}

const changeMonth = (delta: number) => {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() + delta)
  currentDate.value = newDate
}

const goToday = () => {
  currentDate.value = new Date()
}

const changeWeek = (delta: number) => {
  const newStart = new Date(currentWeekStart.value)
  newStart.setDate(newStart.getDate() + delta * 7)
  currentWeekStart.value = newStart
}

const goThisWeek = () => {
  currentWeekStart.value = getWeekStart(new Date())
}

const handleLessonClick = (lesson: LessonDTO) => {
  emit('lesson-click', lesson)
}

const handleDayClick = (day: CalendarDay) => {
  if (day.isOtherMonth) return
  if (!props.canCreate) return
  const date = new Date(day.date)
  date.setHours(9, 0, 0, 0)
  emit('day-click', date)
}

const handleTimeSlotClick = (dateStr: string, hour: number) => {
  if (!props.canCreate) return
  const date = new Date(dateStr)
  date.setHours(hour, 0, 0, 0)
  emit('time-slot-click', date, hour)
}

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

function handleWeekViewDrop(event: DragEvent, dateStr: string, hour: number) {
  event.preventDefault()
  if (!draggedLesson.value) return

  const lesson = draggedLesson.value
  const originalDate = new Date(lesson.lessonTime)
  const newDate = new Date(dateStr)
  newDate.setHours(hour, originalDate.getMinutes(), 0, 0)

  if (newDate.getTime() !== originalDate.getTime()) {
    emit('lesson-drop', lesson, newDate)
  }

  draggedLesson.value = null
}

function handleMonthViewDrop(event: DragEvent, day: CalendarDay) {
  event.preventDefault()
  if (!draggedLesson.value || day.isOtherMonth) return

  const lesson = draggedLesson.value
  const originalDate = new Date(lesson.lessonTime)
  const newDate = new Date(day.date)
  newDate.setHours(originalDate.getHours(), originalDate.getMinutes(), 0, 0)

  if (newDate.getTime() !== originalDate.getTime()) {
    emit('lesson-drop', lesson, newDate)
  }

  draggedLesson.value = null
}
</script>

<style scoped lang="scss">
.lesson-calendar {
  .calendar-toolbar,
  .week-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-md);
  }

  .current-month,
  .current-week {
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

    &:hover {
      background: color-mix(in srgb, var(--color-primary) 10%, transparent);
    }

    &.other-month {
      background: var(--bg-secondary);
      color: var(--text-muted);

      .day-number {
        color: var(--text-muted);
      }
    }

    &.today {
      background: color-mix(in srgb, var(--color-primary) 15%, transparent);
    }
  }

  .day-number {
    font-size: var(--text-sm);
    font-weight: var(--font-weight-bold);
    margin-bottom: var(--spacing-xs);
    color: var(--text-primary);
  }

  .day-lessons {
    font-size: var(--text-xs);
  }

  .lesson-item {
    background: color-mix(in srgb, var(--color-primary) 20%, transparent);
    color: var(--text-primary);
    padding: 2px var(--spacing-xs);
    margin: 2px 0;
    border-radius: var(--radius-sm);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: pointer;
    border-left: 2px solid var(--color-primary);
    display: flex;
    align-items: center;
    gap: 4px;

    &:hover {
      background: color-mix(in srgb, var(--color-primary) 30%, transparent);
    }

    &.dragging {
      opacity: 0.5;
      cursor: grabbing;
    }

    .lesson-subject {
      font-size: 10px;
      color: var(--text-secondary);
      max-width: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .lesson-time {
      font-size: 11px;
      color: var(--text-secondary);
    }

    .lesson-title {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .more-lessons {
    color: var(--text-muted);
    font-size: 11px;
    margin-top: 3px;
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
    padding: var(--spacing-sm);
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
    cursor: pointer;
    transition: all var(--transition-fast);

    &:hover {
      background: color-mix(in srgb, var(--color-primary) 30%, transparent);
      transform: scale(1.02);
    }

    &.dragging {
      opacity: 0.5;
      cursor: grabbing;
    }

    .lesson-subject {
      font-size: 10px;
      color: var(--text-secondary);
      margin-bottom: 2px;
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
}
</style>
