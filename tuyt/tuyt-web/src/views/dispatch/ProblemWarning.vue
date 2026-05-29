<template>
  <div>
    <div class="page-title">问题预警</div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="6">
        <div class="stat-card danger" :class="{ active: activeCard === 'levelI' }" @click="filterByCard('levelI')"><div class="stat-num">{{ counts.levelI }}</div><div class="stat-label">严重预警 (I级)</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card warning" :class="{ active: activeCard === 'levelII' }" @click="filterByCard('levelII')"><div class="stat-num">{{ counts.levelII }}</div><div class="stat-label">较严重预警 (II级)</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card info" :class="{ active: activeCard === 'overdue' }" @click="filterByCard('overdue')"><div class="stat-num">{{ counts.overdue }}</div><div class="stat-label">超期待处理</div></div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" :class="{ active: activeCard === 'today' }" @click="filterByCard('today')"><div class="stat-num">{{ counts.today }}</div><div class="stat-label">今日新增</div></div>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="预警级别">
          <el-select v-model="query.problemLevel" clearable placeholder="全部" style="width:130px" @change="onFilterChange">
            <el-option label="严重 (I级)" value="I" />
            <el-option label="较严重 (II级)" value="II" />
            <el-option label="一般 (III级)" value="III" />
          </el-select>
        </el-form-item>
        <el-form-item label="污染类型">
          <el-select v-model="query.pollutionType" clearable placeholder="全部" style="width:130px" @change="onFilterChange">
            <el-option v-for="opt in pollutionTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="事发区域">
          <el-select v-model="query.gridId" clearable placeholder="全部市" style="width:130px" @change="search">
            <el-option v-for="g in cityGrids" :key="g.id" :label="g.gridName" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="超期类型">
          <el-select v-model="query.overdueType" clearable placeholder="全部" style="width:130px">
            <el-option label="超期任务" value="overdue" />
            <el-option label="即将超期 (24h内)" value="nearly" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="danger" :disabled="selectedIds.length===0" @click="batchDispatch">批量派发</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图表 -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="16">
        <div class="chart-card"><div class="chart-title">近30天问题预警趋势</div><div ref="trendChartRef" style="height: 300px"></div></div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card"><div class="chart-title">预警类型占比</div><div ref="typeChartRef" style="height: 300px"></div></div>
      </el-col>
    </el-row>

    <!-- 问题列表 -->
    <div class="table-card">
      <el-table :data="pagedList" v-loading="loading" @selection-change="handleSelectionChange" empty-text="暂无预警问题">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="problemNo" label="编号" width="100" />
        <el-table-column label="等级" width="80">
          <template #default="{ row }">
            <el-tag :type="row.problemLevel==='I'?'danger':row.problemLevel==='II'?'warning':''" size="small">{{ row.problemLevel==='I'?'严重':row.problemLevel==='II'?'较严重':'一般' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enterpriseName" label="事发企业" min-width="140" />
        <el-table-column prop="problemDesc" label="问题描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="污染类型" width="100">
          <template #default="{ row }">{{ pollutionMap[row.pollutionType] || row.pollutionType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="areaName" label="事发区域" width="120" />
        <el-table-column prop="alarmTime" label="报警时间" width="160" />
        <el-table-column label="超期状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="isOverdue(row)" type="danger" size="small">超期</el-tag>
            <el-tag v-else-if="isNearlyOverdue(row)" type="warning" size="small">即将超期</el-tag>
            <span v-else style="color:#67c23a">正常</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/dispatch/problem/'+row.id)">甄别</el-button>
            <el-button link type="warning" @click="goDispatchTask(row)">派发</el-button>
            <el-button link type="info" @click="ignoreWarning(row)">忽略</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" :total="pagingTotal" layout="total, sizes, prev, pager, next" @current-change="onPageChange" @size-change="onPageChange(true)" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProblemList, getGridList, updateProblem, getWarningStatistics } from '@/api'
import { pollutionTypeOptions } from '@/utils/constants'
import { createLineChart, createRingChart } from '@/utils/echarts'

const router = useRouter()
const loading = ref(false), list = ref([]), total = ref(0), selectedIds = ref([])
/** 全量统计数据（从后端 warning-stats 接口获取，基于全部数据） */
const warningStats = ref(null)
/** 全量统计卡片数值（仅初始化和忽略操作时更新，不受筛选影响） */
const counts = reactive({ levelI: 0, levelII: 0, overdue: 0, today: 0 })
const cityGrids = ref([])
/** 当前激活的统计卡片（用于高亮） */
const activeCard = ref('')
const pollutionMap = { WASTE_WATER:'废水', WASTE_GAS:'废气', NOISE:'噪声', SOLID_WASTE:'固危废', RADIATION:'辐射', OTHER:'其他' }

const query = reactive({
  problemLevel: '', pollutionType: '', gridId: null, overdueType: '', pageNum: 1, pageSize: 10
})

// ECharts 实例
let trendChart = null, typeChart = null
const trendChartRef = ref(null), typeChartRef = ref(null)

const handleSelectionChange = (val) => { selectedIds.value = val.map(v => v.id) }
const isOverdue = (row) => {
  if (!row.alarmTime || row.handleStatus === 'CLOSED') return false
  const alarm = new Date(row.alarmTime)
  return Date.now() - alarm > 7 * 24 * 3600 * 1000
}
const isNearlyOverdue = (row) => {
  if (!row.alarmTime || row.handleStatus === 'CLOSED') return false
  const alarm = new Date(row.alarmTime)
  const diff = Date.now() - alarm
  return diff > 3 * 24 * 3600 * 1000 && diff <= 7 * 24 * 3600 * 1000
}

/** 点击统计卡片 → 筛选对应数据 */
function filterByCard(card) {
  if (activeCard.value === card) {
    activeCard.value = ''
    query.problemLevel = ''
    query.overdueType = ''
    search()
    return
  }
  activeCard.value = card
  query.pageNum = 1
  if (card === 'levelI') {
    query.problemLevel = 'I'
    query.overdueType = ''
    search()
  } else if (card === 'levelII') {
    query.problemLevel = 'II'
    query.overdueType = ''
    search()
  } else if (card === 'overdue') {
    query.problemLevel = ''
    query.overdueType = 'overdue'
    search()
  } else if (card === 'today') {
    query.problemLevel = ''
    query.overdueType = ''
    search()
  }
}

/** 展示列表（超期/今日新增做客户端二次过滤） */
const filteredList = computed(() => {
  const active = activeCard.value
  const overdue = query.overdueType
  if (active === 'today') {
    return list.value.filter(p => {
      if (!p.alarmTime) return false
      return new Date(p.alarmTime).toDateString() === new Date().toDateString()
    })
  }
  if (active === 'overdue' || overdue === 'overdue') {
    return list.value.filter(p => isOverdue(p))
  }
  if (overdue === 'nearly') {
    return list.value.filter(p => isNearlyOverdue(p))
  }
  return list.value
})

/** 是否需要客户端分页（统计卡片筛选或超期类型筛选时，数据已全量拉取） */
const isClientFilter = computed(() => !!activeCard.value || !!query.overdueType)

/** 分页总数 */
const pagingTotal = computed(() => isClientFilter.value ? filteredList.value.length : total.value)

/** 表格展示数据：客户端筛选模式时对 filteredList 做切片分页 */
const pagedList = computed(() => {
  if (!isClientFilter.value) return filteredList.value
  const start = (query.pageNum - 1) * query.pageSize
  return filteredList.value.slice(start, start + query.pageSize)
})

/** 分页变化：客户端模式只更新 pageNum/pageSize，服务端模式调用 search */
const onPageChange = (isSizeChange) => {
  if (isSizeChange) query.pageNum = 1
  if (isClientFilter.value) return
  search()
}

/** 拉取全量统计数据 → 仅更新4个统计卡片数字（不受筛选影响） */
const fetchGlobalCounts = async () => {
  try {
    const res = await getWarningStatistics()
    const stats = res.data || {}
    counts.levelI = stats.levelI || 0
    counts.levelII = stats.levelII || 0
    counts.overdue = stats.overdue || 0
    counts.today = stats.todayNew || 0
  } catch { /* 后端未就绪 */ }
}

/** 拉取带筛选参数的统计 → 仅用于渲染图表（趋势图 + 饼图） */
const fetchFilteredStats = async (filterParams) => {
  try {
    const params = {}
    if (filterParams) {
      if (filterParams.problemLevel) params.problemLevel = filterParams.problemLevel
      if (filterParams.pollutionType) params.pollutionType = filterParams.pollutionType
      if (filterParams.gridId) params.gridId = filterParams.gridId
    }
    const res = await getWarningStatistics(params)
    warningStats.value = res.data || {}
    // 统计就绪后渲染图表
    nextTick(initCharts)
  } catch { /* 后端未就绪 */ }
}

/** 筛选条件变化时自动触发查询（污染类型、预警级别变化） */
const onFilterChange = () => {
  search()
}

const search = async () => {
  query.pageNum = 1
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize, handleStatus: 'PENDING' }
    if (query.problemLevel) params.problemLevel = query.problemLevel
    if (query.pollutionType) params.pollutionType = query.pollutionType
    if (query.gridId) params.gridId = query.gridId
    // 服务端暂不支持 overdueType 精确过滤，全量拉取后客户端过滤
    const needLargePage = !!activeCard.value || !!query.overdueType
    if (needLargePage) params.pageSize = 200
    const res = await getProblemList(params)
    const data = res.data || {}
    list.value = data.records || data.list || []
    total.value = Number(data.total) || 0
    // 刷新图表数据（带筛选条件，不影响统计卡片数字）
    fetchFilteredStats({ problemLevel: query.problemLevel, pollutionType: query.pollutionType, gridId: query.gridId })
  } catch { /* 后端未就绪 */ }
  finally { loading.value = false }
}

const reset = () => {
  query.problemLevel = ''; query.pollutionType = ''; query.gridId = null; query.overdueType = ''
  activeCard.value = ''
  search()
}

const goDispatchTask = (row) => {
  router.push({ path: '/dispatch/task', query: { problemId: row.id } })
}
const ignoreWarning = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail" style="margin-left:0;margin-right:0">
      <div class="del-row"><span class="del-label">问题编号</span><span class="del-value"><strong>${row.problemNo || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">事发企业</span><span class="del-value">${row.enterpriseName || '-'}</span></div>
      <div class="del-row"><span class="del-label">问题描述</span><span class="del-value">${(row.problemDesc || '-').substring(0, 60)}</span></div>
      <div class="del-row"><span class="del-label">预警等级</span><span class="del-value">${row.problemLevel==='I'?'严重 (I级)':row.problemLevel==='II'?'较严重 (II级)':'一般 (III级)'}</span></div>
    </div>
    <div class="action-info">
      <div class="action-info-title">忽略后该问题将从预警列表中移除，不再进行超期提醒。</div>
      <div class="action-info-note">注：已忽略的问题可在"问题管理"中查看，并可后续重新甄别处理。</div>
    </div>`,
    '忽略预警问题',
    {
      confirmButtonText: '确认忽略',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
      draggable: false,
      center: true,
      appendTo: document.body,
      customClass: 'action-confirm-dialog',
      closeOnClickModal: false,
      closeOnPressEscape: false
    }
  ).then(async () => {
    try {
      await updateProblem(row.id, { handleStatus: 'IGNORED' })
      ElMessage.success(`问题 ${row.problemNo} 已忽略`)
      await Promise.all([fetchGlobalCounts(), search()])
    } catch {
      ElMessage.error('忽略失败，请重试')
    }
  }).catch(() => {})
}

const batchDispatch = () => {
  if (selectedIds.value.length === 0) { ElMessage.warning('请先选择问题'); return }
  ElMessage.success(`已选择${selectedIds.value.length}个问题，跳转至任务调度页面`)
  router.push({ path: '/dispatch/task', query: { problemIds: selectedIds.value.join(',') } })
}

// ===== 图表（基于后端返回的全量统计数据，随筛选条件变化） =====
const initCharts = () => {
  const stats = warningStats.value
  if (!stats) return

  // 趋势图：使用后端返回的近30天真实数据
  if (trendChartRef.value) {
    if (trendChart) trendChart.dispose()
    const trendData = stats.trendData || []
    const days = trendData.map(d => {
      const parts = (d.date || '').split('-')
      return parts.length >= 3 ? `${parseInt(parts[1])}/${parseInt(parts[2])}` : d.date
    })
    const data = trendData.map(d => d.count || 0)
    trendChart = createLineChart(trendChartRef.value, {
      xData: days,
      series: [{ name: '预警数', data, color: '#F56C6C' }]
    })
  }

  // 类型占比图：使用环形图（图例右侧垂直排列，避免标签/颜色重叠）
  if (typeChartRef.value) {
    if (typeChart) typeChart.dispose()
    const pollutionData = (stats.pollutionTypes || []).map(item => ({
      name: pollutionMap[item.name] || item.name,
      value: item.value
    }))
    if (pollutionData.length > 0) {
      typeChart = createRingChart(typeChartRef.value, pollutionData, '预警类型')
    }
  }
}

onMounted(async () => {
  try {
    const r = await getGridList({ gridLevel: 1, pageSize: 100 })
    cityGrids.value = (r.data && r.data.records) ? r.data.records : []
  } catch { /* */ }
  // 并行：拉取全量统计卡片（无筛选） + 图表数据（无筛选全量）+ 分页列表
  await Promise.all([fetchGlobalCounts(), fetchFilteredStats(null)])
  search()
})
onBeforeUnmount(() => { if (trendChart) trendChart.dispose(); if (typeChart) typeChart.dispose() })
</script>

<style scoped>
.stat-card {
  background: #fff; border-radius: 8px; padding: 20px; text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,.06); cursor: pointer; transition: all .25s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,.12); }
.stat-card.active { box-shadow: 0 4px 16px rgba(64,158,255,.3); border-left-width: 6px; }
.stat-card.danger { border-left: 4px solid #F56C6C; }
.stat-card.warning { border-left: 4px solid #E6A23C; }
.stat-card.info { border-left: 4px solid #409EFF; }
.stat-num { font-size: 32px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.stat-card.danger .stat-num { color: #F56C6C; }
.stat-card.warning .stat-num { color: #E6A23C; }
.stat-card.info .stat-num { color: #409EFF; }
.search-bar { margin-bottom: 0; }
</style>
