// 高德地图 JSAPI Loader 封装
// 使用前请在高德开放平台注册并获取 Key: https://lbs.amap.com

const AMAP_KEY = '2b6dacd055f08cc60936fadbd14ba23c'
const AMAP_VERSION = '2.0'

// ⚠️ 安全密钥必须在加载 AMap JS 之前设置（高德 2.0 要求）
// 采用代理服务器转发方式：密钥存放在后端/代理层，不暴露在浏览器中
// 开发环境：Vite proxy 自动转发到 https://restapi.amap.com 并附加 jscode（见 vite.config.js）
// 生产环境：需配置 Nginx 或 Java 后端代理转发
window._AMapSecurityConfig = {
  serviceHost: '/_AMapService'
  // 该路径会被前端所有地图请求自动拼接，由代理服务器转发并附加 jscode
}

let AMapPromise = null

/**
 * 加载高德地图 JSAPI（单例，避免重复加载）
 * @returns {Promise<typeof AMap>}
 */
export function loadAMap() {
  if (window.AMap) return Promise.resolve(window.AMap)
  if (AMapPromise) return AMapPromise

  console.log('[AMap] 开始加载 SDK... key:', AMAP_KEY.substring(0, 8) + '***')

  AMapPromise = new Promise((resolve, reject) => {
    const plugin = 'AMap.PolyEditor,AMap.Geocoder,AMap.DistrictSearch,AMap.Scale,AMap.ToolBar'
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=${AMAP_VERSION}&key=${AMAP_KEY}&plugin=${plugin}`
    script.onload = () => {
      if (window.AMap) {
        console.log('[AMap] SDK 加载成功 ✓')
      } else {
        console.error('[AMap] SDK 脚本加载完成但 window.AMap 不存在！可能 Key 无效或缺少安全密钥')
      }
      resolve(window.AMap)
    }
    script.onerror = (e) => {
      console.error('[AMap] SDK 加载失败 ✗  请检查:', {
        'Key 是否正确': true,
        'serviceHost 是否配置': !window._AMapSecurityConfig?.serviceHost ? '未配置（代理转发必须配置 serviceHost）' : '已配置',
        '代理服务器是否启动': true,
        'vite.config.js 中 AMAP_JSCODE 是否替换为真实密钥': true,
        '网络是否正常': true,
        '高德控制台是否启用 JS API': true
      })
      reject(new Error('高德地图加载失败'))
    }
    document.head.appendChild(script)
  })
  return AMapPromise
}

// 默认图标
export const MARKER_ICONS = {
  problem: {
    I: '#F56C6C',   // 严重 - 红
    II: '#E6A23C',  // 较严重 - 黄
    III: '#409EFF'   // 一般 - 蓝
  },
  enterprise: '#67C23A',
  personnel: '#909399'
}

/**
 * 创建地图实例的工厂函数
 */
export async function createMap(containerId, options = {}) {
  const AMap = await loadAMap()
  const container = document.getElementById(containerId)
  if (!container) {
    console.error(`地图容器 #${containerId} 不存在`)
    return null
  }
  return new AMap.Map(containerId, {
    zoom: 11,
    center: [112.55, 37.87], // 默认太原市中心
    mapStyle: 'amap://styles/light',
    ...options
  })
}

/**
 * 在地图上添加问题点位标记
 */
export function addProblemMarkers(map, AMap, problems, onClick) {
  const markers = problems.map(p => {
    const color = MARKER_ICONS.problem[p.level] || '#409EFF'
    const marker = new AMap.Marker({
      position: [p.lng, p.lat],
      title: p.description,
      icon: new AMap.Icon({
        size: new AMap.Size(24, 32),
        image: `https://webapi.amap.com/theme/v1.3/markers/n/mark_r${p.level === 'I' ? 'ed' : p.level === 'II' ? 'orange' : 'blue'}.png`,
        imageSize: new AMap.Size(24, 32)
      }),
      extData: p
    })
    if (onClick) {
      marker.on('click', () => onClick(p))
    }
    return marker
  })
  map.add(markers)
  return markers
}

/**
 * 绘制网格多边形区域
 */
export function drawGridPolygon(map, AMap, paths, style) {
  const polygon = new AMap.Polygon({
    path: paths,
    fillColor: style?.fillColor || '#409EFF',
    fillOpacity: style?.fillOpacity || 0.15,
    strokeColor: style?.strokeColor || '#409EFF',
    strokeWeight: style?.strokeWeight || 2,
    strokeOpacity: style?.strokeOpacity || 0.8
  })
  map.add(polygon)
  return polygon
}
