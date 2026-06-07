<template>
  <view class="page">
    <view v-if="!store.isLoggedIn" class="empty-page">
      <text class="big">💝</text>
      <text class="tip">登录后查看收藏</text>
      <button class="btn" @click="goLogin">去登录</button>
    </view>
    <template v-else>
      <view v-if="loading" class="empty-page"><text class="muted">加载中…</text></view>
      <view v-else-if="!list.length" class="empty-page">
        <text class="big">🌅</text>
        <text class="tip">还没有收藏任何景点</text>
        <button class="btn" @click="goExplore">去发现</button>
      </view>
      <scroll-view v-else scroll-y class="scroll" @scrolltolower="more">
        <view v-for="item in list" :key="item.id" class="row" @click="goDetail(item.attractionId)">
          <image :src="item.coverImage || defaultImg" mode="aspectFill" class="thumb" />
          <view class="body">
            <text class="name">{{ item.name }}</text>
            <text class="score">⭐ {{ item.rating || '4.8' }}</text>
            <text class="price" v-if="item.price > 0">¥{{ item.price }}</text>
            <text class="price free" v-else>免费</text>
          </view>
          <view class="del" @click.stop="remove(item)"><text>🗑</text></view>
        </view>
        <view class="foot"><text class="muted">{{ hasMore ? '加载更多' : '— 到底了 —' }}</text></view>
      </scroll-view>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { store } from '@/store/index.js'
import { getMyFavorites, removeFavorite } from '@/api/index.js'

const loading = ref(true)
const list = ref([])
const page = ref(1)
const hasMore = ref(true)
const defaultImg = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTgwIiBoZWlnaHQ9IjE4MCI+PHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0YwRjJGNyIvPjwvc3ZnPg=='

const load = async (reset = false) => {
  if (reset) { page.value = 1; hasMore.value = true; loading.value = true }
  try {
    const { list: rows, total } = await getMyFavorites({ page: page.value, pageSize: 10 })
    if (reset || page.value === 1) list.value = rows
    else list.value = [...list.value, ...rows]
    hasMore.value = list.value.length < total && rows.length >= 10
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}
const more = () => { if (hasMore.value && !loading.value) { page.value++; load(false) } }
const remove = (item) => {
  uni.showModal({
    title: '取消收藏', content: `确定取消「${item.name}」？`,
    success: async (r) => { if (r.confirm) { await removeFavorite(item.attractionId); load(true) } }
  })
}
const goDetail = (id) => uni.navigateTo({ url: `/pages/attraction/detail?id=${id}` })
const goLogin = () => uni.navigateTo({ url: '/pages/login/index' })
const goExplore = () => uni.switchTab({ url: '/pages/attraction/list' })

onShow(() => { if (store.isLoggedIn) load(true) })
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';
.page { min-height: 100vh; background: $bg-page; }
.empty-page { padding: 160rpx 48rpx; text-align: center; }
.big { font-size: 96rpx; display: block; margin-bottom: 20rpx; }
.tip { color: $text-secondary; font-size: 30rpx; display: block; margin-bottom: 32rpx; }
.muted { color: $text-muted; }
.btn { @include btn-primary; padding: 0 48rpx; height: 80rpx; line-height: 80rpx; font-size: 28rpx; }
.scroll { padding: 20rpx 24rpx; height: calc(100vh - 40rpx); }
.row {
  @include card;
  display: flex;
  align-items: center;
  padding: 16rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid rgba(31,143,58,0.08);
}
.thumb { width: 168rpx; height: 168rpx; border-radius: $radius-sm; flex-shrink: 0; }
.body { flex: 1; padding: 0 20rpx; }
.name { font-size: 30rpx; font-weight: 700; color: $dark; display: block; }
.score { font-size: 24rpx; color: $accent-dark; margin: 8rpx 0; display: block; }
.price { font-size: 34rpx; font-weight: 800; color: $primary; }
.price.free { font-size: 26rpx; color: $secondary; }
.del { padding: 16rpx; font-size: 32rpx; opacity: 0.6; }
.foot { text-align: center; padding: 24rpx; }
</style>
