<template>
  <div class="async-content">
    <!-- Loading State -->
    <template v-if="loading">
      <slot name="loading">
        <SkeletonLoader v-if="skeleton" :type="skeletonType" :rows="skeletonRows" :columns="skeletonColumns" />
        <LoadingState v-else :text="loadingText" :size="loadingSize" />
      </slot>
    </template>

    <!-- Error State -->
    <template v-else-if="error">
      <slot name="error" :error="error" :retry="handleRetry">
        <ErrorState
          :title="errorTitle"
          :message="typeof error === 'string' ? error : error?.message || '加载失败'"
          :show-retry="showRetry"
          @retry="handleRetry"
        />
      </slot>
    </template>

    <!-- Empty State -->
    <template v-else-if="isEmpty">
      <slot name="empty">
        <EmptyState
          :icon="emptyIcon"
          :title="emptyTitle"
          :description="emptyDescription"
        >
          <template v-if="$slots['empty-action']" #action>
            <slot name="empty-action" />
          </template>
        </EmptyState>
      </slot>
    </template>

    <!-- Content -->
    <template v-else>
      <slot />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LoadingState from './LoadingState.vue'
import SkeletonLoader from './SkeletonLoader.vue'
import ErrorState from './ErrorState.vue'
import EmptyState from './EmptyState.vue'

const props = withDefaults(defineProps<{
  loading?: boolean
  error?: string | Error | null
  data?: unknown
  emptyCheck?: (data: unknown) => boolean
  // Loading options
  skeleton?: boolean
  skeletonType?: 'table' | 'card' | 'list' | 'form' | 'stats' | 'text'
  skeletonRows?: number
  skeletonColumns?: number
  loadingText?: string
  loadingSize?: 'small' | 'medium' | 'large'
  // Error options
  errorTitle?: string
  showRetry?: boolean
  // Empty options
  emptyIcon?: 'inbox' | 'document' | 'folder' | 'search' | 'user' | 'calendar' | 'list' | 'image'
  emptyTitle?: string
  emptyDescription?: string
}>(), {
  loading: false,
  error: null,
  data: undefined,
  skeleton: false,
  skeletonType: 'text',
  skeletonRows: 5,
  skeletonColumns: 4,
  loadingText: '加载中...',
  loadingSize: 'medium',
  errorTitle: '加载失败',
  showRetry: true,
  emptyIcon: 'inbox',
  emptyTitle: '暂无数据',
  emptyDescription: ''
})

const emit = defineEmits<{
  retry: []
}>()

const isEmpty = computed(() => {
  if (props.emptyCheck) {
    return props.emptyCheck(props.data)
  }
  if (props.data === undefined || props.data === null) {
    return true
  }
  if (Array.isArray(props.data)) {
    return props.data.length === 0
  }
  if (typeof props.data === 'object') {
    return Object.keys(props.data).length === 0
  }
  return false
})

function handleRetry() {
  emit('retry')
}
</script>

<style scoped lang="scss">
.async-content {
  min-height: 200px;
  position: relative;
}
</style>
