<template>
  <div>
    <div class="page-title">问题甄别</div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="问题详情" name="info">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="问题来源">{{ sourceMap[problemInfo.problemSource] || problemInfo.problemSource || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报警时间">{{ problemInfo.alarmTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="问题等级"><el-tag :type="problemInfo.problemLevel==='I'?'danger':problemInfo.problemLevel==='II'?'warning':''" size="small">{{ problemInfo.problemLevel==='I'?'严重':problemInfo.problemLevel==='II'?'较严重':'一般' }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="事发区域">{{ problemInfo.areaName || problemInfo.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="污染类型">{{ pollutionMap[problemInfo.pollutionType] || problemInfo.pollutionType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="事发企业">{{ problemInfo.enterpriseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="3">{{ problemInfo.problemDesc || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
      <el-tab-pane label="相似问题" name="similar">
        <el-table :data="[]" empty-text="暂无相似问题">
          <el-table-column prop="description" label="问题描述" />
          <el-table-column prop="alarmTime" label="时间" width="160" />
          <el-table-column label="操作" width="80"><template #default><el-button type="primary" link>合并</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="历史问题" name="history"><el-table :data="[]" empty-text="该企业暂无历史问题" /></el-tab-pane>
    </el-tabs>
    <div style="margin-top:20px;text-align:center">
      <el-button type="primary" @click="goDispatch">去派发</el-button>
      <el-button @click="upgradeLevel">升级为严重</el-button>
      <el-button @click="downgradeLevel">降级为一般</el-button>
      <el-button type="danger" @click="openCloseDialog">关闭问题</el-button>
    </div>
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
import { Warning } from '@element-plus/icons-vue'
import { getProblemDetail, closeProblem, changeProblemLevel } from '@/api'

const route = useRoute()
const router = useRouter()
const activeTab = ref('info')
const problemInfo = ref({})
const sourceMap = { PUBLIC_COMPLAINT:'公众投诉', FIELD_INSPECTION:'现场监察', ONLINE_MONITOR:'在线监测' }
const pollutionMap = { WASTE_WATER:'废水', WASTE_GAS:'废气', NOISE:'噪声', SOLID_WASTE:'固危废' }
const closeDialogVisible = ref(false)
const closeReason = ref('')

const openCloseDialog = () => { closeReason.value = ''; closeDialogVisible.value = true }

const fetchDetail = async () => {
  try {
    const res = await getProblemDetail(route.params.id)
    problemInfo.value = res.data || {}
  } catch { /* 后端未就绪 */ }
}

const goDispatch = () => router.push('/dispatch/task')
const upgradeLevel = async () => {
  try {
    await changeProblemLevel(route.params.id, 'I')
    ElMessage.warning('已升级为严重')
    fetchDetail()
  } catch (e) { ElMessage.error(e?.response?.data?.message || '升级失败') }
}
const downgradeLevel = async () => {
  try {
    await changeProblemLevel(route.params.id, 'III')
    ElMessage.info('已降级为一般')
    fetchDetail()
  } catch (e) { ElMessage.error(e?.response?.data?.message || '降级失败') }
}
const handleClose = async () => {
  try {
    await closeProblem({ ids: [route.params.id], reason: closeReason.value.trim() })
    closeDialogVisible.value = false
    ElMessage.success('已关闭')
    router.back()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '关闭失败')
  }
}

onMounted(fetchDetail)
</script>
