import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { loginApi, logoutApi } from '@/api/login'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const username = ref(sessionStorage.getItem('tuyt_username') || '')
  const realName = ref(sessionStorage.getItem('tuyt_realname') || '')
  const roles = ref([])

  const login = async (loginForm) => {
    try {
      const res = await loginApi({
        username: loginForm.username,
        password: loginForm.password
      })
      // 后端返回 { data: { token, userInfo: { username, realName, ... } } }
      const data = res.data
      const info = data.userInfo || data
      token.value = data.token
      username.value = info.username || ''
      realName.value = info.realName || info.username || ''
      roles.value = info.roles || []
      setToken(data.token)
      sessionStorage.setItem('tuyt_username', info.username || '')
      sessionStorage.setItem('tuyt_realname', info.realName || info.username || '')
      return true
    } catch {
      return false
    }
  }

  const logout = () => {
    token.value = ''
    username.value = ''
    realName.value = ''
    roles.value = []
    removeToken()
    sessionStorage.removeItem('tuyt_username')
    sessionStorage.removeItem('tuyt_realname')
    router.push('/login')
  }

  return { token, username, realName, roles, login, logout }
})
