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
    </div>

    <!-- 查看详情 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="600px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="任务编号"><span>{{ currentRow.taskNo }}</span></el-form-item>
        <el-form-item label="任务标题"><span>{{ currentRow.taskTitle }}</span></el-form-item>
        <el-form-item label="任务类型"><span>{{ typeText(currentRow.taskType) }}</span></el-form-item>
        <el-form-item label="紧急程度"><span>{{ urgencyText(currentRow.urgency) }}</span></el-form-item>
        <el-form-item label="完结时间"><span>{{ currentRow.finishTime }}</span></el-form-item>
        <el-form-item label="任务内容"><span>{{ currentRow.taskContent || '-' }}</span></el-form-item>
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

const loading = ref(false)
const list = ref([])
const detailVisible = ref(false)
const currentRow = ref({})

const typeText = (v) => v === 'DAILY' ? '日常巡查' : v === 'SHUTDOWN' ? '停产巡查' : '问题核查'
const urgencyText = (v) => v === 'URGENT' ? '特急' : v === 'NORMAL' ? '一般' : '紧急'

const openDetail = async (row) => {
  currentRow.value = { ...row }
  try {
    const res = await getTaskDetail(row.id)
    if (res.data) currentRow.value = res.data
  } catch { /* 使用列表数据兜底 */ }
  detailVisible.value = true
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDoneList({ pageNum: 1, pageSize: 20 })
    list.value = res.data?.records || res.data?.list || []
  } catch { /* 后端未就绪 */ }
  finally { loading.value = false }
})
</script>
