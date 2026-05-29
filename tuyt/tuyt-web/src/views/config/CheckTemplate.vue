shanc<template>
  <div>
    <div class="page-title">检查模板配置</div>
    <div class="search-bar">
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增模板</el-button>
    </div>
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="templateName" label="选项名称" min-width="160" />
        <el-table-column prop="inputType" label="录入类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="row.inputType==='RADIO'?'':row.inputType==='CHECKBOX'?'success':'info'">
              {{ row.inputType==='RADIO'?'单选':row.inputType==='CHECKBOX'?'多选':'文本' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isNormal" label="是否正常" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isNormal===1?'success':'danger'">
              {{ row.isNormal===1?'正常':'异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑模板':'新增模板'" width="500px">
      <el-form label-width="100px">
        <el-form-item label="选项名称"><el-input v-model="form.templateName" placeholder="请输入选项名称" /></el-form-item>
        <el-form-item label="录入类型">
          <el-select v-model="form.inputType" style="width:100%" placeholder="请选择录入类型">
            <el-option label="单选" value="RADIO" />
            <el-option label="多选" value="CHECKBOX" />
            <el-option label="文本" value="TEXT" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否正常">
          <el-switch v-model="form.isNormal" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="异常" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckTemplateList, saveCheckTemplate, deleteCheckTemplate } from '@/api'

const loading = ref(false), list = ref([]), total = ref(0), pageNum = ref(1), pageSize = ref(10), dialogVisible = ref(false), isEdit = ref(false)
const form = reactive({ id: null, templateName: '', inputType: 'RADIO', isNormal: 1 })

const fetch = async () => {
  loading.value = true
  try {
    const r = await getCheckTemplateList({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = r.data?.records || r.data?.list || r.data || []
    total.value = r.data?.total || 0
  } catch {} finally { loading.value = false }
}
const openDialog = (row) => {
  isEdit.value = !!row
  Object.assign(form, row || { id: null, templateName: '', inputType: 'RADIO', isNormal: 1 })
  dialogVisible.value = true
}
const handleSave = async () => {
  try {
    await saveCheckTemplate({ ...form })
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    fetch()
  } catch { ElMessage.error('保存失败') }
}
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">选项名称</span><span class="del-value"><strong>${row.templateName || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">录入类型</span><span class="del-value">${row.inputType === 'RADIO' ? '单选' : row.inputType === 'CHECKBOX' ? '多选' : '文本'}</span></div>
      <div class="del-row"><span class="del-label">是否正常</span><span class="del-value">${row.isNormal === 1 ? '正常' : '异常'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该检查模板将永久移除！</div>
      <div class="del-warning-note">注：已关联的巡查计划引用此模板时将受影响。</div>
    </div>`,
    '删除模板',
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
    try { await deleteCheckTemplate(row.id); ElMessage.success('已删除'); fetch() }
    catch { ElMessage.error('删除失败') }
  }).catch(() => {})
}
onMounted(fetch)
</script>

<style scoped>
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
.table-card { background: #fff; border-radius: 8px; padding: 4px 0 0 0; }
</style>

<style>
/* 删除弹窗样式已统一在 global.scss 中定义（assess-del-dialog / del-detail 等） */
</style>
