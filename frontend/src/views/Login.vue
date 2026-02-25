<template>
  <div class="login-page">
    <!-- Header with Theme Switcher -->
    <header class="login-header">
      <ThemeSwitcher mode="buttons" :show-message="false" />
    </header>

    <!-- Main Content: Left Brand + Right Form -->
    <main class="login-main">
      <!-- Left: Brand Zone -->
      <div class="brand-zone">
        <div class="brand-content">
          <div class="logo-wrapper">
            <div class="logo-icon">📚</div>
            <h1 class="brand-title">教学管理系统</h1>
          </div>

          <p class="brand-slogan">
            专业的 IT 编程培训管理平台
          </p>

          <div class="decorative-shapes">
            <div class="shape shape-1"></div>
            <div class="shape shape-2"></div>
            <div class="shape shape-3"></div>
          </div>
        </div>
      </div>

      <!-- Right: Login Form -->
      <div class="form-zone">
        <div class="login-card">
          <h2 class="form-title">欢迎登录</h2>
          <p class="form-subtitle">请输入您的账号和密码</p>

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
                show-password
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
    </main>

    <!-- Footer -->
    <footer class="login-footer">
      <p>&copy; 2026 教学管理系统 | 专业的 IT 编程培训管理平台</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ThemeSwitcher } from '@/components/common'
import { useThemeAutoInit } from '@/composables/useTheme'
import type { LoginRequest } from '@/api/types'

// Auto-initialize theme from localStorage or backend
useThemeAutoInit()

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

<style scoped lang="scss">
/* ========================================
   Login Page Container
   ======================================== */
.login-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-gradient-primary, linear-gradient(135deg, #0A1628, #0D2137, #061220));
  overflow-x: hidden;
  animation: pageLoad 0.6s ease-out;
}

@keyframes pageLoad {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* ========================================
   Header with Theme Switcher
   ======================================== */
.login-header {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 100;
  display: flex;
  justify-content: flex-end;
  padding: 0;
}

/* ========================================
   Main Content: 50/50 Split Layout
   ======================================== */
.login-main {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: calc(100vh - 60px);
  padding-top: 60px;
}

/* ========================================
   Left: Brand Zone
   ======================================== */
.brand-zone {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 64px;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 10;
  text-align: center;
  max-width: 500px;
  animation: slideInLeft 0.8s ease-out;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.logo-wrapper {
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 80px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.brand-title {
  font-family: var(--font-display, 'Noto Serif SC', serif);
  font-size: 48px;
  font-weight: 700;
  color: var(--text-on-primary, #FFFFFF);
  margin: 0;
  line-height: 1.2;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.3);
}

.brand-slogan {
  font-size: 18px;
  color: var(--text-on-primary, rgba(255, 255, 255, 0.85));
  margin: 24px 0 0 0;
  line-height: 1.6;
}

/* Decorative Shapes */
.decorative-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: var(--radius-full, 50%);
  background: var(--color-accent, rgba(255, 215, 0, 0.1));
  filter: blur(60px);
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation: pulse 8s ease-in-out infinite;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: 20%;
  right: 15%;
  animation: pulse 6s ease-in-out infinite;
  animation-delay: 2s;
}

.shape-3 {
  width: 150px;
  height: 150px;
  top: 60%;
  left: 30%;
  animation: pulse 7s ease-in-out infinite;
  animation-delay: 4s;
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.1);
  }
}

/* ========================================
   Right: Form Zone
   ======================================== */
.form-zone {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 64px;
  background: var(--bg-secondary, rgba(255, 255, 255, 0.05));
}

.login-card {
  width: 100%;
  max-width: 460px;
  padding: 48px;
  background: var(--bg-card, rgba(255, 255, 255, 0.95));
  border-radius: var(--radius-xl, 24px);
  backdrop-filter: blur(20px);
  box-shadow: var(--shadow-xl, 0 20px 60px rgba(0, 0, 0, 0.3));
  border: 1px solid var(--border-accent, rgba(255, 215, 0, 0.2));
  animation: slideInRight 0.8s ease-out;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.form-title {
  font-family: var(--font-display, 'Noto Serif SC', serif);
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary, #303133);
  margin: 0 0 8px 0;
  text-align: center;
}

.form-subtitle {
  font-size: 14px;
  color: var(--text-secondary, #606266);
  text-align: center;
  margin: 0 0 32px 0;
}

.login-form {
  margin-top: 0;
}

.login-button {
  width: 100%;
  background: var(--btn-gradient, linear-gradient(90deg, #00B4FF, #0088DD));
  border: none;
  font-size: 16px;
  font-weight: 600;
  box-shadow: var(--shadow-md, 0 4px 16px rgba(0, 180, 255, 0.25));
  transition: all var(--transition-normal, 0.25s ease);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg, 0 8px 24px rgba(0, 180, 255, 0.35));
}

.login-button:active {
  transform: translateY(0);
}

/* ========================================
   Footer
   ======================================== */
.login-footer {
  padding: 20px;
  text-align: center;
  color: var(--text-on-primary, rgba(255, 255, 255, 0.7));
  font-size: 14px;
  background: transparent;
}

.login-footer p {
  margin: 0;
}

/* ========================================
   Input Focus Animations
   ======================================== */
:deep(.el-input__wrapper) {
  transition: all var(--transition-normal, 0.25s ease);
}

:deep(.el-input__wrapper:focus-within) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--color-primary, rgba(0, 180, 255, 0.2));
}

:deep(.el-form-item) {
  transition: transform var(--transition-fast, 0.15s ease);
}

:deep(.el-form-item:focus-within) {
  transform: scale(1.01);
}

/* ========================================
   Responsive Design
   ======================================== */
@media (max-width: 1279px) {
  .login-main {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
  }

  .brand-zone {
    padding: 48px 32px 32px 32px;
    min-height: auto;
  }

  .brand-title {
    font-size: 36px;
  }

  .logo-icon {
    font-size: 60px;
  }

  .form-zone {
    padding: 32px;
  }
}

@media (max-width: 767px) {
  .brand-zone {
    display: none;
  }

  .login-header {
    top: 16px;
    right: 16px;
  }

  .login-main {
    padding-top: 80px;
  }

  .form-zone {
    padding: 24px 16px;
  }

  .login-card {
    padding: 32px 24px;
  }

  .form-title {
    font-size: 28px;
  }
}
</style>
