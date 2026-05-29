
<template>
  <div class="stat-container">
    <div class="page-title">问题统计</div>
    <div class="stat-cards">
      <div class="stat-card" @click="goList()"><div class="stat-number">{{ total }}</div><div class="stat-label">今日问题总数</div></div>
      <div class="stat-card" @click="goList('PENDING')"><div class="stat-number danger">{{ pending }}</div><div class="stat-label">待处理</div></div>
      <div class="stat-card" @click="goList('DONE')"><div class="stat-number success">{{ done }}</div><div class="stat-label">已处理</div></div>
    </div>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12"><div class="chart-card"><div class="chart-title">污染类型分布</div><div ref="pollutionChart" style="height:380px"></div></div></el-col>
      <el-col :span="12"><div class="chart-card"><div class="chart-title">问题来源分布</div><div ref="sourceChart" style="height:380px"></div></div></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { getProblemStatistics } from '@/api'
import { createRingChart } from '@/utils/echarts'
import { pollutionTypeOptions, problemSourceMap } from '@/utils/constants'

const router = useRouter()
const total = ref(0), pending = ref(0), done = ref(0)
const pollutionChart = ref(null), sourceChart = ref(null)
let pChart = null, sChart = null

/** 点击统计卡片 → 跳转到问题列表并带上筛选条件 */
const goList = (status) => {
  router.push({ path: '/problem/list', query: status ? { handleStatus: status } : {} })
}

/** 从显示 label 反查 pollutionType value（如 "废水污染" → "WASTE_WATER"） */
const reversePollutionMap = {}
pollutionTypeOptions.forEach(o => { reversePollutionMap[o.label] = o.value })

/** 从显示 label 反查 problemSource value（如 "巡查发现" → "PATROL"） */
const reverseSourceMap = {}
Object.entries(problemSourceMap).forEach(([key, label]) => { reverseSourceMap[label] = key })

/** 点击污染类型图表 → 跳转到问题列表筛选对应类型 */
const onPollutionClick = (params) => {
  if (!params || !params.name) return
  const code = reversePollutionMap[params.name]
  if (code) {
    router.push({ path: '/problem/list', query: { problemType: code } })
  }
}

/** 点击问题来源图表 → 跳转到问题列表筛选对应来源 */
const onSourceClick = (params) => {
  if (!params || !params.name) return
  const code = reverseSourceMap[params.name]
  if (code) {
    router.push({ path: '/problem/list', query: { problemSource: code } })
  }
}

const fetchData = async () => {
  try {
    const res = await getProblemStatistics()
    const data = res.data || {}
    total.value = data.total || 0
    pending.value = data.pending || 0
    done.value = data.done || 0

    const pData = (data.pollutionTypes || []).map(v => ({
      name: pollutionTypeOptions.find(o => o.value === v.name)?.label || v.name,
      value: Number(v.value) || 0
    }))
    const sData = (data.sources || []).map(s => ({ name: problemSourceMap[s.name] || s.name, value: Number(s.value) || 0 }))

    if (pChart) pChart.setOption({ series: [{ data: pData }] })
    if (sChart) sChart.setOption({ series: [{ data: sData }] })
  } catch { /* 后端未就绪 */ }
}

onBeforeUnmount(() => { pChart?.dispose(); sChart?.dispose() })

onMounted(() => {
  if (pollutionChart.value) {
    pChart = createRingChart(pollutionChart.value, [], '污染类型分布')
    pChart.on('click', onPollutionClick)
  }
  if (sourceChart.value) {
    sChart = createRingChart(sourceChart.value, [], '问题来源分布')
    sChart.on('click', onSourceClick)
  }
  fetchData()
})
</script>

<style scoped>
.stat-cards { display: flex; gap: 20px; }
.stat-card { flex:1; text-align:center; padding:28px 20px; cursor:pointer; border-radius:8px; background:#fff; box-shadow:0 2px 12px rgba(0,0,0,0.06); transition:all .25s; }
.stat-card:hover { transform:translateY(-2px); box-shadow:0 4px 16px rgba(0,0,0,.12); }
.stat-number { font-size:36px; font-weight:700; color:#409EFF; }
.stat-number.danger { color:#F56C6C; } .stat-number.success { color:#67C23A; }
.stat-label { font-size:14px; color:#909399; margin-top:8px; }
.chart-card { background:#fff; border-radius:8px; padding:20px; box-shadow:0 2px 12px rgba(0,0,0,0.06); }
.chart-title { font-size:16px; font-weight:600; color:#303133; margin-bottom:16px; }
</style>
