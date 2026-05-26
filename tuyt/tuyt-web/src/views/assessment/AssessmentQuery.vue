<template>
  <div>
    <div class="page-title">考评结果查询</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="考评时间"><el-date-picker v-model="query.month" type="month" placeholder="选择月份" value-format="YYYY-MM" /></el-form-item>
        <el-form-item label="网格名称"><el-input v-model="query.gridName" clearable /></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="gridName" label="网格名称" />
        <el-table-column prop="score" label="考评分数" />
        <el-table-column prop="responseRate" label="响应率" />
        <el-table-column prop="disposalRate" label="处置及时率" />
        <el-table-column prop="completeRate" label="完成率" />
        <el-table-column label="操作"><template #default><el-button type="primary" link>详情</el-button></template></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAssessResult } from '@/api'

const loading = ref(false), list = ref([])
const query = reactive({ month: '', gridName: '' })

const search = async () => {
  loading.value = true
  try { const r = await getAssessResult(query); list.value = r.data?.list || r.data || [] } catch {}
  finally { loading.value = false }
}
onMounted(search)
</script>
