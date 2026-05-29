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
    const plugin = 'AMap.PolyEditor,AMap.Geocoder,AMap.DistrictSearch,AMap.Scale,AMap.ToolBar,AMap.RangingTool,AMap.MouseTool,AMap.AutoComplete,AMap.PlaceSearch,AMap.CircleEditor,AMap.Geolocation'
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
    ...options
  })
}

/**
 * 统一获取坐标（兼容 lng/lat 和 longitude/latitude）
 */
function getCoord(item) {
  return [item.lng ?? item.longitude, item.lat ?? item.latitude]
}

/** 统一获取等级（兼容 level 和 problemLevel） */
function getLevel(item) {
  return item.level ?? item.problemLevel
}

/** 生成纯 CSS 标记 HTML（不依赖外部图片，避免 404） */
function createDotHtml(color, size = 16) {
  const half = size / 2
  return `<div style="width:${size}px;height:${size}px;border-radius:50%;background:${color};border:2px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,0.4);transform:translate(-50%,-50%)"></div>`
}

export function addProblemMarkers(map, AMap, problems, onClick) {
  const colorMap = { I: '#F56C6C', II: '#E6A23C', III: '#409EFF' }
  const markers = problems.map(p => {
    const level = getLevel(p) || 'III'
    const color = colorMap[level] || '#409EFF'
    const marker = new AMap.Marker({
      position: getCoord(p),
      title: p.description || p.problemDesc || '',
      content: createDotHtml(color, 18),
      anchor: 'center',
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
 * 在地图上添加企业点位标记
 */
export function addEnterpriseMarkers(map, AMap, enterprises, onClick) {
  const markers = enterprises.map(e => {
    const marker = new AMap.Marker({
      position: getCoord(e),
      title: e.enterpriseName || '',
      content: createDotHtml('#67C23A', 14),
      anchor: 'center',
      extData: e
    })
    if (onClick) {
      marker.on('click', () => onClick(e))
    }
    return marker
  })
  map.add(markers)
  return markers
}

/**
 * 添加自定义图标标记
 */
export function addCustomMarker(map, AMap, item, iconConfig, onClick) {
  const [lng, lat] = getCoord(item)
  const marker = new AMap.Marker({
    position: [lng, lat],
    title: item.name || item.title || '',
    content: createDotHtml(iconConfig.color, iconConfig.size || 16),
    anchor: 'center',
    offset: new AMap.Pixel(0, 0),
    extData: item
  })
  if (onClick) {
    marker.on('click', () => onClick(item, marker))
  }
  map.add(marker)
  return marker
}

/** 创建图标Marker（使用高德默认图标） */
export function createIconMarker(map, AMap, position, iconUrl, title, extData, onClick) {
  const marker = new AMap.Marker({
    position,
    title: title || '',
    icon: new AMap.Icon({
      size: new AMap.Size(32, 32),
      image: iconUrl,
      imageSize: new AMap.Size(32, 32)
    }),
    offset: new AMap.Pixel(-16, -32),
    extData
  })
  if (onClick) marker.on('click', onClick)
  map.add(marker)
  return marker
}

/** 清除所有覆盖物（保留底图） */
export function clearMapOverlays(map) {
  if (!map) return
  map.clearMap()
}

/** 获取两点间距离（米） */
export function getDistance(map, p1, p2) {
  const AMap = window.AMap
  if (!AMap || !map) return 0
  return Math.round(AMap.GeometryUtil.distance(p1, p2))
}

/** 获取多边形面积（平方米） */
export function getPolygonArea(path) {
  const AMap = window.AMap
  if (!AMap) return 0
  return Math.round(AMap.GeometryUtil.ringArea(path))
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
