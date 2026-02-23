import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import type { MessageOptions } from 'element-plus'

// Message duration constants
const DURATION_SHORT = 2000
const DURATION_DEFAULT = 3000
const DURATION_LONG = 5000

// Unified message service
export const message = {
  success(content: string, duration = DURATION_DEFAULT) {
    return ElMessage({
      message: content,
      type: 'success',
      duration,
      showClose: true
    })
  },

  error(content: string, duration = DURATION_LONG) {
    return ElMessage({
      message: content,
      type: 'error',
      duration,
      showClose: true
    })
  },

  warning(content: string, duration = DURATION_DEFAULT) {
    return ElMessage({
      message: content,
      type: 'warning',
      duration,
      showClose: true
    })
  },

  info(content: string, duration = DURATION_DEFAULT) {
    return ElMessage({
      message: content,
      type: 'info',
      duration,
      showClose: true
    })
  },

  loading(content = '加载中...') {
    return ElMessage({
      message: content,
      type: 'info',
      duration: 0,
      showClose: false,
      icon: 'Loading'
    })
  },

  custom(options: MessageOptions) {
    return ElMessage(options)
  }
}

// Unified notification service
export const notify = {
  success(title: string, message?: string, duration = DURATION_LONG) {
    return ElNotification({
      title,
      message,
      type: 'success',
      duration,
      position: 'top-right'
    })
  },

  error(title: string, message?: string, duration = 0) {
    return ElNotification({
      title,
      message,
      type: 'error',
      duration,
      position: 'top-right'
    })
  },

  warning(title: string, message?: string, duration = DURATION_LONG) {
    return ElNotification({
      title,
      message,
      type: 'warning',
      duration,
      position: 'top-right'
    })
  },

  info(title: string, message?: string, duration = DURATION_LONG) {
    return ElNotification({
      title,
      message,
      type: 'info',
      duration,
      position: 'top-right'
    })
  }
}

// Unified confirm dialog service
export const confirm = {
  delete(itemName = '此项') {
    return ElMessageBox.confirm(
      `确定要删除${itemName}吗？此操作不可撤销。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
  },

  action(message: string, title = '确认操作') {
    return ElMessageBox.confirm(
      message,
      title,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  },

  info(message: string, title = '提示') {
    return ElMessageBox.alert(
      message,
      title,
      {
        confirmButtonText: '知道了',
        type: 'info'
      }
    )
  },

  prompt(message: string, title = '请输入', options?: { inputPattern?: RegExp; inputErrorMessage?: string }) {
    return ElMessageBox.prompt(
      message,
      title,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: options?.inputPattern,
        inputErrorMessage: options?.inputErrorMessage
      }
    )
  }
}

// Error handler for API calls
export function handleApiError(error: unknown, defaultMessage = '操作失败') {
  let errorMessage = defaultMessage

  if (error instanceof Error) {
    errorMessage = error.message || defaultMessage
  } else if (typeof error === 'string') {
    errorMessage = error
  } else if (error && typeof error === 'object') {
    const err = error as Record<string, unknown>
    if (typeof err.message === 'string') {
      errorMessage = err.message
    } else if (typeof err.msg === 'string') {
      errorMessage = err.msg
    }
  }

  // Map common error codes to user-friendly messages
  const errorMessages: Record<string, string> = {
    'Network Error': '网络连接失败，请检查网络后重试',
    'timeout': '请求超时，请稍后重试',
    '401': '登录已过期，请重新登录',
    '403': '没有权限执行此操作',
    '404': '请求的资源不存在',
    '429': '请求过于频繁，请稍后重试',
    '500': '服务器错误，请稍后重试'
  }

  for (const [key, msg] of Object.entries(errorMessages)) {
    if (errorMessage.includes(key)) {
      errorMessage = msg
      break
    }
  }

  message.error(errorMessage)
  return errorMessage
}

export default {
  message,
  notify,
  confirm,
  handleApiError
}
