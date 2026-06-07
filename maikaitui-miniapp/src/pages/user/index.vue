<template>
  <view class="user-page">
    <view v-if="!store.isLoggedIn" class="guest-wrap">
      <view class="guest-hero">
        <view class="gh-bg" />
        <text class="gh-title">欢迎来到迈开腿</text>
        <text class="gh-sub">登录后收藏景点、预订门票、使用 AI 规划</text>
      </view>
      <view class="guest-card">
        <button class="btn-main" @click="goLogin">登录 / 注册</button>
      </view>
      <view class="quick-grid">
        <view class="qg" @click="goTab('/pages/index/index')"><text>🏠</text><text>首页</text></view>
        <view class="qg" @click="goTab('/pages/attraction/list')"><text>🗺️</text><text>探索</text></view>
        <view class="qg" @click="goTab('/pages/ai/index')"><text>✨</text><text>AI</text></view>
      </view>
    </view>

    <template v-else>
      <view class="profile-hero">
        <view class="ph-bg" />
        <view class="ph-body">
          <image class="avatar" :src="store.userInfo?.avatar || defaultAvatar" mode="aspectFill" />
          <view class="pinfo">
            <text class="name">{{ store.userInfo?.nickname || store.userInfo?.username }}</text>
            <text class="phone" v-if="store.userInfo?.phone">{{ store.userInfo.phone }}</text>
          </view>
        </view>
      </view>

      <view class="stats">
        <view class="st" @click="goPage('/pages/favorites/index')">
          <text class="num">{{ stats.favorites }}</text><text class="lbl">收藏</text>
        </view>
        <view class="line" />
        <view class="st" @click="goPage('/pages/orders/index')">
          <text class="num">{{ stats.orders }}</text><text class="lbl">订单</text>
        </view>
        <view class="line" />
        <view class="st">
          <text class="num">{{ stats.comments }}</text><text class="lbl">评论</text>
        </view>
      </view>

      <view class="menu">
        <view class="mi" @click="goPage('/pages/orders/index')">
          <view class="mi-icon o1">📋</view>
          <text class="mi-t">我的订单</text>
          <text class="arr">›</text>
        </view>
        <view class="mi" @click="goPage('/pages/favorites/index')">
          <view class="mi-icon o2">❤️</view>
          <text class="mi-t">我的收藏</text>
          <text class="arr">›</text>
        </view>
        <view class="mi" @click="goTab('/pages/ai/index')">
          <view class="mi-icon o3">🤖</view>
          <text class="mi-t">AI 旅行助手</text>
          <text class="arr">›</text>
        </view>
      </view>

      <button class="logout" @click="logout">退出登录</button>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { store } from '@/store/index.js'
import { getUserStats, getUserInfo } from '@/api/index.js'

const stats = ref({ favorites: 0, orders: 0, comments: 0 })
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCI+PGNpcmNsZSBjeD0iNjAiIGN5PSI2MCIgcj0iNjAiIGZpbGw9IiNlNGU3ZjciLz48L3N2Zz4='

const load = async () => {
  if (!store.isLoggedIn) return
  try {
    const [s, u] = await Promise.all([getUserStats(), getUserInfo()])
    if (s) stats.value = s
    if (u) store.setUserInfo(u)
  } catch (e) { console.error(e) }
}

const goLogin = () => uni.navigateTo({ url: '/pages/login/index' })
const goTab = (url) => uni.switchTab({ url })
const goPage = (url) => uni.navigateTo({ url })
const logout = () => {
  uni.showModal({
    title: '提示', content: '确定退出登录？',
    success: (r) => { if (r.confirm) { store.logout(); stats.value = { favorites: 0, orders: 0, comments: 0 } } }
  })
}

onShow(() => load())
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';

.user-page { min-height: 100vh; background: $bg-page; padding-bottom: 40rpx; }

.guest-wrap { padding-bottom: 32rpx; }
.guest-hero {
  position: relative;
  padding: 80rpx 40rpx 100rpx;
  text-align: center;
  overflow: hidden;
}
.gh-bg {
  position: absolute;
  inset: 0;
  background: $gradient-brand-accent;
  border-radius: 0 0 48rpx 48rpx;
}
.gh-title, .gh-sub { position: relative; z-index: 1; color: #fff; display: block; }
.gh-title { font-size: 40rpx; font-weight: 800; }
.gh-sub { font-size: 26rpx; opacity: 0.9; margin-top: 12rpx; }
.guest-card {
  margin: -48rpx 32rpx 24rpx;
  @include card;
  padding: 40rpx;
  position: relative;
  z-index: 2;
}
.btn-main { @include btn-primary; width: 100%; height: 88rpx; line-height: 88rpx; font-size: 30rpx; }
.quick-grid { display: flex; gap: 16rpx; padding: 0 32rpx; }
.qg {
  flex: 1; @include card; padding: 28rpx; text-align: center;
  text { display: block; &:first-child { font-size: 40rpx; margin-bottom: 8rpx; } &:last-child { font-size: 24rpx; color: $text-secondary; } }
}

.profile-hero { position: relative; }
.ph-bg {
  height: 280rpx;
  background: $gradient-brand-accent;
  border-radius: 0 0 40rpx 40rpx;
}
.ph-body {
  position: absolute;
  bottom: 24rpx;
  left: 32rpx;
  right: 32rpx;
  display: flex;
  align-items: center;
}
.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255,255,255,0.6);
  margin-right: 24rpx;
  box-shadow: $shadow-card;
}
.name { color: #fff; font-size: 38rpx; font-weight: 800; display: block; }
.phone { color: rgba(255,255,255,0.88); font-size: 24rpx; }

.stats {
  display: flex;
  align-items: center;
  margin: 24rpx 24rpx;
  @include card;
  padding: 32rpx 0;
}
.st { flex: 1; text-align: center; }
.num { font-size: 44rpx; font-weight: 800; color: $primary; display: block; }
.lbl { font-size: 24rpx; color: $text-muted; }
.line { width: 1rpx; height: 48rpx; background: $bg-secondary; }

.menu { margin: 0 24rpx; @include card; overflow: hidden; }
.mi {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid $bg-secondary;
  &:last-child { border-bottom: none; }
}
.mi-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  margin-right: 20rpx;
  &.o1 { background: rgba(31,143,58,0.12); }
  &.o2 { background: rgba(250,112,154,0.15); }
  &.o3 { background: rgba(102,126,234,0.15); }
}
.mi-t { flex: 1; font-size: 28rpx; font-weight: 600; color: $dark; }
.arr { color: $text-muted; font-size: 32rpx; }

.logout {
  margin: 40rpx 24rpx 0;
  background: $bg-card;
  color: #DC3545;
  border: 1rpx solid #FFCDD2;
  border-radius: 999rpx;
  font-size: 28rpx;
}
</style>
