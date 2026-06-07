<template>
  <view class="list-page">
    <view class="head-band">
      <view class="band-inner">
        <text class="band-title">探索景区</text>
        <view class="search glass">
          <text class="si">⌕</text>
          <input v-model="keyword" placeholder="搜索景点名称" confirm-type="search" @confirm="refresh" />
          <text v-if="keyword" class="clear" @click="keyword = ''; refresh()">×</text>
        </view>
      </view>
    </view>

    <view class="filters">
      <view class="chip" :class="{ on: regionName }" @click="openPicker('region')">{{ regionName || '地区' }} ▾</view>
      <view class="chip" :class="{ on: categoryName }" @click="openPicker('category')">{{ categoryName || '分类' }} ▾</view>
      <view class="chip sort" @click="cycleSort">{{ sortLabels[sortIndex] }} ▾</view>
    </view>

    <view v-if="loading" class="center"><text class="muted">加载中…</text></view>
    <view v-else-if="!list.length" class="center">
      <text class="emoji">🗺️</text>
      <text class="muted">暂无景点</text>
    </view>
    <scroll-view v-else scroll-y class="scroll" :scroll-top="scrollTop" :scroll-with-animation="true" @scroll="onScroll" @scrolltolower="loadMore">
      <view class="grid">
        <view v-for="item in list" :key="item.id" class="card" @click="goDetail(item.id)">
          <image class="thumb" :src="item.coverImage || defaultImg" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.name }}</text>
            <text class="score">⭐ {{ item.rating || '4.8' }}</text>
            <view class="price-row">
              <text class="price">{{ formatPrice(item.price) }}</text>
              <text class="tag">去看看</text>
            </view>
          </view>
        </view>
      </view>
      <view class="foot">
        <text :class="hasMore ? 'link' : 'muted'">{{ hasMore ? '加载更多' : '— 已经到底了 —' }}</text>
      </view>
    </scroll-view>

    <!-- 回到顶部 -->
    <view v-if="showBackTop" class="back-top" @click="scrollToTop">
      <text class="back-top-icon">↑</text>
    </view>

    <view v-if="pickerShow" class="mask" @click="pickerShow = false">
      <view class="sheet" @click.stop>
        <view class="sheet-head">
          <text @click="pickerShow = false">取消</text>
          <text class="tit">{{ pickerType === 'region' ? '选择地区' : '选择分类' }}</text>
          <text class="ok" @click="confirmPicker">确定</text>
        </view>
        <scroll-view scroll-y class="sheet-body">
          <view v-for="opt in flatOptions" :key="opt.id" class="opt" :class="{ on: tempId === opt.id }" @click="tempId = opt.id">
            <text>{{ opt.name }}</text>
            <text v-if="tempId === opt.id">✓</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getAttractions, getRegions, getCategories } from '@/api/index.js'
import { fallbackAttractions, formatPrice, normalizeList } from '@/utils/travel.js'

const keyword = ref('')
const loading = ref(true)
const list = ref([])
const page = ref(1)
const hasMore = ref(true)
const sortIndex = ref(0)
const sortLabels = ['热门', '评分', '价格', '最新']
const regionId = ref('')
const regionName = ref('')
const categoryId = ref('')
const categoryName = ref('')
const regions = ref([])
const categories = ref([])
const pickerShow = ref(false)
const pickerType = ref('region')
const tempId = ref('')
const defaultImg = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCI+PHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0YwRjJGNyIvPjwvc3ZnPg=='
const scrollTop = ref(0)
const showBackTop = ref(false)

const flatten = (nodes, out = []) => {
  if (!Array.isArray(nodes)) return out
  nodes.forEach((n) => {
    out.push({ id: n.id, name: n.name || n.label })
    if (n.children?.length) flatten(n.children, out)
  })
  return out
}
const flatOptions = computed(() => {
  const src = pickerType.value === 'region' ? regions.value : categories.value
  return [{ id: '', name: '全部' }, ...flatten(src)]
})

const fetchList = async (reset = false) => {
  if (reset) { page.value = 1; hasMore.value = true; loading.value = true }
  try {
    const { list: rows, total } = await getAttractions({
      page: page.value, pageSize: 10, sort: sortIndex.value,
      keyword: keyword.value || undefined,
      regionId: regionId.value || undefined,
      categoryId: categoryId.value || undefined
    })
    const hasFilters = keyword.value || regionId.value || categoryId.value
    const normalized = normalizeList(rows, !hasFilters && page.value === 1 ? fallbackAttractions : [])
    if (reset || page.value === 1) list.value = normalized
    else list.value = [...list.value, ...normalized]
    hasMore.value = list.value.length < total && rows.length >= 10
  } catch (e) {
    if (page.value === 1) {
      list.value = normalizeList([], fallbackAttractions)
      hasMore.value = false
    }
  }
  finally { loading.value = false }
}
const refresh = () => fetchList(true)
const loadMore = () => { if (hasMore.value && !loading.value) { page.value++; fetchList(false) } }
const cycleSort = () => { sortIndex.value = (sortIndex.value + 1) % 4; refresh() }
const openPicker = (type) => { pickerType.value = type; tempId.value = type === 'region' ? regionId.value : categoryId.value; pickerShow.value = true }
const confirmPicker = () => {
  const opt = flatOptions.value.find((o) => o.id === tempId.value)
  if (pickerType.value === 'region') {
    regionId.value = tempId.value
    regionName.value = opt?.name === '全部' ? '' : (opt?.name || '')
  } else {
    categoryId.value = tempId.value
    categoryName.value = opt?.name === '全部' ? '' : (opt?.name || '')
  }
  pickerShow.value = false
  refresh()
}
const goDetail = (id) => uni.navigateTo({ url: `/pages/attraction/detail?id=${id}` })
const onScroll = (e) => { showBackTop.value = (e.detail?.scrollTop || 0) > 500 }
const scrollToTop = () => { scrollTop.value = 1; nextTick(() => { scrollTop.value = 0 }) }

onLoad((opts) => {
  if (opts?.categoryId) { categoryId.value = opts.categoryId; categoryName.value = decodeURIComponent(opts.categoryName || '') }
  if (opts?.keyword) keyword.value = opts.keyword
})
onShow(async () => {
  const pending = uni.getStorageSync('pendingAttractionFilter')
  if (pending) {
    categoryId.value = pending.categoryId || ''
    categoryName.value = pending.categoryName || ''
    uni.removeStorageSync('pendingAttractionFilter')
  }
  try {
    const [r, c] = await Promise.all([getRegions(), getCategories()])
    regions.value = Array.isArray(r) ? r : []
    categories.value = Array.isArray(c) ? c : []
  } catch (e) { /* */ }
  refresh()
})
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';
.list-page { min-height: 100vh; background: $bg-page; }
.head-band {
  position: relative;
  padding-bottom: 24rpx;
  background: linear-gradient(180deg, #42ad4e, $primary);
  border-radius: 0 0 34rpx 34rpx;
}
.band-inner { position: relative; z-index: 1; padding: 28rpx 28rpx 30rpx; }
.band-title { color: #fff; font-size: 36rpx; font-weight: 800; display: block; margin-bottom: 20rpx; }
.search {
  display: flex; align-items: center;
  background: rgba(255,255,255,0.95);
  border-radius: 999rpx; padding: 16rpx 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(18,108,42,0.16);
  input { flex: 1; font-size: 28rpx; margin-left: 12rpx; }
}
.si { color: $primary; font-size: 30rpx; }
.clear { color: $text-muted; padding: 8rpx; }
.filters {
  display: flex; gap: 16rpx; padding: 20rpx 24rpx;
  flex-wrap: wrap;
}
.chip {
  padding: 12rpx 28rpx; border-radius: 999rpx;
  background: $bg-card; font-size: 24rpx; color: $text-secondary;
  box-shadow: $shadow-card; border: 1rpx solid transparent;
  &.on, &.sort { background: rgba(31,143,58,0.1); color: $primary; border-color: rgba(31,143,58,0.25); font-weight: 600; }
}
.scroll { height: calc(100vh - 280rpx); }
.grid {
  display: flex; flex-wrap: wrap;
  justify-content: space-between;
  padding: 8rpx 16rpx 24rpx;
  &::after { content: ''; width: calc(50% - 8rpx); } // 占位保证最后一行左对齐
}
.card {
  width: calc(50% - 8rpx);
  box-sizing: border-box;
  @include card;
  overflow: hidden;
  border: 1rpx solid rgba(31,143,58,0.08);
  margin-bottom: 16rpx;
}
.thumb { width: 100%; height: 240rpx; background: $bg-secondary; }
.info { padding: 18rpx 20rpx 22rpx; }
.name { font-size: 28rpx; font-weight: 700; color: $dark; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.score { font-size: 22rpx; color: $accent-dark; margin: 8rpx 0; display: block; }
.price-row { display: flex; align-items: center; justify-content: space-between; }
.price { font-size: 32rpx; font-weight: 800; color: $primary; }
.price.free { font-size: 24rpx; color: $secondary; }
.tag { font-size: 20rpx; color: $primary; background: rgba(31,143,58,0.1); padding: 4rpx 12rpx; border-radius: 8rpx; }
.center { padding: 100rpx; text-align: center; }
.emoji { font-size: 72rpx; display: block; margin-bottom: 16rpx; }
.muted { color: $text-muted; }
.foot { text-align: center; padding: 24rpx; }
.link { color: $primary; font-weight: 600; }
.mask { position: fixed; inset: 0; background: rgba(26,26,46,0.5); z-index: 99; display: flex; align-items: flex-end; }
.sheet { width: 100%; background: $bg-card; border-radius: $radius-lg $radius-lg 0 0; max-height: 55vh; }
.sheet-head { display: flex; justify-content: space-between; padding: 28rpx 32rpx; border-bottom: 1rpx solid $bg-secondary; }
.tit { font-weight: 700; color: $dark; }
.ok { color: $primary; font-weight: 700; }
.sheet-body { max-height: 45vh; }
.opt { display: flex; justify-content: space-between; padding: 28rpx 40rpx; &.on { color: $primary; background: rgba(31,143,58,0.06); font-weight: 600; } }
.back-top {
  position: fixed; right: 32rpx; bottom: 120rpx; z-index: 90;
  width: 80rpx; height: 80rpx;
  border-radius: 50%;
  background: $primary;
  box-shadow: 0 8rpx 24rpx rgba(31,143,58,0.35);
  display: flex; align-items: center; justify-content: center;
  transition: opacity 0.3s;
}
.back-top-icon { color: #fff; font-size: 36rpx; font-weight: 700; line-height: 1; }
</style>
