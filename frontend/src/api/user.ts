import request from '@/utils/request'
import type { Result, PageResponse, UserDTO, UserRole, UserImportResult } from './types'

export interface CreateUserRequest {
  username: string
  password: string
  role: UserRole
  name: string
  email?: string
}

export interface UpdateUserRequest {
  name?: string
  role?: UserRole
  email?: string
  avatar?: string
  password?: string
}

export interface UserPreferences {
  theme: string
}

export interface UpdateUserPreferencesRequest {
  theme?: string
}

export function listUsers(
  page: number,
  size: number,
  keyword?: string
): Promise<Result<PageResponse<UserDTO>>> {
  return request.get('/users', { params: { page, size, keyword } })
}

export function getUserById(id: number): Promise<Result<UserDTO>> {
  return request.get(`/users/${id}`)
}

export function createUser(data: CreateUserRequest): Promise<Result<void>> {
  return request.post('/users', data)
}

export function updateUser(id: number, data: UpdateUserRequest): Promise<Result<void>> {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id: number): Promise<Result<void>> {
  return request.delete(`/users/${id}`)
}

export function getCurrentUser(): Promise<Result<UserDTO>> {
  return request.get('/users/me')
}

export function updateCurrentUser(data: UpdateUserRequest): Promise<Result<void>> {
  return request.put('/users/me', data)
}

export function uploadAvatar(file: File): Promise<Result<{ url: string }>> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/users/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function listStudents(): Promise<Result<UserDTO[]>> {
  return request.get('/users/students')
}

export function exportUsers(keyword?: string): Promise<Blob> {
  return request.get('/users/export', {
    params: { keyword },
    responseType: 'blob'
  })
}

export function downloadTemplate(): Promise<Blob> {
  return request.get('/users/template', {
    responseType: 'blob'
  })
}

export function importUsers(file: File): Promise<Result<UserImportResult>> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/users/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 获取当前用户偏好设置
 */
export function getUserPreferences(): Promise<Result<UserPreferences>> {
  return request.get('/users/me/preferences')
}

/**
 * 更新当前用户偏好设置
 */
export function updateUserPreferences(
  data: UpdateUserPreferencesRequest
): Promise<Result<UserPreferences>> {
  return request.patch('/users/me/preferences', data)
}
