<template>
  <div class="stat-container">
    <div class="page-title">问题统计</div>
    <div class="stat-cards">
      <div class="stat-card"><div class="stat-number">{{ total }}</div><div class="stat-label">今日问题总数</div></div>
      <div class="stat-card"><div class="stat-number danger">{{ pending }}</div><div class="stat-label">待处理</div></div>
      <div class="stat-card"><div class="stat-number success">{{ done }}</div><div class="stat-label">已处理</div></div>
    </div>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12"><div class="chart-card"><div class="chart-title">污染类型分布</div><div ref="pollutionChart" style="height:340px"></div></div></el-col>
      <el-col :span="12"><div class="chart-card"><div class="chart-title">问题来源分布</div><div ref="sourceChart" style="height:340px"></div></div></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { getProblemStatistics } from '@/api'
import { createRingChart } from '@/utils/echarts'
import { pollutionTypeOptions } from '@/utils/constants'

const total = ref(0), pending = ref(0), done = ref(0)
const pollutionChart = ref(null), sourceChart = ref(null)
let pChart = null, sChart = null

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
    const sData = (data.sources || []).map(s => ({ name: s.name, value: Number(s.value) || 0 }))

    if (pChart) pChart.setOption({ series: [{ data: pData }] })
    if (sChart) sChart.setOption({ series: [{ data: sData }] })
  } catch { /* 后端未就绪 */ }
}

onBeforeUnmount(() => { pChart?.dispose(); sChart?.dispose() })

onMounted(() => {
  if (pollutionChart.value) pChart = createRingChart(pollutionChart.value, [], '污染类型分布')
  if (sourceChart.value) sChart = createRingChart(sourceChart.value, [], '问题来源分布')
  fetchData()
})
</script>

<style scoped>
.stat-cards { display: flex; gap: 20px; }
.stat-card { flex:1; text-align:center; padding:28px 20px; }
.stat-number { font-size:36px; font-weight:700; color:#409EFF; }
.stat-number.danger { color:#F56C6C; } .stat-number.success { color:#67C23A; }
.stat-label { font-size:14px; color:#909399; margin-top:8px; }
.chart-card { background:#fff; border-radius:8px; padding:20px; box-shadow:0 2px 12px rgba(0,0,0,0.06); }
.chart-title { font-size:16px; font-weight:600; color:#303133; margin-bottom:16px; }
</style>
