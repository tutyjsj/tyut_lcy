<template>
  <div>
    <div class="page-title">任务台账</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="任务编号"><el-input v-model="query.taskNo" clearable /></el-form-item>
        <el-form-item label="任务标题"><el-input v-model="query.title" clearable /></el-form-item>
        <el-form-item label="任务状态"><el-select v-model="query.status" clearable><el-option label="已拟定" value="DRAFT" /><el-option label="已派发" value="DISPATCHED" /><el-option label="已完成" value="COMPLETED" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button type="success" @click="exportData">导出</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="taskNo" label="任务单号" width="160" />
        <el-table-column prop="title" label="任务标题" min-width="200" />
        <el-table-column prop="taskType" label="任务类型" width="100" />
        <el-table-column prop="urgency" label="紧急程度" width="80" />
        <el-table-column prop="dispatchTime" label="派发时间" width="160" />
        <el-table-column prop="processor" label="处理人" width="100" />
        <el-table-column prop="status" label="状态" width="80" />
        <el-table-column label="操作" width="80"><template #default><el-button type="primary" link>查看</el-button></template></el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" @current-change="search" @size-change="search" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getTaskList, exportTaskLedger } from '@/api'

const loading = ref(false), list = ref([]), total = ref(0)
const query = reactive({ taskNo: '', title: '', status: '', pageNum: 1, pageSize: 10 })

const search = async () => {
  loading.value = true
  try {
    const res = await getTaskList(query)
    list.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { /* 后端未就绪 */ }
  finally { loading.value = false }
}

const exportData = async () => {
  try {
    const res = await exportTaskLedger(query)
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url; a.download = '任务台账.xlsx'; a.click()
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}
</script>
