import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import './styles/variables.scss'
import './styles/form.scss'
import './styles/dark-theme.scss'

// 导入主题 CSS 文件
import './styles/themes/theme-tech-blue.css'
import './styles/themes/theme-sky-blue.css'
import './styles/themes/theme-nature-green.css'

import App from './App.vue'
import router from './router'
import permissionDirectives from './directives/permission'
import { useUserStore } from './stores/user'
import { useThemeStore } from './stores/theme'

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

// 初始化主题（从 localStorage 恢复用户偏好）
const themeStore = useThemeStore()
themeStore.initFromStorage()

app.mount('#app')
