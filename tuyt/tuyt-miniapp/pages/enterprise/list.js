// 企业列表逻辑
const { getEnterpriseList } = require('../../utils/api')
const { superviseTypeMap, productionStatusMap } = require('../../utils/constants')
const app = getApp()

Page({
  data: {
    list: [], loading: false, pageNum: 1, pageSize: 10, hasMore: true, keyword: ''
  },
  onShow() {
    if (!app.checkLogin()) { wx.redirectTo({ url: '/pages/login/login' }); return }
  },
  onLoad() {
    this.fetchData()
  },
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) this.fetchData()
  },
  onKeyword(e) { this.data.keyword = e.detail.value },
  search() {
    this.setData({ list: [], pageNum: 1, hasMore: true })
    this.fetchData()
  },
  async fetchData() {
    this.setData({ loading: true })
    try {
      const params = { pageNum: this.data.pageNum, pageSize: this.data.pageSize }
      if (this.data.keyword) params.enterpriseName = this.data.keyword
      const res = await getEnterpriseList(params)
      const data = res.data || {}
      const records = (data.records || data.list || []).map(item => ({
        ...item,
        superviseTypeLabel: superviseTypeMap[item.superviseType] || '',
        productionStatusLabel: productionStatusMap[item.productionStatus] || item.productionStatus || '正常生产'
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
  goDetail(e) { wx.navigateTo({ url: '/pages/enterprise/detail?id=' + e.currentTarget.dataset.id }) }
})
