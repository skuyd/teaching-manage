<template>
  <div class="theme-switcher">
    <!-- 下拉菜单模式 -->
    <el-dropdown
      v-if="mode === 'dropdown'"
      trigger="click"
      @command="handleThemeChange"
    >
      <el-button :icon="Sunny" :loading="isTransitioning" circle>
        <template v-if="!isTransitioning">
          {{ themeMetadata.icon }}
        </template>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="theme in availableThemes"
            :key="theme.name"
            :command="theme.name"
            :disabled="theme.name === currentTheme"
          >
            <div class="theme-option">
              <span class="theme-icon">{{ theme.icon }}</span>
              <div class="theme-info">
                <div class="theme-name">{{ theme.displayName }}</div>
                <div class="theme-description">{{ theme.description }}</div>
              </div>
              <el-icon v-if="theme.name === currentTheme" class="theme-check">
                <Check />
              </el-icon>
            </div>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 按钮组模式 -->
    <el-button-group v-else-if="mode === 'buttons'">
      <el-button
        v-for="theme in availableThemes"
        :key="theme.name"
        :type="theme.name === currentTheme ? 'primary' : 'default'"
        :loading="isTransitioning && theme.name === currentTheme"
        @click="handleThemeChange(theme.name)"
      >
        {{ theme.icon }} {{ theme.displayName }}
      </el-button>
    </el-button-group>

    <!-- 单一切换按钮模式（循环切换） -->
    <el-tooltip
      v-else-if="mode === 'toggle'"
      :content="`当前主题: ${themeMetadata.displayName}`"
      placement="bottom"
    >
      <el-button
        :icon="Sunny"
        :loading="isTransitioning"
        circle
        @click="handleCycleTheme"
      >
        <template v-if="!isTransitioning">
          {{ themeMetadata.icon }}
        </template>
      </el-button>
    </el-tooltip>

    <!-- 卡片选择模式 -->
    <div v-else-if="mode === 'cards'" class="theme-cards">
      <div
        v-for="theme in availableThemes"
        :key="theme.name"
        class="theme-card"
        :class="{ 'is-active': theme.name === currentTheme }"
        @click="handleThemeChange(theme.name)"
      >
        <div class="theme-card-icon">{{ theme.icon }}</div>
        <div class="theme-card-content">
          <h4 class="theme-card-title">{{ theme.displayName }}</h4>
          <p class="theme-card-description">{{ theme.description }}</p>
        </div>
        <div
          v-if="theme.name === currentTheme"
          class="theme-card-badge"
        >
          <el-icon><Check /></el-icon>
          <span>使用中</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Sunny, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useThemeStore, THEME_NAMES, type ThemeName } from '@/stores/theme'

/**
 * 组件 Props
 */
interface Props {
  /**
   * 显示模式
   * - dropdown: 下拉菜单（默认）
   * - buttons: 按钮组
   * - toggle: 单一切换按钮（循环）
   * - cards: 卡片选择
   */
  mode?: 'dropdown' | 'buttons' | 'toggle' | 'cards'

  /**
   * 是否显示成功消息
   */
  showMessage?: boolean

  /**
   * 是否启用过渡动画
   */
  enableTransition?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'dropdown',
  showMessage: true,
  enableTransition: true
})

/**
 * 组件 Emits
 */
interface Emits {
  (e: 'change', theme: ThemeName): void
}

const emit = defineEmits<Emits>()

// ========================================
// Store
// ========================================
const themeStore = useThemeStore()

// ========================================
// Computed
// ========================================
const currentTheme = computed(() => themeStore.currentTheme)
const themeMetadata = computed(() => themeStore.themeMetadata)
const availableThemes = computed(() => themeStore.availableThemes)
const isTransitioning = computed(() => themeStore.isTransitioning)

// ========================================
// Methods
// ========================================

/**
 * 处理主题切换
 */
async function handleThemeChange(theme: ThemeName): Promise<void> {
  if (theme === currentTheme.value) {
    return
  }

  try {
    if (props.enableTransition) {
      await themeStore.setThemeWithTransition(theme)
    } else {
      themeStore.setTheme(theme)
    }

    // 显示成功消息
    if (props.showMessage) {
      const metadata = availableThemes.value.find(t => t.name === theme)
      ElMessage.success(`已切换到${metadata?.displayName}主题`)
    }

    // 触发事件
    emit('change', theme)
  } catch (error) {
    console.error('[ThemeSwitcher] Failed to change theme:', error)
    ElMessage.error('主题切换失败，请重试')
  }
}

/**
 * 循环切换主题
 */
async function handleCycleTheme(): Promise<void> {
  try {
    if (props.enableTransition) {
      // 获取下一个主题
      const currentIndex = THEME_NAMES.indexOf(currentTheme.value)
      const nextTheme = THEME_NAMES[(currentIndex + 1) % THEME_NAMES.length]

      await themeStore.setThemeWithTransition(nextTheme)
    } else {
      themeStore.cycleTheme()
    }

    // 显示成功消息
    if (props.showMessage) {
      ElMessage.success(`已切换到${themeMetadata.value.displayName}主题`)
    }

    // 触发事件
    emit('change', currentTheme.value)
  } catch (error) {
    console.error('[ThemeSwitcher] Failed to cycle theme:', error)
    ElMessage.error('主题切换失败，请重试')
  }
}
</script>

<style scoped lang="scss">
.theme-switcher {
  display: inline-block;
}

// ========================================
// 下拉菜单模式样式
// ========================================
.theme-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
  min-width: 280px;

  .theme-icon {
    font-size: 24px;
    flex-shrink: 0;
  }

  .theme-info {
    flex: 1;

    .theme-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-primary, #303133);
      margin-bottom: 2px;
    }

    .theme-description {
      font-size: 12px;
      color: var(--text-secondary, #909399);
      line-height: 1.4;
    }
  }

  .theme-check {
    color: var(--color-primary, #409EFF);
    font-size: 18px;
  }
}

// ========================================
// 卡片选择模式样式
// ========================================
.theme-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  padding: 8px;
}

.theme-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  border: 2px solid var(--border-default, #DCDFE6);
  border-radius: var(--radius-lg, 12px);
  background-color: var(--bg-card, #FFFFFF);
  cursor: pointer;
  transition: all var(--transition-normal, 0.25s ease);

  &:hover {
    border-color: var(--color-primary, #409EFF);
    box-shadow: var(--shadow-md, 0 4px 16px rgba(0, 0, 0, 0.15));
    transform: translateY(-2px);
  }

  &.is-active {
    border-color: var(--color-primary, #409EFF);
    background-color: var(--color-primary-bg, rgba(64, 158, 255, 0.1));
  }

  .theme-card-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }

  .theme-card-content {
    text-align: center;

    .theme-card-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary, #303133);
      margin: 0 0 8px 0;
    }

    .theme-card-description {
      font-size: 14px;
      color: var(--text-secondary, #606266);
      line-height: 1.6;
      margin: 0;
    }
  }

  .theme-card-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 12px;
    background-color: var(--color-primary, #409EFF);
    color: #FFFFFF;
    border-radius: var(--radius-full, 50px);
    font-size: 12px;
    font-weight: 500;
  }
}

// ========================================
// 响应式设计
// ========================================
@media (max-width: 768px) {
  .theme-cards {
    grid-template-columns: 1fr;
  }

  .theme-option {
    min-width: 240px;

    .theme-info .theme-description {
      display: none;
    }
  }
}
</style>
