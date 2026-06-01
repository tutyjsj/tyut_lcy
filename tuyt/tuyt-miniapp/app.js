// 环境网格化综合管理系统 - 小程序端
App({
  globalData: {
    userInfo: null,
    token: '',
    baseUrl: 'http://localhost:8080', // 开发环境，上线改为实际域名
    isLoggedIn: false
  },

  onLaunch() {
    // 检查本地存储的token
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')
    if (token && userInfo) {
      this.globalData.token = token
      this.globalData.userInfo = userInfo
      this.globalData.isLoggedIn = true
    }
  },

  // 检查登录状态
  checkLogin() {
    return this.globalData.isLoggedIn && this.globalData.token
  },

  // 获取token - 供api.js使用
  getToken() {
    return this.globalData.token
  },

  // 保存登录状态
  saveLogin(token, userInfo) {
    this.globalData.token = token
    this.globalData.userInfo = userInfo
    this.globalData.isLoggedIn = true
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', userInfo)
  },

  // 退出登录
  logout() {
    this.globalData.token = ''
    this.globalData.userInfo = null
    this.globalData.isLoggedIn = false
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
    wx.reLaunch({ url: '/pages/login/login' })
  }
})
