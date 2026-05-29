<template>
  <div>
    <div class="page-title">
      一企一档
      <el-button type="primary" size="small" style="margin-left:16px" @click="editDialogVisible = true">编辑</el-button>
    </div>
    <el-tabs v-model="activeTab">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="企业名称">{{ info.enterpriseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="企业编码">{{ info.enterpriseCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="法人代表">{{ info.legalPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="法人电话">{{ info.legalPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="企业类型">{{ info.enterpriseType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="污染类型">{{ pollutionTypeMap[info.pollutionType] || info.pollutionType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="监管类型">
            <el-tag v-if="info.superviseType === 'I'" type="danger" size="small">省重点</el-tag>
            <el-tag v-else-if="info.superviseType === 'II'" type="warning" size="small">市重点</el-tag>
            <el-tag v-else-if="info.superviseType === 'III'" type="" size="small">区属重点</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="环境信用等级">
            <el-tag v-if="info.creditLevel" :type="info.creditLevel==='A'?'success':info.creditLevel==='B'?'':'danger'" size="small">{{ info.creditLevel }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="生产状态">
            <el-tag v-if="info.productionStatus != null" :type="productionStatusNumMap[info.productionStatus]?.tagType" size="small">{{ productionStatusNumMap[info.productionStatus]?.label || '-' }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="来源类型">{{ info.sourceType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属网格">{{ info.gridName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ info.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="经度">{{ info.longitude || '-' }}</el-descriptions-item>
          <el-descriptions-item label="纬度">{{ info.latitude || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <!-- 物联监管 -->
      <el-tab-pane label="物联监管" name="iot">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="视频监控">
            <el-tag v-if="info.hasVideo === 1" type="success" size="small">已部署</el-tag>
            <el-tag v-else type="info" size="small">未部署</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="在线监测">
            <el-tag v-if="info.hasMonitor === 1" type="success" size="small">已部署</el-tag>
            <el-tag v-else type="info" size="small">未部署</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="工况监控">
            <el-tag v-if="info.hasWorking === 1" type="success" size="small">已部署</el-tag>
            <el-tag v-else type="info" size="small">未部署</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-empty v-if="info.hasVideo !== 1 && info.hasMonitor !== 1 && info.hasWorking !== 1" description="该企业暂未部署物联监控设备" style="margin-top:16px" />
      </el-tab-pane>

      <!-- 许可证 -->
      <el-tab-pane label="许可证" name="license">
        <el-empty description="排污许可证信息" />
        <el-descriptions :column="2" border size="small" style="margin-top:12px">
          <el-descriptions-item label="污染类型">{{ pollutionTypeMap[info.pollutionType] || info.pollutionType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="企业类型">{{ info.enterpriseType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="环境信用等级">
            <el-tag v-if="info.creditLevel" :type="info.creditLevel==='A'?'success':'warning'" size="small">{{ info.creditLevel }}级</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <!-- 历史问题 -->
      <el-tab-pane label="历史问题" name="history" lazy>
        <el-table :data="problemList" v-loading="problemLoading" stripe size="small" highlight-current-row @row-click="goProblem">
          <el-table-column prop="problemNo" label="问题编号" width="140" />
          <el-table-column prop="problemDesc" label="问题描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="handleStatus" label="处理状态" width="100">
            <template #default="{ row }">
              <el-tag :type="handleStatusTagType[row.handleStatus] || 'info'" size="small">{{ handleStatusMap[row.handleStatus] || row.handleStatus || '待处理' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reportTime" label="上报时间" width="160" />
        </el-table>
        <el-empty v-if="!problemLoading && !problemList.length" description="该企业暂无历史问题记录" style="margin-top:16px" />
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑企业弹框 -->
    <el-dialog v-model="editDialogVisible" title="编辑企业信息" width="650px" append-to-body>
      <el-form :model="editForm" label-width="100px" ref="editFormRef" :rules="editRules">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="enterpriseName"><el-input v-model="editForm.enterpriseName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业编码" prop="enterpriseCode"><el-input v-model="editForm.enterpriseCode" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="法人代表" prop="legalPerson"><el-input v-model="editForm.legalPerson" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法人电话"><el-input v-model="editForm.legalPhone" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="企业地址" prop="address"><el-input v-model="editForm.address" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="监管类型"><el-select v-model="editForm.superviseType" style="width:100%"><el-option label="省重点" value="I" /><el-option label="市重点" value="II" /><el-option label="区属重点" value="III" /></el-select></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="企业类型"><el-input v-model="editForm.enterpriseType" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="污染类型"><el-input v-model="editForm.pollutionType" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="信用等级"><el-select v-model="editForm.creditLevel" style="width:100%"><el-option label="A级" value="A" /><el-option label="B级" value="B" /><el-option label="C级" value="C" /></el-select></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="生产状态"><el-select v-model="editForm.productionStatus" style="width:100%"><el-option v-for="(cfg, val) in productionStatusNumMap" :key="val" :label="cfg.label" :value="Number(val)" /></el-select></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源类型"><el-input v-model="editForm.sourceType" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSave" :loading="editSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getEnterpriseDetail, updateEnterprise, getProblemList } from '@/api'
import { pollutionTypeMap, productionStatusNumMap, handleStatusMap, handleStatusTagType } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const activeTab = ref('basic')
const info = ref({})

// 历史问题
const problemList = ref([])
const problemLoading = ref(false)

// 编辑弹框
const editDialogVisible = ref(false)
const editSaving = ref(false)
const editFormRef = ref(null)
const editForm = ref({})
const editRules = {
  enterpriseName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  enterpriseCode: [{ required: true, message: '请输入企业编码', trigger: 'blur' }],
  legalPerson: [{ required: true, message: '请输入法人代表', trigger: 'blur' }],
  address: [{ required: true, message: '请输入企业地址', trigger: 'blur' }]
}

const loadDetail = async () => {
  try {
    const res = await getEnterpriseDetail(route.params.id)
    if (res.data) info.value = res.data
  } catch { /* */ }
}

const loadProblems = async () => {
  problemLoading.value = true
  try {
    const res = await getProblemList({ enterpriseId: route.params.id, pageSize: 200 })
    const d = res.data || {}
    const allProblems = d.records || d.list || []
    // 客户端兜底过滤：确保只展示当前企业的问题（防止后端 enterpriseId 未生效）
    const eid = Number(route.params.id)
    problemList.value = allProblems.filter(p => p.enterpriseId === eid || Number(p.enterpriseId) === eid)
  } catch { problemList.value = [] }
  finally { problemLoading.value = false }
}

// 切换到历史问题 tab 时加载
watch(activeTab, (tab) => {
  if (tab === 'history' && !problemList.value.length && !problemLoading.value) loadProblems()
})

/** 点击问题行 → 跳转到问题甄别详情页 */
const goProblem = (row) => {
  if (row && row.id) router.push(`/dispatch/problem/${row.id}`)
}

const handleEditSave = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate().catch(() => { throw new Error('validate') })
  editSaving.value = true
  try {
    await updateEnterprise(route.params.id, editForm.value)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadDetail()
  } catch (e) {
    if (e.message !== 'validate') ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    editSaving.value = false
  }
}

// 编辑按钮打开时预填表单
watch(editDialogVisible, (v) => {
  if (v) editForm.value = { ...info.value }
})

onMounted(() => { loadDetail() })
</script>
