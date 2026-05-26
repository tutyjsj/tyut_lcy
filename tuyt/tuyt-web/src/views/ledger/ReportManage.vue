<template>
  <div>
    <div class="page-title">报表管理</div>
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="统计周期"><el-date-picker type="month" v-model="month" placeholder="选择月份" /></el-form-item>
        <el-form-item label="处理单位"><el-select v-model="unitId" clearable placeholder="全部" /></el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      </el-form>
    </div>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12"><div class="chart-card"><div class="chart-title">任务完成率</div><div ref="pieDom" style="height:320px"></div></div></el-col>
      <el-col :span="12"><div class="chart-card"><div class="chart-title">各单位任务统计</div><div ref="barDom" style="height:320px"></div></div></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { getReportData } from '@/api'
import { createPieChart, createBarChart } from '@/utils/echarts'

const month = ref('')
const unitId = ref('')
const pieDom = ref(null), barDom = ref(null)
let pieChart = null, barChart = null

const fetchData = async () => {
  try {
    const res = await getReportData({ month: month.value, unitId: unitId.value })
    const data = res.data || {}
    if (pieChart) {
      pieChart.setOption({
        series: [{
          data: [
            { name: '已完成', value: data.completed || 0, itemStyle: { color: '#67C23A' } },
            { name: '处理中', value: data.processing || 0, itemStyle: { color: '#409EFF' } },
            { name: '待处理', value: data.pending || 0, itemStyle: { color: '#E6A23C' } },
            { name: '超期', value: data.overdue || 0, itemStyle: { color: '#F56C6C' } }
          ]
        }]
      })
    }
    if (barChart) {
      barChart.setOption({
        xAxis: { data: data.units?.map(u => u.name) || [] },
        series: [
          { name: '已完成', data: data.units?.map(u => u.completed) || [], itemStyle: { color: '#67C23A' } },
          { name: '待处理', data: data.units?.map(u => u.pending) || [], itemStyle: { color: '#E6A23C' } }
        ]
      })
    }
  } catch { /* 后端未就绪 */ }
}

onBeforeUnmount(() => { pieChart?.dispose(); barChart?.dispose() })

onMounted(() => {
  if (pieDom.value) pieChart = createPieChart(pieDom.value, [])
  if (barDom.value) barChart = createBarChart(barDom.value, [], [])
  fetchData()
})
</script>

<style scoped>
.chart-card { background:#fff; border-radius:8px; padding:20px; box-shadow:0 2px 12px rgba(0,0,0,0.06); margin-top:20px; }
.chart-title { font-size:15px; font-weight:600; color:#303133; margin-bottom:16px; }
</style>
