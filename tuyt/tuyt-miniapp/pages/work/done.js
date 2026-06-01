// 完结件逻辑
const { getDoneList } = require('../../utils/api')
const app = getApp()

Page({
  data: {
    list: [],
    loading: false,
    pageNum: 1,
    pageSize: 10,
    hasMore: true
  },
  onShow() {
    if (!app.checkLogin()) return
    this.setData({ list: [], pageNum: 1, hasMore: true })
    this.fetchData()
  },
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) this.fetchData()
  },
  async fetchData() {
    this.setData({ loading: true })
    try {
      const res = await getDoneList({ pageNum: this.data.pageNum, pageSize: this.data.pageSize })
      const data = res.data || {}
      const records = (data.records || data.list || []).map(item => ({
        ...item,
        statusLabel: '已完成'
      }))
      this.setData({
        list: this.data.pageNum === 1 ? records : [...this.data.list, ...records],
        pageNum: this.data.pageNum + 1,
        hasMore: records.length >= this.data.pageSize
      })
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally { this.setData({ loading: false }) }
  },
  goDetail(e) {
    wx.navigateTo({ url: '/pages/task/detail?id=' + e.currentTarget.dataset.id })
  }
})
