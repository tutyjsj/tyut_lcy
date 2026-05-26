<template>
  <div>
    <div class="page-title">考评管理</div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="考评规则设置" name="rules">
        <el-button type="primary" style="margin-bottom:12px" @click="openRuleDialog()"><el-icon><Plus /></el-icon>新增规则</el-button>
        <el-table :data="ruleList" empty-text="暂无规则"><el-table-column prop="indicatorName" label="规则名称" /><el-table-column prop="assessType" label="考评类型" /><el-table-column prop="isValid" label="是否有效"><template #default="{ row }"><el-tag :type="row.isValid===1?'success':'danger'">{{ row.isValid===1?'有效':'无效' }}</el-tag></template></el-table-column><el-table-column label="操作"><template #default="{ row }"><el-button link @click="openRuleDialog(row)">编辑</el-button><el-button link type="danger" @click="delRule(row.id)">删除</el-button></template></el-table-column></el-table>
      </el-tab-pane>
      <el-tab-pane label="考评指标管理" name="indicators">
        <el-button type="primary" style="margin-bottom:12px" @click="openIndDialog()"><el-icon><Plus /></el-icon>新增指标</el-button>
        <el-table :data="indicatorList" empty-text="暂无指标"><el-table-column prop="indicatorName" label="指标名称" /><el-table-column prop="assessType" label="考评类型" /><el-table-column prop="isValid" label="是否有效"><template #default="{ row }"><el-tag :type="row.isValid===1?'success':'danger'">{{ row.isValid===1?'有效':'无效' }}</el-tag></template></el-table-column><el-table-column label="操作"><template #default="{ row }"><el-button link @click="openIndDialog(row)">编辑</el-button><el-button link type="danger" @click="delIndicator(row.id)">删除</el-button></template></el-table-column></el-table>
      </el-tab-pane>
      <el-tab-pane label="考评结果" name="results">
        <el-select v-model="template" style="width:180px;margin-right:12px" @change="fetchResults"><el-option label="月度考评" value="MONTH" /><el-option label="半年考评" value="HALF_YEAR" /><el-option label="年度考评" value="YEAR" /></el-select>
        <el-button type="primary" @click="handleRun">执行考评</el-button>
        <el-table :data="results" style="margin-top:12px" empty-text="暂无结果"><el-table-column prop="gridName" label="网格名称" /><el-table-column prop="score" label="考评分数" /><el-table-column prop="level" label="评级" /></el-table>
      </el-tab-pane>
      <el-dialog v-model="ruleDialog" title="考评规则" width="500px">
        <el-form label-width="80px"><el-form-item label="规则名称"><el-input v-model="ruleForm.indicatorName" /></el-form-item><el-form-item label="考评类型"><el-input v-model="ruleForm.assessType" /></el-form-item></el-form>
        <template #footer><el-button @click="ruleDialog=false">取消</el-button><el-button type="primary" @click="saveRule">确定</el-button></template>
      </el-dialog>
      <el-dialog v-model="indDialog" title="考评指标" width="500px">
        <el-form label-width="80px"><el-form-item label="指标名称"><el-input v-model="indForm.indicatorName" /></el-form-item><el-form-item label="考评类型"><el-input v-model="indForm.assessType" /></el-form-item></el-form>
        <template #footer><el-button @click="indDialog=false">取消</el-button><el-button type="primary" @click="saveIndicator">确定</el-button></template>
      </el-dialog>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAssessRuleList, saveAssessRule, getAssessIndicatorList, saveAssessIndicator, getAssessResult, runAssess } from '@/api'

const activeTab = ref('rules')
const template = ref('MONTH')
const results = ref([])

// 规则
const ruleList = ref([]), ruleDialog = ref(false), ruleForm = reactive({ id: null, name: '', version: '', category: '' })
const fetchRules = async () => { try { const r = await getAssessRuleList(); ruleList.value = r.data?.records || [] } catch {} }
const openRuleDialog = (row) => { Object.assign(ruleForm, row || { id: null, indicatorName: '', assessType: '' }); ruleDialog.value = true }
const saveRule = async () => { try { await saveAssessRule(ruleForm); ElMessage.success('保存成功'); ruleDialog.value = false; fetchRules() } catch { ElMessage.error('保存失败') } }
const delRule = (id) => ElMessageBox.confirm('删除后该考评规则将无法恢复，是否继续？', '删除规则', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }).then(async () => { try { await saveAssessRule({ id, deleted: true }); ElMessage.success('已删除'); fetchRules() } catch {} }).catch(() => {})

// 指标
const indicatorList = ref([]), indDialog = ref(false), indForm = reactive({ id: null, indicatorName: '', assessType: '' })
const fetchIndicators = async () => { try { const r = await getAssessIndicatorList(); indicatorList.value = r.data?.records || [] } catch {} }
const openIndDialog = (row) => { Object.assign(indForm, row || { id: null, indicatorName: '', assessType: '' }); indDialog.value = true }
const saveIndicator = async () => { try { await saveAssessIndicator(indForm); ElMessage.success('保存成功'); indDialog.value = false; fetchIndicators() } catch { ElMessage.error('保存失败') } }
const delIndicator = (id) => ElMessageBox.confirm('删除后该考评指标将无法恢复，是否继续？', '删除指标', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }).then(async () => { try { await saveAssessIndicator({ id, deleted: true }); ElMessage.success('已删除'); fetchIndicators() } catch {} }).catch(() => {})

// 考评结果
const fetchResults = async () => { try { const r = await getAssessResult({ type: template.value }); results.value = r.data?.records || [] } catch {} }
const handleRun = async () => { try { await runAssess({ type: template.value }); ElMessage.success('考评执行完成'); fetchResults() } catch { ElMessage.error('执行失败') } }

onMounted(() => { fetchRules(); fetchIndicators() })
</script>
