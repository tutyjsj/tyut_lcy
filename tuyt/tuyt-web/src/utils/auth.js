const TOKEN_KEY = 'tuyt_token'

// 使用 sessionStorage：关闭浏览器标签页后 Token 自动清除，下次进入必须重新登录
export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  sessionStorage.removeItem(TOKEN_KEY)
}
