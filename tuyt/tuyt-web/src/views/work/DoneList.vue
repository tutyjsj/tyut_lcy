<template>
  <div>
    <div class="page-title">我的完结件</div>
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading" empty-text="暂无完结件">
        <el-table-column prop="taskNo" label="任务编号" width="160" />
        <el-table-column prop="taskTitle" label="任务标题" min-width="200" />
        <el-table-column prop="finishTime" label="完结时间" width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
    </div>

    <!-- 查看详情 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="600px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="任务编号"><span>{{ currentRow.taskNo }}</span></el-form-item>
        <el-form-item label="任务标题"><span>{{ currentRow.taskTitle }}</span></el-form-item>
        <el-form-item label="任务类型"><span>{{ typeText(currentRow.taskType) }}</span></el-form-item>
        <el-form-item label="紧急程度"><span>{{ urgencyText(currentRow.urgency) }}</span></el-form-item>
        <el-form-item label="完结时间"><span>{{ currentRow.finishTime }}</span></el-form-item>
        <el-form-item label="任务内容"><span style="white-space:pre-wrap">{{ formatContent(currentRow.taskContent) }}</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDoneList, getTaskDetail } from '@/api'
import { taskTypeMap, urgencyMap, rectifyStatusMap, productionStatusMap } from '@/utils/constants'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const detailVisible = ref(false)
const currentRow = ref({})

const typeText = (v) => taskTypeMap[v] || v
const urgencyText = (v) => urgencyMap[v] || v
const formatContent = (text) => {
  if (!text) return '-'
  let result = text
  // 替换整改情况
  Object.entries(rectifyStatusMap).forEach(([key, label]) => {
    result = result.replace(new RegExp(`整改情况:\\s*${key}`, 'g'), `整改情况: ${label}`)
  })
  // 替换生产经营情况
  Object.entries(productionStatusMap).forEach(([key, label]) => {
    result = result.replace(new RegExp(`生产经营:\\s*${key}`, 'g'), `生产经营: ${label}`)
  })
  return result
}

const openDetail = async (row) => {
  currentRow.value = { ...row }
  try {
    const res = await getTaskDetail(row.id)
    if (res.data) currentRow.value = res.data
  } catch { /* 使用列表数据兜底 */ }
  detailVisible.value = true
}

const fetch = async () => {
  loading.value = true
  try {
    const res = await getDoneList({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch { /* 后端未就绪 */ }
  finally { loading.value = false }
}

onMounted(fetch)
</script>
