import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import './styles/variables.scss'
import './styles/form.scss'
import './styles/dark-theme.scss'

import App from './App.vue'
import router from './router'
import permissionDirectives from './directives/permission'
import { useUserStore } from './stores/user'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 注册权限指令
app.directive('role', permissionDirectives.role)
app.directive('not-role', permissionDirectives.notRole)
app.directive('permission', permissionDirectives.permission)

// 初始化用户状态（不阻塞应用启动）
const userStore = useUserStore()
userStore.initFromStorage()

app.mount('#app')
