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
      // Token 过期或无效（非登录页面）
      if (res.code === 401) {
        const isLoginPage = window.location.pathname === '/login'
        if (!isLoginPage) {
          ElMessage.error(res.message || '请求失败')
          removeToken()
          router.push('/login')
        }
        // 登录页面的 401 错误由调用方处理，不在此显示
      } else {
        ElMessage.error(res.message || '请求失败')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    // Return the result data, cast to bypass axios type checking
    return res as unknown as typeof response
  },
  (error: AxiosError<Result>) => {
    console.error('Request error:', error)

    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络错误'
    const isLoginPage = window.location.pathname === '/login'

    if (status === 401) {
      if (!isLoginPage) {
        ElMessage.error('登录已过期，请重新登录')
        removeToken()
        router.push('/login')
      }
      // 登录页面的 401 错误由调用方处理
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(new Error(message))
  }
)

export { request }
export default request
