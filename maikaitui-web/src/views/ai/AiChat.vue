<template>
  <div class="ai-chat-page">
    <aside class="chat-sidebar">
      <div class="assistant-brand">
        <div class="bot-mark">
          <img src="/images/ai-robot.png" alt="" />
        </div>
        <div>
          <strong>迈开腿 AI助手</strong>
          <span>智能旅行助手，带你玩转世界</span>
        </div>
      </div>

      <button class="new-chat-btn" @click="newSession">+ 新建对话</button>

      <div class="session-tabs">
        <button class="active">全部对话</button>
        <button>收藏</button>
      </div>

      <div class="session-list">
        <button
          v-for="s in displaySessions"
          :key="s.id"
          class="session-item"
          :class="{ active: s.id === currentSessionId }"
          @click="switchSession(s)"
        >
          <span class="session-icon">⌁</span>
          <span class="session-meta">
            <span class="session-title-row">
              <strong>{{ s.title || '旅行咨询' }}</strong>
              <time>{{ s.updateTime || s.createTime || '刚刚' }}</time>
            </span>
            <small>{{ s.summary || '我会结合季节、交通和预算帮你细化路线。' }}</small>
          </span>
          <span v-if="s.starred" class="session-star">★</span>
          <span v-else-if="!s.mock" class="delete-session" @click.stop="handleDeleteSession(s.id)">×</span>
          <span v-else></span>
        </button>
      </div>

      <button class="clear-btn" @click="handleClearHistory">清空历史记录</button>
    </aside>

    <main class="chat-main">
      <button class="back-btn" @click="$router.push('/')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M15 18l-6-6 6-6"/>
        </svg>
        <span>返回</span>
      </button>
      <div class="welcome" v-if="messages.length === 0">
        <div class="hero-bot">
          <img src="/images/ai-robot.png" alt="迈开腿 AI 助手" />
        </div>
        <h1>Hi，我是迈开腿AI助手</h1>
        <p>有什么旅行问题都可以问我哦</p>
        <div class="prompt-grid">
          <button v-for="item in presets" :key="item" @click="sendPreset(item)">
            {{ item }}
          </button>
        </div>
      </div>

      <div class="chat-messages" ref="chatMessages" v-else>
        <div v-for="(msg, idx) in messages" :key="idx" class="message" :class="msg.role">
          <div class="avatar" v-if="msg.role === 'bot'">AI</div>
          <div class="bubble" v-if="msg.role === 'user'">{{ msg.content }}</div>
          <div class="bubble markdown" v-else v-html="formatText(msg.content)"></div>
          <div class="avatar user" v-if="msg.role === 'user'">{{ userInitial }}</div>
        </div>
        <div v-if="isTyping" class="message bot">
          <div class="avatar">AI</div>
          <div class="bubble typing"><i></i><i></i><i></i></div>
        </div>
      </div>

      <div class="quick-actions">
        <button v-for="item in quickActions" :key="item" @click="sendPreset(item)">{{ item }}</button>
      </div>

      <div class="chat-input-area">
        <input
          v-model="inputText"
          type="text"
          placeholder="请输入你的问题..."
          @keyup.enter="sendMessage"
        />
        <button :disabled="isTyping || !inputText.trim()" @click="sendMessage">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="M2 21 23 12 2 3v7l15 2-15 2v7Z"/>
          </svg>
        </button>
      </div>
      <p class="ai-disclaimer">内容由AI生成，仅供参考</p>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { chat, getSessions, getChatHistory, deleteSession, clearChatHistory } from '@/api/index.js'
import { marked } from 'marked'

const userStore = useUserStore()

const chatMessages = ref(null)
const inputText = ref('')
const isTyping = ref(false)
const messages = ref([])
const sessions = ref([])
const currentSessionId = ref(null)

const presets = [
  '推荐适合夏天的避暑胜地',
  '3天2晚的成都旅游攻略',
  '带父母去旅行，去哪比较合适？',
  '小众但美丽的旅行地推荐'
]

const quickActions = ['帮我规划行程', '当地美食推荐', '交通方式', '住宿建议']

const sampleSessions = [
  { id: 'sample-1', title: '杭州3日游攻略', summary: '好的，杭州是一个非常适合...', updateTime: '今天 10:30', starred: true, mock: true },
  { id: 'sample-2', title: '张家界景点推荐', summary: '张家界国家森林公园是必去的...', updateTime: '昨天 16:45', mock: true },
  { id: 'sample-3', title: '带孩子去哪玩', summary: '带孩子出游可以考虑自然风光...', updateTime: '昨日 09:20', mock: true },
  { id: 'sample-4', title: '桂林自由行路线', summary: '桂林山水甲天下，推荐您...', updateTime: '05-06 14:22', mock: true },
  { id: 'sample-5', title: '云南大理避坑建议', summary: '大理是一个慢生活的好地方...', updateTime: '05-05 11:18', mock: true },
  { id: 'sample-6', title: '国内小众海岛推荐', summary: '推荐几个小众又美丽的海岛...', updateTime: '05-04 20:31', mock: true }
]

const displaySessions = computed(() => sessions.value.length ? sessions.value : sampleSessions)

const userInitial = computed(() => (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase())

function sendPreset(text) {
  inputText.value = text
  sendMessage()
}

async function sendMessage() {
  if (!inputText.value.trim() || isTyping.value) return
  const userMsg = inputText.value.trim()
  messages.value.push({ role: 'user', content: userMsg })
  inputText.value = ''
  scrollToBottom()
  isTyping.value = true

  try {
    const res = await chat(userMsg, currentSessionId.value)
    messages.value.push({ role: 'bot', content: res?.reply || '' })
    if (res?.sessionId && !currentSessionId.value) {
      currentSessionId.value = res.sessionId
      await loadSessions()
    }
  } catch {
    messages.value.push({
      role: 'bot',
      content: 'AI 服务暂时不可用，请确认后端网关和 maikaitui-ai 微服务已经启动。'
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

function newSession() {
  currentSessionId.value = null
  messages.value = []
}

async function switchSession(s) {
  currentSessionId.value = s.id
  if (s.mock) {
    messages.value = [
      { role: 'user', content: s.title },
      {
        role: 'bot',
        content: '当然可以。我会结合季节、交通、景区热度和预算，帮你拆成可执行的路线。你也可以继续告诉我出发城市、天数和同行人。'
      }
    ]
    scrollToBottom()
    return
  }
  try {
    const list = await getChatHistory(s.id)
    messages.value = (list || []).map((m) => ({
      role: m.role === 'assistant' ? 'bot' : 'user',
      content: m.content
    }))
  } catch {
    messages.value = []
  }
  scrollToBottom()
}

async function handleDeleteSession(id) {
  try {
    await deleteSession(id)
  } catch {
    // ignore
  }
  if (currentSessionId.value === id) newSession()
  await loadSessions()
}

async function handleClearHistory() {
  try {
    await clearChatHistory()
  } catch {
    // ignore
  }
  newSession()
  sessions.value = []
}

async function loadSessions() {
  try {
    sessions.value = (await getSessions()) || []
  } catch {
    sessions.value = []
  }
}

function formatText(text) {
  return text ? marked.parse(text, { breaks: true }) : ''
}

function scrollToBottom() {
  nextTick(() => {
    if (chatMessages.value) chatMessages.value.scrollTop = chatMessages.value.scrollHeight
  })
}

onMounted(loadSessions)
</script>

<style lang="scss" scoped>
.ai-chat-page {
  height: calc(100vh - 28px);
  display: grid;
  grid-template-columns: 268px minmax(0, 1fr);
  background: #ffffff;
  max-width: none;
  margin: 0;
  border: 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: none;
}

.chat-sidebar {
  border-right: 1px solid rgba(31, 143, 58, 0.1);
  background: linear-gradient(180deg, #fbfdf9, #ffffff);
  padding: 22px 16px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 28px);
  overflow: hidden;
}

.assistant-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;

  strong {
    display: block;
    color: var(--dark);
    font-size: 1.04rem;
  }

  span {
    display: block;
    color: var(--text-light);
    font-size: 0.78rem;
    margin-top: 2px;
  }
}

.bot-mark,
.avatar {
  background: radial-gradient(circle at 35% 24%, #6be7d8, #15343b 62%);
  color: #89fff1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
}

.bot-mark {
  width: 44px;
  height: 44px;
  border-radius: 50%;

  img {
    width: 48px;
    height: 48px;
    object-fit: contain;
  }
}

.new-chat-btn {
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;
  font-weight: 800;
  margin-bottom: 18px;
}

.session-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;

  button {
    flex: 1;
    height: 34px;
    border-radius: 8px;
    color: var(--text-light);
    font-weight: 700;

    &.active {
      color: var(--primary-dark);
      background: rgba(31, 143, 58, 0.09);
    }
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  width: 100%;
  display: grid;
  grid-template-columns: 30px 1fr 18px;
  gap: 8px;
  align-items: start;
  padding: 11px 8px;
  border-radius: 8px;
  text-align: left;
  color: var(--text);
  margin-bottom: 8px;

  &:hover,
  &.active {
    background: rgba(31, 143, 58, 0.075);
    box-shadow: inset 0 0 0 1px rgba(31, 143, 58, 0.08);
  }
}

.session-icon {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(31, 143, 58, 0.11);
  color: var(--primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.session-meta {
  min-width: 0;

  small {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.session-title-row {
  display: flex;
  align-items: center;
  gap: 8px;

  strong {
    min-width: 0;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 0.88rem;
  }

  time {
    color: var(--text-lighter);
    font-size: 0.68rem;
    flex-shrink: 0;
  }
}

.session-meta {
  small {
    color: var(--text-light);
    font-size: 0.72rem;
    margin-top: 3px;
  }
}

.delete-session,
.session-star {
  color: var(--text-lighter);
  font-size: 1.2rem;
}

.session-star {
  color: var(--accent);
  font-size: 0.9rem;
  line-height: 28px;
}

.empty-history {
  color: var(--text-lighter);
  text-align: center;
  padding: 24px 0;
  font-size: 0.86rem;
}

.clear-btn {
  height: 40px;
  border-radius: 8px;
  background: #f5f7f4;
  color: var(--text-light);
  font-weight: 700;
}

.back-btn {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  height: 36px;
  padding: 0 14px;
  border-radius: 8px;
  color: var(--text-light);
  background: #f5f7f4;
  font-weight: 600;
  z-index: 10;

  &:hover {
    color: var(--primary-dark);
    background: rgba(31, 143, 58, 0.08);
  }
}

.chat-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  height: calc(100vh - 28px);
  padding: 24px 34px 0;
  overflow: hidden;
}

.welcome {
  width: min(760px, 100%);
  text-align: center;
  margin: auto 0 28px;

  h1 {
    color: var(--dark);
    font-size: 1.45rem;
    margin: 18px 0 6px;
  }

  p {
    color: var(--text-light);
    margin-bottom: 30px;
  }
}

.hero-bot {
  width: 116px;
  height: 116px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

.prompt-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;

  button {
    min-height: 42px;
    border-radius: 8px;
    border: 1px solid rgba(31, 143, 58, 0.12);
    background: #fff;
    color: var(--text);
    font-weight: 600;

    &:hover {
      color: var(--primary-dark);
      border-color: rgba(31, 143, 58, 0.28);
    }
  }
}

.chat-messages {
  width: min(760px, 100%);
  flex: 1 1 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 4px 18px;
  min-height: 0;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
  align-items: flex-end;

  &.user {
    justify-content: flex-end;
  }
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  flex: 0 0 auto;
  font-size: 0.72rem;

  &.user {
    background: linear-gradient(135deg, var(--primary), var(--accent));
    color: #fff;
  }
}

.bubble {
  max-width: 72%;
  padding: 15px 18px;
  border-radius: 8px;
  line-height: 1.75;
  background: linear-gradient(180deg, #f6fbf5, #eef7ee);
  color: var(--text);
  box-shadow: 0 8px 24px rgba(26, 64, 38, 0.06);
}

.message.user .bubble {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;
}

.markdown {
  :deep(p) {
    margin: 0 0 8px;
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 20px;
    margin: 8px 0;
  }

  :deep(strong) {
    color: var(--primary-dark);
  }
}

.typing {
  display: inline-flex;
  gap: 5px;

  i {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: var(--primary);
    animation: typing 1.1s infinite ease-in-out;

    &:nth-child(2) { animation-delay: 0.15s; }
    &:nth-child(3) { animation-delay: 0.3s; }
  }
}

@keyframes typing {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.35; }
  40% { transform: translateY(-5px); opacity: 1; }
}

.quick-actions {
  width: min(760px, 100%);
  display: flex;
  gap: 14px;
  justify-content: center;
  flex-wrap: wrap;
  padding: 12px 0 10px;
  flex-shrink: 0;

  button {
    min-height: 38px;
    padding: 0 20px;
    border-radius: 8px;
    color: var(--text-light);
    background: #f6f8f4;
    font-weight: 700;
  }
}

.chat-input-area {
  width: min(760px, 100%);
  display: grid;
  grid-template-columns: 1fr 48px;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  border: 1px solid rgba(31, 143, 58, 0.22);
  background: #fff;
  box-shadow: 0 12px 34px rgba(26, 64, 38, 0.08);
  flex-shrink: 0;

  input {
    height: 40px;
    padding: 0 12px;
    color: var(--text);
  }

  button {
    border-radius: 8px;
    background: linear-gradient(135deg, var(--primary), var(--primary-dark));
    color: #fff;

    &:disabled {
      opacity: 0.45;
      cursor: not-allowed;
    }
  }
}

.ai-disclaimer {
  color: var(--text-lighter);
  font-size: 0.78rem;
  margin-top: 8px;
  padding-bottom: 8px;
  flex-shrink: 0;
}

/* 滚动条美化 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}
.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}
.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(31, 143, 58, 0.18);
  border-radius: 3px;
}
.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(31, 143, 58, 0.32);
}

/* 侧边栏滚动条 */
.session-list::-webkit-scrollbar {
  width: 4px;
}
.session-list::-webkit-scrollbar-thumb {
  background: rgba(31, 143, 58, 0.14);
  border-radius: 2px;
}

@media (max-width: 900px) {
  .ai-chat-page {
    grid-template-columns: 1fr;
  }

  .chat-sidebar {
    height: auto;
    border-right: 0;
    border-bottom: 1px solid rgba(31, 143, 58, 0.1);
  }

  .session-list {
    max-height: 180px;
  }

  .chat-main {
    height: auto;
    min-height: calc(100vh - 28px - 200px);
  }

  .prompt-grid {
    grid-template-columns: 1fr;
  }

  .bubble {
    max-width: 86%;
  }
}
</style>
