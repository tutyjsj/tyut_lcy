// 消息列表逻辑
const { getMessageList, markRead, markAllRead, getUnreadCount } = require('../../utils/api')
const app = getApp()

Page({
  data: {
    list: [], loading: false, pageNum: 1, pageSize: 15, hasMore: true
  },
  onShow() {
    if (!app.checkLogin()) { wx.redirectTo({ url: '/pages/login/login' }); return }
    this.fetchData()
  },
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ pageNum: this.data.pageNum + 1 })
      this.fetchData()
    }
  },
  async fetchData() {
    this.setData({ loading: true })
    try {
      const res = await getMessageList({ pageNum: 1, pageSize: this.data.pageSize })
      const data = res.data || {}
      const records = (data.records || data.list || []).map(item => ({
        ...item,
        isRead: item.isRead || item.readFlag || false
      }))
      this.setData({ list: records, hasMore: records.length >= this.data.pageSize })
    } catch (e) {
      /* ignore */
    } finally { this.setData({ loading: false }) }
  },

  async readMsg(e) {
    const { id, index } = e.currentTarget.dataset
    const msg = this.data.list[index]
    if (msg.isRead) return // 已读不重复标记
    try {
      await markRead({ ids: [id] })
      this.setData({ [`list[${index}].isRead`]: true })
    } catch (e) { /* ignore */ }
    // 展开详情
    wx.showModal({
      title: msg.title || '消息详情',
      content: msg.content || '暂无内容',
      showCancel: false
    })
  },

  async markAll() {
    try {
      await markAllRead()
      const list = this.data.list.map(m => ({ ...m, isRead: true }))
      this.setData({ list })
      wx.showToast({ title: '已全部标记已读', icon: 'success' })
    } catch (e) {
      wx.showToast({ title: '操作失败', icon: 'none' })
    }
  }
})
