<template>
  <MainLayout>
  <div class="submission-detail">
    <el-page-header @back="handleBack">
      <template #content>
        <span class="page-title">作业详情</span>
      </template>
    </el-page-header>

    <el-card class="detail-card" v-loading="loading">
      <template v-if="submission">
        <div class="submission-info">
          <div class="info-header">
            <h3>{{ submission.lessonTitle }}</h3>
            <div class="header-actions">
              <el-button
                v-if="isTeacher"
                type="success"
                :icon="Download"
                @click="handleDownload"
              >
                下载作业
              </el-button>
              <el-button
                v-if="isTeacher && !grade"
                type="primary"
                @click="gradeDialogVisible = true"
              >
                评分
              </el-button>
            </div>
          </div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="提交者">
              {{ submission.submitterName }}
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              {{ formatDateTime(submission.submitTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="小组" v-if="submission.groupName">
              {{ submission.groupName }}
            </el-descriptions-item>
            <el-descriptions-item label="评分" v-if="grade">
              <el-tag :type="getGradeType(grade.grade)" size="large">
                {{ grade.grade }} - {{ grade.gradeDescription }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="评语" v-if="grade && grade.comment" :span="2">
              {{ grade.comment }}
            </el-descriptions-item>
            <el-descriptions-item label="评分者" v-if="grade">
              {{ grade.graderName }}
            </el-descriptions-item>
            <el-descriptions-item label="评分时间" v-if="grade">
              {{ formatDateTime(grade.gradeTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="作业要求" :span="2">
              <div class="markdown-content" v-html="renderMarkdown(submission.homeworkDesc)"></div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider />

        <div class="code-viewer">
          <div class="viewer-layout">
            <div class="file-tree-panel">
              <div class="panel-header">
                <el-icon><Folder /></el-icon>
                <span>文件列表</span>
              </div>
              <div class="panel-content">
                <FileTree
                  v-if="submission.fileTree"
                  :files="[submission.fileTree]"
                  :selected-path="selectedFile?.path"
                  :persist-key="`submission-${submission.id}`"
                  searchable
                  @select="handleFileSelect"
                />
              </div>
            </div>

            <div class="code-editor-panel">
              <div class="panel-header" v-if="selectedFile">
                <el-icon><Document /></el-icon>
                <span>{{ selectedFile.name }}</span>
              </div>
              <div class="panel-content">
                <div
                  v-if="selectedFile && fileContent !== null"
                  ref="editorContainer"
                  class="monaco-editor-container"
                ></div>
                <el-empty
                  v-else
                  description="请选择一个文件查看"
                  :image-size="120"
                />
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-card>

    <!-- Grade Dialog -->
    <el-dialog
      v-model="gradeDialogVisible"
      title="评分"
      width="600px"
      @close="handleGradeDialogClose"
    >
      <GradeForm
        v-if="submission"
        :submission-id="submission.id"
        @submit="handleGradeSubmit"
        @cancel="gradeDialogVisible = false"
      />
    </el-dialog>
  </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import MainLayout from '@/components/MainLayout.vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Folder, Document, Download } from '@element-plus/icons-vue'
import * as monaco from 'monaco-editor'
import { getSubmissionDetail, readFileContent, downloadSubmission } from '@/api/submission'
import type { SubmissionDetailDTO, FileTreeNode } from '@/api/submission'
import {
  getGradeBySubmission,
  gradeSubmission,
  type GradeDTO,
  type CreateGradeRequest,
  type UpdateGradeRequest,
  GradeLevel
} from '@/api/grade'
import { useUserStore } from '@/stores/user'
import FileTree from '@/components/FileTree.vue'
import GradeForm from '@/components/GradeForm.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submission = ref<SubmissionDetailDTO | null>(null)
const selectedFile = ref<FileTreeNode | null>(null)
const fileContent = ref<string | null>(null)
const editorContainer = ref<HTMLElement | null>(null)
const grade = ref<GradeDTO | null>(null)
const gradeDialogVisible = ref(false)

let editor: monaco.editor.IStandaloneCodeEditor | null = null

/**
 * Computed
 */
const isTeacher = computed(() => {
  const role = userStore.user?.role
  return role === 'TEACHER' || role === 'ADMIN'
})

const handleBack = () => {
  router.back()
}

const handleDownload = async () => {
  if (!submission.value) return

  try {
    const { data } = await downloadSubmission(submission.value.id)
    const blob = new Blob([data], { type: 'application/zip' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `submission_${submission.value.id}.zip`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error: any) {
    console.error('Download failed:', error)
    ElMessage.error('下载失败')
  }
}

const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString('zh-CN')
}

const renderMarkdown = (markdown: string) => {
  // Simple markdown rendering (in production, use a library like marked)
  return markdown
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
}

const getLanguageFromFilename = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase()
  const languageMap: Record<string, string> = {
    'js': 'javascript',
    'ts': 'typescript',
    'jsx': 'javascript',
    'tsx': 'typescript',
    'java': 'java',
    'py': 'python',
    'cpp': 'cpp',
    'c': 'c',
    'h': 'cpp',
    'hpp': 'cpp',
    'cs': 'csharp',
    'go': 'go',
    'rs': 'rust',
    'php': 'php',
    'rb': 'ruby',
    'sql': 'sql',
    'json': 'json',
    'xml': 'xml',
    'html': 'html',
    'css': 'css',
    'scss': 'scss',
    'sass': 'sass',
    'less': 'less',
    'md': 'markdown',
    'yml': 'yaml',
    'yaml': 'yaml',
    'sh': 'shell',
    'bash': 'shell',
    'txt': 'plaintext'
  }
  return languageMap[ext || ''] || 'plaintext'
}

const handleFileSelect = async (node: FileTreeNode) => {
  if (node.isDirectory) return

  selectedFile.value = node
  loading.value = true

  try {
    const res = await readFileContent(submission.value!.id, node.path)
    if (res.success) {
      fileContent.value = res.data
      await nextTick()
      createOrUpdateEditor()
    } else {
      ElMessage.error(res.message || '读取文件失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '读取文件失败')
  } finally {
    loading.value = false
  }
}

const createOrUpdateEditor = () => {
  if (!editorContainer.value || fileContent.value === null) return

  const language = getLanguageFromFilename(selectedFile.value!.name)

  if (editor) {
    // Update existing editor
    const model = editor.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, language)
      model.setValue(fileContent.value)
    }
  } else {
    // Create new editor
    editor = monaco.editor.create(editorContainer.value, {
      value: fileContent.value,
      language: language,
      theme: 'vs-dark',
      readOnly: true,
      automaticLayout: true,
      minimap: {
        enabled: true
      },
      lineNumbers: 'on',
      scrollBeyondLastLine: false,
      fontSize: 14,
      tabSize: 2
    })
  }
}

const loadSubmission = async () => {
  const id = Number(route.params.id)
  if (!id) {
    ElMessage.error('无效的提交ID')
    return
  }

  loading.value = true
  try {
    const res = await getSubmissionDetail(id)
    if (res.success) {
      submission.value = res.data
      // Load grade
      await loadGrade(id)
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadGrade = async (submissionId: number) => {
  try {
    const res = await getGradeBySubmission(submissionId)
    if (res.success && res.data) {
      grade.value = res.data
    }
  } catch (error: any) {
    // Grade might not exist yet, that's ok
    console.log('No grade found for submission')
  }
}

const getGradeType = (gradeLevel: GradeLevel): string => {
  const typeMap: Record<GradeLevel, string> = {
    [GradeLevel.A]: 'success',
    [GradeLevel.B]: 'primary',
    [GradeLevel.C]: 'warning',
    [GradeLevel.D]: 'danger'
  }
  return typeMap[gradeLevel]
}

const handleGradeSubmit = async (data: CreateGradeRequest | UpdateGradeRequest) => {
  try {
    // For new grades, data should include submissionId
    if ('submissionId' in data) {
      const res = await gradeSubmission(data as CreateGradeRequest)
      if (res.success) {
        ElMessage.success('评分成功')
        gradeDialogVisible.value = false
        grade.value = res.data
      } else {
        ElMessage.error(res.message || '评分失败')
      }
    }
  } catch (error: any) {
    console.error('Failed to grade submission:', error)
    ElMessage.error(error.response?.data?.message || '评分失败')
  }
}

const handleGradeDialogClose = () => {
  // Reset dialog state if needed
}

onMounted(() => {
  loadSubmission()
})

onBeforeUnmount(() => {
  if (editor) {
    editor.dispose()
    editor = null
  }
})
</script>

<style scoped lang="scss">
.submission-detail {
  padding: 20px;

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }

  .detail-card {
    margin-top: 20px;

    .submission-info {
      .info-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        h3 {
          margin: 0;
          font-size: 20px;
          font-weight: 600;
        }
      }

      .markdown-content {
        line-height: 1.6;
      }
    }

    .code-viewer {
      .viewer-layout {
        display: flex;
        height: 600px;
        border: 1px solid var(--el-border-color);
        border-radius: 4px;
        overflow: hidden;

        .file-tree-panel {
          width: 280px;
          border-right: 1px solid var(--el-border-color);
          display: flex;
          flex-direction: column;

          .panel-header {
            padding: 12px 16px;
            background-color: var(--el-fill-color-light);
            border-bottom: 1px solid var(--el-border-color);
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;
          }

          .panel-content {
            flex: 1;
            overflow-y: auto;
            padding: 8px;
          }
        }

        .code-editor-panel {
          flex: 1;
          display: flex;
          flex-direction: column;

          .panel-header {
            padding: 12px 16px;
            background-color: var(--el-fill-color-light);
            border-bottom: 1px solid var(--el-border-color);
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;
          }

          .panel-content {
            flex: 1;
            position: relative;

            .monaco-editor-container {
              width: 100%;
              height: 100%;
            }
          }
        }
      }
    }
  }
}
</style>
