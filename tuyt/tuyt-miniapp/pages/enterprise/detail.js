// 企业详情逻辑
const { getEnterpriseDetail, getProblemList } = require('../../utils/api')
const { superviseTypeMap, productionStatusMap, handleStatusMap, handleStatusTagType, getTagClass } = require('../../utils/constants')
const app = getApp()

Page({
  data: { detail: {}, problems: [] },
  onLoad(options) {
    if (!app.checkLogin()) { wx.redirectTo({ url: '/pages/login/login' }); return }
    this.loadData(options.id)
  },
  async loadData(id) {
    try {
      const [entRes, probRes] = await Promise.allSettled([
        getEnterpriseDetail(id),
        getProblemList({ enterpriseName: '', pageSize: 10 })
      ])
      if (entRes.status === 'fulfilled' && entRes.value.data) {
        const d = entRes.value.data
        this.setData({
          detail: {
            ...d,
            superviseTypeLabel: superviseTypeMap[d.superviseType] || d.superviseType || '-',
            productionStatusLabel: productionStatusMap[d.productionStatus] || d.productionStatus || '-'
          }
        })
      }
      if (probRes.status === 'fulfilled' && probRes.value.data) {
        const records = probRes.value.data.records || probRes.value.data.list || []
        const problems = records
          .filter(p => p.enterpriseName === this.data.detail.enterpriseName || p.enterpriseName === this.data.detail.name)
          .map(p => ({
            ...p,
            statusLabel: handleStatusMap[p.handleStatus] || p.handleStatus,
            statusClass: getTagClass(handleStatusTagType[p.handleStatus])
          }))
        this.setData({ problems })
      }
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },
  goProblem(e) { wx.navigateTo({ url: '/pages/problem/detail?id=' + e.currentTarget.dataset.id }) }
})
