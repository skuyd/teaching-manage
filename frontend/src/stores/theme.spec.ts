/**
 * Theme Store 单元测试
 *
 * 测试范围：
 * - State 初始化
 * - Getters（计算属性）
 * - Actions（主题设置、循环、重置）
 * - localStorage 持久化
 * - DOM 应用
 * - 事件触发
 */

import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import {
  useThemeStore,
  type ThemeName,
  THEME_CONFIG,
  DEFAULT_THEME
} from './theme'

describe('Theme Store', () => {
  beforeEach(() => {
    // 每个测试前创建新的 Pinia 实例，确保 store 隔离
    setActivePinia(createPinia())

    // 重置 localStorage
    localStorage.clear()

    // 重置 document.documentElement
    document.documentElement.removeAttribute('data-theme')

    // 清除 console.log 和 console.error 的调用记录
    vi.spyOn(console, 'log').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ========================================
  // State 测试
  // ========================================

  describe('State', () => {
    it('should initialize with default theme (tech-blue)', () => {
      const store = useThemeStore()

      expect(store.currentTheme).toBe(DEFAULT_THEME)
      expect(store.currentTheme).toBe('tech-blue')
    })

    it('should initialize isTransitioning as false', () => {
      const store = useThemeStore()

      expect(store.isTransitioning).toBe(false)
    })

    it('should have correct DEFAULT_THEME constant', () => {
      expect(DEFAULT_THEME).toBe('tech-blue')
    })
  })

  // ========================================
  // Getters 测试
  // ========================================

  describe('Getters', () => {
    describe('themeMetadata', () => {
      it('should return metadata for current theme', () => {
        const store = useThemeStore()

        expect(store.themeMetadata).toEqual(THEME_CONFIG['tech-blue'])
        expect(store.themeMetadata.displayName).toBe('科技蓝')
        expect(store.themeMetadata.primaryColor).toBe('#3B82F6')
      })

      it('should update when theme changes', () => {
        const store = useThemeStore()

        store.setTheme('chinese-red')

        expect(store.themeMetadata).toEqual(THEME_CONFIG['chinese-red'])
        expect(store.themeMetadata.displayName).toBe('中国红')
      })
    })

    describe('theme check computed properties', () => {
      it('should return true for isChineseRed when theme is chinese-red', () => {
        const store = useThemeStore()

        store.setTheme('chinese-red')

        expect(store.isChineseRed).toBe(true)
        expect(store.isTechBlue).toBe(false)
        expect(store.isNatureGreen).toBe(false)
      })

      it('should return true for isTechBlue when theme is tech-blue', () => {
        const store = useThemeStore()

        store.setTheme('tech-blue')

        expect(store.isTechBlue).toBe(true)
        expect(store.isChineseRed).toBe(false)
        expect(store.isNatureGreen).toBe(false)
      })

      it('should return true for isNatureGreen when theme is nature-green', () => {
        const store = useThemeStore()

        store.setTheme('nature-green')

        expect(store.isNatureGreen).toBe(true)
        expect(store.isChineseRed).toBe(false)
        expect(store.isTechBlue).toBe(false)
      })
    })

    describe('availableThemes', () => {
      it('should return all 3 themes', () => {
        const store = useThemeStore()

        expect(store.availableThemes).toHaveLength(3)
      })

      it('should contain all theme metadata', () => {
        const store = useThemeStore()

        const themeNames = store.availableThemes.map(t => t.name)

        expect(themeNames).toContain('chinese-red')
        expect(themeNames).toContain('tech-blue')
        expect(themeNames).toContain('nature-green')
      })

      it('should have correct structure for each theme', () => {
        const store = useThemeStore()

        store.availableThemes.forEach(theme => {
          expect(theme).toHaveProperty('name')
          expect(theme).toHaveProperty('displayName')
          expect(theme).toHaveProperty('description')
          expect(theme).toHaveProperty('icon')
          expect(theme).toHaveProperty('primaryColor')
        })
      })
    })
  })

  // ========================================
  // Actions 测试
  // ========================================

  describe('Actions', () => {
    describe('setTheme', () => {
      it('should change currentTheme', () => {
        const store = useThemeStore()

        store.setTheme('chinese-red')

        expect(store.currentTheme).toBe('chinese-red')
      })

      it('should apply theme to DOM', () => {
        const store = useThemeStore()

        store.setTheme('nature-green')

        expect(document.documentElement.getAttribute('data-theme')).toBe('nature-green')
      })

      it('should persist to localStorage by default', () => {
        const store = useThemeStore()

        store.setTheme('chinese-red')

        expect(localStorage.setItem).toHaveBeenCalledWith('user-theme-preference', 'chinese-red')
      })

      it('should not persist when persist=false', () => {
        const store = useThemeStore()

        store.setTheme('chinese-red', false)

        expect(localStorage.setItem).not.toHaveBeenCalled()
      })

      it('should fall back to default theme for invalid theme', () => {
        const store = useThemeStore()

        // 使用 any 绕过 TypeScript 类型检查以测试边界情况
        store.setTheme('invalid-theme' as ThemeName)

        expect(store.currentTheme).toBe(DEFAULT_THEME)
        expect(console.error).toHaveBeenCalled()
      })

      it('should log theme change', () => {
        const store = useThemeStore()

        store.setTheme('chinese-red')

        expect(console.log).toHaveBeenCalledWith(
          expect.stringContaining('chinese-red')
        )
      })
    })

    describe('cycleTheme', () => {
      it('should cycle from tech-blue to chinese-red', () => {
        const store = useThemeStore()
        store.setTheme('tech-blue')

        store.cycleTheme()

        expect(store.currentTheme).toBe('chinese-red')
      })

      it('should cycle from chinese-red to nature-green', () => {
        const store = useThemeStore()
        store.setTheme('chinese-red')

        store.cycleTheme()

        expect(store.currentTheme).toBe('nature-green')
      })

      it('should cycle from nature-green to tech-blue', () => {
        const store = useThemeStore()
        store.setTheme('nature-green')

        store.cycleTheme()

        expect(store.currentTheme).toBe('tech-blue')
      })

      it('should complete full cycle', () => {
        const store = useThemeStore()
        store.setTheme('tech-blue')

        const initialTheme = store.currentTheme

        store.cycleTheme() // -> chinese-red
        store.cycleTheme() // -> nature-green
        store.cycleTheme() // -> tech-blue

        expect(store.currentTheme).toBe(initialTheme)
      })
    })

    describe('initFromStorage', () => {
      it('should load theme from localStorage', () => {
        localStorage.setItem('user-theme-preference', 'nature-green')
        const store = useThemeStore()

        store.initFromStorage()

        expect(store.currentTheme).toBe('nature-green')
      })

      it('should use default theme when localStorage is empty', () => {
        const store = useThemeStore()

        store.initFromStorage()

        expect(store.currentTheme).toBe(DEFAULT_THEME)
      })

      it('should ignore invalid theme in localStorage', () => {
        localStorage.setItem('user-theme-preference', 'invalid-theme')
        const store = useThemeStore()

        store.initFromStorage()

        expect(store.currentTheme).toBe(DEFAULT_THEME)
      })

      it('should apply theme to DOM on init', () => {
        localStorage.setItem('user-theme-preference', 'chinese-red')
        const store = useThemeStore()

        store.initFromStorage()

        expect(document.documentElement.getAttribute('data-theme')).toBe('chinese-red')
      })
    })

    describe('resetTheme', () => {
      it('should reset to default theme', () => {
        const store = useThemeStore()
        store.setTheme('nature-green')

        store.resetTheme()

        expect(store.currentTheme).toBe(DEFAULT_THEME)
      })

      it('should remove theme from localStorage', () => {
        const store = useThemeStore()
        store.setTheme('nature-green')

        store.resetTheme()

        expect(localStorage.removeItem).toHaveBeenCalledWith('user-theme-preference')
      })

      it('should apply default theme to DOM', () => {
        const store = useThemeStore()
        store.setTheme('nature-green')

        store.resetTheme()

        expect(document.documentElement.getAttribute('data-theme')).toBe(DEFAULT_THEME)
      })
    })

    describe('setThemeWithTransition', () => {
      it('should set isTransitioning to true during transition', async () => {
        const store = useThemeStore()

        const promise = store.setThemeWithTransition('chinese-red')

        expect(store.isTransitioning).toBe(true)

        await promise

        expect(store.isTransitioning).toBe(false)
      })

      it('should add theme-transitioning class to body', async () => {
        const store = useThemeStore()

        const promise = store.setThemeWithTransition('chinese-red')

        // 由于是异步，我们需要等待一小段时间
        await new Promise(resolve => setTimeout(resolve, 60))
        expect(document.body.classList.contains('theme-transitioning')).toBe(true)

        await promise
      })

      it('should remove theme-transitioning class after transition', async () => {
        const store = useThemeStore()

        await store.setThemeWithTransition('chinese-red')

        // 过渡完成后类应该被移除
        expect(document.body.classList.contains('theme-transitioning')).toBe(false)
      })

      it('should change theme after transition', async () => {
        const store = useThemeStore()

        await store.setThemeWithTransition('nature-green')

        expect(store.currentTheme).toBe('nature-green')
      })
    })

    describe('syncToBackend', () => {
      it('should log not implemented message', async () => {
        const store = useThemeStore()

        await store.syncToBackend()

        expect(console.log).toHaveBeenCalledWith(
          expect.stringContaining('not implemented')
        )
      })
    })

    describe('loadFromBackend', () => {
      it('should log not implemented message', async () => {
        const store = useThemeStore()

        await store.loadFromBackend()

        expect(console.log).toHaveBeenCalledWith(
          expect.stringContaining('not implemented')
        )
      })
    })
  })

  // ========================================
  // 事件触发测试
  // ========================================

  describe('Event Dispatching', () => {
    it('should dispatch theme-changed event when theme changes', async () => {
      const store = useThemeStore()
      const eventHandler = vi.fn()
      window.addEventListener('theme-changed', eventHandler)

      store.setTheme('chinese-red')

      // 等待 Vue 响应式更新
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(eventHandler).toHaveBeenCalled()
    })

    it('should include from and to in event detail', async () => {
      const store = useThemeStore()
      let eventDetail: { from: ThemeName; to: ThemeName } | null = null
      window.addEventListener('theme-changed', (e: Event) => {
        eventDetail = (e as CustomEvent).detail
      })

      store.setTheme('chinese-red')

      // 等待 Vue 响应式更新
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(eventDetail).not.toBeNull()
      expect(eventDetail!.from).toBe('tech-blue')
      expect(eventDetail!.to).toBe('chinese-red')
    })

    it('should not dispatch event when setting same theme', async () => {
      const store = useThemeStore()
      const eventHandler = vi.fn()

      // 先设置一次主题以触发初始事件
      store.setTheme('chinese-red')
      await new Promise(resolve => setTimeout(resolve, 0))

      // 添加监听器
      window.addEventListener('theme-changed', eventHandler)

      // 设置相同主题
      store.setTheme('chinese-red')
      await new Promise(resolve => setTimeout(resolve, 0))

      // 由于主题没有变化，不应触发事件
      expect(eventHandler).not.toHaveBeenCalled()
    })
  })

  // ========================================
  // THEME_CONFIG 测试
  // ========================================

  describe('THEME_CONFIG', () => {
    it('should have 3 themes', () => {
      expect(Object.keys(THEME_CONFIG)).toHaveLength(3)
    })

    it('should have chinese-red with correct properties', () => {
      const theme = THEME_CONFIG['chinese-red']

      expect(theme.name).toBe('chinese-red')
      expect(theme.displayName).toBe('中国红')
      expect(theme.primaryColor).toBe('#C41E3A')
      expect(theme.icon).toBeDefined()
      expect(theme.description).toBeDefined()
    })

    it('should have tech-blue with correct properties', () => {
      const theme = THEME_CONFIG['tech-blue']

      expect(theme.name).toBe('tech-blue')
      expect(theme.displayName).toBe('科技蓝')
      expect(theme.primaryColor).toBe('#3B82F6')
      expect(theme.icon).toBeDefined()
      expect(theme.description).toBeDefined()
    })

    it('should have nature-green with correct properties', () => {
      const theme = THEME_CONFIG['nature-green']

      expect(theme.name).toBe('nature-green')
      expect(theme.displayName).toBe('自然绿')
      expect(theme.primaryColor).toBe('#10B981')
      expect(theme.icon).toBeDefined()
      expect(theme.description).toBeDefined()
    })
  })

  // ========================================
  // 边界情况测试
  // ========================================

  describe('Edge Cases', () => {
    it('should handle rapid theme changes', async () => {
      const store = useThemeStore()

      store.setTheme('chinese-red')
      store.setTheme('nature-green')
      store.setTheme('tech-blue')

      expect(store.currentTheme).toBe('tech-blue')
    })

    it('should handle null in localStorage gracefully', () => {
      // 模拟 localStorage.getItem 返回 null
      vi.spyOn(localStorage, 'getItem').mockReturnValue(null)

      const store = useThemeStore()
      store.initFromStorage()

      expect(store.currentTheme).toBe(DEFAULT_THEME)
    })

    it('should handle empty string in localStorage gracefully', () => {
      localStorage.setItem('user-theme-preference', '')

      const store = useThemeStore()
      store.initFromStorage()

      expect(store.currentTheme).toBe(DEFAULT_THEME)
    })

    it('should handle concurrent transitions gracefully', async () => {
      const store = useThemeStore()

      // 启动多个过渡
      const promises = [
        store.setThemeWithTransition('chinese-red'),
        store.setThemeWithTransition('nature-green')
      ]

      await Promise.all(promises)

      // 最终状态应该是最后一个设置的主题
      expect(['chinese-red', 'nature-green']).toContain(store.currentTheme)
    })
  })
})
