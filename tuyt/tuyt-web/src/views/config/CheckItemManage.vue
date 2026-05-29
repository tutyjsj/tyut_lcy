<template>
  <div>
    <div class="page-title">检查项管理</div>
    <div class="search-bar">
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新建检查项</el-button>
    </div>
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="itemName" label="检查项目名称" />
        <el-table-column prop="itemType" label="项目类型" />
        <el-table-column prop="monitorType" label="监控点类型" />
        <el-table-column prop="inputType" label="录入方式" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
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
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑检查项':'新建检查项'" width="500px">
      <el-form label-width="100px">
        <el-form-item label="项目名称"><el-input v-model="form.itemName" /></el-form-item>
        <el-form-item label="项目类型">
          <el-select v-model="form.itemType" style="width:100%">
            <el-option label="排污检查" value="排污检查" />
            <el-option label="台账检查" value="台账检查" />
            <el-option label="设备检查" value="设备检查" />
          </el-select>
        </el-form-item>
        <el-form-item label="监控点类型">
          <el-select v-model="form.monitorType" style="width:100%">
            <el-option label="水污染" value="水污染" />
            <el-option label="大气污染" value="大气污染" />
            <el-option label="噪声" value="噪声" />
            <el-option label="固废" value="固废" />
            <el-option label="综合" value="综合" />
          </el-select>
        </el-form-item>
        <el-form-item label="录入方式">
          <el-select v-model="form.inputType" style="width:100%">
            <el-option label="数字输入" value="数字输入" />
            <el-option label="文本描述" value="文本描述" />
            <el-option label="单选" value="单选" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckItemList, saveCheckItem, deleteCheckItem } from '@/api'

const loading = ref(false), list = ref([]), total = ref(0), pageNum = ref(1), pageSize = ref(10), dialogVisible = ref(false), isEdit = ref(false)
const form = reactive({ id: null, itemName: '', itemType: '', monitorType: '', inputType: '', status: 1 })

const fetch = async () => { loading.value = true; try { const r = await getCheckItemList({ pageNum: pageNum.value, pageSize: pageSize.value }); list.value = r.data?.records || r.data?.list || r.data || []; total.value = Number(r.data?.total) || 0 } catch {} finally { loading.value = false } }
const openDialog = (row) => { isEdit.value = !!row; Object.assign(form, row || { id: null, itemName: '', itemType: '', monitorType: '', inputType: '', status: 1 }); dialogVisible.value = true }
const handleSave = async () => { try { await saveCheckItem(form); ElMessage.success(isEdit.value ? '修改成功' : '新建成功'); dialogVisible.value = false; fetch() } catch { ElMessage.error('保存失败') } }
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">检查项名称</span><span class="del-value"><strong>${row.itemName || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">项目类型</span><span class="del-value">${row.itemType || '-'}</span></div>
      <div class="del-row"><span class="del-label">监控点类型</span><span class="del-value">${row.monitorType || '-'}</span></div>
      <div class="del-row"><span class="del-label">录入方式</span><span class="del-value">${row.inputType || '-'}</span></div>
      <div class="del-row"><span class="del-label">状态</span><span class="del-value">${row.status === 1 ? '启用' : '停用'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该检查项将永久移除！</div>
      <div class="del-warning-note">注：已关联的巡查记录和考评数据不受影响。</div>
    </div>`,
    '删除检查项',
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
    try { await deleteCheckItem(row.id); ElMessage.success('已删除'); fetch() }
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
