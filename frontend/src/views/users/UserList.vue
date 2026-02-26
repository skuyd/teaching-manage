<template>
  <div class="user-list">
      <div class="header">
        <h2>用户管理</h2>
        <div class="header-actions">
          <el-button-group>
            <el-button @click="handleExport" :loading="exportLoading">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button @click="handleDownloadTemplate">
              <el-icon><Document /></el-icon>
              模板
            </el-button>
            <el-button @click="showImportDialog">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
          </el-button-group>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </div>

      <el-card class="dark-card">
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名/姓名"
            clearable
            style="width: 300px"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>

        <el-table :data="users" v-loading="loading" stripe class="dark-table">
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="name" label="姓名" min-width="120" />
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.role)">
                {{ getRoleLabel(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadUsers"
            @current-change="loadUsers"
          />
        </div>
      </el-card>

      <UserForm
        v-model="dialogVisible"
        :user="currentUser"
        @success="handleFormSuccess"
      />

      <!-- 导入对话框 -->
      <el-dialog
        v-model="importDialogVisible"
        title="批量导入用户"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-upload
          ref="uploadRef"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              仅支持 .xlsx 或 .xls 格式文件
            </div>
          </template>
        </el-upload>

        <!-- 导入结果 -->
        <div v-if="importResult" class="import-result">
          <el-alert
            :type="importResult.failCount === 0 ? 'success' : 'warning'"
            :closable="false"
          >
            <template #title>
              总计 {{ importResult.totalCount }} 条，成功 {{ importResult.successCount }} 条，
              失败 {{ importResult.failCount }} 条
            </template>
          </el-alert>
          <div v-if="importResult.errors.length > 0" class="error-list">
            <div class="error-title">错误详情：</div>
            <div class="error-scroll">
              <div v-for="(error, index) in importResult.errors" :key="index" class="error-item">
                {{ error }}
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <el-button @click="closeImportDialog">取消</el-button>
          <el-button
            type="primary"
            :loading="importLoading"
            :disabled="!selectedFile"
            @click="handleImport"
          >
            确认导入
          </el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadInstance, UploadFile, UploadRawFile } from 'element-plus'
import { Plus, Search, Download, Upload, Document, UploadFilled } from '@element-plus/icons-vue'
import { listUsers, deleteUser, exportUsers, downloadTemplate, importUsers } from '@/api/user'
import type { UserDTO, UserRole, UserImportResult } from '@/api/types'
import UserForm from './UserForm.vue'

const loading = ref(false)
const users = ref<UserDTO[]>([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const currentUser = ref<UserDTO | null>(null)

// 导入导出相关状态
const exportLoading = ref(false)
const importDialogVisible = ref(false)
const importLoading = ref(false)
const selectedFile = ref<File | null>(null)
const importResult = ref<UserImportResult | null>(null)
const uploadRef = ref<UploadInstance>()

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getRoleTagType = (role: UserRole) => {
  const types: Record<UserRole, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    ADMIN: 'danger',
    TEACHER: 'warning',
    STUDENT: 'info'
  }
  return types[role] || 'info'
}

const getRoleLabel = (role: UserRole) => {
  const labels: Record<UserRole, string> = {
    ADMIN: '管理员',
    TEACHER: '教员',
    STUDENT: '学员'
  }
  return labels[role] || role
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ').substring(0, 19)
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await listUsers(pagination.page, pagination.size, searchKeyword.value || undefined)
    users.value = res.data.list
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

const showCreateDialog = () => {
  currentUser.value = null
  dialogVisible.value = true
}

const showEditDialog = (user: UserDTO) => {
  currentUser.value = { ...user }
  dialogVisible.value = true
}

const handleDelete = async (user: UserDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${user.name}」吗？`,
      '提示',
      { type: 'warning' }
    )
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch {
    // 用户取消或删除失败
  }
}

const handleFormSuccess = () => {
  dialogVisible.value = false
  loadUsers()
}

// 导出用户列表
const handleExport = async () => {
  exportLoading.value = true
  try {
    const blob = await exportUsers(searchKeyword.value || undefined)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'users.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 下载导入模板
const handleDownloadTemplate = async () => {
  try {
    const blob = await downloadTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'user_import_template.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('下载模板失败')
  }
}

// 显示导入对话框
const showImportDialog = () => {
  importDialogVisible.value = true
  selectedFile.value = null
  importResult.value = null
  uploadRef.value?.clearFiles()
}

// 关闭导入对话框
const closeImportDialog = () => {
  importDialogVisible.value = false
  selectedFile.value = null
  importResult.value = null
  uploadRef.value?.clearFiles()
}

// 处理文件选择
const handleFileChange = (uploadFile: UploadFile) => {
  selectedFile.value = uploadFile.raw || null
  importResult.value = null
}

// 处理超出文件数量限制
const handleExceed = (files: File[]) => {
  uploadRef.value?.clearFiles()
  const file = files[0] as UploadRawFile
  uploadRef.value?.handleStart(file)
}

// 执行导入
const handleImport = async () => {
  if (!selectedFile.value) return

  importLoading.value = true
  try {
    const res = await importUsers(selectedFile.value)
    importResult.value = res.data
    if (res.data.successCount > 0) {
      loadUsers()
    }
    if (res.data.failCount === 0) {
      ElMessage.success(`成功导入 ${res.data.successCount} 个用户`)
    } else {
      ElMessage.warning(`导入完成，成功 ${res.data.successCount} 条，失败 ${res.data.failCount} 条`)
    }
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
.user-list {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-lg);

    h2 {
      margin: 0;
      color: var(--text-primary);
      font-size: var(--text-2xl);
      font-weight: var(--font-weight-semibold);
    }

    .header-actions {
      display: flex;
      gap: var(--spacing-sm);
      align-items: center;
    }
  }

  .dark-card {
    background: var(--bg-card);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-lg);

    :deep(.el-card__body) {
      padding: var(--spacing-lg);
    }
  }

  .search-bar {
    margin-bottom: var(--spacing-md);
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

  .pagination {
    margin-top: var(--spacing-md);
    display: flex;
    justify-content: flex-end;
  }

  .import-result {
    margin-top: var(--spacing-md);

    .error-list {
      margin-top: var(--spacing-sm);
      background: var(--bg-secondary);
      border-radius: var(--radius-sm);
      padding: var(--spacing-sm);

      .error-title {
        font-weight: var(--font-weight-medium);
        margin-bottom: var(--spacing-sm);
        color: var(--color-warning);
      }

      .error-scroll {
        max-height: 200px;
        overflow-y: auto;
      }

      .error-item {
        padding: var(--spacing-xs) 0;
        font-size: var(--text-sm);
        color: var(--text-secondary);
        border-bottom: 1px dashed var(--border-light);

        &:last-child {
          border-bottom: none;
        }
      }
    }
  }
}
</style>
