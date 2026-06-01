// 认证工具 - Token管理
const app = getApp()

export function getToken() {
  return app.getToken()
}

export function isLoggedIn() {
  return app.checkLogin()
}

export function getUserInfo() {
  return app.globalData.userInfo
}

export function getRoleName() {
  const user = app.globalData.userInfo
  if (!user) return '未知'
  const roleMap = {
    admin: '超级管理员',
    grid_leader: '网格长',
    inspector: '巡查员',
    dispatcher: '调度员',
    analyst: '数据分析员'
  }
  return roleMap[user.roleCode] || user.roleCode || '工作人员'
}
