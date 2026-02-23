import request from '@/utils/request'
import axios from 'axios'
import { getToken } from '@/utils/request'

const API_URL = '/grade-summary'

/**
 * Overall statistics
 */
export interface OverallStatistics {
  totalGrades: number
  gradeDistribution: {
    A: number
    B: number
    C: number
    D: number
  }
  averageGrade: number
  passRate: number
}

/**
 * Grade summary DTO
 */
export interface GradeSummaryDTO {
  subjectId: number
  subjectName: string
  totalLessons: number
  gradedLessons: number
  totalStudents: number
  submittedStudents: number
  lessonSummaries: LessonGradeSummaryDTO[]
  studentSummaries: StudentGradeSummaryDTO[]
  overallStatistics: OverallStatistics
}

/**
 * Lesson grade summary DTO
 */
export interface LessonGradeSummaryDTO {
  lessonId: number
  lessonTitle: string
  lessonTime: string
  deadline: string | null
  subjectId: number
  subjectName: string
  totalStudents: number
  submittedCount: number
  gradedCount: number
  unsubmittedCount: number
  ungradedCount: number
  submissionRate: number
  averageGrade: number
  passRate: number
  gradeDistribution: {
    A: number
    B: number
    C: number
    D: number
  }
  studentGrades: StudentGradeDetail[]
}

export interface StudentGradeDetail {
  studentId: number
  studentName: string
  groupId: number | null
  groupName: string | null
  grade: string | null
  gradeDescription: string | null
  comment: string | null
  submitTime: string | null
  gradeTime: string | null
  submitted: boolean
  graded: boolean
}

/**
 * Student grade summary DTO
 */
export interface StudentGradeSummaryDTO {
  studentId: number
  studentName: string
  studentEmail: string
  totalLessons: number
  submittedCount: number
  gradedCount: number
  ungradedCount: number
  averageGrade: number
  passRate: number
  gradeDistribution: {
    A: number
    B: number
    C: number
    D: number
  }
  lessonGrades: LessonGradeDetail[]
}

export interface LessonGradeDetail {
  lessonId: number
  lessonTitle: string
  lessonTime: string
  grade: string
  gradeDescription: string
  comment: string | null
  graderName: string
  gradeTime: string
  submitted: boolean
  submitTime: string
}

export interface Result<T> {
  code: number
  message: string
  data: T
  success: boolean
}

/**
 * Get subject grade summary
 */
export function getSubjectGradeSummary(subjectId: number): Promise<Result<GradeSummaryDTO>> {
  return request.get(`${API_URL}/subject/${subjectId}`)
}

/**
 * Get lesson grade summary
 */
export function getLessonGradeSummary(lessonId: number): Promise<Result<LessonGradeSummaryDTO>> {
  return request.get(`${API_URL}/lesson/${lessonId}`)
}

/**
 * Get student grade summary
 */
export function getStudentGradeSummary(studentId: number): Promise<Result<StudentGradeSummaryDTO>> {
  return request.get(`${API_URL}/student/${studentId}`)
}

/**
 * Export subject grades to Excel
 * Note: Uses axios directly for blob response
 */
export function exportSubjectGrades(subjectId: number) {
  return axios.get(`/api${API_URL}/subject/${subjectId}/export`, {
    responseType: 'blob',
    headers: {
      Authorization: `Bearer ${getToken()}`
    }
  })
}

/**
 * Export lesson grades to Excel
 * Note: Uses axios directly for blob response
 */
export function exportLessonGrades(lessonId: number) {
  return axios.get(`/api${API_URL}/lesson/${lessonId}/export`, {
    responseType: 'blob',
    headers: {
      Authorization: `Bearer ${getToken()}`
    }
  })
}

/**
 * Export student grades to Excel
 * Note: Uses axios directly for blob response
 */
export function exportStudentGrades(studentId: number) {
  return axios.get(`/api${API_URL}/student/${studentId}/export`, {
    responseType: 'blob',
    headers: {
      Authorization: `Bearer ${getToken()}`
    }
  })
}
