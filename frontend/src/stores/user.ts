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

const USER_INFO_KEY = 'userInfo'

function setUserInfo(user: UserInfo): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(user))
}

function getUserInfo(): UserInfo | null {
  const stored = localStorage.getItem(USER_INFO_KEY)
  return stored ? JSON.parse(stored) : null
}

function removeUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
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

    const userInfo: UserInfo = {
      username: data.username,
      name: data.name,
      role: data.role,
      token: data.token
    }

    token.value = data.token
    user.value = userInfo

    setToken(data.token)
    setUserInfo(userInfo)
  }

  async function register(data: RegisterRequest): Promise<void> {
    await registerApi(data)
  }

  function logout(): void {
    token.value = null
    user.value = null
    removeToken()
    removeUserInfo()
  }

  function initFromStorage(): void {
    const storedToken = getToken()
    const storedUser = getUserInfo()

    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = storedUser
    } else if (storedToken && !storedUser) {
      // Token exists but no user info - clear everything
      logout()
    }
  }

  function updateUserInfo(updates: Partial<UserInfo>): void {
    if (user.value) {
      user.value = { ...user.value, ...updates }
      setUserInfo(user.value)
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
    initFromStorage,
    updateUserInfo
  }
})
