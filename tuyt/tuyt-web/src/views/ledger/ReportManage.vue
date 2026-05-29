<template>
  <div>
    <div class="page-title">报表管理</div>

    <!-- 查询条件 -->
    <div class="search-bar">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="统计周期">
          <el-date-picker v-model="query.month" type="month" placeholder="选择月份" value-format="YYYY-MM" style="width:140px" />
        </el-form-item>
        <el-form-item label="处理单位">
          <el-cascader
            v-model="query.unitIdPath"
            :options="orgTreeOptions"
            :props="{ value: 'id', label: 'name', checkStrictly: true, emitPath: true }"
            clearable placeholder="全部"
            style="width:220px"
            @change="onUnitChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top:16px">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">任务完成率</div>
          <div ref="pieDom" style="height:300px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">各单位任务统计</div>
          <div ref="barDom" style="height:300px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 机构统计表格 -->
    <div class="table-card" style="margin-top:20px">
      <div class="chart-title" style="margin-bottom:12px">机构任务明细</div>
      <el-table :data="unitList" stripe border v-loading="loading">
        <el-table-column label="单位名称" min-width="200">
          <template #default="{ row }">
            <span v-if="row.hasChildren" style="cursor:pointer;color:#409EFF;font-weight:600" @click="drillDown(row)">
              {{ row.name }} <el-icon><ArrowRight /></el-icon>
            </span>
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="任务总数" width="90" align="center">
          <template #default="{ row }"><span class="clickable-num" @click="showTasks(row.orgId, null, 'total')">{{ row.total }}</span></template>
        </el-table-column>
        <el-table-column label="已完成" width="80" align="center">
          <template #default="{ row }"><span class="clickable-num done" @click="showTasks(row.orgId, 'DONE', 'done')">{{ row.done }}</span></template>
        </el-table-column>
        <el-table-column label="处理中" width="80" align="center">
          <template #default="{ row }"><span class="clickable-num processing" @click="showTasks(row.orgId, 'DISPATCHED,SIGNED,RECEIVED,PROCESSING', 'processing')">{{ row.processing }}</span></template>
        </el-table-column>
        <el-table-column label="待处理" width="80" align="center">
          <template #default="{ row }"><span class="clickable-num pending" @click="showTasks(row.orgId, 'DRAFT', 'pending')">{{ row.pending }}</span></template>
        </el-table-column>
        <el-table-column label="完成率" width="85" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.rate >= 80 ? '#67C23A' : row.rate >= 50 ? '#E6A23C' : '#F56C6C' }">{{ row.rate }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="超期数" width="75" align="center">
          <template #default="{ row }"><span class="clickable-num danger" @click="showOverdueTasks(row.orgId)">{{ row.overdue }}</span></template>
        </el-table-column>
        <el-table-column label="督办数" width="75" align="center">
          <template #default="{ row }"><span class="clickable-num warning" @click="showSupervisedTasks(row.orgId)">{{ row.supervised }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="drillDown(row)" v-if="row.hasChildren">下级</el-button>
            <span v-else style="color:#c0c4cc">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="unitTotal > 0" style="margin-top:14px;display:flex;justify-content:flex-end"
        v-model:current-page="unitPageNum" v-model:page-size="unitPageSize"
        :page-sizes="[10,20,50]" :total="unitTotal"
        layout="total, sizes, prev, pager, next" @current-change="fetchData" @size-change="fetchData" />
    </div>

    <!-- 任务详情对话框 -->
    <el-dialog v-model="taskDialogVisible" :title="taskDialogTitle" width="900px" :close-on-click-modal="false">
      <el-table :data="taskDetailList" stripe v-loading="taskLoading" max-height="500">
        <el-table-column prop="taskNo" label="任务单号" width="150" show-overflow-tooltip />
        <el-table-column prop="taskTitle" label="任务标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="任务类型" width="95">
          <template #default="{ row }">{{ taskTypeMap[row.taskType] || row.taskType }}</template>
        </el-table-column>
        <el-table-column label="紧急程度" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.urgency==='CRITICAL'" type="danger" size="small">特急</el-tag>
            <el-tag v-else-if="row.urgency==='URGENT'" type="warning" size="small">紧急</el-tag>
            <span v-else style="color:#909399">一般</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="145" />
        <el-table-column prop="deadline" label="截止时间" width="145" />
        <el-table-column label="处理单位" width="110">
          <template #default="{ row }">{{ row.gridName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="85">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ taskStatusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finishTime" label="完成时间" width="145" />
      </el-table>
      <el-pagination v-if="taskTotal > 0" style="margin-top:14px;display:flex;justify-content:flex-end"
        v-model:current-page="taskPageNum" v-model:page-size="taskPageSize"
        :page-sizes="[10,20,50]" :total="taskTotal"
        layout="total, sizes, prev, pager, next" @current-change="loadTaskDetails" @size-change="loadTaskDetails" />
    </el-dialog>

    <!-- 面包屑导航 -->
    <div class="breadcrumb-nav" v-if="breadcrumb.length > 1">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="(item, i) in breadcrumb" :key="i">
          <a v-if="i < breadcrumb.length - 1" href="javascript:;" @click="jumpToLevel(i)">{{ item.name }}</a>
          <span v-else>{{ item.name }}</span>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { getReportData, getReportTasks, getReportOrgTree } from '@/api'
import { createPieChart, createBarChart, autoResize } from '@/utils/echarts'
import { taskStatusMap, taskTypeMap } from '@/utils/constants'

const loading = ref(false)
const query = reactive({ month: '', unitIdPath: null })
// 默认当前月
const curMonth = new Date().getFullYear() + '-' + String(new Date().getMonth() + 1).padStart(2, '0')
query.month = curMonth

const pieDom = ref(null), barDom = ref(null)
let pieChart = null, barChart = null

/* ===== 机构树 ===== */
const orgTreeOptions = ref([])
async function loadOrgTree(parentId) {
  try {
    const res = await getReportOrgTree(parentId)
    return res.data || []
  } catch { return [] }
}

/* ===== 数据获取与图表渲染 ===== */
const unitList = ref([])
const unitPageNum = ref(1), unitPageSize = ref(10), unitTotal = ref(0)

async function fetchData() {
  loading.value = true
  try {
    const unitId = query.unitIdPath ? query.unitIdPath[query.unitIdPath.length - 1] : null
    const res = await getReportData({ month: query.month, unitId, pageNum: unitPageNum.value, pageSize: unitPageSize.value })
    const data = res.data || {}

    // 全局状态饼图
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

    // 单位柱状图
    const units = data.units || []
    unitList.value = units
    unitTotal.value = Number(data.total) || units.length
    if (barChart) {
      barChart.setOption({
        xAxis: { data: units.map(u => u.name.length > 8 ? u.name.substring(0, 7) + '..' : u.name) },
        series: [
          { name: '已完成', data: units.map(u => u.done), itemStyle: { color: '#67C23A' } },
          { name: '处理中', data: units.map(u => u.processing), itemStyle: { color: '#409EFF' } },
          { name: '待处理', data: units.map(u => u.pending), itemStyle: { color: '#E6A23C' } },
          { name: '超期', data: units.map(u => u.overdue), itemStyle: { color: '#F56C6C' } }
        ]
      })
    }
  } catch (e) {
    console.error('报表数据加载失败:', e)
    ElMessage.error('加载报表数据失败')
  } finally { loading.value = false }
}

/* ===== 下钻/返回 ===== */
const breadcrumb = ref([{ id: null, name: '全部机构' }])

function onUnitChange(val) {
  // 级联选择器变化时，同步面包屑
  if (!val || val.length === 0) {
    breadcrumb.value = [{ id: null, name: '全部机构' }]
  }
}

function drillDown(row) {
  breadcrumb.value.push({ id: row.orgId, name: row.name })
  query.unitIdPath = [row.orgId]
  fetchData()
}

function jumpToLevel(index) {
  breadcrumb.value = breadcrumb.value.slice(0, index + 1)
  const target = breadcrumb.value[index]
  query.unitIdPath = target.id ? [target.id] : null
  fetchData()
}

/* ===== 任务详情对话框 ===== */
const taskDialogVisible = ref(false)
const taskDialogTitle = ref('')
const taskDetailList = ref([])
const taskLoading = ref(false)
const taskTotal = ref(0)
const taskPageNum = ref(1)
const taskPageSize = ref(10)
let currentTaskFilter = {}

async function showTasks(orgId, status, label) {
  currentTaskFilter = { orgId, status, month: query.month }
  const labels = { total: '全部任务', done: '已完成', processing: '处理中', pending: '待处理' }
  taskDialogTitle.value = (labels[label] || '任务列表') + `（${breadcrumb.value[breadcrumb.value.length - 1].name}）`
  taskPageNum.value = 1
  taskDialogVisible.value = true
  await loadTaskDetails()
}

async function showOverdueTasks(orgId) {
  currentTaskFilter = { orgId, status: null, month: query.month, overdueOnly: true }
  taskDialogTitle.value = '超期任务（' + breadcrumb.value[breadcrumb.value.length - 1].name + '）'
  taskPageNum.value = 1
  taskDialogVisible.value = true
  await loadTaskDetails()
}

async function showSupervisedTasks(orgId) {
  currentTaskFilter = { orgId, status: null, month: query.month, supervisedOnly: true }
  taskDialogTitle.value = '督办任务（' + breadcrumb.value[breadcrumb.value.length - 1].name + '）'
  taskPageNum.value = 1
  taskDialogVisible.value = true
  await loadTaskDetails()
}

async function loadTaskDetails() {
  taskLoading.value = true
  try {
    const params = {
      orgId: currentTaskFilter.orgId,
      status: currentTaskFilter.status,
      month: currentTaskFilter.month,
      pageNum: taskPageNum.value,
      pageSize: taskPageSize.value
    }
    let res
    if (currentTaskFilter.overdueOnly) {
      params.status = 'DRAFT,DISPATCHED,SIGNED,RECEIVED,PROCESSING,RETURNED'
      res = await getReportTasks(params)
      // 前端过滤超期
      const all = res.data?.records || []
      const now = new Date()
      taskDetailList.value = all.filter(t => t.deadline && new Date(t.deadline) < now)
      taskTotal.value = taskDetailList.value.length
    } else if (currentTaskFilter.supervisedOnly) {
      res = await getReportTasks({ ...params, status: undefined })
      const all = res.data?.records || []
      taskDetailList.value = all.filter(t => t.superviseCount && t.superviseCount > 0)
      taskTotal.value = taskDetailList.value.length
    } else {
      res = await getReportTasks(params)
      taskDetailList.value = res.data?.records || []
      taskTotal.value = Number(res.data?.total) || 0
    }
  } catch { /* ignore */ }
  finally { taskLoading.value = false }
}

function statusTagType(status) {
  const map = { DRAFT: 'info', DISPATCHED: 'warning', SIGNED: '', PROCESSING: '', RECEIVED: '',
               DONE: 'success', COMPLETED: 'success', REVOKED: 'info', RETURNED: 'danger' }
  return map[status] || ''
}

/* ===== 初始化 ===== */
onMounted(async () => {
  // 加载机构树
  const roots = await loadOrgTree(null)
  buildCascaderOptions(roots, orgTreeOptions.value)

  // 初始化图表
  await nextTick()
  if (pieDom.value) pieChart = createPieChart(pieDom.value, [])
  if (barDom.value) barChart = createBarChart(barDom.value, [], [])

  if (pieChart) autoResize(pieChart)
  if (barChart) autoResize(barChart)

  // 加载数据
  fetchData()
})

/** 递归构建级联选择器的 options（含 children） */
function buildCascaderOptions(source, target) {
  for (const item of source) {
    const node = { id: item.id, name: item.name, hasChildren: item.hasChildren }
    target.push(node)
  }
}

onBeforeUnmount(() => { pieChart?.dispose(); barChart?.dispose() })
</script>

<style scoped>
.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.clickable-num {
  cursor: pointer;
  font-weight: 600;
}
.clickable-num:hover { text-decoration: underline; }
.clickable-num.done { color: #67C23A; }
.clickable-num.processing { color: #409EFF; }
.clickable-num.pending { color: #E6A23C; }
.clickable-num.danger { color: #F56C6C; }
.clickable-num.warning { color: #E6A23C; }

.breadcrumb-nav {
  margin-top: 16px;
  padding: 8px 16px;
  background: #fff;
  border-radius: 6px;
  display: inline-block;
}
.breadcrumb-nav a { color: #409EFF; text-decoration: none; }
.breadcrumb-nav a:hover { text-decoration: underline; }
</style>
