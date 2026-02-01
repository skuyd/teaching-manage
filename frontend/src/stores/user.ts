import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { setToken, getToken, removeToken } from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, UserRole } from '@/api/types'

export interface UserInfo {
  username: string
  name: string
  role: UserRole
  token: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const user = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isTeacher = computed(() => user.value?.role === 'TEACHER')
  const isStudent = computed(() => user.value?.role === 'STUDENT')

  async function login(credentials: LoginRequest): Promise<void> {
    const res = await loginApi(credentials)
    const data = res.data

    token.value = data.token
    user.value = {
      username: data.username,
      name: data.name,
      role: data.role,
      token: data.token
    }

    setToken(data.token)
  }

  async function register(data: RegisterRequest): Promise<void> {
    await registerApi(data)
  }

  function logout(): void {
    token.value = null
    user.value = null
    removeToken()
  }

  function initFromStorage(): void {
    const storedToken = getToken()
    if (storedToken) {
      token.value = storedToken
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
    login,
    register,
    logout,
    initFromStorage
  }
})
