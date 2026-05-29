<template>
  <div>
    <div class="page-title">行政处罚案件管理</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="案号"><el-input v-model="query.caseNo" placeholder="请输入" clearable style="width:180px" /></el-form-item>
        <el-form-item label="案件状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:130px">
            <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处罚类型">
          <el-select v-model="query.penaltyType" placeholder="全部" clearable style="width:130px">
            <el-option v-for="o in penaltyTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <div style="margin-bottom:12px;display:flex;justify-content:space-between">
        <el-button type="primary" @click="openFile">立案登记</el-button>
        <el-button type="danger" :disabled="!selectedIds.length" @click="batchDelete">批量删除</el-button>
      </div>
      <el-table :data="list" stripe v-loading="loading" @selection-change="onSelect">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="caseNo" label="案号" width="180" />
        <el-table-column prop="taskTitle" label="关联任务" min-width="160" show-overflow-tooltip />
        <el-table-column prop="enterpriseName" label="关联企业" width="160" show-overflow-tooltip />
        <el-table-column label="处罚类型" width="110">
          <template #default="{ row }">
            <el-tag :type="penaltyTypeTag[row.penaltyType] || ''" size="small">{{ penaltyTypeMap[row.penaltyType] || row.penaltyType || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="罚款金额" width="110" align="right">
          <template #default="{ row }">{{ row.penaltyAmount ? '¥' + formatAmount(row.penaltyAmount) : '-' }}</template>
        </el-table-column>
        <el-table-column label="案件状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag[row.status] || ''" size="small">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="立案人" width="100" />
        <el-table-column label="立案时间" width="160">
          <template #default="{ row }">{{ row.createTime ? formatTime(row.createTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status !== 'CLOSED'" type="warning" link @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'INVESTIGATING'" type="success" link @click="openRule(row)">裁决</el-button>
            <el-button v-if="row.status === 'PENALIZED'" type="info" link @click="handleClose(row)">结案</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
    </div>

    <!-- 立案登记/编辑弹框 -->
    <el-dialog v-model="fileVisible" :title="editingId ? '编辑案件' : '行政处罚立案登记'" width="720px" append-to-body destroy-on-close>
      <el-form :model="form" label-width="110px" ref="formRef">
        <template v-if="!editingId">
          <el-form-item label="关联任务ID" prop="taskId">
            <el-input-number v-model="form.taskId" :min="1" placeholder="任务ID" style="width:220px" controls-position="right" />
            <span style="margin-left:8px;color:#909399;font-size:12px">选填，自动关联问题与企业</span>
          </el-form-item>
        </template>
        <el-form-item label="处罚类型" prop="penaltyType" :rules="[{ required: true, message: '请选择处罚类型' }]">
          <el-select v-model="form.penaltyType" placeholder="请选择" style="width:100%">
            <el-option v-for="o in penaltyTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="罚款金额（元）" v-if="form.penaltyType === 'FINE'">
          <el-input-number v-model="form.penaltyAmount" :min="0" :precision="2" :step="1000" style="width:220px" controls-position="right" placeholder="0.00" />
        </el-form-item>
        <el-form-item label="案件描述" prop="caseDesc" :rules="[{ required: true, message: '请输入案件描述' }]">
          <el-input v-model="form.caseDesc" type="textarea" :rows="3" placeholder="请输入案件简要描述" />
        </el-form-item>
        <el-form-item label="法律依据">
          <el-input v-model="form.legalBasis" type="textarea" :rows="2" placeholder="请输入适用的法律法规条款" />
        </el-form-item>
        <el-form-item label="处罚内容">
          <el-input v-model="form.penaltyContent" type="textarea" :rows="3" placeholder="请输入具体处罚措施" />
        </el-form-item>
        <el-form-item label="听证时间" v-if="editingId">
          <el-date-picker v-model="form.hearingDate" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="案件状态" v-if="editingId">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="o in editableStatusOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fileVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitFile">{{ editingId ? '保存' : '确认立案' }}</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹框 -->
    <el-dialog v-model="detailVisible" title="案件详情" width="720px" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="案号">{{ currentRow.caseNo }}</el-descriptions-item>
        <el-descriptions-item label="案件状态">
          <el-tag :type="statusTag[currentRow.status] || ''" size="small">{{ statusMap[currentRow.status] || currentRow.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处罚类型">
          <el-tag :type="penaltyTypeTag[currentRow.penaltyType] || ''" size="small">{{ penaltyTypeMap[currentRow.penaltyType] || currentRow.penaltyType || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="罚款金额">{{ currentRow.penaltyAmount ? '¥' + formatAmount(currentRow.penaltyAmount) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联任务">{{ currentRow.taskTitle || currentRow.taskId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联企业">{{ currentRow.enterpriseName || currentRow.enterpriseId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="立案人">{{ currentRow.applicantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="立案时间">{{ currentRow.createTime ? formatTime(currentRow.createTime) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="听证时间">{{ currentRow.hearingDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="裁决时间">{{ currentRow.rulingDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="案件描述" :span="2">{{ currentRow.caseDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="法律依据" :span="2">{{ currentRow.legalBasis || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处罚内容" :span="2">{{ currentRow.penaltyContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="裁决结果" :span="2">{{ currentRow.rulingResult || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 裁决弹框 -->
    <el-dialog v-model="ruleVisible" title="作出处罚裁决" width="560px" append-to-body destroy-on-close>
      <el-form :model="ruleForm" label-width="100px">
        <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
          <el-descriptions-item label="案号">{{ currentRow.caseNo }}</el-descriptions-item>
          <el-descriptions-item label="处罚类型">{{ penaltyTypeMap[currentRow.penaltyType] || currentRow.penaltyType }}</el-descriptions-item>
          <el-descriptions-item label="关联企业" :span="2">{{ currentRow.enterpriseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="案件描述" :span="2">{{ currentRow.caseDesc || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-form-item label="裁决结果" prop="rulingResult" :rules="[{ required: true, message: '请输入裁决结果' }]">
          <el-input v-model="ruleForm.rulingResult" type="textarea" :rows="4" placeholder="请输入裁决结果，包括处罚决定、执行方式、期限等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleVisible = false">取消</el-button>
        <el-button type="primary" :loading="rulingSubmitting" @click="submitRule">确认裁决</el-button>
      </template>
    </el-dialog>

    <!-- 结案确认弹框 -->
    <el-dialog v-model="closeVisible" width="480px" align-center :show-close="false" append-to-body destroy-on-close>
      <div class="confirm-header">
        <el-icon color="#E6A23C" :size="24"><WarningFilled /></el-icon>
        <span class="confirm-title">确认结案</span>
      </div>
      <div class="confirm-info-card">
        <div class="info-row"><span class="info-label">案号</span><span class="info-value">{{ closeRow.caseNo }}</span></div>
        <div class="info-row"><span class="info-label">处罚类型</span><span class="info-value">{{ penaltyTypeMap[closeRow.penaltyType] || closeRow.penaltyType || '-' }}</span></div>
        <div class="info-row"><span class="info-label">案件状态</span><span class="info-value">{{ statusMap[closeRow.status] || closeRow.status }}</span></div>
        <div class="info-row"><span class="info-label">罚款金额</span><span class="info-value">{{ closeRow.penaltyAmount ? '¥' + formatAmount(closeRow.penaltyAmount) : '-' }}</span></div>
      </div>
      <div class="confirm-alert">
        <el-icon color="#F56C6C" :size="20"><WarningFilled /></el-icon>
        <div class="alert-content">
          <div class="alert-title">此操作不可撤销，结案后案件将归档！</div>
          <div class="alert-desc">注：已作出的处罚决定不受影响，仍可查看详情。</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmClose">确认结案</el-button>
      </template>
    </el-dialog>

    <!-- 批量删除确认弹框 -->
    <el-dialog v-model="deleteVisible" width="480px" align-center :show-close="false" append-to-body destroy-on-close>
      <div class="confirm-header">
        <el-icon color="#E6A23C" :size="24"><WarningFilled /></el-icon>
        <span class="confirm-title">删除案件</span>
      </div>
      <div class="confirm-info-card">
        <div class="info-row"><span class="info-label">选中数量</span><span class="info-value">{{ selectedIds.length }} 个案件</span></div>
        <div class="info-row"><span class="info-label">案号列表</span><span class="info-value">{{ selectedCases }}</span></div>
      </div>
      <div class="confirm-alert">
        <el-icon color="#F56C6C" :size="20"><WarningFilled /></el-icon>
        <div class="alert-content">
          <div class="alert-title">此操作不可撤销，删除后案件将永久移除！</div>
          <div class="alert-desc">注：关联的任务和问题记录将保留不受影响。</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="deleteVisible = false">取消</el-button>
        <el-button type="danger" :loading="deleteSubmitting" @click="confirmDelete">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPenaltyList, filePenalty, updatePenalty, getPenaltyDetail, rulePenalty, closePenalty, deletePenalty } from '@/api'

const route = useRoute()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ caseNo: '', status: '', penaltyType: '', pageNum: 1, pageSize: 10 })

const statusOptions = [
  { label: '立案', value: 'FILED' },
  { label: '调查中', value: 'INVESTIGATING' },
  { label: '已处罚', value: 'PENALIZED' },
  { label: '结案', value: 'CLOSED' }
]
const statusMap = Object.fromEntries(statusOptions.map(o => [o.value, o.label]))
const statusTag = { FILED: 'warning', INVESTIGATING: 'info', PENALIZED: 'danger', CLOSED: 'success' }

const penaltyTypeOptions = [
  { label: '警告', value: 'WARNING' },
  { label: '罚款', value: 'FINE' },
  { label: '停产整顿', value: 'SHUTDOWN' },
  { label: '关闭', value: 'CLOSE' }
]
const penaltyTypeMap = Object.fromEntries(penaltyTypeOptions.map(o => [o.value, o.label]))
const penaltyTypeTag = { WARNING: 'warning', FINE: 'danger', SHUTDOWN: '', CLOSE: 'danger' }

const selectedIds = ref([])
const onSelect = (rows) => { selectedIds.value = rows.map(r => r.id) }

const fetch = async () => {
  loading.value = true
  try {
    const res = await getPenaltyList({ ...query })
    const d = res.data || {}
    list.value = d.records || []
    total.value = Number(d.total) || 0
    selectedIds.value = []
  } catch { list.value = []; total.value = 0 }
  loading.value = false
}
const search = () => { query.pageNum = 1; fetch() }
const reset = () => { query.caseNo = ''; query.status = ''; query.penaltyType = ''; search() }

// ===== 立案/编辑 =====
const fileVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ taskId: null, penaltyType: '', penaltyAmount: null, caseDesc: '', legalBasis: '', penaltyContent: '', hearingDate: '', status: '' })
const editableStatusOptions = [
  { label: '立案', value: 'FILED' },
  { label: '调查中', value: 'INVESTIGATING' }
]

const openFile = () => {
  editingId.value = null
  resetForm()
  fileVisible.value = true
}

const openEdit = async (row) => {
  editingId.value = row.id
  try {
    const res = await getPenaltyDetail(row.id)
    const d = res.data || row
    form.taskId = d.taskId || null
    form.penaltyType = d.penaltyType || ''
    form.penaltyAmount = d.penaltyAmount || null
    form.caseDesc = d.caseDesc || ''
    form.legalBasis = d.legalBasis || ''
    form.penaltyContent = d.penaltyContent || ''
    form.hearingDate = d.hearingDate || ''
    form.status = d.status || ''
  } catch {
    Object.assign(form, {
      penaltyType: row.penaltyType || '',
      penaltyAmount: row.penaltyAmount || null,
      caseDesc: row.caseDesc || '',
      legalBasis: row.legalBasis || '',
      penaltyContent: row.penaltyContent || '',
      status: row.status || ''
    })
  }
  fileVisible.value = true
}

const resetForm = () => {
  form.taskId = null; form.penaltyType = ''; form.penaltyAmount = null
  form.caseDesc = ''; form.legalBasis = ''; form.penaltyContent = ''
  form.hearingDate = ''; form.status = ''
  formRef.value?.resetFields()
}

const submitFile = async () => {
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    if (editingId.value) {
      await updatePenalty(editingId.value, { ...form, taskId: undefined })
      ElMessage.success('保存成功')
    } else {
      await filePenalty({ ...form })
      ElMessage.success('立案成功')
    }
    fileVisible.value = false
    fetch()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally { submitting.value = false }
}

// ===== 详情 =====
const detailVisible = ref(false)
const currentRow = ref({})

const openDetail = async (row) => {
  currentRow.value = { ...row }
  try {
    const res = await getPenaltyDetail(row.id)
    if (res.data) currentRow.value = res.data
  } catch {}
  detailVisible.value = true
}

// ===== 裁决 =====
const ruleVisible = ref(false)
const rulingSubmitting = ref(false)
const ruleForm = reactive({ rulingResult: '' })

const openRule = (row) => {
  currentRow.value = row
  ruleForm.rulingResult = ''
  ruleVisible.value = true
}

const submitRule = async () => {
  if (!ruleForm.rulingResult.trim()) { ElMessage.warning('请输入裁决结果'); return }
  rulingSubmitting.value = true
  try {
    await rulePenalty(currentRow.value.id, ruleForm.rulingResult.trim())
    ElMessage.success('裁决已作出')
    ruleVisible.value = false
    fetch()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '裁决失败')
  } finally { rulingSubmitting.value = false }
}

// ===== 结案 =====
const closeVisible = ref(false)
const closeRow = ref({})

const handleClose = (row) => {
  closeRow.value = row
  closeVisible.value = true
}

const confirmClose = async () => {
  try {
    await closePenalty(closeRow.value.id)
    ElMessage.success('已结案')
    closeVisible.value = false
    fetch()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '结案失败')
  }
}

// ===== 批量删除 =====
const deleteVisible = ref(false)
const deleteSubmitting = ref(false)
const selectedCases = computed(() => {
  const rows = list.value.filter(r => selectedIds.value.includes(r.id))
  const caseNos = rows.map(r => r.caseNo).filter(Boolean)
  return caseNos.length ? caseNos.join('、') : '-'
})

const batchDelete = () => {
  if (!selectedIds.value.length) return
  deleteVisible.value = true
}

const confirmDelete = async () => {
  deleteSubmitting.value = true
  try {
    await deletePenalty(selectedIds.value)
    ElMessage.success('删除成功')
    deleteVisible.value = false
    fetch()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '删除失败')
  } finally {
    deleteSubmitting.value = false
  }
}

const formatAmount = (v) => (Number(v) || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
const formatTime = (v) => v ? v.replace('T', ' ').substring(0, 19) : '-'

// 从路由参数预填任务ID
onMounted(() => {
  const tid = route.query.taskId
  if (tid) {
    form.taskId = Number(tid)
    openFile()
  }
  fetch()
})
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 16px; color: #303133; }
.search-bar { margin-bottom: 16px; padding: 16px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.table-card { padding: 16px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }

/* 确认弹窗样式 */
.confirm-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.confirm-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}
.confirm-info-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
}
.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}
.info-row:last-child {
  border-bottom: none;
}
.info-label {
  width: 80px;
  color: #909399;
  font-size: 14px;
  flex-shrink: 0;
}
.info-value {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  word-break: break-all;
}
.confirm-alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #fef0f0;
  border-left: 4px solid #f56c6c;
  border-radius: 4px;
  padding: 12px 14px;
  margin-bottom: 8px;
}
.alert-content {
  flex: 1;
}
.alert-title {
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
  margin-bottom: 4px;
}
.alert-desc {
  font-size: 13px;
  color: #c45656;
}
</style>
