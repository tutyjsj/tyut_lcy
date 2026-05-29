<template>
  <div>
    <div class="page-title">我的运转件</div>
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading" empty-text="暂无运转件">
        <el-table-column prop="taskNo" label="任务编号" width="160" />
        <el-table-column prop="taskTitle" label="任务标题" min-width="200" />
        <el-table-column prop="transferType" label="运转类型" width="100"><template #default="{ row }"><el-tag :type="row.transferType==='RETURN'?'warning':'success'" size="small">{{ row.transferType==='RETURN'?'退回':'转交' }}</el-tag></template></el-table-column>
        <el-table-column prop="transferTime" label="运转时间" width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
    </div>

    <!-- 查看详情 -->
    <el-dialog v-model="detailVisible" title="运转详情" width="600px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="任务编号"><span>{{ currentRow.taskNo }}</span></el-form-item>
        <el-form-item label="任务标题"><span>{{ currentRow.taskTitle }}</span></el-form-item>
        <el-form-item label="运转类型"><span>{{ currentRow.transferType==='RETURN'?'退回':'转交' }}</span></el-form-item>
        <el-form-item label="运转时间"><span>{{ currentRow.transferTime }}</span></el-form-item>
        <el-form-item label="运转说明"><span>{{ currentRow.transferReason || '-' }}</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTransferList, getTaskDetail } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const detailVisible = ref(false)
const currentRow = ref({})

const openDetail = async (row) => {
  currentRow.value = { ...row }
  try {
    const res = await getTaskDetail(row.taskId || row.id)
    // 保留列表中的运转字段（详情接口不会返回 transferTime/transferType/transferReason）
    if (res.data) currentRow.value = { ...res.data, transferType: row.transferType, transferTime: row.transferTime, transferReason: row.transferReason }
  } catch { /* 使用列表数据兜底 */ }
  detailVisible.value = true
}

const fetch = async () => {
  loading.value = true
  try {
    const res = await getTransferList({ pageNum: pageNum.value, pageSize: pageSize.value })
    list.value = res.data?.records || res.data?.list || []
    total.value = Number(res.data?.total) || 0
  } catch { /* 后端未就绪 */ }
  finally { loading.value = false }
}

onMounted(fetch)
</script>
