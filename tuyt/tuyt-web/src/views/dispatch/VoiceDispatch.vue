<template>
  <div>
    <div class="page-title">语音调度</div>

    <!-- 快速拨号面板 -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="8">
        <div class="quick-call-card">
          <div class="quick-call-icon emergency"><el-icon :size="28"><PhoneFilled /></el-icon></div>
          <div class="quick-call-info">
            <div class="quick-call-title">应急指挥中心</div>
            <div class="quick-call-desc">紧急事件一键调度</div>
          </div>
          <a href="tel:12369" style="text-decoration:none"><el-button type="danger" :icon="Phone" circle @click="recordQuickCall('应急指挥中心','12369','热线','环保应急')" /></a>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="quick-call-card">
          <div class="quick-call-icon warn"><el-icon :size="28"><Headset /></el-icon></div>
          <div class="quick-call-info">
            <div class="quick-call-title">环保执法大队</div>
            <div class="quick-call-desc">执法支援调度</div>
          </div>
          <a href="tel:13800138002" style="text-decoration:none"><el-button type="warning" :icon="Phone" circle @click="recordQuickCall('环保执法大队','13800138002','执法大队','环保局')" /></a>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="quick-call-card">
          <div class="quick-call-icon info"><el-icon :size="28"><UserFilled /></el-icon></div>
          <div class="quick-call-info">
            <div class="quick-call-title">技术专家组</div>
            <div class="quick-call-desc">技术咨询调度</div>
          </div>
          <a href="tel:13800138005" style="text-decoration:none"><el-button type="primary" :icon="Phone" circle @click="recordQuickCall('技术专家组','13800138005','专家组','环保局')" /></a>
        </div>
      </el-col>
    </el-row>

    <!-- 最近通话 + 短信历史标签页 -->
    <el-card shadow="never" style="margin-bottom: 16px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:600">{{ activeTab === 'call' ? '最近通话' : '短信发送记录' }}</span>
          <div>
            <el-button link type="primary" @click="activeTab='call'" :style="{ fontWeight: activeTab==='call' ? 700 : 400 }">通话</el-button>
            <el-divider direction="vertical" />
            <el-button link type="primary" @click="activeTab='sms'" :style="{ fontWeight: activeTab==='sms' ? 700 : 400 }">短信</el-button>
          </div>
        </div>
      </template>

      <!-- 最近通话 -->
      <div v-if="activeTab==='call'" class="recent-calls">
        <div v-for="call in recentCalls" :key="call.id" class="recent-call-item">
          <div class="call-avatar">{{ call.calleeName?.charAt(0) || call.callerName?.charAt(0) || '联' }}</div>
          <div class="call-info">
            <div class="call-name">{{ call.callType === 'OUTGOING' ? call.calleeName : call.callerName }}</div>
            <div class="call-sub">{{ call.callType === 'OUTGOING' ? call.calleePhone : call.callerPhone }} &nbsp; {{ formatCallTime(call.callTime) }}</div>
          </div>
          <div class="call-status">
            <el-tag v-if="call.callType==='OUTGOING'" type="success" size="small" effect="dark">已呼出</el-tag>
            <el-tag v-else type="info" size="small" effect="dark">已呼入</el-tag>
            <span v-if="call.duration" style="margin-left:6px;font-size:12px;color:#909399">{{ formatDuration(call.duration) }}</span>
          </div>
          <a :href="`tel:${call.callType==='OUTGOING'?call.calleePhone:call.callerPhone}`" style="text-decoration:none">
            <el-button link type="primary" :icon="Phone">重拨</el-button>
          </a>
        </div>
        <el-empty v-if="!recentCalls.length" description="暂无通话记录" :image-size="80" />
      </div>

      <!-- 短信历史 -->
      <div v-if="activeTab==='sms'" class="recent-calls">
        <div v-for="sms in smsHistory" :key="sms.id" class="recent-call-item">
          <div class="call-avatar sms-avatar"><el-icon><ChatDotRound /></el-icon></div>
          <div class="call-info" style="flex:2">
            <div class="call-name">{{ sms.content?.substring(0,35) }}{{ (sms.content?.length||0)>35?'...':'' }}</div>
            <div class="call-sub">
              <el-tag :type="sms.smsType==='BATCH'?'warning':''" size="small" effect="plain">
                {{ sms.smsType==='BATCH'?'群发':'单发' }}
              </el-tag>
              <span style="margin-left:6px">{{ sms.recipientNames || sms.recipientPhones }}</span>
            </div>
          </div>
          <div style="text-align:right;min-width:100px">
            <div style="font-size:12px;color:#909399">{{ formatCallTime(sms.sendTime) }}</div>
            <div style="font-size:12px">
              <span style="color:#67C23A" v-if="sms.status==='SUCCESS'">发送成功</span>
              <span style="color:#E6A23C" v-else-if="sms.status==='PARTIAL'">部分成功</span>
              <span style="color:#F56C6C" v-else>发送失败</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!smsHistory.length" description="暂无短信记录" :image-size="80" />
      </div>
    </el-card>

    <!-- 通讯录 + 批量操作工具栏 -->
    <div class="toolbar">
      <el-form :inline="true" :model="query" style="flex:1">
        <el-form-item label="姓名"><el-input v-model="query.name" clearable placeholder="搜索联系人" @clear="search" @keyup.enter="search" /></el-form-item>
        <el-form-item label="职位"><el-input v-model="query.position" clearable placeholder="按职位筛选" @clear="search" @keyup.enter="search" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.contactType" clearable placeholder="全部" style="width:100px" @change="search">
            <el-option label="单位" value="ORG" />
            <el-option label="个人" value="PERSON" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      <div style="display:flex;gap:8px">
        <el-button type="success" :icon="ChatDotRound" :disabled="!selectedRows.length" @click="openBatchSmsDialog">
          批量短信({{ selectedRows.length }})
        </el-button>
      </div>
    </div>

    <!-- 联系人列表 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无联系人" @selection-change="handleSelection">
        <el-table-column type="selection" width="40" />
        <el-table-column label="" width="50">
          <template #default="{ row }">
            <div class="contact-avatar">{{ row.name?.charAt(0) || '联' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="类型" width="70">
          <template #default="{ row }">
            <el-tag :type="row.contactType==='ORG'?'info':''" size="small">{{ row.contactType==='ORG'?'单位':'个人' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="140">
          <template #default="{ row }">
            <span style="font-weight:500;color:#409EFF">{{ row.phone }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orgName" label="所属机构" min-width="140" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <a :href="`tel:${row.phone}`" style="text-decoration:none"><el-button type="primary" :icon="Phone" size="small" @click="recordCallFor(row)">拨号</el-button></a>
            <el-button type="success" :icon="ChatDotRound" size="small" @click="openSmsDialog(row)">短信</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetchData" @size-change="fetchData" />
    </div>

    <!-- 短信发送弹窗 -->
    <el-dialog v-model="smsDialogVisible" :title="smsIsBatch ? '批量发送短信' : '发送短信'" width="520px" top="15vh" :close-on-click-modal="false">
      <el-form :model="smsForm" label-width="90px">
        <el-form-item label="收件人">
          <el-input v-if="!smsIsBatch" :value="smsForm.recipientLabel" disabled />
          <div v-else class="batch-recipients">
            <el-tag v-for="r in selectedRows" :key="r.id" closable type="info" size="small" style="margin:2px" @close="removeFromSelection(r)">
              {{ r.name }}({{ r.phone }})
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="短信模板">
          <el-select v-model="selectedTemplate" placeholder="选择快捷模板" clearable @change="applyTemplate" style="width:100%">
            <el-option v-for="t in smsTemplates" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="短信内容" required>
          <el-input v-model="smsForm.content" type="textarea" :rows="4" placeholder="请输入短信内容..." maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="smsDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="smsSending" @click="doSendSms">发送</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Phone, PhoneFilled, Headset, UserFilled, ChatDotRound } from '@element-plus/icons-vue'
import { getContactList, sendSms, batchSendSms, recordCallApi, getRecentCalls, getSmsList } from '@/api'

const loading = ref(false), list = ref([]), total = ref(0)
const selectedRows = ref([])
const activeTab = ref('call')

const query = reactive({ name: '', position: '', contactType: '', pageNum: 1, pageSize: 10 })

// ==================== 短信模板 ====================
const smsTemplates = [
  { label: '【紧急调度】发现问题，请立即处理', value: '【大同环保·紧急调度】发现环境问题，请立即前往现场核查处理。' },
  { label: '【任务下发】新的巡查任务已分配', value: '【大同环保·任务通知】新的巡查任务已下发，请在规定时间内完成巡查并反馈结果。' },
  { label: '【会议通知】请准时参加调度会议', value: '【大同环保·会议通知】请于指定时间准时参加综合指挥调度会议。' },
  { label: '【催办提醒】任务处理即将超时', value: '【大同环保·催办提醒】您处理的任务即将超出时限，请尽快完成处理并提交反馈。' },
  { label: '【整改通知】企业需限期整改', value: '【大同环保·整改通知】经检查发现存在环保问题，请于收到通知后限期完成整改。' },
  { label: '【预警通知】监测数据异常', value: '【大同环保·预警通知】在线监测数据显示异常指标，请关注并采取相应措施。' }
]

// ==================== 短信弹窗 ====================
const smsDialogVisible = ref(false), smsIsBatch = ref(false), smsSending = ref(false)
const selectedTemplate = ref('')
const smsForm = reactive({ content: '', recipientLabel: '', recipientPhone: '', recipientName: '' })

const openSmsDialog = (row) => {
  smsIsBatch.value = false
  smsForm.content = ''
  smsForm.recipientLabel = `${row.name} (${row.phone})`
  smsForm.recipientPhone = row.phone
  smsForm.recipientName = row.name
  selectedTemplate.value = ''
  smsDialogVisible.value = true
}

const openBatchSmsDialog = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择联系人')
    return
  }
  smsIsBatch.value = true
  smsForm.content = ''
  selectedTemplate.value = ''
  smsDialogVisible.value = true
}

const removeFromSelection = (row) => {
  selectedRows.value = selectedRows.value.filter(r => r.id !== row.id)
  if (!selectedRows.value.length) smsDialogVisible.value = false
}

const applyTemplate = (val) => {
  if (val) {
    const t = smsTemplates.find(t => t.value === val)
    if (t) smsForm.content = t.content
  }
}

const doSendSms = async () => {
  if (!smsForm.content.trim()) {
    ElMessage.warning('请输入短信内容')
    return
  }
  smsSending.value = true
  try {
    if (smsIsBatch.value) {
      const data = {
        content: smsForm.content,
        senderName: '调度员',
        phones: selectedRows.value.map(r => r.phone),
        names: selectedRows.value.map(r => r.name)
      }
      await batchSendSms(data)
      ElMessage.success(`已向 ${selectedRows.value.length} 位联系人发送短信`)
    } else {
      await sendSms({
        content: smsForm.content,
        senderName: '调度员',
        recipientNames: smsForm.recipientName,
        recipientPhones: smsForm.recipientPhone,
        recipientCount: 1,
        smsType: 'MANUAL'
      })
      ElMessage.success('短信发送成功')
    }
    smsDialogVisible.value = false
    loadSmsHistory()
  } catch {
    ElMessage.error('短信发送失败')
  } finally {
    smsSending.value = false
  }
}

// ==================== 通话记录 ====================
const recentCalls = ref([])
const smsHistory = ref([])

const loadRecentCalls = async () => {
  try {
    const res = await getRecentCalls(10)
    recentCalls.value = res.data || []
  } catch { /* */ }
}

const loadSmsHistory = async () => {
  try {
    const res = await getSmsList({ pageNum: 1, pageSize: 20 })
    smsHistory.value = res.data?.records || res.data?.list || res.data || []
  } catch { /* */ }
}

const recordCallFor = async (row) => {
  try {
    await recordCallApi({
      callerName: '调度员',
      callerPhone: '13800000030',
      calleeName: row.name,
      calleePhone: row.phone,
      calleePosition: row.position,
      calleeOrgName: row.orgName,
      callType: 'OUTGOING'
    })
  } catch { /* */ }
  loadRecentCalls()
}

const recordQuickCall = async (name, phone, position, orgName) => {
  try {
    await recordCallApi({
      callerName: '调度员',
      callerPhone: '13800000030',
      calleeName: name,
      calleePhone: phone,
      calleePosition: position,
      calleeOrgName: orgName,
      callType: 'OUTGOING'
    })
  } catch { /* */ }
  loadRecentCalls()
}

const formatCallTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  const isYesterday = d.toDateString() === yesterday.toDateString()
  const hm = d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  if (isToday) return hm
  if (isYesterday) return `昨天 ${hm}`
  return `${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${hm}`
}

const formatDuration = (sec) => {
  if (!sec) return ''
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return m > 0 ? `${m}分${s}秒` : `${s}秒`
}

// ==================== 表格 ====================
const handleSelection = (rows) => { selectedRows.value = rows }

const fetchData = async () => {
  loading.value = true
  try {
    // 将 name + position 合并为后端识别的 keyword 参数
    const keyword = [query.name, query.position].filter(Boolean).join(' ')
    const res = await getContactList({ keyword, contactType: query.contactType, pageNum: query.pageNum, pageSize: query.pageSize })
    list.value = res.data?.records || res.data?.list || res.data || []
    total.value = res.data?.total ?? list.value.length
  } catch { /* */ }
  finally { loading.value = false }
}
const search = () => { query.pageNum = 1; fetchData() }

const reset = () => {
  query.name = ''; query.position = ''; query.contactType = ''
  search()
}

onMounted(() => {
  search()
  loadRecentCalls()
  loadSmsHistory()
})
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 700; color: #303133; margin-bottom: 16px; padding: 16px 0 0 0; }

.quick-call-card {
  background: #fff; border-radius: 8px; padding: 16px; display: flex; align-items: center; gap: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,.05);
}
.quick-call-icon { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.quick-call-icon.emergency { background: #fef0f0; color: #F56C6C; }
.quick-call-icon.warn { background: #fdf6ec; color: #E6A23C; }
.quick-call-icon.info { background: #ecf5ff; color: #409EFF; }
.quick-call-info { flex: 1; }
.quick-call-title { font-weight: 600; color: #303133; }
.quick-call-desc { font-size: 12px; color: #909399; margin-top: 2px; }

.recent-calls { display: flex; flex-direction: column; gap: 0; }
.recent-call-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f5f7fa; }
.recent-call-item:last-child { border-bottom: none; }
.call-avatar, .contact-avatar {
  width: 36px; height: 36px; border-radius: 50%; background: #ecf5ff; color: #409EFF;
  display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 14px;
  flex-shrink: 0;
}
.call-avatar.sms-avatar { background: #f0f9eb; color: #67C23A; }
.call-info { flex: 1; min-width: 0; }
.call-name { font-size: 14px; font-weight: 500; }
.call-sub { font-size: 12px; color: #909399; margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.toolbar { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 12px; }

.batch-recipients { max-height: 100px; overflow-y: auto; padding: 4px; border: 1px solid #dcdfe6; border-radius: 4px; }

.table-card { background: #fff; border-radius: 8px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,.05); }
</style>
