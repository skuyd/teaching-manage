<template>
  <div class="error-state" :class="`error-state--${type}`">
    <div class="error-state__icon">
      <el-icon :size="iconSize">
        <component :is="iconComponent" />
      </el-icon>
    </div>
    <h3 class="error-state__title">{{ title }}</h3>
    <p v-if="message" class="error-state__message">{{ message }}</p>
    <div v-if="showRetry || $slots.action" class="error-state__actions">
      <slot name="action">
        <el-button v-if="showRetry" type="primary" @click="handleRetry">
          <el-icon class="mr-xs"><Refresh /></el-icon>
          重试
        </el-button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  CircleCloseFilled,
  WarningFilled,
  InfoFilled,
  Refresh,
  WifiOff,
  Lock,
  QuestionFilled
} from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  type?: 'error' | 'warning' | 'info' | 'network' | 'forbidden' | 'notfound'
  title?: string
  message?: string
  showRetry?: boolean
  size?: 'small' | 'medium' | 'large'
}>(), {
  type: 'error',
  title: '出错了',
  message: '',
  showRetry: true,
  size: 'medium'
})

const emit = defineEmits<{
  retry: []
}>()

const iconComponent = computed(() => {
  const icons = {
    error: CircleCloseFilled,
    warning: WarningFilled,
    info: InfoFilled,
    network: WifiOff,
    forbidden: Lock,
    notfound: QuestionFilled
  }
  return icons[props.type] || CircleCloseFilled
})

const iconSize = computed(() => {
  const sizes = {
    small: 48,
    medium: 64,
    large: 80
  }
  return sizes[props.size]
})

function handleRetry() {
  emit('retry')
}
</script>

<style scoped lang="scss">
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xxl);
  text-align: center;
  min-height: 200px;

  &__icon {
    margin-bottom: var(--spacing-lg);
  }

  &--error .error-state__icon {
    color: var(--color-danger);
  }

  &--warning .error-state__icon {
    color: var(--color-warning);
  }

  &--info .error-state__icon {
    color: var(--color-info);
  }

  &--network .error-state__icon {
    color: var(--color-warning);
  }

  &--forbidden .error-state__icon {
    color: var(--color-danger);
  }

  &--notfound .error-state__icon {
    color: var(--color-info);
  }

  &__title {
    margin: 0 0 var(--spacing-sm);
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
    color: var(--text-color-primary);
  }

  &__message {
    margin: 0 0 var(--spacing-lg);
    font-size: var(--font-size-sm);
    color: var(--text-color-secondary);
    max-width: 400px;
  }

  &__actions {
    display: flex;
    gap: var(--spacing-sm);
  }
}

.mr-xs {
  margin-right: var(--spacing-xs);
}
</style>
