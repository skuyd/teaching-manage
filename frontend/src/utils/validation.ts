import type { FormItemRule } from 'element-plus'

// Common validation rules factory
export const rules = {
  // Required field
  required(message = '此字段为必填项'): FormItemRule {
    return { required: true, message, trigger: 'blur' }
  },

  // Email validation
  email(message = '请输入有效的邮箱地址'): FormItemRule {
    return {
      type: 'email',
      message,
      trigger: 'blur'
    }
  },

  // String length validation
  length(min: number, max: number, message?: string): FormItemRule {
    return {
      min,
      max,
      message: message || `长度应在 ${min} 到 ${max} 个字符之间`,
      trigger: 'blur'
    }
  },

  // Minimum length
  minLength(min: number, message?: string): FormItemRule {
    return {
      min,
      message: message || `长度不能少于 ${min} 个字符`,
      trigger: 'blur'
    }
  },

  // Maximum length
  maxLength(max: number, message?: string): FormItemRule {
    return {
      max,
      message: message || `长度不能超过 ${max} 个字符`,
      trigger: 'blur'
    }
  },

  // Pattern validation
  pattern(regex: RegExp, message: string): FormItemRule {
    return {
      pattern: regex,
      message,
      trigger: 'blur'
    }
  },

  // Number range validation
  range(min: number, max: number, message?: string): FormItemRule {
    return {
      type: 'number',
      min,
      max,
      message: message || `数值应在 ${min} 到 ${max} 之间`,
      trigger: 'blur'
    }
  },

  // Username validation (letters, numbers, underscore)
  username(message = '用户名只能包含字母、数字和下划线'): FormItemRule {
    return {
      pattern: /^[a-zA-Z0-9_]+$/,
      message,
      trigger: 'blur'
    }
  },

  // Chinese phone number validation
  phone(message = '请输入有效的手机号码'): FormItemRule {
    return {
      pattern: /^1[3-9]\d{9}$/,
      message,
      trigger: 'blur'
    }
  },

  // Password strength (at least 6 chars, must include letter and number)
  password(message = '密码至少6位，必须包含字母和数字'): FormItemRule {
    return {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,}$/,
      message,
      trigger: 'blur'
    }
  },

  // URL validation
  url(message = '请输入有效的URL地址'): FormItemRule {
    return {
      type: 'url',
      message,
      trigger: 'blur'
    }
  },

  // Integer validation
  integer(message = '请输入整数'): FormItemRule {
    return {
      type: 'integer',
      message,
      trigger: 'blur'
    }
  },

  // Positive number validation
  positive(message = '请输入正数'): FormItemRule {
    return {
      validator: (_rule, value, callback) => {
        if (value === '' || value === null || value === undefined) {
          callback()
        } else if (Number(value) <= 0) {
          callback(new Error(message))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  },

  // Custom validator
  custom(validator: (value: unknown) => boolean | string, message: string): FormItemRule {
    return {
      validator: (_rule, value, callback) => {
        const result = validator(value)
        if (result === true) {
          callback()
        } else if (typeof result === 'string') {
          callback(new Error(result))
        } else {
          callback(new Error(message))
        }
      },
      trigger: 'blur'
    }
  },

  // Confirm password validation
  confirmPassword(getPassword: () => string, message = '两次输入的密码不一致'): FormItemRule {
    return {
      validator: (_rule, value, callback) => {
        if (value !== getPassword()) {
          callback(new Error(message))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  },

  // Array not empty validation
  arrayNotEmpty(message = '请至少选择一项'): FormItemRule {
    return {
      type: 'array',
      required: true,
      message,
      trigger: 'change'
    }
  }
}

// Form validation helper
export async function validateForm(
  formRef: { validate: () => Promise<boolean> } | null
): Promise<boolean> {
  if (!formRef) return false
  try {
    return await formRef.validate()
  } catch {
    // Focus on first error field
    setTimeout(() => {
      const errorField = document.querySelector('.el-form-item.is-error .el-input__inner') as HTMLElement
      errorField?.focus()
    }, 100)
    return false
  }
}

// Reset form helper
export function resetForm(
  formRef: { resetFields: () => void } | null
): void {
  formRef?.resetFields()
}

// Scroll to first error
export function scrollToFirstError(containerSelector = '.el-form'): void {
  setTimeout(() => {
    const container = document.querySelector(containerSelector)
    const errorField = container?.querySelector('.el-form-item.is-error')
    if (errorField) {
      errorField.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }, 100)
}

export default rules
