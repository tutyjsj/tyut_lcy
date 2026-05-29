<template>
  <div>
    <div class="page-title">污染源档案</div>
    <div class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="企业名称"><el-input v-model="query.name" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="监管类型">
          <el-select v-model="query.superviseType" clearable placeholder="全部" style="width: 140px">
            <el-option v-for="opt in supervisionTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属网格">
          <el-select v-model="query.gridId" clearable placeholder="全部" style="width: 160px">
            <el-option v-for="g in gridOptions" :key="g.id" :label="g.gridName" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button @click="reset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="enterpriseCode" label="企业编码" width="140" />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="200" />
        <el-table-column prop="address" label="地址" min-width="180" />
        <el-table-column prop="superviseType" label="监管类型" width="100"><template #default="{ row }">{{ superviseTypeMap[row.superviseType] || row.superviseType }}</template></el-table-column>
        <el-table-column prop="legalPerson" label="法人" width="100" />
        <el-table-column prop="status" label="状态" width="80"><template #default><el-tag type="success" size="small">正常</el-tag></template></el-table-column>
        <el-table-column label="操作" width="100"><template #default="{ row }"><el-button type="primary" link @click="$router.push('/enterprise/detail/'+row.id)">查看</el-button></template></el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetch" @size-change="fetch" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getEnterpriseList, getGridList } from '@/api'
import { superviseTypeMap, supervisionTypeOptions } from '@/utils/constants'

const loading = ref(false), list = ref([]), total = ref(0)
const query = reactive({ name: '', superviseType: '', gridId: null, pageNum: 1, pageSize: 10 })
const gridOptions = ref([])

const fetch = async () => {
  loading.value = true
  const r = await getEnterpriseList({ ...query }).catch(() => ({ data: { records: [], total: 0 } }))
  const d = r.data || {}
  list.value = d.records || d.list || []
  total.value = d.total ?? list.value.length
  loading.value = false
}
const search = () => { query.pageNum = 1; fetch() }
const reset = () => { query.name = ''; query.superviseType = ''; query.gridId = null; search() }

onMounted(async () => {
  try {
    const r = await getGridList({ gridLevel: 1, pageSize: 100 })
    gridOptions.value = (r.data && r.data.records) ? r.data.records : []
  } catch { /* 忽略 */ }
  fetch()
})
</script>
