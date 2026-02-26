<template>
  <el-dialog
    :model-value="modelValue"
    :title="lesson?.title || '课程详情'"
    width="800px"
    @close="handleClose"
    destroy-on-close
  >
    <div v-if="lesson" class="lesson-detail">
      <!-- 基本信息 -->
      <div class="info-section">
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">
              <el-icon><Clock /></el-icon>
              上课时间
            </span>
            <span class="info-value">{{ formatDateTime(lesson.lessonTime) }}</span>
          </div>
          <div class="info-item" v-if="lesson.deadline">
            <span class="info-label">
              <el-icon><Calendar /></el-icon>
              截止时间
            </span>
            <span class="info-value">{{ formatDateTime(lesson.deadline) }}</span>
          </div>
        </div>
        <div class="info-row">
          <div class="info-item">
            <span class="info-label">
              <el-icon><Document /></el-icon>
              提交类型
            </span>
            <el-tag :type="lesson.submitType === 'GROUP' ? 'warning' : 'primary'" size="small">
              {{ lesson.submitType === 'GROUP' ? '小组提交' : '个人提交' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">
              <el-icon><Timer /></el-icon>
              允许补交
            </span>
            <el-tag :type="lesson.allowLate ? 'success' : 'danger'" size="small">
              {{ lesson.allowLate ? '是' : '否' }}
            </el-tag>
          </div>
        </div>
      </div>

      <el-divider />

      <!-- 课程内容 -->
      <div class="content-section" v-if="lesson.content">
        <h4 class="section-title">
          <el-icon><Notebook /></el-icon>
          课程内容
        </h4>
        <MarkdownPreview :content="lesson.content" :max-height="300" />
      </div>

      <!-- 作业要求 -->
      <div class="content-section" v-if="lesson.homeworkDesc">
        <h4 class="section-title">
          <el-icon><EditPen /></el-icon>
          作业要求
        </h4>
        <MarkdownPreview :content="lesson.homeworkDesc" :max-height="300" />
      </div>

      <!-- 无内容提示 -->
      <div v-if="!lesson.content && !lesson.homeworkDesc" class="empty-content">
        <el-empty description="暂无课程内容" :image-size="100" />
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button v-if="showSubmitButton" type="primary" @click="handleGoSubmit">
        <el-icon><Upload /></el-icon>
        去提交作业
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Clock, Calendar, Document, Timer, Notebook, EditPen, Upload } from '@element-plus/icons-vue'
import type { LessonDTO } from '@/api/lesson'
import { MarkdownPreview } from '@/components/common'

interface Props {
  modelValue: boolean
  lesson: LessonDTO | null
  showSubmitButton?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showSubmitButton: true
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const router = useRouter()

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

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleGoSubmit = () => {
  handleClose()
  router.push('/submissions')
}
</script>

<style scoped lang="scss">
.lesson-detail {
  .info-section {
    .info-row {
      display: flex;
      gap: var(--spacing-xl);
      margin-bottom: var(--spacing-md);
      flex-wrap: wrap;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      min-width: 200px;

      .info-label {
        display: flex;
        align-items: center;
        gap: var(--spacing-xs);
        color: var(--text-secondary);
        font-size: var(--text-sm);
      }

      .info-value {
        color: var(--text-primary);
        font-weight: var(--font-weight-medium);
      }
    }
  }

  .content-section {
    margin-bottom: var(--spacing-lg);

    .section-title {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      margin: 0 0 var(--spacing-md) 0;
      font-size: var(--text-base);
      font-weight: var(--font-weight-semibold);
      color: var(--text-primary);
    }

    :deep(.markdown-preview) {
      background: var(--bg-secondary);
      border: 1px solid var(--border-default);
      border-radius: var(--radius-md);
    }
  }

  .empty-content {
    padding: var(--spacing-xl);
    text-align: center;
  }
}
</style>
