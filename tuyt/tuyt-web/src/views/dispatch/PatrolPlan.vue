<template>
  <div>
    <div class="page-title">巡查计划</div>

    <!-- 查询栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="配置类型">
          <el-select v-model="query.configType" clearable placeholder="全部" style="width:130px">
            <el-option label="日常巡查" value="PATROL" />
            <el-option label="停产巡查" value="SHUTDOWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划标题">
          <el-input v-model="query.title" clearable placeholder="请输入标题" style="width:160px" />
        </el-form-item>
        <el-form-item label="启用时间">
          <el-date-picker v-model="query.startTime" type="date" clearable placeholder="选择日期" style="width:150px" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="检查周期">
          <el-select v-model="query.cycle" clearable placeholder="全部" style="width:120px">
            <el-option label="每月" value="MONTHLY" />
            <el-option label="每季度" value="QUARTERLY" />
            <el-option label="每半年" value="SEMIANNUAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:100px">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetch">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="success" @click="openDialog()"><el-icon><Plus /></el-icon>新增计划</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 计划列表 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无巡查计划">
        <el-table-column prop="taskTitle" label="计划标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="配置类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.taskType==='SHUTDOWN'?'danger':''" size="small">
              {{ taskTypeMap[row.taskType] || row.taskType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="检查周期" width="100">
          <template #default="{ row }">{{ cycleMap[row.cycle] || row.cycle || '-' }}</template>
        </el-table-column>
        <el-table-column label="启用时间" width="110">
          <template #default="{ row }">{{ row.startTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="上次执行" width="160">
          <template #default="{ row }">{{ row.lastExecuteTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="下次执行" width="160">
          <template #default="{ row }">{{ row.nextExecuteTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="监管企业数" width="100" align="center">
          <template #default="{ row }">{{ row.enterpriseCount || 0 }} 家</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status==='ENABLED'?'success':'info'" size="small">
              {{ row.status==='ENABLED'?'启用':'停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.taskContent || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="success" @click="toggleStatus(row)">
              {{ row.status==='ENABLED'?'停用':'启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end"
        v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50,100]" :total="total"
        layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
    </div>

    <!-- 新建/编辑弹框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑巡查计划':'新增巡查计划'" width="800px" @close="resetForm">
      <el-form label-width="100px" :model="form" ref="formRef" :rules="formRules">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="配置类型" prop="taskType">
              <el-select v-model="form.taskType" style="width:100%" @change="onConfigTypeChange">
                <el-option label="日常巡查" value="PATROL" />
                <el-option label="停产巡查" value="SHUTDOWN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划标题" prop="taskTitle">
              <el-input v-model="form.taskTitle" placeholder="请输入计划标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="检查周期" prop="cycle">
              <el-select v-model="form.cycle" style="width:100%">
                <el-option label="每月" value="MONTHLY" />
                <el-option label="每季度" value="QUARTERLY" />
                <el-option label="每半年" value="SEMIANNUAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用时间" prop="startTime">
              <el-date-picker v-model="form.startTime" type="date" placeholder="选择启用日期"
                style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="检查模板">
              <el-input v-model="form.checkTemplateName" placeholder="输入模板名称快速查找" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="ENABLED">启用</el-radio>
                <el-radio value="DISABLED">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.taskContent" type="textarea" :rows="2" placeholder="计划描述（可选）" />
        </el-form-item>

        <!-- 监管企业 -->
        <el-divider content-position="left" style="margin:12px 0">
          <span style="font-size:14px;font-weight:600">监管企业</span>
          <span style="font-size:12px;color:#909399;margin-left:8px">
            {{ form.taskType==='SHUTDOWN' ? '（仅显示停产/关闭状态企业）' : '（仅显示非停产/关闭状态且未纳入日常巡查的企业）' }}
          </span>
        </el-divider>
        <div style="margin-bottom:8px">
          <el-button type="primary" size="small" @click="openEnterpriseDialog">
            <el-icon><Plus /></el-icon>添加监管企业
          </el-button>
          <el-button v-if="selectedEnterprises.length" type="danger" size="small" plain
            @click="selectedEnterprises = []; selectedEnterpriseIds = []">
            清空全部
          </el-button>
          <span v-if="selectedEnterprises.length" style="margin-left:8px;color:#606266;font-size:13px">
            已选择 <b style="color:#409EFF">{{ selectedEnterprises.length }}</b> 家企业
          </span>
        </div>
        <el-table v-if="selectedEnterprises.length" :data="selectedEnterprises" border size="small" max-height="220">
          <el-table-column type="index" label="#" width="40" />
          <el-table-column prop="enterpriseCode" label="企业编码" width="120" />
          <el-table-column prop="enterpriseName" label="企业名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="address" label="企业地址" min-width="150" show-overflow-tooltip />
          <el-table-column label="企业类型" width="90">
            <template #default="{ row }">{{ row.enterpriseType || '-' }}</template>
          </el-table-column>
          <el-table-column label="所属网格" width="100" show-overflow-tooltip>
            <template #default="{ row }">{{ row.gridName || '-' }}</template>
          </el-table-column>
          <el-table-column label="企业状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status==='SHUTDOWN'||row.status==='CLOSED'?'danger':'success'" size="small">
                {{ row.status || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="removeEnterprise($index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 选择企业弹框 -->
    <el-dialog v-model="enterpriseDialogVisible" title="选择监管企业" width="900px" append-to-body>
      <div class="search-bar" style="padding:0">
        <el-form :inline="true" :model="enterpriseQuery">
          <el-form-item label="企业名称">
            <el-input v-model="enterpriseQuery.enterpriseName" clearable placeholder="输入名称搜索" style="width:150px" />
          </el-form-item>
          <el-form-item label="所属网格">
            <el-select v-model="enterpriseQuery.gridId" clearable placeholder="全部" style="width:140px">
              <el-option v-for="g in allGrids" :key="g.id" :label="g.gridName" :value="g.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="企业类型">
            <el-select v-model="enterpriseQuery.enterpriseType" clearable placeholder="全部" style="width:120px">
              <el-option label="钢铁冶炼" value="钢铁冶炼" />
              <el-option label="机械制造" value="机械制造" />
              <el-option label="化工" value="化工" />
              <el-option label="煤炭开采" value="煤炭开采" />
              <el-option label="污水处理" value="污水处理" />
              <el-option label="电力" value="电力" />
              <el-option label="建材" value="建材" />
              <el-option label="制药" value="制药" />
              <el-option label="食品加工" value="食品加工" />
              <el-option label="纺织印染" value="纺织印染" />
              <el-option label="电镀" value="电镀" />
              <el-option label="造纸" value="造纸" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchEnterprises">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-alert
        :title="`提示：${form.taskType==='SHUTDOWN' ? '停产巡查仅可选择停产或关闭状态的企业' : '日常巡查仅可选择非停产、非关闭状态且未被其他日常巡查计划纳入的企业'}`"
        type="info" :closable="false" show-icon style="margin-bottom:12px" />
      <el-table :data="availableEnterprises" v-loading="enterpriseLoading"
        @selection-change="onEnterpriseSelect" ref="enterpriseTable"
        max-height="400" empty-text="暂无符合条件的企业">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="enterpriseCode" label="企业编码" width="120" />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="address" label="企业地址" min-width="150" show-overflow-tooltip />
        <el-table-column label="监管类型" width="90">
          <template #default="{ row }">{{ row.regulateType || '-' }}</template>
        </el-table-column>
        <el-table-column label="企业类型" width="100">
          <template #default="{ row }">{{ row.enterpriseType || '-' }}</template>
        </el-table-column>
        <el-table-column label="所属网格" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.gridName || '-' }}</template>
        </el-table-column>
        <el-table-column label="负责人" width="90">
          <template #default="{ row }">{{ row.legalPerson || '-' }}</template>
        </el-table-column>
        <el-table-column label="企业状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status==='SHUTDOWN'||row.status==='CLOSED'?'danger':'success'" size="small">
              {{ row.status || '-' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:12px;display:flex;justify-content:flex-end"
        v-model:current-page="enterprisePager.pageNum" v-model:page-size="enterprisePager.pageSize"
        :page-sizes="[10,20,50]" :total="enterpriseTotal"
        layout="total, sizes, prev, pager, next" @current-change="fetchEnterprises" @size-change="fetchEnterprises" />
      <template #footer>
        <el-button @click="enterpriseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddEnterprises">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹框 -->
    <el-dialog v-model="detailVisible" title="巡查计划详情" width="750px">
      <template v-if="detailPlan">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="计划标题">{{ detailPlan.taskTitle }}</el-descriptions-item>
          <el-descriptions-item label="配置类型">
            <el-tag :type="detailPlan.taskType==='SHUTDOWN'?'danger':''" size="small">
              {{ taskTypeMap[detailPlan.taskType] || detailPlan.taskType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="检查周期">{{ cycleMap[detailPlan.cycle] || detailPlan.cycle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="启用时间">{{ detailPlan.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="上次执行">{{ detailPlan.lastExecuteTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下次执行">{{ detailPlan.nextExecuteTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailPlan.status==='ENABLED'?'success':'info'" size="small">
              {{ detailPlan.status==='ENABLED'?'启用':'停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="检查模板">{{ detailPlan.checkTemplateName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailPlan.taskContent || '无' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider content-position="left">监管企业清单（{{ detailEnterprises.length }} 家）</el-divider>
        <el-table :data="detailEnterprises" border size="small" max-height="300" empty-text="暂无监管企业">
          <el-table-column type="index" label="#" width="40" />
          <el-table-column prop="enterpriseCode" label="企业编码" width="120" />
          <el-table-column prop="enterpriseName" label="企业名称" min-width="160" />
          <el-table-column label="企业状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status==='SHUTDOWN'||row.status==='CLOSED'?'danger':'success'" size="small">
                {{ row.status || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="所属网格" width="110" show-overflow-tooltip>
            <template #default="{ row }">{{ row.gridName || '-' }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { ElMessage, ElMessageBox, ElIcon } from 'element-plus'
import { SuccessFilled, WarningFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { getPatrolPlanList, createPatrolPlan, updatePatrolPlan, deletePatrolPlan, getEnterpriseList, getGridList } from '@/api'
import { taskTypeMap, taskTypeOptions } from '@/utils/constants'

const loading = ref(false), saving = ref(false)
const list = ref([]), total = ref(0)
const dialogVisible = ref(false), isEdit = ref(false)
const formRef = ref(null)
const enterpriseDialogVisible = ref(false)
const detailVisible = ref(false), detailPlan = ref(null), detailEnterprises = ref([])

const cycleMap = { MONTHLY: '每月', QUARTERLY: '每季度', SEMIANNUAL: '每半年' }

const query = reactive({
  configType: '', title: '', startTime: '', cycle: '', status: '',
  pageNum: 1, pageSize: 10
})

const form = reactive({
  id: null, taskTitle: '', taskType: 'PATROL', cycle: 'MONTHLY',
  startTime: '', checkTemplateName: '', status: 'ENABLED', taskContent: ''
})

const formRules = {
  taskTitle: [{ required: true, message: '请输入计划标题', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
  cycle: [{ required: true, message: '请选择检查周期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择启用时间', trigger: 'change' }]
}

// 监管企业
const selectedEnterprises = ref([])
const selectedEnterpriseIds = ref([])
const allGrids = ref([])
const availableEnterprises = ref([])
const enterpriseLoading = ref(false)
const enterpriseTotal = ref(0)
const tempSelectedRows = ref([])

const enterpriseQuery = reactive({
  enterpriseName: '', gridId: null, enterpriseType: ''
})
const enterprisePager = reactive({ pageNum: 1, pageSize: 10 })

/** 查询巡查计划列表 */
const fetch = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
    if (query.configType) params.type = query.configType
    if (query.title) params.title = query.title
    if (query.startTime) params.startTime = query.startTime
    if (query.cycle) params.cycle = query.cycle
    if (query.status) params.status = query.status
    const r = await getPatrolPlanList(params)
    list.value = r.data?.records || r.data?.list || []
    total.value = r.data?.total || list.value.length
  } catch { /* */ }
  finally { loading.value = false }
}

const reset = () => {
  query.configType = ''; query.title = ''; query.startTime = ''; query.cycle = ''; query.status = ''
  fetch()
}

/** 重置表单 */
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, { id: null, taskTitle: '', taskType: 'PATROL', cycle: 'MONTHLY',
    startTime: '', checkTemplateName: '', status: 'ENABLED', taskContent: '' })
  selectedEnterprises.value = []
  selectedEnterpriseIds.value = []
}

const onConfigTypeChange = () => {
  // 切换配置类型时清空已选企业（因为不同巡查类型对应不同企业范围）
  selectedEnterprises.value = []
  selectedEnterpriseIds.value = []
}

/** 打开编辑弹框 */
const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    form.id = row.id
    form.taskTitle = row.taskTitle || ''
    form.taskType = row.taskType || 'PATROL'
    form.cycle = row.cycle || 'MONTHLY'
    form.startTime = row.startTime || ''
    form.checkTemplateName = row.checkTemplateName || ''
    form.status = row.status || 'ENABLED'
    form.taskContent = row.taskContent?.replace(/^处理人：.*\n/, '') || ''
    // 加载已有监管企业
    selectedEnterprises.value = row.enterprises || []
    selectedEnterpriseIds.value = selectedEnterprises.value.map(e => e.id)
  } else {
    isEdit.value = false
    resetForm()
  }
  dialogVisible.value = true
}

/** 查看详情 */
const viewDetail = (row) => {
  detailPlan.value = row
  detailEnterprises.value = row.enterprises || []
  detailVisible.value = true
}

/** 启用/停用切换 */
const toggleStatus = async (row) => {
  const newStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const isEnable = newStatus === 'ENABLED'
  const action = isEnable ? '启用' : '停用'
  const actionColor = isEnable ? '#67C23A' : '#E6A23C'
  const bgColor = isEnable ? '#F0F9EB' : '#FAECDF'
  try {
    await ElMessageBox.confirm(
      h('div', { style: 'padding:6px 0 2px;' }, [
        // 标题行 + 图标
        h('div', { style: 'display:flex;align-items:center;margin-bottom:16px;' }, [
          h('div', { style: `width:36px;height:36px;border-radius:50%;background:${bgColor};display:flex;align-items:center;justify-content:center;margin-right:12px;flex-shrink:0;` },
            h(ElIcon, { style: `font-size:20px;color:${actionColor};` },
              () => h(isEnable ? SuccessFilled : WarningFilled)
            )
          ),
          h('span', { style: 'font-size:16px;font-weight:600;color:#303133;' }, `${action}巡查计划`)
        ]),
        // 计划信息卡片
        h('div', { style: 'padding:14px 16px;background:#FAFBFC;border:1px solid #EBEEF5;border-radius:8px;margin-bottom:14px;' }, [
          h('div', { style: 'display:flex;align-items:baseline;margin-bottom:8px;' }, [
            h('span', { style: 'font-size:13px;color:#909399;white-space:nowrap;margin-right:8px;' }, '计划名称'),
            h('span', { style: 'font-size:15px;color:#303133;font-weight:600;' }, row.taskTitle)
          ]),
          h('div', { style: 'display:flex;align-items:center;gap:16px;padding-top:8px;border-top:1px dashed #EBEEF5;' }, [
            h('div', { style: 'display:flex;align-items:center;gap:4px;' }, [
              h('span', { style: 'font-size:12px;color:#909399;' }, '类型'),
              h('el-tag', { size: 'small', type: row.taskType === 'SHUTDOWN' ? 'danger' : '', style: 'margin:0;' }, () => taskTypeMap[row.taskType] || row.taskType)
            ]),
            h('div', { style: 'display:flex;align-items:center;gap:4px;' }, [
              h('span', { style: 'font-size:12px;color:#909399;' }, '周期'),
              h('span', { style: 'font-size:12px;color:#606266;font-weight:500;' }, cycleMap[row.cycle] || '-')
            ]),
            h('div', { style: 'display:flex;align-items:center;gap:4px;' }, [
              h('span', { style: 'font-size:12px;color:#909399;' }, '企业'),
              h('span', { style: 'font-size:12px;color:#409EFF;font-weight:500;' }, `${row.enterpriseCount || 0} 家`)
            ])
          ])
        ]),
        // 提示信息区
        h('div', { style: `display:flex;align-items:flex-start;gap:8px;padding:12px 14px;background:${bgColor};border-radius:6px;border-left:3px solid ${actionColor};` }, [
          h(ElIcon, { style: `font-size:16px;color:${actionColor};flex-shrink:0;margin-top:1px;` },
            () => h(isEnable ? SuccessFilled : WarningFilled)
          ),
          h('div', { style: `font-size:13px;color:${actionColor === '#67C23A' ? '#529B2E' : '#B88230'};line-height:1.7;` },
            isEnable
              ? '启用后，系统将按「检查周期」自动为监管企业派发巡查任务，请确认计划信息无误。'
              : '停用后，系统将停止自动派发该巡查任务，已派发且未完成的任务不受影响。'
          )
        ])
      ]),
      `${action}巡查计划`,
      {
        confirmButtonText: `确认${action}`,
        cancelButtonText: '取消',
        type: isEnable ? 'success' : 'warning',
        buttonSize: 'default',
        customClass: 'patrol-plan-confirm-dialog'
      }
    )
    await updatePatrolPlan(row.id, { status: newStatus })
    ElMessage.success(`已${action}`)
    fetch()
  } catch { /* 用户取消 */ }
}

/** 打开企业选择弹框 */
const openEnterpriseDialog = () => {
  tempSelectedRows.value = [...selectedEnterprises.value]
  enterpriseDialogVisible.value = true
  fetchEnterprises()
}

/** 加载可用企业列表 */
const fetchEnterprises = async () => {
  enterpriseLoading.value = true
  try {
    const params = { pageNum: enterprisePager.pageNum, pageSize: enterprisePager.pageSize }
    if (enterpriseQuery.enterpriseName) params.name = enterpriseQuery.enterpriseName
    if (enterpriseQuery.gridId) params.gridId = enterpriseQuery.gridId
    if (enterpriseQuery.enterpriseType) params.enterpriseType = enterpriseQuery.enterpriseType
    // 停产巡查：仅显示停产/关闭企业；日常巡查：显示非停产/非关闭企业
    if (form.taskType === 'SHUTDOWN') {
      params.status = 'SHUTDOWN'
    } else {
      params.excludeStatus = 'SHUTDOWN,CLOSED'
    }
    const r = await getEnterpriseList(params)
    const records = r.data?.records || r.data?.list || []
    enterpriseTotal.value = r.data?.total || records.length
    availableEnterprises.value = records
  } catch { /* */ }
  finally { enterpriseLoading.value = false }
}

/** 企业表格选择事件 */
const onEnterpriseSelect = (rows) => {
  tempSelectedRows.value = rows
}

/** 确认添加选中企业 */
const confirmAddEnterprises = () => {
  // 合并去重
  const existingIds = new Set(selectedEnterpriseIds.value)
  for (const ent of tempSelectedRows.value) {
    if (!existingIds.has(ent.id)) {
      selectedEnterprises.value.push(ent)
      selectedEnterpriseIds.value.push(ent.id)
    }
  }
  ElMessage.success(`已添加 ${tempSelectedRows.value.length} 家企业`)
  enterpriseDialogVisible.value = false
}

/** 移除单个企业 */
const removeEnterprise = (index) => {
  const id = selectedEnterprises.value[index].id
  selectedEnterprises.value.splice(index, 1)
  selectedEnterpriseIds.value = selectedEnterpriseIds.value.filter(eid => eid !== id)
}

/** 保存巡查计划 */
const handleSave = async () => {
  try {
    await formRef.value.validate()
  } catch { return }
  saving.value = true
  try {
    const payload = {
      ...form,
      startTime: form.startTime ? form.startTime + ' 00:00:00' : null,
      cycle: form.cycle,
      enterprises: selectedEnterpriseIds.value.map(id => ({ id })),
      enterpriseCount: selectedEnterprises.value.length
    }
    if (isEdit.value) {
      payload.id = form.id
      await updatePatrolPlan(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createPatrolPlan(payload)
      ElMessage.success('新建成功，系统将按计划自动派发巡查任务')
    }
    dialogVisible.value = false
    fetch()
  } catch (e) { ElMessage.error(e?.response?.data?.message || '保存失败') }
  finally { saving.value = false }
}

/** 删除 */
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">计划名称</span><span class="del-value"><strong>${row.taskTitle || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">类型</span><span class="del-value">${taskTypeMap[row.taskType] || row.taskType || '-'}</span></div>
      <div class="del-row"><span class="del-label">检查周期</span><span class="del-value">${cycleMap[row.cycle] || row.cycle || '-'}</span></div>
      <div class="del-row"><span class="del-label">监管企业</span><span class="del-value">${row.enterpriseCount || 0} 家</span></div>
      <div class="del-row"><span class="del-label">状态</span><span class="del-value">${row.status==='ENABLED'?'启用':'停用'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该计划将永久移除！</div>
      <div class="del-warning-note">注：已派发的任务记录将保留不受影响。</div>
    </div>`,
    '删除巡查计划',
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
    try { await deletePatrolPlan(row.id); ElMessage.success('已删除'); fetch() }
    catch { ElMessage.error('删除失败') }
  }).catch(() => {})
}

onMounted(async () => {
  try {
    const r = await getGridList({ pageSize: 200 })
    allGrids.value = r.data?.records || r.data?.list || []
  } catch { /* */ }
  fetch()
})
</script>

<style scoped>
.search-bar { margin-bottom: 0; }
</style>

<style>
/* patrol-plan-confirm-dialog 样式已统一在 global.scss 中定义 */
</style>
