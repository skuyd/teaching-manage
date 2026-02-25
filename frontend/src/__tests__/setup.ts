/**
 * Vitest 测试配置文件
 *
 * 设置测试环境，包括：
 * - localStorage 模拟
 * - document 模拟
 * - window 事件模拟
 * - console 抑制（可选）
 */

import { vi, beforeEach, afterEach } from 'vitest'

// ========================================
// localStorage 模拟
// ========================================

const localStorageMock = (() => {
  let store: Record<string, string> = {}

  return {
    getItem: vi.fn((key: string) => store[key] ?? null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value.toString()
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key]
    }),
    clear: vi.fn(() => {
      store = {}
    }),
    get length() {
      return Object.keys(store).length
    },
    key: vi.fn((index: number) => {
      const keys = Object.keys(store)
      return keys[index] ?? null
    }),
    // 辅助方法：获取当前存储状态（用于测试断言）
    _getStore: () => ({ ...store }),
    // 辅助方法：重置存储
    _reset: () => {
      store = {}
      localStorageMock.getItem.mockClear()
      localStorageMock.setItem.mockClear()
      localStorageMock.removeItem.mockClear()
      localStorageMock.clear.mockClear()
    }
  }
})()

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true
})

// ========================================
// document.documentElement 模拟
// ========================================

// 确保 data-theme 属性可以正常工作
const originalSetAttribute = document.documentElement.setAttribute.bind(document.documentElement)
const originalRemoveAttribute = document.documentElement.removeAttribute.bind(document.documentElement)
const originalGetAttribute = document.documentElement.getAttribute.bind(document.documentElement)

let dataTheme: string | null = null

vi.spyOn(document.documentElement, 'setAttribute').mockImplementation((name: string, value: string) => {
  if (name === 'data-theme') {
    dataTheme = value
  }
  originalSetAttribute(name, value)
})

vi.spyOn(document.documentElement, 'removeAttribute').mockImplementation((name: string) => {
  if (name === 'data-theme') {
    dataTheme = null
  }
  originalRemoveAttribute(name)
})

vi.spyOn(document.documentElement, 'getAttribute').mockImplementation((name: string) => {
  if (name === 'data-theme') {
    return dataTheme
  }
  return originalGetAttribute(name)
})

// ========================================
// document.body.classList 模拟
// ========================================

// classList 已经在 jsdom 中实现，但我们可以监控它
vi.spyOn(document.body.classList, 'add')
vi.spyOn(document.body.classList, 'remove')

// ========================================
// CustomEvent 和 window 事件模拟
// ========================================

const eventListeners: Map<string, Set<EventListener>> = new Map()

const originalAddEventListener = window.addEventListener.bind(window)
const originalRemoveEventListener = window.removeEventListener.bind(window)
const originalDispatchEvent = window.dispatchEvent.bind(window)

vi.spyOn(window, 'addEventListener').mockImplementation((
  type: string,
  listener: EventListenerOrEventListenerObject,
  options?: boolean | AddEventListenerOptions
) => {
  if (!eventListeners.has(type)) {
    eventListeners.set(type, new Set())
  }
  eventListeners.get(type)!.add(listener as EventListener)
  originalAddEventListener(type, listener, options)
})

vi.spyOn(window, 'removeEventListener').mockImplementation((
  type: string,
  listener: EventListenerOrEventListenerObject,
  options?: boolean | EventListenerOptions
) => {
  eventListeners.get(type)?.delete(listener as EventListener)
  originalRemoveEventListener(type, listener, options)
})

// 辅助函数：获取指定事件类型的监听器数量
export function getEventListenerCount(type: string): number {
  return eventListeners.get(type)?.size ?? 0
}

// ========================================
// 测试生命周期钩子
// ========================================

beforeEach(() => {
  // 重置 localStorage
  localStorageMock._reset()

  // 重置 data-theme
  dataTheme = null

  // 清除 body 上的类
  document.body.className = ''

  // 清除事件监听器追踪
  eventListeners.clear()
})

afterEach(() => {
  // 清除所有模拟
  vi.clearAllMocks()
})

// ========================================
// 导出辅助工具
// ========================================

export { localStorageMock }
