<template>
  <div>
    <div class="page-title">问题管理</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="事发企业"><el-input v-model="query.enterpriseName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="问题等级"><el-select v-model="query.level" clearable placeholder="全部"><el-option label="严重" value="I" /><el-option label="较严重" value="II" /><el-option label="一般" value="III" /></el-select></el-form-item>
        <el-form-item label="污染类型"><el-select v-model="query.pollutionType" clearable placeholder="全部"><el-option label="废水" value="WASTE_WATER" /><el-option label="废气" value="WASTE_GAS" /><el-option label="噪声" value="NOISE" /><el-option label="固危废" value="SOLID_WASTE" /></el-select></el-form-item>
        <el-form-item label="问题来源"><el-select v-model="query.source" clearable placeholder="全部"><el-option label="公众投诉" value="PUBLIC_COMPLAINT" /><el-option label="现场监察" value="FIELD_INSPECTION" /><el-option label="在线监测" value="ONLINE_MONITOR" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button><el-button type="warning" :disabled="!selectedIds.length" @click="batchClose">批量关闭</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading" @selection-change="v=>selectedIds=v.map(i=>i.id)">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="enterpriseName" label="事发企业" width="160" />
        <el-table-column prop="alarmTime" label="报警时间" width="160" />
        <el-table-column prop="problemDesc" label="问题描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="problemSource" label="来源" width="100"><template #default="{row}">{{ sourceMap[row.problemSource] || row.problemSource }}</template></el-table-column>
        <el-table-column prop="problemLevel" label="等级" width="80"><template #default="{row}"><el-tag :type="row.problemLevel==='I'?'danger':row.problemLevel==='II'?'warning':''" size="small">{{ row.problemLevel==='I'?'严重':row.problemLevel==='II'?'较严重':'一般' }}</el-tag></template></el-table-column>
        <el-table-column prop="handleStatus" label="状态" width="90"><template #default="{row}">{{ statusMap[row.handleStatus] || row.handleStatus }}</template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}"><el-button type="primary" link @click="$router.push('/dispatch/problem/'+row.id)">甄别</el-button><el-button type="primary" link @click="openEdit(row)">编辑</el-button><el-button type="danger" link @click="handleClose(row)">关闭</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" />
    </div>
    <el-dialog v-model="dialogVisible" title="编辑问题" width="600px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="问题等级"><el-select v-model="editForm.problemLevel" style="width:100%"><el-option label="严重" value="I" /><el-option label="较严重" value="II" /><el-option label="一般" value="III" /></el-select></el-form-item>
        <el-form-item label="污染类型"><el-select v-model="editForm.pollutionType" style="width:100%"><el-option label="废水" value="WASTE_WATER" /><el-option label="废气" value="WASTE_GAS" /><el-option label="噪声" value="NOISE" /><el-option label="固危废" value="SOLID_WASTE" /></el-select></el-form-item>
        <el-form-item label="问题来源"><el-select v-model="editForm.problemSource" style="width:100%"><el-option label="公众投诉" value="PUBLIC_COMPLAINT" /><el-option label="现场监察" value="FIELD_INSPECTION" /><el-option label="在线监测" value="ONLINE_MONITOR" /></el-select></el-form-item>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { getProblemList, closeProblem, getProblemDetail, updateProblem } from '@/api'

const loading = ref(false), list = ref([]), total = ref(0), selectedIds = ref([])
const query = reactive({ enterpriseName:'', level:'', pollutionType:'', source:'', pageNum:1, pageSize:10 })
const dialogVisible = ref(false)
const editForm = reactive({ id:null, problemLevel:'', pollutionType:'', problemSource:'', problemDesc:'', address:'' })
const statusMap = { PENDING:'待处理', PROCESSED:'已处理', DONE:'处理完成', CLOSED:'已关闭' }
const sourceMap = { PUBLIC_COMPLAINT:'公众投诉', FIELD_INSPECTION:'现场监察', ONLINE_MONITOR:'在线监测' }
const pollutionMap = { WASTE_WATER:'废水', WASTE_GAS:'废气', NOISE:'噪声', SOLID_WASTE:'固危废' }
const closeDialogVisible = ref(false)
const closeReason = ref('')
const closeTarget = ref(null)
const fetch = async () => { loading.value=true; const r=await getProblemList(query).catch(()=>({data:{records:[],total:0}})); const d=r.data||{}; list.value=d.records||d.list||[]; total.value=d.total||0; loading.value=false }
const search = () => { query.pageNum=1; fetch() }
const reset = () => { query.enterpriseName=''; query.level=''; query.pollutionType=''; query.source=''; search() }
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
const batchClose = () => { closeTarget.value = [...selectedIds.value]; closeReason.value = ''; closeDialogVisible.value = true }
const confirmClose = async () => {
  try {
    await closeProblem({ ids: closeTarget.value, reason: closeReason.value.trim() })
    closeDialogVisible.value = false
    ElMessage.success(closeTarget.value.length > 1 ? `已批量关闭${closeTarget.value.length}个问题` : '已关闭')
    fetch()
  } catch { ElMessage.error('关闭失败') }
}
onMounted(fetch)
</script>
