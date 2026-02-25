<template>
  <MainLayout>
  <div class="submission-list dark-page">
    <el-page-header @back="handleBack" class="dark-page-header">
      <template #content>
        <span class="page-title">作业管理</span>
      </template>
    </el-page-header>

    <el-card class="search-card dark-card">
      <div class="search-bar">
        <div class="filters">
          <el-select
            v-model="selectedSubjectId"
            placeholder="选择学科"
            @change="handleSubjectChange"
            style="width: 200px"
            clearable
          >
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
          <el-select
            v-model="selectedLessonId"
            placeholder="选择课程"
            @change="handleLessonChange"
            style="width: 200px"
            :disabled="!selectedSubjectId"
            clearable
          >
            <el-option
              v-for="lesson in lessons"
              :key="lesson.id"
              :label="lesson.title"
              :value="lesson.id"
            />
          </el-select>
        </div>

        <div class="actions" v-if="selectedLessonId && userRole === 'STUDENT'">
          <el-button
            type="primary"
            :icon="Upload"
            @click="showUploadDialog = true"
            v-if="!mySubmission"
          >
            提交作业
          </el-button>
          <el-button
            type="warning"
            :icon="Upload"
            @click="showResubmitDialog = true"
            v-else
          >
            重新提交
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Student View: My Submission -->
    <el-card class="list-card dark-card" v-if="userRole === 'STUDENT' && mySubmission">
      <template #header>
        <div class="card-header">
          <span>我的提交</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="课程">
          {{ mySubmission.lessonTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDateTime(mySubmission.submitTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="小组" v-if="mySubmission.groupName">
          {{ mySubmission.groupName }}
        </el-descriptions-item>
        <el-descriptions-item label="评分状态">
          <el-tag :type="mySubmission.graded ? 'success' : 'info'">
            {{ mySubmission.graded ? '已评分' : '未评分' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div class="submission-actions">
        <el-button type="primary" @click="viewSubmission(mySubmission.id)">
          查看详情
        </el-button>
        <el-popconfirm
          title="确定要删除这个提交吗？"
          @confirm="handleDelete(mySubmission.id)"
        >
          <template #reference>
            <el-button type="danger" :icon="Delete">删除</el-button>
          </template>
        </el-popconfirm>
      </div>
    </el-card>

    <!-- Teacher View: All Submissions -->
    <el-card class="list-card dark-card" v-if="userRole !== 'STUDENT' && selectedLessonId">
      <template #header>
        <div class="card-header">
          <span>提交列表 ({{ submissions.length }} 人)</span>
        </div>
      </template>
      <el-table :data="submissions" v-loading="loading" border class="dark-table">
        <el-table-column prop="submitterName" label="提交者" width="120" />
        <el-table-column prop="groupName" label="小组" width="150">
          <template #default="{ row }">
            {{ row.groupName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="graded" label="评分状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.graded ? 'success' : 'info'">
              {{ row.graded ? '已评分' : '未评分' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewSubmission(row.id)">
              查看
            </el-button>
            <el-button
              type="success"
              size="small"
              :icon="Download"
              @click="handleDownload(row)"
            >
              下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Upload Dialog -->
    <el-dialog
      v-model="showUploadDialog"
      title="提交作业"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        :on-exceed="handleExceed"
        :on-change="handleFileChange"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持单文件或ZIP压缩包（ZIP文件会自动解压）
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
          :loading="uploading"
          :disabled="!selectedFile"
        >
          提交
        </el-button>
      </template>
    </el-dialog>

    <!-- Resubmit Dialog -->
    <el-dialog
      v-model="showResubmitDialog"
      title="重新提交作业"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-alert
        title="注意"
        type="warning"
        description="重新提交将删除之前的作业文件和评分（如有）"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-upload
        ref="reuploadRef"
        :auto-upload="false"
        :limit="1"
        :on-exceed="handleExceed"
        :on-change="handleFileChange"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持单文件或ZIP压缩包（ZIP文件会自动解压）
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="showResubmitDialog = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleResubmit"
          :loading="uploading"
          :disabled="!selectedFile"
        >
          重新提交
        </el-button>
      </template>
    </el-dialog>
  </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import MainLayout from '@/components/MainLayout.vue'
import { useRouter } from 'vue-router'
import { ElMessage, genFileId } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { UploadInstance, UploadProps, UploadRawFile } from 'element-plus'
import { Upload, Delete, UploadFilled, Download } from '@element-plus/icons-vue'
import {
  submitAssignment,
  resubmitAssignment,
  getSubmissionsByLesson,
  getMySubmission,
  deleteSubmission,
  downloadSubmission
} from '@/api/submission'
import type { SubmissionDTO } from '@/api/submission'
import { getLessonsBySubject } from '@/api/lesson'
import type { LessonDTO } from '@/api/lesson'
import { listSubjects, getMySubjects, type SubjectDTO } from '@/api/subject'

const router = useRouter()
const userStore = useUserStore()

const userRole = computed(() => userStore.user?.role || 'STUDENT')
const subjects = ref<SubjectDTO[]>([])
const selectedSubjectId = ref<number | null>(null)
const selectedLessonId = ref<number | null>(null)
const lessons = ref<LessonDTO[]>([])
const submissions = ref<SubmissionDTO[]>([])
const mySubmission = ref<SubmissionDTO | null>(null)
const loading = ref(false)
const showUploadDialog = ref(false)
const showResubmitDialog = ref(false)
const uploading = ref(false)
const selectedFile = ref<File | null>(null)

const uploadRef = ref<UploadInstance>()
const reuploadRef = ref<UploadInstance>()

const handleBack = () => {
  router.back()
}

const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString('zh-CN')
}

const handleFileChange: UploadProps['onChange'] = (uploadFile) => {
  selectedFile.value = uploadFile.raw || null
}

const handleExceed: UploadProps['onExceed'] = (files) => {
  uploadRef.value?.clearFiles()
  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  uploadRef.value?.handleStart(file)
  selectedFile.value = file
}

const handleSubjectChange = async () => {
  selectedLessonId.value = null
  lessons.value = []
  submissions.value = []
  mySubmission.value = null

  if (selectedSubjectId.value) {
    try {
      const res = await getLessonsBySubject(selectedSubjectId.value)
      if (res.success) {
        lessons.value = res.data
      }
    } catch (error) {
      console.error('Failed to load lessons:', error)
      ElMessage.error('加载课程列表失败')
    }
  }
}

const handleLessonChange = async () => {
  if (!selectedLessonId.value) return

  loading.value = true
  try {
    if (userRole.value === 'STUDENT') {
      // Load student's submission
      const res = await getMySubmission(selectedLessonId.value)
      mySubmission.value = res.success ? res.data : null
    } else {
      // Load all submissions for teachers
      const res = await getSubmissionsByLesson(selectedLessonId.value)
      if (res.success) {
        submissions.value = res.data
      }
    }
  } catch (error: any) {
    // 如果是没有提交记录，不显示错误
    if (error.message !== '未找到提交记录') {
      ElMessage.error(error.message || '加载失败')
    }
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!selectedFile.value || !selectedLessonId.value) return

  uploading.value = true
  try {
    const res = await submitAssignment(selectedLessonId.value, selectedFile.value)
    if (res.success) {
      ElMessage.success('提交成功')
      showUploadDialog.value = false
      selectedFile.value = null
      uploadRef.value?.clearFiles()
      await handleLessonChange()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '提交失败')
  } finally {
    uploading.value = false
  }
}

const handleResubmit = async () => {
  if (!selectedFile.value || !selectedLessonId.value) return

  uploading.value = true
  try {
    const res = await resubmitAssignment(selectedLessonId.value, selectedFile.value)
    if (res.success) {
      ElMessage.success('重新提交成功')
      showResubmitDialog.value = false
      selectedFile.value = null
      reuploadRef.value?.clearFiles()
      await handleLessonChange()
    } else {
      ElMessage.error(res.message || '重新提交失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '重新提交失败')
  } finally {
    uploading.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    const res = await deleteSubmission(id)
    if (res.success) {
      ElMessage.success('删除成功')
      mySubmission.value = null
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}

const viewSubmission = (id: number) => {
  router.push(`/submissions/${id}`)
}

const handleDownload = async (row: SubmissionDTO) => {
  try {
    const { data } = await downloadSubmission(row.id)
    const blob = new Blob([data], { type: 'application/zip' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `submission_${row.id}.zip`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error: any) {
    console.error('Download failed:', error)
    ElMessage.error('下载失败')
  }
}

const loadSubjects = async () => {
  try {
    if (userRole.value === 'STUDENT') {
      // 学生获取自己所属的学科
      const res = await getMySubjects()
      if (res.success) {
        subjects.value = res.data
      }
    } else {
      // 教师/管理员获取所有学科
      const res = await listSubjects(1, 100)
      if (res.success) {
        subjects.value = res.data.list
      }
    }
  } catch (error) {
    console.error('Failed to load subjects:', error)
    ElMessage.error('加载学科列表失败')
  }
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped lang="scss">
.submission-list {
  padding: var(--spacing-lg);

  .dark-page-header {
    :deep(.el-page-header__left) {
      color: var(--text-primary);
    }
  }

  .page-title {
    font-size: var(--text-lg);
    font-weight: var(--font-weight-semibold);
    color: var(--text-primary);
  }

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

  .search-card {
    margin-top: var(--spacing-lg);

    .search-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .filters {
        display: flex;
        gap: var(--spacing-sm);
      }
    }
  }

  .list-card {
    margin-top: var(--spacing-lg);

    .card-header {
      font-weight: var(--font-weight-semibold);
      color: var(--text-primary);
    }

    .submission-actions {
      margin-top: var(--spacing-md);
      display: flex;
      gap: var(--spacing-sm);
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
</style>
