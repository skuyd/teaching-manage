<template>
  <div class="stat-card" :class="[`stat-card--${color}`, { 'stat-card--clickable': !!onClick }]" @click="handleClick">
    <div class="stat-card__content">
      <div class="stat-card__icon" :class="`stat-card__icon--${color}`">
        <el-icon v-if="!loading" :size="28">
          <component :is="iconComponent" />
        </el-icon>
        <el-skeleton v-else :rows="0" animated style="width: 28px; height: 28px;" />
      </div>
      <div class="stat-card__info">
        <div class="stat-card__value">
          <template v-if="!loading">{{ displayValue }}</template>
          <el-skeleton v-else :rows="0" animated style="width: 60px; height: 32px;" />
        </div>
        <div class="stat-card__label">{{ title }}</div>
        <div v-if="trend && trendValue" class="stat-card__trend" :class="`stat-card__trend--${trend}`">
          <el-icon :size="14">
            <ArrowUp v-if="trend === 'up'" />
            <ArrowDown v-else-if="trend === 'down'" />
          </el-icon>
          <span>{{ trendValue }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'

// Icon components map
const iconMap: Record<string, ReturnType<typeof defineAsyncComponent>> = {
  User: defineAsyncComponent(() => import('@element-plus/icons-vue').then(m => m.User)),
  Reading: defineAsyncComponent(() => import('@element-plus/icons-vue').then(m => m.Reading)),
  Document: defineAsyncComponent(() => import('@element-plus/icons-vue').then(m => m.Document)),
  Folder: defineAsyncComponent(() => import('@element-plus/icons-vue').then(m => m.Folder)),
  Clock: defineAsyncComponent(() => import('@element-plus/icons-vue').then(m => m.Clock)),
  CircleCheck: defineAsyncComponent(() => import('@element-plus/icons-vue').then(m => m.CircleCheck)),
  Upload: defineAsyncComponent(() => import('@element-plus/icons-vue').then(m => m.Upload)),
  Medal: defineAsyncComponent(() => import('@element-plus/icons-vue').then(m => m.Medal)),
  TrendCharts: defineAsyncComponent(() => import('@element-plus/icons-vue').then(m => m.TrendCharts)),
}

interface Props {
  title: string
  value: number | string
  icon: string
  color?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  trend?: 'up' | 'down' | 'none'
  trendValue?: string
  loading?: boolean
  onClick?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  color: 'primary',
  trend: undefined,
  trendValue: undefined,
  loading: false,
  onClick: undefined
})

const emit = defineEmits<{
  click: []
}>()

const iconComponent = computed(() => {
  return iconMap[props.icon] || iconMap.Document
})

const displayValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})

const handleClick = () => {
  if (props.onClick) {
    props.onClick()
  }
  emit('click')
}
</script>

<style scoped>
.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  transition: all var(--transition-normal);
}

.stat-card--clickable {
  cursor: pointer;
}

.stat-card:hover {
  border-color: var(--border-light);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-card--primary:hover {
  border-color: var(--color-primary);
}

.stat-card--success:hover {
  border-color: var(--color-success);
}

.stat-card--warning:hover {
  border-color: var(--color-warning);
}

.stat-card--danger:hover {
  border-color: var(--color-danger);
}

.stat-card--info:hover {
  border-color: var(--color-info);
}

.stat-card__content {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
}

.stat-card__icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card__icon--primary {
  background: rgba(59, 130, 246, 0.15);
  color: var(--color-primary);
}

.stat-card__icon--success {
  background: rgba(103, 194, 58, 0.15);
  color: var(--color-success);
}

.stat-card__icon--warning {
  background: rgba(230, 162, 60, 0.15);
  color: var(--color-warning);
}

.stat-card__icon--danger {
  background: rgba(245, 108, 108, 0.15);
  color: var(--color-danger);
}

.stat-card__icon--info {
  background: rgba(144, 147, 153, 0.15);
  color: var(--color-info);
}

.stat-card__info {
  flex: 1;
  min-width: 0;
}

.stat-card__value {
  font-size: var(--text-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: var(--spacing-xs);
}

.stat-card__label {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin-bottom: var(--spacing-xs);
}

.stat-card__trend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--text-xs);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}

.stat-card__trend--up {
  background: rgba(103, 194, 58, 0.15);
  color: var(--color-success);
}

.stat-card__trend--down {
  background: rgba(245, 108, 108, 0.15);
  color: var(--color-danger);
}

.stat-card__trend--none {
  background: rgba(144, 147, 153, 0.15);
  color: var(--color-info);
}

/* 响应式 */
@media (max-width: 768px) {
  .stat-card {
    padding: var(--spacing-md);
  }

  .stat-card__icon {
    width: 48px;
    height: 48px;
  }

  .stat-card__value {
    font-size: var(--text-2xl);
  }
}
</style>
