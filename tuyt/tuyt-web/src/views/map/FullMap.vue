<template>
  <div class="fullmap-container">
    <div class="page-title">电子地图</div>
    <div class="map-toolbar">
      <el-button-group>
        <el-button size="small" @click="zoomIn"><el-icon><ZoomIn /></el-icon></el-button>
        <el-button size="small" @click="zoomOut"><el-icon><ZoomOut /></el-icon></el-button>
        <el-button size="small" @click="goFullScreen"><el-icon><FullScreen /></el-icon></el-button>
      </el-button-group>
      <el-checkbox-group v-model="layers" style="margin-left:16px">
        <el-checkbox label="pollution" checked>污染源点位</el-checkbox>
        <el-checkbox label="sensitive">敏感点</el-checkbox>
        <el-checkbox label="device">感知设备</el-checkbox>
        <el-checkbox label="personnel">人员位置</el-checkbox>
      </el-checkbox-group>
    </div>
    <div id="full-map" style="flex:1;background:#e8e8e8;border-radius:8px"></div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { ZoomIn, ZoomOut, FullScreen } from '@element-plus/icons-vue'
import { getEnterpriseList } from '@/api'
import { createMap } from '@/utils/amap'

const layers = ref(['pollution'])
let map = null, AMapModule = null
const enterpriseMarkers = ref([])
const sensitiveMarkers = ref([])
const deviceMarkers = ref([])
const personnelMarkers = ref([])

const zoomIn = () => map?.zoomIn()
const zoomOut = () => map?.zoomOut()
const goFullScreen = () => {
  const el = document.getElementById('full-map')
  el?.requestFullscreen?.()
}

const loadEnterprises = async () => {
  try {
    const res = await getEnterpriseList({ pageNum: 1, pageSize: 200 })
    const data = res.data || {}
    const list = data.records || data.list || []
    if (list.length === 0) {
      console.log('电子地图：企业数据为空')
    }
    list.filter(e => e.lng && e.lat).forEach(e => {
      const m = new AMapModule.Marker({
        position: [e.lng, e.lat],
        title: e.name,
        icon: new AMapModule.Icon({
          size: [22, 28],
          image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
          imageSize: [22, 28]
        }),
        extData: e
      })
      m.on('click', () => {
        map.setZoomAndCenter(15, [e.lng, e.lat])
        const infoWin = new AMapModule.InfoWindow({
          content: `<div style="padding:8px"><strong>${e.name}</strong><br/>${e.address||''}</div>`,
          offset: [0, -30]
        })
        infoWin.open(map, [e.lng, e.lat])
      })
      enterpriseMarkers.value.push(m)
      if (layers.value.includes('pollution')) m.setMap(map)
    })
  } catch (e) {
    console.error('电子地图：加载企业数据失败', e)
  }
}

watch(() => layers.value.includes('pollution'), v => {
  enterpriseMarkers.value.forEach(m => v ? m.setMap(map) : m.setMap(null))
})

onBeforeUnmount(() => { if (map) map.destroy() })

onMounted(async () => {
  try {
    map = await createMap('full-map', { zoom: 12, center: [112.55, 37.87] })
    if (!map) return
    AMapModule = window.AMap
    map.addControl(new AMapModule.Scale())
    map.addControl(new AMapModule.ToolBar({ position: 'RT' }))
    loadEnterprises()
  } catch (e) {
    console.error('电子地图：初始化失败', e)
  }
})
</script>

<style scoped>
.fullmap-container { height: calc(100vh - 120px); display: flex; flex-direction: column; }
.map-toolbar { display: flex; align-items: center; padding-bottom: 12px; }
</style>
