<template>
  <div>
    <div class="page-title">考评管理</div>
    <el-tabs v-model="activeTab" class="assess-tabs">
      <!-- 考评规则设置 -->
      <el-tab-pane label="考评规则设置" name="rules">
        <div class="panel-header">
          <span class="panel-subtitle">配置考评周期和评分规则，支持月度、季度、年度多种考核方式</span>
          <el-button type="primary" @click="openRuleDialog()"><el-icon><Plus /></el-icon>新增规则</el-button>
        </div>
        <div class="table-card">
          <el-table :data="ruleList" v-loading="ruleLoading" empty-text="暂无规则，请点击「新增规则」添加" stripe>
            <el-table-column prop="ruleName" label="规则名称" min-width="200" />
            <el-table-column prop="version" label="版本号" width="100" />
            <el-table-column prop="category" label="考评周期" width="100">
              <template #default="{ row }">
                <el-tag :type="row.category==='月度'?'':row.category==='季度'?'warning':'success'" size="small">{{ row.category || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openRuleDialog(row)">编辑</el-button>
                <el-divider direction="vertical" />
                <el-button link type="danger" size="small" @click="confirmDelRule(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="rulePageNum" v-model:page-size="rulePageSize" :page-sizes="[10,20,50]" :total="ruleTotal" layout="total, sizes, prev, pager, next" @current-change="fetchRules" @size-change="fetchRules" />
        </div>
      </el-tab-pane>

      <!-- 考评指标管理 -->
      <el-tab-pane label="考评指标管理" name="indicators">
        <div class="panel-header">
          <span class="panel-subtitle">管理各考评周期下的具体考核指标项，如巡查完成率、整改及时率等</span>
          <el-button type="primary" @click="openIndDialog()"><el-icon><Plus /></el-icon>新增指标</el-button>
        </div>
        <div class="table-card">
          <el-table :data="indicatorList" v-loading="indLoading" empty-text="暂无指标，请点击「新增指标」添加" stripe>
            <el-table-column prop="indicatorName" label="指标名称" min-width="200" />
            <el-table-column prop="assessType" label="考评周期" width="100">
              <template #default="{ row }">
                <el-tag :type="row.assessType==='月度'?'':row.assessType==='季度'?'warning':'success'" size="small">{{ row.assessType || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isValid" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.isValid===1?'success':'danger'" size="small">{{ row.isValid===1?'有效':'无效' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="openIndDialog(row)">编辑</el-button>
                <el-divider direction="vertical" />
                <el-button link type="danger" size="small" @click="confirmDelIndicator(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="indPageNum" v-model:page-size="indPageSize" :page-sizes="[10,20,50]" :total="indTotal" layout="total, sizes, prev, pager, next" @current-change="fetchIndicators" @size-change="fetchIndicators" />
        </div>
      </el-tab-pane>

      <!-- 考评结果 -->
      <el-tab-pane label="考评结果查询" name="results">
        <div class="panel-header">
          <div class="result-toolbar">
            <span class="panel-subtitle">选择周期后执行考评，系统将自动计算各网格得分</span>
            <div style="display:flex;align-items:center;gap:12px">
              <span style="font-size:14px;color:#606266;white-space:nowrap">考评周期：</span>
              <el-select v-model="template" style="width:140px">
                <el-option label="月度考评" value="月度" />
                <el-option label="季度考评" value="季度" />
                <el-option label="年度考评" value="年度" />
              </el-select>
              <el-button type="primary" @click="handleRun">执行考评</el-button>
            </div>
          </div>
        </div>
        <div class="table-card">
          <el-table :data="results" v-loading="resultLoading" empty-text="暂无结果，请先选择周期并点击「执行考评」" stripe>
            <el-table-column prop="gridName" label="网格名称" min-width="150" />
            <el-table-column prop="score" label="考评分数" width="100">
              <template #default="{ row }">
                <span :style="{ color: scoreColor(row.score), fontWeight: 'bold' }">{{ row.score }}分</span>
              </template>
            </el-table-column>
            <el-table-column prop="level" label="评级" width="110">
              <template #default="{ row }">
                <el-tag v-if="row.level==='A'" type="success">A - 优秀</el-tag>
                <el-tag v-else-if="row.level==='B'" type="">B - 良好</el-tag>
                <el-tag v-else-if="row.level==='C'" type="warning">C - 合格</el-tag>
                <el-tag v-else-if="row.level==='D'" type="danger">D - 待改进</el-tag>
                <span v-else>{{ row.level || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="responseRate" label="响应率" width="90">
              <template #default="{ row }">{{ row.responseRate }}%</template>
            </el-table-column>
            <el-table-column prop="disposalRate" label="处置及时率" width="100">
              <template #default="{ row }">{{ row.disposalRate }}%</template>
            </el-table-column>
            <el-table-column prop="completeRate" label="完成率" width="90">
              <template #default="{ row }">{{ row.completeRate }}%</template>
            </el-table-column>
            <el-table-column prop="assessPeriod" label="考评周期" width="100" />
          </el-table>
          <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="resultPageNum" v-model:page-size="resultPageSize" :page-sizes="[10,20,50]" :total="resultTotal" layout="total, sizes, prev, pager, next" @current-change="fetchResults" @size-change="fetchResults" />
        </div>
      </el-tab-pane>

      <!-- 规则弹窗 -->
      <el-dialog v-model="ruleDialog" title="考评规则" width="520px" destroy-on-close center>
        <el-form :model="ruleForm" label-width="90px">
          <el-form-item label="规则名称" required>
            <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称，如：月度网格巡查考核规则" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="版本号">
            <el-input v-model="ruleForm.version" placeholder="如：v1.0、v2.0" maxlength="10" />
          </el-form-item>
          <el-form-item label="考评周期" required>
            <el-select v-model="ruleForm.category" placeholder="请选择考评周期" style="width:100%">
              <el-option label="月度" value="月度">月度</el-option>
              <el-option label="季度" value="季度">季度</el-option>
              <el-option label="年度" value="年度">年度</el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="ruleDialog=false">取消</el-button>
          <el-button type="primary" @click="saveRule">保存</el-button>
        </template>
      </el-dialog>

      <!-- 指标弹窗 -->
      <el-dialog v-model="indDialog" title="考评指标" width="520px" destroy-on-close center>
        <el-form :model="indForm" label-width="90px">
          <el-form-item label="指标名称" required>
            <el-input v-model="indForm.indicatorName" placeholder="请输入指标名称，如：巡查任务完成率" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="考评周期" required>
            <el-select v-model="indForm.assessType" placeholder="请选择考评周期" style="width:100%">
              <el-option label="月度" value="月度">月度</el-option>
              <el-option label="季度" value="季度">季度</el-option>
              <el-option label="年度" value="年度">年度</el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="indDialog=false">取消</el-button>
          <el-button type="primary" @click="saveIndicator">保存</el-button>
        </template>
      </el-dialog>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAssessRuleList, saveAssessRule, getAssessIndicatorList, saveAssessIndicator, getAssessResult, runAssess } from '@/api'

const activeTab = ref('rules')
const template = ref('月度')
const results = ref([])

// ===== 考评规则（独立表 assess_rule）=====
const ruleLoading = ref(false), ruleList = ref([]), ruleDialog = ref(false)
const ruleForm = reactive({ id: null, ruleName: '', version: '', category: '' })
const rulePageNum = ref(1), rulePageSize = ref(10), ruleTotal = ref(0)

const fetchRules = async () => {
  ruleLoading.value = true
  try {
    const r = await getAssessRuleList({ pageNum: rulePageNum.value, pageSize: rulePageSize.value })
    ruleList.value = r.data?.records || []
    ruleTotal.value = Number(r.data?.total) || 0
  } catch (e) { console.error(e) }
  finally { ruleLoading.value = false }
}

const openRuleDialog = (row) => {
  if (row) {
    Object.assign(ruleForm, { id: row.id, ruleName: row.ruleName || '', version: row.version || '', category: row.category || '' })
  } else {
    Object.assign(ruleForm, { id: null, ruleName: '', version: '', category: '' })
  }
  ruleDialog.value = true
}

const saveRule = async () => {
  if (!ruleForm.ruleName || !ruleForm.category) return ElMessage.warning('请填写规则名称和考评周期')
  try {
    await saveAssessRule(ruleForm)
    ElMessage.success('保存成功')
    ruleDialog.value = false
    fetchRules()
  } catch (e) { ElMessage.error('保存失败') }
}

const confirmDelRule = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">规则名称</span><span class="del-value"><strong>${row.ruleName || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">版本号</span><span class="del-value">${row.version || '-'}</span></div>
      <div class="del-row"><span class="del-label">考评周期</span><span class="del-value">${row.category || '-'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该规则将永久移除！</div>
      <div class="del-warning-note">注：已关联的考评指标和结果将保留不受影响。</div>
    </div>`,
    '删除考评规则',
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
  )
    .then(async () => {
      try {
        await saveAssessRule({ id: row.id, deleted: 1 })
        ElMessage.success('已删除')
        fetchRules()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// ===== 考评指标（表 assess_indicator）=====
const indLoading = ref(false), indicatorList = ref([]), indDialog = ref(false)
const indForm = reactive({ id: null, indicatorName: '', assessType: '' })
const indPageNum = ref(1), indPageSize = ref(10), indTotal = ref(0)

const fetchIndicators = async () => {
  indLoading.value = true
  try {
    const r = await getAssessIndicatorList({ pageNum: indPageNum.value, pageSize: indPageSize.value })
    indicatorList.value = r.data?.records || []
    indTotal.value = Number(r.data?.total) || 0
  } catch (e) { console.error(e) }
  finally { indLoading.value = false }
}

const openIndDialog = (row) => {
  if (row) {
    Object.assign(indForm, { id: row.id, indicatorName: row.indicatorName || '', assessType: row.assessType || '' })
  } else {
    Object.assign(indForm, { id: null, indicatorName: '', assessType: '' })
  }
  indDialog.value = true
}

const saveIndicator = async () => {
  if (!indForm.indicatorName || !indForm.assessType) return ElMessage.warning('请填写完整信息')
  try {
    await saveAssessIndicator(indForm)
    ElMessage.success('保存成功')
    indDialog.value = false
    fetchIndicators()
  } catch (e) { ElMessage.error('保存失败') }
}

const confirmDelIndicator = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">指标名称</span><span class="del-value"><strong>${row.indicatorName || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">考评周期</span><span class="del-value">${row.assessType || '-'}</span></div>
      <div class="del-row"><span class="del-label">状态</span><span class="del-value">${row.isValid === 1 ? '有效' : '无效'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该指标将永久移除！</div>
      <div class="del-warning-note">注：已关联的考评结果将保留不受影响。</div>
    </div>`,
    '删除考评指标',
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
  )
    .then(async () => {
      try {
        await saveAssessIndicator({ id: row.id, deleted: 1 })
        ElMessage.success('已删除')
        fetchIndicators()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// ===== 考评结果 =====
const resultLoading = ref(false), resultPageNum = ref(1), resultPageSize = ref(10), resultTotal = ref(0)

const fetchResults = async () => {
  resultLoading.value = true
  try {
    const r = await getAssessResult({ type: template.value, pageNum: resultPageNum.value, pageSize: resultPageSize.value })
    results.value = r.data?.records || []
    resultTotal.value = Number(r.data?.total) || 0
  } catch (e) { console.error(e) }
  finally { resultLoading.value = false }
}

const handleRun = async () => {
  try {
    await runAssess({ type: template.value })
    ElMessage.success('考评执行完成')
    fetchResults()
  } catch (e) { ElMessage.error('执行失败') }
}

const scoreColor = (score) => {
  if (!score) return '#909399'
  const s = Number(score)
  if (s >= 90) return '#67C23A'
  if (s >= 80) return '#409EFF'
  if (s >= 70) return '#E6A23C'
  return '#F56C6C'
}

onMounted(() => { fetchRules(); fetchIndicators(); fetchResults() })
</script>

<style scoped>
.assess-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.panel-subtitle {
  color: #909399;
  font-size: 13px;
  flex: 1;
  min-width: 0;
}

.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 4px 0 0 0;
}

.result-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  flex-wrap: wrap;
  gap: 12px;
}
</style>

<style>
/* 删除弹窗样式已统一在 global.scss 中定义（assess-del-dialog / del-detail 等） */
</style>
