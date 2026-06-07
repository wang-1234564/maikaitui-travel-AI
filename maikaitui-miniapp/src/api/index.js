import request, { parsePage, sortIndexToKey } from '@/utils/request.js'

// Auth
export const login = (data) => request('/auth/login', 'POST', data)
export const register = (data) => request('/auth/register', 'POST', data)
export const getUserInfo = () => request('/auth/userinfo')
export const updateProfile = (data) => request('/auth/profile', 'PUT', data)

// Miniapp 聚合
export const getHomeData = () => request('/tourism/miniapp/home')
export const getUserStats = () => request('/tourism/miniapp/user/stats')
export const miniappRecommend = (preference, limit = 6) =>
  request(`/tourism/miniapp/recommend?preference=${encodeURIComponent(preference || '')}&limit=${limit}`)

// Attractions
export const getHotAttractions = (limit = 10) => request(`/tourism/attraction/hot?limit=${limit}`)
export const getAttractions = (params = {}) => {
  const { page = 1, pageSize = 10, sort, sortBy, ...rest } = params
  const sb = sortBy ?? (sort !== undefined ? sortIndexToKey(sort) : undefined)
  return request('/tourism/attraction/list', 'GET', {
    page,
    size: pageSize,
    ...(sb ? { sortBy: sb } : {}),
    ...rest
  }).then(parsePage)
}
export const getAttractionById = (id) => request(`/tourism/attraction/${id}`)
export const getRecommendations = (id, limit = 5) =>
  request(`/tourism/attraction/${id}/recommendations?limit=${limit}`)
export const getRegions = () => request('/tourism/region/tree')
export const getCategories = () => request('/tourism/category/tree')

// Orders
export const createOrder = (data) => request('/tourism/order', 'POST', data)
export const getMyOrders = (params = {}) => {
  const { page = 1, pageSize = 10, ...rest } = params
  return request('/tourism/order/list', 'GET', { page, size: pageSize, ...rest }).then(parsePage)
}
export const cancelOrder = (id) => request(`/tourism/order/${id}/cancel`, 'PUT')
export const payOrder = (id) => request(`/tourism/order/${id}/status?status=paid`, 'PUT')

// Favorites
export const addFavorite = (attractionId) =>
  request(`/tourism/favorite?attractionId=${attractionId}`, 'POST')
export const removeFavorite = (attractionId) =>
  request(`/tourism/favorite/${attractionId}`, 'DELETE')
export const getMyFavorites = (params = {}) => {
  const { page = 1, pageSize = 10 } = params
  return request('/tourism/favorite/list', 'GET', { page, size: pageSize }).then(parsePage)
}
export const checkFavorited = (attractionId) => request(`/tourism/favorite/check/${attractionId}`)

// Comments
export const getComments = (attractionId, params = {}) => {
  const { page = 1, pageSize = 10 } = params
  return request(`/tourism/comment/list/${attractionId}`, 'GET', { page, size: pageSize }).then(parsePage)
}
export const addComment = (data) => request('/tourism/comment', 'POST', data)

// AI
export const aiChat = (message, sessionId) =>
  request('/ai/chat', 'POST', { message, sessionId })
export const aiSessions = () => request('/ai/sessions')
export const aiHistory = (sessionId) => request(`/ai/history?sessionId=${sessionId}`)
export const aiClearHistory = () => request('/ai/history', 'DELETE')

export { parsePage, sortIndexToKey }
export default request
