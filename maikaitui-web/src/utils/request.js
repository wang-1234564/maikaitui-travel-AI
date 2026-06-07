import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

function notify(message) {
  if (typeof window !== 'undefined' && message) {
    window.dispatchEvent(new CustomEvent('maikaitui:notify', { detail: message }))
  }
}

// Request interceptor - attach token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    const userInfo = (() => {
      try { return JSON.parse(localStorage.getItem('userInfo')) } catch { return null }
    })()
    if (userInfo?.id || userInfo?.userId) {
      config.headers['X-User-Id'] = userInfo.id || userInfo.userId
    }
    if (userInfo?.username) {
      config.headers['X-User-Name'] = userInfo.username
    }
    if (userInfo?.avatar) {
      config.headers['X-User-Avatar'] = userInfo.avatar
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor - handle response format
request.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code !== undefined) {
      if (res.code === 200 || res.code === 0) {
        return res.data !== undefined ? res.data : res
      } else if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
        return Promise.reject(new Error(res.message || '未授权'))
      } else {
        notify(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    }

    return res
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
      } else if (status === 500) {
        notify('服务器错误，请稍后重试')
      }
    } else if (error.code === 'ECONNABORTED') {
      notify('请求超时，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
