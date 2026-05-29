import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './styles/global.scss'
import { preloadAMap } from '@/utils/amap'

// 应用启动后，在浏览器空闲时预加载高德地图 SDK（不阻塞首屏渲染）
// 这样后续打开地图页面时，SDK 已经就绪，无需等待
preloadAMap()

// 按需注册项目中实际使用的 Element Plus 图标（避免全量注册 287 个图标）
import {
  Monitor, Fold, Expand, UserFilled, ArrowDown, SwitchButton,
  List, Grid, Folder, WarningFilled, Odometer, Document,
  Trophy, Setting, MapLocation, ZoomIn, ZoomOut, FullScreen,
  User, Lock, SuccessFilled, CircleCloseFilled,
  Search, Location, Connection, Crop, Minus, Link,
  Aim, Edit, RefreshLeft, Delete, MoreFilled, Select,
  CircleCheck, Picture, VideoCamera, Bell, ChatDotRound,
  Calendar, Timer, Finished, Promotion
} from '@element-plus/icons-vue'

const icons = {
  Monitor, Fold, Expand, UserFilled, ArrowDown, SwitchButton,
  List, Grid, Folder, WarningFilled, Odometer, Document,
  Trophy, Setting, MapLocation, ZoomIn, ZoomOut, FullScreen,
  User, Lock, SuccessFilled, CircleCloseFilled,
  Search, Location, Connection, Crop, Minus, Link,
  Aim, Edit, RefreshLeft, Delete, MoreFilled, Select,
  CircleCheck, Picture, VideoCamera, Bell, ChatDotRound,
  Calendar, Timer, Finished, Promotion
}

const app = createApp(App)

for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
// Element Plus 组件和样式由 unplugin-vue-components + ElementPlusResolver 按需自动导入
// 中文语言包通过 App.vue 中的 ElConfigProvider 全局配置

app.mount('#app')
