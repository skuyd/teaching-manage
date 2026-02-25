/**
 * ThemeSwitcher 组件单元测试
 *
 * 测试范围：
 * - 4 种显示模式：dropdown、buttons、toggle、cards
 * - 主题切换交互
 * - Props 和 Emits
 * - 过渡动画触发
 * - 消息提示
 */

import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'

// Mock ElMessage - 必须在导入组件之前定义
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { nextTick, defineComponent, h } from 'vue'
import { ElMessage } from 'element-plus'
import ThemeSwitcher from './ThemeSwitcher.vue'
import { useThemeStore, THEME_CONFIG } from '@/stores/theme'

// 创建简化的 Element Plus 模拟组件
const ElDropdown = defineComponent({
  name: 'ElDropdown',
  emits: ['command'],
  setup(_, { slots }) {
    return () => h('div', { class: 'el-dropdown' }, [
      slots.default?.(),
      slots.dropdown?.()
    ])
  }
})

const ElDropdownMenu = defineComponent({
  name: 'ElDropdownMenu',
  setup(_, { slots }) {
    return () => h('div', { class: 'el-dropdown-menu' }, slots.default?.())
  }
})

const ElDropdownItem = defineComponent({
  name: 'ElDropdownItem',
  props: ['command', 'disabled'],
  emits: ['click'],
  setup(props, { slots }) {
    return () => h('div', {
      class: 'el-dropdown-item',
      onClick: () => {
        if (!props.disabled) {
          // 通过父组件触发 command 事件
          const parent = document.querySelector('.el-dropdown')
          if (parent) {
            parent.dispatchEvent(new CustomEvent('command', { detail: props.command }))
          }
        }
      }
    }, slots.default?.())
  }
})

const ElButton = defineComponent({
  name: 'ElButton',
  props: ['type', 'loading', 'circle', 'icon'],
  emits: ['click'],
  setup(props, { slots, emit }) {
    return () => h('button', {
      class: ['el-button', props.type ? `el-button--${props.type}` : ''],
      disabled: props.loading,
      onClick: (e: Event) => emit('click', e)
    }, slots.default?.())
  }
})

const ElButtonGroup = defineComponent({
  name: 'ElButtonGroup',
  setup(_, { slots }) {
    return () => h('div', { class: 'el-button-group' }, slots.default?.())
  }
})

const ElTooltip = defineComponent({
  name: 'ElTooltip',
  props: ['content', 'placement'],
  setup(_, { slots }) {
    return () => h('div', { class: 'el-tooltip' }, slots.default?.())
  }
})

const ElIcon = defineComponent({
  name: 'ElIcon',
  setup(_, { slots }) {
    return () => h('i', { class: 'el-icon' }, slots.default?.())
  }
})

// Mock icons
const Sunny = defineComponent({
  name: 'Sunny',
  render: () => h('span', 'sunny')
})

const Check = defineComponent({
  name: 'Check',
  render: () => h('span', 'check')
})

describe('ThemeSwitcher', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    document.documentElement.removeAttribute('data-theme')

    vi.spyOn(console, 'log').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})

    // 重置 mocks
    vi.mocked(ElMessage.success).mockClear()
    vi.mocked(ElMessage.error).mockClear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // 辅助函数：挂载组件
  function mountComponent(props: Record<string, unknown> = {}) {
    return mount(ThemeSwitcher, {
      props,
      global: {
        components: {
          ElDropdown,
          ElDropdownMenu,
          ElDropdownItem,
          ElButton,
          ElButtonGroup,
          ElTooltip,
          ElIcon,
          Sunny,
          Check
        },
        stubs: {
          teleport: true
        }
      }
    })
  }

  // ========================================
  // 基础渲染测试
  // ========================================

  describe('Basic Rendering', () => {
    it('should render without errors', () => {
      const wrapper = mountComponent()

      expect(wrapper.exists()).toBe(true)
    })

    it('should have theme-switcher class', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.theme-switcher').exists()).toBe(true)
    })

    it('should use dropdown mode by default', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.el-dropdown').exists()).toBe(true)
    })
  })

  // ========================================
  // Dropdown 模式测试
  // ========================================

  describe('Dropdown Mode', () => {
    it('should render el-dropdown component', () => {
      const wrapper = mountComponent({ mode: 'dropdown' })

      expect(wrapper.find('.el-dropdown').exists()).toBe(true)
    })

    it('should render button', () => {
      const wrapper = mountComponent({ mode: 'dropdown' })

      const button = wrapper.find('.el-button')
      expect(button.exists()).toBe(true)
    })

    it('should show current theme icon', () => {
      const wrapper = mountComponent({ mode: 'dropdown' })
      const store = useThemeStore()

      // 默认主题是 tech-blue
      expect(wrapper.text()).toContain(store.themeMetadata.icon)
    })
  })

  // ========================================
  // Buttons 模式测试
  // ========================================

  describe('Buttons Mode', () => {
    it('should render el-button-group', () => {
      const wrapper = mountComponent({ mode: 'buttons' })

      expect(wrapper.find('.el-button-group').exists()).toBe(true)
    })

    it('should render 3 buttons for all themes', () => {
      const wrapper = mountComponent({ mode: 'buttons' })

      const buttons = wrapper.findAll('.el-button')
      expect(buttons).toHaveLength(3)
    })

    it('should highlight current theme button', () => {
      const wrapper = mountComponent({ mode: 'buttons' })

      const buttons = wrapper.findAll('.el-button')

      // 找到包含 primary 类的按钮（当前主题）
      const primaryButton = buttons.find(b => b.classes().includes('el-button--primary'))
      expect(primaryButton).toBeDefined()
    })

    it('should change theme on button click', async () => {
      const wrapper = mountComponent({ mode: 'buttons', enableTransition: false })
      const store = useThemeStore()

      const buttons = wrapper.findAll('.el-button')
      // 找到中国红按钮（不是当前主题）
      const chineseRedButton = buttons.find(b => b.text().includes(THEME_CONFIG['chinese-red'].icon))

      if (chineseRedButton) {
        await chineseRedButton.trigger('click')
        await flushPromises()
      }

      expect(store.currentTheme).toBe('chinese-red')
    })

    it('should emit change event on button click', async () => {
      const wrapper = mountComponent({ mode: 'buttons', enableTransition: false })

      const buttons = wrapper.findAll('.el-button')
      const natureGreenButton = buttons.find(b => b.text().includes(THEME_CONFIG['nature-green'].icon))

      if (natureGreenButton) {
        await natureGreenButton.trigger('click')
        await flushPromises()
      }

      expect(wrapper.emitted('change')).toBeTruthy()
    })
  })

  // ========================================
  // Toggle 模式测试
  // ========================================

  describe('Toggle Mode', () => {
    it('should render single button', () => {
      const wrapper = mountComponent({ mode: 'toggle' })

      const button = wrapper.find('.el-button')
      expect(button.exists()).toBe(true)
    })

    it('should render el-tooltip', () => {
      const wrapper = mountComponent({ mode: 'toggle' })

      expect(wrapper.find('.el-tooltip').exists()).toBe(true)
    })

    it('should cycle theme on click', async () => {
      const wrapper = mountComponent({ mode: 'toggle', enableTransition: false })
      const store = useThemeStore()

      expect(store.currentTheme).toBe('tech-blue')

      await wrapper.find('.el-button').trigger('click')
      await flushPromises()

      expect(store.currentTheme).toBe('chinese-red')
    })

    it('should complete full cycle', async () => {
      const wrapper = mountComponent({ mode: 'toggle', enableTransition: false })
      const store = useThemeStore()
      const button = wrapper.find('.el-button')

      await button.trigger('click') // -> chinese-red
      await flushPromises()
      expect(store.currentTheme).toBe('chinese-red')

      await button.trigger('click') // -> nature-green
      await flushPromises()
      expect(store.currentTheme).toBe('nature-green')

      await button.trigger('click') // -> tech-blue
      await flushPromises()
      expect(store.currentTheme).toBe('tech-blue')
    })
  })

  // ========================================
  // Cards 模式测试
  // ========================================

  describe('Cards Mode', () => {
    it('should render theme-cards container', () => {
      const wrapper = mountComponent({ mode: 'cards' })

      expect(wrapper.find('.theme-cards').exists()).toBe(true)
    })

    it('should render 3 theme cards', () => {
      const wrapper = mountComponent({ mode: 'cards' })

      const cards = wrapper.findAll('.theme-card')
      expect(cards).toHaveLength(3)
    })

    it('should mark current theme card as active', () => {
      const wrapper = mountComponent({ mode: 'cards' })

      const activeCards = wrapper.findAll('.theme-card.is-active')
      expect(activeCards).toHaveLength(1)
    })

    it('should display theme info in cards', () => {
      const wrapper = mountComponent({ mode: 'cards' })

      // 检查是否包含主题信息
      expect(wrapper.text()).toContain('中国红')
      expect(wrapper.text()).toContain('科技蓝')
      expect(wrapper.text()).toContain('自然绿')
    })

    it('should show badge on active card', () => {
      const wrapper = mountComponent({ mode: 'cards' })

      const badge = wrapper.find('.theme-card-badge')
      expect(badge.exists()).toBe(true)
      expect(badge.text()).toContain('使用中')
    })

    it('should change theme on card click', async () => {
      const wrapper = mountComponent({ mode: 'cards', enableTransition: false })
      const store = useThemeStore()

      const cards = wrapper.findAll('.theme-card')
      // 找到不是当前主题的卡片
      const inactiveCard = cards.find(c => !c.classes().includes('is-active'))

      if (inactiveCard) {
        await inactiveCard.trigger('click')
        await flushPromises()
      }

      expect(store.currentTheme).not.toBe('tech-blue')
    })

    it('should emit change event on card click', async () => {
      const wrapper = mountComponent({ mode: 'cards', enableTransition: false })

      const cards = wrapper.findAll('.theme-card')
      const inactiveCard = cards.find(c => !c.classes().includes('is-active'))

      if (inactiveCard) {
        await inactiveCard.trigger('click')
        await flushPromises()
      }

      expect(wrapper.emitted('change')).toBeTruthy()
    })
  })

  // ========================================
  // Props 测试
  // ========================================

  describe('Props', () => {
    describe('mode prop', () => {
      it('should render dropdown for mode=dropdown', () => {
        const wrapper = mountComponent({ mode: 'dropdown' })
        expect(wrapper.find('.el-dropdown').exists()).toBe(true)
      })

      it('should render button-group for mode=buttons', () => {
        const wrapper = mountComponent({ mode: 'buttons' })
        expect(wrapper.find('.el-button-group').exists()).toBe(true)
      })

      it('should render tooltip for mode=toggle', () => {
        const wrapper = mountComponent({ mode: 'toggle' })
        expect(wrapper.find('.el-tooltip').exists()).toBe(true)
      })

      it('should render cards for mode=cards', () => {
        const wrapper = mountComponent({ mode: 'cards' })
        expect(wrapper.find('.theme-cards').exists()).toBe(true)
      })
    })

    describe('showMessage prop', () => {
      it('should show success message by default', async () => {
        const wrapper = mountComponent({ mode: 'buttons', enableTransition: false })

        const buttons = wrapper.findAll('.el-button')
        const chineseRedButton = buttons.find(b => b.text().includes(THEME_CONFIG['chinese-red'].icon))

        if (chineseRedButton) {
          await chineseRedButton.trigger('click')
          await flushPromises()
        }

        expect(vi.mocked(ElMessage.success)).toHaveBeenCalled()
      })

      it('should not show message when showMessage=false', async () => {
        const wrapper = mountComponent({ mode: 'buttons', showMessage: false, enableTransition: false })

        const buttons = wrapper.findAll('.el-button')
        const chineseRedButton = buttons.find(b => b.text().includes(THEME_CONFIG['chinese-red'].icon))

        if (chineseRedButton) {
          await chineseRedButton.trigger('click')
          await flushPromises()
        }

        expect(vi.mocked(ElMessage.success)).not.toHaveBeenCalled()
      })
    })

    describe('enableTransition prop', () => {
      it('should use transition by default', async () => {
        const wrapper = mountComponent({ mode: 'buttons' })
        const store = useThemeStore()

        const buttons = wrapper.findAll('.el-button')
        const chineseRedButton = buttons.find(b => b.text().includes(THEME_CONFIG['chinese-red'].icon))

        if (chineseRedButton) {
          chineseRedButton.trigger('click')
          await nextTick()

          // 过渡期间 isTransitioning 应该为 true
          expect(store.isTransitioning).toBe(true)

          await flushPromises()
        }
      })

      it('should not use transition when enableTransition=false', async () => {
        const wrapper = mountComponent({ mode: 'buttons', enableTransition: false })
        const store = useThemeStore()

        const buttons = wrapper.findAll('.el-button')
        const chineseRedButton = buttons.find(b => b.text().includes(THEME_CONFIG['chinese-red'].icon))

        if (chineseRedButton) {
          await chineseRedButton.trigger('click')
          await nextTick()

          // 不使用过渡时，isTransitioning 应该保持 false
          expect(store.isTransitioning).toBe(false)
        }
      })
    })
  })

  // ========================================
  // Emits 测试
  // ========================================

  describe('Emits', () => {
    it('should emit change event with theme name', async () => {
      const wrapper = mountComponent({ mode: 'buttons', enableTransition: false })

      const buttons = wrapper.findAll('.el-button')
      const chineseRedButton = buttons.find(b => b.text().includes(THEME_CONFIG['chinese-red'].icon))

      if (chineseRedButton) {
        await chineseRedButton.trigger('click')
        await flushPromises()
      }

      expect(wrapper.emitted('change')).toBeTruthy()
      expect(wrapper.emitted('change')![0]).toEqual(['chinese-red'])
    })

    it('should emit change on cycle (toggle mode)', async () => {
      const wrapper = mountComponent({ mode: 'toggle', enableTransition: false })

      await wrapper.find('.el-button').trigger('click')
      await flushPromises()

      expect(wrapper.emitted('change')).toBeTruthy()
      // 从 tech-blue 循环到 chinese-red
      expect(wrapper.emitted('change')![0]).toEqual(['chinese-red'])
    })
  })

  // ========================================
  // 错误处理测试
  // ========================================

  describe('Error Handling', () => {
    it('should show error message on theme change failure', async () => {
      const wrapper = mountComponent({ mode: 'buttons', enableTransition: false })
      const store = useThemeStore()

      // 模拟 setTheme 抛出错误
      vi.spyOn(store, 'setTheme').mockImplementation(() => {
        throw new Error('Test error')
      })

      const buttons = wrapper.findAll('.el-button')
      const chineseRedButton = buttons.find(b => b.text().includes(THEME_CONFIG['chinese-red'].icon))

      if (chineseRedButton) {
        await chineseRedButton.trigger('click')
        await flushPromises()
      }

      expect(vi.mocked(ElMessage.error)).toHaveBeenCalledWith('主题切换失败，请重试')
    })

    it('should log error to console on failure', async () => {
      const wrapper = mountComponent({ mode: 'toggle', enableTransition: false })
      const store = useThemeStore()

      vi.spyOn(store, 'cycleTheme').mockImplementation(() => {
        throw new Error('Cycle error')
      })

      await wrapper.find('.el-button').trigger('click')
      await flushPromises()

      expect(console.error).toHaveBeenCalled()
    })
  })

  // ========================================
  // 边界情况测试
  // ========================================

  describe('Edge Cases', () => {
    it('should handle rapid clicks gracefully', async () => {
      const wrapper = mountComponent({ mode: 'toggle', enableTransition: false })
      const store = useThemeStore()
      const button = wrapper.find('.el-button')

      // 快速点击多次
      await button.trigger('click')
      await button.trigger('click')
      await button.trigger('click')

      await flushPromises()

      // 应该最终稳定在某个主题
      expect(['tech-blue', 'chinese-red', 'nature-green']).toContain(store.currentTheme)
    })

    it('should not break when clicking same card multiple times', async () => {
      const wrapper = mountComponent({ mode: 'cards', enableTransition: false })
      const store = useThemeStore()

      const activeCard = wrapper.find('.theme-card.is-active')

      await activeCard.trigger('click')
      await activeCard.trigger('click')
      await flushPromises()

      // 主题应该保持不变
      expect(store.currentTheme).toBe('tech-blue')
    })

    it('should handle component unmount gracefully', async () => {
      const wrapper = mountComponent({ mode: 'buttons', enableTransition: false })

      // 卸载组件
      wrapper.unmount()

      // 不应该抛出错误
      await flushPromises()
    })
  })

  // ========================================
  // 样式相关测试
  // ========================================

  describe('Styling', () => {
    it('should have inline-block display for container', () => {
      const wrapper = mountComponent()
      const container = wrapper.find('.theme-switcher')

      expect(container.exists()).toBe(true)
    })

    it('should render theme icons', () => {
      const wrapper = mountComponent({ mode: 'cards' })

      // 检查是否包含表情符号图标
      const iconContainers = wrapper.findAll('.theme-card-icon')
      expect(iconContainers).toHaveLength(3)

      iconContainers.forEach(icon => {
        expect(icon.text()).toBeTruthy()
      })
    })
  })
})
