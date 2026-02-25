import request from '@/utils/request'
import axios from 'axios'
import { getToken } from '@/utils/request'
import type { Result } from './types'

export interface FileTreeNode {
  name: string
  path: string
  isDirectory: boolean
  children?: FileTreeNode[]
}

export interface SubmissionDTO {
  id: number
  lessonId: number
  lessonTitle?: string
  submitterId: number
  submitterName?: string
  groupId?: number
  groupName?: string
  filePath: string
  submitTime: string
  isGroupSubmission: boolean
  graded: boolean
  grade?: string
}

export interface SubmissionDetailDTO {
  id: number
  lessonId: number
  lessonTitle: string
  homeworkDesc: string
  submitterId: number
  submitterName: string
  groupId?: number
  groupName?: string
  filePath: string
  submitTime: string
  fileTree: FileTreeNode
}

/**
 * 提交作业
 */
export function submitAssignment(lessonId: number, file: File): Promise<Result<SubmissionDTO>> {
  const formData = new FormData()
  formData.append('lessonId', lessonId.toString())
  formData.append('file', file)

  return request.post('/submissions/submit', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 重新提交作业
 */
export function resubmitAssignment(lessonId: number, file: File): Promise<Result<SubmissionDTO>> {
  const formData = new FormData()
  formData.append('lessonId', lessonId.toString())
  formData.append('file', file)

  return request.post('/submissions/resubmit', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取课程的所有提交（教师视角）
 */
export function getSubmissionsByLesson(lessonId: number): Promise<Result<SubmissionDTO[]>> {
  return request.get(`/submissions/lesson/${lessonId}`)
}

/**
 * 获取提交详情
 */
export function getSubmissionDetail(id: number): Promise<Result<SubmissionDetailDTO>> {
  return request.get(`/submissions/${id}`)
}

/**
 * 获取我的提交（学员视角）
 */
export function getMySubmission(lessonId: number): Promise<Result<SubmissionDTO>> {
  return request.get(`/submissions/my/${lessonId}`)
}

/**
 * 删除提交
 */
export function deleteSubmission(id: number): Promise<Result<void>> {
  return request.delete(`/submissions/${id}`)
}

/**
 * 获取文件树
 */
export function getFileTree(submissionId: number): Promise<Result<FileTreeNode>> {
  return request.get(`/submissions/${submissionId}/file-tree`)
}

/**
 * 读取文件内容
 */
export function readFileContent(submissionId: number, path: string): Promise<Result<string>> {
  return request.get(`/submissions/${submissionId}/file-content`, {
    params: { path }
  })
}

/**
 * 下载提交文件（ZIP格式）
 */
export function downloadSubmission(id: number) {
  // 下载需要使用原生 axios 处理 blob 响应
  return axios.get(`/api/submissions/${id}/download`, {
    responseType: 'blob',
    headers: {
      Authorization: `Bearer ${getToken()}`
    }
  })
}
