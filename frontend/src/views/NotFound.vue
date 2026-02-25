<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  gap: var(--spacing-md);
  background: var(--bg-primary);

  h1 {
    font-size: 72px;
    color: var(--color-primary);
    margin: 0;
  }

  p {
    color: var(--text-secondary);
    font-size: var(--text-lg);
    margin: 0;
  }
}
</style>
