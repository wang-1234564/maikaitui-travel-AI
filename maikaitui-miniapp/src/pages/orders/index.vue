<template>
  <view class="page">
    <view v-if="!store.isLoggedIn" class="empty-page">
      <text class="big">🔐</text>
      <text class="tip">登录后查看订单</text>
      <button class="btn" @click="goLogin">去登录</button>
    </view>
    <template v-else>
      <view class="tabs">
        <text v-for="(t, i) in tabLabels" :key="t" :class="{ on: tab === i }" @click="tab = i">{{ t }}</text>
      </view>

      <view v-if="loading" class="empty-page"><text class="muted">加载中…</text></view>
      <view v-else-if="!filtered.length" class="empty-page">
        <text class="big">📋</text>
        <text class="tip">暂无订单</text>
        <button class="btn" @click="goExplore">去预订</button>
      </view>
      <scroll-view v-else scroll-y class="scroll" @scrolltolower="more">
        <view v-for="o in filtered" :key="o.id" class="card">
          <view class="head">
            <text class="no">{{ o.orderNo || o.id }}</text>
            <text class="st" :class="norm(o.orderStatus)">{{ statusLabel(norm(o.orderStatus)) }}</text>
          </view>
          <view class="body" @click="goDetail(o.attractionId)">
            <text class="name">{{ o.attractionName || '景点门票' }}</text>
            <text class="sub">游玩 {{ o.visitDate || '-' }} · {{ o.quantity || 1 }} 张</text>
          </view>
          <view class="foot">
            <text class="total">合计 <text class="amt">¥{{ o.totalPrice || 0 }}</text></text>
            <view class="acts">
              <button v-if="norm(o.orderStatus) === 'pending'" class="ghost" @click="cancel(o)">取消</button>
              <button v-if="norm(o.orderStatus) === 'pending'" class="primary" @click="pay(o)">去支付</button>
            </view>
          </view>
        </view>
        <view class="end"><text class="muted">{{ hasMore ? '加载更多' : '— 到底了 —' }}</text></view>
      </scroll-view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { store } from '@/store/index.js'
import { getMyOrders, cancelOrder, payOrder } from '@/api/index.js'

const loading = ref(true)
const orders = ref([])
const page = ref(1)
const hasMore = ref(true)
const tab = ref(0)
const tabLabels = ['全部', '待支付', '已支付', '已完成', '已取消']

const norm = (s) => (s || '').toLowerCase()
const statusLabel = (s) => ({ pending: '待支付', paid: '已支付', completed: '已完成', cancelled: '已取消' }[s] || s)

const filtered = computed(() => {
  const map = ['', 'pending', 'paid', 'completed', 'cancelled']
  const key = map[tab.value]
  if (!key) return orders.value
  return orders.value.filter((o) => norm(o.orderStatus) === key)
})

const load = async (reset = false) => {
  if (reset) { page.value = 1; hasMore.value = true; loading.value = true }
  try {
    const { list: rows, total } = await getMyOrders({ page: page.value, pageSize: 10 })
    if (reset || page.value === 1) orders.value = rows
    else orders.value = [...orders.value, ...rows]
    hasMore.value = orders.value.length < total && rows.length >= 10
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const more = () => { if (hasMore.value && !loading.value) { page.value++; load(false) } }
const cancel = (o) => {
  uni.showModal({
    title: '取消订单', content: '确定取消？',
    success: async (r) => { if (r.confirm) { await cancelOrder(o.id); load(true) } }
  })
}
const pay = async (o) => {
  try { await payOrder(o.id); uni.showToast({ title: '支付成功', icon: 'success' }); load(true) }
  catch (e) { /* */ }
}
const goDetail = (id) => { if (id) uni.navigateTo({ url: `/pages/attraction/detail?id=${id}` }) }
const goLogin = () => uni.navigateTo({ url: '/pages/login/index' })
const goExplore = () => uni.switchTab({ url: '/pages/attraction/list' })

onShow(() => { if (store.isLoggedIn) load(true) })
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';
.page { min-height: 100vh; background: $bg-page; }
.empty-page { padding: 140rpx 48rpx; text-align: center; }
.big { font-size: 80rpx; display: block; margin-bottom: 16rpx; }
.tip { color: $text-secondary; display: block; margin-bottom: 28rpx; }
.muted { color: $text-muted; font-size: 26rpx; }
.btn { @include btn-primary; height: 80rpx; line-height: 80rpx; padding: 0 40rpx; font-size: 28rpx; }

.tabs {
  display: flex;
  background: $bg-card;
  padding: 16rpx 20rpx;
  gap: 8rpx;
  box-shadow: $shadow-card;
  text {
    flex: 1;
    text-align: center;
    font-size: 24rpx;
    color: $text-secondary;
    padding: 14rpx 0;
    border-radius: 999rpx;
    &.on {
      background: $gradient-brand;
      color: #fff;
      font-weight: 700;
      box-shadow: $shadow-primary;
    }
  }
}

.scroll { padding: 20rpx 24rpx; height: calc(100vh - 100rpx); }
.card {
  @include card;
  margin-bottom: 20rpx;
  overflow: hidden;
  border-left: 6rpx solid $primary;
}
.head {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background: linear-gradient(90deg, rgba(31,143,58,0.06), transparent);
}
.no { font-size: 24rpx; color: $text-muted; }
.st {
  font-size: 24rpx;
  font-weight: 700;
  &.pending { color: #F59E0B; }
  &.paid { color: $secondary; }
  &.completed { color: $dark-tertiary; }
  &.cancelled { color: $text-muted; }
}
.body { padding: 20rpx 24rpx; }
.name { font-size: 30rpx; font-weight: 700; color: $dark; display: block; }
.sub { font-size: 24rpx; color: $text-muted; margin-top: 8rpx; display: block; }
.foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 24rpx;
  border-top: 1rpx solid $bg-secondary;
}
.total { font-size: 26rpx; color: $text-secondary; }
.amt { font-size: 36rpx; font-weight: 800; color: $primary; }
.acts { display: flex; gap: 12rpx; }
.ghost, .primary {
  font-size: 24rpx;
  padding: 10rpx 28rpx;
  border-radius: 999rpx;
  line-height: 1.5;
  background: $bg-page;
  color: $text-secondary;
  border: 1rpx solid $bg-secondary;
}
.primary {
  @include btn-primary;
  padding: 10rpx 28rpx;
  height: auto;
  line-height: 1.5;
  font-size: 24rpx;
  box-shadow: none;
}
.end { text-align: center; padding: 24rpx; }
</style>
