<template>
  <view class="ai-page">
    <view class="ai-hero">
      <view class="hero-content">
        <view class="badge">AI · 智能旅行助手</view>
        <text class="title">迈开腿 AI 助手</text>
        <text class="sub">推荐景点 · 规划行程 · 旅行问答</text>
      </view>
    </view>

    <scroll-view scroll-y class="msgs" :scroll-into-view="scrollId">
      <view v-if="!messages.length" class="welcome glass">
        <image class="w-icon" src="/static/images/ai-robot.png" mode="aspectFit" />
        <text class="w-title">你好，我是你的旅行搭档</text>
        <text class="w-desc">问我目的地、行程、美食住宿，都可以</text>
      </view>
      <view v-for="(m, i) in messages" :key="i" :id="'m' + i" class="msg" :class="m.role">
        <view v-if="m.role === 'bot'" class="avatar bot-av">AI</view>
        <view class="bubble">
          <text class="txt">{{ m.content }}</text>
        </view>
        <view v-if="m.role === 'user'" class="avatar user-av">{{ userInitial }}</view>
      </view>
      <view v-if="typing" class="msg bot">
        <view class="avatar bot-av">AI</view>
        <view class="bubble"><text class="txt dim">正在思考…</text></view>
      </view>
    </scroll-view>

    <view class="chips" v-if="messages.length < 2">
      <view v-for="q in quickQs" :key="q" class="chip" @click="send(q)">{{ q }}</view>
    </view>

    <view class="input-bar">
      <input v-model="input" class="inp" placeholder="输入你的旅行问题…" confirm-type="send" @confirm="send(input)" />
      <button class="send" :disabled="typing || !input.trim()" @click="send(input)">
        <text>发送</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { aiChat } from '@/api/index.js'
import { store } from '@/store/index.js'

const messages = ref([])
const input = ref('')
const typing = ref(false)
const sessionId = ref(null)
const scrollId = ref('')
const quickQs = ['周末去哪玩？', '三天成都行程', '亲子游推荐', '预算2000旅行']

const userInitial = computed(() => {
  const n = store.userInfo?.nickname || store.userInfo?.username || '我'
  return n.charAt(0).toUpperCase()
})

const scrollBottom = async () => {
  await nextTick()
  if (messages.value.length) scrollId.value = 'm' + (messages.value.length - 1)
}

const send = async (text) => {
  const msg = (text || input.value || '').trim()
  if (!msg || typing.value) return
  input.value = ''
  messages.value.push({ role: 'user', content: msg })
  await scrollBottom()
  typing.value = true
  try {
    const data = await aiChat(msg, sessionId.value)
    if (data?.sessionId) sessionId.value = data.sessionId
    messages.value.push({ role: 'bot', content: data?.reply || '暂时无法回答' })
  } catch (e) {
    messages.value.push({ role: 'bot', content: '服务繁忙，请确认后端 AI 模块已启动' })
  } finally {
    typing.value = false
    scrollBottom()
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';

.ai-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: $bg-page;
}

.ai-hero {
  position: relative;
  padding: 34rpx 28rpx 42rpx;
  overflow: hidden;
  background: linear-gradient(180deg, #42ad4e, $primary);
  border-radius: 0 0 34rpx 34rpx;
}
.hero-content { position: relative; z-index: 1; color: #fff; }
.badge {
  display: inline-block;
  background: rgba(255,255,255,0.18);
  border: 1rpx solid rgba(255,255,255,0.25);
  font-size: 22rpx;
  font-weight: 800;
  padding: 6rpx 20rpx;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}
.title { font-size: 40rpx; font-weight: 800; display: block; }
.sub { font-size: 24rpx; opacity: 0.88; margin-top: 8rpx; display: block; }

.msgs {
  flex: 1;
  height: calc(100vh - 380rpx);
  padding: 20rpx 24rpx;
  box-sizing: border-box;
}

.welcome {
  text-align: center;
  padding: 40rpx 32rpx;
  margin-bottom: 24rpx;
  &.glass {
    @include card-glass;
    background: rgba(255,255,255,0.95);
  }
}
.w-icon {
  width: 118rpx;
  height: 118rpx;
  margin: 0 auto 16rpx;
}
.w-title { font-size: 30rpx; font-weight: 700; color: $dark; display: block; }
.w-desc { font-size: 26rpx; color: $text-secondary; margin-top: 8rpx; display: block; }

.msg {
  display: flex;
  align-items: flex-end;
  margin-bottom: 24rpx;
  gap: 12rpx;
  &.user { flex-direction: row-reverse; }
}

.avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
  flex-shrink: 0;
}
.bot-av {
  background: radial-gradient(circle at 35% 24%, #6be7d8, #15343b 62%);
  color: #89fff1;
}
.user-av {
  background: $gradient-brand-accent;
  color: #fff;
}

.bubble {
  max-width: 72%;
  padding: 22rpx 26rpx;
  border-radius: $radius-md;
  background: $bg-card;
  box-shadow: $shadow-card;
}
.msg.user .bubble {
  background: $gradient-brand;
  .txt { color: #fff; }
}
.txt { font-size: 28rpx; line-height: 1.65; color: $text-primary; }
.txt.dim { color: $text-muted; }

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  padding: 0 24rpx 12rpx;
}
.chip {
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  color: $primary;
  background: rgba(31,143,58,0.08);
  border: 1rpx solid rgba(31,143,58,0.2);
}

.input-bar {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: $bg-card;
  border-top: 1rpx solid $bg-secondary;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.04);
}
.inp {
  flex: 1;
  background: $bg-page;
  border-radius: 999rpx;
  padding: 18rpx 28rpx;
  font-size: 28rpx;
  border: 2rpx solid transparent;
  &:focus { border-color: $primary; }
}
.send {
  @include btn-primary;
  padding: 0 36rpx;
  height: 76rpx;
  line-height: 76rpx;
  font-size: 28rpx;
  &[disabled] { opacity: 0.5; box-shadow: none; }
}
</style>
