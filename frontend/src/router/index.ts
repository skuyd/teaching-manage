import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true },
    alias: '/dashboard'
  },
  {
    path: '/subjects',
    name: 'Subjects',
    component: () => import('@/views/subjects/SubjectList.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'] }
  },
  {
    path: '/subjects/:id',
    name: 'SubjectDetail',
    component: () => import('@/views/subjects/SubjectDetail.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
  },
  {
    path: '/subjects/:id/groups',
    name: 'GroupManagement',
    component: () => import('@/views/groups/GroupManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/submissions',
    name: 'SubmissionList',
    component: () => import('@/views/submissions/SubmissionList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/submissions/board',
    name: 'SubmissionBoard',
    component: () => import('@/views/submissions/SubmissionBoard.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'] }
  },
  {
    path: '/submissions/:id',
    name: 'SubmissionDetail',
    component: () => import('@/views/submissions/SubmissionDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/grades',
    name: 'GradeManagement',
    component: () => import('@/views/grades/GradeManagement.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'] }
  },
  {
    path: '/grade-summary',
    name: 'GradeSummary',
    component: () => import('@/views/grades/GradeSummary.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'NotificationCenter',
    component: () => import('@/views/notifications/NotificationCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('@/views/users/UserList.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ name: 'Dashboard' })
  } else if (to.meta.roles) {
    // Check role-based access
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      const requiredRoles = to.meta.roles as string[]
      if (requiredRoles.includes(userInfo.role)) {
        next()
      } else {
        // Redirect to dashboard if user doesn't have required role
        next({ name: 'Dashboard' })
      }
    } else {
      next({ name: 'Login' })
    }
  } else {
    next()
  }
})

export default router
