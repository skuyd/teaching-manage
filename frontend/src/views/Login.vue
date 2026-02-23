<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">教学管理系统</h1>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { LoginRequest } from '@/api/types'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const loginFormRef = ref<FormInstance>()

const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.login(loginForm)
      ElMessage.success('登录成功')
      router.push('/')
    } catch (error: any) {
      // 登录失败使用警告提示，更友好
      const message = error.message || '登录失败'
      if (message.includes('用户名或密码') || error.code === 401) {
        ElMessage.warning('用户名或密码不正确，请重新输入')
      } else {
        ElMessage.warning(message)
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0A0A0B 0%, #111113 100%);
}

.login-card {
  width: 480px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.37);
}

.title {
  text-align: center;
  font-size: 32px;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 32px;
  background: linear-gradient(135deg, #FF5C00 0%, #FF8A4C 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-form {
  margin-top: 24px;
}

.login-button {
  width: 100%;
  background: linear-gradient(135deg, #FF5C00 0%, #FF8A4C 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
}

.login-button:hover {
  background: linear-gradient(135deg, #FF6D10 0%, #FF9A5C 100%);
}

:deep(.el-input__wrapper) {
  background-color: #1A1A1D;
  box-shadow: 0 0 0 1px #2A2A2E;
}

:deep(.el-input__inner) {
  color: #FFFFFF;
}

:deep(.el-input__inner::placeholder) {
  color: #6B6B70;
}
</style>
