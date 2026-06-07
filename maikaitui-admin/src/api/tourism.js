import request from '@/utils/request'

// ============ 景点管理 ============
export function getAttractions(params) {
  return request.get('/tourism/attraction/list', { params })
}

export function getAttraction(id) {
  return request.get(`/tourism/attraction/${id}`)
}

export function addAttraction(data) {
  return request.post('/tourism/attraction', data)
}

export function updateAttraction(data) {
  return request.put('/tourism/attraction', data)
}

export function deleteAttraction(id) {
  return request.delete(`/tourism/attraction/${id}`)
}

export function getHotAttractions(limit) {
  return request.get('/tourism/attraction/hot', { params: { limit } })
}

// ============ 地区管理 ============
export function getRegionTree() {
  return request.get('/tourism/region/tree')
}

export function getRegions(params) {
  return request.get('/tourism/region/list', { params })
}

export function addRegion(data) {
  return request.post('/tourism/region', data)
}

export function updateRegion(data) {
  return request.put('/tourism/region', data)
}

export function deleteRegion(id) {
  return request.delete(`/tourism/region/${id}`)
}

// ============ 分类管理 ============
export function getCategoryTree() {
  return request.get('/tourism/category/tree')
}

export function getCategories() {
  return request.get('/tourism/category/list')
}

export function addCategory(data) {
  return request.post('/tourism/category', data)
}

export function updateCategory(data) {
  return request.put('/tourism/category', data)
}

export function deleteCategory(id) {
  return request.delete(`/tourism/category/${id}`)
}

// ============ 订单管理 ============
export function getOrders(params) {
  return request.get('/tourism/order/admin/list', { params })
}

export function getOrder(id) {
  return request.get(`/tourism/order/${id}`)
}

export function updateOrderStatus(id, status) {
  return request.put(`/tourism/order/${id}/status`, { status })
}

export function cancelOrder(id) {
  return request.put(`/tourism/order/${id}/cancel`)
}

// ============ 仪表盘 ============
export function getDashboardData() {
  return request.get('/tourism/dashboard')
}

// ============ 评论管理 ============
export function getComments(attractionId, params) {
  return request.get(`/tourism/comment/list/${attractionId || 0}`, { params })
}

export function getAllComments(params) {
  return request.get('/tourism/comment/admin/list', { params })
}

export function deleteComment(id) {
  return request.delete(`/tourism/comment/${id}`)
}
