<template>
  <div>
    <div class="page-title">网格排名</div>
    <el-row :gutter="20">
      <el-col :span="4" v-for="(item, idx) in rankingList" :key="item.id">
        <div class="rank-card" :class="'rank-'+ (idx+1)">
          <div class="rank-badge">{{ idx + 1 }}</div>
          <div class="rank-name">{{ item.name }}</div>
          <div class="rank-person">负责人: {{ item.person }}</div>
          <div class="rank-data">
            <span>问题总数: <b>{{ item.problemCount }}</b></span>
            <span>待处理: <b style="color:#F56C6C">{{ item.pendingCount }}</b></span>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getGridRanking } from '@/api'

const rankingList = ref([])

onMounted(async () => {
  try {
    const res = await getGridRanking({ pageNum: 1, pageSize: 4 })
    rankingList.value = res.data?.records || res.data?.list || []
  } catch { /* 后端未就绪时使用默认 */ }
})
</script>

<style scoped>
.rank-card { background:#fff; border-radius:8px; padding:24px 16px; text-align:center; box-shadow:0 2px 12px rgba(0,0,0,0.06); position:relative; }
.rank-badge { width:36px; height:36px; border-radius:50%; display:flex; align-items:center; justify-content:center; color:#fff; font-weight:700; font-size:18px; margin:0 auto 12px; }
.rank-1 .rank-badge { background:#F56C6C; } .rank-2 .rank-badge { background:#E6A23C; } .rank-3 .rank-badge { background:#409EFF; } .rank-4 .rank-badge { background:#909399; }
.rank-name { font-size:16px; font-weight:600; color:#303133; margin-bottom:6px; }
.rank-person { font-size:13px; color:#909399; margin-bottom:12px; }
.rank-data { display:flex; justify-content:space-around; font-size:13px; color:#606266; }
</style>
