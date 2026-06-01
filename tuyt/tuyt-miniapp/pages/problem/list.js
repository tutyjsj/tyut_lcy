// 问题列表逻辑
const { getProblemList } = require('../../utils/api')
const { handleStatusMap, handleStatusTagType, getTagClass, problemLevelMap, pollutionTypeMap } = require('../../utils/constants')
const app = getApp()

Page({
  data: {
    list: [], loading: false, pageNum: 1, pageSize: 10, hasMore: true,
    keyword: '',
    showFilterPanel: false,
    selectedStatus: '', selectedLevel: '', selectedType: '',
    selectedStatusLabel: '', selectedLevelLabel: '', selectedTypeLabel: '',
    statusOptions: [
      { value: '', label: '全部状态' },
      ...Object.entries(handleStatusMap).map(([k, v]) => ({ value: k, label: v }))
    ],
    levelOptions: [
      { value: '', label: '全部等级' },
      ...Object.entries(problemLevelMap).map(([k, v]) => ({ value: k, label: v.label }))
    ],
    typeOptions: [
      { value: '', label: '全部类型' },
      ...Object.entries(pollutionTypeMap).map(([k, v]) => ({ value: k, label: v }))
    ]
  },

  onShow() {
    if (!app.checkLogin()) { wx.redirectTo({ url: '/pages/login/login' }); return }
    this.setData({ list: [], pageNum: 1, hasMore: true })
    this.fetchData()
  },
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) this.fetchData()
  },

  async fetchData() {
    this.setData({ loading: true })
    try {
      const { keyword, selectedStatus, selectedLevel, selectedType, pageNum, pageSize } = this.data
      const params = { pageNum, pageSize }
      if (keyword) params.enterpriseName = keyword
      if (selectedStatus) params.handleStatus = selectedStatus
      if (selectedLevel) params.problemLevel = selectedLevel
      if (selectedType) params.pollutionType = selectedType
      const res = await getProblemList(params)
      const data = res.data || {}
      const records = (data.records || data.list || []).map(item => ({
        ...item,
        statusLabel: handleStatusMap[item.handleStatus] || item.handleStatus,
        statusClass: getTagClass(handleStatusTagType[item.handleStatus]),
        levelLabel: problemLevelMap[item.problemLevel]?.label || item.problemLevel,
        levelClass: problemLevelMap[item.problemLevel]?.tagType === 'danger' ? 'tag-danger' : problemLevelMap[item.problemLevel]?.tagType === 'warning' ? 'tag-warning' : '',
        typeLabel: pollutionTypeMap[item.pollutionType] || item.pollutionType || ''
      }))
      this.setData({
        list: pageNum === 1 ? records : [...this.data.list, ...records],
        pageNum: pageNum + 1,
        hasMore: records.length >= pageSize
      })
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally { this.setData({ loading: false }) }
  },

  onKeyword(e) { this.data.keyword = e.detail.value },
  showFilter() { this.setData({ showFilterPanel: !this.data.showFilterPanel }) },
  onStatusChange(e) {
    const opt = this.data.statusOptions[e.detail.value]
    this.setData({ selectedStatus: opt.value, selectedStatusLabel: opt.label })
  },
  onLevelChange(e) {
    const opt = this.data.levelOptions[e.detail.value]
    this.setData({ selectedLevel: opt.value, selectedLevelLabel: opt.label })
  },
  onTypeChange(e) {
    const opt = this.data.typeOptions[e.detail.value]
    this.setData({ selectedType: opt.value, selectedTypeLabel: opt.label })
  },
  applyFilter() {
    this.setData({ showFilterPanel: false, list: [], pageNum: 1, hasMore: true })
    this.fetchData()
  },
  goReport() { wx.navigateTo({ url: '/pages/problem/report' }) },
  goDetail(e) { wx.navigateTo({ url: '/pages/problem/detail?id=' + e.currentTarget.dataset.id }) }
})
