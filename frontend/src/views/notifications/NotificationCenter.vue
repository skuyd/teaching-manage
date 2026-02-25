<template>
  <MainLayout>
  <div class="notification-center">
    <div class="header">
      <h2>通知中心</h2>
      <div class="actions">
        <el-button
          v-if="notifications.length > 0"
          type="primary"
          @click="handleMarkAllRead"
        >
          全部已读
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="notification-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="未读通知" name="unread">
        <div class="notification-list">
          <template v-if="unreadNotifications.length > 0">
            <el-card
              v-for="notification in unreadNotifications"
              :key="notification.id"
              class="notification-card unread"
            >
              <div class="notification-header">
                <span class="type-tag" :class="`type-${notification.type}`">
                  {{ getTypeText(notification.type) }}
                </span>
                <span class="time">{{ formatTime(notification.createTime) }}</span>
              </div>
              <h3 class="notification-title">{{ notification.title }}</h3>
              <p class="notification-content">{{ notification.content }}</p>
              <div class="notification-actions">
                <el-button type="text" @click="handleMarkAsRead(notification.id)">
                  标记已读
                </el-button>
                <el-button type="text" @click="handleDelete(notification.id)">
                  删除
                </el-button>
              </div>
            </el-card>
          </template>
          <el-empty v-else description="暂无未读通知" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="全部通知" name="all">
        <div class="notification-list">
          <template v-if="notifications.length > 0">
            <el-card
              v-for="notification in notifications"
              :key="notification.id"
              class="notification-card"
              :class="{ read: notification.isRead }"
            >
              <div class="notification-header">
                <span class="type-tag" :class="`type-${notification.type}`">
                  {{ getTypeText(notification.type) }}
                </span>
                <span class="time">{{ formatTime(notification.createTime) }}</span>
              </div>
              <h3 class="notification-title">{{ notification.title }}</h3>
              <p class="notification-content">{{ notification.content }}</p>
              <div class="notification-actions">
                <el-button
                  v-if="!notification.isRead"
                  type="text"
                  @click="handleMarkAsRead(notification.id)"
                >
                  标记已读
                </el-button>
                <el-button type="text" @click="handleDelete(notification.id)">
                  删除
                </el-button>
              </div>
            </el-card>
          </template>
          <el-empty v-else description="暂无通知" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MainLayout from '@/components/MainLayout.vue'
import { ElMessage } from 'element-plus'
import type { NotificationDTO, NotificationType } from '@/api/types'
import {
  getUserNotifications,
  getUnreadNotifications,
  markAsRead,
  markAllAsRead,
  deleteNotification
} from '@/api/notification'

const activeTab = ref('unread')
const notifications = ref<NotificationDTO[]>([])
const unreadNotifications = ref<NotificationDTO[]>([])

const fetchNotifications = async () => {
  try {
    const res = await getUserNotifications()
    if (res.success) {
      notifications.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取通知失败')
  }
}

const fetchUnreadNotifications = async () => {
  try {
    const res = await getUnreadNotifications()
    if (res.success) {
      unreadNotifications.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取未读通知失败')
  }
}

const handleTabChange = async (tabName: string) => {
  if (tabName === 'unread') {
    await fetchUnreadNotifications()
  } else {
    await fetchNotifications()
  }
}

const handleMarkAsRead = async (id: number) => {
  try {
    await markAsRead(id)
    ElMessage.success('已标记为已读')

    // 更新列表
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.isRead = true
    }
    unreadNotifications.value = unreadNotifications.value.filter(n => n.id !== id)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')

    // 更新列表
    notifications.value.forEach(n => n.isRead = true)
    unreadNotifications.value = []
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await deleteNotification(id)
    ElMessage.success('已删除')

    // 更新列表
    notifications.value = notifications.value.filter(n => n.id !== id)
    unreadNotifications.value = unreadNotifications.value.filter(n => n.id !== id)
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const getTypeText = (type: NotificationType): string => {
  const typeMap: Record<NotificationType, string> = {
    LESSON_PUBLISHED: '课程发布',
    DEADLINE_REMINDER: '截止提醒',
    GRADE_COMPLETED: '评分完成',
    GROUP_APPLICATION: '小组申请',
    GROUP_APPROVAL: '小组审批'
  }
  return typeMap[type] || '通知'
}

const formatTime = (time: string): string => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  await fetchUnreadNotifications()
})
</script>

<style scoped lang="scss">
.notification-center {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  padding: var(--spacing-lg);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);

  h2 {
    color: var(--text-primary);
    font-size: var(--text-2xl);
    font-weight: var(--font-weight-semibold);
    margin: 0;
  }
}

.notification-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.el-tabs__header) {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__item) {
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

:deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}

:deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
}

:deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.notification-card {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);

  &.unread {
    background: color-mix(in srgb, var(--color-primary) 5%, var(--bg-card));
    border-color: color-mix(in srgb, var(--color-primary) 30%, transparent);
  }

  &.read {
    opacity: 0.7;
  }

  :deep(.el-card__body) {
    padding: var(--spacing-lg);
  }
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.type-tag {
  display: inline-block;
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: var(--text-xs);
  border-radius: var(--radius-sm);
  background: var(--bg-secondary);
  color: var(--text-secondary);

  &.type-LESSON_PUBLISHED {
    background: color-mix(in srgb, var(--color-info) 20%, transparent);
    color: var(--color-info);
  }

  &.type-DEADLINE_REMINDER {
    background: color-mix(in srgb, var(--color-danger) 20%, transparent);
    color: var(--color-danger);
  }

  &.type-GRADE_COMPLETED {
    background: color-mix(in srgb, var(--color-success) 20%, transparent);
    color: var(--color-success);
  }

  &.type-GROUP_APPLICATION,
  &.type-GROUP_APPROVAL {
    background: color-mix(in srgb, #9B59B6 20%, transparent);
    color: #9B59B6;
  }
}

.time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.notification-title {
  color: var(--text-primary);
  font-size: var(--text-lg);
  font-weight: var(--font-weight-medium);
  margin: 0 0 var(--spacing-sm) 0;
}

.notification-content {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  line-height: var(--line-height-normal);
  margin: 0 0 var(--spacing-md) 0;
}

.notification-actions {
  display: flex;
  gap: var(--spacing-md);

  .el-button {
    color: var(--color-primary);
    padding: 0;

    &:hover {
      color: var(--color-primary-light);
    }
  }
}
</style>
