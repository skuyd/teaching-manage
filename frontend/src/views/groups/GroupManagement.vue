<template>
  <div class="group-management">
    <div class="header">
      <h2>小组管理 - {{ subject?.name || '加载中...' }}</h2>
      <div class="actions">
        <el-button v-if="!myGroup" type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          创建小组
        </el-button>
      </div>
    </div>

    <!-- 我的小组卡片 -->
    <el-card v-if="myGroup" class="my-group-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">我的小组</span>
          <el-tag type="success">{{ isLeader ? '组长' : '成员' }}</el-tag>
        </div>
      </template>
      <div class="group-info">
        <div class="info-row">
          <span class="label">小组名称：</span>
          <span class="value">{{ myGroup.name }}</span>
        </div>
        <div class="info-row">
          <span class="label">组长：</span>
          <span class="value">{{ myGroup.leaderName }}</span>
        </div>
        <div class="info-row">
          <span class="label">成员数量：</span>
          <span class="value">{{ myGroup.memberCount }} / {{ subject?.maxMembers }}</span>
        </div>
        <div class="info-row">
          <span class="label">创建时间：</span>
          <span class="value">{{ formatDateTime(myGroup.createTime) }}</span>
        </div>
      </div>

      <!-- 成员列表 -->
      <el-divider />
      <div class="members-section">
        <div class="section-title">成员列表</div>
        <el-table :data="myGroup.members" stripe>
          <el-table-column prop="userName" label="姓名" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isLeader" type="warning">组长</el-tag>
              <el-tag v-else type="info">成员</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'APPROVED'" type="success">已通过</el-tag>
              <el-tag v-else-if="row.status === 'PENDING'" type="warning">待审核</el-tag>
              <el-tag v-else type="danger">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="isLeader" label="操作" width="200">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button link type="success" size="small" @click="handleApprove(row)">
                  批准
                </el-button>
                <el-button link type="danger" size="small" @click="handleReject(row)">
                  拒绝
                </el-button>
              </template>
              <template v-else-if="!row.isLeader">
                <el-button link type="danger" size="small" @click="handleRemove(row)">
                  移除
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 操作按钮 -->
      <el-divider />
      <div class="group-actions">
        <el-button v-if="isLeader" type="danger" @click="handleDisband">解散小组</el-button>
        <el-button v-else @click="handleLeave">退出小组</el-button>
      </div>
    </el-card>

    <!-- 所有小组列表 -->
    <el-card class="groups-list-card">
      <template #header>
        <div class="card-header">
          <span class="title">所有小组 ({{ groups.length }})</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索小组名称"
            clearable
            style="width: 300px"
          >
            <template #append>
              <el-button :icon="Search" />
            </template>
          </el-input>
        </div>
      </template>

      <el-table :data="filteredGroups" v-loading="loading" stripe>
        <el-table-column prop="name" label="小组名称" min-width="150" />
        <el-table-column prop="leaderName" label="组长" width="120" />
        <el-table-column label="成员" width="100">
          <template #default="{ row }">
            {{ row.memberCount }} / {{ subject?.maxMembers }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.memberCount >= (subject?.maxMembers || 0)" type="danger">
              已满
            </el-tag>
            <el-tag v-else type="success">可加入</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!myGroup && row.memberCount < (subject?.maxMembers || 0)"
              type="primary"
              link
              @click="handleJoin(row)"
            >
              申请加入
            </el-button>
            <el-button v-else type="info" link disabled>
              {{ myGroup ? '已有小组' : '已满' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建小组对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建小组"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="小组名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入小组名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  listGroupsBySubject,
  getMyGroup,
  createGroup,
  joinGroup,
  leaveGroup,
  approveMember,
  rejectMember,
  removeMember,
  disbandGroup,
  type GroupDTO,
  type GroupMemberDTO,
  type CreateGroupRequest
} from '@/api/group'
import { getSubjectById, type SubjectDTO } from '@/api/subject'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const groups = ref<GroupDTO[]>([])
const myGroup = ref<GroupDTO | null>(null)
const subject = ref<SubjectDTO | null>(null)
const searchKeyword = ref('')
const createDialogVisible = ref(false)
const formRef = ref()

const form = reactive<CreateGroupRequest>({
  subjectId: 0,
  name: ''
})

const rules = {
  name: [
    { required: true, message: '请输入小组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const subjectId = computed(() => parseInt(route.params.id as string))

const isLeader = computed(() => {
  if (!myGroup.value) return false
  return myGroup.value.members.some(m => m.isLeader)
})

const filteredGroups = computed(() => {
  if (!searchKeyword.value) return groups.value
  return groups.value.filter(g =>
    g.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const loadSubject = async () => {
  try {
    const res = await getSubjectById(subjectId.value)
    if (res.success) {
      subject.value = res.data
      if (!res.data.isGrouped) {
        ElMessage.warning('该学科未开启分组功能')
        router.push('/subjects')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载学科信息失败')
  }
}

const loadGroups = async () => {
  loading.value = true
  try {
    const res = await listGroupsBySubject(subjectId.value)
    if (res.success) {
      groups.value = res.data
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载小组列表失败')
  } finally {
    loading.value = false
  }
}

const loadMyGroup = async () => {
  try {
    const res = await getMyGroup(subjectId.value)
    if (res.success) {
      myGroup.value = res.data
    }
  } catch (error: any) {
    // 没有小组时返回404，不显示错误
    if (error.response?.status !== 404) {
      ElMessage.error(error.message || '加载我的小组失败')
    }
  }
}

const loadData = async () => {
  await loadSubject()
  await Promise.all([loadGroups(), loadMyGroup()])
}

const showCreateDialog = () => {
  form.subjectId = subjectId.value
  form.name = ''
  createDialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const handleCreate = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const res = await createGroup(form)
    if (res.success) {
      ElMessage.success('创建小组成功')
      createDialogVisible.value = false
      await loadData()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '创建小组失败')
  } finally {
    submitting.value = false
  }
}

const handleJoin = async (group: GroupDTO) => {
  try {
    await ElMessageBox.confirm(`确定要申请加入「${group.name}」吗？`, '提示', {
      type: 'info'
    })
    const res = await joinGroup(group.id)
    if (res.success) {
      ElMessage.success('申请已提交，等待组长审批')
      await loadData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '申请加入失败')
    }
  }
}

const handleApprove = async (member: GroupMemberDTO) => {
  try {
    const res = await approveMember(myGroup.value!.id, member.id)
    if (res.success) {
      ElMessage.success('已批准成员加入')
      await loadData()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '批准失败')
  }
}

const handleReject = async (member: GroupMemberDTO) => {
  try {
    await ElMessageBox.confirm(`确定要拒绝「${member.userName}」的申请吗？`, '提示', {
      type: 'warning'
    })
    const res = await rejectMember(myGroup.value!.id, member.id)
    if (res.success) {
      ElMessage.success('已拒绝该成员')
      await loadData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '拒绝失败')
    }
  }
}

const handleRemove = async (member: GroupMemberDTO) => {
  try {
    await ElMessageBox.confirm(`确定要移除「${member.userName}」吗？`, '提示', {
      type: 'warning'
    })
    const res = await removeMember(myGroup.value!.id, member.userId)
    if (res.success) {
      ElMessage.success('已移除该成员')
      await loadData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '移除失败')
    }
  }
}

const handleLeave = async () => {
  try {
    await ElMessageBox.confirm('确定要退出当前小组吗？退出后需要重新申请加入。', '提示', {
      type: 'warning'
    })
    const res = await leaveGroup(myGroup.value!.id)
    if (res.success) {
      ElMessage.success('已退出小组')
      await loadData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退出失败')
    }
  }
}

const handleDisband = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要解散小组吗？解散后所有成员将被移除，此操作不可恢复！',
      '警告',
      {
        type: 'error',
        confirmButtonText: '确定解散'
      }
    )
    const res = await disbandGroup(myGroup.value!.id)
    if (res.success) {
      ElMessage.success('小组已解散')
      await loadData()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '解散失败')
    }
  }
}

const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  return new Date(datetime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.group-management {
  padding: var(--spacing-lg);

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-lg);

    h2 {
      margin: 0;
      font-size: var(--text-2xl);
      color: var(--text-primary);
      font-weight: var(--font-weight-semibold);
    }

    .actions {
      display: flex;
      gap: var(--spacing-sm);
    }
  }

  .my-group-card {
    margin-bottom: var(--spacing-lg);
    background: var(--bg-card);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-lg);

    :deep(.el-card__header) {
      background: var(--bg-secondary);
      border-bottom: 1px solid var(--border-default);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title {
        font-size: var(--text-lg);
        font-weight: var(--font-weight-semibold);
        color: var(--text-primary);
      }
    }

    .group-info {
      .info-row {
        display: flex;
        margin-bottom: var(--spacing-sm);

        .label {
          width: 100px;
          color: var(--text-muted);
        }

        .value {
          flex: 1;
          color: var(--text-primary);
          font-weight: var(--font-weight-medium);
        }
      }
    }

    .members-section {
      .section-title {
        font-size: var(--text-sm);
        font-weight: var(--font-weight-semibold);
        margin-bottom: var(--spacing-sm);
        color: var(--text-primary);
      }

      :deep(.el-table) {
        --el-table-bg-color: transparent;
        --el-table-tr-bg-color: transparent;
        --el-table-header-bg-color: var(--bg-secondary);
        --el-table-row-hover-bg-color: color-mix(in srgb, var(--color-primary) 10%, transparent);
        --el-table-text-color: var(--text-secondary);
        --el-table-header-text-color: var(--text-primary);
        --el-table-border-color: var(--border-default);
      }
    }

    .group-actions {
      display: flex;
      justify-content: flex-end;
      gap: var(--spacing-sm);
    }
  }

  .groups-list-card {
    background: var(--bg-card);
    border: 1px solid var(--border-default);
    border-radius: var(--radius-lg);

    :deep(.el-card__header) {
      background: var(--bg-secondary);
      border-bottom: 1px solid var(--border-default);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title {
        font-size: var(--text-lg);
        font-weight: var(--font-weight-semibold);
        color: var(--text-primary);
      }
    }

    :deep(.el-table) {
      --el-table-bg-color: transparent;
      --el-table-tr-bg-color: transparent;
      --el-table-header-bg-color: var(--bg-secondary);
      --el-table-row-hover-bg-color: color-mix(in srgb, var(--color-primary) 10%, transparent);
      --el-table-text-color: var(--text-secondary);
      --el-table-header-text-color: var(--text-primary);
      --el-table-border-color: var(--border-default);
    }
  }
}
</style>
