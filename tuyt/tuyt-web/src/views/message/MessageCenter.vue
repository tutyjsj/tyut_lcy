
<template>
  <div class="message-center">
    <div class="page-title">消息中心</div>
    <div class="mc-layout">
      <!-- 左侧分类导航 -->
      <div class="mc-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">消息分类</span>
        </div>
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="sidebar-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          <div class="sidebar-item-inner">
            <el-icon :size="18" :color="activeTab === tab.key ? '#409EFF' : '#909399'">
              <component :is="tab.icon" />
            </el-icon>
            <span>{{ tab.label }}</span>
          </div>
          <el-badge v-if="getTabUnread(tab.key) > 0" :value="getTabUnread(tab.key)" :max="99" type="danger" />
        </div>
      </div>

      <!-- 右侧消息列表 -->
      <div class="mc-main">
        <div class="mc-toolbar">
          <div class="toolbar-left">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索消息内容…"
              clearable
              size="default"
              style="width:260px"
              :prefix-icon="Search"
              @input="onSearchInput"
            />
          </div>
          <div class="toolbar-right">
            <el-button text type="primary" @click="handleMarkAllRead" :disabled="unreadTotal === 0">
              全部标为已读
            </el-button>
            <el-button text type="danger" @click="handleClearRead" :disabled="readCount === 0">
              清空已读消息
            </el-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="mc-list" v-loading="loading">
          <template v-if="filteredMessages.length > 0">
            <div
              v-for="msg in filteredMessages"
              :key="msg.id"
              class="mc-item"
              :class="{ unread: !msg.read }"
              @click="handleClickMessage(msg)"
            >
              <div class="mc-item-left">
                <div class="mc-item-icon" :style="{ background: getTypeConfig(msg.type).color + '12', color: getTypeConfig(msg.type).color }">
                  <el-icon :size="22"><component :is="getTypeConfig(msg.type).icon" /></el-icon>
                </div>
              </div>
              <div class="mc-item-body">
                <div class="mc-item-header">
                  <span class="mc-item-title">{{ msg.title }}</span>
                  <el-tag size="small" :color="getTypeConfig(msg.type).color" effect="plain" round>
                    {{ getTypeConfig(msg.type).label }}
                  </el-tag>
                </div>
                <div class="mc-item-desc">{{ msg.content }}</div>
                <div class="mc-item-footer">
                  <span class="mc-item-source" v-if="msg.sourceName">来源：{{ msg.sourceName }}</span>
                  <span class="mc-item-time">{{ formatTime(msg.createTime) }}</span>
                </div>
              </div>
              <div class="mc-item-right">
                <div v-if="!msg.read" class="unread-dot"></div>
                <div class="mc-item-actions">
                  <el-button v-if="!msg.read" link type="primary" size="small" @click.stop="markSingle(msg)">标为已读</el-button>
                  <el-button link type="danger" size="small" @click.stop="deleteSingle(msg)">删除</el-button>
                </div>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无消息" :image-size="120" style="margin-top:80px" />
        </div>

        <!-- 分页 -->
        <div class="mc-pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @current-change="fetchMessages"
            @size-change="fetchMessages"
          />
        </div>
      </div>
    </div>

    <!-- 删除单条消息确认弹框 -->
    <el-dialog v-model="deleteVisible" width="480px" align-center :show-close="false" append-to-body destroy-on-close>
      <div class="confirm-header">
        <el-icon color="#E6A23C" :size="24"><WarningFilled /></el-icon>
        <span class="confirm-title">删除消息</span>
      </div>
      <div class="confirm-info-card">
        <div class="info-row"><span class="info-label">消息标题</span><span class="info-value">{{ deleteRow.title || '-' }}</span></div>
        <div class="info-row"><span class="info-label">消息类型</span><span class="info-value">{{ getTypeConfig(deleteRow.type).label }}</span></div>
        <div class="info-row"><span class="info-label">发送时间</span><span class="info-value">{{ formatTime(deleteRow.createTime) }}</span></div>
      </div>
      <div class="confirm-alert">
        <el-icon color="#F56C6C" :size="20"><WarningFilled /></el-icon>
        <div class="alert-content">
          <div class="alert-title">此操作不可撤销，删除后消息将永久移除！</div>
          <div class="alert-desc">注：关联的任务或问题记录将保留不受影响。</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="deleteVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">确认删除</el-button>
      </template>
    </el-dialog>

    <!-- 清空已读消息确认弹框 -->
    <el-dialog v-model="clearVisible" width="480px" align-center :show-close="false" append-to-body destroy-on-close>
      <div class="confirm-header">
        <el-icon color="#E6A23C" :size="24"><WarningFilled /></el-icon>
        <span class="confirm-title">清空已读消息</span>
      </div>
      <div class="confirm-info-card">
        <div class="info-row"><span class="info-label">选中数量</span><span class="info-value">{{ readCount }} 条已读消息</span></div>
        <div class="info-row"><span class="info-label">未读消息</span><span class="info-value">保留 {{ unreadTotal }} 条未读消息</span></div>
      </div>
      <div class="confirm-alert">
        <el-icon color="#F56C6C" :size="20"><WarningFilled /></el-icon>
        <div class="alert-content">
          <div class="alert-title">此操作不可撤销，已读消息将被永久清除！</div>
          <div class="alert-desc">注：未读消息将保留，可继续查看。</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="clearVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmClear">确认清空</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/message'
import { ElMessage } from 'element-plus'
import { Search, WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()
const msgStore = useMessageStore()

const activeTab = ref('ALL')
const searchKeyword = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const tabs = [
  { key: 'ALL', label: '全部消息', icon: 'Bell' },
  { key: 'URGE', label: '催办通知', icon: 'Promotion' },
  { key: 'SUPERVISE', label: '督办通知', icon: 'WarningFilled' },
  { key: 'DEADLINE', label: '截止提醒', icon: 'Timer' },
  { key: 'SMS', label: '短信通知', icon: 'ChatDotRound' },
]

const unreadTotal = computed(() => msgStore.unreadCount)
const readCount = computed(() => msgStore.messages.filter(m => m.read).length)

// ===== 删除单条 =====
const deleteVisible = ref(false)
const deleteRow = ref({})

function confirmDelete() {
  msgStore.messages = msgStore.messages.filter(m => m.id !== deleteRow.value.id)
  deleteVisible.value = false
  ElMessage.success('已删除')
}

// ===== 清空已读 =====
const clearVisible = ref(false)

function confirmClear() {
  msgStore.messages = msgStore.messages.filter(m => !m.read)
  clearVisible.value = false
  ElMessage.success('已清空已读消息')
}

const filteredMessages = computed(() => {
  let msgs = msgStore.messages
  if (activeTab.value !== 'ALL') {
    msgs = msgs.filter(m => m.type === activeTab.value)
  }
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    msgs = msgs.filter(m =>
      m.title?.toLowerCase().includes(kw) ||
      m.content?.toLowerCase().includes(kw)
    )
  }
  total.value = msgs.length
  const start = (currentPage.value - 1) * pageSize.value
  return msgs.slice(start, start + pageSize.value)
})

function getTabUnread(key) {
  if (key === 'ALL') return msgStore.unreadCount
  return msgStore.unreadByType[key] || 0
}

function getTypeConfig(type) {
  return msgStore.typeConfig[type] || { icon: 'Bell', color: '#909399', label: '通知' }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 172800000) return '昨天'
  const month = d.getMonth() + 1
  const date = d.getDate()
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  if (d.getFullYear() === now.getFullYear()) {
    return `${month}月${date}日 ${hour}:${minute}`
  }
  return `${d.getFullYear()}-${month}-${date} ${hour}:${minute}`
}

function handleClickMessage(msg) {
  if (!msg.read) markSingle(msg)
  if (msg.relatedType === 'task') {
    router.push({ path: '/work/todo', query: { taskId: msg.relatedId } })
  } else if (msg.relatedType === 'problem') {
    router.push({ path: '/dispatch/problem', query: { problemId: msg.relatedId } })
  } else if (msg.relatedType === 'sms') {
    router.push({ path: '/dispatch/voice' })
  } else if (msg.relatedType === 'calendar') {
    router.push({ path: '/dispatch/calendar' })
  }
}

async function markSingle(msg) {
  try { await msgStore.markRead([msg.id]) } catch { /* ignore */ }
}

async function handleMarkAllRead() {
  if (unreadTotal.value === 0) return
  try {
    await msgStore.markAllMessageRead()
    ElMessage.success('已全部标为已读')
  } catch { ElMessage.error('操作失败') }
}

async function handleClearRead() {
  if (readCount.value === 0) { ElMessage.warning('没有已读消息可清空'); return }
  clearVisible.value = true
}

async function deleteSingle(msg) {
  deleteRow.value = msg
  deleteVisible.value = true
}

async function fetchMessages() {
  loading.value = true
  try {
    await msgStore.fetchMessages()
  } catch {
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

let searchTimer = null
function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1 }, 300)
}

watch(activeTab, () => { currentPage.value = 1 })

onMounted(() => {
  fetchMessages()
  msgStore.fetchUnreadCount()
})

onUnmounted(() => {
  clearTimeout(searchTimer)
})
</script>

<style scoped>
.message-center {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}
.mc-layout {
  flex: 1;
  display: flex;
  gap: 0;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

/* 左侧边栏 */
.mc-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #eee;
  padding: 8px 0;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 12px 16px 8px;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 4px;
}
.sidebar-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}
.sidebar-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  color: #606266;
  border-right: 2px solid transparent;
}
.sidebar-item:hover {
  background: #f5f7fa;
}
.sidebar-item.active {
  background: #ecf5ff;
  color: #409EFF;
  border-right-color: #409EFF;
  font-weight: 500;
}
.sidebar-item-inner {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 主内容区 */
.mc-main {
  flex: 1;
  background: #fff;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.mc-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.toolbar-left, .toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 消息列表 */
.mc-list {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}
.mc-list::-webkit-scrollbar { width: 5px; }
.mc-list::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 3px; }

.mc-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px;
  border-bottom: 1px solid #f8f8f8;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}
.mc-item:hover { background: #f9fafc; }
.mc-item.unread { background: #f8faff; }
.mc-item.unread:hover { background: #f0f5ff; }

.mc-item-left { flex-shrink: 0; }
.mc-item-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mc-item-body {
  flex: 1;
  min-width: 0;
}
.mc-item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}
.mc-item-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mc-item-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
}
.mc-item-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #C0C4CC;
}
.mc-item-source { color: #909399; }

.mc-item-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.unread-dot {
  width: 9px;
  height: 9px;
  background: #409EFF;
  border-radius: 50%;
  animation: pulse-dot 2s infinite;
}
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.3); }
}
.mc-item-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}
.mc-item:hover .mc-item-actions { opacity: 1; }

.mc-pagination {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

/* 确认弹窗样式 */
.confirm-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.confirm-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}
.confirm-info-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
}
.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}
.info-row:last-child {
  border-bottom: none;
}
.info-label {
  width: 80px;
  color: #909399;
  font-size: 14px;
  flex-shrink: 0;
}
.info-value {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  word-break: break-all;
}
.confirm-alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #fef0f0;
  border-left: 4px solid #f56c6c;
  border-radius: 4px;
  padding: 12px 14px;
  margin-bottom: 8px;
}
.alert-content {
  flex: 1;
}
.alert-title {
  font-size: 14px;
  font-weight: 600;
  color: #f56c6c;
  margin-bottom: 4px;
}
.alert-desc {
  font-size: 13px;
  color: #c45656;
}
</style>
