<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          :disabled="isEdit"
          placeholder="请输入用户名"
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          :placeholder="isEdit ? '留空则不修改' : '请输入密码'"
        />
      </el-form-item>

      <el-form-item label="角色" prop="role">
        <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="教员" value="TEACHER" />
          <el-option label="学员" value="STUDENT" />
        </el-select>
      </el-form-item>

      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入姓名" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createUser, updateUser } from '@/api/user'
import type { UserDTO, UserRole } from '@/api/types'

const props = defineProps<{
  modelValue: boolean
  user: UserDTO | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  role: '' as UserRole | '',
  name: '',
  email: ''
})

const isEdit = computed(() => !!props.user?.id)

const validatePassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!isEdit.value && !value) {
    callback(new Error('请输入密码'))
  } else if (value && value.length < 6) {
    callback(new Error('密码长度至少6位'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50之间', trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const resetForm = () => {
  form.username = ''
  form.password = ''
  form.role = ''
  form.name = ''
  form.email = ''
}

watch(() => props.user, (user) => {
  if (user) {
    form.username = user.username
    form.password = ''
    form.role = user.role
    form.name = user.name
    form.email = user.email || ''
  } else {
    resetForm()
  }
}, { immediate: true })

const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value && props.user) {
        const updateData: Record<string, unknown> = {
          name: form.name,
          role: form.role,
          email: form.email || undefined
        }
        if (form.password) {
          updateData.password = form.password
        }
        await updateUser(props.user.id, updateData)
        ElMessage.success('更新成功')
      } else {
        await createUser({
          username: form.username,
          password: form.password,
          role: form.role as UserRole,
          name: form.name,
          email: form.email || undefined
        })
        ElMessage.success('创建成功')
      }

      emit('success')
      emit('update:modelValue', false)
    } finally {
      loading.value = false
    }
  })
}
</script>
