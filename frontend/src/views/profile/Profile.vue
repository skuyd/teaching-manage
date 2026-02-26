<template>
  <div class="profile-page">
      <div class="header">
        <h2>个人设置</h2>
      </div>

      <el-card class="profile-card">
        <div class="avatar-section">
          <el-avatar :size="120" :src="avatarUrl" class="avatar">
            <el-icon :size="60"><User /></el-icon>
          </el-avatar>
          <el-upload
            class="avatar-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            accept="image/jpeg,image/png,image/gif"
            :before-upload="beforeAvatarUpload"
            @change="handleAvatarChange"
          >
            <el-button size="small" type="primary">
              <el-icon><Upload /></el-icon>
              更换头像
            </el-button>
          </el-upload>
          <p class="avatar-tip">支持 JPG/PNG/GIF 格式，最大 2MB</p>
        </div>

        <el-divider />

        <el-descriptions :column="1" border class="user-info">
          <el-descriptions-item label="用户名">
            {{ userInfo.username }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="getRoleTagType(userInfo.role)">
              {{ getRoleLabel(userInfo.role) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatDateTime(userInfo.createTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <h3 class="section-title">基本信息</h3>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          class="profile-form"
        >
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">
              保存修改
            </el-button>
            <el-button @click="showPasswordDialog = true">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <PasswordDialog
        v-model="showPasswordDialog"
        @success="handlePasswordSuccess"
      />
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Upload } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { getCurrentUser, updateCurrentUser, uploadAvatar } from '@/api/user'
import type { UserDTO, UserRole } from '@/api/types'
import { useUserStore } from '@/stores/user'
import PasswordDialog from './PasswordDialog.vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const saving = ref(false)
const showPasswordDialog = ref(false)

const userInfo = ref<UserDTO>({
  id: 0,
  username: '',
  role: 'STUDENT',
  name: '',
  email: '',
  avatar: '',
  createTime: ''
})

const form = reactive({
  name: '',
  email: ''
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const avatarUrl = computed(() => {
  if (userInfo.value.avatar) {
    return userInfo.value.avatar.startsWith('http')
      ? userInfo.value.avatar
      : `${import.meta.env.VITE_API_BASE_URL || ''}${userInfo.value.avatar}`
  }
  return ''
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

const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    userInfo.value = res.data
    form.name = res.data.name
    form.email = res.data.email || ''
  } catch {
    ElMessage.error('获取用户信息失败')
  }
}

const beforeAvatarUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('头像只能是 JPG/PNG/GIF 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarChange = async (uploadFile: UploadFile) => {
  if (!uploadFile.raw) return

  if (!beforeAvatarUpload(uploadFile.raw)) return

  try {
    const res = await uploadAvatar(uploadFile.raw)
    userInfo.value.avatar = res.data.url
    userStore.updateUserInfo({ avatar: res.data.url })
    ElMessage.success('头像上传成功')
  } catch {
    ElMessage.error('头像上传失败')
  }
}

const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      await updateCurrentUser({
        name: form.name,
        email: form.email || undefined
      })
      userInfo.value.name = form.name
      userInfo.value.email = form.email
      userStore.updateUserInfo({ name: form.name, email: form.email })
      ElMessage.success('保存成功')
    } finally {
      saving.value = false
    }
  })
}

const handlePasswordSuccess = () => {
  ElMessage.success('密码修改成功，请重新登录')
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.profile-page {
  max-width: 600px;
  margin: 0 auto;

  .header {
    margin-bottom: 24px;

    h2 {
      margin: 0;
      color: #FFFFFF;
      font-size: 24px;
      font-weight: 600;
    }
  }

  .profile-card {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid #2A2A2E;

    :deep(.el-card__body) {
      padding: 32px;
    }

    .avatar-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;

      .avatar {
        border: 3px solid #FF5C00;
      }

      .avatar-tip {
        color: #6B6B70;
        font-size: 12px;
        margin: 0;
      }
    }

    .section-title {
      color: #FFFFFF;
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 16px 0;
    }

    .user-info {
      :deep(.el-descriptions__label) {
        background: rgba(255, 255, 255, 0.05);
        color: #ADADB0;
      }

      :deep(.el-descriptions__content) {
        background: transparent;
        color: #FFFFFF;
      }
    }

    .profile-form {
      max-width: 400px;

      :deep(.el-form-item__label) {
        color: #ADADB0;
      }
    }
  }

  :deep(.el-divider) {
    border-color: #2A2A2E;
  }
}
</style>
