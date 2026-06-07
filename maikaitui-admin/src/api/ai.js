import request from '@/utils/request'

// ==================== Admin AI Chat ====================

/**
 * 管理员 AI 对话（支持工具调用）
 * @param {Object} data - { message: string, conversationId?: string }
 * @returns {Promise} { code, message, data: { reply, conversationId, timestamp, elapsedMs } }
 */
export function adminAiChat(data) {
  return request.post('/admin/ai/chat', data, { timeout: 300000 }) // 5分钟，Tool Calling 多轮调用需较长时间
}

/**
 * 获取 AI 操作日志（分页）
 * @param {Object} params - { page: number, size: number }
 * @returns {Promise} { code, message, data: Page<AdminAiActionLog> }
 */
export function getAiActionLogs(params) {
  return request.get('/admin/ai/actions', { params })
}

/**
 * AI 服务健康检查
 * @returns {Promise} { code, message, data: { status, service, tools, timestamp } }
 */
export function aiHealthCheck() {
  return request.get('/admin/ai/health')
}
