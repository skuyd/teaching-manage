<template>
  <div class="lesson-detail-page" v-loading="loading">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="goBack" text>
          返回日历
        </el-button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/student/calendar' }">课程日历</el-breadcrumb-item>
          <el-breadcrumb-item v-if="lesson">{{ lesson.subjectName }}</el-breadcrumb-item>
          <el-breadcrumb-item v-if="lesson">{{ lesson.title }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Upload" @click="handleGoSubmit">
          去提交作业
        </el-button>
      </div>
    </div>

    <div v-if="lesson" class="page-content">
      <!-- 课程标题 -->
      <div class="lesson-title-section">
        <h1 class="lesson-title">{{ lesson.title }}</h1>
        <el-tag v-if="lesson.subjectName" type="info" size="large">
          {{ lesson.subjectName }}
        </el-tag>
      </div>

      <!-- 基本信息卡片 -->
      <el-card class="info-card" shadow="never">
        <div class="info-grid">
          <div class="info-item">
            <div class="info-icon">
              <el-icon size="20"><Clock /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">上课时间</span>
              <span class="info-value">{{ formatDateTime(lesson.lessonTime) }}</span>
            </div>
          </div>

          <div class="info-item" v-if="lesson.deadline">
            <div class="info-icon deadline">
              <el-icon size="20"><Calendar /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">截止时间</span>
              <span class="info-value" :class="{ 'is-expired': isExpired }">
                {{ formatDateTime(lesson.deadline) }}
                <el-tag v-if="isExpired" type="danger" size="small" style="margin-left: 8px">已截止</el-tag>
                <el-tag v-else-if="isNearDeadline" type="warning" size="small" style="margin-left: 8px">即将截止</el-tag>
              </span>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">
              <el-icon size="20"><Document /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">提交类型</span>
              <el-tag :type="lesson.submitType === 'GROUP' ? 'warning' : 'primary'">
                {{ lesson.submitType === 'GROUP' ? '小组提交' : '个人提交' }}
              </el-tag>
            </div>
          </div>

          <div class="info-item">
            <div class="info-icon">
              <el-icon size="20"><Timer /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">允许补交</span>
              <el-tag :type="lesson.allowLate ? 'success' : 'danger'">
                {{ lesson.allowLate ? '允许' : '不允许' }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 课程内容 -->
      <el-card class="content-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Notebook /></el-icon>
            <span>课程内容</span>
          </div>
        </template>
        <div v-if="lesson.content" class="markdown-content">
          <MarkdownPreview :content="lesson.content" />
        </div>
        <el-empty v-else description="本课程暂无内容介绍" :image-size="80" />
      </el-card>

      <!-- 作业要求 -->
      <el-card class="content-card homework-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><EditPen /></el-icon>
            <span>作业要求</span>
          </div>
        </template>
        <div v-if="lesson.homeworkDesc" class="markdown-content">
          <MarkdownPreview :content="lesson.homeworkDesc" />
        </div>
        <el-empty v-else description="本课程暂无作业要求" :image-size="80" />
      </el-card>
    </div>

    <!-- 404 状态 -->
    <div v-else-if="!loading" class="not-found">
      <el-empty description="课程不存在或已被删除">
        <el-button type="primary" @click="goBack">返回日历</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, Calendar, Document, Timer, Notebook, EditPen, Upload } from '@element-plus/icons-vue'
import { getLessonById, type LessonDTO } from '@/api/lesson'
import { getSubjectById } from '@/api/subject'
import { MarkdownPreview } from '@/components/common'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const lesson = ref<LessonDTO | null>(null)

const isExpired = computed(() => {
  if (!lesson.value?.deadline) return false
  return new Date(lesson.value.deadline) < new Date()
})

const isNearDeadline = computed(() => {
  if (!lesson.value?.deadline || isExpired.value) return false
  const deadline = new Date(lesson.value.deadline)
  const now = new Date()
  const diffHours = (deadline.getTime() - now.getTime()) / (1000 * 60 * 60)
  return diffHours <= 24
})

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    weekday: 'short'
  })
}

const loadLesson = async () => {
  const lessonId = Number(route.params.id)
  if (!lessonId || isNaN(lessonId)) {
    ElMessage.error('无效的课程ID')
    return
  }

  loading.value = true
  try {
    const res = await getLessonById(lessonId)
    if (res.success && res.data) {
      lesson.value = res.data
      // 如果没有学科名称，单独获取
      if (!lesson.value.subjectName && lesson.value.subjectId) {
        const subjectRes = await getSubjectById(lesson.value.subjectId)
        if (subjectRes.success && subjectRes.data) {
          lesson.value.subjectName = subjectRes.data.name
        }
      }
    } else {
      ElMessage.error('课程不存在')
    }
  } catch (error) {
    console.error('加载课程失败:', error)
    ElMessage.error('加载课程失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/student/calendar')
}

const handleGoSubmit = () => {
  router.push('/submissions')
}

onMounted(() => {
  loadLesson()
})
</script>

<style scoped lang="scss">
.lesson-detail-page {
  min-height: 100%;
  background: var(--bg-secondary);
  padding-bottom: var(--spacing-2xl);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-md) var(--spacing-xl);
    background: var(--bg-card);
    border-bottom: 1px solid var(--border-default);
    position: sticky;
    top: 0;
    z-index: 10;

    .header-left {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);

      :deep(.el-breadcrumb) {
        font-size: var(--text-sm);
      }
    }
  }

  .page-content {
    max-width: 1000px;
    margin: 0 auto;
    padding: var(--spacing-xl);
  }

  .lesson-title-section {
    display: flex;
    align-items: center;
    gap: var(--spacing-md);
    margin-bottom: var(--spacing-xl);
    flex-wrap: wrap;

    .lesson-title {
      margin: 0;
      font-size: var(--text-3xl);
      font-weight: var(--font-weight-bold);
      color: var(--text-primary);
    }
  }

  .info-card {
    margin-bottom: var(--spacing-xl);
    background: var(--bg-card);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-lg);

    :deep(.el-card__body) {
      padding: var(--spacing-lg);
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: var(--spacing-lg);
    }

    .info-item {
      display: flex;
      align-items: flex-start;
      gap: var(--spacing-md);

      .info-icon {
        width: 40px;
        height: 40px;
        border-radius: var(--radius-md);
        background: var(--color-primary-light);
        color: var(--color-primary);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        &.deadline {
          background: var(--color-warning-light);
          color: var(--color-warning);
        }
      }

      .info-content {
        display: flex;
        flex-direction: column;
        gap: var(--spacing-xs);

        .info-label {
          font-size: var(--text-xs);
          color: var(--text-tertiary);
        }

        .info-value {
          font-size: var(--text-sm);
          color: var(--text-primary);
          font-weight: var(--font-weight-medium);

          &.is-expired {
            color: var(--color-danger);
          }
        }
      }
    }
  }

  .content-card {
    margin-bottom: var(--spacing-xl);
    background: var(--bg-card);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-lg);

    :deep(.el-card__header) {
      padding: var(--spacing-md) var(--spacing-lg);
      border-bottom: 1px solid var(--border-default);
      background: var(--bg-secondary);
    }

    :deep(.el-card__body) {
      padding: var(--spacing-lg);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      font-size: var(--text-base);
      font-weight: var(--font-weight-semibold);
      color: var(--text-primary);
    }

    .markdown-content {
      :deep(.markdown-preview) {
        background: transparent;
        border: none;
        padding: 0;

        // 确保代码块和表格等元素正确显示
        pre {
          background: var(--bg-secondary);
          border-radius: var(--radius-md);
          padding: var(--spacing-md);
          overflow-x: auto;
        }

        table {
          width: 100%;
          border-collapse: collapse;

          th, td {
            border: 1px solid var(--border-default);
            padding: var(--spacing-sm) var(--spacing-md);
          }

          th {
            background: var(--bg-secondary);
          }
        }

        img {
          max-width: 100%;
          border-radius: var(--radius-md);
        }
      }
    }

    &.homework-card {
      .card-header {
        color: var(--color-warning);
      }

      :deep(.el-card__header) {
        background: var(--color-warning-light);
      }
    }
  }

  .not-found {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 400px;
  }
}
</style>
