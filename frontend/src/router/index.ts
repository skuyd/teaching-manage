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
    meta: { requiresAuth: true }
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
    meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'] }
  },
  {
    path: '/subjects/:id/groups',
    name: 'GroupManagement',
    component: () => import('@/views/groups/GroupManagement.vue'),
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
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
