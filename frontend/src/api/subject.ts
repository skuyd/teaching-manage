import request from '@/utils/request'
import type { Result, PageResponse } from './types'

export interface SubjectDTO {
  id: number
  name: string
  description: string
  isGrouped: boolean
  minMembers: number
  maxMembers: number
  startDate: string
  endDate: string
  createTime: string
  studentCount?: number
  lessonCount?: number
}

export interface CreateSubjectRequest {
  name: string
  description?: string
  isGrouped: boolean
  minMembers?: number
  maxMembers?: number
  startDate?: string
  endDate?: string
}

export interface UpdateSubjectRequest {
  name?: string
  description?: string
  isGrouped?: boolean
  minMembers?: number
  maxMembers?: number
  startDate?: string
  endDate?: string
}

export function listSubjects(
  page: number,
  size: number,
  keyword?: string
): Promise<Result<PageResponse<SubjectDTO>>> {
  return request.get('/subjects', { params: { page, size, keyword } })
}

export function getSubjectById(id: number): Promise<Result<SubjectDTO>> {
  return request.get(`/subjects/${id}`)
}

export function createSubject(data: CreateSubjectRequest): Promise<Result<void>> {
  return request.post('/subjects', data)
}

export function updateSubject(id: number, data: UpdateSubjectRequest): Promise<Result<void>> {
  return request.put(`/subjects/${id}`, data)
}

export function deleteSubject(id: number): Promise<Result<void>> {
  return request.delete(`/subjects/${id}`)
}

export function addStudentToSubject(subjectId: number, studentId: number): Promise<Result<void>> {
  return request.post(`/subjects/${subjectId}/students/${studentId}`)
}

export function removeStudentFromSubject(subjectId: number, studentId: number): Promise<Result<void>> {
  return request.delete(`/subjects/${subjectId}/students/${studentId}`)
}
