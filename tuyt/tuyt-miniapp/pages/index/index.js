// 首页逻辑
const { getTodoList, getProblemList, getUnreadCount, getProblemStatistics } = require('../../utils/api')
const { handleStatusMap, handleStatusTagType, getTagClass, getRoleName } = require('../../utils/constants')
const { getUserInfo } = require('../../utils/auth')
const app = getApp()

Page({
  data: {
    userName: '',
    roleName: '',
    today: '',
    unreadCount: 0,
    stats: { pending: 0, processing: 0, done: 0 },
    recentProblems: []
  },

  onShow() {
    if (!app.checkLogin()) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    const user = app.globalData.userInfo
    this.setData({
      userName: user?.realName || user?.username || '用户',
      roleName: getRoleName(),
      today: this.formatToday()
    })
    this.loadData()
  },

  onPullDownRefresh() {
    this.loadData().then(() => wx.stopPullDownRefresh())
  },

  formatToday() {
    const d = new Date()
    const week = ['日','一','二','三','四','五','六']
    return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日 星期${week[d.getDay()]}`
  },

  async loadData() {
    try {
      // 并行加载
      const [todoRes, problemRes, unreadRes] = await Promise.allSettled([
        getTodoList({ pageSize: 1 }),
        getProblemList({ pageSize: 5 }),
        getUnreadCount()
      ])

      // 待办统计
      let pending = 0, processing = 0, done = 0
      if (todoRes.status === 'fulfilled' && todoRes.value.data) {
        // 使用统计数据
        const d = todoRes.value.data
        pending = d.total || d.pending || 0
      }

      // 问题统计
      let statRes
      try { statRes = await getProblemStatistics() } catch (e) { /* ignore */ }
      if (statRes && statRes.data) {
        const s = statRes.data
        pending = s.pending || pending
        processing = s.processing || 0
        done = s.done || 0
      }

      // 最近问题
      const recentProblems = []
      if (problemRes.status === 'fulfilled' && problemRes.value.data) {
        const list = problemRes.value.data.records || problemRes.value.data.list || []
        list.forEach(item => {
          recentProblems.push({
            ...item,
            statusLabel: handleStatusMap[item.handleStatus] || item.handleStatus,
            statusClass: getTagClass(handleStatusTagType[item.handleStatus])
          })
        })
      }

      // 未读数
      let count = 0
      if (unreadRes.status === 'fulfilled' && unreadRes.value.data !== undefined) {
        count = unreadRes.value.data || 0
      }

      this.setData({ stats: { pending, processing, done }, recentProblems, unreadCount: count })
    } catch (e) {
      console.error('首页数据加载失败:', e)
    }
  },

  goTodo() { wx.switchTab({ url: '/pages/work/todo' }) },
  goTransfer() { wx.navigateTo({ url: '/pages/work/transfer' }) },
  goDone() { wx.navigateTo({ url: '/pages/work/done' }) },
  goReport() { wx.navigateTo({ url: '/pages/problem/report' }) },
  goProblems() { wx.switchTab({ url: '/pages/problem/list' }) },
  goProblemDetail(e) { wx.navigateTo({ url: '/pages/problem/detail?id=' + e.currentTarget.dataset.id }) },
  goEnterprises() { wx.navigateTo({ url: '/pages/enterprise/list' }) },
  goPatrol() { wx.navigateTo({ url: '/pages/enterprise/list' }) },
  goMessages() { wx.switchTab({ url: '/pages/message/list' }) }
})
