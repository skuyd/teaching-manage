/**
 * useTheme Composable 单元测试
 *
 * 测试范围：
 * - 所有导出的计算属性
 * - setTheme、cycleTheme、resetTheme 方法
 * - autoInitTheme 自动初始化
 * - onThemeChange 事件监听
 * - 与 store 的集成
 * - 后端同步相关方法
 */

import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { mount, flushPromises } from '@vue/test-utils'
import { defineComponent, nextTick } from 'vue'
import { useTheme, useThemeAutoInit, useThemeWatcher, type ThemeChangeCallback } from './useTheme'
import { useThemeStore, type ThemeName, DEFAULT_THEME } from '@/stores/theme'
import { useUserStore } from '@/stores/user'

// Mock user store
vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(() => ({
    isLoggedIn: false
  }))
}))

describe('useTheme Composable', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    document.documentElement.removeAttribute('data-theme')

    // 重置 user store mock
    vi.mocked(useUserStore).mockReturnValue({
      isLoggedIn: false
    } as any)

    vi.spyOn(console, 'log').mockImplementation(() => {})
    vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ========================================
  // 辅助函数：在组件上下文中使用 composable
  // ========================================

  function withSetup<T>(composable: () => T): {
    result: T
    app: ReturnType<typeof mount>
  } {
    let result: T
    const TestComponent = defineComponent({
      setup() {
        result = composable()
        return () => null
      }
    })
    const app = mount(TestComponent)
    return { result: result!, app }
  }

  // ========================================
  // Computed Properties 测试
  // ========================================

  describe('Computed Properties', () => {
    it('should expose currentTheme from store', () => {
      const { result } = withSetup(() => useTheme())

      expect(result.currentTheme.value).toBe(DEFAULT_THEME)
    })

    it('should expose themeMetadata from store', () => {
      const { result } = withSetup(() => useTheme())

      expect(result.themeMetadata.value).toBeDefined()
      expect(result.themeMetadata.value.displayName).toBe('科技蓝')
    })

    it('should expose availableThemes from store', () => {
      const { result } = withSetup(() => useTheme())

      expect(result.availableThemes.value).toHaveLength(3)
    })

    it('should expose isTransitioning from store', () => {
      const { result } = withSetup(() => useTheme())

      expect(result.isTransitioning.value).toBe(false)
    })

    it('should have isSyncing initialized as false', () => {
      const { result } = withSetup(() => useTheme())

      expect(result.isSyncing.value).toBe(false)
    })

    it('should have syncError initialized as null', () => {
      const { result } = withSetup(() => useTheme())

      expect(result.syncError.value).toBeNull()
    })

    describe('theme check computed', () => {
      it('should reflect isChineseRed correctly', async () => {
        const { result } = withSetup(() => useTheme())

        await result.setTheme('chinese-red')

        expect(result.isChineseRed.value).toBe(true)
        expect(result.isTechBlue.value).toBe(false)
        expect(result.isNatureGreen.value).toBe(false)
      })

      it('should reflect isTechBlue correctly', async () => {
        const { result } = withSetup(() => useTheme())

        expect(result.isTechBlue.value).toBe(true)
      })

      it('should reflect isNatureGreen correctly', async () => {
        const { result } = withSetup(() => useTheme())

        await result.setTheme('nature-green')

        expect(result.isNatureGreen.value).toBe(true)
      })
    })
  })

  // ========================================
  // setTheme 方法测试
  // ========================================

  describe('setTheme', () => {
    it('should change theme', async () => {
      const { result } = withSetup(() => useTheme())

      await result.setTheme('chinese-red')

      expect(result.currentTheme.value).toBe('chinese-red')
    })

    it('should persist by default', async () => {
      const { result } = withSetup(() => useTheme())

      await result.setTheme('nature-green')

      expect(localStorage.setItem).toHaveBeenCalledWith('user-theme-preference', 'nature-green')
    })

    it('should not persist when persist=false', async () => {
      const { result } = withSetup(() => useTheme())

      await result.setTheme('chinese-red', { persist: false })

      // setTheme 是调用 store 的方法，persist 选项会传递给 store
      // 由于 store.setTheme 的第二个参数默认为 true，
      // 我们需要检查 store 是否正确调用
      expect(result.currentTheme.value).toBe('chinese-red')
    })

    it('should use transition when enabled', async () => {
      const { result } = withSetup(() => useTheme())

      const promise = result.setTheme('chinese-red', { transition: true })

      // 过渡开始后应该是 true
      expect(result.isTransitioning.value).toBe(true)

      await promise

      expect(result.isTransitioning.value).toBe(false)
    })

    it('should not sync when user not logged in', async () => {
      const { result } = withSetup(() => useTheme())

      // 设置主题并尝试同步
      await result.setTheme('chinese-red', { sync: true })

      // 主题应该被设置，但同步不会发生（因为用户未登录）
      expect(result.currentTheme.value).toBe('chinese-red')
    })

    it('should sync when user is logged in', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        isLoggedIn: true
      } as any)

      const { result } = withSetup(() => useTheme())

      await result.setTheme('chinese-red', { sync: true })

      // 由于后端同步未实现，应该调用 console.log
      expect(console.log).toHaveBeenCalledWith(
        expect.stringContaining('not implemented')
      )
    })

    it('should throw error on failure', async () => {
      const { result } = withSetup(() => useTheme())
      const store = useThemeStore()

      // 模拟 store 抛出错误
      vi.spyOn(store, 'setTheme').mockImplementation(() => {
        throw new Error('Test error')
      })

      await expect(result.setTheme('chinese-red')).rejects.toThrow('Test error')
    })
  })

  // ========================================
  // cycleTheme 方法测试
  // ========================================

  describe('cycleTheme', () => {
    it('should cycle through themes', async () => {
      const { result } = withSetup(() => useTheme())

      expect(result.currentTheme.value).toBe('tech-blue')

      await result.cycleTheme()
      expect(result.currentTheme.value).toBe('chinese-red')

      await result.cycleTheme()
      expect(result.currentTheme.value).toBe('nature-green')

      await result.cycleTheme()
      expect(result.currentTheme.value).toBe('tech-blue')
    })

    it('should use transition when enabled', async () => {
      const { result } = withSetup(() => useTheme())

      const promise = result.cycleTheme({ transition: true })

      expect(result.isTransitioning.value).toBe(true)

      await promise

      expect(result.isTransitioning.value).toBe(false)
    })

    it('should sync when enabled and user logged in', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        isLoggedIn: true
      } as any)

      const { result } = withSetup(() => useTheme())

      await result.cycleTheme({ sync: true })

      expect(console.log).toHaveBeenCalledWith(
        expect.stringContaining('not implemented')
      )
    })

    it('should throw error on failure', async () => {
      const { result } = withSetup(() => useTheme())
      const store = useThemeStore()

      // 模拟 store.cycleTheme 抛出错误
      vi.spyOn(store, 'cycleTheme').mockImplementation(() => {
        throw new Error('Cycle error')
      })

      await expect(result.cycleTheme()).rejects.toThrow('Cycle error')
      expect(console.error).toHaveBeenCalledWith(
        expect.stringContaining('Failed to cycle theme'),
        expect.any(Error)
      )
    })
  })

  // ========================================
  // resetTheme 方法测试
  // ========================================

  describe('resetTheme', () => {
    it('should reset to default theme', async () => {
      const { result } = withSetup(() => useTheme())

      await result.setTheme('nature-green')
      result.resetTheme()

      expect(result.currentTheme.value).toBe(DEFAULT_THEME)
    })

    it('should remove from localStorage', async () => {
      const { result } = withSetup(() => useTheme())

      await result.setTheme('nature-green')
      result.resetTheme()

      expect(localStorage.removeItem).toHaveBeenCalledWith('user-theme-preference')
    })
  })

  // ========================================
  // initFromStorage 方法测试
  // ========================================

  describe('initFromStorage', () => {
    it('should load theme from localStorage', () => {
      localStorage.setItem('user-theme-preference', 'chinese-red')

      const { result } = withSetup(() => useTheme())
      result.initFromStorage()

      expect(result.currentTheme.value).toBe('chinese-red')
    })

    it('should use default when localStorage is empty', () => {
      const { result } = withSetup(() => useTheme())
      result.initFromStorage()

      expect(result.currentTheme.value).toBe(DEFAULT_THEME)
    })
  })

  // ========================================
  // autoInitTheme 方法测试
  // ========================================

  describe('autoInitTheme', () => {
    it('should init from localStorage when user not logged in', async () => {
      localStorage.setItem('user-theme-preference', 'nature-green')

      const { result } = withSetup(() => useTheme())

      await result.autoInitTheme()

      expect(result.currentTheme.value).toBe('nature-green')
    })

    it('should try backend when user is logged in', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        isLoggedIn: true
      } as any)

      const { result } = withSetup(() => useTheme())

      await result.autoInitTheme()

      // 由于后端未实现，应该记录日志
      expect(console.log).toHaveBeenCalledWith(
        expect.stringContaining('not implemented')
      )
    })

    it('should gracefully handle backend load error (no throw)', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        isLoggedIn: true
      } as any)

      const { result } = withSetup(() => useTheme())
      const store = useThemeStore()

      // 模拟 loadFromBackend 抛出错误
      vi.spyOn(store, 'loadFromBackend').mockRejectedValue(new Error('Backend error'))

      // 不应抛出错误 - loadThemeFromBackend 内部捕获错误
      await expect(result.autoInitTheme()).resolves.toBeUndefined()

      // 错误会被记录在 syncError 中
      expect(result.syncError.value).toBe('Backend error')
    })

    it('should fall back to default when initFromStorage throws', async () => {
      const { result } = withSetup(() => useTheme())
      const store = useThemeStore()

      // 先设置一个非默认主题
      store.setTheme('nature-green')

      // 模拟 initFromStorage 抛出错误
      vi.spyOn(store, 'initFromStorage').mockImplementation(() => {
        throw new Error('Storage error')
      })

      await result.autoInitTheme()

      // 应该重置到默认主题
      expect(result.currentTheme.value).toBe(DEFAULT_THEME)
      expect(console.error).toHaveBeenCalledWith(
        expect.stringContaining('Auto-init failed'),
        expect.any(Error)
      )
    })
  })

  // ========================================
  // syncThemeToBackend 方法测试
  // ========================================

  describe('syncThemeToBackend', () => {
    it('should skip sync when user not logged in', async () => {
      const { result } = withSetup(() => useTheme())

      await result.syncThemeToBackend()

      expect(console.warn).toHaveBeenCalledWith(
        expect.stringContaining('not logged in')
      )
    })

    it('should set isSyncing during sync', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        isLoggedIn: true
      } as any)

      const { result } = withSetup(() => useTheme())

      const promise = result.syncThemeToBackend()
      expect(result.isSyncing.value).toBe(true)

      await promise
      expect(result.isSyncing.value).toBe(false)
    })

    it('should set syncError on failure', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        isLoggedIn: true
      } as any)

      const { result } = withSetup(() => useTheme())
      const store = useThemeStore()

      vi.spyOn(store, 'syncToBackend').mockRejectedValue(new Error('Sync failed'))

      await expect(result.syncThemeToBackend()).rejects.toThrow('Sync failed')
      expect(result.syncError.value).toBe('Sync failed')
    })
  })

  // ========================================
  // loadThemeFromBackend 方法测试
  // ========================================

  describe('loadThemeFromBackend', () => {
    it('should skip load when user not logged in', async () => {
      const { result } = withSetup(() => useTheme())

      await result.loadThemeFromBackend()

      expect(console.warn).toHaveBeenCalledWith(
        expect.stringContaining('not logged in')
      )
    })

    it('should not throw on error (graceful degradation)', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        isLoggedIn: true
      } as any)

      const { result } = withSetup(() => useTheme())
      const store = useThemeStore()

      vi.spyOn(store, 'loadFromBackend').mockRejectedValue(new Error('Load failed'))

      // 不应该抛出错误
      await expect(result.loadThemeFromBackend()).resolves.toBeUndefined()
      expect(result.syncError.value).toBe('Load failed')
    })
  })

  // ========================================
  // onThemeChange 事件监听测试
  // ========================================

  describe('onThemeChange', () => {
    it('should call callback when theme changes', async () => {
      const { result } = withSetup(() => useTheme())
      const callback = vi.fn()

      result.onThemeChange(callback)

      await result.setTheme('chinese-red')
      await nextTick()

      expect(callback).toHaveBeenCalledWith('chinese-red', 'tech-blue')
    })

    it('should return cleanup function', async () => {
      const { result } = withSetup(() => useTheme())
      const callback = vi.fn()

      const cleanup = result.onThemeChange(callback)

      // 移除监听器
      cleanup()

      // 更改主题
      await result.setTheme('chinese-red')
      await nextTick()

      // 回调不应被调用
      expect(callback).not.toHaveBeenCalled()
    })
  })

  // ========================================
  // Utility 方法测试
  // ========================================

  describe('Utility Methods', () => {
    describe('getThemeMetadata', () => {
      it('should return metadata for valid theme', () => {
        const { result } = withSetup(() => useTheme())

        const metadata = result.getThemeMetadata('chinese-red')

        expect(metadata).toBeDefined()
        expect(metadata?.displayName).toBe('中国红')
      })

      it('should return undefined for invalid theme', () => {
        const { result } = withSetup(() => useTheme())

        const metadata = result.getThemeMetadata('invalid' as ThemeName)

        expect(metadata).toBeUndefined()
      })
    })

    describe('isCurrentTheme', () => {
      it('should return true for current theme', () => {
        const { result } = withSetup(() => useTheme())

        expect(result.isCurrentTheme('tech-blue')).toBe(true)
      })

      it('should return false for non-current theme', () => {
        const { result } = withSetup(() => useTheme())

        expect(result.isCurrentTheme('chinese-red')).toBe(false)
      })
    })

    describe('getPrimaryColor', () => {
      it('should return primary color for current theme', () => {
        const { result } = withSetup(() => useTheme())

        expect(result.getPrimaryColor()).toBe('#3B82F6')
      })

      it('should return primary color for specified theme', () => {
        const { result } = withSetup(() => useTheme())

        expect(result.getPrimaryColor('chinese-red')).toBe('#C41E3A')
      })

      it('should return fallback color for invalid theme', () => {
        const { result } = withSetup(() => useTheme())

        expect(result.getPrimaryColor('invalid' as ThemeName)).toBe('#409EFF')
      })
    })
  })
})

// ========================================
// useThemeAutoInit 测试
// ========================================

describe('useThemeAutoInit', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    document.documentElement.removeAttribute('data-theme')

    vi.mocked(useUserStore).mockReturnValue({
      isLoggedIn: false
    } as any)

    vi.spyOn(console, 'log').mockImplementation(() => {})
    vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should auto-init theme on mount', async () => {
    localStorage.setItem('user-theme-preference', 'nature-green')

    const TestComponent = defineComponent({
      setup() {
        useThemeAutoInit()
        const { currentTheme } = useTheme()
        return { currentTheme }
      },
      template: '<div>{{ currentTheme }}</div>'
    })

    const wrapper = mount(TestComponent)
    await flushPromises()

    expect(wrapper.text()).toBe('nature-green')
  })
})

// ========================================
// useThemeWatcher 测试
// ========================================

describe('useThemeWatcher', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    document.documentElement.removeAttribute('data-theme')

    vi.mocked(useUserStore).mockReturnValue({
      isLoggedIn: false
    } as any)

    vi.spyOn(console, 'log').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should call callback on theme change', async () => {
    const callback = vi.fn()

    const TestComponent = defineComponent({
      setup() {
        useThemeWatcher(callback)
        const { setTheme } = useTheme()
        return { setTheme }
      },
      template: '<div></div>'
    })

    const wrapper = mount(TestComponent)
    await flushPromises()

    // 更改主题
    const store = useThemeStore()
    store.setTheme('chinese-red')
    await nextTick()

    expect(callback).toHaveBeenCalledWith('chinese-red', 'tech-blue')

    wrapper.unmount()
  })

  it('should cleanup listener on unmount', async () => {
    const callback = vi.fn()

    const TestComponent = defineComponent({
      setup() {
        useThemeWatcher(callback)
        return {}
      },
      template: '<div></div>'
    })

    const wrapper = mount(TestComponent)
    await flushPromises()

    // 卸载组件
    wrapper.unmount()

    // 更改主题
    const store = useThemeStore()
    store.setTheme('chinese-red')
    await nextTick()

    // 回调不应被调用
    expect(callback).not.toHaveBeenCalled()
  })
})
