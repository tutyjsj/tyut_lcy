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
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
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

const loading = ref(false), list = ref([]), total = ref(0), dialogVisible = ref(false), isEdit = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ id: null, gridName: '', gridLevel: '', parentId: null, orgId: '', leader: '', responsiblePerson: '', responsiblePhone: '' })

const fetch = async () => {
  loading.value = true
  try {
    const r = await getGridList({ ...query })
    list.value = r.data?.records || []
    total.value = Number(r.data?.total) || 0
  } catch {
    list.value = []
    total.value = 0
  }
  finally { loading.value = false }
}
const openDialog = (row) => { isEdit.value = !!row; Object.assign(form, row||{id:null,gridName:'',gridLevel:'',parentId:null,orgId:'',leader:'',responsiblePerson:'',responsiblePhone:''}); dialogVisible.value = true }
const submitForm = async () => {
  try {
    isEdit.value ? await updateGrid(form.id, form) : await createGrid(form)
    ElMessage.success(isEdit.value ? '修改成功' : '新建成功')
    dialogVisible.value = false
    fetch()
  } catch { ElMessage.error('操作失败') }
}
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">网格名称</span><span class="del-value"><strong>${row.gridName || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">网格级别</span><span class="del-value">${row.gridLevel===1?'市级':row.gridLevel===2?'区县级':'乡镇/街道'}</span></div>
      <div class="del-row"><span class="del-label">分管领导</span><span class="del-value">${row.leader || '-'}</span></div>
      <div class="del-row"><span class="del-label">责任人</span><span class="del-value">${row.responsiblePerson || '-'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该网格将永久移除！</div>
      <div class="del-warning-note">注：该网格下的所有下级网格也将一并删除，已派发的任务记录不受影响。</div>
    </div>`,
    '删除网格',
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
    try { await deleteGrid(row.id); ElMessage.success('删除成功'); fetch() }
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
