<template>
  <div>
    <div class="page-title">我的待办件</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="任务编号"><el-input v-model="query.taskNo" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="任务标题"><el-input v-model="query.title" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="任务类型"><el-select v-model="query.taskType" placeholder="全部" clearable><el-option label="日常巡查" value="DAILY" /><el-option label="停产巡查" value="SHUTDOWN" /><el-option label="问题核查" value="CHECK" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="taskNo" label="任务编号" width="160" />
        <el-table-column prop="taskTitle" label="任务标题" min-width="200" />
        <el-table-column prop="taskType" label="任务类型" width="100"><template #default="{ row }">{{ row.taskType=== 'DAILY' ? '日常巡查' : row.taskType==='SHUTDOWN' ? '停产巡查' : '问题核查' }}</template></el-table-column>
        <el-table-column prop="urgency" label="紧急程度" width="90"><template #default="{ row }"><el-tag :type="row.urgency==='URGENT'?'danger':row.urgency==='NORMAL'?'':'warning'" size="small">{{ row.urgency==='URGENT'?'特急':row.urgency==='NORMAL'?'一般':'紧急' }}</el-tag></template></el-table-column>
        <el-table-column prop="dispatchTime" label="派发时间" width="160" />
        <el-table-column prop="deadline" label="截止时间" width="160" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link @click="openHandle(row)">处理</el-button>
            <el-button type="primary" link @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" />
    </div>

    <!-- 查看详情 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="600px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="任务编号"><span>{{ currentRow.taskNo }}</span></el-form-item>
        <el-form-item label="任务标题"><span>{{ currentRow.taskTitle }}</span></el-form-item>
        <el-form-item label="任务类型"><span>{{ typeText(currentRow.taskType) }}</span></el-form-item>
        <el-form-item label="紧急程度"><span>{{ urgencyText(currentRow.urgency) }}</span></el-form-item>
        <el-form-item label="派发时间"><span>{{ currentRow.dispatchTime }}</span></el-form-item>
        <el-form-item label="截止时间"><span>{{ currentRow.deadline }}</span></el-form-item>
        <el-form-item label="任务内容"><span>{{ currentRow.content || '-' }}</span></el-form-item>
        <el-form-item label="派发人"><span>{{ currentRow.dispatcher || '-' }}</span></el-form-item>
        <el-form-item label="处理单位"><span>{{ currentRow.handleOrg || '-' }}</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 处理任务 -->
    <el-dialog v-model="handleVisible" title="任务处理" width="600px" append-to-body>
      <el-form :model="handleForm" label-width="110px" ref="handleRef">
        <el-form-item label="现场检查结论" prop="conclusion" :rules="[{ required: true, message: '请输入现场检查结论' }]">
          <el-input v-model="handleForm.conclusion" type="textarea" :rows="2" placeholder="请输入现场检查结论" />
        </el-form-item>
        <el-form-item label="现场处置建议" prop="suggestion" :rules="[{ required: true, message: '请输入现场处置建议' }]">
          <el-input v-model="handleForm.suggestion" type="textarea" :rows="2" placeholder="请输入现场处置建议" />
        </el-form-item>
        <el-form-item label="整改情况" prop="rectifyStatus" :rules="[{ required: true, message: '请选择整改情况' }]">
          <el-radio-group v-model="handleForm.rectifyStatus">
            <el-radio label="DONE">整改完成</el-radio>
            <el-radio label="LIMIT">限期整改</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="整改期限" v-if="handleForm.rectifyStatus === 'LIMIT'" prop="rectifyDeadline" :rules="[{ required: true, message: '请选择整改期限' }]">
          <el-date-picker v-model="handleForm.rectifyDeadline" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="生产经营情况" prop="productionStatus" :rules="[{ required: true, message: '请选择生产经营情况' }]">
          <el-radio-group v-model="handleForm.productionStatus">
            <el-radio label="NORMAL">正常生产</el-radio>
            <el-radio label="SHUTDOWN">停产</el-radio>
            <el-radio label="CLOSED">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="handleForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getTodoList, getTaskDetail, processTask } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ taskNo: '', title: '', taskType: '', pageNum: 1, pageSize: 10 })

const detailVisible = ref(false)
const handleVisible = ref(false)
const currentRow = ref({})
const submitting = ref(false)
const handleRef = ref(null)
const handleForm = reactive({ conclusion: '', suggestion: '', rectifyStatus: '', rectifyDeadline: '', productionStatus: '', remark: '' })

const typeText = (v) => v === 'DAILY' ? '日常巡查' : v === 'SHUTDOWN' ? '停产巡查' : '问题核查'
const urgencyText = (v) => v === 'URGENT' ? '特急' : v === 'NORMAL' ? '一般' : '紧急'

const fetch = async () => {
  loading.value = true
  const res = await getTodoList(query).catch(() => ({ data: { list: [], total: 0 } }))
  list.value = res.data?.records || res.data?.list || []
  total.value = res.data?.total || 0
  loading.value = false
}
const search = () => { query.pageNum = 1; fetch() }
const reset = () => { query.taskNo = ''; query.title = ''; query.taskType = ''; search() }

const openDetail = async (row) => {
  currentRow.value = { ...row }
  try {
    const res = await getTaskDetail(row.id)
    if (res.data) currentRow.value = res.data
  } catch { /* 使用列表数据兜底 */ }
  detailVisible.value = true
}

const openHandle = (row) => {
  currentRow.value = row
  Object.assign(handleForm, { conclusion: '', suggestion: '', rectifyStatus: '', rectifyDeadline: '', productionStatus: '', remark: '' })
  handleVisible.value = true
}

const submitHandle = async () => {
  if (!handleRef.value) return
  await handleRef.value.validate()
  submitting.value = true
  try {
    await processTask(currentRow.value.id, { ...handleForm })
    ElMessage.success('处理成功')
    handleVisible.value = false
    fetch()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '处理失败')
  } finally {
    submitting.value = false
  }
}

fetch()
</script>
