<template>
  <div class="subject-list">
    <div class="header">
      <h2>学科管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        新增学科
      </el-button>
    </div>

    <el-card>
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

      <el-table :data="subjects" v-loading="loading" stripe>
        <el-table-column prop="name" label="学科名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="分组设置" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.isGrouped" type="success">
              分组 ({{ row.minMembers }}-{{ row.maxMembers }}人)
            </el-tag>
            <el-tag v-else type="info">不分组</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间范围" width="200">
          <template #default="{ row }">
            <span v-if="row.startDate && row.endDate">
              {{ row.startDate }} ~ {{ row.endDate }}
            </span>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button type="primary" link @click="goToDetail(row.id)">详情</el-button>
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
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { listSubjects, deleteSubject, type SubjectDTO } from '@/api/subject'
import SubjectForm from './SubjectForm.vue'

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
    await ElMessageBox.confirm(
      `确定要删除学科「${subject.name}」吗？`,
      '提示',
      { type: 'warning' }
    )
    await deleteSubject(subject.id)
    ElMessage.success('删除成功')
    loadSubjects()
  } catch (error) {
    // 用户取消或删除失败
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
    margin-bottom: 20px;

    h2 {
      margin: 0;
    }
  }

  .search-bar {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .text-muted {
    color: #909399;
  }
}
</style>
