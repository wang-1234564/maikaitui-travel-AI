const isH5 = typeof window !== 'undefined' && typeof document !== 'undefined'
const BASE_URL = import.meta.env?.VITE_API_BASE_URL || (isH5 ? '/api' : 'http://127.0.0.1:8080/api')

const SORT_KEYS = ['hot', 'rating', 'price', 'newest']

export const getToken = () => {
  try { return uni.getStorageSync('token') || '' } catch (e) { return '' }
}

export const toast = (msg) => {
  try { uni.showToast({ title: msg, icon: 'none' }) } catch (e) { /* ignore */ }
}

export const parsePage = (data) => {
  if (!data) return { list: [], total: 0 }
  if (Array.isArray(data)) return { list: data, total: data.length }
  const list = data.records || data.list || []
  return { list, total: data.total ?? data.totalCount ?? list.length }
}

export const sortIndexToKey = (index) => SORT_KEYS[index] || 'hot'

const request = (url, method = 'GET', data = {}) => {
  const token = getToken()
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 401) {
          toast('请先登录')
          reject(res.data)
          return
        }
        if (res.data?.code === 200 || res.data?.code === 0) {
          resolve(res.data.data)
        } else {
          toast(res.data?.message || '请求失败')
          reject(res.data)
        }
      },
      fail: () => {
        toast('网络错误，请检查网关或后端微服务')
        reject(new Error('network'))
      }
    })
  })
}

export default request
