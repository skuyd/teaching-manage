<template>
  <MainLayout>
    <div class="subject-list">
      <div class="header">
        <h2>学科管理</h2>
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增学科
        </el-button>
      </div>

      <el-card class="dark-card">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学科名称"
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

      <div class="subject-cards" v-loading="loading">
        <div
          v-for="subject in subjects"
          :key="subject.id"
          class="subject-card"
          @click="goToDetail(subject.id)"
        >
          <div class="card-header">
            <h3>{{ subject.name }}</h3>
            <el-tag :type="subject.isGrouped ? 'success' : 'info'" size="small">
              {{ subject.isGrouped ? `分组(${subject.minMembers}-${subject.maxMembers}人)` : '不分组' }}
            </el-tag>
          </div>
          <p class="card-desc">{{ subject.description || '暂无描述' }}</p>
          <div class="card-meta">
            <span v-if="subject.startDate">{{ subject.startDate }} ~ {{ subject.endDate }}</span>
            <span v-else>未设置时间</span>
          </div>
          <div class="card-footer" @click.stop>
            <el-button type="primary" link @click="showEditDialog(subject)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(subject)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && subjects.length === 0" description="暂无学科" />

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadSubjects"
          @current-change="loadSubjects"
        />
      </div>
    </el-card>

      <!-- 新增/编辑对话框 -->
      <SubjectForm
        v-model="dialogVisible"
        :subject="currentSubject"
        @success="handleFormSuccess"
      />
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { listSubjects, deleteSubject, getSubjectDeleteStats, type SubjectDTO } from '@/api/subject'
import SubjectForm from './SubjectForm.vue'
import MainLayout from '@/components/MainLayout.vue'

const router = useRouter()

const loading = ref(false)
const subjects = ref<SubjectDTO[]>([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const currentSubject = ref<SubjectDTO | null>(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadSubjects = async () => {
  loading.value = true
  try {
    const res = await listSubjects(pagination.page, pagination.size, searchKeyword.value || undefined)
    subjects.value = res.data.list
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadSubjects()
}

const showCreateDialog = () => {
  currentSubject.value = null
  dialogVisible.value = true
}

const showEditDialog = (subject: SubjectDTO) => {
  currentSubject.value = { ...subject }
  dialogVisible.value = true
}

const goToDetail = (id: number) => {
  router.push(`/subjects/${id}`)
}

const handleDelete = async (subject: SubjectDTO) => {
  try {
    // 先获取删除统计信息
    const statsRes = await getSubjectDeleteStats(subject.id)
    const stats = statsRes.data

    // 构建确认消息
    let message = `确定要删除学科「${subject.name}」吗？`

    if (stats.lessonCount > 0 || stats.submissionCount > 0 || stats.studentCount > 0 || stats.groupCount > 0) {
      message = `<div style="text-align: left; line-height: 1.8;">
        <p style="color: #E6A23C; margin-bottom: 8px;">⚠️ 该学科包含以下关联数据，删除后将无法恢复：</p>
        <ul style="margin: 0; padding-left: 20px; color: #606266;">
          <li>课程：<strong>${stats.lessonCount}</strong> 个</li>
          <li>作业提交：<strong>${stats.submissionCount}</strong> 份</li>
          <li>评分记录：<strong>${stats.gradeCount}</strong> 条</li>
          <li>代码评论：<strong>${stats.commentCount}</strong> 条</li>
          <li>已加入学员：<strong>${stats.studentCount}</strong> 人</li>
          <li>小组：<strong>${stats.groupCount}</strong> 个</li>
        </ul>
        <p style="margin-top: 12px; color: #F56C6C;">确定要删除学科「${subject.name}」及所有关联数据吗？</p>
      </div>`
    }

    await ElMessageBox.confirm(
      message,
      '删除确认',
      {
        type: 'warning',
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )

    await deleteSubject(subject.id)
    ElMessage.success('删除成功')
    loadSubjects()
  } catch (error: any) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      // 实际错误，而非用户取消
      console.error('删除学科失败', error)
    }
  }
}

const handleFormSuccess = () => {
  dialogVisible.value = false
  loadSubjects()
}

onMounted(() => {
  loadSubjects()
})
</script>

<style scoped lang="scss">
.subject-list {
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

  .subject-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: var(--spacing-md);
    min-height: 200px;
  }

  .subject-card {
    background: var(--bg-secondary);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-lg);
    padding: var(--spacing-md);
    cursor: pointer;
    transition: all var(--transition-normal);

    &:hover {
      border-color: var(--color-primary);
      transform: translateY(-4px);
      box-shadow: 0 8px 24px var(--btn-shadow-primary);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: var(--spacing-sm);

      h3 {
        margin: 0;
        color: var(--text-primary);
        font-size: var(--text-lg);
        font-weight: var(--font-weight-semibold);
        flex: 1;
        margin-right: var(--spacing-sm);
      }
    }

    .card-desc {
      color: var(--text-secondary);
      font-size: var(--text-sm);
      line-height: var(--line-height-normal);
      margin: 0 0 var(--spacing-md) 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
      min-height: 42px;
    }

    .card-meta {
      color: var(--text-muted);
      font-size: var(--text-sm);
      margin-bottom: var(--spacing-md);
      padding-bottom: var(--spacing-md);
      border-bottom: 1px solid var(--border-default);
    }

    .card-footer {
      display: flex;
      gap: var(--spacing-md);
    }
  }

  .pagination {
    margin-top: var(--spacing-md);
    display: flex;
    justify-content: flex-end;
  }

  .text-muted {
    color: var(--text-muted);
  }
}
</style>
