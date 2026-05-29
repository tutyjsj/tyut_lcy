<template>
  <div>
    <div class="page-title">问题台账</div>

    <!-- 查询条件 -->
    <div class="search-bar">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="问题编号"><el-input v-model="query.problemNo" clearable placeholder="问题编号" style="width:140px" /></el-form-item>
        <el-form-item label="事发企业"><el-input v-model="query.enterpriseName" clearable placeholder="企业名称" style="width:160px" /></el-form-item>
        <el-form-item label="事发区域"><el-input v-model="query.areaName" clearable placeholder="事发区域" style="width:130px" /></el-form-item>
        <el-form-item label="问题等级">
          <el-select v-model="query.problemLevel" clearable placeholder="全部" style="width:110px">
            <el-option v-for="(v,k) in problemLevelMap" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="污染类型">
          <el-select v-model="query.problemType" clearable placeholder="全部" style="width:120px">
            <el-option v-for="o in pollutionTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题来源">
          <el-select v-model="query.problemSource" clearable placeholder="全部" style="width:120px">
            <el-option v-for="o in problemSourceOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="query.handleStatus" clearable placeholder="全部" style="width:110px">
            <el-option v-for="(v,k) in handleStatusMap" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮 -->
    <div style="margin-bottom:12px;display:flex;gap:8px">
      <el-button type="danger" @click="showBatchClose" :disabled="selectedIds.length===0">批量关闭 ({{ selectedIds.length }})</el-button>
      <el-button type="success" @click="exportData">导出 Excel</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading" @selection-change="onSelectionChange" ref="tableRef">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="problemNo" label="问题编号" width="150" show-overflow-tooltip />
        <el-table-column prop="enterpriseName" label="事发企业" width="160" show-overflow-tooltip>
          <template #default="{row}">{{ row.enterpriseName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="报警时间" width="160" />
        <el-table-column prop="problemDesc" label="问题详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="problemSource" label="问题来源" width="100">
          <template #default="{row}">{{ problemSourceMap[row.problemSource] || row.problemSource || '-' }}</template>
        </el-table-column>
        <el-table-column prop="problemLevel" label="问题等级" width="90">
          <template #default="{row}">
            <el-tag v-if="problemLevelMap[row.problemLevel]" :type="problemLevelMap[row.problemLevel].tagType" size="small">
              {{ problemLevelMap[row.problemLevel].label }}
            </el-tag>
            <span v-else>{{ row.problemLevel }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="handleStatus" label="处理状态" width="95">
          <template #default="{row}">
            <el-tag :type="handleStatusTagType[row.handleStatus]||''" size="small">
              {{ handleStatusMap[row.handleStatus] || row.handleStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="penaltyStatus" label="处罚状态" width="90">
          <template #default="{row}">{{ row.penaltyStatus || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link size="small" @click="openDetail(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="openEdit(row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50,100]" :total="total"
        layout="total, sizes, prev, pager, next" @current-change="fetchData" @size-change="fetchData" />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editVisible" title="编辑问题" width="580px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="问题编号"><el-input :model-value="editForm.problemNo" disabled /></el-form-item>
        <el-form-item label="事发企业"><el-input v-model="editForm.enterpriseName" @blur="searchEnterprise" placeholder="输入企业名称搜索" /></el-form-item>
        <el-form-item label="事发地点" required><el-input v-model="editForm.address" /></el-form-item>
        <el-form-item label="污染类型" required>
          <el-select v-model="editForm.pollutionType" style="width:100%">
            <el-option v-for="o in pollutionTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题详情" required><el-input v-model="editForm.problemDesc" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="事发区域" required><el-input v-model="editForm.areaName" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 - 三Tab -->
    <el-dialog v-model="detailVisible" title="问题详情" width="700px" :close-on-click-modal="false">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="问题编号">{{ currentProblem.problemNo }}</el-descriptions-item>
            <el-descriptions-item label="问题等级">
              <el-tag v-if="problemLevelMap[currentProblem.problemLevel]" :type="problemLevelMap[currentProblem.problemLevel].tagType" size="small">
                {{ problemLevelMap[currentProblem.problemLevel].label }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="报警时间">{{ currentProblem.alarmTime }}</el-descriptions-item>
            <el-descriptions-item label="问题来源">{{ problemSourceMap[currentProblem.problemSource] || currentProblem.problemSource }}</el-descriptions-item>
            <el-descriptions-item label="问题类型">{{ currentProblem.problemType }}</el-descriptions-item>
            <el-descriptions-item label="污染类型">{{ currentProblem.pollutionType }}</el-descriptions-item>
            <el-descriptions-item label="事发企业">{{ currentProblem.enterpriseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="事发地址">{{ currentProblem.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属区域">{{ currentProblem.areaName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理状态">
              <el-tag :type="handleStatusTagType[currentProblem.handleStatus]||''" size="small">
                {{ handleStatusMap[currentProblem.handleStatus] || currentProblem.handleStatus }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="处罚状态">{{ currentProblem.penaltyStatus || '-' }}</el-descriptions-item>
            <el-descriptions-item label="关闭原因" :span="2">{{ currentProblem.closeReason || '-' }}</el-descriptions-item>
            <el-descriptions-item label="问题详情" :span="2">{{ currentProblem.problemDesc || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="处理动态" name="dynamic">
          <el-timeline v-if="logs.length>0">
            <el-timeline-item v-for="log in logs" :key="log.id"
              :timestamp="log.createTime"
              :color="logTypeColor(log.operationType)"
              placement="top">
              <div class="log-title">{{ logTypeLabel(log.operationType) }}</div>
              <div class="log-content">{{ log.content }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无处理动态" />
        </el-tab-pane>
        <el-tab-pane label="地理信息" name="geo">
          <div v-if="currentProblem.longitude && currentProblem.latitude" style="text-align:center;padding:20px">
            <p style="margin-bottom:12px;color:#666">
              经度: {{ currentProblem.longitude }} &nbsp;&nbsp; 纬度: {{ currentProblem.latitude }}
            </p>
            <p style="color:#999">事发地址: {{ currentProblem.address || '未填写' }}</p>
            <div style="width:100%;height:300px;background:#f0f2f5;border-radius:8px;display:flex;align-items:center;justify-content:center;margin-top:12px">
              <span style="color:#999">[ 地图定位: {{ currentProblem.longitude }}, {{ currentProblem.latitude }} ]</span>
            </div>
          </div>
          <el-empty v-else description="该问题无地理坐标信息" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 批量关闭对话框 -->
    <el-dialog v-model="closeVisible" title="批量关闭问题" width="480px" :close-on-click-modal="false">
      <el-alert type="warning" :closable="false" style="margin-bottom:16px">
        已选择 <b>{{ selectedIds.length }}</b> 个问题，只能关闭状态为<el-tag size="small" type="warning">待处理</el-tag><el-tag size="small" type="primary">已处理</el-tag><el-tag size="small" type="success">处理完成</el-tag>的问题
      </el-alert>
      <el-form :model="closeForm" label-width="80px">
        <el-form-item label="关闭原因" required>
          <el-input v-model="closeForm.reason" type="textarea" :rows="3" placeholder="请填写关闭原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeVisible=false">取消</el-button>
        <el-button type="primary" @click="submitBatchClose" :loading="submitting">确认关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProblemList, getProblemDetail, updateProblem, closeProblem, getProblemLogs, exportProblemLedger, getEnterpriseList } from '@/api'
import { problemLevelMap, pollutionTypeOptions, problemSourceMap, problemSourceOptions, handleStatusMap, handleStatusTagType } from '@/utils/constants'

const loading = ref(false), list = ref([]), total = ref(0), submitting = ref(false)
const tableRef = ref(null), selectedIds = ref([])
const query = reactive({ problemNo: '', enterpriseName: '', areaName: '', problemLevel: '', pollutionType: '', problemType: '', problemSource: '', handleStatus: '', pageNum: 1, pageSize: 10 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProblemList({ ...query })
    list.value = res.data?.records || res.data?.list || []
    total.value = Number(res.data?.total) || 0
  } catch (e) { ElMessage.error('查询失败') }
  finally { loading.value = false }
}
const search = () => { query.pageNum = 1; fetchData() }

const resetQuery = () => {
  query.problemNo = ''; query.enterpriseName = ''; query.areaName = ''; query.problemLevel = ''; query.pollutionType = ''; query.problemType = ''; query.problemSource = ''; query.handleStatus = ''
  query.pageNum = 1
  search()
}

const onSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }

// ===== 编辑 =====
const editVisible = ref(false)
const editForm = reactive({ id: null, problemNo: '', enterpriseName: '', address: '', pollutionType: '', problemDesc: '', areaName: '', enterpriseId: null })
const openEdit = (row) => {
  Object.assign(editForm, {
    id: row.id, problemNo: row.problemNo,
    enterpriseName: row.enterpriseName || '', enterpriseId: row.enterpriseId || null,
    address: row.address || '', pollutionType: row.pollutionType || '',
    problemDesc: row.problemDesc || '', areaName: row.areaName || ''
  })
  editVisible.value = true
}
const searchEnterprise = async () => {
  if (!editForm.enterpriseName) { editForm.enterpriseId = null; return }
  try {
    const res = await getEnterpriseList({ enterpriseName: editForm.enterpriseName, pageNum: 1, pageSize: 5 })
    const list = res.data?.records || []
    if (list.length === 1) {
      editForm.enterpriseName = list[0].enterpriseName
      editForm.enterpriseId = list[0].id
    } else if (list.length > 1) {
      ElMessage.info('存在多个匹配企业，请精确输入企业名称')
    }
  } catch { /* ignore */ }
}
const submitEdit = async () => {
  if (!editForm.address || !editForm.pollutionType || !editForm.problemDesc || !editForm.areaName) {
    ElMessage.warning('事发地点、污染类型、问题详情、事发区域为必填项')
    return
  }
  submitting.value = true
  try {
    await updateProblem(editForm.id, {
      enterpriseId: editForm.enterpriseId,
      address: editForm.address,
      pollutionType: editForm.pollutionType,
      problemDesc: editForm.problemDesc,
      areaName: editForm.areaName
    })
    ElMessage.success('修改成功')
    editVisible.value = false
    search()
  } catch (e) { ElMessage.error('修改失败: ' + (e?.message || '')) }
  finally { submitting.value = false }
}

// ===== 查看详情 =====
const detailVisible = ref(false), activeTab = ref('basic')
const currentProblem = ref({})
const logs = ref([])
const openDetail = async (row) => {
  activeTab.value = 'basic'; logs.value = []
  try {
    const res = await getProblemDetail(row.id)
    currentProblem.value = res.data || row
  } catch { currentProblem.value = row }
  try {
    const logRes = await getProblemLogs(row.id)
    logs.value = logRes.data || []
  } catch { logs.value = [] }
  detailVisible.value = true
}

// ===== 批量关闭 =====
const closeVisible = ref(false)
const closeForm = reactive({ reason: '' })
const showBatchClose = () => {
  closeForm.reason = ''
  closeVisible.value = true
}
const submitBatchClose = async () => {
  if (!closeForm.reason.trim()) { ElMessage.warning('请填写关闭原因'); return }
  submitting.value = true
  try {
    await closeProblem({ ids: selectedIds.value, reason: closeForm.reason })
    ElMessage.success('批量关闭成功')
    closeVisible.value = false
    selectedIds.value = []
    search()
  } catch (e) { ElMessage.error('关闭失败: ' + (e?.message || '')) }
  finally { submitting.value = false }
}

// ===== 导出 =====
const exportData = async () => {
  try {
    const params = {}
    Object.keys(query).forEach(k => { if (query[k] !== '' && query[k] != null && k !== 'pageNum' && k !== 'pageSize') params[k] = query[k] })
    const res = await exportProblemLedger(params)
    const url = window.URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url; a.download = '问题台账.xlsx'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

// ===== 日志辅助 =====
const logTypeLabel = (type) => {
  const m = { warn: '预警', update: '修改', dispatch: '派发', process: '处理', close: '关闭', merge: '合并' }
  return m[type] || type
}
const logTypeColor = (type) => {
  const m = { warn: '#E6A23C', update: '#409EFF', dispatch: '#67C23A', process: '#409EFF', close: '#909399', merge: '#F56C6C' }
  return m[type] || '#909399'
}

onMounted(search)
</script>

<style scoped>
.log-title { font-weight: 600; margin-bottom: 4px; }
.log-content { color: #606266; font-size: 13px; line-height: 1.5; }
</style>
