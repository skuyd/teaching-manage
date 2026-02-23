import request from '@/utils/request'
import type { Result, NotificationDTO } from './types'

export function getUserNotifications(): Promise<Result<NotificationDTO[]>> {
  return request.get('/notifications')
}

export function getUnreadNotifications(): Promise<Result<NotificationDTO[]>> {
  return request.get('/notifications/unread')
}

export function getUnreadCount(): Promise<Result<number>> {
  return request.get('/notifications/unread/count')
}

export function markAsRead(id: number): Promise<Result<void>> {
  return request.put(`/notifications/${id}/read`)
}

export function markAllAsRead(): Promise<Result<void>> {
  return request.put('/notifications/read-all')
}

export function deleteNotification(id: number): Promise<Result<void>> {
  return request.delete(`/notifications/${id}`)
}
