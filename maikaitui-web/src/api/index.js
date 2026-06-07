import request from '@/utils/request'

// ==================== Auth API ====================

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function refreshToken(token) {
  return request.post('/auth/refresh', { token })
}

export function getUserInfo() {
  return request.get('/auth/userinfo')
}

export function updateProfile(data) {
  return request.put('/auth/profile', data)
}

// ==================== Attractions API ====================

export function getHotAttractions(limit = 10) {
  return request.get('/tourism/attraction/hot', { params: { limit } })
}

export function getAttractions(params) {
  return request.get('/tourism/attraction/list', { params })
}

export function getAttractionById(id) {
  return request.get(`/tourism/attraction/${id}`)
}

export function getRecommendations(attractionId, limit = 6) {
  return request.get(`/tourism/attraction/${attractionId}/recommendations`, { params: { limit } })
}

export function getRegions() {
  return request.get('/tourism/region/tree')
}

export function getCategories() {
  return request.get('/tourism/category/tree')
}

// ==================== Orders API ====================

export function createOrder(data, payNow = false) {
  return request.post('/tourism/order', data, { params: { payNow } })
}

export function getMyOrders(params) {
  return request.get('/tourism/order/list', { params })
}

export function cancelOrder(id) {
  return request.put(`/tourism/order/${id}/cancel`)
}

export function payOrder(id) {
  return request.put(`/tourism/order/${id}/pay`)
}

// ==================== Favorites API ====================

export function addFavorite(attractionId) {
  return request.post('/tourism/favorite', null, { params: { attractionId } })
}

export function removeFavorite(attractionId) {
  return request.delete(`/tourism/favorite/${attractionId}`)
}

export function getMyFavorites(params) {
  return request.get('/tourism/favorite/list', { params })
}

export function checkFavorited(attractionId) {
  return request.get(`/tourism/favorite/check/${attractionId}`)
}

// ==================== Comments API ====================

export function getComments(attractionId, params) {
  return request.get(`/tourism/comment/list/${attractionId}`, { params })
}

export function addComment(data) {
  return request.post('/tourism/comment', data)
}

export function deleteComment(id) {
  return request.delete(`/tourism/comment/${id}`)
}

// ==================== File API ====================

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}

// ==================== Guides API ====================

export function getGuides(params) {
  return request.get('/tourism/guide/list', { params })
}

export function getHotGuides(limit = 6) {
  return request.get('/tourism/guide/hot', { params: { limit } })
}

export function getGuideById(id) {
  return request.get(`/tourism/guide/${id}`)
}

export function getGuidesByAttraction(attractionId, limit = 4) {
  return request.get(`/tourism/guide/attraction/${attractionId}`, { params: { limit } })
}

// ==================== AI API ====================

export function chat(message, sessionId, context) {
  return request.post('/ai/chat', { message, sessionId, context }, { timeout: 120000 })
}

export function getSessions() {
  return request.get('/ai/sessions')
}

export function getChatHistory(sessionId) {
  return request.get('/ai/history', { params: { sessionId } })
}

export function deleteSession(id) {
  return request.delete(`/ai/session/${id}`)
}

export function clearChatHistory() {
  return request.delete('/ai/history')
}

export function getRecommend(preference = '') {
  return request.get('/ai/recommend', { params: { preference } })
}
