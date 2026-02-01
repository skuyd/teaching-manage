import request from '@/utils/request'

export enum SubmitType {
  PERSONAL = 'PERSONAL',
  GROUP = 'GROUP'
}

export interface LessonDTO {
  id: number
  subjectId: number
  title: string
  content: string
  lessonTime: string
  homeworkDesc: string
  submitType: SubmitType
  deadline: string
  allowLate: boolean
  createTime: string
  subjectName?: string
  submissionCount?: number
}

export interface CreateLessonRequest {
  subjectId: number
  title: string
  content?: string
  lessonTime: string
  homeworkDesc?: string
  submitType: SubmitType
  deadline?: string
  allowLate: boolean
}

export interface UpdateLessonRequest {
  title?: string
  content?: string
  lessonTime?: string
  homeworkDesc?: string
  submitType?: SubmitType
  deadline?: string
  allowLate?: boolean
}

export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface Result<T> {
  code: number
  message: string
  data: T
  success: boolean
}

/**
 * 分页查询课程列表
 */
export function listLessons(
  page: number,
  size: number,
  subjectId?: number,
  keyword?: string,
  startTime?: string,
  endTime?: string
): Promise<Result<PageResponse<LessonDTO>>> {
  return request.get('/lessons', {
    params: { page, size, subjectId, keyword, startTime, endTime }
  })
}

/**
 * 根据ID查询课程
 */
export function getLessonById(id: number): Promise<Result<LessonDTO>> {
  return request.get(`/lessons/${id}`)
}

/**
 * 创建课程
 */
export function createLesson(data: CreateLessonRequest): Promise<Result<LessonDTO>> {
  return request.post('/lessons', data)
}

/**
 * 更新课程
 */
export function updateLesson(id: number, data: UpdateLessonRequest): Promise<Result<LessonDTO>> {
  return request.put(`/lessons/${id}`, data)
}

/**
 * 删除课程
 */
export function deleteLesson(id: number): Promise<Result<void>> {
  return request.delete(`/lessons/${id}`)
}
