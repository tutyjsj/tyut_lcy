import request from '@/utils/request'

export function loginApi(data) {
  return request.post('/login', data)
}

export function logoutApi() {
  return request.post('/logout')
}
