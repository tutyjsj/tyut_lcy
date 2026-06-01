// 问题上报逻辑
const { createProblem } = require('../../utils/api')
const { pollutionTypeOptions, problemLevelMap, problemSourceOptions } = require('../../utils/constants')
const app = getApp()

Page({
  data: {
    enterpriseName: '', problemDesc: '', address: '',
    pollutionType: '', problemLevel: '', problemSource: '',
    selectedTypeLabel: '', selectedLevelLabel: '', selectedSourceLabel: '',
    typeOptions: pollutionTypeOptions,
    levelOptions: Object.entries(problemLevelMap).map(([k, v]) => ({ value: k, label: v.label })),
    sourceOptions: problemSourceOptions,
    images: [], submitting: false
  },

  onShow() {
    if (!app.checkLogin()) { wx.redirectTo({ url: '/pages/login/login' }); return }
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [field]: e.detail.value })
  },
  onTypeChange(e) {
    const opt = this.data.typeOptions[e.detail.value]
    this.setData({ pollutionType: opt.value, selectedTypeLabel: opt.label })
  },
  onLevelChange(e) {
    const opt = this.data.levelOptions[e.detail.value]
    this.setData({ problemLevel: opt.value, selectedLevelLabel: opt.label })
  },
  onSourceChange(e) {
    const opt = this.data.sourceOptions[e.detail.value]
    this.setData({ problemSource: opt.value, selectedSourceLabel: opt.label })
  },
  chooseImage() {
    wx.chooseImage({
      count: 6 - this.data.images.length,
      sizeType: ['compressed'],
      success: res => {
        this.setData({ images: [...this.data.images, ...res.tempFilePaths] })
      }
    })
  },
  previewImage(e) {
    wx.previewImage({ urls: this.data.images, current: e.currentTarget.dataset.url })
  },
  async submit() {
    const { enterpriseName, problemDesc, pollutionType, problemLevel, problemSource, address } = this.data
    if (!enterpriseName.trim()) { wx.showToast({ title: '请输入企业名称', icon: 'none' }); return }
    if (!problemDesc.trim()) { wx.showToast({ title: '请填写问题描述', icon: 'none' }); return }
    if (!pollutionType) { wx.showToast({ title: '请选择污染类型', icon: 'none' }); return }
    if (!problemLevel) { wx.showToast({ title: '请选择问题等级', icon: 'none' }); return }
    if (!problemSource) { wx.showToast({ title: '请选择问题来源', icon: 'none' }); return }

    this.setData({ submitting: true })
    try {
      await createProblem({
        enterpriseName: enterpriseName.trim(),
        problemDesc: problemDesc.trim(),
        pollutionType,
        problemLevel,
        problemSource,
        address: address.trim()
      })
      wx.showToast({ title: '上报成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 1500)
    } catch (err) {
      wx.showToast({ title: err.message || '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  }
})
