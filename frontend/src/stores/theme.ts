import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

/**
 * 主题名称类型
 * - chinese-red: 中国红主题（温暖、喜庆、传统）
 * - tech-blue: 科技蓝主题（现代、科技、专业）- 默认主题
 * - nature-green: 自然绿主题（清新、护眼、自然）
 */
export type ThemeName = 'chinese-red' | 'tech-blue' | 'nature-green'

/**
 * 主题元数据
 */
export interface ThemeMetadata {
  name: ThemeName
  displayName: string
  description: string
  icon: string
  primaryColor: string
}

/**
 * 主题配置
 */
export const THEME_CONFIG: Record<ThemeName, ThemeMetadata> = {
  'chinese-red': {
    name: 'chinese-red',
    displayName: '中国红',
    description: '温暖喜庆的传统风格，适合节日和庆典氛围',
    icon: '🏮',
    primaryColor: '#C41E3A'
  },
  'tech-blue': {
    name: 'tech-blue',
    displayName: '科技蓝',
    description: '现代专业的科技风格，适合长时间工作使用',
    icon: '💻',
    primaryColor: '#3B82F6'
  },
  'nature-green': {
    name: 'nature-green',
    displayName: '自然绿',
    description: '清新护眼的自然风格，减轻视觉疲劳',
    icon: '🌿',
    primaryColor: '#10B981'
  }
}

/**
 * 默认主题
 */
export const DEFAULT_THEME: ThemeName = 'tech-blue'

/**
 * 所有可用主题列表（用于循环切换）
 */
export const THEME_NAMES: ThemeName[] = ['tech-blue', 'chinese-red', 'nature-green']

/**
 * localStorage 存储键
 */
const THEME_STORAGE_KEY = 'user-theme-preference'

/**
 * 从 localStorage 获取主题
 */
function getStoredTheme(): ThemeName | null {
  const stored = localStorage.getItem(THEME_STORAGE_KEY)
  if (stored && isValidTheme(stored)) {
    return stored as ThemeName
  }
  return null
}

/**
 * 保存主题到 localStorage
 */
function setStoredTheme(theme: ThemeName): void {
  localStorage.setItem(THEME_STORAGE_KEY, theme)
}

/**
 * 从 localStorage 移除主题
 */
function removeStoredTheme(): void {
  localStorage.removeItem(THEME_STORAGE_KEY)
}

/**
 * 验证主题名称是否有效
 */
function isValidTheme(theme: string): theme is ThemeName {
  return THEME_NAMES.includes(theme as ThemeName)
}

/**
 * 应用主题到 DOM
 */
function applyThemeToDOM(theme: ThemeName): void {
  // 移除所有主题类
  document.documentElement.removeAttribute('data-theme')

  // 添加新主题
  document.documentElement.setAttribute('data-theme', theme)
}

/**
 * 主题 Store
 */
export const useThemeStore = defineStore('theme', () => {
  // ========================================
  // State
  // ========================================
  const currentTheme = ref<ThemeName>(DEFAULT_THEME)
  const isTransitioning = ref(false)

  // ========================================
  // Getters (Computed)
  // ========================================
  const themeMetadata = computed(() => THEME_CONFIG[currentTheme.value])

  const isChineseRed = computed(() => currentTheme.value === 'chinese-red')
  const isTechBlue = computed(() => currentTheme.value === 'tech-blue')
  const isNatureGreen = computed(() => currentTheme.value === 'nature-green')

  const availableThemes = computed(() => Object.values(THEME_CONFIG))

  // ========================================
  // Actions
  // ========================================

  /**
   * 设置主题
   * @param theme 主题名称
   * @param persist 是否持久化到 localStorage（默认 true）
   */
  function setTheme(theme: ThemeName, persist: boolean = true): void {
    if (!isValidTheme(theme)) {
      console.error(`Invalid theme: ${theme}. Using default theme.`)
      theme = DEFAULT_THEME
    }

    currentTheme.value = theme
    applyThemeToDOM(theme)

    if (persist) {
      setStoredTheme(theme)
    }

    if (import.meta.env.DEV) {
      console.log(`[ThemeStore] Theme changed to: ${theme}`)
    }
  }

  /**
   * 切换到下一个主题（循环切换）
   */
  function cycleTheme(): void {
    const currentIndex = THEME_NAMES.indexOf(currentTheme.value)
    const nextIndex = (currentIndex + 1) % THEME_NAMES.length
    setTheme(THEME_NAMES[nextIndex])
  }

  /**
   * 从 localStorage 初始化主题
   */
  function initFromStorage(): void {
    const stored = getStoredTheme()
    if (stored) {
      setTheme(stored, false) // 不需要再次保存
      if (import.meta.env.DEV) {
        console.log(`[ThemeStore] Initialized from storage: ${stored}`)
      }
    } else {
      // 使用默认主题
      setTheme(DEFAULT_THEME)
      if (import.meta.env.DEV) {
        console.log(`[ThemeStore] Using default theme: ${DEFAULT_THEME}`)
      }
    }
  }

  /**
   * 重置为默认主题
   */
  function resetTheme(): void {
    setTheme(DEFAULT_THEME)
    removeStoredTheme()
    if (import.meta.env.DEV) {
      console.log(`[ThemeStore] Reset to default theme: ${DEFAULT_THEME}`)
    }
  }

  /**
   * 带过渡效果的主题切换
   * @param theme 主题名称
   */
  async function setThemeWithTransition(theme: ThemeName): Promise<void> {
    isTransitioning.value = true

    // 添加过渡类
    document.body.classList.add('theme-transitioning')

    // 短暂延迟以触发过渡动画
    await new Promise(resolve => setTimeout(resolve, 50))

    // 应用主题
    setTheme(theme)

    // 过渡动画完成后移除类
    await new Promise(resolve => setTimeout(resolve, 300))
    document.body.classList.remove('theme-transitioning')

    isTransitioning.value = false
  }

  /**
   * 同步主题到后端（需要用户已登录）
   * 注意：此方法将在后端 API 准备好后实现
   */
  async function syncToBackend(): Promise<void> {
    // TODO: 实现后端同步逻辑
    // const userStore = useUserStore()
    // if (userStore.isLoggedIn) {
    //   await updateUserPreferences({ theme: currentTheme.value })
    // }
    if (import.meta.env.DEV) {
      console.log('[ThemeStore] Backend sync not implemented yet')
    }
  }

  /**
   * 从后端加载用户主题偏好
   * 注意：此方法将在后端 API 准备好后实现
   */
  async function loadFromBackend(): Promise<void> {
    // TODO: 实现从后端加载主题
    // const userStore = useUserStore()
    // if (userStore.isLoggedIn) {
    //   const preferences = await getUserPreferences()
    //   if (preferences.theme) {
    //     setTheme(preferences.theme, false)
    //   }
    // }
    if (import.meta.env.DEV) {
      console.log('[ThemeStore] Backend load not implemented yet')
    }
  }

  // ========================================
  // Watchers
  // ========================================

  // 监听主题变化，可用于分析或日志记录
  watch(currentTheme, (newTheme, oldTheme) => {
    if (oldTheme !== newTheme) {
      if (import.meta.env.DEV) {
        console.log(`[ThemeStore] Theme changed: ${oldTheme} → ${newTheme}`)
      }

      // 触发自定义事件供外部监听
      window.dispatchEvent(new CustomEvent('theme-changed', {
        detail: { from: oldTheme, to: newTheme }
      }))
    }
  })

  // ========================================
  // Return
  // ========================================
  return {
    // State
    currentTheme,
    isTransitioning,

    // Getters
    themeMetadata,
    isChineseRed,
    isTechBlue,
    isNatureGreen,
    availableThemes,

    // Actions
    setTheme,
    cycleTheme,
    initFromStorage,
    resetTheme,
    setThemeWithTransition,
    syncToBackend,
    loadFromBackend
  }
})
