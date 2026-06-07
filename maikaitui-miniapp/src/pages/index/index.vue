<template>
  <view class="home">
    <view class="top-band">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }" />
      <view class="nav-row">
        <text class="nav-title">迈开腿</text>
        <view class="capsule"><text>•••</text><text>◎</text></view>
      </view>
      <view class="search" @click="goSearch">
        <text class="search-icon">⌕</text>
        <text>搜索景区/景点/攻略</text>
        <button>搜索</button>
      </view>

      <view class="hero-card">
        <image class="hero-img" :src="heroImage" mode="aspectFill" @error="handleHeroError" />
        <view class="hero-copy">
          <text class="hero-title">迈开腿</text>
          <text class="hero-sub">去热爱这个世界</text>
          <text class="hero-pill">探索自然 · 遇见美好</text>
        </view>
      </view>
    </view>

    <view class="shortcut-grid">
      <view v-for="item in shortcuts" :key="item.name" class="shortcut" @click="item.action">
        <view
          class="shortcut-icon"
          :class="[
            item.tone,
            'icon-' + (item.iconKind || 'mountain'),
            { 'has-image': item.iconImage && !shortcutIconErrors[item.name] }
          ]"
        >
          <image
            v-if="item.iconImage && !shortcutIconErrors[item.name]"
            class="shortcut-img"
            :src="item.iconImage"
            mode="aspectFit"
            @error="handleShortcutIconError(item.name)"
          />
          <text v-else-if="item.iconKind === 'ai'" class="shortcut-ai">AI</text>
        </view>
        <text>{{ item.name }}</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-title">热门景区</text>
        <text class="more" @click="goAll">查看更多 〉</text>
      </view>
      <scroll-view scroll-x class="hot-scroll" show-scrollbar="false">
        <view v-for="item in hotList" :key="item.id" class="hot-card" @click="goDetail(item.id)">
          <image :src="item.coverImage" mode="aspectFill" />
          <view class="hot-body">
            <text class="hot-name">{{ item.name }}</text>
            <text class="hot-region">{{ item.regionName }}</text>
            <view class="hot-meta">
              <text>★ {{ item.rating.toFixed(1) }}</text>
              <text>{{ formatPrice(item.price) }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="ai-card">
      <view>
        <text class="ai-title">AI 旅行助手</text>
        <text class="ai-desc">你好，我是你的旅行小助手，有什么可以帮你的吗？</text>
      </view>
      <button @click="goAI">去对话</button>
      <image class="bot-face" src="/static/images/ai-robot.png" mode="aspectFit" />
    </view>

    <view class="section guide-section">
      <view class="section-head">
        <text class="section-title">精选攻略</text>
        <text class="more">查看更多 〉</text>
      </view>
      <view class="guide-card" @click="goAI">
        <image :src="guideImage" mode="aspectFill" @error="handleGuideError" />
        <view>
          <text class="guide-title">三天两晚自然风光路线</text>
          <text class="guide-sub">AI 为你组合景点、交通与住宿建议</text>
        </view>
      </view>
    </view>

    <view class="safe" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getHomeData } from '@/api/index.js'
import { fallbackAttractions, fallbackCategories, formatPrice, normalizeList } from '@/utils/travel.js'

const statusBarHeight = ref(20)
try {
  statusBarHeight.value = uni.getWindowInfo?.()?.statusBarHeight || uni.getSystemInfoSync().statusBarHeight || 20
} catch {
  statusBarHeight.value = 20
}

const hotList = ref(normalizeList([], fallbackAttractions))
const shortcutIconErrors = ref({})
const fallbackHeroImage = 'https://picsum.photos/seed/mini-hero/900/420'
const fallbackGuideImage = 'https://picsum.photos/seed/mini-guide/640/360'
const heroImage = ref('/static/images/mini-home-banner.png')
const guideImage = ref('/static/images/mini-guide-card.png')

const goSearch = () => uni.switchTab({ url: '/pages/attraction/list' })
const goAll = () => uni.switchTab({ url: '/pages/attraction/list' })
const goAI = () => uni.switchTab({ url: '/pages/ai/index' })
const goDetail = (id) => uni.navigateTo({ url: `/pages/attraction/detail?id=${id}` })
const goCategory = (item) => {
  uni.setStorageSync('pendingAttractionFilter', {
    categoryId: item.id,
    categoryName: item.name
  })
  uni.switchTab({ url: '/pages/attraction/list' })
}
const handleHeroError = () => {
  heroImage.value = fallbackHeroImage
}
const handleGuideError = () => {
  guideImage.value = fallbackGuideImage
}
const handleShortcutIconError = (name) => {
  shortcutIconErrors.value = { ...shortcutIconErrors.value, [name]: true }
}

const shortcuts = ref(fallbackCategories.map((item, index) => ({
  ...item,
  tone: `tone-${index % 4}`,
  action: () => {
    if (item.name === 'AI对话') goAI()
    else if (item.name === '找攻略') goAI()
    else if (item.name === '全部分类') goAll()
    else goCategory(item)
  }
})))

const loadHome = async () => {
  try {
    const data = await getHomeData()
    hotList.value = normalizeList(data?.hotAttractions, fallbackAttractions).slice(0, 6)
    const cats = data?.categories?.length ? data.categories : fallbackCategories
    shortcuts.value = fallbackCategories.map((fallback, index) => {
      const matched = cats[index] || fallback
      return {
        ...fallback,
        id: matched.id || fallback.id,
        name: fallback.name,
        iconKind: fallback.iconKind,
        iconImage: fallback.iconImage,
        tone: `tone-${index % 4}`,
        action: () => {
          if (fallback.name === 'AI对话' || fallback.name === '找攻略') goAI()
          else if (fallback.name === '全部分类') goAll()
          else goCategory({ id: matched.id || fallback.id, name: matched.name || fallback.name })
        }
      }
    })
  } catch {
    hotList.value = normalizeList([], fallbackAttractions)
  }
}

onLoad(loadHome)
onPullDownRefresh(() => loadHome().finally(() => uni.stopPullDownRefresh()))
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';

.home {
  min-height: 100vh;
  background: $bg-page;
}

.top-band {
  background: linear-gradient(180deg, #42ad4e 0%, $primary 58%, #2fa443 100%);
  padding: 0 28rpx 86rpx;
  border-radius: 0 0 34rpx 34rpx;
}

.status-bar {
  width: 100%;
}

.nav-row {
  height: 86rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.nav-title {
  color: #fff;
  font-size: 34rpx;
  font-weight: 800;
}

.capsule {
  position: absolute;
  right: 0;
  top: 18rpx;
  height: 52rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
  color: #fff;
  background: rgba(255,255,255,0.16);
  border: 1rpx solid rgba(255,255,255,0.22);
}

.search {
  height: 74rpx;
  background: #fff;
  border-radius: 999rpx;
  display: grid;
  grid-template-columns: 42rpx 1fr 96rpx;
  align-items: center;
  padding: 0 12rpx 0 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(18, 108, 42, 0.16);
  color: $text-muted;
  font-size: 26rpx;

  button {
    height: 54rpx;
    line-height: 54rpx;
    border-radius: 999rpx;
    background: transparent;
    color: $primary-dark;
    font-size: 26rpx;
    font-weight: 800;
    padding: 0;
  }
}

.search-icon {
  color: $text-muted;
  font-size: 34rpx;
}

.hero-card {
  position: relative;
  margin: 24rpx 0 -54rpx;
  height: 274rpx;
  border-radius: 26rpx;
  overflow: hidden;
  box-shadow: $shadow-card-hover;
  background:
    linear-gradient(135deg, #dff5d3, #f7fff4 48%, #cdeefe);
}

.hero-img {
  width: 100%;
  height: 100%;
}

.hero-copy {
  position: absolute;
  inset: 0;
  padding: 34rpx 38rpx;
  background: linear-gradient(90deg, rgba(238,250,232,0.94), rgba(238,250,232,0.42), rgba(255,255,255,0));
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-title {
  font-family: 'KaiTi', serif;
  color: $primary-dark;
  font-size: 54rpx;
  font-weight: 800;
}

.hero-sub {
  color: $primary-dark;
  font-size: 34rpx;
  font-weight: 800;
  margin-top: 2rpx;
}

.hero-pill {
  margin-top: 18rpx;
  align-self: flex-start;
  color: #fff;
  background: linear-gradient(135deg, $primary, $primary-dark);
  border-radius: 999rpx;
  padding: 8rpx 18rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.shortcut-grid {
  margin: 28rpx 28rpx 0;
  padding: 22rpx 6rpx 20rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: $shadow-card;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  row-gap: 18rpx;
}

.shortcut {
  text-align: center;
  font-size: 24rpx;
  color: $dark;
}

.shortcut-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 22rpx;
  margin: 0 auto 8rpx;
  position: relative;
  overflow: hidden;
  color: $primary-dark;
  background: rgba(31,143,58,0.12);

  &::before,
  &::after {
    content: '';
    position: absolute;
    box-sizing: border-box;
  }

  &.has-image {
    background: transparent;
  }

  &.has-image::before,
  &.has-image::after {
    display: none;
  }
}

.shortcut-img {
  width: 64rpx;
  height: 64rpx;
  display: block;
}

.tone-1 { background: rgba(245,184,75,0.18); color: $accent-dark; }
.tone-2 { background: rgba(31,181,167,0.14); color: $secondary-dark; }
.tone-3 { background: rgba(31,143,58,0.18); color: $primary-dark; }

.shortcut-ai {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24rpx;
  font-weight: 900;
  z-index: 1;
}

.icon-mountain::before {
  left: 13rpx;
  bottom: 15rpx;
  border-left: 16rpx solid transparent;
  border-right: 16rpx solid transparent;
  border-bottom: 30rpx solid currentColor;
}

.icon-mountain::after {
  right: 11rpx;
  bottom: 15rpx;
  border-left: 13rpx solid transparent;
  border-right: 13rpx solid transparent;
  border-bottom: 24rpx solid currentColor;
  opacity: 0.64;
}

.icon-pin::before {
  left: 17rpx;
  top: 10rpx;
  width: 30rpx;
  height: 30rpx;
  border: 7rpx solid currentColor;
  border-radius: 50% 50% 50% 10rpx;
  transform: rotate(-45deg);
}

.icon-pin::after {
  left: 28rpx;
  top: 22rpx;
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: currentColor;
}

.icon-guide::before {
  left: 16rpx;
  top: 15rpx;
  width: 34rpx;
  height: 38rpx;
  border: 6rpx solid currentColor;
  border-radius: 8rpx;
}

.icon-guide::after {
  left: 28rpx;
  top: 25rpx;
  width: 22rpx;
  height: 5rpx;
  border-radius: 999rpx;
  background: currentColor;
  box-shadow: 0 12rpx 0 currentColor;
}

.icon-ai {
  background: radial-gradient(circle at 32% 22%, #67ead8, $secondary-dark 64%);
}

.icon-ai::before {
  left: 29rpx;
  top: 6rpx;
  width: 6rpx;
  height: 12rpx;
  border-radius: 999rpx;
  background: #75f4df;
}

.icon-ai::after {
  left: 24rpx;
  top: 6rpx;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #75f4df;
  opacity: 0.95;
}

.icon-nature::before {
  left: 18rpx;
  top: 12rpx;
  width: 30rpx;
  height: 34rpx;
  border-radius: 50% 50% 44% 44%;
  background: currentColor;
}

.icon-nature::after {
  left: 30rpx;
  top: 40rpx;
  width: 6rpx;
  height: 16rpx;
  border-radius: 999rpx;
  background: currentColor;
}

.icon-culture::before {
  left: 12rpx;
  top: 21rpx;
  width: 40rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: currentColor;
  box-shadow: 0 24rpx 0 currentColor;
}

.icon-culture::after {
  left: 18rpx;
  top: 32rpx;
  width: 6rpx;
  height: 20rpx;
  border-radius: 999rpx;
  background: currentColor;
  box-shadow: 11rpx 0 0 currentColor, 22rpx 0 0 currentColor;
}

.icon-city::before {
  left: 14rpx;
  bottom: 13rpx;
  width: 13rpx;
  height: 32rpx;
  border-radius: 4rpx 4rpx 0 0;
  background: currentColor;
  box-shadow: 18rpx -10rpx 0 currentColor, 36rpx -2rpx 0 currentColor;
}

.icon-city::after {
  left: 18rpx;
  bottom: 24rpx;
  width: 5rpx;
  height: 5rpx;
  border-radius: 2rpx;
  background: rgba(255,255,255,0.72);
  box-shadow: 18rpx -7rpx 0 rgba(255,255,255,0.72), 36rpx 1rpx 0 rgba(255,255,255,0.72);
}

.icon-grid::before {
  left: 16rpx;
  top: 16rpx;
  width: 13rpx;
  height: 13rpx;
  border-radius: 5rpx;
  background: currentColor;
  box-shadow: 20rpx 0 0 currentColor, 0 20rpx 0 currentColor, 20rpx 20rpx 0 currentColor;
}

.section {
  margin-top: 30rpx;
  padding: 0 28rpx;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.section-title {
  color: $dark;
  font-size: 32rpx;
  font-weight: 800;
}

.more {
  color: $text-muted;
  font-size: 24rpx;
}

.hot-scroll {
  white-space: nowrap;
}

.hot-card {
  display: inline-block;
  width: 214rpx;
  margin-right: 18rpx;
  border-radius: 18rpx;
  overflow: hidden;
  background: #fff;
  box-shadow: $shadow-card;
  vertical-align: top;

  image {
    width: 214rpx;
    height: 138rpx;
  }
}

.hot-body {
  padding: 14rpx 14rpx 16rpx;
}

.hot-name {
  display: block;
  color: $dark;
  font-size: 25rpx;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-region {
  display: block;
  color: $text-secondary;
  font-size: 21rpx;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-meta {
  margin-top: 10rpx;
  display: flex;
  justify-content: space-between;
  color: $primary;
  font-size: 22rpx;
  font-weight: 800;
}

.ai-card {
  margin: 30rpx 28rpx;
  min-height: 120rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #f2fbf1, #e3f5f1);
  box-shadow: $shadow-card;
  display: grid;
  grid-template-columns: 1fr 112rpx 88rpx;
  align-items: center;
  gap: 8rpx;
  padding: 18rpx 18rpx 18rpx 26rpx;

  button {
    height: 54rpx;
    line-height: 54rpx;
    border-radius: 999rpx;
    background: linear-gradient(135deg, $primary, $primary-dark);
    color: #fff;
    font-size: 24rpx;
    font-weight: 800;
    padding: 0;
  }
}

.ai-title,
.ai-desc {
  display: block;
}

.ai-title {
  color: $primary-dark;
  font-size: 28rpx;
  font-weight: 900;
}

.ai-desc {
  color: $text-secondary;
  font-size: 23rpx;
  margin-top: 6rpx;
}

.bot-face {
  width: 86rpx;
  height: 86rpx;
}

.guide-card {
  border-radius: 20rpx;
  overflow: hidden;
  background: #fff;
  box-shadow: $shadow-card;
  display: grid;
  grid-template-columns: 192rpx 1fr;
  gap: 18rpx;
  padding: 16rpx;

  image {
    width: 192rpx;
    height: 122rpx;
    border-radius: 14rpx;
  }
}

.guide-title,
.guide-sub {
  display: block;
}

.guide-title {
  color: $dark;
  font-size: 28rpx;
  font-weight: 800;
  margin-top: 12rpx;
}

.guide-sub {
  color: $text-secondary;
  font-size: 23rpx;
  margin-top: 8rpx;
}

.safe {
  height: 40rpx;
}
</style>
