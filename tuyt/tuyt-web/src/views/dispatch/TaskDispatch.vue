<template>
  <div>
    <div class="page-title">任务调度</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="任务编号"><el-input v-model="query.taskNo" clearable /></el-form-item>
        <el-form-item label="任务标题"><el-input v-model="query.title" clearable /></el-form-item>
        <el-form-item label="任务状态"><el-select v-model="query.status" clearable><el-option label="已拟定" value="DRAFT" /><el-option label="已派发" value="DISPATCHED" /><el-option label="已签收" value="RECEIVED" /><el-option label="已完成" value="COMPLETED" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button type="success" @click="dialogVisible=true"><el-icon><Plus /></el-icon>派发任务</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无任务">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="taskNo" label="任务单号" width="160" />
        <el-table-column prop="title" label="任务标题" min-width="200" />
        <el-table-column prop="taskType" label="任务类型" width="100" />
        <el-table-column prop="urgency" label="紧急程度" width="80" />
        <el-table-column prop="deadline" label="截止时间" width="160" />
        <el-table-column prop="processor" label="处理人" width="100" />
        <el-table-column prop="status" label="状态" width="80" />
        <el-table-column label="操作" width="160"><template #default="{ row }"><el-button link type="primary" @click="handleSupervise(row.id)">督办</el-button><el-button link type="warning" @click="handleUrge(row.id)">催办</el-button><el-button link type="danger" @click="handleRevoke(row.id)">撤销</el-button></template></el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" @current-change="search" @size-change="search" />
    </div>
    <el-dialog v-model="dialogVisible" title="派发任务" width="600px">
      <el-form label-width="100px" :model="dispatchForm">
        <el-form-item label="任务标题"><el-input v-model="dispatchForm.title" /></el-form-item>
        <el-form-item label="任务类型"><el-select v-model="dispatchForm.taskType" style="width:100%"><el-option label="日常巡查" value="DAILY" /><el-option label="专项检查" value="SPECIAL" /><el-option label="应急任务" value="EMERGENCY" /></el-select></el-form-item>
        <el-form-item label="紧急程度"><el-select v-model="dispatchForm.urgency" style="width:100%"><el-option label="一般" value="NORMAL" /><el-option label="紧急" value="URGENT" /><el-option label="非常紧急" value="CRITICAL" /></el-select></el-form-item>
        <el-form-item label="处理单位"><el-select v-model="dispatchForm.gridId" style="width:100%" placeholder="请选择网格"><el-option label="尖草坪区" :value="1" /><el-option label="万柏林区" :value="2" /><el-option label="晋源区" :value="3" /><el-option label="杏花岭区" :value="4" /><el-option label="迎泽区" :value="5" /><el-option label="小店区" :value="6" /></el-select></el-form-item>
        <el-form-item label="处理人"><el-input v-model="dispatchForm.processor" placeholder="请输入处理人" /></el-form-item>
        <el-form-item label="处理期限"><el-date-picker v-model="dispatchForm.deadline" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item label="任务内容"><el-input v-model="dispatchForm.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleDispatch">提交派发</el-button></template>
    </el-dialog>
    <el-dialog v-model="reasonDialogVisible" :title="reasonTitle" width="500px" align-center>
      <div style="display:flex;align-items:flex-start;gap:12px;margin-bottom:16px">
        <el-icon color="#E6A23C" :size="22"><Warning /></el-icon>
        <div>
          <div style="font-weight:500;margin-bottom:4px">{{ reasonTitle }}</div>
          <div style="color:#909399;font-size:13px">请填写原因后提交，该操作将通知相关人员。</div>
        </div>
      </div>
      <el-form label-width="90px">
        <el-form-item label="原因说明" required>
          <el-input v-model="reasonContent" type="textarea" :rows="3" placeholder="请输入原因说明，不少于5个字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reasonDialogVisible=false">取消</el-button>
        <el-button type="primary" :disabled="reasonContent.trim().length<5" @click="confirmReason">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { getTaskList, dispatchTask, urgeTask, superviseTask, revokeTask } from '@/api'

const dialogVisible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ taskNo: '', title: '', status: '', pageNum: 1, pageSize: 10 })
const dispatchForm = reactive({ title: '', taskType: 'DAILY', urgency: 'NORMAL', gridId: null, processor: '', deadline: '', content: '' })
const reasonDialogVisible = ref(false)
const reasonTitle = ref('')
const reasonContent = ref('')
const reasonAction = ref('')
const reasonTargetId = ref(null)

const openReasonDialog = (id, title, action) => {
  reasonTargetId.value = id
  reasonTitle.value = title
  reasonAction.value = action
  reasonContent.value = ''
  reasonDialogVisible.value = true
}
const confirmReason = async () => {
  try {
    if (reasonAction.value === 'urge') {
      await urgeTask(reasonTargetId.value, { reason: reasonContent.value.trim() })
      ElMessage.success('已催办')
    } else if (reasonAction.value === 'supervise') {
      await superviseTask(reasonTargetId.value, { reason: reasonContent.value.trim() })
      ElMessage.success('已督办')
    }
    reasonDialogVisible.value = false
    search()
  } catch { ElMessage.error('操作失败') }
}

const search = async () => {
  loading.value = true
  try {
    const res = await getTaskList(query)
    list.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { /* 后端未就绪 */ }
  finally { loading.value = false }
}

const handleDispatch = async () => {
  try { await dispatchTask({ ...dispatchForm }); ElMessage.success('派发成功'); dialogVisible.value = false; Object.assign(dispatchForm, { title: '', taskType: 'DAILY', urgency: 'NORMAL', gridId: null, processor: '', deadline: '', content: '' }); search() } catch {}
}
const handleUrge = (id) => openReasonDialog(id, '催办任务', 'urge')
const handleSupervise = (id) => openReasonDialog(id, '督办任务', 'supervise')
const handleRevoke = (id) => ElMessageBox.confirm('撤销后该任务将退回至待派发状态，是否继续？', '撤销任务', { confirmButtonText: '确认撤销', cancelButtonText: '取消', type: 'warning' }).then(() => revokeTask(id).then(() => { ElMessage.success('已撤销'); search() })).catch(() => {})

onMounted(search)
</script>
