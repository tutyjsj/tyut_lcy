<template>
  <div>
    <div class="page-title">检查项管理</div>
    <div class="search-bar">
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新建检查项</el-button>
    </div>
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="name" label="检查项目名称" />
        <el-table-column prop="type" label="项目类型" />
        <el-table-column prop="monitorType" label="监控点类型" />
        <el-table-column prop="inputType" label="录入方式" />
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作"><template #default="{ row }"><el-button link type="primary" @click="openDialog(row)">编辑</el-button><el-button link type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑检查项':'新建检查项'" width="500px">
      <el-form label-width="100px">
        <el-form-item label="项目名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="项目类型"><el-select v-model="form.type" style="width:100%"><el-option label="废水" value="WASTE_WATER" /><el-option label="废气" value="WASTE_GAS" /><el-option label="噪声" value="NOISE" /></el-select></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option label="启用" value="ENABLED" /><el-option label="停用" value="DISABLED" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckItemList, saveCheckItem, deleteCheckItem } from '@/api'

const loading = ref(false), list = ref([]), dialogVisible = ref(false), isEdit = ref(false)
const form = reactive({ id: null, name: '', type: '', monitorType: '', inputType: '', status: 'ENABLED' })

const fetch = async () => { loading.value = true; try { const r = await getCheckItemList(); list.value = r.data?.list || r.data || [] } catch {} finally { loading.value = false } }
const openDialog = (row) => { isEdit.value = !!row; Object.assign(form, row || { id: null, name: '', type: '', monitorType: '', inputType: '', status: 'ENABLED' }); dialogVisible.value = true }
const handleSave = async () => { try { await saveCheckItem(form); ElMessage.success(isEdit.value ? '修改成功' : '新建成功'); dialogVisible.value = false; fetch() } catch { ElMessage.error('保存失败') } }
const handleDelete = (id) => { ElMessageBox.confirm('删除后该检查项将无法恢复，是否继续？', '删除检查项', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }).then(async () => { try { await deleteCheckItem(id); ElMessage.success('已删除'); fetch() } catch { ElMessage.error('删除失败') } }).catch(() => {}) }
onMounted(fetch)
</script>
