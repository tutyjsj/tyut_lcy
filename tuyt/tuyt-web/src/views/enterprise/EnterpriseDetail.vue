<template>
  <div>
    <div class="page-title">一企一档</div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="企业名称">{{ info.name }}</el-descriptions-item>
          <el-descriptions-item label="企业编码">{{ info.code }}</el-descriptions-item>
          <el-descriptions-item label="法人代表">{{ info.legalPerson }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ info.address }}</el-descriptions-item>
          <el-descriptions-item label="监管类型">{{ info.supervisionType }}</el-descriptions-item>
          <el-descriptions-item label="所属网格">{{ info.gridName }}</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
      <el-tab-pane label="许可证" name="license">许可证信息</el-tab-pane>
      <el-tab-pane label="物联监管" name="iot">物联设备数据</el-tab-pane>
      <el-tab-pane label="历史问题" name="history">历史环境问题列表</el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getEnterpriseDetail } from '@/api'

const route = useRoute()
const activeTab = ref('basic')
const info = ref({ name: '-', code: '-', legalPerson: '-', address: '-', supervisionType: '-', gridName: '-' })

onMounted(async () => {
  try {
    const res = await getEnterpriseDetail(route.params.id)
    if (res.data) info.value = res.data
  } catch { /* 后端未就绪 */ }
})
</script>
