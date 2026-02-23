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

export interface SubjectDeleteStatsDTO {
  subjectId: number
  subjectName: string
  lessonCount: number
  submissionCount: number
  gradeCount: number
  commentCount: number
  studentCount: number
  groupCount: number
}

/**
 * 获取学科删除统计信息
 */
export function getSubjectDeleteStats(id: number): Promise<Result<SubjectDeleteStatsDTO>> {
  return request.get(`/subjects/${id}/delete-stats`)
}

export function addStudentToSubject(subjectId: number, studentId: number): Promise<Result<void>> {
  return request.post(`/subjects/${subjectId}/students/${studentId}`)
}

export function removeStudentFromSubject(subjectId: number, studentId: number): Promise<Result<void>> {
  return request.delete(`/subjects/${subjectId}/students/${studentId}`)
}

export interface StudentDTO {
  id: number
  username: string
  name: string
  email?: string
  avatar?: string
  role: string
}

/**
 * 获取学科下的学员列表
 */
export function getSubjectStudents(subjectId: number): Promise<Result<StudentDTO[]>> {
  return request.get(`/subjects/${subjectId}/students`)
}

/**
 * 获取当前学生所属的学科列表
 */
export function getMySubjects(): Promise<Result<SubjectDTO[]>> {
  return request.get('/subjects/my')
}
