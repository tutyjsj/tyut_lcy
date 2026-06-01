// 问题详情逻辑
const { getProblemDetail, getProblemLogs } = require('../../utils/api')
const { handleStatusMap, handleStatusTagType, getTagClass, problemLevelMap, pollutionTypeMap, problemSourceMap } = require('../../utils/constants')
const app = getApp()

Page({
  data: {
    detail: {}, logs: []
  },
  onLoad(options) {
    if (!app.checkLogin()) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    this.loadData(options.id)
  },

  async loadData(id) {
    try {
      const [detailRes, logsRes] = await Promise.allSettled([
        getProblemDetail(id),
        getProblemLogs(id)
      ])

      if (detailRes.status === 'fulfilled' && detailRes.value.data) {
        const d = detailRes.value.data
        this.setData({
          detail: {
            ...d,
            statusLabel: handleStatusMap[d.handleStatus] || d.handleStatus,
            statusClass: getTagClass(handleStatusTagType[d.handleStatus]),
            levelLabel: problemLevelMap[d.problemLevel]?.label || d.problemLevel || '-',
            typeLabel: pollutionTypeMap[d.pollutionType] || d.pollutionType || '-',
            sourceLabel: problemSourceMap[d.problemSource] || d.problemSource || '-'
          }
        })
      }

      if (logsRes.status === 'fulfilled' && logsRes.value.data) {
        const logs = Array.isArray(logsRes.value.data) ? logsRes.value.data :
          (logsRes.value.data.records || logsRes.value.data.list || [])
        this.setData({ logs })
      }
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  }
})
