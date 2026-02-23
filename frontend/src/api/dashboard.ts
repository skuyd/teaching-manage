import request from '@/utils/request'
import type { Result } from './types'

export interface AdminStats {
  totalUsers: number
  totalSubjects: number
  totalLessons: number
  totalSubmissions: number
}

export interface TeacherStats {
  mySubjects: number
  pendingGrades: number
  completedGrades: number
}

export interface StudentStats {
  mySubjects: number
  submittedAssignments: number
  gradedAssignments: number
  averageGrade: number
}

export function getAdminStats(): Promise<Result<AdminStats>> {
  return request.get('/dashboard/admin/stats')
}

export function getTeacherStats(): Promise<Result<TeacherStats>> {
  return request.get('/dashboard/teacher/stats')
}

export function getStudentStats(): Promise<Result<StudentStats>> {
  return request.get('/dashboard/student/stats')
}
