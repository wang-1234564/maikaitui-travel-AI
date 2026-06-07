<template>
  <div class="user-favorites-page">
    <div class="list-hero">
      <div class="container">
        <h1 class="list-hero-title">我的收藏</h1>
        <p class="list-hero-desc">共收藏 <strong>{{ total }}</strong> 个景区</p>
      </div>
    </div>

    <div class="container section">
      <!-- Loading -->
      <div v-if="loading" class="favorites-grid">
        <div v-for="n in 10" :key="n" class="skeleton-card skeleton"></div>
      </div>

      <!-- Favorites Grid -->
      <div v-else-if="favorites.length > 0" class="favorites-grid">
        <AttractionCard
          v-for="fav in favorites"
          :key="fav.id"
          :attraction="fav"
          :favorited="true"
          @toggle-favorite="handleToggleFav(fav, $event)"
        />
      </div>

      <!-- Empty -->
      <div v-else class="empty-state">
        <div class="empty-icon">❤️</div>
        <p class="empty-text">还没有收藏任何景点</p>
        <p class="empty-hint">浏览景点时点击爱心即可收藏</p>
        <router-link to="/attractions" class="btn-primary" style="margin-top: 20px;">去探索</router-link>
      </div>

      <!-- Pagination -->
      <div class="pagination" v-if="total > pageSize && !loading">
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span class="page-info">{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
        <button :disabled="currentPage >= Math.ceil(total / pageSize)" @click="changePage(currentPage + 1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyFavorites, removeFavorite } from '@/api'
import { normalizeAttractionList, getPageRows } from '@/utils/travel'
import AttractionCard from '@/components/common/AttractionCard.vue'

const favorites = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function fetchFavorites(page = 1) {
  loading.value = true
  try {
    const data = await getMyFavorites({ page, pageSize: pageSize.value })
    const { rows, total: totalCount } = getPageRows(data)
    // 用 attractionId 覆盖 id，确保 AttractionCard 跳转到正确的景区详情页
    favorites.value = normalizeAttractionList(rows, []).map(item => ({
      ...item,
      id: item.attractionId || item.id
    }))
    total.value = totalCount
    currentPage.value = page
  } catch (e) {
    favorites.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  fetchFavorites(page)
  window.scrollTo({ top: 200, behavior: 'smooth' })
}

async function handleToggleFav(fav, event) {
  // If unfavorited from the card, remove from list
  if (event && !event.favorited) {
    try {
      await removeFavorite(fav.attractionId || fav.id)
    } catch {
      // handled by interceptor
    }
    favorites.value = favorites.value.filter(f => f.id !== fav.id)
    total.value = Math.max(0, total.value - 1)
  }
}

onMounted(() => {
  fetchFavorites(1)
})
</script>

<style lang="scss" scoped>
.user-favorites-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fbfcfa 0%, #f6f9f3 42%, #ffffff 100%);
}

.list-hero {
  background: linear-gradient(135deg, var(--dark), var(--dark-secondary));
  padding: 50px 0;
  text-align: center;

  .list-hero-title {
    font-size: 2rem;
    font-weight: 800;
    color: var(--white);
  }

  .list-hero-desc {
    color: rgba(255,255,255,0.65);
    margin-top: 10px;
    font-size: 0.95rem;

    strong {
      color: var(--accent);
      font-size: 1.2rem;
    }
  }
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(4, 1fr);
  }

  @media (max-width: 992px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  @media (max-width: 480px) {
    grid-template-columns: 1fr;
  }
}

.skeleton-card {
  height: 310px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;

  button {
    padding: 10px 24px;
    border: 1.5px solid rgba(0,0,0,0.1);
    border-radius: 999px;
    font-size: 0.9rem;
    font-weight: 600;
    transition: var(--transition);
    color: var(--text);

    &:hover:not(:disabled) {
      border-color: var(--primary);
      color: var(--primary);
      background: rgba(31, 143, 58, 0.04);
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }

  .page-info {
    font-size: 0.9rem;
    color: var(--text-light);
    font-weight: 600;
  }
}
</style>
