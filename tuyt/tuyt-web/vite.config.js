import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

// ==================== 高德地图安全密钥配置 ====================
// 请到高德开放平台控制台 → 应用管理 → 我的应用 → 点击 Key 右侧「设置」获取安全密钥
// https://console.amap.com/dev/key/app
const AMAP_JSCODE = '125018d57465b3bc066339648062a8b3'
// =============================================================

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia']
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        // 分包策略：将大型第三方库拆分为独立 chunk，提高加载效率
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          'echarts': ['echarts']
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/ws': {
        target: 'ws://127.0.0.1:8080',
        ws: true
      },
      // ==================== 高德地图代理（隐藏安全密钥）====================
      // 自定义地图样式服务代理（必须放在通用规则前面，优先级更高）
      '/_AMapService/v4/map/styles': {
        target: 'https://webapi.amap.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/_AMapService/, ''),
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => {
            proxyReq.path += (proxyReq.path.includes('?') ? '&' : '?') + 'jscode=' + AMAP_JSCODE
          })
        }
      },
      // Web 服务 API 代理
      '/_AMapService': {
        target: 'https://restapi.amap.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/_AMapService/, ''),
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => {
            proxyReq.path += (proxyReq.path.includes('?') ? '&' : '?') + 'jscode=' + AMAP_JSCODE
          })
        }
      }
      // ================================================================
    }
  }
})
