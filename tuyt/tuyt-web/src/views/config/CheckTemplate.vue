<template>
  <div>
    <div class="page-title">检查模板配置</div>
    <div style="margin-bottom:12px"><el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增模板</el-button></div>
    <el-table :data="list" v-loading="loading" empty-text="暂无模板"><el-table-column prop="name" label="选项名称" /><el-table-column prop="inputType" label="录入类型" /><el-table-column prop="isNormal" label="是否正常" /><el-table-column label="操作"><template #default="{ row }"><el-button link @click="openDialog(row)">编辑</el-button><el-button link type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column></el-table>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑模板':'新增模板'" width="500px">
      <el-form label-width="100px">
        <el-form-item label="选项名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="录入类型"><el-select v-model="form.inputType" style="width:100%"><el-option label="单选" value="RADIO" /><el-option label="多选" value="CHECKBOX" /><el-option label="文本" value="TEXT" /></el-select></el-form-item>
        <el-form-item label="是否正常"><el-switch v-model="form.isNormal" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckTemplateList, saveCheckTemplate, deleteCheckTemplate } from '@/api'

const loading = ref(false), list = ref([]), dialogVisible = ref(false), isEdit = ref(false)
const form = reactive({ id: null, name: '', inputType: '', isNormal: false })

const fetch = async () => { loading.value = true; try { const r = await getCheckTemplateList(); list.value = r.data?.list || r.data || [] } catch {} finally { loading.value = false } }
const openDialog = (row) => { isEdit.value = !!row; Object.assign(form, row || { id: null, name: '', inputType: '', isNormal: false }); dialogVisible.value = true }
const handleSave = async () => { try { await saveCheckTemplate(form); ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; fetch() } catch { ElMessage.error('保存失败') } }
const handleDelete = (id) => { ElMessageBox.confirm('删除后该检查模板将无法恢复，是否继续？', '删除模板', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }).then(async () => { try { await deleteCheckTemplate(id); ElMessage.success('已删除'); fetch() } catch { ElMessage.error('删除失败') } }).catch(() => {}) }
onMounted(fetch)
</script>
