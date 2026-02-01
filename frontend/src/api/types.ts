export interface Result<T = any> {
  code: number
  message: string
  data: T
  success: boolean
}

export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  name: string
  role: UserRole
}

export interface RegisterRequest {
  username: string
  password: string
  name: string
  email?: string
}

export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT'

export interface UserDTO {
  id: number
  username: string
  role: UserRole
  name: string
  email: string
  avatar: string
  createTime: string
}
