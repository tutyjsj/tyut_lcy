<template>
  <div>
    <div class="page-title">退回任务管理</div>

    <!-- 查询栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="任务单号">
          <el-input v-model="query.taskNo" clearable placeholder="输入编号" style="width:140px" />
        </el-form-item>
        <el-form-item label="任务标题">
          <el-input v-model="query.title" clearable placeholder="输入标题" style="width:140px" />
        </el-form-item>
        <el-form-item label="退回人">
          <el-input v-model="query.returnPerson" clearable placeholder="输入退回人" style="width:120px" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.auditStatus" clearable placeholder="全部" style="width:120px">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetch">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 退回任务列表 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无退回任务">
        <el-table-column prop="taskNo" label="任务单号" width="150" />
        <el-table-column prop="taskTitle" label="任务标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="任务类型" width="100">
          <template #default="{ row }">{{ taskTypeMap[row.taskType] || row.taskType }}</template>
        </el-table-column>
        <el-table-column prop="returnPerson" label="退回人" width="100" />
        <el-table-column label="退回原因" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span :style="{ color: '#E6A23C' }">{{ row.returnReason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退回时间" width="100">
          <template #default="{ row }">{{ row.returnTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="建议处理人" width="100">
          <template #default="{ row }">{{ row.suggestHandler || '-' }}</template>
        </el-table-column>
        <el-table-column label="建议处理单位" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.suggestUnit || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.auditResult==='APPROVED'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="row.auditResult==='REJECTED'" type="danger" size="small">已驳回</el-tag>
            <el-tag v-else type="warning" size="small">待审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="!row.auditResult || row.auditResult==='PENDING'" link type="warning" @click="openAudit(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50,100]" :total="total"
        layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
    </div>

    <!-- 审核弹框 -->
    <el-dialog v-model="auditVisible" title="审核退回任务" width="750px">
      <template v-if="auditTask">
        <el-alert title="请仔细审核退回信息后填写审核意见" type="warning" :closable="false" show-icon style="margin-bottom:16px" />
        <!-- 任务基本信息 -->
        <el-divider content-position="left">任务信息</el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="任务单号">{{ auditTask.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag type="danger" size="small">已退回</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务标题" :span="2">{{ auditTask.taskTitle }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">{{ taskTypeMap[auditTask.taskType] || auditTask.taskType }}</el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag v-if="auditTask.urgency==='CRITICAL'" type="danger" size="small">非常紧急</el-tag>
            <el-tag v-else-if="auditTask.urgency==='URGENT'" type="warning" size="small">紧急</el-tag>
            <span v-else style="color:#909399">一般</span>
          </el-descriptions-item>
          <el-descriptions-item label="处理单位">{{ auditTask.gridName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ auditTask.deadline || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务内容" :span="2">{{ auditTask.taskContent || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 退回信息 -->
        <el-divider content-position="left">退回信息</el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="退回人">{{ auditTask.returnPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退回时间">{{ auditTask.returnTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="建议处理人">{{ auditTask.suggestHandler || '-' }}</el-descriptions-item>
          <el-descriptions-item label="建议处理单位">{{ auditTask.suggestUnit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退回原因" :span="2">
            <span style="color:#E6A23C;font-weight:500">{{ auditTask.returnReason || '-' }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 审核操作 -->
        <el-divider content-position="left">审核操作</el-divider>
        <el-form label-width="100px" :model="auditForm" :rules="auditRules" ref="auditFormRef">
          <el-form-item label="审核结果" prop="auditResult" required>
            <el-radio-group v-model="auditForm.auditResult">
              <el-radio value="APPROVED">通过（任务将被撤销，退回人收到通知）</el-radio>
              <el-radio value="REJECTED">驳回（任务退回原处理人，退回人收到通知）</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审核意见" prop="auditComment">
            <el-input v-model="auditForm.auditComment" type="textarea" :rows="3"
              placeholder="请填写审核意见（必填，不少于5字）" show-word-limit maxlength="200" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit" :loading="auditSubmitting">提交审核</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹框 -->
    <el-dialog v-model="detailVisible" title="退回任务详情" width="700px">
      <template v-if="detailTask">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="任务单号">{{ detailTask.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="任务状态"><el-tag type="danger" size="small">已退回</el-tag></el-descriptions-item>
          <el-descriptions-item label="任务标题" :span="2">{{ detailTask.taskTitle }}</el-descriptions-item>
          <el-descriptions-item label="退回人">{{ detailTask.returnPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退回时间">{{ detailTask.returnTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退回原因" :span="2">{{ detailTask.returnReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核结果">
            <el-tag v-if="detailTask.auditResult==='APPROVED'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="detailTask.auditResult==='REJECTED'" type="danger" size="small">已驳回</el-tag>
            <el-tag v-else type="warning" size="small">待审核</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核意见">{{ detailTask.auditComment || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTaskList, auditReturnedTask } from '@/api'
import { taskTypeMap } from '@/utils/constants'

const loading = ref(false), list = ref([]), total = ref(0)
const auditVisible = ref(false), auditTask = ref(null), auditSubmitting = ref(false)
const detailVisible = ref(false), detailTask = ref(null)
const auditFormRef = ref(null)

const query = reactive({
  taskNo: '', title: '', returnPerson: '', auditStatus: '',
  pageNum: 1, pageSize: 10
})

const auditForm = reactive({
  auditResult: 'APPROVED',
  auditComment: ''
})

const auditRules = {
  auditResult: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  auditComment: [
    { required: true, message: '请填写审核意见', trigger: 'blur' },
    { min: 5, message: '审核意见不少于5字', trigger: 'blur' }
  ]
}

/** 从 taskContent 解析退回信息 */
const parseReturnInfo = (task) => {
  const content = task.taskContent || ''
  // 尝试解析退回时间戳格式
  const returnMatch = content.match(/=== 退回记录 \[([^\]]+)\] ===/)
  if (returnMatch) task.returnTime = returnMatch[1]
  const reasonMatch = content.match(/退回原因[:：]\s*(.*)/)
  if (reasonMatch) task.returnReason = reasonMatch[1]
  const suggestHandlerMatch = content.match(/建议处理人[:：]\s*(.*)/)
  if (suggestHandlerMatch) task.suggestHandler = suggestHandlerMatch[1]
  const suggestUnitMatch = content.match(/建议单位[:：]\s*(.*)/)
  if (suggestUnitMatch) task.suggestUnit = suggestUnitMatch[1]
  const auditResultMatch = content.match(/审核结果[:：]\s*(.*)/)
  if (auditResultMatch) task.auditResult = auditResultMatch[1]
  const auditCommentMatch = content.match(/审核意见[:：]\s*(.*)/)
  if (auditCommentMatch) task.auditComment = auditCommentMatch[1]
}

const fetch = async () => {
  loading.value = true
  try {
    const params = { status: 'RETURNED', ...query, pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.taskNo) params.taskNo = query.taskNo
    const res = await getTaskList(params)
    const records = res.data?.records || res.data?.list || []
    records.forEach(parseReturnInfo)
    // 客户端按审核状态过滤
    let filtered = records
    if (query.auditStatus === 'PENDING') {
      filtered = records.filter(r => !r.auditResult || r.auditResult === 'PENDING')
    } else if (query.auditStatus === 'APPROVED' || query.auditStatus === 'REJECTED') {
      filtered = records.filter(r => r.auditResult === query.auditStatus)
    }
    list.value = filtered
    total.value = res.data?.total || list.value.length
  } catch { /* */ }
  finally { loading.value = false }
}

const reset = () => {
  query.taskNo = ''; query.title = ''; query.returnPerson = ''; query.auditStatus = ''
  fetch()
}

const viewDetail = (row) => {
  detailTask.value = { ...row }
  detailVisible.value = true
}

const openAudit = (row) => {
  auditTask.value = { ...row }
  auditForm.auditResult = 'APPROVED'
  auditForm.auditComment = ''
  auditFormRef.value?.resetFields()
  auditVisible.value = true
}

const confirmAudit = async () => {
  try {
    await auditFormRef.value.validate()
  } catch { return }
  auditSubmitting.value = true
  try {
    await auditReturnedTask(auditTask.value.id, {
      auditResult: auditForm.auditResult,
      auditComment: auditForm.auditComment.trim()
    })
    ElMessage.success(auditForm.auditResult === 'APPROVED' ? '审核通过，退回人员已收到通知' : '审核驳回，任务已退回原处理人')
    auditVisible.value = false
    fetch()
  } catch { ElMessage.error('审核失败，请重试') }
  finally { auditSubmitting.value = false }
}

onMounted(fetch)
</script>

<style scoped>
.search-bar { margin-bottom: 0; }
</style>
