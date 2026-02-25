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
            <el-icon :size="48"><BellFilled /></el-icon>
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

// Props
interface Props {
  maxVisible?: number
  pollIntervalActive?: number  // 活跃状态轮询间隔（毫秒）
  pollIntervalIdle?: number    // 空闲状态轮询间隔（毫秒）
  idleThreshold?: number       // 判定为空闲的时间（毫秒）
}

const props = withDefaults(defineProps<Props>(), {
  maxVisible: 10,
  pollIntervalActive: 10000,   // 活跃时 10 秒
  pollIntervalIdle: 30000,     // 空闲时 30 秒
  idleThreshold: 60000         // 1 分钟无操作判定为空闲
})

const router = useRouter()

// State
const unreadCount = ref(0)
const notifications = ref<NotificationDTO[]>([])
const dropdownVisible = ref(false)

// Polling state
let pollingTimer: number | null = null
let lastActivityTime = Date.now()
let isPageVisible = true
let isUserActive = true

// Activity tracking
const updateActivity = () => {
  lastActivityTime = Date.now()
  if (!isUserActive) {
    isUserActive = true
    adjustPollingInterval()
  }
}

const checkActivity = () => {
  const now = Date.now()
  const wasActive = isUserActive
  isUserActive = (now - lastActivityTime) < props.idleThreshold

  if (wasActive !== isUserActive) {
    adjustPollingInterval()
  }
}

// Visibility handling
const handleVisibilityChange = () => {
  isPageVisible = document.visibilityState === 'visible'

  if (isPageVisible) {
    // 页面重新可见时立即获取一次
    fetchUnreadCount()
    startPolling()
  } else {
    // 页面不可见时停止轮询
    stopPolling()
  }
}

// Polling management
const adjustPollingInterval = () => {
  stopPolling()
  if (isPageVisible) {
    const interval = isUserActive ? props.pollIntervalActive : props.pollIntervalIdle
    pollingTimer = window.setInterval(() => {
      checkActivity()
      fetchUnreadCount()
    }, interval)
  }
}

const startPolling = () => {
  fetchUnreadCount()
  adjustPollingInterval()
}

const stopPolling = () => {
  if (pollingTimer !== null) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// API calls
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
      notifications.value = res.data.slice(0, props.maxVisible)
    }
  } catch (error) {
    console.error('Failed to fetch notifications:', error)
  }
}

// Event handlers
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

// Helpers
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

// Lifecycle
onMounted(() => {
  // Start polling
  startPolling()

  // Add activity listeners
  document.addEventListener('mousemove', updateActivity)
  document.addEventListener('keydown', updateActivity)
  document.addEventListener('click', updateActivity)
  document.addEventListener('scroll', updateActivity)

  // Add visibility listener
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  stopPolling()

  // Remove activity listeners
  document.removeEventListener('mousemove', updateActivity)
  document.removeEventListener('keydown', updateActivity)
  document.removeEventListener('click', updateActivity)
  document.removeEventListener('scroll', updateActivity)

  // Remove visibility listener
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
}

.notification-badge {
  :deep(.el-badge__content) {
    background-color: var(--color-danger);
    border: none;
  }
}

.bell-button {
  background: var(--bg-tertiary);
  border: 1px solid var(--border-default);
  color: var(--text-secondary);
  transition: all var(--transition-normal);
}

.bell-button:hover {
  background: color-mix(in srgb, var(--color-primary) 10%, transparent);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.notification-dropdown {
  background: var(--bg-card);
  color: var(--text-primary);
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--border-default);
}

.dropdown-header .title {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.notification-item:hover {
  background: var(--bg-hover);
}

.notification-item.unread {
  background: color-mix(in srgb, var(--color-primary) 5%, transparent);
}

.notification-item.unread:hover {
  background: color-mix(in srgb, var(--color-primary) 10%, transparent);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.type-tag {
  display: inline-block;
  padding: 2px var(--spacing-sm);
  font-size: var(--text-xs);
  border-radius: var(--radius-sm);
  background: var(--bg-tertiary);
  color: var(--text-muted);
  flex-shrink: 0;
}

.type-tag.type-LESSON_PUBLISHED {
  background: color-mix(in srgb, var(--color-info) 20%, transparent);
  color: var(--color-info);
}

.type-tag.type-DEADLINE_REMINDER {
  background: color-mix(in srgb, var(--color-danger) 20%, transparent);
  color: var(--color-danger);
}

.type-tag.type-GRADE_COMPLETED {
  background: color-mix(in srgb, var(--color-success) 20%, transparent);
  color: var(--color-success);
}

.type-tag.type-GROUP_APPLICATION,
.type-tag.type-GROUP_APPROVAL {
  background: color-mix(in srgb, var(--color-warning) 20%, transparent);
  color: var(--color-warning);
}

.title-text {
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-body {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: 1.5;
  margin-bottom: var(--spacing-xs);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.delete-button {
  color: var(--text-muted);
  padding: var(--spacing-xs);
}

.delete-button:hover {
  color: var(--color-danger);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-2xl) var(--spacing-lg);
  color: var(--text-muted);
}

.empty-state p {
  margin-top: var(--spacing-md);
  font-size: var(--text-sm);
}

.dropdown-footer {
  padding: var(--spacing-sm) var(--spacing-md);
  text-align: center;
  border-top: 1px solid var(--border-default);
}

.dropdown-footer .el-button {
  color: var(--color-primary);
}

.dropdown-footer .el-button:hover {
  color: var(--color-primary-light);
}
</style>

<style>
.notification-popover {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-default) !important;
  padding: 0 !important;
}

.notification-popover .el-popper__arrow::before {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-default) !important;
}
</style>
