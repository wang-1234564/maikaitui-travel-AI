<template>
  <view class="detail">
    <view v-if="loading" class="center"><text class="muted">加载中…</text></view>
    <template v-else-if="attraction">
      <view class="top-actions" :style="{ paddingTop: statusBarHeight + 'px' }">
        <button class="circle-btn" @click="goBack">‹</button>
        <view class="right-actions">
          <button class="glass-btn">♡ 收藏</button>
          <button class="glass-btn">分享</button>
        </view>
      </view>
      <swiper class="gallery" circular indicator-dots indicator-active-color="#1f8f3a">
        <swiper-item v-for="(img, i) in attraction.images" :key="i">
          <image class="gal-img" :src="img" mode="aspectFill" />
        </swiper-item>
      </swiper>

      <view class="main-card">
        <view class="title-row">
          <view>
            <text class="hot">🔥 热门景点</text>
            <text class="title">{{ attraction.name }}</text>
          </view>
          <view class="score">
            <text>{{ attraction.rating.toFixed(1) }}</text>
            <text>★★★★★</text>
          </view>
        </view>
        <view class="tags">
          <text>{{ attraction.categoryName }}</text>
          <text>AAAAA</text>
          <text>{{ attraction.regionName }}</text>
        </view>
        <text class="addr">⌖ {{ attraction.address || attraction.regionName }}</text>
        <text class="desc">{{ attraction.description || '这里拥有舒适的旅行体验与丰富的自然人文景观，适合亲友出游、亲子旅行和周末短途。' }}</text>

        <view class="info-grid">
          <view><text>票价</text><strong>{{ formatPrice(attraction.price) }}</strong></view>
          <view><text>开放时间</text><strong>{{ attraction.openTime || '07:00 - 18:00' }}</strong></view>
          <view><text>浏览量</text><strong>{{ formatCount(attraction.viewCount) }}</strong></view>
          <view><text>经纬度</text><strong>{{ attraction.longitude || '--' }} / {{ attraction.latitude || '--' }}</strong></view>
        </view>

        <view class="thumbs">
          <image v-for="(img, i) in attraction.images.slice(0, 5)" :key="i" :src="img" mode="aspectFill" />
        </view>
      </view>

      <view class="panel" v-if="recommendations.length">
        <view class="section-head">
          <text class="block-title">相关推荐</text>
          <text class="more">查看更多 〉</text>
        </view>
        <scroll-view scroll-x class="rec-scroll" show-scrollbar="false">
          <view v-for="r in recommendations" :key="r.id" class="rec" @click="reload(r.id)">
            <image :src="r.coverImage" mode="aspectFill" />
            <text class="rn">{{ r.name }}</text>
            <text class="rp">{{ formatPrice(r.price) }}</text>
          </view>
        </scroll-view>
      </view>

      <view class="panel">
        <view class="section-head">
          <text class="block-title">游客评价</text>
          <text class="more">{{ comments.length }}条</text>
        </view>
        <view v-if="comments.length">
          <view v-for="c in comments" :key="c.id" class="comment">
            <view class="avatar">{{ (c.username || c.userName || '游')[0] }}</view>
            <view>
              <text class="c-user">{{ c.username || c.userName || '游客' }}</text>
              <text class="c-body">{{ c.content }}</text>
            </view>
          </view>
        </view>
        <text v-else class="muted">暂无评价，成为第一个分享体验的人吧。</text>
      </view>

      <view class="bottom-bar">
        <view>
          <text class="lbl">参考价</text>
          <text class="val">{{ formatPrice(attraction.price) }}</text>
        </view>
        <button @click="openBook">立即预订</button>
      </view>
    </template>

    <view v-if="bookShow" class="mask" @click="bookShow = false">
      <view class="book-sheet" @click.stop>
        <text class="sheet-title">填写订单</text>
        <text class="sheet-sub">{{ attraction?.name }}</text>
        <view class="field"><text>游玩日期</text><picker mode="date" :value="book.visitDate" @change="e => book.visitDate = e.detail.value"><view class="picker-val">{{ book.visitDate || '选择日期' }}</view></picker></view>
        <view class="field"><text>数量</text><input type="number" v-model.number="book.quantity" /></view>
        <view class="field"><text>联系人</text><input v-model="book.contactName" placeholder="姓名" /></view>
        <view class="field"><text>手机</text><input v-model="book.contactPhone" type="number" maxlength="11" placeholder="手机号" /></view>
        <button class="submit" :disabled="submitting" @click="submitOrder">{{ submitting ? '提交中…' : '确认预订' }}</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getAttractionById, getRecommendations, getComments, createOrder } from '@/api/index.js'
import { store } from '@/store/index.js'
import { fallbackAttractions, formatCount, formatPrice, normalizeAttraction, normalizeList } from '@/utils/travel.js'

const loading = ref(true)
const statusBarHeight = ref(20)
const attraction = ref(null)
const comments = ref([])
const recommendations = ref([])
const bookShow = ref(false)
const submitting = ref(false)
const attractionId = ref('')
const book = ref({ visitDate: '', quantity: 1, contactName: '', contactPhone: '' })

try {
  statusBarHeight.value = uni.getWindowInfo?.()?.statusBarHeight || uni.getSystemInfoSync().statusBarHeight || 20
} catch {
  statusBarHeight.value = 20
}

const requireLogin = () => {
  if (store.isLoggedIn) return true
  uni.showToast({ title: '请先登录', icon: 'none' })
  setTimeout(() => uni.navigateTo({ url: '/pages/login/index' }), 700)
  return false
}

const load = async (id) => {
  if (!id) return
  attractionId.value = id
  loading.value = true
  try {
    const [detail, rec, commentData] = await Promise.all([
      getAttractionById(id),
      getRecommendations(id, 6).catch(() => []),
      getComments(id, { pageSize: 5 }).catch(() => ({ list: [] }))
    ])
    attraction.value = normalizeAttraction(detail)
    recommendations.value = normalizeList(rec, []).slice(0, 6)
    comments.value = commentData?.list || []
  } catch {
    attraction.value = normalizeAttraction({ ...fallbackAttractions[0], id })
    recommendations.value = normalizeList(fallbackAttractions.slice(1), []).slice(0, 5)
    comments.value = []
  } finally {
    loading.value = false
  }
}

const openBook = () => { if (requireLogin()) bookShow.value = true }

const submitOrder = async () => {
  const b = book.value
  if (!b.visitDate || !b.contactName || !/^1[3-9]\d{9}$/.test(b.contactPhone)) {
    uni.showToast({ title: '请完善订单信息', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const qty = Math.max(1, b.quantity || 1)
    await createOrder({
      attractionId: +attractionId.value,
      attractionName: attraction.value.name,
      quantity: qty,
      totalPrice: (attraction.value.price || 0) * qty,
      visitDate: b.visitDate,
      contactName: b.contactName,
      contactPhone: b.contactPhone
    })
    uni.showToast({ title: '预订成功', icon: 'success' })
    bookShow.value = false
    uni.navigateTo({ url: '/pages/orders/index' })
  } finally {
    submitting.value = false
  }
}

const reload = (id) => uni.redirectTo({ url: `/pages/attraction/detail?id=${id}` })
const goBack = () => {
  const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
  if (pages.length > 1) uni.navigateBack({ delta: 1 })
  else uni.switchTab({ url: '/pages/attraction/list' })
}

onLoad((opts) => load(opts?.id))
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';

.detail {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 148rpx;
  position: relative;
}

.center {
  padding: 220rpx 0;
  text-align: center;
}

.muted {
  color: $text-muted;
  font-size: 26rpx;
}

.gallery {
  height: 500rpx;
}

.gal-img {
  width: 100%;
  height: 100%;
}

.top-actions {
  position: absolute;
  left: 24rpx;
  right: 24rpx;
  top: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.circle-btn,
.glass-btn {
  color: #fff;
  background: rgba(20, 37, 27, 0.34);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(12rpx);
}

.circle-btn {
  width: 66rpx;
  height: 66rpx;
  line-height: 60rpx;
  border-radius: 50%;
  font-size: 50rpx;
  padding: 0;
}

.right-actions {
  display: flex;
  gap: 14rpx;
}

.glass-btn {
  height: 62rpx;
  line-height: 62rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 800;
  padding: 0 20rpx;
}

.main-card,
.panel {
  margin: -56rpx 28rpx 0;
  padding: 30rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: $shadow-card-hover;
  position: relative;
  z-index: 2;
}

.panel {
  margin-top: 24rpx;
  box-shadow: $shadow-card;
}

.title-row {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}

.hot {
  display: inline-block;
  color: #b96a00;
  background: rgba(245,184,75,0.16);
  border-radius: 999rpx;
  padding: 6rpx 16rpx;
  font-size: 22rpx;
  font-weight: 800;
}

.title {
  display: block;
  color: $dark;
  font-size: 40rpx;
  font-weight: 900;
  margin-top: 12rpx;
  line-height: 1.3;
}

.score {
  width: 136rpx;
  min-height: 112rpx;
  border: 1rpx solid rgba(31,143,58,0.12);
  border-radius: 18rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  text:first-child {
    color: $primary;
    font-size: 42rpx;
    font-weight: 900;
  }

  text:last-child {
    color: $accent;
    font-size: 18rpx;
    margin-top: 6rpx;
  }
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin: 18rpx 0;

  text {
    color: $primary-dark;
    background: rgba(31,143,58,0.1);
    border-radius: 999rpx;
    padding: 6rpx 16rpx;
    font-size: 22rpx;
  }
}

.addr,
.desc {
  display: block;
}

.addr {
  color: $text-secondary;
  font-size: 25rpx;
  margin-bottom: 18rpx;
}

.desc {
  color: $text-primary;
  font-size: 27rpx;
  line-height: 1.75;
}

.info-grid {
  margin-top: 24rpx;
  border: 1rpx solid rgba(31,143,58,0.1);
  border-radius: 18rpx;
  overflow: hidden;
  display: grid;
  grid-template-columns: repeat(2, 1fr);

  view {
    min-height: 92rpx;
    padding: 18rpx;
    border-right: 1rpx solid rgba(31,143,58,0.08);
    border-bottom: 1rpx solid rgba(31,143,58,0.08);
  }

  text {
    display: block;
    color: $text-muted;
    font-size: 22rpx;
    margin-bottom: 6rpx;
  }

  strong {
    color: $dark;
    font-size: 25rpx;
  }
}

.thumbs {
  display: flex;
  gap: 12rpx;
  margin-top: 24rpx;

  image {
    width: 118rpx;
    height: 78rpx;
    border-radius: 12rpx;
  }
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18rpx;
}

.block-title {
  color: $dark;
  font-size: 31rpx;
  font-weight: 900;
}

.more {
  color: $text-muted;
  font-size: 24rpx;
}

.rec-scroll {
  white-space: nowrap;
}

.rec {
  display: inline-block;
  width: 212rpx;
  margin-right: 16rpx;
  vertical-align: top;

  image {
    width: 212rpx;
    height: 132rpx;
    border-radius: 14rpx;
  }
}

.rn,
.rp {
  display: block;
}

.rn {
  color: $dark;
  font-size: 24rpx;
  font-weight: 800;
  margin-top: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rp {
  color: $primary;
  font-size: 24rpx;
  font-weight: 900;
  margin-top: 4rpx;
}

.comment {
  display: grid;
  grid-template-columns: 58rpx 1fr;
  gap: 14rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid $bg-secondary;
}

.avatar {
  width: 58rpx;
  height: 58rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $secondary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
}

.c-user,
.c-body {
  display: block;
}

.c-user {
  color: $dark;
  font-size: 26rpx;
  font-weight: 800;
}

.c-body {
  color: $text-secondary;
  font-size: 25rpx;
  margin-top: 6rpx;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  background: #fff;
  padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -10rpx 34rpx rgba(26,64,38,0.1);

  button {
    height: 84rpx;
    line-height: 84rpx;
    border-radius: 999rpx;
    background: linear-gradient(135deg, $primary, $primary-dark);
    color: #fff;
    font-size: 30rpx;
    font-weight: 900;
    padding: 0 58rpx;
  }
}

.lbl,
.val {
  display: block;
}

.lbl {
  color: $text-muted;
  font-size: 22rpx;
}

.val {
  color: $primary;
  font-size: 40rpx;
  font-weight: 900;
}

.mask {
  position: fixed;
  inset: 0;
  background: rgba(20,37,27,0.55);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.book-sheet {
  width: 100%;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  padding: 34rpx 32rpx calc(34rpx + env(safe-area-inset-bottom));
}

.sheet-title,
.sheet-sub {
  display: block;
}

.sheet-title {
  color: $dark;
  font-size: 34rpx;
  font-weight: 900;
}

.sheet-sub {
  color: $text-secondary;
  font-size: 25rpx;
  margin-top: 6rpx;
  margin-bottom: 24rpx;
}

.field {
  display: flex;
  align-items: center;
  margin-bottom: 18rpx;

  > text {
    width: 154rpx;
    color: $text-secondary;
    font-size: 27rpx;
  }

  input,
  .picker-val {
    flex: 1;
    background: $bg-page;
    border-radius: 14rpx;
    padding: 18rpx 22rpx;
    font-size: 27rpx;
  }
}

.submit {
  margin-top: 12rpx;
  height: 90rpx;
  line-height: 90rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, $primary, $primary-dark);
  color: #fff;
  font-size: 31rpx;
  font-weight: 900;
}
</style>
