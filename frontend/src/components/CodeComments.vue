<template>
  <div class="code-comments">
    <div class="comments-header">
      <div class="header-title">
        <el-icon><ChatLineSquare /></el-icon>
        <span>代码评论 ({{ comments.length }})</span>
      </div>
      <el-button
        v-if="isTeacher && selectedLine"
        type="primary"
        size="small"
        @click="showAddDialog = true"
      >
        添加评论
      </el-button>
    </div>

    <div class="comments-list">
      <template v-if="comments.length > 0">
        <div
          v-for="comment in sortedComments"
          :key="comment.id"
          class="comment-item"
          :class="{ 'is-highlighted': comment.lineNumber === selectedLine }"
        >
          <div class="comment-header">
            <div class="comment-meta">
              <span class="commenter-name">{{ comment.commenterName }}</span>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              <el-tag size="small" type="info">第 {{ comment.lineNumber }} 行</el-tag>
            </div>
            <div class="comment-actions" v-if="canEdit(comment)">
              <el-button
                type="text"
                size="small"
                @click="handleEdit(comment)"
              >
                编辑
              </el-button>
              <el-popconfirm
                title="确定要删除这条评论吗？"
                @confirm="handleDelete(comment.id)"
              >
                <template #reference>
                  <el-button type="text" size="small" style="color: var(--el-color-danger)">
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
          <div class="comment-content">
            {{ comment.content }}
          </div>
        </div>
      </template>
      <el-empty
        v-else
        description="暂无评论"
        :image-size="80"
      />
    </div>

    <!-- Add Comment Dialog -->
    <el-dialog
      v-model="showAddDialog"
      title="添加评论"
      width="500px"
    >
      <el-form :model="commentForm" label-width="80px">
        <el-form-item label="行号">
          <el-input-number
            v-model="commentForm.lineNumber"
            :min="1"
            :disabled="!!selectedLine"
          />
        </el-form-item>
        <el-form-item label="评论内容">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="4"
            placeholder="输入评论内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="submitting">
          提交
        </el-button>
      </template>
    </el-dialog>

    <!-- Edit Comment Dialog -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑评论"
      width="500px"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="评论内容">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="4"
            placeholder="输入评论内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="submitting">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineSquare } from '@element-plus/icons-vue'
import {
  addComment,
  updateComment,
  deleteComment,
  getCommentsByFile
} from '@/api/comment'
import type { CodeCommentDTO } from '@/api/comment'

interface Props {
  submissionId: number
  filePath: string
  selectedLine?: number
}

const props = defineProps<Props>()

const isTeacher = ref(localStorage.getItem('role') === 'TEACHER' || localStorage.getItem('role') === 'ADMIN')
const userId = ref(Number(localStorage.getItem('userId')))
const comments = ref<CodeCommentDTO[]>([])
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const submitting = ref(false)

const commentForm = ref({
  lineNumber: 1,
  content: ''
})

const editForm = ref({
  id: 0,
  content: ''
})

const sortedComments = computed(() => {
  return [...comments.value].sort((a, b) => {
    // First sort by line number
    if (a.lineNumber !== b.lineNumber) {
      return a.lineNumber - b.lineNumber
    }
    // Then by creation time
    return new Date(a.createTime).getTime() - new Date(b.createTime).getTime()
  })
})

const canEdit = (comment: CodeCommentDTO) => {
  return isTeacher.value && comment.commenterId === userId.value
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

const loadComments = async () => {
  if (!props.filePath) return

  try {
    const { data } = await getCommentsByFile(props.submissionId, props.filePath)
    if (data.success) {
      comments.value = data.data
    }
  } catch (error: any) {
    console.error('加载评论失败:', error)
  }
}

const handleAdd = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const { data } = await addComment({
      submissionId: props.submissionId,
      filePath: props.filePath,
      lineNumber: commentForm.value.lineNumber,
      content: commentForm.value.content
    })

    if (data.success) {
      ElMessage.success('评论添加成功')
      showAddDialog.value = false
      commentForm.value.content = ''
      await loadComments()
    } else {
      ElMessage.error(data.message || '添加失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  } finally {
    submitting.value = false
  }
}

const handleEdit = (comment: CodeCommentDTO) => {
  editForm.value.id = comment.id
  editForm.value.content = comment.content
  showEditDialog.value = true
}

const handleUpdate = async () => {
  if (!editForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const { data } = await updateComment(editForm.value.id, editForm.value.content)

    if (data.success) {
      ElMessage.success('评论更新成功')
      showEditDialog.value = false
      await loadComments()
    } else {
      ElMessage.error(data.message || '更新失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    const { data } = await deleteComment(id)

    if (data.success) {
      ElMessage.success('评论已删除')
      await loadComments()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}

// Watch for file path changes
watch(
  () => props.filePath,
  () => {
    loadComments()
  },
  { immediate: true }
)

// Watch for selected line changes
watch(
  () => props.selectedLine,
  (newLine) => {
    if (newLine) {
      commentForm.value.lineNumber = newLine
    }
  }
)

defineExpose({
  loadComments
})
</script>

<style scoped lang="scss">
.code-comments {
  display: flex;
  flex-direction: column;
  height: 100%;

  .comments-header {
    padding: 12px 16px;
    background-color: var(--el-fill-color-light);
    border-bottom: 1px solid var(--el-border-color);
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
    }
  }

  .comments-list {
    flex: 1;
    overflow-y: auto;
    padding: 16px;

    .comment-item {
      margin-bottom: 16px;
      padding: 12px;
      border: 1px solid var(--el-border-color);
      border-radius: 4px;
      transition: background-color 0.2s;

      &.is-highlighted {
        background-color: var(--el-color-primary-light-9);
        border-color: var(--el-color-primary);
      }

      .comment-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .comment-meta {
          display: flex;
          align-items: center;
          gap: 8px;

          .commenter-name {
            font-weight: 600;
            color: var(--el-color-primary);
          }

          .comment-time {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }
        }

        .comment-actions {
          display: flex;
          gap: 4px;
        }
      }

      .comment-content {
        line-height: 1.6;
        white-space: pre-wrap;
        word-break: break-word;
      }
    }
  }
}
</style>
