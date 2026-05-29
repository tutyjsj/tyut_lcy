<template>
  <div>
    <div class="page-title">考评模板配置</div>
    <p class="page-desc">灵活设置月度、半年、年度考评模板及考评项权重，系统将根据模板自动计算考评分数</p>

    <!-- 左右布局 -->
    <div class="config-layout">
      <!-- 左侧：模板卡片列表 -->
      <div class="template-sidebar">
        <div class="sidebar-header">
          <h3 class="sidebar-title">考评模板 <span class="sidebar-badge">{{ templateList.length }}</span></h3>
          <el-button type="primary" size="small" :icon="Plus" @click="openTemplateDialog()">新增模板</el-button>
        </div>

        <div class="template-list" v-loading="templateLoading">
          <template v-if="templateList.length > 0">
            <div
              v-for="tpl in templateList" :key="tpl.id"
              class="template-card"
              :class="{ active: currentTemplate?.id === tpl.id }"
              @click="selectTemplate(tpl)"
            >
              <div class="tpl-top">
                <div class="tpl-name">{{ tpl.templateName }}</div>
                <div class="tpl-actions">
                  <el-button :icon="Edit" link size="small" @click.stop="openTemplateDialog(tpl)" title="编辑" />
                  <el-button :icon="Delete" link size="small" type="danger" @click.stop="confirmDelTemplate(tpl)" title="删除" />
                </div>
              </div>
              <div class="tpl-meta">
                <el-tag
                  :type="tpl.templateType === '月度' ? 'primary' : tpl.templateType === '半年' ? 'warning' : 'success'"
                  size="small" effect="dark"
                >{{ tpl.templateType }}</el-tag>
                <el-tag :type="tpl.status === 1 ? 'success' : 'info'" size="small">
                  {{ tpl.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </div>
              <div v-if="tpl.templateDesc" class="tpl-desc">{{ tpl.templateDesc }}</div>
            </div>
          </template>
          <el-empty v-else description="暂无模板，请点击「新增模板」创建" :image-size="60" />
        </div>
        <el-pagination v-if="templateTotal > 0" style="margin-top:10px;display:flex;justify-content:center"
          v-model:current-page="templatePageNum" v-model:page-size="templatePageSize"
          :page-sizes="[10,20,50]" :total="templateTotal"
          layout="total, prev, pager, next" small @current-change="fetchTemplates" @size-change="fetchTemplates" />
      </div>

      <!-- 右侧：考评项详情 -->
      <div class="items-panel">
        <template v-if="currentTemplate">
          <!-- 头部信息 -->
          <div class="items-panel-header">
            <div>
              <h3 class="items-panel-title">{{ currentTemplate.templateName }}</h3>
              <span class="items-panel-subtitle">
                <el-tag :type="currentTemplate.templateType === '月度' ? 'primary' : currentTemplate.templateType === '半年' ? 'warning' : 'success'" size="small" effect="dark">
                  {{ currentTemplate.templateType }}考核
                </el-tag>
                &nbsp;{{ currentTemplate.templateDesc || '' }}
              </span>
            </div>
            <el-button type="primary" size="small" :icon="Plus" @click="openItemDialog()">新增考评项</el-button>
          </div>

          <!-- 考评项表格 -->
          <div class="table-card" v-loading="itemLoading">
            <el-table :data="itemList" empty-text="暂无考评项，请点击「新增考评项」添加" stripe size="small">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="itemName" label="考评项名称" min-width="160" />
              <el-table-column prop="itemDesc" label="描述" min-width="200" show-overflow-tooltip />
              <el-table-column prop="weight" label="权重(%)" width="160">
                <template #default="{ row }">
                  <div class="weight-cell">
                    <el-progress
                      :percentage="Number(row.weight)"
                      :color="weightColor(row.weight)"
                      :stroke-width="16"
                      :show-text="false"
                      style="flex:1;max-width:100px"
                    />
                    <span class="weight-text" :style="{ color: weightColor(row.weight) }">{{ row.weight }}%</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="openItemDialog(row)">编辑</el-button>
                  <el-divider direction="vertical" />
                  <el-button link type="danger" size="small" @click="confirmDelItem(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 权重合计 -->
            <div class="weight-summary">
              <span class="weight-label">权重合计</span>
              <span class="weight-total" :class="{ valid: totalWeight === 100, invalid: totalWeight !== 100 }">
                {{ totalWeight }}%
              </span>
              <template v-if="totalWeight !== 100">
                <span class="weight-hint">（权重总和必须等于 100%）</span>
              </template>
              <template v-else>
                <el-icon class="weight-check"><CircleCheckFilled /></el-icon>
              </template>
            </div>
          </div>
        </template>

        <!-- 未选择模板 -->
        <div v-else class="items-empty">
          <el-empty description="请选择左侧模板查看或编辑考评项" :image-size="100">
            <template #image>
              <el-icon :size="80" color="#c0c4cc"><Select /></el-icon>
            </template>
          </el-empty>
        </div>
      </div>
    </div>

    <!-- 模板弹窗 -->
    <el-dialog v-model="templateDialog" :title="templateForm.id ? '编辑模板' : '新建模板'" width="520px" destroy-on-close center>
      <el-form :model="templateForm" label-width="90px">
        <el-form-item label="模板名称" required>
          <el-input v-model="templateForm.templateName" placeholder="如：月度网格巡查考核模板" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="考评周期" required>
          <el-select v-model="templateForm.templateType" placeholder="请选择考评周期" style="width:100%">
            <el-option label="月度考核" value="月度" />
            <el-option label="半年考核" value="半年" />
            <el-option label="年度考核" value="年度" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="templateForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="templateForm.templateDesc" type="textarea" :rows="3" placeholder="简要说明模板用途和考核重点" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 考评项弹窗 -->
    <el-dialog v-model="itemDialog" :title="editingItemId ? '编辑考评项' : '新增考评项'" width="480px" destroy-on-close center>
      <el-form :model="itemForm" label-width="90px">
        <el-form-item label="考评项名称" required>
          <el-input v-model="itemForm.itemName" placeholder="如：巡查任务完成率" maxlength="50" />
        </el-form-item>
        <el-form-item label="权重(%)" required>
          <el-input-number v-model="itemForm.weight" :min="1" :max="100" :step="5" style="width:100%" />
          <div class="weight-available">
            当前其他项权重合计 <strong>{{ currentTotalWeight }}%</strong>，最多可用 <strong style="color:#409EFF">{{ 100 - currentTotalWeight }}%</strong>
          </div>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="itemForm.itemDesc" type="textarea" :rows="2" placeholder="简要说明该考评项的考核内容和标准" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialog = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, CircleCheckFilled, Select } from '@element-plus/icons-vue'
import { getAssessTemplateList, saveAssessTemplate, deleteAssessTemplate, getAssessTemplateItems, saveAssessTemplateItems } from '@/api'

const templateLoading = ref(false), templateList = ref([])
const templatePageNum = ref(1), templatePageSize = ref(10), templateTotal = ref(0)
const itemLoading = ref(false), itemList = ref([])
const currentTemplate = ref(null)

// ================= 模板 =================
const templateDialog = ref(false)
const templateForm = reactive({ id: null, templateName: '', templateType: '', templateDesc: '', status: 1 })

const fetchTemplates = async () => {
  templateLoading.value = true
  try {
    const r = await getAssessTemplateList({ pageNum: templatePageNum.value, pageSize: templatePageSize.value })
    templateList.value = r.data?.records || []
    templateTotal.value = Number(r.data?.total) || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载模板列表失败')
  } finally { templateLoading.value = false }
}

const openTemplateDialog = (row) => {
  if (row) {
    Object.assign(templateForm, { id: row.id, templateName: row.templateName || '', templateType: row.templateType || '', templateDesc: row.templateDesc || '', status: row.status ?? 1 })
  } else {
    Object.assign(templateForm, { id: null, templateName: '', templateType: '', templateDesc: '', status: 1 })
  }
  templateDialog.value = true
}

const saveTemplate = async () => {
  if (!templateForm.templateName || !templateForm.templateType) return ElMessage.warning('请填写模板名称和考评周期')
  try {
    await saveAssessTemplate(templateForm)
    ElMessage.success('保存成功')
    templateDialog.value = false
    await fetchTemplates()
  } catch (e) { ElMessage.error('保存失败') }
}

const confirmDelTemplate = (tpl) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">模板名称</span><span class="del-value"><strong>${tpl.templateName || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">考评周期</span><span class="del-value">${tpl.templateType || '-'}</span></div>
      <div class="del-row"><span class="del-label">状态</span><span class="del-value">${tpl.status === 1 ? '启用' : '停用'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该模板将永久移除！</div>
      <div class="del-warning-note">注：该模板下的所有考评项也将一并删除，已产生的考评结果不受影响。</div>
    </div>`,
    '删除考评模板',
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
        await deleteAssessTemplate(tpl.id)
        ElMessage.success('已删除')
        if (currentTemplate.value?.id === tpl.id) {
          currentTemplate.value = null
          itemList.value = []
        }
        fetchTemplates()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// ================= 模板项 =================
const itemDialog = ref(false)
const itemForm = reactive({ id: null, itemName: '', itemDesc: '', weight: 20 })
const editingItemId = ref(null)

const selectTemplate = async (row) => {
  currentTemplate.value = row
  await fetchItems(row.id)
}

const fetchItems = async (templateId) => {
  itemLoading.value = true
  try {
    const r = await getAssessTemplateItems(templateId)
    itemList.value = r.data || []
  } catch (e) { console.error(e) } finally { itemLoading.value = false }
}

const totalWeight = computed(() => itemList.value.reduce((sum, it) => sum + Number(it.weight || 0), 0))

const currentTotalWeight = computed(() => {
  const others = itemList.value.filter(it => it.id !== editingItemId.value)
  return others.reduce((sum, it) => sum + Number(it.weight || 0), 0)
})

const openItemDialog = (row) => {
  if (row) {
    editingItemId.value = row.id
    Object.assign(itemForm, { id: row.id, itemName: row.itemName || '', itemDesc: row.itemDesc || '', weight: row.weight || 20 })
  } else {
    editingItemId.value = null
    Object.assign(itemForm, { id: null, itemName: '', itemDesc: '', weight: 20 })
  }
  itemDialog.value = true
}

const saveItem = async () => {
  if (!itemForm.itemName || !itemForm.weight) return ElMessage.warning('请填写考评项名称和权重')
  let newList
  if (editingItemId.value) {
    newList = itemList.value.map(it => it.id === editingItemId.value ? { ...it, itemName: itemForm.itemName, itemDesc: itemForm.itemDesc, weight: itemForm.weight } : it)
  } else {
    newList = [...itemList.value, { itemName: itemForm.itemName, itemDesc: itemForm.itemDesc, weight: itemForm.weight }]
  }
  const sum = newList.reduce((s, it) => s + Number(it.weight || 0), 0)
  if (sum !== 100) return ElMessage.warning(`考评项权重总和必须为100%，当前为 ${sum}%`)
  try {
    await saveAssessTemplateItems(currentTemplate.value.id, newList)
    ElMessage.success('保存成功')
    itemDialog.value = false
    await fetchItems(currentTemplate.value.id)
  } catch (e) { ElMessage.error('保存失败') }
}

const confirmDelItem = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">考评项名称</span><span class="del-value"><strong>${row.itemName || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">权重</span><span class="del-value">${row.weight || 0}%</span></div>
      <div class="del-row"><span class="del-label">描述</span><span class="del-value">${row.itemDesc || '-'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该考评项将永久移除！</div>
      <div class="del-warning-note">注：删除后需调整其他考评项权重，使总和等于100%。</div>
    </div>`,
    '删除考评项',
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
      const newList = itemList.value.filter(it => it.id !== row.id)
      try {
        await saveAssessTemplateItems(currentTemplate.value.id, newList)
        ElMessage.success('已删除')
        await fetchItems(currentTemplate.value.id)
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const weightColor = (weight) => {
  const w = Number(weight)
  if (w >= 35) return '#409EFF'
  if (w >= 25) return '#67C23A'
  if (w >= 15) return '#E6A23C'
  return '#909399'
}

onMounted(fetchTemplates)
</script>

<style scoped>
/* ---- 页面头部 ---- */
.page-desc {
  color: #909399;
  font-size: 13px;
  margin: -8px 0 20px 0;
  line-height: 1.6;
}

/* ---- 左右分栏 ---- */
.config-layout {
  display: flex;
  gap: 24px;
  min-height: 560px;
  align-items: flex-start;
}

/* ---- 左侧模板列表 ---- */
.template-sidebar {
  width: 340px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 18px 12px;
  border-bottom: 1px solid #ebeef5;
}

.sidebar-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.sidebar-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: #ecf5ff;
  color: #409EFF;
  font-size: 12px;
  font-weight: 600;
}

.template-list {
  padding: 10px 14px;
  max-height: 520px;
  overflow-y: auto;
}

.template-card {
  padding: 14px 16px;
  margin-bottom: 8px;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.template-card:hover {
  background: #f0f5ff;
  border-color: #b3d8ff;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.template-card.active {
  background: #ecf5ff;
  border-color: #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.18);
}

.tpl-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.tpl-name {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  word-break: break-all;
}

.tpl-actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.template-card:hover .tpl-actions,
.template-card.active .tpl-actions {
  opacity: 1;
}

.tpl-meta {
  display: flex;
  gap: 6px;
  margin-bottom: 6px;
}

.tpl-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* ---- 右侧考评项详情 ---- */
.items-panel {
  flex: 1;
  min-width: 0;
}

.items-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.items-panel-title {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.items-panel-subtitle {
  font-size: 13px;
  color: #909399;
}

.table-card {
  background: #fff;
  border-radius: 10px;
  padding: 8px 0 0 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

/* ---- 权重显示 ---- */
.weight-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.weight-text {
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.weight-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid #ebeef5;
  background: #fafbfc;
}

.weight-label {
  font-size: 13px;
  color: #909399;
}

.weight-total {
  font-size: 18px;
  font-weight: 700;
}

.weight-total.valid {
  color: #67C23A;
}

.weight-total.invalid {
  color: #F56C6C;
}

.weight-hint {
  font-size: 12px;
  color: #F56C6C;
}

.weight-check {
  font-size: 18px;
  color: #67C23A;
}

.weight-available {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  line-height: 1.6;
}

/* ---- 空状态 ---- */
.items-empty {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>

<style>
/* 删除弹窗样式已统一在 global.scss 中定义（assess-del-dialog / del-detail 等） */
</style>
