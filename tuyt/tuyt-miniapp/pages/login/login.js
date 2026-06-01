// 登录页逻辑
const { login } = require('../../utils/api')
const app = getApp()

Page({
  data: {
    username: 'admin',
    password: '123456',
    loading: false
  },

  onLoad() {
    // 已登录直接跳转
    if (app.checkLogin()) {
      wx.switchTab({ url: '/pages/index/index' })
    }
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  async handleLogin() {
    const { username, password } = this.data
    if (!username.trim()) {
      wx.showToast({ title: '请输入账号', icon: 'none' })
      return
    }
    if (!password.trim()) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      const res = await login(username.trim(), password.trim())
      console.log('登录响应:', res)
      const data = res.data
      if (data && data.token) {
        app.saveLogin(data.token, data.userInfo || { username })
        wx.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          wx.switchTab({ url: '/pages/index/index' })
        }, 800)
      } else if (res.code && res.code !== 200) {
        wx.showToast({ title: res.message || '登录失败', icon: 'none' })
      } else {
        wx.showToast({ title: '登录失败：响应格式异常', icon: 'none' })
      }
    } catch (err) {
      console.error('登录异常:', err)
      wx.showToast({ title: err.message || '登录失败，请检查网络', icon: 'none', duration: 2500 })
    } finally {
      this.setData({ loading: false })
    }
  }
})
