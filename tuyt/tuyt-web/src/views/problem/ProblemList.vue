<template>
  <div>
    <div class="page-title">问题管理</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="事发企业"><el-input v-model="query.enterpriseName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="网格区域">
          <el-select v-model="query.gridId" clearable placeholder="全部市" style="width:130px" @change="onGridChange">
            <el-option v-for="g in cityGrids" :key="g.id" :label="g.gridName" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题等级">
          <el-select v-model="query.problemLevel" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="opt in Object.entries(problemLevelMap).map(([value, cfg]) => ({ value, label: cfg.label }))" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="污染类型">
          <el-select v-model="query.problemType" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="opt in pollutionTypeOptions.filter(o => ['WASTE_WATER','WASTE_GAS','NOISE','SOLID_WASTE','HAZARDOUS'].includes(o.value))" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题来源">
          <el-select v-model="query.problemSource" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="opt in problemSourceOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="query.handleStatus" clearable placeholder="全部" style="width: 100px">
            <el-option v-for="(label, value) in handleStatusMap" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <div style="margin-bottom:12px;display:flex;align-items:center;gap:12px">
        <el-button type="danger" :disabled="selectedIds.length===0" @click="batchClose">批量关闭</el-button>
        <span v-if="selectedIds.length>0" style="color:#909399;font-size:13px">已选择 {{ selectedIds.length }} 项</span>
      </div>
      <el-table :data="list" stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="enterpriseName" label="事发企业" width="160" />
        <el-table-column prop="alarmTime" label="报警时间" width="160" />
        <el-table-column prop="problemDesc" label="问题描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="problemSource" label="来源" width="100"><template #default="{row}">{{ problemSourceMap[row.problemSource] || row.problemSource }}</template></el-table-column>
        <el-table-column prop="pollutionType" label="污染类型" width="100"><template #default="{row}">{{ pollutionTypeMap[row.pollutionType] || row.pollutionType || '-' }}</template></el-table-column>
        <el-table-column prop="problemLevel" label="等级" width="80"><template #default="{row}"><el-tag :type="problemLevelMap[row.problemLevel]?.tagType || ''" size="small">{{ problemLevelMap[row.problemLevel]?.label || row.problemLevel }}</el-tag></template></el-table-column>
        <el-table-column prop="handleStatus" label="状态" width="90"><template #default="{row}"><el-tag :type="handleStatusTagType[row.handleStatus] || ''" size="small">{{ handleStatusMap[row.handleStatus] || row.handleStatus }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}"><el-button type="primary" link @click="$router.push('/dispatch/problem/'+row.id)">甄别</el-button><el-button type="primary" link @click="openEdit(row)">编辑</el-button><el-button type="danger" link @click="handleClose(row)">关闭</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
    </div>
    <el-dialog v-model="dialogVisible" title="编辑问题" width="600px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="问题等级">
          <el-select v-model="editForm.problemLevel" style="width:100%">
            <el-option v-for="opt in Object.entries(problemLevelMap).map(([value, cfg]) => ({ value, label: cfg.label }))" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="污染类型">
          <el-select v-model="editForm.pollutionType" style="width:100%">
            <el-option v-for="opt in pollutionTypeOptions.filter(o => ['WASTE_WATER','WASTE_GAS','NOISE','SOLID_WASTE','HAZARDOUS'].includes(o.value))" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题来源">
          <el-select v-model="editForm.problemSource" style="width:100%">
            <el-option v-for="opt in problemSourceOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述"><el-input v-model="editForm.problemDesc" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="editForm.address" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="closeDialogVisible" title="关闭问题" width="500px" align-center>
      <div style="display:flex;align-items:flex-start;gap:12px;margin-bottom:16px">
        <el-icon color="#E6A23C" :size="22"><Warning /></el-icon>
        <div>
          <div style="font-weight:500;margin-bottom:4px">确认关闭{{ closeTarget?.length>1 ? `选中的 ${closeTarget.length} 个问题` : '该问题' }}？</div>
          <div style="color:#909399;font-size:13px">关闭后问题将不再参与后续处理流程，请填写关闭原因。</div>
        </div>
      </div>
      <el-form label-width="90px">
        <el-form-item label="关闭原因" required>
          <el-input v-model="closeReason" type="textarea" :rows="3" placeholder="请输入关闭原因，不少于5个字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialogVisible=false">取消</el-button>
        <el-button type="danger" :disabled="closeReason.trim().length<5" @click="confirmClose">确认关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { getProblemList, closeProblem, getProblemDetail, updateProblem, getGridList } from '@/api'
import { problemLevelMap, pollutionTypeOptions, pollutionTypeMap, problemSourceOptions, problemSourceMap, handleStatusMap, handleStatusTagType } from '@/utils/constants'

const route = useRoute()
const loading = ref(false), list = ref([]), total = ref(0)
const query = reactive({ enterpriseName:'', areaName:'', problemLevel:'', pollutionType:'', problemType:'', problemSource:'', handleStatus:'', gridId:null, pageNum:1, pageSize:10 })
const dialogVisible = ref(false)
const editForm = reactive({ id:null, problemLevel:'', pollutionType:'', problemSource:'', problemDesc:'', address:'' })
const closeDialogVisible = ref(false)
const closeReason = ref('')
const closeTarget = ref(null)
const selectedIds = ref([])

const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map(r => r.id)
}

const batchClose = () => {
  const selectedRows = list.value.filter(r => selectedIds.value.includes(r.id))
  const closable = selectedRows.filter(r => ['PENDING','PROCESSED','DONE'].includes(r.handleStatus))
  const unclosable = selectedRows.filter(r => !['PENDING','PROCESSED','DONE'].includes(r.handleStatus))
  if (closable.length === 0) {
    ElMessage.warning('所选问题均为已关闭状态，无法再次关闭')
    return
  }
  if (unclosable.length > 0) {
    ElMessage.warning(`已自动排除 ${unclosable.length} 个已关闭的问题`)
  }
  closeTarget.value = closable.map(r => r.id)
  closeReason.value = ''
  closeDialogVisible.value = true
}

const fetch = async () => {
  loading.value = true
  try {
    const r = await getProblemList({ ...query })
    const d = r.data || {}
    list.value = d.records || d.list || []
    total.value = Number(d.total) || 0
  } catch {
    list.value = []
    total.value = 0
  }
  loading.value = false
}
const search = () => { query.pageNum=1; fetch() }
const reset = () => { query.enterpriseName=''; query.areaName=''; query.problemLevel=''; query.pollutionType=''; query.problemType=''; query.problemSource=''; query.handleStatus=''; query.gridId=null; search() }
const onGridChange = () => { query.areaName = ''; search() }

// 加载地级市网格下拉选项
const cityGrids = ref([])
onMounted(async () => {
  try {
    const r = await getGridList({ gridLevel: 1, pageSize: 100 })
    cityGrids.value = (r.data && r.data.records) ? r.data.records : []
  } catch { /* 后端未就绪 */ }
  fetch()
})
const openEdit = async (row) => {
  try {
    const r = await getProblemDetail(row.id)
    const d = r.data || {}
    Object.assign(editForm, { id: d.id, problemLevel: d.problemLevel||'', pollutionType: d.pollutionType||'', problemSource: d.problemSource||'', problemDesc: d.problemDesc||'', address: d.address||'' })
    dialogVisible.value = true
  } catch { ElMessage.error('获取详情失败') }
}
const handleUpdate = async () => {
  try {
    await updateProblem(editForm.id, editForm)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    fetch()
  } catch { ElMessage.error('更新失败') }
}
const handleClose = (row) => { closeTarget.value = [row.id]; closeReason.value = ''; closeDialogVisible.value = true }
const confirmClose = async () => {
  try {
    await closeProblem({ ids: closeTarget.value, reason: closeReason.value.trim() })
    closeDialogVisible.value = false
    selectedIds.value = []
    ElMessage.success(closeTarget.value.length > 1 ? `已批量关闭${closeTarget.value.length}个问题` : '已关闭')
    fetch()
  } catch { ElMessage.error('关闭失败') }
}

// 从网格排名跳转时接收 gridId 参数，自动选中城市并查询
watch(() => route.query.gridId, (val) => {
  if (val) {
    query.gridId = Number(val)
    query.areaName = ''
    search()
  }
}, { immediate: true })
// 从问题统计跳转时接收 handleStatus 参数，自动筛选状态
watch(() => route.query.handleStatus, (val) => {
  if (val) {
    query.handleStatus = val
    search()
  }
}, { immediate: true })
// 从问题统计图表点击跳转时接收 problemType 参数，自动筛选污染类型
watch(() => route.query.problemType, (val) => {
  if (val) {
    query.problemType = val
    search()
  }
}, { immediate: true })
// 从问题统计图表点击跳转时接收 problemSource 参数，自动筛选问题来源
watch(() => route.query.problemSource, (val) => {
  if (val) {
    query.problemSource = val
    search()
  }
}, { immediate: true })
</script>
