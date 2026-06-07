<template>
  <!-- 折叠按钮 -->
  <button v-if="!panelOpen" class="fab-btn" @click="openPanel" title="AI 旅行助手">
    <svg width="26" height="26" viewBox="0 0 48 48" fill="none">
      <circle cx="24" cy="24" r="22" fill="url(#fabGrad)"/>
      <path d="M12 31c4-10 8-15 12-15s8 5 12 15" stroke="white" stroke-width="3" fill="none" stroke-linecap="round"/>
      <path d="M17 29l5-8 5 8" stroke="white" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
      <defs><linearGradient id="fabGrad" x1="0" y1="0" x2="48" y2="48"><stop offset="0%" stop-color="#31A84A"/><stop offset="100%" stop-color="#126C2A"/></linearGradient></defs>
    </svg>
  </button>

  <Teleport to="body">
    <Transition name="panel-slide">
      <div v-if="panelOpen" class="chat-panel-wrapper">
        <div class="chat-overlay" @click="closePanel"></div>
        <div class="chat-panel">
          <!-- ==== 头部 ==== -->
          <div class="panel-header">
            <div class="header-left">
              <div class="panel-avatar"><img src="/images/ai-robot.png" alt="AI" /></div>
              <div><strong>迈开腿 AI助手</strong><span>在线 · 随时为你解答</span></div>
            </div>
            <div class="header-actions">
              <button class="header-btn" :class="{ active: showHistory }" @click="showHistory = !showHistory" title="历史记录">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              </button>
              <button class="header-btn" @click="newSession" title="新建对话">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              </button>
              <button class="header-btn" @click="closePanel" title="折叠">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
              </button>
            </div>
          </div>

          <!-- ==== 主体：历史面板 + 对话区 ==== -->
          <div class="panel-body">
            <!-- 历史面板 -->
            <Transition name="history-slide">
              <div v-if="showHistory" class="history-panel">
                <div class="history-title">对话历史</div>
                <div class="history-list">
                  <button
                    v-for="s in sessions"
                    :key="s.id"
                    class="history-item"
                    :class="{ active: s.id === currentSessionId }"
                    @click="switchSession(s)"
                  >
                    <span class="hi-icon">⌁</span>
                    <span class="hi-meta">
                      <strong>{{ s.title || '新对话' }}</strong>
                      <small>{{ s.summary || '暂无摘要' }}</small>
                    </span>
                    <span class="hi-del" @click.stop="handleDeleteSession(s.id)" title="删除">×</span>
                  </button>
                  <div v-if="sessions.length === 0" class="history-empty">暂无历史对话</div>
                </div>
              </div>
            </Transition>

            <!-- 消息区 -->
            <div class="panel-messages" ref="msgContainer">
              <div v-if="messages.length === 0" class="welcome-block">
                <div class="welcome-icon"><img src="/images/ai-robot.png" alt="AI" /></div>
                <h3>Hi，有什么可以帮你？</h3>
                <p>我是你的智能旅行助手</p>
                <div class="quick-prompts">
                  <button v-for="q in quickPrompts" :key="q" @click="sendPreset(q)">{{ q }}</button>
                </div>
              </div>

              <!-- msg.role === 'user' 或 'bot' -->
              <div v-for="(msg, idx) in messages" :key="idx" class="chat-msg" :class="msg.role">
                <template v-if="msg.role === 'bot'">
                  <div class="msg-avatar bot-av">AI</div>
                  <div class="msg-bubble markdown" v-html="renderMd(msg.content)"></div>
                </template>
                <template v-else>
                  <div class="msg-bubble user-bubble">{{ msg.content }}</div>
                  <div class="msg-avatar user-av">{{ userInitial }}</div>
                </template>
              </div>

              <!-- AI 思考中动画 -->
              <div v-if="isTyping" class="chat-msg bot">
                <div class="msg-avatar bot-av">AI</div>
                <div class="msg-bubble thinking-block">
                  <div class="thinking-dots">
                    <span></span><span></span><span></span>
                  </div>
                  <small>AI 正在思考...</small>
                </div>
              </div>
            </div>
          </div>

          <!-- ==== 输入区 ==== -->
          <div class="panel-input">
            <input v-model="inputText" type="text" placeholder="输入你的问题..."
              @keyup.enter="sendMessage" :disabled="isTyping" />
            <button @click="sendMessage" :disabled="isTyping || !inputText.trim()">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M2 21 23 12 2 3v7l15 2-15 2v7Z"/></svg>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useAiChatStore } from '@/stores/aiChat'
import { chat, getSessions, getChatHistory, deleteSession } from '@/api/index.js'
import { marked } from 'marked'

const userStore = useUserStore()
const { panelOpen, context, open: openPanel, close: closePanel, clearContext } = useAiChatStore()

const inputText = ref('')
const isTyping = ref(false)
const messages = ref([])
const currentSessionId = ref(null)
const msgContainer = ref(null)
const showHistory = ref(false)
const sessions = ref([])

const quickPrompts = [
  '推荐适合夏天的避暑胜地',
  '3天2晚的成都旅游攻略',
  '带父母去旅行去哪合适？',
  '推荐几个小众旅行地'
]

const userInitial = computed(() =>
  (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase()
)

function renderMd(text) { return text ? marked.parse(text, { breaks: true }) : '' }

function scrollToBottom() {
  nextTick(() => { const el = msgContainer.value; if (el) el.scrollTop = el.scrollHeight })
}

function sendPreset(text) { inputText.value = text; sendMessage() }

function newSession() {
  currentSessionId.value = null
  messages.value = []
  showHistory.value = false
}

async function switchSession(s) {
  if (s.id === currentSessionId.value) { showHistory.value = false; return }
  currentSessionId.value = s.id
  showHistory.value = false
  try {
    const list = await getChatHistory(s.id)
    messages.value = (list || []).map(m => ({
      role: m.role === 'assistant' ? 'bot' : 'user',
      content: m.content
    }))
  } catch { messages.value = [] }
  scrollToBottom()
}

async function handleDeleteSession(id) {
  try { await deleteSession(id) } catch { /* ignore */ }
  if (currentSessionId.value === id) newSession()
  await loadSessions()
}

async function loadSessions() {
  try { sessions.value = (await getSessions()) || [] }
  catch { sessions.value = [] }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return

  // 首次消息带上下文，发送后清除
  const ctx = context.value?.name
    ? `景区：${context.value.name}，票价：¥${context.value.price || 0}，评分：${context.value.rating || 0}，简介：${context.value.desc || ''}`
    : null

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()
  isTyping.value = true

  try {
    const res = await chat(text, currentSessionId.value, ctx)
    clearContext() // 仅首次消息带上下文
    messages.value.push({ role: 'bot', content: res?.reply || '抱歉，请稍后重试' })
    if (res?.sessionId && !currentSessionId.value) {
      currentSessionId.value = res.sessionId
      await loadSessions()
    }
  } catch {
    messages.value.push({ role: 'bot', content: 'AI 服务暂不可用，请稍后重试' })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

watch(panelOpen, (val) => { if (val) { scrollToBottom(); loadSessions() } })
</script>

<style lang="scss" scoped>
/* ====== 浮动按钮 ====== */
.fab-btn {
  position: fixed; right: 24px; bottom: 80px; z-index: 1500;
  width: 56px; height: 56px; border-radius: 50%; background: #fff;
  box-shadow: 0 6px 24px rgba(31,143,58,0.25);
  display: flex; align-items: center; justify-content: center;
  animation: fabPulse 2.5s infinite;
  &:hover { transform: scale(1.1); box-shadow: 0 8px 32px rgba(31,143,58,0.35); }
}
@keyframes fabPulse {
  0%,100% { box-shadow: 0 6px 24px rgba(31,143,58,0.25); }
  50% { box-shadow: 0 6px 36px rgba(31,143,58,0.45); }
}

/* ====== 面板 ====== */
.chat-panel-wrapper { position: fixed; inset: 0; z-index: 2000; display: flex; }
.chat-overlay { flex: 1; background: rgba(0,0,0,0.2); }
.chat-panel {
  width: 420px; max-width: 92vw; height: 100vh; height: 100dvh;
  background: #fff; display: flex; flex-direction: column;
  box-shadow: -8px 0 40px rgba(0,0,0,0.12);
}
.panel-slide-enter-active,.panel-slide-leave-active { transition: all 0.25s ease; }
.panel-slide-enter-from,.panel-slide-leave-to { .chat-panel { transform: translateX(100%); } .chat-overlay { opacity: 0; } }

/* ====== 头部 ====== */
.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; border-bottom: 1px solid #eef2ed; flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 10px;
  strong { display: block; font-size: 0.95rem; color: var(--dark); }
  span { font-size: 0.72rem; color: var(--primary); }
}
.panel-avatar { width: 38px; height: 38px; border-radius: 50%; overflow: hidden; background: linear-gradient(135deg,#e8f5e9,#c8e6c9);
  img { width: 100%; height: 100%; object-fit: contain; }
}
.header-actions { display: flex; gap: 2px; }
.header-btn {
  width: 34px; height: 34px; border-radius: 8px; display: flex; align-items: center; justify-content: center;
  color: var(--text-light);
  &:hover { background: #f0f4ef; color: var(--text); }
  &.active { background: rgba(31,143,58,0.1); color: var(--primary-dark); }
}

/* ====== 主体 ====== */
.panel-body { flex: 1; display: flex; overflow: hidden; min-height: 0; }

/* 历史面板 */
.history-panel {
  width: 100%; border-right: 1px solid #eef2ed; display: flex; flex-direction: column; flex-shrink: 0;
  background: #fafcfa;
}
.history-title { font-size: 0.85rem; font-weight: 700; padding: 14px 16px; color: var(--dark); border-bottom: 1px solid #eef2ed; }
.history-list { flex: 1; overflow-y: auto; padding: 8px; }
.history-item {
  width: 100%; display: flex; gap: 8px; align-items: start; padding: 10px; border-radius: 8px; text-align: left; margin-bottom: 4px;
  &:hover,&.active { background: rgba(31,143,58,0.06); }
}
.hi-icon { width: 26px; height: 26px; border-radius: 50%; background: rgba(31,143,58,0.1); color: var(--primary); display: flex; align-items: center; justify-content: center; flex-shrink: 0; font-size: 0.8rem; }
.hi-meta { min-width: 0;
  strong { display: block; font-size: 0.82rem; color: var(--dark); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  small { font-size: 0.7rem; color: var(--text-light); }
}
.hi-del {
  visibility: hidden; flex-shrink: 0; width: 22px; height: 22px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%; font-size: 1rem; color: var(--text-lighter);
  &:hover { background: rgba(220,53,69,0.1); color: #DC3545; }
}
.history-item:hover .hi-del { visibility: visible; }
.history-empty { text-align: center; color: var(--text-lighter); padding: 24px 0; font-size: 0.85rem; }
.history-slide-enter-active,.history-slide-leave-active { transition: all 0.2s ease; }
.history-slide-enter-from,.history-slide-leave-to { opacity: 0; transform: translateX(-10px); }

/* 消息区 */
.panel-messages { flex: 1; overflow-y: auto; padding: 16px; }
.welcome-block { text-align: center; padding: 30px 10px;
  h3 { font-size: 1.05rem; color: var(--dark); margin: 12px 0 4px; }
  p { color: var(--text-light); font-size: 0.85rem; margin-bottom: 18px; }
  .welcome-icon { width: 56px; height: 56px; margin: 0 auto; img { width: 100%; height: 100%; object-fit: contain; } }
}
.quick-prompts { display: grid; grid-template-columns: 1fr 1fr; gap: 8px;
  button { min-height: 36px; border-radius: 8px; font-size: 0.8rem; font-weight: 500; background: #f5f8f3; color: var(--text); border: 1px solid rgba(31,143,58,0.08);
    &:hover { border-color: rgba(31,143,58,0.25); color: var(--primary-dark); }
  }
}

/* 消息行 */
.chat-msg {
  display: flex; gap: 8px; margin-bottom: 14px;
  &.user { justify-content: flex-end; }
  &.bot { align-items: flex-start; }   /* AI 头像顶部对齐 */
}
.msg-avatar {
  width: 28px; height: 28px; border-radius: 50%; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 0.6rem; font-weight: 800;
  &.bot-av { background: linear-gradient(135deg,#e8f5e9,#c8e6c9); color: var(--primary-dark); }
  &.user-av { background: linear-gradient(135deg,var(--primary),var(--accent)); color: #fff; }
}
.msg-bubble {
  max-width: 78%; padding: 10px 13px; border-radius: 10px;
  font-size: 0.88rem; line-height: 1.6; word-break: break-word;
  background: #f4f8f2; color: var(--text);
  :deep(p) { margin: 0 0 4px; }
  :deep(ul),:deep(ol) { padding-left: 16px; margin: 4px 0; }
  :deep(strong) { color: var(--primary-dark); }
}
.user-bubble { background: linear-gradient(135deg,var(--primary),var(--primary-dark)); color: #fff; }

/* AI 思考中动画 */
.thinking-block {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 16px 20px; min-width: 120px;
  small { font-size: 0.72rem; color: var(--text-lighter); }
}
.thinking-dots {
  display: flex; gap: 6px;
  span {
    width: 8px; height: 8px; border-radius: 50%; background: var(--primary);
    animation: thinkBounce 1.4s infinite ease-in-out;
    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}
@keyframes thinkBounce {
  0%,80%,100% { transform: scale(0.6); opacity: 0.3; }
  40% { transform: scale(1.2); opacity: 1; }
}

/* ====== 输入区 ====== */
.panel-input { display: flex; gap: 8px; padding: 12px 14px; border-top: 1px solid #eef2ed; flex-shrink: 0;
  input { flex: 1; height: 40px; padding: 0 14px; border-radius: 999px; border: 1px solid rgba(31,143,58,0.15); font-size: 0.88rem; color: var(--text);
    &:focus { border-color: var(--primary); box-shadow: 0 0 0 2px rgba(31,143,58,0.08); }
    &:disabled { background: #f8faf7; }
  }
  button { width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0; background: linear-gradient(135deg,var(--primary),var(--primary-dark)); color: #fff; display: flex; align-items: center; justify-content: center;
    &:disabled { opacity: 0.4; }
  }
}
.panel-messages::-webkit-scrollbar { width: 4px; }
.panel-messages::-webkit-scrollbar-thumb { background: rgba(31,143,58,0.14); border-radius: 2px; }
.history-list::-webkit-scrollbar { width: 3px; }
.history-list::-webkit-scrollbar-thumb { background: rgba(31,143,58,0.1); border-radius: 2px; }

@media (max-width: 480px) { .chat-panel { width: 100vw; max-width: 100vw; } .fab-btn { right: 14px; bottom: 60px; } }
</style>
