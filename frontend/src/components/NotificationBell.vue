<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
      <el-button
        circle
        :icon="Bell"
        class="bell-button"
        @click="toggleDropdown"
      />
    </el-badge>

    <el-popover
      v-model:visible="dropdownVisible"
      placement="bottom-end"
      :width="400"
      trigger="manual"
      popper-class="notification-popover"
    >
      <template #reference>
        <span></span>
      </template>

      <div class="notification-dropdown">
        <div class="dropdown-header">
          <span class="title">通知中心</span>
          <el-button
            v-if="notifications.length > 0"
            type="text"
            size="small"
            @click="handleMarkAllRead"
          >
            全部已读
          </el-button>
        </div>

        <div class="notification-list">
          <template v-if="notifications.length > 0">
            <div
              v-for="notification in notifications"
              :key="notification.id"
              class="notification-item"
              :class="{ unread: !notification.isRead }"
              @click="handleNotificationClick(notification)"
            >
              <div class="notification-content">
                <div class="notification-title">
                  <span class="type-tag" :class="`type-${notification.type}`">
                    {{ getTypeText(notification.type) }}
                  </span>
                  <span class="title-text">{{ notification.title }}</span>
                </div>
                <div class="notification-body">{{ notification.content }}</div>
                <div class="notification-time">{{ formatTime(notification.createTime) }}</div>
              </div>
              <el-button
                type="text"
                :icon="Close"
                class="delete-button"
                @click.stop="handleDelete(notification.id)"
              />
            </div>
          </template>
          <div v-else class="empty-state">
            <el-icon :size="48" color="#6B6B70"><BellFilled /></el-icon>
            <p>暂无通知</p>
          </div>
        </div>

        <div class="dropdown-footer">
          <el-button type="text" @click="goToNotificationCenter">
            查看全部通知 →
          </el-button>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Close, BellFilled } from '@element-plus/icons-vue'
import type { NotificationDTO, NotificationType } from '@/api/types'
import {
  getUnreadCount,
  getUnreadNotifications,
  markAsRead,
  markAllAsRead,
  deleteNotification
} from '@/api/notification'

const router = useRouter()

const unreadCount = ref(0)
const notifications = ref<NotificationDTO[]>([])
const dropdownVisible = ref(false)
let pollingTimer: number | null = null

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.success) {
      unreadCount.value = res.data
    }
  } catch (error) {
    console.error('Failed to fetch unread count:', error)
  }
}

const fetchNotifications = async () => {
  try {
    const res = await getUnreadNotifications()
    if (res.success) {
      notifications.value = res.data.slice(0, 10) // 只显示最新10条
    }
  } catch (error) {
    console.error('Failed to fetch notifications:', error)
  }
}

const toggleDropdown = async () => {
  dropdownVisible.value = !dropdownVisible.value
  if (dropdownVisible.value) {
    await fetchNotifications()
  }
}

const handleNotificationClick = async (notification: NotificationDTO) => {
  if (!notification.isRead) {
    try {
      await markAsRead(notification.id)
      notification.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('Failed to mark as read:', error)
    }
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
    await fetchUnreadCount()
    ElMessage.success('已删除')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const goToNotificationCenter = () => {
  dropdownVisible.value = false
  router.push('/notifications')
}

const getTypeText = (type: NotificationType): string => {
  const typeMap: Record<NotificationType, string> = {
    LESSON_PUBLISHED: '课程',
    DEADLINE_REMINDER: '截止',
    GRADE_COMPLETED: '评分',
    GROUP_APPLICATION: '小组',
    GROUP_APPROVAL: '小组'
  }
  return typeMap[type] || '通知'
}

const formatTime = (time: string): string => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const startPolling = () => {
  fetchUnreadCount()
  pollingTimer = window.setInterval(() => {
    fetchUnreadCount()
  }, 30000) // 30秒轮询
}

const stopPolling = () => {
  if (pollingTimer !== null) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

onMounted(() => {
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.notification-bell {
  position: relative;
}

.notification-badge {
  :deep(.el-badge__content) {
    background-color: #FF5C00;
    border: none;
  }
}

.bell-button {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid #2A2A2E;
  color: #ADADB0;
  transition: all 0.3s;
}

.bell-button:hover {
  background: rgba(255, 92, 0, 0.1);
  border-color: #FF5C00;
  color: #FF5C00;
}

.notification-dropdown {
  background: #111113;
  color: #FFFFFF;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #2A2A2E;
}

.dropdown-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #FFFFFF;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #2A2A2E;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.notification-item.unread {
  background: rgba(255, 92, 0, 0.05);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.type-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  color: #ADADB0;
  flex-shrink: 0;
}

.type-tag.type-LESSON_PUBLISHED {
  background: rgba(52, 152, 219, 0.2);
  color: #3498DB;
}

.type-tag.type-DEADLINE_REMINDER {
  background: rgba(231, 76, 60, 0.2);
  color: #E74C3C;
}

.type-tag.type-GRADE_COMPLETED {
  background: rgba(46, 204, 113, 0.2);
  color: #2ECC71;
}

.type-tag.type-GROUP_APPLICATION,
.type-tag.type-GROUP_APPROVAL {
  background: rgba(155, 89, 182, 0.2);
  color: #9B59B6;
}

.title-text {
  font-size: 14px;
  font-weight: 500;
  color: #FFFFFF;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-body {
  font-size: 13px;
  color: #ADADB0;
  line-height: 1.5;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-time {
  font-size: 12px;
  color: #6B6B70;
}

.delete-button {
  color: #6B6B70;
  padding: 4px;
}

.delete-button:hover {
  color: #E74C3C;
}

.empty-state {
  text-align: center;
  padding: 48px 24px;
  color: #6B6B70;
}

.empty-state p {
  margin-top: 16px;
  font-size: 14px;
}

.dropdown-footer {
  padding: 12px 16px;
  text-align: center;
  border-top: 1px solid #2A2A2E;
}

.dropdown-footer .el-button {
  color: #FF5C00;
}

.dropdown-footer .el-button:hover {
  color: #FF8A4C;
}
</style>

<style>
.notification-popover {
  background: #111113 !important;
  border: 1px solid #2A2A2E !important;
  padding: 0 !important;
}

.notification-popover .el-popper__arrow::before {
  background: #111113 !important;
  border: 1px solid #2A2A2E !important;
}
</style>
