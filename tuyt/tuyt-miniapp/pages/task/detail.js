// 任务详情逻辑
const { getTaskDetail, returnTask, publishTask } = require('../../utils/api')
const { taskStatusMap, getTagClass } = require('../../utils/constants')
const app = getApp()

Page({
  data: {
    detail: {},
    canOperate: false
  },
  onLoad(options) {
    if (!app.checkLogin()) { wx.redirectTo({ url: '/pages/login/login' }); return }
    this.loadData(options.id)
  },

  async loadData(id) {
    try {
      const res = await getTaskDetail(id)
      const d = res.data || {}
      const status = d.status || d.taskStatus || ''
      this.setData({
        detail: {
          ...d,
          statusLabel: taskStatusMap[status] || status || '待处理',
          statusClass: getTagClass(status === 'DONE' || status === 'COMPLETED' ? 'success' :
            status === 'DISPATCHED' || status === 'RECEIVED' || status === 'PROCESSING' ? 'primary' :
            status === 'RETURNED' || status === 'REVOKED' ? 'danger' : 'warning')
        },
        canOperate: ['DISPATCHED', 'RECEIVED'].includes(status)
      })
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  handleProcess() {
    wx.showModal({
      title: '确认签收',
      content: '签收后将开始处理此任务',
      success: async (res) => {
        if (res.confirm) {
          try {
            await publishTask(this.data.detail.id || this.data.detail.taskId)
            wx.showToast({ title: '已签收', icon: 'success' })
            setTimeout(() => this.loadData(this.data.detail.id || this.data.detail.taskId), 800)
          } catch (err) {
            wx.showToast({ title: err.message || '操作失败', icon: 'none' })
          }
        }
      }
    })
  },

  handleReturn() {
    wx.showModal({
      title: '退回任务',
      editable: true,
      placeholderText: '请输入退回原因',
      success: async (res) => {
        if (res.confirm) {
          try {
            await returnTask(this.data.detail.id || this.data.detail.taskId)
            wx.showToast({ title: '已退回', icon: 'success' })
            setTimeout(() => wx.navigateBack(), 1000)
          } catch (err) {
            wx.showToast({ title: err.message || '退回失败', icon: 'none' })
          }
        }
      }
    })
  },

  goProblem() {
    if (this.data.detail.problemId) {
      wx.navigateTo({ url: '/pages/problem/detail?id=' + this.data.detail.problemId })
    }
  }
})
