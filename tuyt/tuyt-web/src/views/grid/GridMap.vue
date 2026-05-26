<template>
  <div class="grid-map-container">
    <div class="page-title">网格地图</div>
    <div class="map-wrapper">
      <div class="map-left">
        <el-tree :data="treeData" :props="treeProps" node-key="id" default-expand-all @node-click="onNodeClick" style="background:transparent" />
      </div>
      <div class="map-right">
        <div id="grid-map" style="width:100%;height:100%"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { getGridTree, getGridEnterprises } from '@/api'
import { createMap, drawGridPolygon, addProblemMarkers } from '@/utils/amap'

const treeData = ref([])
const treeProps = { label: 'gridName', children: 'children' }
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
    map = await createMap('grid-map', { zoom: 11, center: [112.55, 37.87] })
    if (!map) return
    AMapModule = window.AMap
    map.addControl(new AMapModule.Scale())
  } catch (e) {
    console.error('网格地图：初始化失败', e)
  }
})
</script>

<style scoped>
.grid-map-container { height: calc(100vh - 120px); display: flex; flex-direction: column; }
.map-wrapper { flex: 1; display: flex; gap: 0; border-radius: 8px; overflow: hidden; }
.map-left { width: 240px; background: #fff; padding: 12px; overflow-y: auto; }
.map-right { flex: 1; background: #e8e8e8; }
</style>
