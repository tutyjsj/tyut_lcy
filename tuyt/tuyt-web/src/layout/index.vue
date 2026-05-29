 <template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-container" @click="goHome">
        <el-icon :size="28" color="#409EFF"><Monitor /></el-icon>
        <span v-show="!isCollapse" class="logo-title">环境网格化系统</span>
      </div>
      <div class="menu-scroll-wrapper">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <template v-for="route in routes" :key="route.path">
            <el-sub-menu v-if="route.children && route.children.length > 0" :index="route.path">
              <template #title>
                <el-icon><component :is="route.meta.icon" /></el-icon>
                <span>{{ route.meta.title }}</span>
              </template>
              <el-menu-item
                v-for="child in route.children.filter(c => !c.meta?.hidden)"
                :key="child.path"
                :index="child.path"
              >
                {{ child.meta.title }}
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="route.path">
              <el-icon><component :is="route.meta.icon" /></el-icon>
              <template #title>{{ route.meta.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" :size="20" @click="toggleCollapse">
            <Fold v-if="!isCollapse" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
              {{ item.meta?.title || item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" :key="route.path" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { asyncRoutes } from '@/router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const routes = asyncRoutes

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})

const toggleCollapse = () => { isCollapse.value = !isCollapse.value }
const goHome = () => { router.push('/') }
const handleCommand = (cmd) => { if (cmd === 'logout') userStore.logout() }
</script>

<style scoped>
.layout-container { height: 100vh; }
.layout-aside {
  background: linear-gradient(180deg, #263445 0%, #304156 100%);
  overflow: hidden;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(0,0,0,0.06);
}
.logo-container {
  display: flex; align-items: center; justify-content: center;
  height: 60px; gap: 10px; cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  flex-shrink: 0;
  background: rgba(0,0,0,0.08);
}
.logo-title { color: #fff; font-size: 16px; font-weight: 700; white-space: nowrap; }

/* 菜单滚动区域 */
.menu-scroll-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}
/* 自定义滚动条样式（暗色背景） */
.menu-scroll-wrapper::-webkit-scrollbar { width: 4px; }
.menu-scroll-wrapper::-webkit-scrollbar-track { background: #304156; }
.menu-scroll-wrapper::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.2); border-radius: 2px; }
.menu-scroll-wrapper::-webkit-scrollbar-thumb:hover { background: rgba(255,255,255,0.35); }

.el-menu { border-right: none; }
.layout-header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #e6e6e6; padding: 0 20px; height: 56px; }
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { cursor: pointer; }
.user-info { display: flex; align-items: center; gap: 6px; cursor: pointer; color: #606266; }
.layout-main {
  position: relative;
  min-height: calc(100vh - 56px);
  padding: 20px;
  overflow-y: auto;
  /* 多层背景叠加：渐变底色 + 几何网格图案 + 装饰光斑 */
  background:
    /* 第三层：装饰性柔和光斑 */
    radial-gradient(ellipse at 20% 10%, rgba(64, 158, 255, 0.06) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 90%, rgba(64, 158, 255, 0.04) 0%, transparent 60%),
    radial-gradient(ellipse at 50% 50%, rgba(103, 194, 58, 0.04) 0%, transparent 70%),
    /* 第二层：细密网格线（呼应"网格化"主题） */
    linear-gradient(rgba(64, 158, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(64, 158, 255, 0.03) 1px, transparent 1px),
    /* 第一层：柔和渐变底色 */
    linear-gradient(180deg, #f5f7fb 0%, #eef1f6 40%, #f0f3f8 100%);
  background-size:
    100% 100%, 100% 100%, 100% 100%,
    24px 24px, 24px 24px,
    100% 100%;
}
/* 页面切换过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active { transition: all 0.2s ease; }
.fade-transform-enter-from { opacity: 0; transform: translateX(-10px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(10px); }
</style>
