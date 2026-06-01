// API工具模块 - 封装wx.request，自动附加JWT Token

const app = getApp()

// 基础请求
function request(options) {
  return new Promise((resolve, reject) => {
    const token = app.getToken()
    const header = {
      'Content-Type': 'application/json'
    }
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    // wx.request的data为对象时，需要手动JSON序列化
    let payload = options.data || {}
    if (options.method !== 'GET' && typeof payload === 'object') {
      payload = JSON.stringify(payload)
    }

    wx.request({
      url: `${app.globalData.baseUrl}${options.url}`,
      method: options.method || 'GET',
      header: header,
      data: payload,
      timeout: 15000,
      success(res) {
        if (res.statusCode === 200) {
          const body = res.data
          if (body.code === 200) {
            resolve(body)
          } else if (body.code === 401) {
            wx.showToast({ title: '登录已过期，请重新登录', icon: 'none', duration: 2000 })
            setTimeout(() => app.logout(), 2000)
            reject(new Error(body.message || '未登录'))
          } else {
            reject(new Error(body.message || '请求失败'))
          }
        } else if (res.statusCode === 401) {
          wx.showToast({ title: '登录已过期', icon: 'none' })
          setTimeout(() => app.logout(), 1500)
          reject(new Error('未登录'))
        } else {
          reject(new Error(`服务器错误(${res.statusCode})`))
        }
      },
      fail(err) {
        console.error('请求失败:', options.url, err)
        wx.showToast({ title: `网络错误：${err.errMsg || '请检查服务器'}` , icon: 'none', duration: 2000 })
        reject(err)
      }
    })
  })
}

// GET请求
function get(url, data = {}) {
  // 将data参数拼接到url上
  const params = Object.keys(data)
    .filter(key => data[key] !== undefined && data[key] !== null && data[key] !== '')
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
    .join('&')
  const fullUrl = params ? `${url}?${params}` : url
  return request({ url: fullUrl, method: 'GET' })
}

// POST请求
function post(url, data = {}) {
  return request({ url, method: 'POST', data })
}

// PUT请求
function put(url, data = {}) {
  return request({ url, method: 'PUT', data })
}

// DELETE请求
function del(url, data = {}) {
  return request({ url, method: 'DELETE', data })
}

// ==================== API接口 ====================

// --- 登录 ---
export function login(username, password) {
  return post('/login', { username, password })
}

export function logout() {
  return post('/logout')
}

export function getUserInfo() {
  return get('/user/info')
}

// --- 我的工作 ---
export function getTodoList(params = {}) {
  return get('/work/todo', params)
}

export function handleTodo(id, data) {
  return put(`/work/todo/${id}`, data)
}

export function getTransferList(params = {}) {
  return get('/work/transfer', params)
}

export function getDoneList(params = {}) {
  return get('/work/done', params)
}

// --- 环境问题 ---
export function getProblemList(params = {}) {
  return get('/problem/list', params)
}

export function getProblemDetail(id) {
  return get(`/problem/${id}`)
}

export function createProblem(data) {
  return post('/problem', data)
}

export function updateProblem(id, data) {
  return put(`/problem/${id}`, data)
}

export function closeProblem(data) {
  return put('/problem/close', data)
}

export function getProblemStatistics() {
  return get('/problem/statistics')
}

export function getProblemLogs(id) {
  return get(`/problem/${id}/logs`)
}

// --- 任务 ---
export function getTaskDetail(id) {
  return get(`/task/${id}`)
}

export function returnTask(id) {
  return post(`/task/${id}/return`)
}

export function publishTask(id) {
  return put(`/task/${id}/publish`)
}

// --- 企业 ---
export function getEnterpriseList(params = {}) {
  return get('/enterprise/list', params)
}

export function getEnterpriseDetail(id) {
  return get(`/enterprise/${id}`)
}

// --- 消息 ---
export function getMessageList(params = {}) {
  return get('/message/list', params)
}

export function getUnreadCount() {
  return get('/message/unread-count')
}

export function markRead(data) {
  return put('/message/read', data)
}

export function markAllRead() {
  return put('/message/read-all')
}

// --- 网格 ---
export function getGridTree() {
  return get('/grid/tree')
}

export function getGridList(params = {}) {
  return get('/grid/list', params)
}

// --- 巡查计划 ---
export function getPatrolPlanList(params = {}) {
  return get('/patrol-plan/list', params)
}

// --- 通讯录 ---
export function getContactList(params = {}) {
  return get('/config/contacts', params)
}

export default {
  get, post, put, del
}
