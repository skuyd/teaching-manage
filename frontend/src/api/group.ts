import request from '@/utils/request'
import type { Result } from './types'

export type MemberStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface GroupMemberDTO {
  id: number
  groupId: number
  userId: number
  userName: string
  status: MemberStatus
  isLeader: boolean
}

export interface GroupDTO {
  id: number
  subjectId: number
  name: string
  leaderId: number
  leaderName: string
  memberCount: number
  members: GroupMemberDTO[]
  createTime: string
}

export interface CreateGroupRequest {
  subjectId: number
  name: string
}

export function listGroupsBySubject(subjectId: number): Promise<Result<GroupDTO[]>> {
  return request.get(`/groups/subject/${subjectId}`)
}

export function getGroupById(id: number): Promise<Result<GroupDTO>> {
  return request.get(`/groups/${id}`)
}

export function getMyGroup(subjectId: number): Promise<Result<GroupDTO>> {
  return request.get(`/groups/my/${subjectId}`)
}

export function createGroup(data: CreateGroupRequest): Promise<Result<void>> {
  return request.post('/groups', data)
}

export function joinGroup(groupId: number): Promise<Result<void>> {
  return request.post(`/groups/${groupId}/join`)
}

export function leaveGroup(groupId: number): Promise<Result<void>> {
  return request.post(`/groups/${groupId}/leave`)
}

export function approveMember(groupId: number, memberId: number): Promise<Result<void>> {
  return request.post(`/groups/${groupId}/members/${memberId}/approve`)
}

export function rejectMember(groupId: number, memberId: number): Promise<Result<void>> {
  return request.post(`/groups/${groupId}/members/${memberId}/reject`)
}

export function removeMember(groupId: number, userId: number): Promise<Result<void>> {
  return request.delete(`/groups/${groupId}/members/${userId}`)
}

export function disbandGroup(groupId: number): Promise<Result<void>> {
  return request.delete(`/groups/${groupId}`)
}
