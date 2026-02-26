<template>
  <div class="login-page">
    <!-- Theme Switcher -->
    <header class="login-header">
      <ThemeSwitcher mode="buttons" :show-message="false" />
    </header>

    <!-- Floating Card -->
    <div class="login-card-wrapper">
      <div class="login-card">
        <!-- Left: Brand Panel -->
        <div class="brand-panel">
          <div class="brand-content">
            <div class="logo-box">
              <span class="logo-icon">🎓</span>
            </div>
            <h1 class="brand-title">教学管理系统</h1>
            <p class="brand-subtitle">Teaching Management System</p>
            <p class="brand-welcome">欢迎回来</p>
          </div>
          <!-- Decorative circles -->
          <div class="decor-circle decor-circle-1"></div>
          <div class="decor-circle decor-circle-2"></div>
        </div>

        <!-- Right: Form Panel -->
        <div class="form-panel">
          <h2 class="form-title">登录</h2>
          <p class="form-subtitle">请输入您的账号和密码</p>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <label class="input-label">用户名</label>
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password">
              <label class="input-label">密码</label>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
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

          <p class="role-hint">支持管理员、教员、学员三种角色登录</p>
          <div class="divider"></div>
          <p class="help-text">首次使用？请联系系统管理员分配账号</p>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <footer class="login-footer">
      <p>&copy; 2026 教学管理系统</p>
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
   Full-screen Background
   ======================================== */
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--bg-gradient-primary);
  padding: 24px;
  box-sizing: border-box;
  animation: pageLoad 0.5s ease-out;
}

@keyframes pageLoad {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ========================================
   Header - Theme Switcher
   ======================================== */
.login-header {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 100;
}

/* ========================================
   Floating Card Wrapper
   ======================================== */
.login-card-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  width: 100%;
}

.login-card {
  display: flex;
  width: 100%;
  max-width: 1000px;
  height: 560px;
  background: #FFFFFF;
  border-radius: var(--radius-xl, 16px);
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3), 0 10px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: cardFloat 0.6s ease-out;
}

@keyframes cardFloat {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========================================
   Left: Brand Panel
   ======================================== */
.brand-panel {
  position: relative;
  width: 360px;
  min-width: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-brand, #E8EFE2);
  padding: 48px 32px;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
}

.logo-box {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary, #9CA986);
  border-radius: 16px;
  margin-bottom: 8px;
}

.logo-icon {
  font-size: 36px;
  filter: grayscale(100%) brightness(200%);
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary, #2D3436);
  margin: 0;
  line-height: 1.3;
}

.brand-subtitle {
  font-size: 13px;
  color: var(--text-secondary, #636E72);
  margin: 0;
}

.brand-welcome {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-primary-dark, #7A8A6C);
  margin: 8px 0 0 0;
}

/* Decorative Circles */
.decor-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.4;
  background: var(--color-primary, #9CA986);
}

.decor-circle-1 {
  width: 120px;
  height: 120px;
  bottom: -30px;
  left: -30px;
}

.decor-circle-2 {
  width: 80px;
  height: 80px;
  top: 40px;
  right: -20px;
  opacity: 0.25;
}

/* ========================================
   Right: Form Panel
   ======================================== */
.form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 64px;
  background: #FFFFFF;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #2D3436;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 14px;
  color: #636E72;
  margin: 0 0 32px 0;
}

.login-form {
  width: 100%;
}

.login-form *:focus-visible {
  outline: none !important;
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #636E72;
  margin-bottom: 8px;
}

.login-button {
  width: 100%;
  height: 44px;
  background: var(--btn-gradient-primary, linear-gradient(90deg, #9CA986, #7A8A68));
  border: none;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
  margin-top: 8px;
  transition: all 0.25s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--btn-hover-shadow, 0 4px 16px rgba(156, 169, 134, 0.35));
}

.role-hint {
  font-size: 13px;
  color: #9CA3AF;
  text-align: center;
  margin: 24px 0 16px 0;
}

.divider {
  height: 1px;
  background: #E5E7EB;
  margin: 0 0 16px 0;
}

.help-text {
  font-size: 13px;
  color: #636E72;
  text-align: center;
  margin: 0;
}

/* ========================================
   Footer
   ======================================== */
.login-footer {
  padding: 16px;
  text-align: center;
  color: var(--text-on-primary, rgba(255, 255, 255, 0.7));
  font-size: 13px;
}

.login-footer p {
  margin: 0;
}

/* ========================================
   Form Item Adjustments
   ======================================== */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  display: none;
}

:deep(.el-form-item__error) {
  padding-top: 4px;
}

:deep(.el-form-item.is-error .el-input__wrapper),
:deep(.el-form-item.is-error .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #F56C6C inset !important;
}

:deep(.el-form-item:focus-within) {
  outline: none !important;
}

:deep(.el-input:focus-within) {
  outline: none !important;
}

:deep(.el-input) {
  --el-input-focus-border-color: var(--color-primary, #9CA986);
}

:deep(.el-input__wrapper) {
  background: #F9FAFB !important;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #E5E7EB inset !important;
  transition: all 0.25s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary, #9CA986) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--color-primary, #9CA986) inset !important;
}

:deep(.el-input__wrapper::before),
:deep(.el-input__wrapper::after) {
  display: none !important;
}

:deep(.el-input__wrapper:focus),
:deep(.el-input__wrapper:focus-visible),
:deep(.el-input__wrapper *:focus),
:deep(.el-input__wrapper *:focus-visible) {
  outline: none !important;
}

:deep(.el-input__inner:focus),
:deep(.el-input__inner:focus-visible) {
  outline: none !important;
}

:deep(.el-input__inner) {
  color: #2D3436;
}

:deep(.el-input__inner::placeholder) {
  color: #9CA3AF;
}


/* ========================================
   Responsive Design
   ======================================== */
@media (max-width: 1024px) {
  .login-card {
    flex-direction: column;
    max-width: 420px;
    height: auto;
    max-height: none;
  }

  .brand-panel {
    width: 100%;
    min-width: auto;
    padding: 32px 24px;
  }

  .brand-title {
    font-size: 24px;
  }

  .logo-box {
    width: 60px;
    height: 60px;
  }

  .logo-icon {
    font-size: 30px;
  }

  .form-panel {
    padding: 32px 24px;
  }

  .form-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 16px;
  }

  .login-header {
    top: 16px;
    right: 16px;
  }

  .brand-panel {
    display: none;
  }

  .form-panel {
    padding: 32px 20px;
  }
}
</style>

<!-- 非 scoped 样式用于科技蓝主题的全局覆盖 -->
<style lang="scss">
/* ========================================
   Dark Theme (Tech Blue) - Login Page
   ======================================== */
:root[data-theme="tech-blue"] {
  .login-card {
    background: #0D2137;
  }

  .brand-panel {
    background: linear-gradient(180deg, #0A1628 0%, #0F2A42 100%);
  }

  .brand-title {
    color: #FFFFFF;
  }

  .brand-subtitle {
    color: #6B9CC3;
  }

  .brand-welcome {
    color: #00B4FF;
  }

  .decor-circle {
    background: #00B4FF;
  }

  .form-panel {
    background: #0D2137;
  }

  .form-title {
    color: #FFFFFF;
  }

  .form-subtitle {
    color: #6B9CC3;
  }

  .input-label {
    color: #6B9CC3;
  }

  .role-hint {
    color: #4A7A9C;
  }

  .divider {
    background: #1E3A5F;
  }

  .help-text {
    color: #6B9CC3;
  }

  /* 登录表单输入框 - 确保文字可见 */
  .login-form {
    .el-input__wrapper {
      background: #0A1628 !important;
      box-shadow: 0 0 0 1px #1E3A5F inset !important;

      &:hover {
        box-shadow: 0 0 0 1px #00B4FF inset !important;
      }

      &.is-focus {
        box-shadow: 0 0 0 2px #00B4FF inset !important;
      }
    }

    .el-input__inner {
      color: #FFFFFF !important;
      -webkit-text-fill-color: #FFFFFF !important;

      &::placeholder {
        color: #4A7A9C !important;
        -webkit-text-fill-color: #4A7A9C !important;
      }
    }

    /* 输入框前缀图标颜色 */
    .el-input__prefix {
      color: #6B9CC3;
    }
  }
}
</style>
