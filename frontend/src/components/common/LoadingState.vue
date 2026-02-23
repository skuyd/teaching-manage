<template>
  <div class="loading-state" :class="[`loading-state--${size}`, { 'loading-state--fullscreen': fullscreen }]">
    <div class="loading-state__content">
      <div class="loading-state__spinner">
        <svg viewBox="0 0 50 50" class="spinner-svg">
          <circle
            cx="25"
            cy="25"
            r="20"
            fill="none"
            stroke="currentColor"
            stroke-width="4"
            stroke-linecap="round"
          />
        </svg>
      </div>
      <p v-if="text" class="loading-state__text">{{ text }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  text?: string
  size?: 'small' | 'medium' | 'large'
  fullscreen?: boolean
}>(), {
  text: '',
  size: 'medium',
  fullscreen: false
})
</script>

<style scoped lang="scss">
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);

  &--fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: var(--bg-color-overlay);
    z-index: var(--z-index-modal);
    padding: 0;
  }

  &--small {
    .loading-state__spinner {
      width: 24px;
      height: 24px;
    }
    .loading-state__text {
      font-size: var(--font-size-xs);
    }
  }

  &--medium {
    .loading-state__spinner {
      width: 40px;
      height: 40px;
    }
    .loading-state__text {
      font-size: var(--font-size-sm);
    }
  }

  &--large {
    .loading-state__spinner {
      width: 56px;
      height: 56px;
    }
    .loading-state__text {
      font-size: var(--font-size-base);
    }
  }

  &__content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-md);
  }

  &__spinner {
    color: var(--color-primary);
  }

  &__text {
    color: var(--text-color-secondary);
    margin: 0;
  }
}

.spinner-svg {
  width: 100%;
  height: 100%;
  animation: rotate 2s linear infinite;

  circle {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: 0;
    animation: dash 1.5s ease-in-out infinite;
  }
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}
</style>
