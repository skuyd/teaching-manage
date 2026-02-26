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

export interface LessonDeleteStatsDTO {
  lessonId: number
  lessonTitle: string
  subjectName: string
  submissionCount: number
  gradeCount: number
  commentCount: number
}

/**
 * 获取课程删除统计信息
 */
export function getLessonDeleteStats(id: number): Promise<Result<LessonDeleteStatsDTO>> {
  return request.get(`/lessons/${id}/delete-stats`)
}

/**
 * 根据学科ID获取所有课程（不分页）
 */
export function getLessonsBySubject(subjectId: number): Promise<Result<LessonDTO[]>> {
  return request.get('/lessons/subject/' + subjectId)
}

/**
 * 获取所有课程（不分页）
 */
export function getLessons(): Promise<Result<LessonDTO[]>> {
  return request.get('/lessons/all')
}

/**
 * 调整课程时间（拖拽）
 */
export function updateLessonTime(id: number, lessonTime: string): Promise<Result<LessonDTO>> {
  return request.put(`/lessons/${id}/time`, { lessonTime })
}

/**
 * 获取当前学生所有已报名学科的课程
 */
export function getMyLessons(): Promise<Result<LessonDTO[]>> {
  return request.get('/lessons/my')
}
