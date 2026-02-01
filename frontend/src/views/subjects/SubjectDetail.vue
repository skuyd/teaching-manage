<template>
  <div class="subject-detail">
    <div class="header">
      <h2>{{ subject?.name || '课程管理' }}</h2>
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

    <el-card>
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
            >
              <div class="day-number">{{ day.day }}</div>
              <div v-if="day.lessons.length > 0" class="day-lessons">
                <div
                  v-for="lesson in day.lessons.slice(0, 3)"
                  :key="lesson.id"
                  class="lesson-item"
                  @click.stop="viewLesson(lesson)"
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
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, User } from '@element-plus/icons-vue'
import { listLessons, deleteLesson, type LessonDTO } from '@/api/lesson'
import { getSubjectById, type SubjectDTO } from '@/api/subject'
import LessonForm from './LessonForm.vue'

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

const goToGroups = () => {
  router.push(`/subjects/${subjectId}/groups`)
}

onMounted(() => {
  loadSubject()
  loadLessons()
})
</script>

<style scoped>
.subject-detail {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
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
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #e0e0e0;
  border: 1px solid #e0e0e0;
}

.calendar-header {
  background: #f5f5f5;
  padding: 10px;
  text-align: center;
  font-weight: bold;
}

.calendar-day {
  background: white;
  min-height: 100px;
  padding: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.calendar-day:hover {
  background: #f9f9f9;
}

.calendar-day.other-month {
  background: #fafafa;
  color: #999;
}

.calendar-day.today {
  background: #e3f2fd;
}

.day-number {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
}

.day-lessons {
  font-size: 12px;
}

.lesson-item {
  background: #e3f2fd;
  padding: 2px 5px;
  margin: 2px 0;
  border-radius: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.lesson-item:hover {
  background: #bbdefb;
}

.more-lessons {
  color: #666;
  font-size: 11px;
  margin-top: 3px;
}
</style>
