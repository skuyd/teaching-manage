import request from '@/utils/request'

const API_URL = '/grades'

/**
 * Grade level enum
 */
export enum GradeLevel {
  A = 'A',
  B = 'B',
  C = 'C',
  D = 'D'
}

/**
 * Grade DTO
 */
export interface GradeDTO {
  id: number
  submissionId: number
  grade: GradeLevel
  gradeDescription: string
  comment: string | null
  graderId: number
  graderName: string
  gradeTime: string
  lessonTitle?: string
  submitterName?: string
}

/**
 * Create grade request
 */
export interface CreateGradeRequest {
  submissionId: number
  grade: GradeLevel
  comment?: string
}

/**
 * Update grade request
 */
export interface UpdateGradeRequest {
  grade?: GradeLevel
  comment?: string
}

/**
 * Grade statistics
 */
export interface GradeStatistics {
  total: number
  gradeDistribution: {
    A: number
    B: number
    C: number
    D: number
  }
  averageGrade: number | null
  passRate: number
}

export interface Result<T> {
  code: number
  message: string
  data: T
  success: boolean
}

/**
 * Create new grade for a submission
 */
export function gradeSubmission(requestData: CreateGradeRequest): Promise<Result<GradeDTO>> {
  return request.post(API_URL, requestData)
}

/**
 * Update existing grade
 */
export function updateGrade(id: number, requestData: UpdateGradeRequest): Promise<Result<GradeDTO>> {
  return request.put(`${API_URL}/${id}`, requestData)
}

/**
 * Delete grade
 */
export function deleteGrade(id: number): Promise<Result<void>> {
  return request.delete(`${API_URL}/${id}`)
}

/**
 * Get grade by submission ID
 */
export function getGradeBySubmission(submissionId: number): Promise<Result<GradeDTO>> {
  return request.get(`${API_URL}/submission/${submissionId}`)
}

/**
 * Get all grades for a lesson
 */
export function getGradesByLesson(lessonId: number): Promise<Result<GradeDTO[]>> {
  return request.get(`${API_URL}/lesson/${lessonId}`)
}

/**
 * Get all grades for a student
 */
export function getGradesByStudent(studentId: number): Promise<Result<GradeDTO[]>> {
  return request.get(`${API_URL}/student/${studentId}`)
}

/**
 * Get lesson grade statistics
 */
export function getLessonStatistics(lessonId: number): Promise<Result<GradeStatistics>> {
  return request.get(`${API_URL}/lesson/${lessonId}/statistics`)
}

/**
 * Get student grade statistics
 */
export function getStudentStatistics(studentId: number): Promise<Result<GradeStatistics>> {
  return request.get(`${API_URL}/student/${studentId}/statistics`)
}
