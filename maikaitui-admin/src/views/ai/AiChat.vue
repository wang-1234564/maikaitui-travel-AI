<template>
  <div class="ai-console">
    <!-- 顶部：AI 能力卡片 -->
    <div class="capability-cards">
      <div class="cap-card" v-for="cap in capabilities" :key="cap.key">
        <div class="cap-icon" :style="{ background: cap.color }">{{ cap.icon }}</div>
        <div class="cap-info">
          <span class="cap-name">{{ cap.name }}</span>
          <span class="cap-desc">{{ cap.desc }}</span>
        </div>
      </div>
    </div>

    <!-- 主体：双栏布局 -->
    <div class="console-main">
      <!-- 左栏：对话区 -->
      <div class="chat-panel">
        <div class="chat-header">
          <div class="chat-title">
            <el-icon :size="18"><ChatDotRound /></el-icon>
            <span>AI 管理助手</span>
          </div>
          <el-tag size="small" type="success" v-if="aiOnline">在线</el-tag>
          <el-tag size="small" type="danger" v-else>离线</el-tag>
        </div>

        <div class="chat-messages" ref="messagesRef">
          <!-- 欢迎提示 -->
          <div v-if="messages.length === 0" class="welcome-area">
            <div class="welcome-icon">🤖</div>
            <h3>迈开腿管理AI助手</h3>
            <p>我可以帮助您执行以下管理任务：</p>
            <ul>
              <li>📝 <b>评论审核</b> — 分析评论内容，识别违规信息</li>
              <li>🔍 <b>敏感词检测</b> — 搜索包含特定关键字的评论</li>
              <li>🗑️ <b>评论管理</b> — 批量通过、拒绝或删除违规评论</li>
              <li>📊 <b>数据统计</b> — 查询评论审核统计数据</li>
            </ul>
            <p class="welcome-hint">试试下方的快捷指令，或直接输入您的需求</p>
          </div>

          <!-- 消息列表 -->
          <div v-for="(msg, i) in messages" :key="i"
               :class="['chat-msg', msg.role === 'user' ? 'msg-user' : 'msg-ai']">
            <div class="msg-avatar">
              <el-avatar :size="32"
                         :style="msg.role === 'user'
                           ? 'background:linear-gradient(135deg,#43e97b,#38f9d7)'
                           : 'background:linear-gradient(135deg,#667eea,#764ba2)'">
                {{ msg.role === 'user' ? '管' : 'AI' }}
              </el-avatar>
            </div>
            <div class="msg-body">
              <div class="msg-content" v-html="renderMarkdown(msg.content)"></div>
              <div class="msg-meta">
                <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
                <span v-if="msg.elapsedMs" class="msg-elapsed">{{ msg.elapsedMs }}ms</span>
              </div>
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="chat-msg msg-ai">
            <div class="msg-avatar">
              <el-avatar :size="32" style="background:linear-gradient(135deg,#667eea,#764ba2)">AI</el-avatar>
            </div>
            <div class="msg-body">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷指令 -->
        <div class="quick-actions">
          <el-button v-for="qa in quickActions" :key="qa.key"
                     size="small" round :type="qa.type || ''"
                     @click="sendQuickAction(qa.text)"
                     :disabled="loading">
            {{ qa.label }}
          </el-button>
        </div>

        <!-- 输入区 -->
        <div class="chat-input">
          <el-input
            v-model="inputText"
            placeholder="输入管理指令，如「审核最近10条评论」..."
            :disabled="loading"
            :rows="2"
            type="textarea"
            resize="none"
            @keydown.enter.exact.prevent="sendMessage"
          >
          </el-input>
          <el-button type="primary" :loading="loading" @click="sendMessage"
                     :disabled="!inputText.trim()">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>

      <!-- 右栏：操作日志 -->
      <div class="log-panel">
        <div class="log-header">
          <div class="log-title">
            <el-icon :size="16"><Clock /></el-icon>
            <span>AI 操作记录</span>
          </div>
          <el-button size="small" text @click="fetchActionLogs" :loading="logLoading">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>

        <div class="log-list" v-loading="logLoading">
          <div v-if="actionLogs.length === 0 && !logLoading" class="log-empty">
            <el-icon :size="32" color="#c0c4cc"><Folder /></el-icon>
            <p>暂无操作记录</p>
            <p class="hint">AI 执行工具操作后将在此展示</p>
          </div>

          <div v-for="log in actionLogs" :key="log.id" class="log-item">
            <div class="log-icon" :class="actionIconClass(log.actionType)">
              {{ actionIcon(log.actionType) }}
            </div>
            <div class="log-body">
              <div class="log-desc">{{ log.actionDesc }}</div>
              <div class="log-detail">
                <span v-if="log.targetType && log.targetId">
                  {{ log.targetType }} #{{ log.targetId }}
                </span>
                <span class="log-time">{{ formatTime(log.createTime) }}</span>
              </div>
              <div v-if="log.aiResponse" class="log-response">{{ log.aiResponse }}</div>
            </div>
            <div class="log-status">
              <el-tag :type="log.success === 1 ? 'success' : 'danger'" size="small">
                {{ log.success === 1 ? '成功' : '失败' }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="log-pagination" v-if="logTotal > logPageSize">
          <el-pagination
            v-model:current-page="logPage"
            :page-size="logPageSize"
            :total="logTotal"
            small
            layout="prev, next"
            @current-change="fetchActionLogs"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, Promotion, Clock, Refresh, Folder
} from '@element-plus/icons-vue'
import { adminAiChat, getAiActionLogs, aiHealthCheck } from '@/api/ai'

// ==================== AI 能力定义（仅展示，实际通过对话调用） ====================
const capabilities = [
  { key: 'audit', icon: '🛡️', name: '评论审核', desc: '分析评论内容，识别违规信息并标记处理', color: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { key: 'search', icon: '🔍', name: '敏感词检测', desc: '搜索包含特定关键字的评论内容', color: 'linear-gradient(135deg, #f093fb, #f5576c)' },
  { key: 'stats', icon: '📊', name: '数据统计', desc: '查询评论审核统计数据概览', color: 'linear-gradient(135deg, #43e97b, #38f9d7)' },
  { key: 'delete', icon: '🗑️', name: '评论删除', desc: '对严重违规评论执行永久删除', color: 'linear-gradient(135deg, #fa709a, #fee140)' },
  { key: 'attraction', icon: '🏔️', name: '景区录入', desc: 'AI 根据知识库自动生成景区数据并写入数据库', color: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
  { key: 'batch', icon: '📦', name: '批量导入', desc: '一次性批量添加多个景区的完整数据', color: 'linear-gradient(135deg, #a18cd1, #fbc2eb)' }
]

// ==================== 快捷指令 ====================
const quickActions = [
  { key: 'audit', label: '审核最新评论', text: '请获取最近10条待审核评论，逐条分析并处理' },
  { key: 'stats', label: '查看评论统计', text: '查询当前评论统计数据' },
  { key: 'search-ad', label: '搜索广告评论', text: '搜索包含"加微信"、"优惠"、"免费"等广告嫌疑关键词的评论' },
  { key: 'add-attraction', label: '添加景点', text: '帮我添加张家界的5个核心景点到系统中，先查可用的地区和分类再添加' },
  { key: 'batch-beijing', label: '录入北京景区', text: '把北京最著名的5个旅游景点依次添加到系统，包括故宫、八达岭长城、天坛、颐和园、圆明园' }
]

// ==================== 对话状态 ====================
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const conversationId = ref(null)
const aiOnline = ref(false)
const messagesRef = ref(null)

// ==================== 操作日志 ====================
const actionLogs = ref([])
const logLoading = ref(false)
const logPage = ref(1)
const logPageSize = ref(20)
const logTotal = ref(0)

// ==================== 方法 ====================

/** 发送消息 */
async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text, timestamp: new Date().toISOString() })
  inputText.value = ''
  loading.value = true

  try {
    const res = await adminAiChat({
      message: text,
      conversationId: conversationId.value
    })
    const data = res.data || res
    conversationId.value = data.conversationId
    messages.value.push({
      role: 'assistant',
      content: data.reply,
      timestamp: data.timestamp,
      elapsedMs: data.elapsedMs
    })
    // AI 可能执行了工具操作，刷新日志
    fetchActionLogs()
  } catch (e) {
    ElMessage.error('AI 服务请求失败，请检查服务状态')
    messages.value.push({
      role: 'assistant',
      content: '⚠️ 请求失败，请稍后重试。如果持续失败，请检查 AI 服务是否正常运行。',
      timestamp: new Date().toISOString()
    })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

/** 快捷指令 */
function sendQuickAction(text) {
  inputText.value = text
  sendMessage()
}

/** 获取操作日志 */
async function fetchActionLogs() {
  logLoading.value = true
  try {
    const res = await getAiActionLogs({ page: logPage.value, size: logPageSize.value })
    const data = res.data || res
    if (data && data.records) {
      actionLogs.value = data.records
      logTotal.value = data.total || 0
    }
  } catch (e) {
    // 静默失败
  } finally {
    logLoading.value = false
  }
}

/** 健康检查 */
async function checkHealth() {
  try {
    const res = await aiHealthCheck()
    aiOnline.value = !!(res.data || res).status
  } catch {
    aiOnline.value = false
  }
}

/** 操作日志图标 */
function actionIcon(type) {
  const map = {
    comment_query: '🔍', comment_approve: '✅', comment_reject: '❌',
    comment_delete: '🗑️', comment_stats: '📊'
  }
  return map[type] || '🔧'
}

function actionIconClass(type) {
  if (type === 'comment_delete') return 'icon-danger'
  if (type === 'comment_reject') return 'icon-warn'
  if (type === 'comment_approve') return 'icon-success'
  return ''
}

/** 简单 Markdown 渲染 */
function renderMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>')
    .replace(/### (.*?)(\n|$)/g, '<h4>$1</h4>')
    .replace(/## (.*?)(\n|$)/g, '<h3>$1</h3>')
    .replace(/- (.*?)(\n|$)/g, '<li>$1</li>')
    .replace(/\n/g, '<br>')
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

onMounted(() => {
  checkHealth()
  fetchActionLogs()
})
</script>

<style scoped>
.ai-console {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  gap: 16px;
}

/* ========== 能力卡片 ========== */
.capability-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.cap-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  gap: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.cap-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 12px rgba(0,0,0,0.08);
}

.cap-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  color: #fff;
}

.cap-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.cap-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.cap-desc {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== 主布局 ========== */
.console-main {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
}

/* ========== 对话面板 ========== */
.chat-panel {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #f8f9fb;
}

.chat-msg {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.msg-user {
  flex-direction: row-reverse;
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-body {
  max-width: 70%;
}

.msg-content {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  color: #303133;
  word-break: break-word;
}

.msg-user .msg-content {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-top-right-radius: 4px;
}

.msg-ai .msg-content {
  background: #fff;
  border: 1px solid #ebeef5;
  border-top-left-radius: 4px;
}

.msg-content :deep(h3), .msg-content :deep(h4) {
  margin: 6px 0;
  font-size: 15px;
}

.msg-content :deep(li) {
  margin: 2px 0 2px 16px;
}

.msg-content :deep(b) {
  color: #667eea;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding: 0 4px;
}

.msg-time {
  font-size: 11px;
  color: #909399;
}

.msg-elapsed {
  font-size: 10px;
  color: #c0c4cc;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  border-top-left-radius: 4px;
}

.typing-indicator span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* 欢迎区域 */
.welcome-area {
  text-align: center;
  padding: 40px 20px;
}

.welcome-icon {
  font-size: 56px;
  margin-bottom: 12px;
}

.welcome-area h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 12px;
}

.welcome-area p {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}

.welcome-area ul {
  text-align: left;
  max-width: 380px;
  margin: 12px auto;
  list-style: none;
  padding: 0;
}

.welcome-area li {
  padding: 6px 0;
  font-size: 13px;
  color: #606266;
}

.welcome-hint {
  margin-top: 16px !important;
  color: #c0c4cc !important;
  font-size: 12px !important;
}

/* 快捷指令 */
.quick-actions {
  display: flex;
  gap: 8px;
  padding: 8px 20px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

/* 输入区 */
.chat-input {
  display: flex;
  gap: 10px;
  padding: 12px 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.chat-input :deep(.el-textarea__inner) {
  resize: none;
}

/* ========== 操作日志面板 ========== */
.log-panel {
  width: 380px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.log-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #ebeef5;
}

.log-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.log-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;
}

.log-empty {
  text-align: center;
  padding: 60px 20px;
  color: #c0c4cc;
}

.log-empty p {
  margin: 8px 0 0;
  font-size: 13px;
}

.log-empty .hint {
  font-size: 12px;
  color: #e0e0e0;
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 6px;
  transition: background 0.15s;
}

.log-item:hover {
  background: #f8f9fb;
}

.log-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.log-icon.icon-danger { background: rgba(245, 108, 108, 0.12); }
.log-icon.icon-warn { background: rgba(230, 162, 60, 0.12); }
.log-icon.icon-success { background: rgba(103, 194, 58, 0.12); }

.log-body {
  flex: 1;
  min-width: 0;
}

.log-desc {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.log-detail {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
  font-size: 11px;
  color: #909399;
}

.log-time {
  color: #c0c4cc;
}

.log-response {
  margin-top: 4px;
  padding: 6px 8px;
  background: #f8f9fb;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  word-break: break-all;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.log-status {
  flex-shrink: 0;
}

.log-pagination {
  padding: 8px 12px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #ebeef5;
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .capability-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .log-panel {
    width: 300px;
  }
}

@media (max-width: 900px) {
  .console-main {
    flex-direction: column;
  }
  .log-panel {
    width: 100%;
    max-height: 300px;
  }
}
</style>
