<template>
  <div>
    <div class="page-title">问题台账</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="事发企业"><el-input v-model="query.enterpriseName" clearable /></el-form-item>
        <el-form-item label="时间范围"><el-date-picker v-model="query.dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" /></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button type="success" @click="exportData">导出Excel</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="enterpriseName" label="事发企业" width="160" />
        <el-table-column prop="alarmTime" label="报警时间" width="160" />
        <el-table-column prop="description" label="问题描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column prop="level" label="等级" width="80" />
        <el-table-column prop="status" label="状态" width="90" />
        <el-table-column label="操作" width="120"><template #default><el-button type="primary" link>查看</el-button><el-button type="primary" link>修改</el-button></template></el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getProblemList, exportProblemLedger } from '@/api'

const loading = ref(false), list = ref([]), total = ref(0)
const query = reactive({ enterpriseName: '', dateRange: [], pageNum: 1, pageSize: 10 })

const search = async () => {
  loading.value = true
  try {
    const params = { ...query }
    if (params.dateRange?.length === 2) {
      params.startTime = params.dateRange[0]
      params.endTime = params.dateRange[1]
    }
    delete params.dateRange
    const res = await getProblemList(params)
    list.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { /* 后端未就绪 */ }
  finally { loading.value = false }
}

const exportData = async () => {
  try {
    const res = await exportProblemLedger(query)
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url; a.download = '问题台账.xlsx'; a.click()
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}
</script>
