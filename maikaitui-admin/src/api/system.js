import request from '@/utils/request'

// ============ 用户管理 ============
export function getUsers(params) {
  return request.get('/system/user/list', { params })
}

export function getUser(id) {
  return request.get(`/system/user/${id}`)
}

export function addUser(data) {
  return request.post('/system/user', data)
}

export function updateUser(data) {
  return request.put('/system/user', data)
}

export function deleteUser(id) {
  return request.delete(`/system/user/${id}`)
}

export function assignRoles(userId, roleIds) {
  return request.put(`/system/user/${userId}/roles`, { roleIds })
}

// ============ 角色管理 ============
export function getRoles(params) {
  return request.get('/system/role/list', { params })
}

export function getAllRoles() {
  return request.get('/system/role/all')
}

export function getRole(id) {
  return request.get(`/system/role/${id}`)
}

export function addRole(data) {
  return request.post('/system/role', data)
}

export function updateRole(data) {
  return request.put('/system/role', data)
}

export function deleteRole(id) {
  return request.delete(`/system/role/${id}`)
}

export function getMenusByRoleId(roleId) {
  return request.get(`/system/role/${roleId}/menus`)
}

export function assignMenus(roleId, menuIds) {
  return request.put(`/system/role/${roleId}/menus`, { menuIds })
}

// ============ 菜单管理 ============
export function getMenuTree() {
  return request.get('/system/menu/tree')
}

export function getMenu(id) {
  return request.get(`/system/menu/${id}`)
}

export function addMenu(data) {
  return request.post('/system/menu', data)
}

export function updateMenu(data) {
  return request.put('/system/menu', data)
}

export function deleteMenu(id) {
  return request.delete(`/system/menu/${id}`)
}

// ============ 字典管理 ============
export function getDictTypes(params) {
  return request.get('/system/dict/type/list', { params })
}

export function getDictDataByType(type) {
  return request.get(`/system/dict/data/type/${type}`)
}

export function addDictType(data) {
  return request.post('/system/dict/type', data)
}

export function updateDictType(data) {
  return request.put('/system/dict/type', data)
}

export function deleteDictType(id) {
  return request.delete(`/system/dict/type/${id}`)
}

export function addDictData(data) {
  return request.post('/system/dict/data', data)
}

export function updateDictData(data) {
  return request.put('/system/dict/data', data)
}

export function deleteDictData(id) {
  return request.delete(`/system/dict/data/${id}`)
}

// ============ 操作日志 ============
export function getLogs(params) {
  return request.get('/system/log/list', { params })
}
