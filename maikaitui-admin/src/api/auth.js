import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function getUserInfo() {
  return request.get('/auth/user/info')
}

export function refreshToken(token) {
  return request.post('/auth/refresh', { token })
}
