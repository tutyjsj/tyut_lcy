<template>
  <div>
    <div class="page-title">巡查计划</div>
    <div class="search-bar">
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新建计划</el-button>
    </div>
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无计划">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="templateName" label="检查模板" width="140" />
        <el-table-column prop="cycle" label="检查周期" width="100" />
        <el-table-column prop="startTime" label="启用时间" width="160" />
        <el-table-column prop="lastTime" label="上次执行" width="160" />
        <el-table-column prop="nextTime" label="下次执行" width="160" />
        <el-table-column prop="type" label="配置类型" width="100" />
        <el-table-column prop="status" label="状态" width="80" />
        <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" @click="openDialog(row)">编辑</el-button><el-button link type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑巡查计划':'新建巡查计划'" width="700px">
      <el-form label-width="100px">
        <el-form-item label="配置类型"><el-select v-model="form.type" style="width:100%"><el-option label="日常巡查" value="DAILY" /><el-option label="停产巡查" value="SHUTDOWN" /></el-select></el-form-item>
        <el-form-item label="计划标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="检查周期"><el-select v-model="form.cycle" style="width:100%"><el-option label="每月" value="MONTHLY" /><el-option label="每季度" value="QUARTERLY" /><el-option label="每半年" value="SEMIANNUAL" /></el-select></el-form-item>
        <el-form-item label="启用时间"><el-date-picker v-model="form.startTime" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="检查模板"><el-input v-model="form.templateName" /></el-form-item>
        <el-form-item label="监管企业"><el-button type="primary" link>添加企业</el-button></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPatrolPlanList, createPatrolPlan, deletePatrolPlan } from '@/api'

const loading = ref(false), list = ref([]), dialogVisible = ref(false), isEdit = ref(false)
const form = reactive({ id: null, title: '', type: '', cycle: '', startTime: '', templateName: '' })

const fetch = async () => { loading.value = true; try { const r = await getPatrolPlanList(); list.value = r.data?.list || r.data || [] } catch {} finally { loading.value = false } }
const openDialog = (row) => { isEdit.value = !!row; Object.assign(form, row || { id: null, title: '', type: '', cycle: '', startTime: '', templateName: '' }); dialogVisible.value = true }
const handleSave = async () => { try { await createPatrolPlan(form); ElMessage.success(isEdit.value ? '修改成功' : '新建成功'); dialogVisible.value = false; fetch() } catch { ElMessage.error('保存失败') } }
const handleDelete = (id) => { ElMessageBox.confirm('删除后该巡查计划将无法恢复，是否继续？', '删除巡查计划', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }).then(async () => { try { await deletePatrolPlan(id); ElMessage.success('已删除'); fetch() } catch { ElMessage.error('删除失败') } }).catch(() => {}) }
onMounted(fetch)
</script>
