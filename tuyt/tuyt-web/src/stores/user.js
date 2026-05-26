import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { loginApi, logoutApi } from '@/api/login'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const username = ref(localStorage.getItem('tuyt_username') || '')
  const realName = ref(localStorage.getItem('tuyt_realname') || '')
  const roles = ref([])

  const login = async (loginForm) => {
    try {
      const res = await loginApi({
        username: loginForm.username,
        password: loginForm.password
      })
      token.value = res.data.token
      username.value = res.data.username
      realName.value = res.data.realName
      roles.value = res.data.roles || []
      setToken(res.data.token)
      localStorage.setItem('tuyt_username', res.data.username)
      localStorage.setItem('tuyt_realname', res.data.realName)
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
    localStorage.removeItem('tuyt_username')
    localStorage.removeItem('tuyt_realname')
    router.push('/login')
  }

  return { token, username, realName, roles, login, logout }
})
