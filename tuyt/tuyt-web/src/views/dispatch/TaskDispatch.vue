<template>
  <div>
    <div class="page-title">任务调度</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="任务编号"><el-input v-model="query.taskNo" clearable style="width:140px" /></el-form-item>
        <el-form-item label="任务标题"><el-input v-model="query.title" clearable style="width:140px" /></el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="query.taskType" clearable placeholder="全部" style="width:120px">
            <el-option v-for="opt in taskTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:100px">
            <el-option label="已拟定" value="DRAFT" />
            <el-option label="已派发" value="DISPATCHED" />
            <el-option label="已签收" value="SIGNED" />
            <el-option label="已完成" value="DONE" />
            <el-option label="已撤销" value="REVOKED" />
            <el-option label="已退回" value="RETURNED" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-select v-model="query.urgency" clearable placeholder="全部" style="width:100px">
            <el-option label="一般" value="NORMAL" />
            <el-option label="紧急" value="URGENT" />
            <el-option label="非常紧急" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="超期类型">
          <el-select v-model="query.overdueType" clearable placeholder="全部" style="width:120px">
            <el-option label="超期任务" value="overdue" />
            <el-option label="即将超期 (24h)" value="nearly" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="success" @click="openDispatchDialog()"><el-icon><Plus /></el-icon>派发任务</el-button>
          <el-button type="warning" :disabled="selectedRows.length===0" @click="openBatchUrge">批量催办</el-button>
          <el-button type="info" :icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无任务" @selection-change="handleSelection">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="taskNo" label="任务单号" width="160" />
        <el-table-column prop="taskTitle" label="任务标题" min-width="200" />
        <el-table-column prop="taskType" label="任务类型" width="100">
          <template #default="{ row }">{{ taskTypeMap[row.taskType] || row.taskType }}</template>
        </el-table-column>
        <el-table-column label="紧急程度" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.urgency==='CRITICAL'" type="danger" size="small">非常紧急</el-tag>
            <el-tag v-else-if="row.urgency==='URGENT'" type="warning" size="small">紧急</el-tag>
            <span v-else style="color:#909399">一般</span>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="160">
          <template #default="{ row }">
            <span :style="{ color: isTaskOverdue(row) ? '#F56C6C' : '' }">{{ row.deadline || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="处理人" width="100">
          <template #default="{ row }">{{ row.handlerId ? '用户' + row.handlerId : '-' }}</template>
        </el-table-column>
        <el-table-column label="处理单位" width="120">
          <template #default="{ row }">{{ row.gridName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ taskStatusMap[row.status] || row.status }}</template>
        </el-table-column>
        <el-table-column label="催办/督办" width="120" align="center">
          <template #default="{ row }">
            <div style="display:flex;flex-direction:column;gap:2px;align-items:center">
              <el-tag v-if="(row.urgeCount||0) > 0" type="warning" size="small">催办 {{ row.urgeCount }}次</el-tag>
              <el-tag v-if="(row.superviseCount||0) > 0" type="danger" size="small">督办 {{ row.superviseCount }}次</el-tag>
              <span v-if="!(row.urgeCount||0) && !(row.superviseCount||0)" style="color:#C0C4CC">—</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status==='DRAFT'" link type="success" @click="handlePublishTask(row)">发布</el-button>
            <el-button v-if="row.status==='DRAFT'" link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="['DISPATCHED','SIGNED'].includes(row.status)" link type="warning" @click="handleUrge(row)">催办</el-button>
            <el-button v-if="['DISPATCHED','SIGNED'].includes(row.status)" link type="danger" @click="handleSupervise(row)">督办</el-button>
            <el-button v-if="['DISPATCHED','SIGNED'].includes(row.status)" link type="danger" @click="handleRevoke(row)">撤销</el-button>
            <el-button v-if="row.status==='DRAFT'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetchData" @size-change="fetchData" />
    </div>

    <!-- 派发任务弹框 -->
    <el-dialog v-model="dispatchDialogVisible" title="派发任务" width="650px">
      <el-alert title="任务提交后，将立即派发给处理人员，请仔细填写！" type="warning" :closable="false" show-icon style="margin-bottom:16px" />
      <el-form label-width="100px" :model="dispatchForm">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="任务标题"><el-input v-model="dispatchForm.title" placeholder="请输入任务标题" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务类型">
              <el-select v-model="dispatchForm.taskType" style="width:100%">
                <el-option v-for="opt in taskTypeOptions.filter(o => ['PATROL','SPECIAL','CHECK','COMPLAINT','EMERGENCY','RECTIFY','RECHECK'].includes(o.value))" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="紧急程度">
              <el-select v-model="dispatchForm.urgency" style="width:100%">
                <el-option label="一般" value="NORMAL" />
                <el-option label="紧急" value="URGENT" />
                <el-option label="非常紧急" value="CRITICAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理期限">
              <el-date-picker v-model="dispatchForm.deadline" type="datetime" placeholder="选择截止时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="处理单位">
              <el-select v-model="dispatchForm.gridId" style="width:100%" placeholder="请选择网格">
                <el-option v-for="g in cityGrids" :key="g.id" :label="g.gridName" :value="g.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理人"><el-input v-model="dispatchForm.processor" placeholder="请输入处理人" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务内容">
          <el-input v-model="dispatchForm.content" type="textarea" :rows="3" placeholder="请详细描述任务内容..." />
        </el-form-item>
        <el-form-item label="关联问题">
          <el-input v-model="dispatchForm.problemId" placeholder="输入问题编号或ID（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialogVisible=false">取消</el-button>
        <el-button @click="handleSaveDraft">暂存</el-button>
        <el-button type="primary" @click="handleDispatch">提交派发</el-button>
      </template>
    </el-dialog>

    <!-- 编辑任务弹框 -->
    <el-dialog v-model="editDialogVisible" :title="editForm.originalStatus==='DRAFT'?'编辑草稿任务':'编辑已发布任务'" width="650px">
      <el-alert v-if="editForm.originalStatus==='DRAFT'" title="编辑草稿任务，保存后仍然为拟定状态，发布后将派发给处理人员。" type="info" :closable="false" show-icon style="margin-bottom:16px" />
      <el-alert v-else title="已发布任务仅允许修改截止时间、处理单位、任务内容等信息，状态保持不变。" type="warning" :closable="false" show-icon style="margin-bottom:16px" />
      <el-form label-width="100px" :model="editForm">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="任务标题"><el-input v-model="editForm.title" placeholder="请输入任务标题" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务类型">
              <el-select v-model="editForm.taskType" style="width:100%">
                <el-option v-for="opt in taskTypeOptions.filter(o => ['PATROL','SPECIAL','CHECK','COMPLAINT','EMERGENCY','RECTIFY','RECHECK'].includes(o.value))" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="紧急程度">
              <el-select v-model="editForm.urgency" style="width:100%">
                <el-option label="一般" value="NORMAL" />
                <el-option label="紧急" value="URGENT" />
                <el-option label="非常紧急" value="CRITICAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理期限">
              <el-date-picker v-model="editForm.deadline" type="datetime" placeholder="选择截止时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="处理单位">
              <el-select v-model="editForm.gridId" style="width:100%" placeholder="请选择网格">
                <el-option v-for="g in cityGrids" :key="g.id" :label="g.gridName" :value="g.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理人"><el-input v-model="editForm.processor" placeholder="请输入处理人" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务内容">
          <el-input v-model="editForm.content" type="textarea" :rows="3" placeholder="请详细描述任务内容..." />
        </el-form-item>
        <el-form-item label="关联问题">
          <el-input v-model="editForm.problemId" placeholder="输入问题编号或ID（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible=false">取消</el-button>
        <el-button v-if="editForm.originalStatus==='DRAFT'" @click="handleEditSave('DRAFT')" :loading="editSubmitting">保存草稿</el-button>
        <el-button type="primary" @click="handleEditSave(editForm.originalStatus==='DRAFT'?'DISPATCHED':editForm.originalStatus)" :loading="editSubmitting">
          {{ editForm.originalStatus==='DRAFT'?'保存并发布':'保存修改' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 任务详情弹框 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="750px">
      <template v-if="detailTask">
        <el-descriptions :column="2" border style="margin-bottom:16px">
          <el-descriptions-item label="任务单号">{{ detailTask.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="任务标题">{{ detailTask.taskTitle }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">{{ taskTypeMap[detailTask.taskType] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag v-if="detailTask.urgency==='CRITICAL'" type="danger" size="small">非常紧急</el-tag>
            <el-tag v-else-if="detailTask.urgency==='URGENT'" type="warning" size="small">紧急</el-tag>
            <span v-else style="color:#909399">一般</span>
          </el-descriptions-item>
          <el-descriptions-item label="处理人">{{ detailTask.handlerId ? '用户' + detailTask.handlerId : '-' }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ detailTask.deadline || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务状态">{{ taskStatusMap[detailTask.status] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="派发时间">{{ detailTask.dispatchTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理单位">{{ detailTask.gridName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="催办次数">
            <el-tag v-if="(detailTask.urgeCount||0) > 0" type="warning" size="small">{{ detailTask.urgeCount }} 次</el-tag>
            <span v-else style="color:#909399">无</span>
          </el-descriptions-item>
          <el-descriptions-item label="督办次数">
            <el-tag v-if="(detailTask.superviseCount||0) > 0" type="danger" size="small">{{ detailTask.superviseCount }} 次</el-tag>
            <span v-else style="color:#909399">无</span>
          </el-descriptions-item>
          <el-descriptions-item label="任务内容" :span="2">{{ detailTask.taskContent || '无' }}</el-descriptions-item>
        </el-descriptions>
        <!-- 催办/督办历史 -->
        <div v-if="urgeHistory.length || superviseHistory.length" style="margin-top:8px">
          <el-divider content-position="left">催办/督办记录</el-divider>
          <el-timeline>
            <el-timeline-item
              v-for="item in combinedHistory"
              :key="item.idx"
              :timestamp="item.time"
              :color="item.type === 'supervise' ? '#F56C6C' : '#E6A23C'"
              placement="top"
            >
              <el-tag :type="item.type === 'supervise' ? 'danger' : 'warning'" size="small" style="margin-bottom:4px">{{ item.label }}</el-tag>
              <div v-if="item.reason" style="color:#606266;font-size:13px">原因：{{ item.reason }}</div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </template>
    </el-dialog>

    <!-- 催办/督办 弹框 -->
    <el-dialog v-model="reasonDialogVisible" :title="reasonTitle" width="600px">
      <el-alert :title="reasonTitle + '——请确认任务信息后填写原因（必填）'" type="warning" :closable="false" show-icon style="margin-bottom:16px" />
      <!-- 任务简要信息 -->
      <el-descriptions v-if="reasonTargetTask" :column="2" border size="small" style="margin-bottom:16px">
        <el-descriptions-item label="任务单号">{{ reasonTargetTask.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">
          <el-tag v-if="reasonTargetTask.urgency==='CRITICAL'" type="danger" size="small">非常紧急</el-tag>
          <el-tag v-else-if="reasonTargetTask.urgency==='URGENT'" type="warning" size="small">紧急</el-tag>
          <span v-else style="color:#909399">一般</span>
        </el-descriptions-item>
        <el-descriptions-item label="任务标题" :span="2">{{ reasonTargetTask.taskTitle }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ taskTypeMap[reasonTargetTask.taskType] || reasonTargetTask.taskType }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">{{ taskStatusMap[reasonTargetTask.status] || reasonTargetTask.status }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ reasonTargetTask.handlerId ? '用户' + reasonTargetTask.handlerId : '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理单位">{{ reasonTargetTask.gridName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间" :span="2">
          <span :style="{ color: isTaskOverdue(reasonTargetTask) ? '#F56C6C' : '', fontWeight: isTaskOverdue(reasonTargetTask) ? 'bold' : '' }">
            {{ reasonTargetTask.deadline || '-' }}
            <el-tag v-if="isTaskOverdue(reasonTargetTask)" type="danger" size="small" style="margin-left:6px">已超期</el-tag>
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="已有催办" :span="1">
          <span v-if="(reasonTargetTask.urgeCount||0) > 0" style="color:#E6A23C;font-weight:bold">{{ reasonTargetTask.urgeCount }} 次</span>
          <span v-else style="color:#909399">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="已有督办" :span="1">
          <span v-if="(reasonTargetTask.superviseCount||0) > 0" style="color:#F56C6C;font-weight:bold">{{ reasonTargetTask.superviseCount }} 次</span>
          <span v-else style="color:#909399">无</span>
        </el-descriptions-item>
      </el-descriptions>
      <!-- 原因输入 -->
      <el-form label-width="90px" :model="reasonForm" :rules="reasonRules" ref="reasonFormRef">
        <el-form-item label="原因说明" prop="reason" required>
          <el-input v-model="reasonForm.reason" type="textarea" :rows="3" placeholder="请填写原因说明（必填，不少于5个字）" show-word-limit maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reasonDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="confirmReason">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 批量催办弹框 -->
    <el-dialog v-model="batchUrgeVisible" title="批量催办" width="500px" align-center>
      <el-alert :title="`已选择 ${selectedRows.length} 个任务，请填写催办原因（必填）`" type="warning" :closable="false" show-icon style="margin-bottom:16px" />
      <el-form label-width="90px" :model="reasonForm" :rules="reasonRules" ref="batchReasonFormRef">
        <el-form-item label="原因说明" prop="reason" required>
          <el-input v-model="reasonForm.reason" type="textarea" :rows="3" placeholder="请填写原因说明（必填，不少于5个字）" show-word-limit maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchUrgeVisible=false">取消</el-button>
        <el-button type="primary" @click="confirmBatchUrge">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, Plus, Download } from '@element-plus/icons-vue'
import { getTaskList, dispatchTask, updateTask, urgeTask, superviseTask, revokeTask, deleteTask, publishTask, exportTaskLedger, getGridList, getProblemDetail } from '@/api'
import { taskTypeMap, taskTypeOptions, taskStatusMap } from '@/utils/constants'

const route = useRoute()
const loading = ref(false), list = ref([]), total = ref(0), selectedRows = ref([])
const query = reactive({ taskNo: '', title: '', taskType: '', status: '', urgency: '', overdueType: '', pageNum: 1, pageSize: 10 })
const dispatchDialogVisible = ref(false)
const dispatchForm = reactive({ title: '', taskType: 'PATROL', urgency: 'NORMAL', gridId: null, processor: '', deadline: '', content: '', problemId: '' })
const detailVisible = ref(false), detailTask = ref(null)
const reasonDialogVisible = ref(false), reasonTitle = ref(''), reasonAction = ref('')
const reasonTargetId = ref(null), reasonTargetTask = ref(null)
const batchUrgeVisible = ref(false)
const reasonFormRef = ref(null), batchReasonFormRef = ref(null)
const reasonForm = reactive({ reason: '' })
const reasonRules = { reason: [{ required: true, message: '请填写原因说明（不少于5个字）', trigger: 'blur' }, { min: 5, message: '原因不少于5个字', trigger: 'blur' }] }
const cityGrids = ref([])

// 详情弹框用：催办/督办历史
const urgeHistory = ref([])
const superviseHistory = ref([])
const combinedHistory = computed(() => {
  const all = [
    ...urgeHistory.value.map((item, i) => ({ ...item, type: 'urge', label: '催办', idx: i })),
    ...superviseHistory.value.map((item, i) => ({ ...item, type: 'supervise', label: '督办', idx: 1000 + i }))
  ]
  all.sort((a, b) => b.time.localeCompare(a.time))
  return all
})

/** 从 taskContent 解析催办/督办历史 */
function parseUrgeHistory(content) {
  if (!content) return { urge: [], supervise: [] }
  const urge = [], supervise = []
  const urgeRegex = /=== 催办记录 \[([^\]]+)\] ===\n?(?:原因:\s*(.*?))?(?=\n===|\n?$)/gs
  const supRegex = /=== 督办记录 \[([^\]]+)\] ===\n?(?:原因:\s*(.*?))?(?=\n===|\n?$)/gs
  let m
  while ((m = urgeRegex.exec(content)) !== null) {
    urge.push({ time: m[1], reason: (m[2] || '').trim() })
  }
  while ((m = supRegex.exec(content)) !== null) {
    supervise.push({ time: m[1], reason: (m[2] || '').trim() })
  }
  return { urge, supervise }
}

const handleSelection = (rows) => { selectedRows.value = rows }
const isTaskOverdue = (row) => {
  if (!row.deadline || row.status === 'DONE') return false
  return new Date(row.deadline) < new Date()
}

const openDispatchDialog = async (preset) => {
  Object.assign(dispatchForm, { title: '', taskType: 'PATROL', urgency: 'NORMAL', gridId: null, processor: '', deadline: '', content: '', problemId: '' })
  const problemId = route.query.problemId || (preset && preset.problemId)
  if (problemId) {
    dispatchForm.problemId = problemId
    try {
      const r = await getProblemDetail(problemId)
      const p = r.data || {}
      dispatchForm.title = `处理${p.enterpriseName || ''} - ${(p.problemDesc || '').substring(0, 30)}`
      dispatchForm.content = `现场核查处理：${p.problemDesc || ''}`
    } catch { /* */ }
  }
  dispatchDialogVisible.value = true
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTaskList({ ...query })
    list.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total ?? list.value.length
  } catch { /* */ }
  finally { loading.value = false }
}
const search = () => { query.pageNum = 1; fetchData() }

const reset = () => {
  query.taskNo = ''; query.title = ''; query.taskType = ''; query.status = ''; query.urgency = ''; query.overdueType = ''
  search()
}

function buildDispatchPayload(form, status) {
  return {
    taskTitle: form.title,
    taskType: form.taskType,
    urgency: form.urgency,
    deadline: form.deadline,
    gridId: form.gridId ? Number(form.gridId) : null,
    taskContent: form.processor ? `处理人：${form.processor}\n${form.content || ''}` : (form.content || ''),
    problemId: form.problemId ? Number(form.problemId) : null,
    status
  }
}

const handleDispatch = async () => {
  try {
    await dispatchTask(buildDispatchPayload(dispatchForm, 'DISPATCHED'))
    ElMessage.success('任务已派发，处理人员将收到通知')
    dispatchDialogVisible.value = false
    search()
  } catch (e) { ElMessage.error(e?.response?.data?.message || '派发失败') }
}

const handleSaveDraft = async () => {
  try {
    await dispatchTask(buildDispatchPayload(dispatchForm, 'DRAFT'))
    ElMessage.success('已暂存为拟定任务')
    dispatchDialogVisible.value = false
    search()
  } catch (e) { ElMessage.error('暂存失败') }
}

const handlePublishTask = (row) => {
  if (row.status !== 'DRAFT') {
    ElMessage.warning('该任务已发布，无需重复操作')
    return
  }
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">任务编号</span><span class="del-value"><strong>${row.taskNo || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">任务标题</span><span class="del-value">${row.taskTitle || '-'}</span></div>
      <div class="del-row"><span class="del-label">任务类型</span><span class="del-value">${taskTypeMap[row.taskType] || row.taskType || '-'}</span></div>
      <div class="del-row"><span class="del-label">处理单位</span><span class="del-value">${row.gridName || '-'}</span></div>
    </div>
    <div class="action-info">
      <div class="action-info-title">发布后该任务将立即派发给处理人员！</div>
      <div class="action-info-note">注：发布后任务状态变为"已派发"，可进行催办、督办、撤销等操作。</div>
    </div>`,
    '发布任务',
    {
      confirmButtonText: '确认发布',
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
    try { await publishTask(row.id); ElMessage.success('已发布'); search() }
    catch (e) { ElMessage.error(e?.response?.data?.message || '发布失败') }
  }).catch(() => {})
}

// ===== 编辑任务 =====
const editDialogVisible = ref(false)
const editForm = reactive({ id: null, originalStatus: '', title: '', taskType: 'PATROL', urgency: 'NORMAL', gridId: null, processor: '', deadline: '', content: '', problemId: '' })
const editSubmitting = ref(false)

const openEditDialog = (row) => {
  editForm.id = row.id
  editForm.originalStatus = row.status || ''
  editForm.title = row.taskTitle || ''
  editForm.taskType = row.taskType || 'PATROL'
  editForm.urgency = row.urgency || 'NORMAL'
  editForm.gridId = row.gridId || null
  editForm.deadline = row.deadline || ''
  editForm.content = (row.taskContent || '').replace(/^处理人：.*\n/, '')
  editForm.problemId = row.problemId || ''
  editForm.processor = (row.taskContent || '').match(/处理人：(.*)/)?.[1] || ''
  editDialogVisible.value = true
}

const handleEditSave = async (status) => {
  editSubmitting.value = true
  try {
    const payload = buildDispatchPayload(editForm, status)
    await updateTask(editForm.id, payload)
    ElMessage.success(status === 'DRAFT' ? '已保存' : '已发布')
    editDialogVisible.value = false
    search()
  } catch (e) { ElMessage.error(e?.response?.data?.message || '操作失败') }
  finally { editSubmitting.value = false }
}

// ===== 查看详情 =====
const viewDetail = (row) => {
  detailTask.value = row
  const { urge, supervise } = parseUrgeHistory(row.taskContent)
  urgeHistory.value = urge; superviseHistory.value = supervise
  detailVisible.value = true
}

// ===== 催办/督办 弹框逻辑 =====
const openReasonDialog = (id, title, action, taskRow) => {
  reasonTargetId.value = id; reasonTitle.value = title; reasonAction.value = action
  reasonTargetTask.value = taskRow
  reasonForm.reason = ''
  reasonFormRef.value?.resetFields()
  reasonDialogVisible.value = true
}

const confirmReason = async () => {
  try {
    await reasonFormRef.value.validate()
  } catch { return }
  try {
    if (reasonAction.value === 'urge') {
      await urgeTask(reasonTargetId.value, { reason: reasonForm.reason.trim() })
      ElMessage.success('催办已发送，处理人将收到提醒')
    } else if (reasonAction.value === 'supervise') {
      await superviseTask(reasonTargetId.value, { reason: reasonForm.reason.trim() })
      ElMessage.success('督办已发送，处理人将收到严重提醒')
    }
    reasonDialogVisible.value = false
    search()
  } catch { ElMessage.error('操作失败') }
}

// ===== 批量催办 =====
const confirmBatchUrge = async () => {
  try {
    await batchReasonFormRef.value.validate()
  } catch { return }
  try {
    for (const id of selectedRows.value.map(r => r.id)) {
      await urgeTask(id, { reason: reasonForm.reason.trim() })
    }
    ElMessage.success(`已批量催办 ${selectedRows.value.length} 个任务`)
    batchUrgeVisible.value = false
    search()
  } catch { ElMessage.error('操作失败') }
}

const handleUrge = (row) => openReasonDialog(row.id, '催办任务', 'urge', row)
const handleSupervise = (row) => openReasonDialog(row.id, '督办任务', 'supervise', row)
const openBatchUrge = () => {
  if (selectedRows.value.length === 0) { ElMessage.warning('请先选择任务'); return }
  reasonForm.reason = ''
  batchReasonFormRef.value?.resetFields()
  batchUrgeVisible.value = true
}

const handleRevoke = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">任务编号</span><span class="del-value"><strong>${row.taskNo || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">任务标题</span><span class="del-value">${row.taskTitle || '-'}</span></div>
      <div class="del-row"><span class="del-label">当前状态</span><span class="del-value">${taskStatusMap[row.status] || row.status || '-'}</span></div>
      <div class="del-row"><span class="del-label">处理单位</span><span class="del-value">${row.gridName || '-'}</span></div>
    </div>
    <div class="action-info">
      <div class="action-info-title">撤销后该任务将退回至"已拟定"状态，可重新编辑或发布。</div>
      <div class="action-info-note">注：已产生的催办/督办记录将保留不受影响。</div>
    </div>`,
    '撤销任务',
    {
      confirmButtonText: '确认撤销',
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
    try { await revokeTask(row.id); ElMessage.success('已撤销'); search() } catch { ElMessage.error('撤销失败') }
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">任务编号</span><span class="del-value"><strong>${row.taskNo || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">任务标题</span><span class="del-value">${row.taskTitle || '-'}</span></div>
      <div class="del-row"><span class="del-label">任务类型</span><span class="del-value">${taskTypeMap[row.taskType] || row.taskType || '-'}</span></div>
      <div class="del-row"><span class="del-label">处理单位</span><span class="del-value">${row.gridName || '-'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该拟定任务将永久移除！</div>
      <div class="del-warning-note">注：仅"已拟定"状态的任务可删除。</div>
    </div>`,
    '删除任务',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
      draggable: false,
      center: true,
      appendTo: document.body,
      customClass: 'assess-del-dialog',
      closeOnClickModal: false,
      closeOnPressEscape: false
    }
  ).then(async () => {
    try { await deleteTask([row.id]); ElMessage.success('已删除'); search() } catch (e) { ElMessage.error(e?.response?.data?.message || '删除失败') }
  }).catch(() => {})
}

const handleExport = async () => {
  try {
    const res = await exportTaskLedger(query)
    // 创建 Blob 并触发下载
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '任务台账_' + new Date().toISOString().slice(0, 10) + '.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功，文件正在下载')
  } catch { ElMessage.error('导出失败，请稍后重试') }
}

onMounted(async () => {
  try {
    const r = await getGridList({ gridLevel: 1, pageSize: 100 })
    cityGrids.value = (r.data && r.data.records) ? r.data.records : []
  } catch { /* */ }
  if (route.query.problemId || route.query.problemIds) {
    openDispatchDialog({ problemId: route.query.problemId || route.query.problemIds?.split(',')[0] })
  }
  search()
})
</script>
