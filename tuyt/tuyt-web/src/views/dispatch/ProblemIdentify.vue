<template>
  <div class="identify-container">
    <div class="page-title">问题甄别</div>

    <!-- 基本信息 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="section-header">
          <span>问题基本信息</span>
          <el-button type="primary" link :icon="Edit" @click="editMode=!editMode">{{ editMode?'取消编辑':'编辑信息' }}</el-button>
        </div>
      </template>
      <el-descriptions v-if="!editMode" :column="3" border>
        <el-descriptions-item label="问题编号">{{ problemInfo.problemNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题来源">{{ sourceMap[problemInfo.problemSource] || problemInfo.problemSource || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报警时间">{{ problemInfo.alarmTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题等级">
          <el-tag :type="problemInfo.problemLevel==='I'?'danger':problemInfo.problemLevel==='II'?'warning':''" size="small">
            {{ problemInfo.problemLevel==='I'?'严重 (I级)':problemInfo.problemLevel==='II'?'较严重 (II级)':'一般 (III级)' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="事发区域">{{ problemInfo.areaName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="污染类型">{{ pollutionMap[problemInfo.pollutionType] || problemInfo.pollutionType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="事发企业">
          <el-link v-if="problemInfo.enterpriseId" type="primary" @click="$router.push('/enterprise/detail/'+problemInfo.enterpriseId)">
            {{ problemInfo.enterpriseName || '-' }}
          </el-link>
          <span v-else>{{ problemInfo.enterpriseName || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="企业信用等级">
          <el-tag v-if="problemInfo.creditLevel" :type="problemInfo.creditLevel==='A'?'success':problemInfo.creditLevel==='B'?'':'danger'" size="small">
            {{ problemInfo.creditLevel==='A'?'环保诚信企业':problemInfo.creditLevel==='B'?'环保良好企业':'环保警示企业' }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="事发地点">{{ problemInfo.address || problemInfo.addressDetail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题详情" :span="3">
          <span class="problem-desc">{{ problemInfo.problemDesc || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态" :span="1">
          <el-tag :type="handleStatusTagType[problemInfo.handleStatus] || ''" size="small">{{ handleStatusMap[problemInfo.handleStatus] || problemInfo.handleStatus }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 编辑模式 -->
      <el-form v-else label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="事发地点"><el-input v-model="editForm.address" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="事发区域"><el-input v-model="editForm.areaName" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="污染类型">
              <el-select v-model="editForm.pollutionType" style="width:100%">
                <el-option v-for="opt in pollutionTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="问题等级">
              <el-select v-model="editForm.problemLevel" style="width:100%">
                <el-option label="严重 (I级)" value="I" />
                <el-option label="较严重 (II级)" value="II" />
                <el-option label="一般 (III级)" value="III" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="问题详情"><el-input v-model="editForm.problemDesc" type="textarea" :rows="3" /></el-form-item>
        <el-form-item><el-button type="primary" @click="saveEdit">保存修改</el-button></el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区 -->
    <el-row :gutter="16" style="margin: 16px 0">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span style="font-weight:600">问题处置</span></template>
          <div class="action-btns">
            <el-button type="primary" :icon="Promotion" @click="goDispatch">派发任务</el-button>
            <el-button type="warning" :icon="Top" @click="upgradeLevel">升级为严重</el-button>
            <el-button type="info" :icon="Bottom" @click="downgradeLevel">降级为一般</el-button>
            <el-button type="danger" :icon="CloseBold" @click="openCloseDialog">关闭问题</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 关联信息 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span style="font-weight:600">关联信息</span></template>
          <div class="related-info">
            <div class="related-item" @click="$router.push('/enterprise/detail/'+problemInfo.enterpriseId)">
              <el-icon><OfficeBuilding /></el-icon>
              <span>一企一档</span>
            </div>
            <div class="related-item" @click="activeTab='history'">
              <el-icon><Clock /></el-icon>
              <span>历史问题 ({{ problemInfo.historyCount || 0 }})</span>
            </div>
            <div class="related-item">
              <el-icon><Monitor /></el-icon>
              <span>物联设备</span>
            </div>
            <div class="related-item" @click="viewOnMap">
              <el-icon><MapLocation /></el-icon>
              <span>地图定位</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 相似问题 + 历史问题 Tab -->
    <el-card shadow="never">
      <template #header>
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="相似问题" name="similar" />
          <el-tab-pane label="历史问题" name="history" />
          <el-tab-pane label="处理动态" name="dynamic" />
        </el-tabs>
      </template>

      <!-- 相似问题 -->
      <div v-if="activeTab==='similar'">
        <div class="search-bar" style="padding:0 0 12px">
          <el-form :inline="true">
            <el-form-item label="过滤条件">
              <el-checkbox-group v-model="similarFilters" @change="onSimilarFilterChange">
                <el-checkbox label="same_source">同一来源</el-checkbox>
                <el-checkbox label="same_enterprise">同一企业</el-checkbox>
                <el-checkbox label="same_complainer">同一投诉人</el-checkbox>
                <el-checkbox label="same_location">同一地点</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </div>
        <el-table :data="similarProblems" v-loading="similarLoading" empty-text="暂无相似问题" @selection-change="handleSimilarSelect">
          <el-table-column type="selection" width="40" />
          <el-table-column prop="enterpriseName" label="事发企业" min-width="120" />
          <el-table-column prop="address" label="事发地点" min-width="150" show-overflow-tooltip />
          <el-table-column prop="problemDesc" label="问题描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="alarmTime" label="事发时间" width="160" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="handleStatusTagType[row.handleStatus] || ''" size="small">{{ handleStatusMap[row.handleStatus] || row.handleStatus }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination style="margin-top:12px;display:flex;justify-content:flex-end" v-model:current-page="similarPageNum" v-model:page-size="similarPageSize" :page-sizes="[10,20,50]" :total="similarTotal" layout="total, sizes, prev, pager, next" @current-change="loadSimilarProblems" @size-change="loadSimilarProblems" />
        <div style="margin-top:12px">
          <el-button type="primary" :disabled="similarSelectedIds.length<1" @click="handleMerge">合并选中问题到当前</el-button>
        </div>
      </div>

      <!-- 历史问题 -->
      <div v-else-if="activeTab==='history'">
        <el-table :data="historyProblems" v-loading="historyLoading" empty-text="该企业暂无历史问题">
          <el-table-column prop="problemNo" label="编号" width="100" />
          <el-table-column prop="problemDesc" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="等级" width="80">
            <template #default="{ row }">
              <el-tag :type="row.problemLevel==='I'?'danger':row.problemLevel==='II'?'warning':''" size="small">{{ row.problemLevel==='I'?'严重':row.problemLevel==='II'?'较严重':'一般' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="alarmTime" label="时间" width="160" />
          <el-table-column label="结果" width="80">
            <template #default="{ row }">
              <el-tag :type="handleStatusTagType[row.handleStatus] || ''" size="small">{{ handleStatusMap[row.handleStatus] || row.handleStatus }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination style="margin-top:12px;display:flex;justify-content:flex-end" v-model:current-page="historyPageNum" v-model:page-size="historyPageSize" :page-sizes="[10,20,50]" :total="historyTotal" layout="total, sizes, prev, pager, next" @current-change="loadHistory" @size-change="loadHistory" />
      </div>

      <!-- 处理动态 -->
      <div v-else>
        <el-timeline>
          <el-timeline-item v-for="log in timelines" :key="log.time" :timestamp="log.time" :type="log.type" placement="top">
            <div>{{ log.content }}</div>
            <div v-if="log.detail" style="color:#909399;font-size:12px">{{ log.detail }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!timelines.length" description="暂无处理动态" />
      </div>
    </el-card>

    <!-- 关闭问题弹框 -->
    <el-dialog v-model="closeDialogVisible" title="关闭问题" width="500px" align-center>
      <div style="display:flex;align-items:flex-start;gap:12px;margin-bottom:16px">
        <el-icon color="#E6A23C" :size="22"><Warning /></el-icon>
        <div>
          <div style="font-weight:500;margin-bottom:4px">确认关闭该问题？</div>
          <div style="color:#909399;font-size:13px">关闭后该问题将不再参与后续处理流程，请填写关闭原因。</div>
        </div>
      </div>
      <el-form label-width="90px">
        <el-form-item label="关闭原因" required>
          <el-input v-model="closeReason" type="textarea" :rows="3" placeholder="请输入关闭原因，不少于5个字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialogVisible=false">取消</el-button>
        <el-button type="danger" :disabled="closeReason.trim().length<5" @click="handleClose">确认关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning, Edit, Promotion, Top, Bottom, CloseBold, OfficeBuilding, Clock, Monitor, MapLocation } from '@element-plus/icons-vue'
import { getProblemDetail, getProblemList, closeProblem, changeProblemLevel, updateProblem, mergeProblems } from '@/api'
import { pollutionTypeOptions, handleStatusMap, handleStatusTagType } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const activeTab = ref('similar')
const problemInfo = ref({})
const editMode = ref(false)
const editForm = ref({})
const similarLoading = ref(false), historyLoading = ref(false)
const similarProblems = ref([]), similarSelectedIds = ref([])
const similarPageNum = ref(1), similarPageSize = ref(10), similarTotal = ref(0)
const historyProblems = ref([])
const historyPageNum = ref(1), historyPageSize = ref(10), historyTotal = ref(0)
const similarFilters = ref(['same_source', 'same_enterprise'])
const timelines = ref([])
const closeDialogVisible = ref(false)
const closeReason = ref('')

const sourceMap = { PATROL:'巡查发现', MONITOR:'在线监测', COMPLAINT:'群众举报', SUPERIOR:'上级交办' }
const pollutionMap = { WASTE_WATER:'废水', WASTE_GAS:'废气', NOISE:'噪声', SOLID_WASTE:'固危废', RADIATION:'辐射', OTHER:'其他' }

const handleSimilarSelect = (rows) => { similarSelectedIds.value = rows.map(r => r.id) }

const fetchDetail = async () => {
  try {
    const res = await getProblemDetail(route.params.id)
    problemInfo.value = res.data || {}
    editForm.value = { ...problemInfo.value }
    loadSimilarProblems()
    loadHistory()
    loadTimelines()
  } catch { /* 后端未就绪 */ }
}

const loadSimilarProblems = async () => {
  similarLoading.value = true
  try {
    const params = { pageNum: similarPageNum.value, pageSize: similarPageSize.value, handleStatus: 'PENDING' }
    if (similarFilters.value.includes('same_enterprise') && problemInfo.value.enterpriseName) {
      params.enterpriseName = problemInfo.value.enterpriseName
    }
    if (similarFilters.value.includes('same_source') && problemInfo.value.problemSource) {
      params.problemSource = problemInfo.value.problemSource
    }
    const res = await getProblemList(params)
    const all = (res.data?.records || res.data?.list || [])
    similarProblems.value = all.filter(p => p.id != route.params.id)
    similarTotal.value = Number(res.data?.total) || all.length
  } catch { similarProblems.value = [] }
  finally { similarLoading.value = false }
}

const loadHistory = async () => {
  historyLoading.value = true
  try {
    if (problemInfo.value.enterpriseName) {
      const res = await getProblemList({ enterpriseName: problemInfo.value.enterpriseName, pageNum: historyPageNum.value, pageSize: historyPageSize.value })
      const all = (res.data?.records || res.data?.list || [])
      historyProblems.value = all.filter(p => p.id != route.params.id)
      historyTotal.value = Number(res.data?.total) || all.length
      if (historyPageNum.value === 1) problemInfo.value.historyCount = Number(res.data?.total) || historyProblems.value.length
    }
  } catch { historyProblems.value = [] }
  finally { historyLoading.value = false }
}

const loadTimelines = () => {
  timelines.value = [{ time: problemInfo.value.alarmTime || '-', type: 'danger', content: '问题产生', detail: `来源: ${sourceMap[problemInfo.value.problemSource] || '-'}` }]
}

const saveEdit = async () => {
  try {
    await updateProblem(route.params.id, editForm.value)
    ElMessage.success('保存成功')
    editMode.value = false
    fetchDetail()
  } catch (e) { ElMessage.error(e?.response?.data?.message || '保存失败') }
}

const goDispatch = () => router.push({ path: '/dispatch/task', query: { problemId: route.params.id } })

const upgradeLevel = async () => {
  try { await changeProblemLevel(route.params.id, 'I'); ElMessage.warning('已升级为严重'); fetchDetail() }
  catch (e) { ElMessage.error(e?.response?.data?.message || '升级失败') }
}
const downgradeLevel = async () => {
  try { await changeProblemLevel(route.params.id, 'III'); ElMessage.info('已降级为一般'); fetchDetail() }
  catch (e) { ElMessage.error(e?.response?.data?.message || '降级失败') }
}

const openCloseDialog = () => { closeReason.value = ''; closeDialogVisible.value = true }

const handleClose = async () => {
  try {
    await closeProblem({ ids: [Number(route.params.id)], reason: closeReason.value.trim() })
    closeDialogVisible.value = false
    ElMessage.success('已关闭')
    router.back()
  } catch (e) { ElMessage.error(e?.response?.data?.message || '关闭失败') }
}

const handleMerge = async () => {
  try {
    await mergeProblems({ ids: similarSelectedIds.value, targetId: Number(route.params.id) })
    ElMessage.success('合并成功')
    fetchDetail()
  } catch (e) { ElMessage.error(e?.response?.data?.message || '合并失败') }
}

const viewOnMap = () => {
  if (problemInfo.value.longitude && problemInfo.value.latitude) {
    router.push({ path: '/map/full', query: { lng: problemInfo.value.longitude, lat: problemInfo.value.latitude } })
  } else {
    ElMessage.warning('该问题暂无坐标信息')
  }
}

const handleTabChange = (tab) => {
  if (tab === 'similar') { similarPageNum.value = 1; loadSimilarProblems() }
  if (tab === 'history') { historyPageNum.value = 1; loadHistory() }
}

const onSimilarFilterChange = () => { similarPageNum.value = 1; loadSimilarProblems() }

onMounted(fetchDetail)
</script>

<style scoped>
.identify-container { max-width: 1400px; }
.section-card { margin-bottom: 0; }
.section-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.action-btns { display: flex; gap: 8px; flex-wrap: wrap; }
.related-info { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.related-item {
  display: flex; align-items: center; gap: 6px; padding: 12px;
  background: #f5f7fa; border-radius: 6px; cursor: pointer; font-size: 13px;
  transition: all .2s;
}
.related-item:hover { background: #ecf5ff; color: #409EFF; }
.problem-desc { white-space: pre-wrap; line-height: 1.6; }
</style>
