<template>
  <div class="guide-list-page">
    <!-- Hero -->
    <section class="list-hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <h1>旅行攻略</h1>
        <p>发现别人的玩法，规划自己的旅程</p>
      </div>
    </section>

    <!-- Filter Bar -->
    <div class="container">
      <div class="filter-bar glass-card">
        <div class="filter-row">
          <div class="filter-item">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
            </svg>
            <input
              v-model="filters.destination"
              placeholder="输入目的地搜索"
              @keyup.enter="handleSearch"
            />
          </div>
          <select v-model="filters.durationDays" @change="handleSearch" class="filter-select">
            <option value="">全部天数</option>
            <option value="1">1天</option>
            <option value="2">2天</option>
            <option value="3">3天</option>
            <option value="4">4天</option>
            <option value="5">5天</option>
            <option value="6">6天</option>
            <option value="7">7天+</option>
          </select>
          <select v-model="filters.season" @change="handleSearch" class="filter-select">
            <option value="">全部季节</option>
            <option value="春">🌸 春季</option>
            <option value="夏">☀️ 夏季</option>
            <option value="秋">🍂 秋季</option>
            <option value="冬">❄️ 冬季</option>
            <option value="全年">📅 全年</option>
          </select>
          <select v-model="filters.travelStyle" @change="handleSearch" class="filter-select">
            <option value="">全部风格</option>
            <option value="亲子">👨‍👩‍👧 亲子</option>
            <option value="情侣">💑 情侣</option>
            <option value="独自">🚶 独自</option>
            <option value="朋友">👫 朋友</option>
          </select>
          <select v-model="filters.sortBy" @change="handleSearch" class="filter-select">
            <option value="popular">🔥 热门</option>
            <option value="newest">🆕 最新</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Results -->
    <section class="container guide-results">
      <div class="results-head" v-if="!loading">
        <span>共 <strong>{{ total }}</strong> 篇攻略</span>
      </div>

      <!-- Loading -->
      <div class="guide-grid" v-if="loading">
        <div v-for="n in 6" :key="n" class="skeleton-card skeleton"></div>
      </div>

      <!-- Empty -->
      <div class="empty-state" v-else-if="!guides.length">
        <div class="empty-icon">📝</div>
        <p class="empty-text">暂无相关攻略</p>
        <p class="empty-hint">试试更换筛选条件</p>
      </div>

      <!-- Grid -->
      <div class="guide-grid" v-else>
        <GuideCard v-for="item in guides" :key="item.id" :guide="item" />
      </div>

      <!-- Load More -->
      <div class="load-more" v-if="hasMore && !loading">
        <button class="btn-secondary" @click="loadMore" :disabled="loadingMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getGuides } from '@/api'
import GuideCard from '@/components/common/GuideCard.vue'

const guides = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = 9
const hasMore = ref(false)

const filters = reactive({
  destination: '',
  durationDays: '',
  season: '',
  travelStyle: '',
  sortBy: 'popular'
})

async function fetchData(pageNum = 1, append = false) {
  if (pageNum === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const params = {
      page: pageNum,
      size: pageSize,
      sortBy: filters.sortBy
    }
    if (filters.destination.trim()) params.destination = filters.destination.trim()
    if (filters.durationDays) params.durationDays = filters.durationDays
    if (filters.season) params.season = filters.season
    if (filters.travelStyle) params.travelStyle = filters.travelStyle

    const res = await getGuides(params)
    const data = res.data || res
    const rows = data.records || data.list || []
    total.value = data.total || rows.length

    if (append) {
      guides.value.push(...rows)
    } else {
      guides.value = rows
    }

    hasMore.value = guides.value.length < total.value && rows.length >= pageSize
    page.value = pageNum
  } catch {
    if (!append) guides.value = []
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function handleSearch() {
  fetchData(1, false)
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  await fetchData(page.value + 1, true)
}

onMounted(() => {
  fetchData(1, false)
})
</script>

<style lang="scss" scoped>
.guide-list-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fbfcfa 0%, #f6f9f3 42%, #ffffff 100%);
  padding-bottom: 64px;
}

.list-hero {
  position: relative;
  min-height: 220px;
  overflow: hidden;
  border-radius: 0 0 0 0;

  .hero-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(18,108,42,0.94), rgba(31,143,58,0.78)),
                url('/images/home-hero.png') center/cover no-repeat;
  }

  .hero-content {
    position: relative;
    z-index: 2;
    min-height: 220px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    color: #fff;

    h1 {
      font-size: 2.2rem;
      font-weight: 800;
      margin-bottom: 12px;
      text-shadow: 0 2px 12px rgba(0,0,0,0.2);
    }

    p {
      font-size: 1rem;
      opacity: 0.9;
    }
  }
}

.filter-bar {
  margin-top: -28px;
  position: relative;
  z-index: 10;
  padding: 20px 24px;
  border-radius: 12px;
  background: rgba(255,255,255,0.94);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(31,143,58,0.1);
  box-shadow: 0 12px 36px rgba(26,64,38,0.1);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 180px;
  padding: 8px 14px;
  border-radius: 8px;
  background: var(--light);
  border: 1px solid rgba(31,143,58,0.1);
  color: var(--text-light);

  input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 0.9rem;
    color: var(--text);

    &::placeholder { color: var(--text-lighter); }
    &:focus { outline: none; }
  }
}

.filter-select {
  padding: 8px 14px;
  border-radius: 8px;
  background: var(--light);
  border: 1px solid rgba(31,143,58,0.1);
  font-size: 0.85rem;
  color: var(--text);
  cursor: pointer;
  min-width: 100px;

  &:focus { outline: none; border-color: var(--primary); }
}

.guide-results {
  margin-top: 32px;
}

.results-head {
  margin-bottom: 20px;
  color: var(--text-light);
  font-size: 0.9rem;

  strong { color: var(--dark); }
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.skeleton-card {
  height: 340px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-icon { font-size: 3rem; margin-bottom: 16px; }
.empty-text { font-size: 1.1rem; color: var(--text); font-weight: 600; }
.empty-hint { color: var(--text-lighter); margin-top: 8px; }

.load-more {
  text-align: center;
  margin-top: 40px;

  .btn-secondary {
    min-width: 160px;
  }
}

@media (max-width: 992px) {
  .guide-grid { grid-template-columns: repeat(2, 1fr); }
  .list-hero { min-height: 180px; }
  .list-hero .hero-content { min-height: 180px; }
  .list-hero .hero-content h1 { font-size: 1.8rem; }
}

@media (max-width: 600px) {
  .guide-grid { grid-template-columns: 1fr; }
  .filter-row { flex-direction: column; }
  .filter-item { width: 100%; }
  .filter-select { width: 100%; }
}
</style>
