<template>
  <div>
    <div class="page-title">我的待办件</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="任务编号"><el-input v-model="query.taskNo" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="任务标题"><el-input v-model="query.title" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="query.taskType" placeholder="全部" clearable style="width: 160px">
            <el-option
              v-for="opt in taskTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="taskNo" label="任务编号" width="160" />
        <el-table-column prop="taskTitle" label="任务标题" min-width="200" />
        <el-table-column prop="taskType" label="任务类型" width="100"><template #default="{ row }">{{ taskTypeMap[row.taskType] || row.taskType }}</template></el-table-column>
        <el-table-column prop="urgency" label="紧急程度" width="90"><template #default="{ row }"><el-tag :type="urgencyTagType[row.urgency] || ''" size="small">{{ urgencyMap[row.urgency] || row.urgency }}</el-tag></template></el-table-column>
        <el-table-column prop="dispatchTime" label="派发时间" width="160" />
        <el-table-column prop="deadline" label="截止时间" width="160" />
        <el-table-column label="催办/督办" width="120" align="center">
          <template #default="{ row }">
            <div style="display:flex;flex-direction:column;gap:2px;align-items:center">
              <el-tag v-if="(row.urgeCount||0) > 0" type="warning" size="small">催办 {{ row.urgeCount }}次</el-tag>
              <el-tag v-if="(row.superviseCount||0) > 0" type="danger" size="small">督办 {{ row.superviseCount }}次</el-tag>
              <span v-if="!(row.urgeCount||0) && !(row.superviseCount||0)" style="color:#C0C4CC">—</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link @click="openHandle(row)">处理</el-button>
            <el-button type="primary" link @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
    </div>

    <!-- 查看详情 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="700px" append-to-body>
      <el-descriptions :column="2" border size="small" style="margin-bottom:16px">
        <el-descriptions-item label="任务编号">{{ currentRow.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="任务标题">{{ currentRow.taskTitle }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ typeText(currentRow.taskType) }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">
          <el-tag :type="urgencyTagType[currentRow.urgency] || ''" size="small">{{ urgencyMap[currentRow.urgency] || currentRow.urgency || '一般' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="派发时间">{{ currentRow.dispatchTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">
          <span :style="{ color: isDetailOverdue ? '#F56C6C' : '', fontWeight: isDetailOverdue ? 'bold' : '' }">
            {{ currentRow.deadline || '-' }}
            <el-tag v-if="isDetailOverdue" type="danger" size="small" style="margin-left:4px">已超期</el-tag>
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="催办">
          <el-tag v-if="(currentRow.urgeCount||0) > 0" type="warning" size="small">{{ currentRow.urgeCount }} 次</el-tag>
          <span v-else style="color:#909399">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="督办">
          <el-tag v-if="(currentRow.superviseCount||0) > 0" type="danger" size="small">{{ currentRow.superviseCount }} 次</el-tag>
          <span v-else style="color:#909399">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="派发人">{{ currentRow.dispatcher || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理单位">{{ currentRow.gridName || currentRow.handleOrg || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任务内容" :span="2">{{ currentRow.taskContent || currentRow.content || '-' }}</el-descriptions-item>
      </el-descriptions>
      <!-- 催办/督办历史 -->
      <div v-if="combinedHistory.length" style="margin-top:8px">
        <el-divider content-position="left">
          <span style="font-size:14px;font-weight:500">催办/督办记录</span>
          <el-tag v-if="combinedHistory.length" type="info" size="small" style="margin-left:6px">{{ combinedHistory.length }}条</el-tag>
        </el-divider>
        <el-timeline>
          <el-timeline-item
            v-for="(item, idx) in combinedHistory"
            :key="idx"
            :timestamp="item.time"
            :color="item.type === 'supervise' ? '#F56C6C' : '#E6A23C'"
            placement="top"
          >
            <el-tag :type="item.type === 'supervise' ? 'danger' : 'warning'" size="small" style="margin-bottom:4px">{{ item.label }}</el-tag>
            <div v-if="item.reason" style="color:#606266;font-size:13px;margin-top:2px">原因：{{ item.reason }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 处理任务 -->
    <el-dialog v-model="handleVisible" title="任务处理" width="700px" append-to-body>
      <el-alert title="请认真填写各项检查信息，确保执法规范和取证完整" type="warning" :closable="false" show-icon style="margin-bottom:16px" />
      <el-form :model="handleForm" label-width="120px" ref="handleRef">
        <el-form-item label="处理日期" prop="processDate" :rules="[{ required: true, message: '请选择处理日期' }]">
          <el-date-picker v-model="handleForm.processDate" type="date" placeholder="选择日期"
            style="width:100%" value-format="YYYY-MM-DD" :disabled-date="disabledDate" />
        </el-form-item>
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
          <el-date-picker v-model="handleForm.rectifyDeadline" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="生产经营情况" prop="productionStatus" :rules="[{ required: true, message: '请选择生产经营情况' }]">
          <el-radio-group v-model="handleForm.productionStatus">
            <el-radio label="NORMAL">正常生产</el-radio>
            <el-radio label="SHUTDOWN">停产</el-radio>
            <el-radio label="CLOSED">关闭</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 协作人员 -->
        <el-divider content-position="left" style="margin:8px 0">
          <span style="font-size:13px;font-weight:500">协作人员</span>
        </el-divider>
        <div style="margin-bottom:8px">
          <el-button type="primary" size="small" link @click="addCollaborator"><el-icon><Plus /></el-icon>添加同行人员</el-button>
          <span v-if="handleForm.collaborators.length" style="margin-left:6px;color:#606266;font-size:12px">
            共 {{ handleForm.collaborators.length }} 人
          </span>
        </div>
        <div v-for="(co, idx) in handleForm.collaborators" :key="idx" style="margin-bottom:8px;padding:8px 12px;background:#f5f7fa;border-radius:6px">
          <el-row :gutter="12" align="middle">
            <el-col :span="8">
              <el-form-item label="姓名" label-width="48px" :prop="`collaborators.${idx}.name`" :rules="[{ required: true, message: '必填' }]">
                <el-input v-model="co.name" placeholder="输入姓名" size="small" />
              </el-form-item>
            </el-col>
            <el-col :span="4" style="text-align:center;padding-top:4px">
              <el-checkbox v-model="co.attended" label="到场" size="small" />
            </el-col>
            <el-col :span="8">
              <el-form-item label="执法文号" label-width="72px">
                <el-input v-model="co.enforceNo" placeholder="自动关联" size="small" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="3" style="text-align:right">
              <el-button link type="danger" size="small" @click="removeCollaborator(idx)">删除</el-button>
            </el-col>
          </el-row>
        </div>

        <!-- 现场取证附件 -->
        <el-divider content-position="left" style="margin:8px 0">
          <span style="font-size:13px;font-weight:500">现场取证材料</span>
        </el-divider>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-upload
              action="#" :auto-upload="false" :show-file-list="false"
              :on-change="(file) => onFileChange(file, 'doorPhoto')"
              accept="image/*"
            >
              <el-button size="small">
                <el-icon><Upload /></el-icon>大门照片 *
              </el-button>
            </el-upload>
            <div v-if="attachFiles.doorPhoto.length" style="margin-top:4px">
              <el-tag v-for="(f,i) in attachFiles.doorPhoto" :key="i" size="small" closable @close="removeFile('doorPhoto',i)" style="margin:2px">{{ f.name }}</el-tag>
            </div>
          </el-col>
          <el-col :span="8">
            <el-upload action="#" :auto-upload="false" :show-file-list="false" :on-change="(file) => onFileChange(file, 'outfallPhoto')" accept="image/*">
              <el-button size="small"><el-icon><Upload /></el-icon>排口照片</el-button>
            </el-upload>
            <div v-if="attachFiles.outfallPhoto.length" style="margin-top:4px">
              <el-tag v-for="(f,i) in attachFiles.outfallPhoto" :key="i" size="small" closable @close="removeFile('outfallPhoto',i)" style="margin:2px">{{ f.name }}</el-tag>
            </div>
          </el-col>
          <el-col :span="8">
            <el-upload action="#" :auto-upload="false" :show-file-list="false" :on-change="(file) => onFileChange(file, 'facilityPhoto')" accept="image/*">
              <el-button size="small"><el-icon><Upload /></el-icon>治理设施照片</el-button>
            </el-upload>
            <div v-if="attachFiles.facilityPhoto.length" style="margin-top:4px">
              <el-tag v-for="(f,i) in attachFiles.facilityPhoto" :key="i" size="small" closable @close="removeFile('facilityPhoto',i)" style="margin:2px">{{ f.name }}</el-tag>
            </div>
          </el-col>
          <el-col :span="8" style="margin-top:8px">
            <el-upload action="#" :auto-upload="false" :show-file-list="false" :on-change="(file) => onFileChange(file, 'riskPhoto')" accept="image/*">
              <el-button size="small"><el-icon><Upload /></el-icon>风险单元照片</el-button>
            </el-upload>
            <div v-if="attachFiles.riskPhoto.length" style="margin-top:4px">
              <el-tag v-for="(f,i) in attachFiles.riskPhoto" :key="i" size="small" closable @close="removeFile('riskPhoto',i)" style="margin:2px">{{ f.name }}</el-tag>
            </div>
          </el-col>
          <el-col :span="8" style="margin-top:8px">
            <el-upload action="#" :auto-upload="false" :show-file-list="false" :on-change="(file) => onFileChange(file, 'audio')" accept="audio/*">
              <el-button size="small"><el-icon><Upload /></el-icon>取证音频</el-button>
            </el-upload>
            <div v-if="attachFiles.audio.length" style="margin-top:4px">
              <el-tag v-for="(f,i) in attachFiles.audio" :key="i" size="small" closable @close="removeFile('audio',i)" style="margin:2px">{{ f.name }}</el-tag>
            </div>
          </el-col>
          <el-col :span="8" style="margin-top:8px">
            <el-upload action="#" :auto-upload="false" :show-file-list="false" :on-change="(file) => onFileChange(file, 'other')" accept="*">
              <el-button size="small"><el-icon><Upload /></el-icon>其他附件</el-button>
            </el-upload>
            <div v-if="attachFiles.other.length" style="margin-top:4px">
              <el-tag v-for="(f,i) in attachFiles.other" :key="i" size="small" closable @close="removeFile('other',i)" style="margin:2px">{{ f.name }}</el-tag>
            </div>
          </el-col>
        </el-row>
        <div v-if="!attachFiles.doorPhoto.length" style="color:#F56C6C;font-size:12px;margin-top:4px">* 大门照片为必填项</div>

        <el-form-item label="备注" style="margin-top:16px">
          <el-input v-model="handleForm.remark" type="textarea" :rows="2" placeholder="请输入备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="danger" link @click="goToPenalty" style="margin-right:auto">行政处罚</el-button>
        <el-button type="primary" @click="submitHandle" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { getTodoList, getTaskDetail, processTask } from '@/api'
import { taskTypeMap, taskTypeOptions, urgencyMap, urgencyTagType } from '@/utils/constants'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ taskNo: '', title: '', taskType: '', pageNum: 1, pageSize: 10 })

const detailVisible = ref(false)
const handleVisible = ref(false)
const currentRow = ref({})
const submitting = ref(false)
const handleRef = ref(null)

const todayStr = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

const handleForm = reactive({
  processDate: todayStr(),
  conclusion: '', suggestion: '',
  rectifyStatus: '', rectifyDeadline: '',
  productionStatus: '', remark: '',
  collaborators: []
})

/** 附件文件列表 */
const attachFiles = reactive({
  doorPhoto: [],
  outfallPhoto: [],
  facilityPhoto: [],
  riskPhoto: [],
  audio: [],
  other: []
})

/** 限制日期选择：只能选今天及以前 */
const disabledDate = (time) => time.getTime() > Date.now()

const typeText = (v) => taskTypeMap[v] || v

/** 详情弹框：当前任务是否超期 */
const isDetailOverdue = computed(() => {
  if (!currentRow.value.deadline || currentRow.value.status === 'DONE') return false
  return new Date(currentRow.value.deadline) < new Date()
})

/** 从 taskContent 解析催办/督办历史 */
function parseUrgeHistory(content) {
  if (!content) return []
  const records = []
  const urgeRegex = /=== 催办记录 \[([^\]]+)\] ===\n?(?:原因:\s*(.*?))?(?=\n===|\n?$)/gs
  const supRegex = /=== 督办记录 \[([^\]]+)\] ===\n?(?:原因:\s*(.*?))?(?=\n===|\n?$)/gs
  let m
  while ((m = urgeRegex.exec(content)) !== null) {
    records.push({ time: m[1], reason: (m[2] || '').trim(), type: 'urge', label: '催办' })
  }
  while ((m = supRegex.exec(content)) !== null) {
    records.push({ time: m[1], reason: (m[2] || '').trim(), type: 'supervise', label: '督办' })
  }
  records.sort((a, b) => b.time.localeCompare(a.time))
  return records
}

const combinedHistory = computed(() => {
  const raw = currentRow.value.taskContent || currentRow.value.content || ''
  return parseUrgeHistory(raw)
})

const fetch = async () => {
  loading.value = true
  const res = await getTodoList({ ...query }).catch(() => ({ data: { records: [], total: 0 } }))
  const d = res.data || {}
  list.value = d.records || d.list || []
  total.value = Number(d.total) || 0
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

/** 重置表单 */
const resetHandleForm = () => {
  handleForm.processDate = todayStr()
  handleForm.conclusion = ''
  handleForm.suggestion = ''
  handleForm.rectifyStatus = ''
  handleForm.rectifyDeadline = ''
  handleForm.productionStatus = ''
  handleForm.remark = ''
  handleForm.collaborators = []
  Object.keys(attachFiles).forEach(k => { attachFiles[k] = [] })
}

const openHandle = (row) => {
  currentRow.value = row
  resetHandleForm()
  handleVisible.value = true
}

/** 添加协作人 */
const addCollaborator = () => {
  handleForm.collaborators.push({ name: '', attended: false, enforceNo: '' })
}

/** 移除协作人 */
const removeCollaborator = (idx) => {
  handleForm.collaborators.splice(idx, 1)
}

/** 文件选择 */
const onFileChange = (file, type) => {
  if (!attachFiles[type]) attachFiles[type] = []
  // 模拟执法文号生成
  if (type === 'doorPhoto' && attachFiles.doorPhoto.length === 0) {
    handleForm.collaborators.forEach(co => {
      if (!co.enforceNo) co.enforceNo = 'ZF-' + new Date().getFullYear() + String(Math.random()).substring(2, 8)
    })
  }
  attachFiles[type].push(file)
}

/** 移除文件 */
const removeFile = (type, idx) => {
  attachFiles[type].splice(idx, 1)
}

/** 跳转行政处罚 */
const goToPenalty = () => {
  ElMessage.info('跳转至行政处罚立案界面（功能开发中）')
}

const submitHandle = async () => {
  if (!handleRef.value) return
  // 验证大门照片
  if (attachFiles.doorPhoto.length === 0) {
    ElMessage.warning('请上传大门照片（必填）')
    return
  }
  try {
    await handleRef.value.validate()
  } catch { return }
  submitting.value = true
  try {
    const payload = {
      ...handleForm,
      attachments: Object.entries(attachFiles).reduce((acc, [k, v]) => {
        acc[k] = v.length
        return acc
      }, {})
    }
    await processTask(currentRow.value.id, payload)
    ElMessage.success('任务处理成功')
    handleVisible.value = false
    fetch()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '处理失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetch)
</script>
