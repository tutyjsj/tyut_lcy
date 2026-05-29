<template>
  <el-popover
    :visible="visible"
    placement="bottom-end"
    trigger="click"
    :width="380"
    popper-class="message-notice-popper"
  >
    <template #reference>
      <div class="notice-trigger" @click="toggleVisible">
        <el-badge :value="msgStore.unreadCount" :hidden="msgStore.unreadCount === 0" :max="99">
          <el-icon :size="20"><Bell /></el-icon>
        </el-badge>
      </div>
    </template>

    <!-- 消息面板 -->
    <div class="notice-panel">
      <!-- 头部 -->
      <div class="notice-header">
        <div class="notice-title">
          <span>消息通知</span>
          <el-tag v-if="msgStore.unreadCount > 0" size="small" type="danger" round effect="dark">
            {{ msgStore.unreadCount }}条未读
          </el-tag>
        </div>
        <div class="notice-actions">
          <el-button link type="primary" size="small" @click="handleMarkAllRead">
            全部已读
          </el-button>
        </div>
      </div>

      <!-- 分类标签 -->
      <div class="notice-tabs">
        <span
          v-for="tab in tabs"
          :key="tab.key"
          class="notice-tab"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <sup v-if="getTabUnread(tab.key) > 0" class="tab-badge">{{ getTabUnread(tab.key) }}</sup>
        </span>
      </div>

      <!-- 消息列表 -->
      <div class="notice-body" v-loading="msgStore.loading">
        <template v-if="filteredMessages.length > 0">
          <div
            v-for="msg in filteredMessages"
            :key="msg.id"
            class="notice-item"
            :class="{ unread: !msg.read }"
            @click="handleClickMessage(msg)"
          >
            <div class="item-icon" :style="{ background: getTypeConfig(msg.type).color + '15', color: getTypeConfig(msg.type).color }">
              <el-icon><component :is="getTypeConfig(msg.type).icon" /></el-icon>
            </div>
            <div class="item-content">
              <div class="item-title">{{ msg.title }}</div>
              <div class="item-desc">{{ msg.content }}</div>
              <div class="item-meta">
                <span class="item-type-tag" :style="{ color: getTypeConfig(msg.type).color }">
                  {{ getTypeConfig(msg.type).label }}
                </span>
                <span class="item-time">{{ formatTime(msg.createTime) }}</span>
              </div>
            </div>
            <div v-if="!msg.read" class="unread-dot"></div>
          </div>
        </template>
        <el-empty v-else description="暂无消息" :image-size="80" />
      </div>

      <!-- 底部 -->
      <div class="notice-footer">
        <el-button text type="primary" size="small" @click="goToMessageCenter">
          查看全部消息
          <el-icon style="margin-left: 4px;"><ArrowDown /></el-icon>
        </el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/message'

const props = defineProps({
  /** 自动拉取间隔(ms)，默认60秒 */
  pollInterval: { type: Number, default: 60000 }
})

const router = useRouter()
const msgStore = useMessageStore()
const visible = ref(false)
const activeTab = ref('ALL')

const tabs = [
  { key: 'ALL', label: '全部' },
  { key: 'URGE', label: '催办' },
  { key: 'SUPERVISE', label: '督办' },
  { key: 'DEADLINE', label: '截止' },
  { key: 'SMS', label: '短信' }
]

const filteredMessages = computed(() => {
  if (activeTab.value === 'ALL') return msgStore.messages
  return msgStore.messages.filter(m => m.type === activeTab.value)
})

function toggleVisible() {
  visible.value = !visible.value
  if (visible.value && msgStore.messages.length === 0) {
    msgStore.fetchMessages()
  }
}

function getTypeConfig(type) {
  return msgStore.typeConfig[type] || { icon: 'Bell', color: '#909399', label: '通知', descPrefix: '' }
}

function handleClickMessage(msg) {
  if (!msg.read) {
    msgStore.markRead([msg.id])
  }
  visible.value = false

  // 根据消息类型和关联数据跳转
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

function handleMarkAllRead() {
  msgStore.markAllMessageRead()
}

function goToMessageCenter() {
  visible.value = false
  router.push('/message/center')
}

function getTabUnread(key) {
  if (key === 'ALL') return msgStore.unreadCount
  return msgStore.unreadByType[key] || 0
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
  if (d.getFullYear() === now.getFullYear()) {
    return `${d.getMonth() + 1}月${d.getDate()}日`
  }
  return `${d.getFullYear()}/${d.getMonth() + 1}/${d.getDate()}`
}

onMounted(() => {
  msgStore.fetchUnreadCount()
  msgStore.startPolling(props.pollInterval)
})

onUnmounted(() => {
  msgStore.stopPolling()
})
</script>

<style scoped>
.notice-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  cursor: pointer;
  color: #606266;
  transition: all 0.25s;
}
.notice-trigger:hover {
  background: #ecf5ff;
  color: #409EFF;
}

.notice-panel {
  padding: 0;
}
.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 10px;
  border-bottom: 1px solid #f0f0f0;
}
.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}
.notice-tabs {
  display: flex;
  gap: 4px;
  padding: 8px 16px 6px;
  border-bottom: 1px solid #f5f5f5;
  overflow-x: auto;
}
.notice-tab {
  padding: 3px 10px;
  font-size: 12px;
  color: #909399;
  border-radius: 12px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
  position: relative;
}
.notice-tab:hover { color: #409EFF; background: #ecf5ff; }
.notice-tab.active {
  color: #fff;
  background: #409EFF;
}
.tab-badge {
  position: absolute;
  top: -4px;
  right: -2px;
  min-width: 14px;
  height: 14px;
  line-height: 14px;
  font-size: 10px;
  background: #F56C6C;
  color: #fff;
  border-radius: 7px;
  text-align: center;
  padding: 0 3px;
  transform: scale(0.85);
}

.notice-body {
  max-height: 360px;
  overflow-y: auto;
  padding: 4px 0;
}
.notice-body::-webkit-scrollbar { width: 4px; }
.notice-body::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 2px; }

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}
.notice-item:hover { background: #f5f7fa; }
.notice-item.unread { background: #fafbff; }
.item-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 17px;
}
.item-content {
  flex: 1;
  min-width: 0;
}
.item-title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.item-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 4px;
}
.item-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.item-type-tag {
  font-size: 11px;
  font-weight: 500;
}
.item-time {
  font-size: 11px;
  color: #C0C4CC;
}
.unread-dot {
  width: 8px;
  height: 8px;
  background: #409EFF;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 5px;
  animation: pulse-dot 2s infinite;
}
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

.notice-footer {
  padding: 10px 16px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}
</style>

<!-- 全局样式：覆盖 popover 内滚动条 -->
<style>
.message-notice-popper {
  padding: 0 !important;
  border-radius: 12px !important;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1), 0 0 1px rgba(0, 0, 0, 0.08) !important;
}
</style>
