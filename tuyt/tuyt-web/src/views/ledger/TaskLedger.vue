<template>
  <div>
    <div class="page-title">任务台账</div>

    <!-- 查询条件 -->
    <div class="search-bar">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="任务单号">
          <el-input v-model="query.taskNo" clearable placeholder="模糊查询" style="width:140px" />
        </el-form-item>
        <el-form-item label="任务标题">
          <el-input v-model="query.title" clearable placeholder="任务标题" style="width:140px" />
        </el-form-item>
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
            <el-option label="特急" value="CRITICAL" />
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
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮栏 -->
    <div style="margin-bottom:12px;display:flex;gap:8px;flex-wrap:wrap">
      <el-button type="primary" @click="openAdd">新增任务</el-button>
      <el-button type="warning" :disabled="selectedIds.length===0" @click="showBatchUrge">批量催办 ({{ selectedIds.length }})</el-button>
      <el-button type="danger" :disabled="selectedIds.length===0" @click="showBatchSupervise">批量督办 ({{ selectedIds.length }})</el-button>
      <el-button type="danger" plain :disabled="selectedIds.length===0" @click="showBatchRevoke">批量撤销 ({{ selectedIds.length }})</el-button>
      <el-button :disabled="selectedIds.length===0" @click="showBatchDelete">批量删除 ({{ selectedIds.length }})</el-button>
      <el-button type="success" @click="exportData" :loading="exporting">导出 Excel</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading" @selection-change="onSelectionChange" ref="tableRef">
        <el-table-column type="selection" width="45" />
        <el-table-column label="任务单号" width="170">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">{{ row.taskNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="任务标题" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">{{ row.taskTitle }}</el-button>
          </template>
        </el-table-column>
        <el-table-column label="任务类型" width="100">
          <template #default="{ row }">{{ taskTypeMap[row.taskType] || row.taskType }}</template>
        </el-table-column>
        <el-table-column label="紧急程度" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.urgency==='CRITICAL'" type="danger" size="small">特急</el-tag>
            <el-tag v-else-if="row.urgency==='URGENT'" type="warning" size="small">紧急</el-tag>
            <span v-else style="color:#909399">一般</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150" />
        <el-table-column prop="deadline" label="截止时间" width="150" />
        <el-table-column prop="dispatchTime" label="派发时间" width="150" />
        <el-table-column label="处理单位" width="120">
          <template #default="{ row }">{{ row.gridName || '-' }}</template>
        </el-table-column>
        <el-table-column label="处理人" width="100">
          <template #default="{ row }">{{ row.handlerId ? '用户' + row.handlerId : '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="taskStatusTagType(row.status)" size="small">{{ taskStatusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="finishTime" label="完成时间" width="150" />
        <el-table-column label="催/督" width="100" align="center">
          <template #default="{ row }">
            <div style="display:flex;flex-direction:column;gap:2px;align-items:center">
              <el-tag v-if="(row.urgeCount||0) > 0" type="warning" size="small">催{{ row.urgeCount }}次</el-tag>
              <el-tag v-if="(row.superviseCount||0) > 0" type="danger" size="small">督{{ row.superviseCount }}次</el-tag>
              <span v-if="!(row.urgeCount||0) && !(row.superviseCount||0)" style="color:#C0C4CC">—</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">查看</el-button>
            <template v-if="row.status === 'DRAFT'">
              <el-button type="primary" link size="small" @click="openEdit(row)">修改</el-button>
              <el-button type="success" link size="small" @click="handlePublish(row)">发布</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </template>
            <template v-else-if="row.status === 'DISPATCHED' || row.status === 'SIGNED'">
              <el-button type="primary" link size="small" @click="openEdit(row)">修改</el-button>
              <el-button type="warning" link size="small" @click="handleUrge(row)">催办</el-button>
              <el-button type="danger" link size="small" @click="handleSupervise(row)">督办</el-button>
              <el-button type="danger" link size="small" @click="handleRevoke(row)">撤销</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50,100]" :total="total"
        layout="total, sizes, prev, pager, next" @current-change="fetchData" @size-change="fetchData" />
    </div>

    <!-- ===== 新增/编辑任务对话框 ===== -->
    <el-dialog v-model="editVisible" :title="isEdit ? '编辑任务' : '新增任务'" width="650px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="100px" ref="editFormRef">
        <el-form-item label="任务单号" v-if="isEdit">
          <el-input :model-value="editForm.taskNo" disabled />
        </el-form-item>
        <el-form-item label="任务标题" required>
          <el-input v-model="editForm.taskTitle" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="editForm.taskType" style="width:100%">
            <el-option v-for="o in taskTypeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-select v-model="editForm.urgency" style="width:100%">
            <el-option label="一般" value="NORMAL" />
            <el-option label="紧急" value="URGENT" />
            <el-option label="特急" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理单位">
          <el-select v-model="editForm.handlerUnitId" style="width:100%" filterable clearable placeholder="选择处理网格">
            <el-option v-for="g in gridOptions" :key="g.id" :label="g.gridName" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="editForm.deadline" type="datetime" placeholder="选择截止时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="任务内容">
          <el-input v-model="editForm.taskContent" type="textarea" :rows="4" placeholder="输入任务详细内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="info" @click="submitForm('DRAFT')" :loading="submitting">暂存</el-button>
        <el-button type="primary" @click="submitForm('DISPATCHED')" :loading="submitting">提交发布</el-button>
      </template>
    </el-dialog>

    <!-- ===== 催办对话框 ===== -->
    <el-dialog v-model="urgeVisible" title="催办任务" width="480px" :close-on-click-modal="false">
      <el-alert type="warning" :closable="false" style="margin-bottom:12px">
        任务单号：<b>{{ urgeForm.taskNo }}</b> &nbsp; 标题：<b>{{ urgeForm.taskTitle }}</b>
      </el-alert>
      <el-form :model="urgeForm" label-width="80px">
        <el-form-item label="催办原因" required>
          <el-input v-model="urgeForm.reason" type="textarea" :rows="3" placeholder="请填写催办原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="urgeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUrge" :loading="submitting">确认催办</el-button>
      </template>
    </el-dialog>

    <!-- ===== 督办对话框 ===== -->
    <el-dialog v-model="superviseVisible" title="督办任务" width="480px" :close-on-click-modal="false">
      <el-alert type="warning" :closable="false" style="margin-bottom:12px">
        任务单号：<b>{{ superviseForm.taskNo }}</b> &nbsp; 标题：<b>{{ superviseForm.taskTitle }}</b>
      </el-alert>
      <el-form :model="superviseForm" label-width="80px">
        <el-form-item label="督办原因" required>
          <el-input v-model="superviseForm.reason" type="textarea" :rows="3" placeholder="请填写督办原因（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="superviseVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSupervise" :loading="submitting">确认督办</el-button>
      </template>
    </el-dialog>

    <!-- ===== 撤销对话框 ===== -->
    <el-dialog v-model="revokeVisible" title="撤销任务" width="480px" :close-on-click-modal="false">
      <el-alert type="error" :closable="false" style="margin-bottom:12px">
        任务单号：<b>{{ revokeForm.taskNo }}</b> &nbsp; 标题：<b>{{ revokeForm.taskTitle }}</b>
      </el-alert>
      <p style="color:#E6A23C">是否确认撤销该任务？撤销后任务状态将变为<el-tag type="info" size="small">已撤销</el-tag>。</p>
      <el-form :model="revokeForm" label-width="80px" style="margin-top:16px">
        <el-form-item label="撤销原因">
          <el-input v-model="revokeForm.reason" type="textarea" :rows="2" placeholder="请填写撤销原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="revokeVisible = false">取消</el-button>
        <el-button type="danger" @click="submitRevoke" :loading="submitting">确认撤销</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情对话框 ===== -->
    <el-dialog v-model="detailVisible" title="任务详情" width="750px" :close-on-click-modal="false">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="任务单号">{{ detail.taskNo }}</el-descriptions-item>
            <el-descriptions-item label="任务标题">{{ detail.taskTitle }}</el-descriptions-item>
            <el-descriptions-item label="任务类型">{{ taskTypeMap[detail.taskType] || detail.taskType }}</el-descriptions-item>
            <el-descriptions-item label="紧急程度">
              <el-tag v-if="detail.urgency==='CRITICAL'" type="danger" size="small">特急</el-tag>
              <el-tag v-else-if="detail.urgency==='URGENT'" type="warning" size="small">紧急</el-tag>
              <span v-else>一般</span>
            </el-descriptions-item>
            <el-descriptions-item label="任务状态">
              <el-tag :type="taskStatusTagType(detail.status)" size="small">{{ taskStatusMap[detail.status] || detail.status }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="处理单位">{{ detail.gridName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ detail.startTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="截止时间">{{ detail.deadline || '-' }}</el-descriptions-item>
            <el-descriptions-item label="派发时间">{{ detail.dispatchTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ detail.finishTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理人">{{ detail.handlerId ? '用户' + detail.handlerId : '-' }}</el-descriptions-item>
            <el-descriptions-item label="发起人">{{ detail.initiatorId ? '用户' + detail.initiatorId : '-' }}</el-descriptions-item>
            <el-descriptions-item label="催办次数">{{ detail.urgeCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="督办次数">{{ detail.superviseCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="任务内容" :span="2">
              <pre style="white-space:pre-wrap;font-size:13px;color:#333;margin:0">{{ detail.taskContent || '-' }}</pre>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="任务动态" name="dynamic">
          <div v-if="detailLogs.length > 0">
            <el-timeline>
              <el-timeline-item v-for="(log, idx) in detailLogs" :key="idx"
                :timestamp="log.time" :color="log.color" placement="top">
                <div style="font-weight:600;margin-bottom:4px">{{ log.title }}</div>
                <div style="color:#606266;font-size:13px;line-height:1.5;white-space:pre-wrap">{{ log.content }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>
          <el-empty v-else description="暂无任务动态" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskList, getTaskDetail, dispatchTask, updateTask, urgeTask, superviseTask, deleteTask, publishTask, revokeTask, exportTaskLedger } from '@/api'
import { getGridList } from '@/api'
import { taskTypeMap, taskTypeOptions, taskStatusMap } from '@/utils/constants'

const loading = ref(false), exporting = ref(false), submitting = ref(false)
const list = ref([]), total = ref(0)
const tableRef = ref(null), selectedIds = ref([])
const gridOptions = ref([])

const query = reactive({
  taskNo: '', title: '', taskType: '', status: '', urgency: '', overdueType: '',
  pageNum: 1, pageSize: 10
})

// 表格多选
const onSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }

// 查询
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTaskList({ ...query })
    list.value = res.data?.records || res.data?.list || []
    total.value = Number(res.data?.total) || 0
  } catch { ElMessage.error('查询失败') }
  finally { loading.value = false }
}
const search = () => { query.pageNum = 1; fetchData() }

const reset = () => {
  query.taskNo = ''; query.title = ''; query.taskType = ''; query.status = ''; query.urgency = ''; query.overdueType = ''
  query.pageNum = 1
  search()
}

// 加载网格选项
const loadGridOptions = async () => {
  try {
    const res = await getGridList({ pageNum: 1, pageSize: 200 })
    gridOptions.value = res.data?.records || res.data?.list || []
  } catch { /* ignore */ }
}

// ===== 新增/编辑任务 =====
const editVisible = ref(false), isEdit = ref(false)
const editForm = reactive({
  id: null, taskNo: '', taskTitle: '', taskType: 'PATROL', urgency: 'NORMAL',
  handlerUnitId: null, deadline: '', taskContent: '', status: ''
})

const openAdd = () => {
  isEdit.value = false
  Object.assign(editForm, { id: null, taskNo: '', taskTitle: '', taskType: 'PATROL', urgency: 'NORMAL', handlerUnitId: null, deadline: '', taskContent: '', status: '' })
  editVisible.value = true
  if (gridOptions.value.length === 0) loadGridOptions()
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(editForm, {
    id: row.id, taskNo: row.taskNo, taskTitle: row.taskTitle || '',
    taskType: row.taskType || 'PATROL', urgency: row.urgency || 'NORMAL',
    handlerUnitId: row.handlerUnitId || row.gridId || null,
    deadline: row.deadline || '', taskContent: row.taskContent || '', status: row.status || ''
  })
  editVisible.value = true
  if (gridOptions.value.length === 0) loadGridOptions()
}

const submitForm = async (targetStatus) => {
  if (!editForm.taskTitle.trim()) { ElMessage.warning('请输入任务标题'); return }
  submitting.value = true
  try {
    const data = {
      taskTitle: editForm.taskTitle.trim(),
      taskType: editForm.taskType,
      urgency: editForm.urgency,
      gridId: editForm.handlerUnitId,
      handlerUnitId: editForm.handlerUnitId,
      deadline: editForm.deadline || null,
      taskContent: editForm.taskContent || '',
      status: targetStatus
    }
    if (isEdit.value) {
      await updateTask(editForm.id, data)
      ElMessage.success(targetStatus === 'DRAFT' ? '保存成功' : '发布成功')
    } else {
      await dispatchTask(data)
      ElMessage.success(targetStatus === 'DRAFT' ? '暂存成功' : '发布成功')
    }
    editVisible.value = false
    search()
  } catch (e) { ElMessage.error('操作失败: ' + (e?.message || '')) }
  finally { submitting.value = false }
}

// ===== 发布 =====
const handlePublish = async (row) => {
  await ElMessageBox.confirm(`确认发布任务"${row.taskTitle}"？发布后将派发给处理单位。`, '确认发布', { type: 'info' })
  submitting.value = true
  try {
    await publishTask(row.id)
    ElMessage.success('发布成功')
    search()
  } catch (e) { ElMessage.error('发布失败: ' + (e?.message || '')) }
  finally { submitting.value = false }
}

// ===== 删除 =====
const showBatchDelete = () => {
  const selectRows = selectedIds.value.map(id => list.value.find(r => r.id === id)).filter(Boolean)
  const nonDraft = selectRows.filter(r => r.status !== 'DRAFT')
  if (nonDraft.length > 0) {
    ElMessage.warning(`选中的任务中有 ${nonDraft.length} 个不是"已拟定"状态，只能删除"已拟定"状态的任务。请重新选择。`)
    return
  }
  const maxShow = 5
  const showList = selectRows.slice(0, maxShow)
  const moreCount = selectRows.length - maxShow
  const rowsHtml = showList.map(r =>
    `<div style="display:flex;align-items:baseline;padding:8px 0;border-bottom:1px dashed #e0e1e6"><span style="color:#86909c;font-size:13px;width:60px;flex-shrink:0">编号</span><span style="color:#1d2129;font-size:14px;flex:1;font-weight:700">${r.taskNo || '-'}</span></div>` +
    `<div style="display:flex;align-items:baseline;padding:8px 0;border-bottom:1px dashed #e0e1e6"><span style="color:#86909c;font-size:13px;width:60px;flex-shrink:0">标题</span><span style="color:#1d2129;font-size:14px;flex:1">${r.taskTitle || '-'}</span></div>`
  ).join('')
  const moreHtml = moreCount > 0 ? `<div style="display:flex;align-items:baseline;padding:8px 0"><span style="width:60px;flex-shrink:0"></span><span style="color:#909399;font-size:13px">...还有 ${moreCount} 个任务</span></div>` : ''
  ElMessageBox.confirm(
    `<div class="del-detail">
      ${rowsHtml}${moreHtml}
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，${selectedIds.value.length} 个任务将被永久删除！</div>
      <div class="del-warning-note">注：仅"已拟定"状态的任务可删除，已派发的任务记录不受影响。</div>
    </div>`,
    '批量删除任务',
    {
      confirmButtonText: '确认删除',
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
    submitting.value = true
    try {
      await deleteTask(selectedIds.value)
      ElMessage.success('删除成功')
      selectedIds.value = []
      search()
    } catch (e) { ElMessage.error('删除失败: ' + (e?.message || '')) }
    finally { submitting.value = false }
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">任务编号</span><span class="del-value"><strong>${row.taskNo || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">任务标题</span><span class="del-value">${row.taskTitle || '-'}</span></div>
      <div class="del-row"><span class="del-label">任务类型</span><span class="del-value">${taskTypeMap[row.taskType] || row.taskType || '-'}</span></div>
      <div class="del-row"><span class="del-label">处理单位</span><span class="del-value">${row.gridName || '-'}</span></div>
      <div class="del-row"><span class="del-label">状态</span><span class="del-value">${taskStatusMap[row.status] || row.status || '-'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该任务将被永久移除！</div>
      <div class="del-warning-note">注：仅"已拟定"状态的任务可删除，已派发的任务记录不受影响。</div>
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
    submitting.value = true
    try {
      await deleteTask([row.id])
      ElMessage.success('删除成功')
      search()
    } catch (e) { ElMessage.error('删除失败: ' + (e?.message || '')) }
    finally { submitting.value = false }
  }).catch(() => {})
}

// ===== 催办 =====
const urgeVisible = ref(false)
const urgeForm = reactive({ id: null, taskNo: '', taskTitle: '', reason: '' })

const handleUrge = (row) => {
  urgeForm.id = row.id; urgeForm.taskNo = row.taskNo; urgeForm.taskTitle = row.taskTitle; urgeForm.reason = ''
  urgeVisible.value = true
}

const showBatchUrge = () => {
  const selectRows = selectedIds.value.map(id => list.value.find(r => r.id === id)).filter(Boolean)
  const valid = selectRows.filter(r => r.status === 'DISPATCHED' || r.status === 'SIGNED')
  if (valid.length === 0) { ElMessage.warning('选中的任务中没有"已派发"或"已签收"状态的任务'); return }
  if (valid.length < selectedIds.value.length) {
    ElMessage.warning(`已自动过滤 ${selectedIds.value.length - valid.length} 个不可催办的任务`)
  }
  urgeForm.id = null; urgeForm.taskNo = valid.map(r => r.taskNo).join(', '); urgeForm.taskTitle = `共 ${valid.length} 个任务`; urgeForm.reason = ''
  urgeForm._ids = valid.map(r => r.id)
  urgeVisible.value = true
}

const submitUrge = async () => {
  if (!urgeForm.reason.trim()) { ElMessage.warning('请填写催办原因'); return }
  submitting.value = true
  try {
    const ids = urgeForm._ids || [urgeForm.id]
    let successCount = 0, failCount = 0
    for (const id of ids) {
      try {
        await urgeTask(id, { reason: urgeForm.reason })
        successCount++
      } catch { failCount++ }
    }
    if (successCount > 0) {
      ElMessage.success(`催办成功 ${successCount} 个任务` + (failCount > 0 ? `，${failCount} 个失败` : ''))
    } else {
      ElMessage.error('催办失败')
    }
    urgeVisible.value = false
    selectedIds.value = []
    search()
  } catch (e) { ElMessage.error('催办失败: ' + (e?.message || '')) }
  finally { submitting.value = false }
}

// ===== 督办 =====
const superviseVisible = ref(false)
const superviseForm = reactive({ id: null, taskNo: '', taskTitle: '', reason: '' })

const handleSupervise = (row) => {
  superviseForm.id = row.id; superviseForm.taskNo = row.taskNo; superviseForm.taskTitle = row.taskTitle; superviseForm.reason = ''
  superviseVisible.value = true
}

const showBatchSupervise = () => {
  const selectRows = selectedIds.value.map(id => list.value.find(r => r.id === id)).filter(Boolean)
  const valid = selectRows.filter(r => r.status === 'DISPATCHED' || r.status === 'SIGNED')
  if (valid.length === 0) { ElMessage.warning('选中的任务中没有"已派发"或"已签收"状态的任务'); return }
  superviseForm.id = null; superviseForm.taskNo = valid.map(r => r.taskNo).join(', '); superviseForm.taskTitle = `共 ${valid.length} 个任务`; superviseForm.reason = ''
  superviseForm._ids = valid.map(r => r.id)
  superviseVisible.value = true
}

const submitSupervise = async () => {
  if (!superviseForm.reason.trim()) { ElMessage.warning('请填写督办原因'); return }
  submitting.value = true
  try {
    const ids = superviseForm._ids || [superviseForm.id]
    let successCount = 0, failCount = 0
    for (const id of ids) {
      try {
        await superviseTask(id, { reason: superviseForm.reason })
        successCount++
      } catch { failCount++ }
    }
    if (successCount > 0) {
      ElMessage.success(`督办成功 ${successCount} 个任务` + (failCount > 0 ? `，${failCount} 个失败` : ''))
    } else {
      ElMessage.error('督办失败')
    }
    superviseVisible.value = false
    selectedIds.value = []
    search()
  } catch (e) { ElMessage.error('督办失败: ' + (e?.message || '')) }
  finally { submitting.value = false }
}

// ===== 撤销 =====
const revokeVisible = ref(false)
const revokeForm = reactive({ id: null, taskNo: '', taskTitle: '', reason: '' })

const handleRevoke = (row) => {
  revokeForm.id = row.id; revokeForm.taskNo = row.taskNo; revokeForm.taskTitle = row.taskTitle; revokeForm.reason = ''
  revokeVisible.value = true
}

const showBatchRevoke = () => {
  const selectRows = selectedIds.value.map(id => list.value.find(r => r.id === id)).filter(Boolean)
  const valid = selectRows.filter(r => r.status === 'DISPATCHED' || r.status === 'SIGNED')
  if (valid.length === 0) { ElMessage.warning('选中的任务中没有"已派发"或"已签收"状态的任务'); return }
  revokeForm.id = null; revokeForm.taskNo = valid.map(r => r.taskNo).join(', '); revokeForm.taskTitle = `共 ${valid.length} 个任务`; revokeForm.reason = ''
  revokeForm._ids = valid.map(r => r.id)
  revokeVisible.value = true
}

const submitRevoke = async () => {
  submitting.value = true
  try {
    const ids = revokeForm._ids || [revokeForm.id]
    let successCount = 0, failCount = 0
    for (const id of ids) {
      try {
        await revokeTask(id, { reason: revokeForm.reason || '管理人员主动撤销' })
        successCount++
      } catch { failCount++ }
    }
    if (successCount > 0) {
      ElMessage.success(`撤销成功 ${successCount} 个任务` + (failCount > 0 ? `，${failCount} 个失败` : ''))
    } else {
      ElMessage.error('撤销失败')
    }
    revokeVisible.value = false
    selectedIds.value = []
    search()
  } catch (e) { ElMessage.error('撤销失败: ' + (e?.message || '')) }
  finally { submitting.value = false }
}

// ===== 导出 =====
const exportData = async () => {
  exporting.value = true
  try {
    const params = {}
    Object.keys(query).forEach(k => { if (query[k] !== '' && query[k] != null && k !== 'pageNum' && k !== 'pageSize') params[k] = query[k] })
    const res = await exportTaskLedger(params)
    const url = window.URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url; a.download = '任务台账.xlsx'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
  finally { exporting.value = false }
}

// ===== 查看详情 =====
const detailVisible = ref(false), activeTab = ref('basic')
const detail = ref({})

const viewDetail = async (row) => {
  activeTab.value = 'basic'
  submitting.value = true
  try {
    const res = await getTaskDetail(row.id)
    detail.value = res.data || row
  } catch { detail.value = row }
  finally { submitting.value = false }
  // 解析任务动态
  parseTaskLogs()
  detailVisible.value = true
}

// 从 taskContent 解析任务动态（催办/督办/退回/审核记录）
const detailLogs = ref([])

const parseTaskLogs = () => {
  const content = detail.value.taskContent
  if (!content) { detailLogs.value = []; return }
  const logs = []

  // 催办记录
  const urgeMatches = content.matchAll(/=== 催办记录 \[([^\]]+)\] ===\n?原因:\s*(.*?)(?=\n===|\n?$)/gs)
  for (const m of urgeMatches) {
    logs.push({ time: m[1], title: '催办', content: '原因: ' + (m[2].trim() || '未填写'), color: '#E6A23C' })
  }

  // 督办记录
  const superMatches = content.matchAll(/=== 督办记录 \[([^\]]+)\] ===\n?原因:\s*(.*?)(?=\n===|\n?$)/gs)
  for (const m of superMatches) {
    logs.push({ time: m[1], title: '督办', content: '原因: ' + (m[2].trim() || '未填写'), color: '#F56C6C' })
  }

  // 退回记录
  const returnMatches = content.matchAll(/=== 退回记录 \[([^\]]+)\] ===\n退回原因:\s*(.*?)(?:\n建议处理人:\s*(.*?))?(?:\n建议单位:\s*(.*?))?(?=\n===|\n?$)/gs)
  for (const m of returnMatches) {
    let c = '退回原因: ' + (m[2]?.trim() || '未填写')
    if (m[3]?.trim()) c += '\n建议处理人: ' + m[3].trim()
    if (m[4]?.trim()) c += '\n建议单位: ' + m[4].trim()
    logs.push({ time: m[1], title: '退回', content: c, color: '#909399' })
  }

  // 审核记录
  const auditMatches = content.matchAll(/=== 审核记录 \[([^\]]+)\] ===\n审核结果:\s*(.*?)(?:\n审核意见:\s*(.*?))?(?=\n===|\n?$)/gs)
  for (const m of auditMatches) {
    let c = '审核结果: ' + m[2].trim()
    if (m[3]?.trim()) c += '\n审核意见: ' + m[3].trim()
    logs.push({ time: m[1], title: '审核', content: c, color: '#67C23A' })
  }

  // 处理结果
  if (content.includes('=== 处理结果 ===')) {
    const idx = content.indexOf('=== 处理结果 ===')
    const processContent = content.substring(idx + '=== 处理结果 ==='.length).split('=== ')[0].trim()
    logs.push({ time: detail.value.finishTime || '', title: '处理完成', content: processContent, color: '#409EFF' })
  }

  // 按时间倒序
  logs.sort((a, b) => (b.time || '').localeCompare(a.time || ''))
  detailLogs.value = logs
}

const taskStatusTagType = (status) => {
  const map = { DRAFT: 'info', DISPATCHED: 'warning', SIGNED: '', PROCESSING: '', RECEIVED: '', DONE: 'success', COMPLETED: 'success', REVOKED: 'info', RETURNED: 'danger' }
  return map[status] || ''
}

onMounted(search)
</script>

<style scoped>
pre { font-family: inherit; }
</style>
