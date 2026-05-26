<template>
  <div>
    <div class="page-title">网格划分及管理</div>
    <div class="search-bar">
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新建网格</el-button>
    </div>
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading" row-key="id" default-expand-all>
        <el-table-column prop="gridName" label="网格名称" min-width="180" />
        <el-table-column prop="gridLevel" label="网格级别" width="120"><template #default="{ row }">{{ row.gridLevel===1?'市级':row.gridLevel===2?'区县级':'乡镇/街道' }}</template></el-table-column>
        <el-table-column prop="orgId" label="责任单位ID" width="120" />
        <el-table-column prop="leader" label="分管领导" width="100" />
        <el-table-column prop="responsiblePerson" label="网格责任人" width="120" />
        <el-table-column prop="responsiblePhone" label="联系电话" width="130" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="primary" link @click="$router.push('/grid/map')">地图</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑网格':'新建网格'" width="600px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="网格名称"><el-input v-model="form.gridName" /></el-form-item>
        <el-form-item label="网格级别"><el-select v-model="form.gridLevel" style="width:100%"><el-option label="市级" :value="1" /><el-option label="区县级" :value="2" /><el-option label="乡镇/街道" :value="3" /></el-select></el-form-item>
        <el-form-item label="上级网格"><el-select v-model="form.parentId" style="width:100%" clearable><el-option v-for="g in list" :key="g.id" :label="g.gridName" :value="g.id" /></el-select></el-form-item>
        <el-form-item label="责任单位ID"><el-input v-model="form.orgId" /></el-form-item>
        <el-form-item label="分管领导"><el-input v-model="form.leader" /></el-form-item>
        <el-form-item label="网格责任人"><el-input v-model="form.responsiblePerson" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.responsiblePhone" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGridList, createGrid, updateGrid, deleteGrid } from '@/api'

const loading = ref(false), list = ref([]), dialogVisible = ref(false), isEdit = ref(false)
const form = reactive({ id: null, gridName: '', gridLevel: '', parentId: null, orgId: '', leader: '', responsiblePerson: '', responsiblePhone: '' })

const fetch = async () => { loading.value = true; const r = await getGridList().catch(()=>({data:{records:[]}})); list.value = r.data?.records||[]; loading.value = false }
const openDialog = (row) => { isEdit.value = !!row; Object.assign(form, row||{id:null,gridName:'',gridLevel:'',parentId:null,orgId:'',leader:'',responsiblePerson:'',responsiblePhone:''}); dialogVisible.value = true }
const submitForm = async () => {
  try {
    isEdit.value ? await updateGrid(form.id, form) : await createGrid(form)
    ElMessage.success(isEdit.value ? '修改成功' : '新建成功')
    dialogVisible.value = false
    fetch()
  } catch { ElMessage.error('操作失败') }
}
const handleDelete = (row) => { ElMessageBox.confirm(`删除后「${row.gridName}」及其下级网格将无法恢复，是否继续？`, '删除网格', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }).then(async () => { try { await deleteGrid(row.id); ElMessage.success('删除成功'); fetch() } catch { ElMessage.error('删除失败') } }).catch(()=>{}) }
onMounted(fetch)
</script>
