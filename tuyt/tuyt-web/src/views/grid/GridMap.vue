<template>
  <div class="grid-map-container">
    <div class="page-title">网格地图</div>
    <div class="map-wrapper">
      <div class="map-left">
        <el-tree :data="treeData" :props="treeProps" node-key="id" default-expand-all @node-click="onNodeClick" style="background:transparent" />
      </div>
      <div class="map-right">
        <!-- 地图加载骨架 -->
        <div v-if="mapLoading" class="map-loading">
          <div class="loading-spin"><el-icon class="is-loading" :size="40"><Loading /></el-icon></div>
          <span class="loading-text">地图加载中…</span>
        </div>
        <div id="grid-map" style="width:100%;height:100%"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { getGridTree, getGridEnterprises } from '@/api'
import { createMap, drawGridPolygon, addProblemMarkers, preloadAMap } from '@/utils/amap'

const treeData = ref([])
const treeProps = { label: 'gridName', children: 'children' }
const mapLoading = ref(true)
let map = null, AMapModule = null, currentPolygon = null, currentMarkers = []
const polygonColorMap = { CITY: ['#409EFF', '#337ECC'], DISTRICT: ['#67C23A', '#529B2E'], TOWN: ['#E6A23C', '#B88230'] }

const buildTree = (flatList) => {
  const map = {}
  flatList.forEach(item => { map[item.id] = { ...item, children: [] } })
  const roots = []
  flatList.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  return roots
}

const loadTree = async () => {
  try {
    const res = await getGridTree()
    const flatList = res.data || []
    treeData.value = buildTree(flatList)
    console.log('网格地图：树数据', treeData.value.length, '个根节点')
  } catch (e) {
    console.error('网格地图：加载树数据失败', e)
  }
}

const onNodeClick = async (node) => {
  if (currentPolygon) { map.remove(currentPolygon); currentPolygon = null }
  if (currentMarkers.length) { map.remove(currentMarkers); currentMarkers = [] }
  if (node.polygonData) {
    currentPolygon = drawGridPolygon(map, AMapModule, JSON.parse(node.polygonData), { fillColor: '#409EFF', fillOpacity: 0.2 })
  }
  map.setFitView(null, false, [200, 100, 200, 100])
  if (node.id) {
    try {
      const res = await getGridEnterprises(node.id)
      const enterprises = res.data || []
      enterprises.forEach(e => {
        const lng = e.longitude, lat = e.latitude
        if (lng && lat) {
          const marker = new AMapModule.Marker({
            position: [lng, lat],
            title: e.enterpriseName,
            icon: new AMapModule.Icon({
              size: [20, 26],
              image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
              imageSize: [20, 26]
            })
          })
          marker.setMap(map)
          marker.on('click', () => { map.setZoomAndCenter(15, [lng, lat]) })
          currentMarkers.push(marker)
        }
      })
    } catch (e) {
      console.error('网格地图：加载企业数据失败', e)
    }
  }
}

onBeforeUnmount(() => { if (map) map.destroy() })

onMounted(async () => {
  try {
    loadTree()
    // 在后台预加载地图SDK（应用启动时调用一次即可）
    preloadAMap()
    map = await createMap('grid-map', { zoom: 11, center: [112.55, 37.87] })
    mapLoading.value = false
    if (!map) return
    AMapModule = window.AMap
    map.addControl(new AMapModule.Scale())
  } catch (e) {
    mapLoading.value = false
    console.error('网格地图：初始化失败', e)
  }
})
</script>

<style scoped>
.grid-map-container { height: calc(100vh - 120px); display: flex; flex-direction: column; }
.map-wrapper { flex: 1; display: flex; gap: 0; border-radius: 8px; overflow: hidden; }
.map-left { width: 240px; background: #fff; padding: 12px; overflow-y: auto; }
.map-right { flex: 1; background: #e8e8e8; position: relative; }
.map-loading {
  position: absolute; inset: 0; z-index: 10; display: flex;
  flex-direction: column; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.85); gap: 12px;
}
.loading-spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.loading-text { font-size: 14px; color: #909399; }
</style>
