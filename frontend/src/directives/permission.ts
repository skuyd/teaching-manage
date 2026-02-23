import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'
import type { UserRole } from '@/api/types'

// v-role 指令：显示特定角色可见的元素
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<UserRole | UserRole[]>) {
    const userStore = useUserStore()
    const roles = Array.isArray(binding.value) ? binding.value : [binding.value]

    if (!userStore.user || !roles.includes(userStore.user.role)) {
      el.style.display = 'none'
      el.remove()
    }
  }
}

// v-not-role 指令：隐藏特定角色的元素
export const notRole: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<UserRole | UserRole[]>) {
    const userStore = useUserStore()
    const roles = Array.isArray(binding.value) ? binding.value : [binding.value]

    if (userStore.user && roles.includes(userStore.user.role)) {
      el.style.display = 'none'
      el.remove()
    }
  }
}

// v-permission 指令：基于权限检查的通用指令
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<{
    roles?: UserRole | UserRole[]
    check?: (userRole: UserRole) => boolean
  }>) {
    const userStore = useUserStore()

    if (!userStore.user) {
      el.style.display = 'none'
      el.remove()
      return
    }

    const { roles, check } = binding.value

    let hasPermission = true

    // 基于角色检查
    if (roles) {
      const roleList = Array.isArray(roles) ? roles : [roles]
      hasPermission = roleList.includes(userStore.user.role)
    }

    // 自定义检查函数
    if (check) {
      hasPermission = check(userStore.user.role)
    }

    if (!hasPermission) {
      el.style.display = 'none'
      el.remove()
    }
  }
}

export default {
  role,
  notRole,
  permission
}
