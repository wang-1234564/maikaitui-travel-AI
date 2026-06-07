<template>
  <div class="attraction-list-page">
    <!-- Hero header -->
    <div class="list-hero">
      <div class="container">
        <h1 class="list-hero-title">景区探索</h1>
        <p class="list-hero-subtitle">按地区、分类和热度快速找到适合你的目的地</p>
      </div>
    </div>

    <div class="container section">
      <!-- Filter Bar -->
      <div class="filter-bar glass-card">
        <div class="filter-row">
          <div class="filter-group">
            <select v-model="filters.regionId" @change="handleFilterChange" class="filter-select">
              <option value="">全部地区</option>
              <option v-for="r in regions" :key="r.id" :value="r.id">{{ r.name }}</option>
            </select>
          </div>

          <div class="filter-group">
            <select v-model="filters.categoryId" @change="handleFilterChange" class="filter-select">
              <option value="">全部分类</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>

          <div class="filter-group">
            <select v-model="filters.sort" @change="handleFilterChange" class="filter-select">
              <option value="hot">热门优先</option>
              <option value="rating">评分最高</option>
              <option value="price">价格最低</option>
              <option value="newest">最新发布</option>
            </select>
          </div>

          <div class="filter-search">
            <input
              v-model="filters.keyword"
              type="text"
              placeholder="搜索景点..."
              class="filter-search-input"
              @keyup.enter="handleFilterChange"
            />
            <button class="filter-search-btn" @click="handleFilterChange">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Results -->
      <div class="results-section">
        <div class="results-header" v-if="!loading">
          <span>共找到 <strong>{{ total }}</strong> 个景区/景点</span>
        </div>

        <!-- Loading Skeleton -->
        <div v-if="loading" class="attractions-grid">
          <div v-for="n in 6" :key="n" class="skeleton-card skeleton"></div>
        </div>

        <!-- Attraction Grid -->
        <div v-else-if="attractions.length > 0" class="attractions-grid">
          <AttractionCard
            v-for="item in attractions"
            :key="item.id"
            :attraction="item"
          />
        </div>

        <!-- Empty State -->
        <div v-else class="empty-state">
          <div class="empty-icon">🔍</div>
          <p class="empty-text">暂无找到相关景区</p>
          <p class="empty-hint">试试调整筛选条件或搜索其他关键词</p>
          <button class="btn-primary" @click="resetFilters" style="margin-top: 20px;">重置筛选</button>
        </div>

        <!-- Load More -->
        <div class="load-more" v-if="hasMore && !loading && attractions.length > 0">
          <button class="btn-secondary" @click="loadMore" :disabled="loadingMore">
            {{ loadingMore ? '加载中...' : '加载更多' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AttractionCard from '@/components/common/AttractionCard.vue'
import { getAttractions, getRegions, getCategories } from '@/api'
import { fallbackAttractions, flattenTree, getPageRows, normalizeAttractionList } from '@/utils/travel'

const route = useRoute()
const router = useRouter()

const attractions = ref([])
const regions = ref([])
const categories = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 12
const hasMore = ref(false)

const filters = reactive({
  keyword: '',
  regionId: '',
  categoryId: '',
  sort: 'hot'
})

function buildParams(page = 1) {
  const params = { page, size: pageSize, sortBy: filters.sort }
  if (filters.keyword.trim()) params.keyword = filters.keyword.trim()
  if (filters.regionId) params.regionId = filters.regionId
  if (filters.categoryId) params.categoryId = filters.categoryId
  return params
}

async function fetchData(page = 1, append = false) {
  if (page === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const data = await getAttractions(buildParams(page))
    const { rows, total: count } = getPageRows(data)
    const hasFilters = filters.keyword.trim() || filters.regionId || filters.categoryId
    const list = normalizeAttractionList(rows, !hasFilters && page === 1 ? fallbackAttractions : [])
    total.value = count || list.length

    if (append) {
      attractions.value.push(...list)
    } else {
      attractions.value = list
    }

    hasMore.value = rows.length > 0 && attractions.value.length < total.value && rows.length >= pageSize
    currentPage.value = page
  } catch (e) {
    if (page === 1) {
      attractions.value = normalizeAttractionList([], fallbackAttractions)
      total.value = attractions.value.length
      hasMore.value = false
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  await fetchData(currentPage.value + 1, true)
}

function handleFilterChange() {
  router.replace({
    query: {
      ...(filters.keyword && { keyword: filters.keyword }),
      ...(filters.regionId && { regionId: filters.regionId }),
      ...(filters.categoryId && { categoryId: filters.categoryId }),
      ...(filters.sort !== 'hot' && { sort: filters.sort })
    }
  })
  fetchData(1, false)
}

function resetFilters() {
  filters.keyword = ''
  filters.regionId = ''
  filters.categoryId = ''
  filters.sort = 'hot'
  router.replace({ query: {} })
  fetchData(1, false)
}

async function fetchFilters() {
  try {
    const [rData, cData] = await Promise.all([getRegions(), getCategories()])
    regions.value = flattenTree(rData).filter((item) => item.id && item.name)
    categories.value = flattenTree(cData).filter((item) => item.id && item.name)
  } catch (e) {
    // API error handled by interceptor
  }
}

onMounted(() => {
  if (route.query.keyword) filters.keyword = route.query.keyword
  if (route.query.regionId) filters.regionId = route.query.regionId
  if (route.query.categoryId) filters.categoryId = route.query.categoryId
  if (route.query.sort) filters.sort = route.query.sort === 'popular' ? 'hot' : route.query.sort

  fetchFilters()
  fetchData(1, false)
})
</script>

<style lang="scss" scoped>
.attraction-list-page {
  min-height: 100vh;
}

.list-hero {
  background:
    linear-gradient(135deg, rgba(18,108,42,0.94), rgba(31,143,58,0.82)),
    url('https://picsum.photos/seed/maikaitui-list/1400/360') center / cover no-repeat;
  padding: 48px 0 54px;
  text-align: center;

  .list-hero-title {
    font-size: 2.15rem;
    font-weight: 800;
    color: var(--white);
    margin-bottom: 8px;
  }

  .list-hero-subtitle {
    font-size: 1.1rem;
    color: rgba(255, 255, 255, 0.7);
  }

  @media (max-width: 768px) {
    padding: 40px 0;
    .list-hero-title { font-size: 1.8rem; }
  }
}

/* Filter Bar */
.filter-bar {
  margin-top: -28px;
  margin-bottom: 30px;
  padding: 16px 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(31, 143, 58, 0.08);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  @media (max-width: 768px) {
    flex-direction: column;
  }
}

.filter-group {
  flex-shrink: 0;

  @media (max-width: 768px) { width: 100%; }
}

.filter-select {
  padding: 10px 36px 10px 14px;
  border: 1.5px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  font-size: 0.9rem;
  color: var(--text);
  background: var(--white);
  cursor: pointer;
  appearance: none;
  -webkit-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1l4 4 4-4' stroke='%23636E72' stroke-width='1.5' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  transition: var(--transition);

  &:focus { border-color: var(--primary); }

  @media (max-width: 768px) { width: 100%; }
}

.filter-search {
  flex: 1;
  display: flex;
  align-items: center;
  background: #f6f8f4;
  border: 1.5px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
  transition: var(--transition);
  min-width: 180px;

  &:focus-within {
    border-color: var(--primary);
    background: var(--white);
  }

  @media (max-width: 768px) { width: 100%; }
}

.filter-search-input {
  flex: 1;
  padding: 10px 14px;
  font-size: 0.9rem;
  border: none;
  background: transparent;
  min-width: 0;

  &::placeholder { color: var(--text-lighter); }
}

.filter-search-btn {
  padding: 10px 14px;
  color: var(--primary-dark);
  transition: var(--transition);

  &:hover { background: rgba(31, 143, 58, 0.08); }
}

/* Results */
.results-header {
  margin-bottom: 20px;
  font-size: 0.95rem;
  color: var(--text-light);

  strong { color: var(--primary); font-weight: 700; }
}

.attractions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;

  @media (max-width: 1024px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 600px) { grid-template-columns: 1fr; }
}

.skeleton-card {
  height: 360px;
}

.load-more {
  text-align: center;
  margin-top: 40px;
}
</style>
