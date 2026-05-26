<template>
  <div>
    <div class="page-title">通讯录</div>
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="姓名"><el-input v-model="query.name" clearable /></el-form-item>
        <el-form-item label="组织机构"><el-input v-model="query.org" clearable /></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button><el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-card">
      <el-table :data="list" v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="type" label="类型" width="80" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="org" label="组织机构" width="150" />
        <el-table-column prop="position" label="职位" width="100" />
        <el-table-column label="操作" width="140"><template #default="{ row }"><el-button link @click="openDialog(row)">编辑</el-button><el-button link type="primary" @click="dialPhone(row.phone)">拨号</el-button><el-button link type="danger" @click="handleDelete(row.id)">删除</el-button></template></el-table-column>
      </el-table>
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑联系人':'新增联系人'" width="450px">
      <el-form label-width="80px">
        <el-form-item label="类型"><el-select v-model="contactForm.type" style="width:100%"><el-option label="单位" value="ORG" /><el-option label="个人" value="PERSON" /></el-select></el-form-item>
        <el-form-item label="姓名"><el-input v-model="contactForm.name" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="contactForm.phone" /></el-form-item>
        <el-form-item label="组织机构"><el-input v-model="contactForm.org" /></el-form-item>
        <el-form-item label="职位"><el-input v-model="contactForm.position" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContactList, saveContact, deleteContact } from '@/api'

const loading = ref(false), dialogVisible = ref(false), isEdit = ref(false), list = ref([])
const contactForm = reactive({ id: null, type: '', name: '', phone: '', org: '', position: '' })
const query = reactive({ name: '', org: '' })

const search = async () => {
  loading.value = true
  try {
    const res = await getContactList(query)
    list.value = res.data?.records || res.data?.list || res.data || []
  } catch { /* 后端未就绪 */ }
  finally { loading.value = false }
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    Object.assign(contactForm, row)
  } else {
    isEdit.value = false
    Object.assign(contactForm, { id: null, type: '', name: '', phone: '', org: '', position: '' })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await saveContact(contactForm)
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    search()
  } catch { ElMessage.error('保存失败') }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后该联系人信息将无法恢复，是否继续？', '删除联系人', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }).then(async () => {
    try { await deleteContact(id); ElMessage.success('已删除'); search() } catch { ElMessage.error('删除失败') }
  }).catch(() => {})
}

const dialPhone = (phone) => { window.open('tel:' + phone) }

onMounted(search)
</script>
