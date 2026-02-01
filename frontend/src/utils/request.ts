import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { Result } from '@/api/types'

const TOKEN_KEY = 'token'

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res: Result = response.data

    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // Token 过期或无效
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error: AxiosError) => {
    console.error('Request error:', error)

    if (error.response?.status === 401) {
      removeToken()
      router.push('/login')
    }

    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export { request }
export default request
