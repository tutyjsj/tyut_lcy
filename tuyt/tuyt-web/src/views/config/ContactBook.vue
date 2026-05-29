<template>
  <div>
    <div class="page-title">通讯录</div>
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="搜索姓名/电话/机构/职位" clearable style="width:280px" @keyup.enter="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.contactType" placeholder="全部类型" clearable style="width:140px" @change="search">
        <el-option label="个人" value="PERSON" />
        <el-option label="单位" value="ORG" />
      </el-select>
      <el-button type="primary" @click="search"><el-icon><Search /></el-icon>查询</el-button>
      <el-button @click="reset"><el-icon><RefreshRight /></el-icon>重置</el-button>
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增联系人</el-button>
    </div>
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="contactType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.contactType==='ORG'?'warning':'info'">
              {{ row.contactType==='ORG'?'单位':'个人' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="电话" min-width="140" />
        <el-table-column prop="orgName" label="组织机构" min-width="160" />
        <el-table-column prop="position" label="职位" min-width="120" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="success" @click="dialPhone(row.phone)">拨号</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;display:flex;justify-content:flex-end" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="fetchData" @size-change="fetchData" />
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑联系人':'新增联系人'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="类型">
          <el-select v-model="form.contactType" style="width:100%" placeholder="请选择类型">
            <el-option label="个人" value="PERSON" />
            <el-option label="单位" value="ORG" />
          </el-select>
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" placeholder="请输入姓名" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" placeholder="请输入电话号码" /></el-form-item>
        <el-form-item label="组织机构"><el-input v-model="form.orgName" placeholder="请输入组织机构" /></el-form-item>
        <el-form-item label="职位"><el-input v-model="form.position" placeholder="请输入职位" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, RefreshRight } from '@element-plus/icons-vue'
import { getContactList, saveContact, deleteContact } from '@/api'

const loading = ref(false), dialogVisible = ref(false), isEdit = ref(false), list = ref([]), total = ref(0)
const form = reactive({ id: null, contactType: 'PERSON', name: '', phone: '', orgName: '', position: '' })
const query = reactive({ keyword: '', contactType: '', pageNum: 1, pageSize: 10 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getContactList({ ...query })
    list.value = res.data?.records || res.data?.list || res.data || []
    total.value = res.data?.total ?? list.value.length
  } catch {} finally { loading.value = false }
}
const search = () => { query.pageNum = 1; fetchData() }
const reset = () => { query.keyword = ''; query.contactType = ''; search() }

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    Object.assign(form, { id: null, contactType: 'PERSON', name: '', phone: '', orgName: '', position: '' })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await saveContact(form)
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    search()
  } catch { ElMessage.error('保存失败') }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `<div class="del-detail">
      <div class="del-row"><span class="del-label">姓名</span><span class="del-value"><strong>${row.name || '-'}</strong></span></div>
      <div class="del-row"><span class="del-label">类型</span><span class="del-value">${row.contactType==='ORG'?'单位':'个人'}</span></div>
      <div class="del-row"><span class="del-label">电话</span><span class="del-value">${row.phone || '-'}</span></div>
      <div class="del-row"><span class="del-label">组织机构</span><span class="del-value">${row.orgName || '-'}</span></div>
      <div class="del-row"><span class="del-label">职位</span><span class="del-value">${row.position || '-'}</span></div>
    </div>
    <div class="del-warning">
      <div class="del-warning-title">此操作不可撤销，删除后该联系人将永久移除！</div>
      <div class="del-warning-note">注：删除后相关调度记录中的联系人信息不受影响。</div>
    </div>`,
    '删除联系人',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
      draggable: false,
      center: true,
      appendTo: document.body,
      customClass: 'assess-del-dialog',
      closeOnClickModal: false,
      closeOnPressEscape: false
    }
  ).then(async () => {
    try { await deleteContact(row.id); ElMessage.success('已删除'); search() }
    catch { ElMessage.error('删除失败') }
  }).catch(() => {})
}

const dialPhone = (phone) => { window.open('tel:' + phone) }

onMounted(search)
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 700; color: #1d2129; margin-bottom: 20px; }
.search-bar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.table-card { background: #fff; border-radius: 8px; padding: 4px 0 0 0; }
</style>

<style>
/* 删除弹窗样式已统一在 global.scss 中定义（assess-del-dialog / del-detail 等） */
</style>
