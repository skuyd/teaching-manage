import axios from 'axios'
import type { Result } from './types'

export interface CodeCommentDTO {
  id: number
  submissionId: number
  filePath: string
  lineNumber: number
  content: string
  commenterId: number
  commenterName: string
  createTime: string
  updateTime: string
}

export interface CreateCodeCommentRequest {
  submissionId: number
  filePath: string
  lineNumber: number
  content: string
}

export interface FileCommentsDTO {
  filePath: string
  commentsByLine: Record<number, CodeCommentDTO[]>
  totalComments: number
}

const API_URL = '/api/comments'

/**
 * 添加代码评论
 */
export function addComment(request: CreateCodeCommentRequest) {
  return axios.post<Result<CodeCommentDTO>>(API_URL, request)
}

/**
 * 更新评论内容
 */
export function updateComment(id: number, content: string) {
  return axios.put<Result<CodeCommentDTO>>(`${API_URL}/${id}`, { content })
}

/**
 * 删除评论
 */
export function deleteComment(id: number) {
  return axios.delete<Result<void>>(`${API_URL}/${id}`)
}

/**
 * 获取提交的所有评论
 */
export function getCommentsBySubmission(submissionId: number) {
  return axios.get<Result<CodeCommentDTO[]>>(`${API_URL}/submission/${submissionId}`)
}

/**
 * 获取文件的所有评论
 */
export function getCommentsByFile(submissionId: number, filePath: string) {
  return axios.get<Result<CodeCommentDTO[]>>(`${API_URL}/submission/${submissionId}/file`, {
    params: { filePath }
  })
}

/**
 * 获取特定行的评论
 */
export function getCommentsByLine(submissionId: number, filePath: string, lineNumber: number) {
  return axios.get<Result<CodeCommentDTO[]>>(`${API_URL}/submission/${submissionId}/line`, {
    params: { filePath, lineNumber }
  })
}

/**
 * 获取提交的评论（按文件和行号分组）
 */
export function getCommentsGrouped(submissionId: number) {
  return axios.get<Result<FileCommentsDTO[]>>(`${API_URL}/submission/${submissionId}/grouped`)
}
