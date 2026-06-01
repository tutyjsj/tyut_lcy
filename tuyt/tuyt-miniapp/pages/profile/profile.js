// 个人中心逻辑
const { getRoleName } = require('../../utils/constants')
const app = getApp()

Page({
  data: {
    userInfo: {},
    roleName: ''
  },
  onShow() {
    if (!app.checkLogin()) { wx.redirectTo({ url: '/pages/login/login' }); return }
    this.setData({
      userInfo: app.globalData.userInfo || {},
      roleName: getRoleName()
    })
  },
  goTodo() { wx.switchTab({ url: '/pages/work/todo' }) },
  goTransfer() { wx.navigateTo({ url: '/pages/work/transfer' }) },
  goDone() { wx.navigateTo({ url: '/pages/work/done' }) },
  goEnterprises() { wx.navigateTo({ url: '/pages/enterprise/list' }) },
  goContacts() { wx.navigateTo({ url: '/pages/enterprise/list' }) },
  goPatrol() { wx.navigateTo({ url: '/pages/enterprise/list' }) },
  goAbout() {
    wx.showModal({
      title: '环境网格化管理系统',
      content: '大同市生态环境综合管理平台\n小程序端 v1.0\n\n专为一线巡查员和网格管理者设计',
      showCancel: false
    })
  },
  handleLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出当前账号吗？',
      success: (res) => {
        if (res.confirm) app.logout()
      }
    })
  }
})
