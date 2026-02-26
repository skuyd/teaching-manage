import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useThemeStore, THEME_NAMES, type ThemeName, type ThemeMetadata } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { getUserPreferences, updateUserPreferences } from '@/api/user'

/**
 * 主题变化事件回调类型
 */
export type ThemeChangeCallback = (newTheme: ThemeName, oldTheme: ThemeName) => void

/**
 * 主题 Composable
 *
 * 提供主题管理的便捷方法和响应式状态
 *
 * @example
 * ```ts
 * const { currentTheme, setTheme, themeMetadata } = useTheme()
 *
 * // 切换主题
 * setTheme('sky-blue')
 *
 * // 监听主题变化
 * onThemeChange((newTheme, oldTheme) => {
 *   console.log(`Theme changed from ${oldTheme} to ${newTheme}`)
 * })
 * ```
 */
export function useTheme() {
  const themeStore = useThemeStore()
  const userStore = useUserStore()

  // ========================================
  // Reactive State
  // ========================================

  /**
   * 是否正在同步到后端
   */
  const isSyncing = ref(false)

  /**
   * 同步错误消息
   */
  const syncError = ref<string | null>(null)

  // ========================================
  // Computed Properties
  // ========================================

  /**
   * 当前主题名称
   */
  const currentTheme = computed(() => themeStore.currentTheme)

  /**
   * 当前主题元数据
   */
  const themeMetadata = computed(() => themeStore.themeMetadata)

  /**
   * 可用主题列表
   */
  const availableThemes = computed(() => themeStore.availableThemes)

  /**
   * 是否正在过渡动画中
   */
  const isTransitioning = computed(() => themeStore.isTransitioning)

  /**
   * 当前是否为天蓝色主题
   */
  const isSkyBlue = computed(() => themeStore.isSkyBlue)

  /**
   * 当前是否为科技蓝主题
   */
  const isTechBlue = computed(() => themeStore.isTechBlue)

  /**
   * 当前是否为自然绿主题
   */
  const isNatureGreen = computed(() => themeStore.isNatureGreen)

  /**
   * 用户是否已登录
   */
  const isUserLoggedIn = computed(() => userStore.isLoggedIn)

  // ========================================
  // Theme Management Methods
  // ========================================

  /**
   * 设置主题
   *
   * @param theme 主题名称
   * @param options 选项
   * @param options.persist 是否持久化到 localStorage（默认 true）
   * @param options.sync 是否同步到后端（默认 false，需要用户已登录）
   * @param options.transition 是否启用过渡动画（默认 false）
   */
  async function setTheme(
    theme: ThemeName,
    options: {
      persist?: boolean
      sync?: boolean
      transition?: boolean
    } = {}
  ): Promise<void> {
    const { persist = true, sync = false, transition = false } = options

    try {
      // 应用主题
      if (transition) {
        await themeStore.setThemeWithTransition(theme)
      } else {
        themeStore.setTheme(theme, persist)
      }

      // 同步到后端
      if (sync && isUserLoggedIn.value) {
        await syncThemeToBackend()
      }
    } catch (error) {
      console.error('[useTheme] Failed to set theme:', error)
      throw error
    }
  }

  /**
   * 循环切换主题
   *
   * @param options 选项
   */
  async function cycleTheme(
    options: {
      sync?: boolean
      transition?: boolean
    } = {}
  ): Promise<void> {
    const { sync = false, transition = false } = options

    try {
      if (transition) {
        // 获取下一个主题
        const currentIndex = THEME_NAMES.indexOf(currentTheme.value)
        const nextTheme = THEME_NAMES[(currentIndex + 1) % THEME_NAMES.length]
        await themeStore.setThemeWithTransition(nextTheme)
      } else {
        themeStore.cycleTheme()
      }

      // 同步到后端
      if (sync && isUserLoggedIn.value) {
        await syncThemeToBackend()
      }
    } catch (error) {
      console.error('[useTheme] Failed to cycle theme:', error)
      throw error
    }
  }

  /**
   * 重置为默认主题
   */
  function resetTheme(): void {
    themeStore.resetTheme()
  }

  // ========================================
  // Persistence Methods
  // ========================================

  /**
   * 从 localStorage 初始化主题
   */
  function initFromStorage(): void {
    themeStore.initFromStorage()
  }

  /**
   * 同步主题到后端
   */
  async function syncThemeToBackend(): Promise<void> {
    if (!isUserLoggedIn.value) {
      console.warn('[useTheme] User not logged in, skipping backend sync')
      return
    }

    isSyncing.value = true
    syncError.value = null

    try {
      await updateUserPreferences({ theme: currentTheme.value })
      if (import.meta.env.DEV) {
        console.log('[useTheme] Theme synced to backend successfully')
      }
    } catch (error) {
      console.error('[useTheme] Failed to sync theme to backend:', error)
      syncError.value = error instanceof Error ? error.message : 'Unknown error'
      throw error
    } finally {
      isSyncing.value = false
    }
  }

  /**
   * 从后端加载主题偏好
   * 如果后端没有返回有效主题，降级到 localStorage
   */
  async function loadThemeFromBackend(): Promise<void> {
    if (!isUserLoggedIn.value) {
      console.warn('[useTheme] User not logged in, skipping backend load')
      return
    }

    isSyncing.value = true
    syncError.value = null

    try {
      const response = await getUserPreferences()
      if (response.data && response.data.theme) {
        themeStore.setTheme(response.data.theme as ThemeName, true)
        if (import.meta.env.DEV) {
          console.log('[useTheme] Theme loaded from backend successfully')
        }
        return
      }
      // 后端没有返回主题，降级到 localStorage
      if (import.meta.env.DEV) {
        console.log('[useTheme] No theme from backend, falling back to localStorage')
      }
      initFromStorage()
    } catch (error) {
      console.error('[useTheme] Failed to load theme from backend:', error)
      syncError.value = error instanceof Error ? error.message : 'Unknown error'
      // 降级到 localStorage
      initFromStorage()
    } finally {
      isSyncing.value = false
    }
  }

  // ========================================
  // Event Handling
  // ========================================

  /**
   * 监听主题变化事件
   *
   * @param callback 回调函数
   * @returns 清理函数
   */
  function onThemeChange(callback: ThemeChangeCallback): () => void {
    const handler = (event: Event) => {
      const customEvent = event as CustomEvent<{ from: ThemeName; to: ThemeName }>
      callback(customEvent.detail.to, customEvent.detail.from)
    }

    window.addEventListener('theme-changed', handler)

    // 返回清理函数
    return () => {
      window.removeEventListener('theme-changed', handler)
    }
  }

  // ========================================
  // Utility Methods
  // ========================================

  /**
   * 获取指定主题的元数据
   *
   * @param theme 主题名称
   * @returns 主题元数据
   */
  function getThemeMetadata(theme: ThemeName): ThemeMetadata | undefined {
    return availableThemes.value.find(t => t.name === theme)
  }

  /**
   * 判断指定主题是否为当前主题
   *
   * @param theme 主题名称
   * @returns 是否为当前主题
   */
  function isCurrentTheme(theme: ThemeName): boolean {
    return currentTheme.value === theme
  }

  /**
   * 获取主题的主色调
   *
   * @param theme 主题名称（可选，默认为当前主题）
   * @returns 主色调
   */
  function getPrimaryColor(theme?: ThemeName): string {
    const targetTheme = theme || currentTheme.value
    const metadata = getThemeMetadata(targetTheme)
    return metadata?.primaryColor || '#409EFF'
  }

  // ========================================
  // Lifecycle Hooks (Auto-init)
  // ========================================

  /**
   * 自动初始化主题（组件挂载时）
   *
   * 优先级：后端 > localStorage > 默认
   */
  async function autoInitTheme(): Promise<void> {
    try {
      if (isUserLoggedIn.value) {
        // 尝试从后端加载
        await loadThemeFromBackend()
      } else {
        // 从 localStorage 加载
        initFromStorage()
      }
    } catch (error) {
      console.error('[useTheme] Auto-init failed, using default theme:', error)
      // 降级到默认主题
      resetTheme()
    }
  }

  // ========================================
  // Return
  // ========================================
  return {
    // State
    currentTheme,
    themeMetadata,
    availableThemes,
    isTransitioning,
    isSyncing,
    syncError,

    // Theme checks
    isSkyBlue,
    isTechBlue,
    isNatureGreen,

    // Actions
    setTheme,
    cycleTheme,
    resetTheme,

    // Persistence
    initFromStorage,
    syncThemeToBackend,
    loadThemeFromBackend,
    autoInitTheme,

    // Events
    onThemeChange,

    // Utilities
    getThemeMetadata,
    isCurrentTheme,
    getPrimaryColor
  }
}

/**
 * 主题自动初始化 Composable
 *
 * 在组件挂载时自动初始化主题，卸载时无需清理
 *
 * @example
 * ```ts
 * // 在 App.vue 或 Layout 组件中使用
 * useThemeAutoInit()
 * ```
 */
export function useThemeAutoInit() {
  const { autoInitTheme } = useTheme()

  onMounted(async () => {
    await autoInitTheme()
  })
}

/**
 * 主题变化监听 Composable
 *
 * 自动管理事件监听器的生命周期
 *
 * @param callback 主题变化回调函数
 *
 * @example
 * ```ts
 * useThemeWatcher((newTheme, oldTheme) => {
 *   console.log(`Theme changed: ${oldTheme} → ${newTheme}`)
 * })
 * ```
 */
export function useThemeWatcher(callback: ThemeChangeCallback) {
  const { onThemeChange } = useTheme()
  let cleanup: (() => void) | null = null

  onMounted(() => {
    cleanup = onThemeChange(callback)
  })

  onUnmounted(() => {
    if (cleanup) {
      cleanup()
      cleanup = null
    }
  })
}
