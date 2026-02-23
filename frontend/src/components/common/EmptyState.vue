<template>
  <div class="empty-state" :class="`empty-state--${size}`">
    <div class="empty-state__icon">
      <el-icon :size="iconSize">
        <component :is="iconComponent" />
      </el-icon>
    </div>
    <h3 v-if="title" class="empty-state__title">{{ title }}</h3>
    <p v-if="description" class="empty-state__description">{{ description }}</p>
    <div v-if="$slots.action" class="empty-state__action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Inbox,
  Document,
  Folder,
  Search,
  User,
  Calendar,
  List,
  PictureFilled
} from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  icon?: 'inbox' | 'document' | 'folder' | 'search' | 'user' | 'calendar' | 'list' | 'image'
  title?: string
  description?: string
  size?: 'small' | 'medium' | 'large'
}>(), {
  icon: 'inbox',
  title: '暂无数据',
  description: '',
  size: 'medium'
})

const iconComponent = computed(() => {
  const icons = {
    inbox: Inbox,
    document: Document,
    folder: Folder,
    search: Search,
    user: User,
    calendar: Calendar,
    list: List,
    image: PictureFilled
  }
  return icons[props.icon] || Inbox
})

const iconSize = computed(() => {
  const sizes = {
    small: 48,
    medium: 64,
    large: 80
  }
  return sizes[props.size]
})
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xxl);
  text-align: center;
  min-height: 200px;

  &--small {
    padding: var(--spacing-lg);
    min-height: 120px;
  }

  &--large {
    padding: var(--spacing-xxl) var(--spacing-xl);
    min-height: 300px;
  }

  &__icon {
    color: var(--text-color-placeholder);
    margin-bottom: var(--spacing-lg);
    opacity: 0.6;
  }

  &__title {
    margin: 0 0 var(--spacing-sm);
    font-size: var(--font-size-md);
    font-weight: var(--font-weight-medium);
    color: var(--text-color-secondary);
  }

  &__description {
    margin: 0 0 var(--spacing-lg);
    font-size: var(--font-size-sm);
    color: var(--text-color-placeholder);
    max-width: 300px;
  }

  &__action {
    margin-top: var(--spacing-md);
  }
}
</style>
