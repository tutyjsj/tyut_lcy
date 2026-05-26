<template>
  <div class="login-container">
    <div class="login-panel">
      <div class="login-header">
        <el-icon :size="48" color="#409EFF"><Monitor /></el-icon>
        <h2>环境网格化综合管理系统</h2>
        <p>Environmental Grid Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名"><template #prefix><el-icon><User /></el-icon></template></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password @keyup.enter="handleLogin"><template #prefix><el-icon><Lock /></el-icon></template></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Monitor, User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref(null)
const form = reactive({ username: 'admin', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  const ok = await userStore.login(form)
  loading.value = false
  if (ok) {
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } else {
    ElMessage.error('用户名或密码错误')
  }
}
</script>

<style scoped>
.login-container {
  display: flex; align-items: center; justify-content: center;
  height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-panel {
  width: 420px; padding: 40px; background: #fff; border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.login-header { text-align: center; margin-bottom: 32px; }
.login-header h2 { margin: 12px 0 4px; color: #303133; }
.login-header p { color: #909399; font-size: 13px; }
</style>
