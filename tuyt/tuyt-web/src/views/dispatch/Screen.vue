<template>
  <div class="screen-container">
    <div class="page-title">综合指挥调度大屏</div>
    <el-row :gutter="16" style="height:100%">
      <el-col :span="6" style="height:100%">
        <div class="panel">
          <div class="panel-title">待处理问题</div>
          <div v-if="dataLoading" class="empty-tip">加载中…</div>
          <div v-else-if="!pendingProblems.length" class="empty-tip">暂无待处理问题</div>
          <div v-for="p in pendingProblems" :key="p.id" class="pending-item" @click="$router.push('/dispatch/problem/'+p.id)">
            <el-tag :type="p.problemLevel==='I'?'danger':'warning'" size="small">{{p.problemLevel==='I'?'严重':'较严重'}}</el-tag>
            <span style="margin-left:8px">{{p.problemDesc}}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="12" style="height:100%">
        <div class="panel map-panel"><div id="dispatch-map" style="width:100%;height:100%"></div></div>
      </el-col>
      <el-col :span="6" style="height:100%">
        <div class="panel">
          <div class="panel-title">最新预警</div>
          <div v-if="dataLoading" class="empty-tip">加载中…</div>
          <div v-else-if="!alerts.length" class="empty-tip">暂无最新预警</div>
          <div v-for="a in alerts" :key="a.id" class="alert-item">
            <el-icon color="#F56C6C"><WarningFilled /></el-icon>
            <span>{{ a.content }}</span>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, onActivated } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'
import { getProblemList, getEnterpriseList } from '@/api'
import { createMap, addProblemMarkers, addEnterpriseMarkers } from '@/utils/amap'

const pendingProblems = ref([])
const alerts = ref([])
const dataLoading = ref(false)
let map = null, AMapModule = null, infoWindow = null
let currentMarkers = []

function extractRecords(res) {
  const d = res?.data ?? res ?? {}
  return Array.isArray(d) ? d : (d.records || d.list || [])
}

const fetchData = async () => {
  dataLoading.value = true
  let allProblems = []

  // 1) 问题列表（独立 catch，避免一损俱损）
  try {
    const probRes = await getProblemList({ handleStatus: 'PENDING', pageNum: 1, pageSize: 50 })
    allProblems = extractRecords(probRes)
    console.log('[调度大屏] 问题数据条数:', allProblems.length)
    pendingProblems.value = allProblems
      .filter(p => (p.problemLevel || p.problem_level) === 'I' || (p.problemLevel || p.problem_level) === 'II')
      .slice(0, 10)
    alerts.value = allProblems.slice(0, 5).map(p => {
      const desc = p.problemDesc || p.problem_desc || p.description || '暂无描述'
      return { id: p.id, content: desc.substring(0, 20) + '…', lng: p.longitude || p.lng, lat: p.latitude || p.lat, level: p.problemLevel || p.problem_level }
    })
  } catch (e) {
    console.error('[调度大屏] 问题列表加载失败', e)
  }

  // 2) 企业列表 + 地图标记（独立 catch）
  try {
    if (!map || !AMapModule) return
    // 移除旧标记
    if (currentMarkers.length) {
      map.remove(currentMarkers)
      currentMarkers = []
    }
    // 问题点位
    const problemMarkers = addProblemMarkers(map, AMapModule, allProblems.filter(p => (p.longitude || p.lng) && (p.latitude || p.lat)), (p) => {
      const content = `<div style="padding:8px"><strong>${p.enterpriseName||'未知企业'}</strong><br/>${p.problemDesc||p.problem_desc||''}<br/><span style="color:#999">${p.alarmTime||''}</span></div>`
      if (infoWindow) infoWindow.close()
      infoWindow = new AMapModule.InfoWindow({ content, offset: [0, -30] })
      infoWindow.open(map, [p.longitude || p.lng, p.latitude || p.lat])
    })
    currentMarkers.push(...problemMarkers)

    // 企业点位
    const entRes = await getEnterpriseList({ pageNum: 1, pageSize: 999 })
    const enterprises = extractRecords(entRes)
    const violatingIds = new Set(allProblems.map(p => p.enterpriseId).filter(Boolean))
    const filteredEnterprises = enterprises.filter(e => (e.longitude || e.lng) && (e.latitude || e.lat))
    const entMarkers = addEnterpriseMarkers(map, AMapModule, filteredEnterprises, (e) => {
      const isViolating = violatingIds.has(e.id)
      const tag = isViolating ? '<span style="color:#F56C6C;font-weight:bold">[违法]</span> ' : ''
      const content = `<div style="padding:8px"><strong>${tag}${e.enterpriseName||e.name||'未知企业'}</strong><br/>地址：${e.address||'-'}<br/>污染类型：${e.pollutionType||'-'}</div>`
      if (infoWindow) infoWindow.close()
      infoWindow = new AMapModule.InfoWindow({ content, offset: [0, -30] })
      infoWindow.open(map, [e.longitude || e.lng, e.latitude || e.lat])
    })
    currentMarkers.push(...entMarkers)

    if (currentMarkers.length) map.setFitView()
  } catch (e) {
    console.error('[调度大屏] 企业/标记加载失败', e)
  } finally {
    dataLoading.value = false
  }
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

// keep-alive 缓存后重新进入时刷新数据
onActivated(() => {
  if (map && AMapModule) fetchData()
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
.empty-tip { color:#909399; font-size:13px; text-align:center; padding:24px 0; }
</style>
