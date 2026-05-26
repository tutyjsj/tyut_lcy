<template>
  <div class="screen-container">
    <div class="page-title">综合指挥调度大屏</div>
    <el-row :gutter="16" style="height:100%">
      <el-col :span="6" style="height:100%">
        <div class="panel"><div class="panel-title">待处理问题</div><div v-for="p in pendingProblems" :key="p.id" class="pending-item" @click="$router.push('/dispatch/problem/'+p.id)"><el-tag :type="p.level==='I'?'danger':'warning'" size="small">{{p.level==='I'?'严重':'较严重'}}</el-tag><span style="margin-left:8px">{{p.description}}</span></div></div>
      </el-col>
      <el-col :span="12" style="height:100%">
        <div class="panel map-panel"><div id="dispatch-map" style="width:100%;height:100%"></div></div>
      </el-col>
      <el-col :span="6" style="height:100%">
        <div class="panel"><div class="panel-title">最新预警</div><div v-for="a in alerts" :key="a.id" class="alert-item"><el-icon color="#F56C6C"><WarningFilled /></el-icon><span>{{ a.content }}</span></div></div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'
import { getProblemList } from '@/api'
import { createMap, addProblemMarkers } from '@/utils/amap'

const pendingProblems = ref([])
const alerts = ref([])
let map = null, AMapModule = null, infoWindow = null

const fetchData = async () => {
  try {
    const res = await getProblemList({ status: 'PENDING', pageNum: 1, pageSize: 50 })
    const data = res.data || {}
    const all = data.records || data.list || []
    pendingProblems.value = all.filter(p => p.level === 'I' || p.level === 'II').slice(0, 10)
    alerts.value = all.slice(0, 5).map(p => ({ id: p.id, content: p.description?.substring(0, 20) + '…', lng: p.lng, lat: p.lat, level: p.level }))
    if (map && AMapModule) {
      addProblemMarkers(map, AMapModule, all.filter(p => p.lng && p.lat), (p) => {
        const content = `<div style="padding:8px"><strong>${p.enterpriseName||'未知企业'}</strong><br/>${p.description||''}<br/><span style="color:#999">${p.alarmTime||''}</span></div>`
        if (infoWindow) infoWindow.close()
        infoWindow = new AMapModule.InfoWindow({ content, offset: [0, -30] })
        infoWindow.open(map, [p.lng, p.lat])
      })
      map.setFitView()
    }
  } catch { /* 后端未就绪 */ }
}

onBeforeUnmount(() => { if (map) map.destroy() })

onMounted(async () => {
  try {
    map = await createMap('dispatch-map', { zoom: 12, center: [112.55, 37.87] })
    if (!map) return
    AMapModule = window.AMap
    fetchData()
  } catch (e) {
    console.error('调度大屏：地图初始化失败', e)
  }
})
</script>

<style scoped>
.screen-container { height: calc(100vh - 120px); }
.panel { background:#fff; border-radius:8px; padding:16px; height:100%; overflow-y:auto; }
.panel.map-panel { padding:0; }
.panel-title { font-size:15px; font-weight:600; color:#303133; margin-bottom:12px; padding-bottom:8px; border-bottom:1px solid #ebeef5; }
.pending-item { padding:8px; border-radius:4px; margin-bottom:8px; cursor:pointer; display:flex; align-items:center; }
.pending-item:hover { background:#f5f7fa; }
.alert-item { padding:10px 8px; border-left:3px solid #F56C6C; margin-bottom:10px; background:#fef0f0; display:flex; align-items:center; gap:8px; font-size:13px; }
</style>
