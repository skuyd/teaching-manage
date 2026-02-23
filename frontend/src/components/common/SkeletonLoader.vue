<template>
  <div class="skeleton-loader">
    <!-- Table Skeleton -->
    <template v-if="type === 'table'">
      <div class="skeleton-table">
        <div class="skeleton-table__header">
          <div v-for="i in columns" :key="i" class="skeleton skeleton--text" :style="{ width: `${100 / columns}%` }"></div>
        </div>
        <div v-for="row in rows" :key="row" class="skeleton-table__row">
          <div v-for="col in columns" :key="col" class="skeleton skeleton--text" :style="{ width: `${100 / columns}%` }"></div>
        </div>
      </div>
    </template>

    <!-- Card Skeleton -->
    <template v-else-if="type === 'card'">
      <div class="skeleton-card">
        <div class="skeleton skeleton--title"></div>
        <div class="skeleton skeleton--text"></div>
        <div class="skeleton skeleton--text" style="width: 80%"></div>
        <div class="skeleton skeleton--text" style="width: 60%"></div>
      </div>
    </template>

    <!-- List Skeleton -->
    <template v-else-if="type === 'list'">
      <div v-for="i in rows" :key="i" class="skeleton-list-item">
        <div class="skeleton skeleton--avatar"></div>
        <div class="skeleton-list-item__content">
          <div class="skeleton skeleton--text" style="width: 40%"></div>
          <div class="skeleton skeleton--text" style="width: 70%"></div>
        </div>
      </div>
    </template>

    <!-- Form Skeleton -->
    <template v-else-if="type === 'form'">
      <div v-for="i in rows" :key="i" class="skeleton-form-item">
        <div class="skeleton skeleton--text" style="width: 80px; margin-bottom: 8px"></div>
        <div class="skeleton" style="height: 32px; width: 100%"></div>
      </div>
    </template>

    <!-- Stats Skeleton -->
    <template v-else-if="type === 'stats'">
      <div class="skeleton-stats">
        <div v-for="i in columns" :key="i" class="skeleton-stat-card">
          <div class="skeleton skeleton--text" style="width: 60%"></div>
          <div class="skeleton" style="height: 32px; width: 40%; margin-top: 8px"></div>
        </div>
      </div>
    </template>

    <!-- Default Text Skeleton -->
    <template v-else>
      <div v-for="i in rows" :key="i" class="skeleton skeleton--text" :style="{ width: `${Math.random() * 40 + 60}%` }"></div>
    </template>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  type?: 'table' | 'card' | 'list' | 'form' | 'stats' | 'text'
  rows?: number
  columns?: number
}>(), {
  type: 'text',
  rows: 5,
  columns: 4
})
</script>

<style scoped lang="scss">
.skeleton-loader {
  padding: var(--spacing-base);
}

.skeleton {
  background: linear-gradient(
    90deg,
    var(--border-color-lighter) 25%,
    var(--border-color-extra-light) 50%,
    var(--border-color-lighter) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--border-radius-small);

  &--text {
    height: 16px;
    margin: 8px 0;
  }

  &--title {
    height: 24px;
    width: 60%;
    margin-bottom: 16px;
  }

  &--avatar {
    width: 40px;
    height: 40px;
    border-radius: var(--border-radius-circle);
    flex-shrink: 0;
  }
}

.skeleton-table {
  &__header {
    display: flex;
    gap: var(--spacing-md);
    padding: var(--spacing-md) 0;
    border-bottom: 1px solid var(--border-color-lighter);
  }

  &__row {
    display: flex;
    gap: var(--spacing-md);
    padding: var(--spacing-md) 0;
    border-bottom: 1px solid var(--border-color-extra-light);
  }
}

.skeleton-card {
  background: var(--bg-color-card);
  border-radius: var(--border-radius-large);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.skeleton-list-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--border-color-extra-light);

  &__content {
    flex: 1;
  }
}

.skeleton-form-item {
  margin-bottom: var(--spacing-lg);
}

.skeleton-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
}

.skeleton-stat-card {
  background: var(--bg-color-card);
  border-radius: var(--border-radius-large);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>
