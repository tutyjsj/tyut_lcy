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
        <el-table-column prop="gridName" label="网格名称" min-width="140" />
        <el-table-column prop="score" label="考评分数" width="100">
          <template #default="{ row }">
            <span :style="{ color: scoreColor(row.score), fontWeight: 'bold' }">{{ row.score }}分</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="评级" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.level==='A'" type="success">A-优秀</el-tag>
            <el-tag v-else-if="row.level==='B'" type="">B-良好</el-tag>
            <el-tag v-else-if="row.level==='C'" type="warning">C-合格</el-tag>
            <el-tag v-else-if="row.level==='D'" type="danger">D-待改进</el-tag>
            <span v-else>{{ row.level || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="responseRate" label="响应率" width="90">
          <template #default="{ row }">{{ row.responseRate }}%</template>
        </el-table-column>
        <el-table-column prop="disposalRate" label="处置及时率" width="100">
          <template #default="{ row }">{{ row.disposalRate }}%</template>
        </el-table-column>
        <el-table-column prop="completeRate" label="完成率" width="90">
          <template #default="{ row }">{{ row.completeRate }}%</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }"><el-button type="primary" link @click="showDetail(row)">详情</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetchData" @size-change="fetchData" />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="考评结果详情" width="560px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="网格名称">{{ detail.gridName }}</el-descriptions-item>
          <el-descriptions-item label="考评周期">{{ detail.assessPeriod }}</el-descriptions-item>
          <el-descriptions-item label="综合得分">
            <span :style="{ color: scoreColor(detail.score), fontWeight: 'bold', fontSize: '16px' }">{{ detail.score }}分</span>
          </el-descriptions-item>
          <el-descriptions-item label="评级等级">
            <el-tag v-if="detail.level==='A'" type="success" size="large">A - 优秀 (>=90)</el-tag>
            <el-tag v-else-if="detail.level==='B'" size="large">B - 良好 (>=80)</el-tag>
            <el-tag v-else-if="detail.level==='C'" type="warning" size="large">C - 合格 (>=70)</el-tag>
            <el-tag v-else-if="detail.level==='D'" type="danger" size="large">D - 待改进 (<70)</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应率">{{ detail.responseRate }}%</el-descriptions-item>
          <el-descriptions-item label="处置及时率">{{ detail.disposalRate }}%</el-descriptions-item>
          <el-descriptions-item label="任务完成率">{{ detail.completeRate }}%</el-descriptions-item>
        </el-descriptions>
        <!-- 分数条形图 -->
        <div style="margin-top:24px">
          <h4 style="margin-bottom:16px;color:#303133">各项指标对比</h4>
          <div style="display:flex;flex-direction:column;gap:12px">
            <div><div style="display:flex;justify-content:space-between;margin-bottom:4px;font-size:13px"><span>响应率</span><span>{{ detail.responseRate }}%</span></div><el-progress :percentage="Number(detail.responseRate)" :color="progressColor(Number(detail.responseRate))" /></div>
            <div><div style="display:flex;justify-content:space-between;margin-bottom:4px;font-size:13px"><span>处置及时率</span><span>{{ detail.disposalRate }}%</span></div><el-progress :percentage="Number(detail.disposalRate)" :color="progressColor(Number(detail.disposalRate))" /></div>
            <div><div style="display:flex;justify-content:space-between;margin-bottom:4px;font-size:13px"><span>任务完成率</span><span>{{ detail.completeRate }}%</span></div><el-progress :percentage="Number(detail.completeRate)" :color="progressColor(Number(detail.completeRate))" /></div>
          </div>
        </div>
      </template>
      <template #footer><el-button @click="detailVisible=false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAssessResult } from '@/api'

const loading = ref(false), list = ref([]), total = ref(0)
const query = reactive({ month: '', gridName: '', pageNum: 1, pageSize: 10 })

// 详情
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = (row) => {
  detail.value = row
  detailVisible.value = true
}

const scoreColor = (score) => {
  if (!score) return '#909399'
  const s = Number(score)
  if (s >= 90) return '#67C23A'
  if (s >= 80) return '#409EFF'
  if (s >= 70) return '#E6A23C'
  return '#F56C6C'
}

const progressColor = (val) => {
  if (val >= 90) return '#67C23A'
  if (val >= 75) return '#409EFF'
  if (val >= 60) return '#E6A23C'
  return '#F56C6C'
}

const fetchData = async () => {
  loading.value = true
  try {
    const r = await getAssessResult({ ...query })
    list.value = r.data?.records || r.data?.list || []
    total.value = r.data?.total || list.value.length
  } catch (e) {
    console.error('考评结果查询失败:', e)
    ElMessage.error('加载失败，请检查网络或联系管理员')
  }
  finally { loading.value = false }
}
const search = () => { query.pageNum = 1; fetchData() }
onMounted(fetchData)
</script>
