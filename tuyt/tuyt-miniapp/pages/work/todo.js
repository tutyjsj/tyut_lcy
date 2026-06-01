// 待办列表逻辑
const { getTodoList, handleTodo, returnTask, publishTask } = require('../../utils/api')
const { handleStatusMap, handleStatusTagType, getTagClass } = require('../../utils/constants')
const app = getApp()

Page({
  data: {
    list: [],
    loading: false,
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    status: 'all'
  },

  onShow() {
    if (!app.checkLogin()) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.setData({ list: [], pageNum: 1, hasMore: true })
    this.fetchData()
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.fetchData()
    }
  },

  switchTab(e) {
    const status = e.currentTarget.dataset.status
    this.setData({ status, list: [], pageNum: 1, hasMore: true })
    this.fetchData()
  },

  async fetchData() {
    this.setData({ loading: true })
    try {
      const params = { pageNum: this.data.pageNum, pageSize: this.data.pageSize }
      if (this.data.status !== 'all') {
        params.handleStatus = this.data.status
      }
      const res = await getTodoList(params)
      const data = res.data || {}
      const records = (data.records || data.list || []).map(item => ({
        ...item,
        statusLabel: handleStatusMap[item.handleStatus] || item.handleStatus || '待处理',
        statusClass: getTagClass(handleStatusTagType[item.handleStatus])
      }))
      const newList = this.data.pageNum === 1 ? records : [...this.data.list, ...records]
      this.setData({
        list: newList,
        pageNum: this.data.pageNum + 1,
        hasMore: records.length >= this.data.pageSize
      })
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  goDetail(e) {
    const { id, type } = e.currentTarget.dataset
    if (type === 'TASK') {
      wx.navigateTo({ url: `/pages/task/detail?id=${id}` })
    } else {
      wx.navigateTo({ url: `/pages/problem/detail?id=${id}` })
    }
  },

  handleTask(e) {
    const { id } = e.currentTarget.dataset
    wx.showActionSheet({
      itemList: ['签收并处理', '退回任务', '查看详情'],
      success: async (res) => {
        if (res.tapIndex === 0) {
          // 签收处理
          try {
            await handleTodo(id, { action: 'process' })
            wx.showToast({ title: '已签收', icon: 'success' })
            this.setData({ list: [], pageNum: 1, hasMore: true })
            this.fetchData()
          } catch (err) {
            wx.showToast({ title: err.message || '操作失败', icon: 'none' })
          }
        } else if (res.tapIndex === 1) {
          // 退回
          wx.showModal({
            title: '退回任务',
            content: '确定要退回此任务吗？',
            success: async (modalRes) => {
              if (modalRes.confirm) {
                try {
                  await returnTask(id)
                  wx.showToast({ title: '已退回', icon: 'success' })
                  this.setData({ list: [], pageNum: 1, hasMore: true })
                  this.fetchData()
                } catch (err) {
                  wx.showToast({ title: err.message || '退回失败', icon: 'none' })
                }
              }
            }
          })
        } else {
          wx.navigateTo({ url: `/pages/task/detail?id=${id}` })
        }
      }
    })
  }
})
