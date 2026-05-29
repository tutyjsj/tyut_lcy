<template>
  <div>
    <div class="page-title">网格排名</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="时间范围">
          <el-radio-group v-model="query.timeRange" @change="search">
            <el-radio-button value="month">本月</el-radio-button>
            <el-radio-button value="today">今日</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="网格名称"><el-input v-model="query.keyword" placeholder="请输入网格名称/负责人" clearable style="width:200px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button></el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button :type="query.sort==='top5'?'primary':''" @click="query.sort='top5';search()">前五名</el-button>
            <el-button :type="query.sort==='bottom5'?'primary':''" @click="query.sort='bottom5';search()">后五名</el-button>
            <el-button :type="query.sort===''?'primary':''" @click="query.sort='';search()">全部</el-button>
          </el-button-group>
        </el-form-item>
        <el-form-item v-if="parentGrid">
          <el-button @click="goBack">{{ parentGrid.gridName }} ← 返回上级</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="ranking-table" v-loading="loading">
      <el-table :data="list" stripe>
        <el-table-column label="排名" width="70" align="center">
          <template #default="{row}">
            <span v-if="row.rank===1" class="rank-icon" style="color:#F56C6C">🥇</span>
            <span v-else-if="row.rank===2" class="rank-icon" style="color:#E6A23C">🥈</span>
            <span v-else-if="row.rank===3" class="rank-icon" style="color:#409EFF">🥉</span>
            <span v-else class="rank-num">{{ row.rank }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="gridName" label="网格名称" width="120" />
        <el-table-column prop="gridLeader" label="负责人" width="90" />
        <el-table-column label="下级网格" width="90" align="center">
          <template #default="{row}">
            <el-link v-if="row.subGridCount>0" type="primary" @click="drillDown(row)">{{ row.subGridCount }}</el-link>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column label="企业数" width="80" align="center">
          <template #default="{row}">{{ row.enterpriseCount }}</template>
        </el-table-column>
        <el-table-column label="问题总数" width="90" align="center">
          <template #default="{row}">
            <el-link type="danger" @click="toProblemList(row)">{{ row.problemTotal }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="待处理" width="80" align="center">
          <template #default="{row}">
            <span style="color:#F56C6C;font-weight:bold">{{ row.pendingCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="问题占比" width="120" align="center">
          <template #default="{row}">
            <div :id="'chart-'+row.gridId" class="mini-chart"></div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top:16px;display:flex;justify-content:flex-end"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :total="total" :page-sizes="[3,6,9]" layout="total, sizes, prev, pager, next"
        @current-change="fetch" @size-change="fetch"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getGridRanking } from '@/api'
import { autoResize } from '@/utils/echarts'

echarts.use([PieChart, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const router = useRouter()
const loading = ref(false), list = ref([]), total = ref(0), parentGrid = ref(null)
const query = reactive({ keyword:'', sort:'', parentId:null, timeRange:'month', pageNum:1, pageSize:3 })
const chartInstances = []

const disposeCharts = () => { chartInstances.splice(0).forEach(c => { try { c.dispose() } catch {} }) }

const renderCharts = async () => {
  await nextTick()
  disposeCharts()
  list.value.forEach(row => {
    const el = document.getElementById('chart-' + row.gridId)
    if (!el) return
    const chart = echarts.init(el)
    chartInstances.push(chart)
    const total = row.problemTotal || 0
    if (total === 0) { chart.setOption({ series:[{ type:'pie', data:[{ name:'无数据', value:1, itemStyle:{ color:'#DCDFE6' } }] }] }); return }
    chart.setOption({
      tooltip: { trigger:'item', formatter:'{b}: {c}' },
      series: [{
        type: 'pie', radius: ['40%', '70%'], center: ['50%','50%'],
        label: { show: false }, emphasis: { label: { show: true } },
        data: [
          { name:'待处理', value: row.pendingCount||0, itemStyle:{ color:'#F56C6C' } },
          { name:'已处理', value: row.processedCount||0, itemStyle:{ color:'#E6A23C' } },
          { name:'已关闭', value: row.closedCount||0, itemStyle:{ color:'#67C23A' } }
        ]
      }]
    })
    autoResize(chart)
  })
}

const fetch = async () => {
  loading.value = true
  try {
    const r = await getGridRanking({ ...query })
    const d = r.data || {}
    list.value = d.records || d.list || []
    total.value = d.total || list.value.length
    await renderCharts()
  } catch {
    list.value = []; total.value = 0
  }
  loading.value = false
}

const search = () => { query.pageNum = 1; fetch() }
const drillDown = (row) => {
  parentGrid.value = { gridName: row.gridName, parentId: query.parentId }
  query.parentId = row.gridId
  query.keyword = ''
  query.sort = ''
  search()
}
const goBack = () => {
  query.parentId = parentGrid.value?.parentId || null
  parentGrid.value = null
  search()
}
const toProblemList = (row) => {
  router.push({ path: '/problem/list', query: { gridId: row.gridId } })
}

onMounted(fetch)
</script>

<style scoped>
.ranking-table { background:#fff; border-radius:8px; padding:16px; box-shadow:0 2px 12px rgba(0,0,0,0.06); }
.mini-chart { width:60px; height:60px; display:inline-block; }
.rank-num { font-size:18px; font-weight:700; color:#606266; }
.rank-icon { font-size:24px; }
</style>
