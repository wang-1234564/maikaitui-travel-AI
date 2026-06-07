<template>
  <div class="guide-detail-page" v-if="guide">
    <!-- Hero -->
    <section class="detail-hero">
      <div class="hero-img">
        <img :src="guide.coverImage || defaultCover" :alt="guide.title" />
      </div>
      <div class="hero-overlay"></div>
      <div class="hero-info">
        <div class="hero-tags">
          <span class="tag" v-if="guide.durationDays">{{ guide.durationDays }}天{{ guide.durationDays - 1 }}晚</span>
          <span class="tag" v-if="guide.season">{{ guide.season }}季</span>
          <span class="tag" v-if="guide.travelStyle">{{ styleLabel(guide.travelStyle) }}</span>
        </div>
        <h1>{{ guide.title }}</h1>
        <p class="hero-summary" v-if="guide.summary">{{ guide.summary }}</p>
        <div class="hero-stats">
          <span>📍 {{ guide.destination }}</span>
          <span>👁️ {{ viewCountText }}</span>
          <span>❤️ {{ guide.likeCount || 0 }} 赞</span>
          <span v-if="budgetText">💰 {{ budgetText }}</span>
        </div>
      </div>
      <button class="back-btn" @click="$router.back()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
    </section>

    <!-- Body -->
    <section class="detail-body container">
      <div class="body-layout">
        <!-- Sidebar -->
        <aside class="detail-sidebar">
          <!-- Itinerary -->
          <div class="sidebar-card" v-if="itinerary.length">
            <h3>📋 行程概览</h3>
            <div class="itinerary-list">
              <div class="itin-day" v-for="(day, i) in itinerary" :key="i">
                <div class="day-head">
                  <span class="day-num">Day {{ day.day }}</span>
                  <span class="day-title">{{ day.title }}</span>
                </div>
                <div class="day-detail">
                  <div v-if="day.spots?.length" class="day-spots">
                    <span class="day-label">🏞️ 景点</span>
                    <span v-for="spot in day.spots" :key="spot" class="spot-chip">{{ spot }}</span>
                  </div>
                  <div v-if="day.hotel && day.hotel !== '-'" class="day-hotel">
                    <span class="day-label">🏨 住宿</span>
                    <span>{{ day.hotel }}</span>
                  </div>
                  <div v-if="day.meals?.length" class="day-meals">
                    <span class="day-label">🍜 美食</span>
                    <span>{{ day.meals.join('、') }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Budget -->
          <div class="sidebar-card" v-if="guide.budgetMin > 0 || guide.budgetMax > 0">
            <h3>💰 预算参考</h3>
            <div class="budget-range">
              <span class="budget-num">¥{{ guide.budgetMin }}</span>
              <span class="budget-sep">~</span>
              <span class="budget-num">¥{{ guide.budgetMax }}</span>
              <span class="budget-unit">/人</span>
            </div>
          </div>

          <!-- Tips -->
          <div class="sidebar-card" v-if="tips.length">
            <h3>💡 旅行贴士</h3>
            <div class="tips-list">
              <div class="tip-item" v-for="(tip, i) in tips" :key="i">
                <strong>{{ tip.title }}</strong>
                <p>{{ tip.content }}</p>
              </div>
            </div>
          </div>

          <!-- Related Attractions -->
          <div class="sidebar-card" v-if="attractionNames.length">
            <h3>🏞️ 相关景区</h3>
            <div class="related-attrs">
              <span v-for="(name, i) in attractionNames" :key="i" class="attr-link">{{ name }}</span>
            </div>
          </div>
        </aside>

        <!-- Main Content -->
        <article class="detail-content">
          <div class="markdown-body" v-html="renderedContent"></div>

          <div class="content-footer">
            <button class="btn-secondary" @click="handleLike">
              ❤️ 点赞 ({{ guide.likeCount || 0 }})
            </button>
            <button class="btn-secondary" @click="handleShare">
              📤 分享
            </button>
          </div>
        </article>
      </div>
    </section>

    <!-- Related Guides -->
    <section class="container related-section" v-if="relatedGuides.length">
      <div class="section-head">
        <h2 class="section-title">相关攻略推荐</h2>
      </div>
      <div class="related-grid">
        <GuideCard v-for="item in relatedGuides" :key="item.id" :guide="item" />
      </div>
    </section>
  </div>

  <!-- Loading -->
  <div class="loading-state" v-else-if="loading">
    <div class="skeleton-hero skeleton"></div>
    <div class="container" style="margin-top:32px">
      <div style="display:grid;grid-template-columns:280px 1fr;gap:32px">
        <div class="skeleton" style="height:400px"></div>
        <div class="skeleton" style="height:600px"></div>
      </div>
    </div>
  </div>

  <!-- Error -->
  <div class="empty-state" v-else>
    <div class="empty-icon">😢</div>
    <p class="empty-text">攻略不存在或已下架</p>
    <router-link to="/guides" class="btn-primary">返回攻略列表</router-link>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import { getGuideById, getGuidesByAttraction, getHotGuides } from '@/api'
import GuideCard from '@/components/common/GuideCard.vue'

const route = useRoute()
const guide = ref(null)
const loading = ref(true)
const relatedGuides = ref([])
const defaultCover = 'https://picsum.photos/seed/guide-detail/1200/500'

const itinerary = computed(() => {
  if (!guide.value?.itinerary) return []
  if (typeof guide.value.itinerary === 'string') {
    try { return JSON.parse(guide.value.itinerary) } catch { return [] }
  }
  return guide.value.itinerary
})

const tips = computed(() => {
  if (!guide.value?.tips) return []
  if (typeof guide.value.tips === 'string') {
    try { return JSON.parse(guide.value.tips) } catch { return [] }
  }
  return guide.value.tips
})

const attractionIds = computed(() => {
  if (!guide.value?.attractions) return []
  if (typeof guide.value.attractions === 'string') {
    try { return JSON.parse(guide.value.attractions) } catch { return [] }
  }
  return guide.value.attractions
})

const attractionNames = computed(() => {
  // Map attraction IDs to known names from seed data
  const nameMap = { 1: '西湖', 2: '故宫', 3: '成都熊猫基地', 4: '大理古城', 5: '三亚亚龙湾', 6: '上海外滩', 7: '八达岭长城', 8: '灵隐寺' }
  return attractionIds.value.map(id => nameMap[id] || `景区#${id}`)
})

const viewCountText = computed(() => {
  const v = guide.value?.viewCount || 0
  if (v >= 10000) return `${(v / 10000).toFixed(1)}万`
  if (v >= 1000) return `${(v / 1000).toFixed(1)}k`
  return String(v)
})

const budgetText = computed(() => {
  if (!guide.value) return ''
  const min = guide.value.budgetMin || 0
  const max = guide.value.budgetMax || 0
  if (min === 0 && max === 0) return ''
  if (min === max && min > 0) return `¥${min}`
  return `¥${min}~${max}`
})

const renderedContent = computed(() => {
  if (!guide.value?.content) return ''
  return marked(guide.value.content)
})

const styleLabel = (s) => {
  const map = { '亲子': '亲子游', '情侣': '情侣游', '独自': '独自游', '朋友': '朋友游' }
  return map[s] || s
}

function handleLike() {
  if (guide.value) {
    guide.value.likeCount = (guide.value.likeCount || 0) + 1
  }
}

function handleShare() {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(window.location.href)
    alert('链接已复制，分享给朋友吧！')
  }
}

onMounted(async () => {
  const id = route.params.id
  try {
    const res = await getGuideById(id)
    guide.value = res.data || res

    // Fetch related guides
    try {
      const [byAttr, hot] = await Promise.all([
        attractionIds.value.length
          ? getGuidesByAttraction(attractionIds.value[0], 3).catch(() => [])
          : Promise.resolve([]),
        getHotGuides(3).catch(() => [])
      ])
      const byAttrRows = byAttr.data || byAttr || []
      const hotRows = (hot.data || hot || []).filter(g => g.id !== Number(id))
      const seen = new Set()
      relatedGuides.value = [...byAttrRows, ...hotRows]
        .filter(g => {
          if (g.id === Number(id) || seen.has(g.id)) return false
          seen.add(g.id)
          return true
        })
        .slice(0, 3)
    } catch {
      relatedGuides.value = []
    }
  } catch {
    guide.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.guide-detail-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fbfcfa 0%, #f6f9f3 30%, #ffffff 100%);
  padding-bottom: 80px;
}

/* Hero */
.detail-hero {
  position: relative;
  height: 420px;
  overflow: hidden;
}

.hero-img {
  position: absolute;
  inset: 0;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(20,37,27,0.3) 0%,
    rgba(20,37,27,0.45) 40%,
    rgba(20,37,27,0.82) 100%
  );
}

.hero-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 40px;
  color: #fff;
  max-width: 800px;

  h1 {
    font-size: 2.2rem;
    font-weight: 900;
    line-height: 1.3;
    margin: 12px 0;
    text-shadow: 0 2px 8px rgba(0,0,0,0.25);
  }

  .hero-summary {
    font-size: 1rem;
    opacity: 0.9;
    margin-bottom: 16px;
  }
}

.hero-tags {
  display: flex;
  gap: 8px;
  .tag {
    padding: 4px 14px;
    border-radius: 999px;
    background: rgba(255,255,255,0.2);
    backdrop-filter: blur(8px);
    font-size: 0.8rem;
    font-weight: 600;
  }
}

.hero-stats {
  display: flex;
  gap: 20px;
  font-size: 0.85rem;
  opacity: 0.85;

  span { display: inline-flex; align-items: center; gap: 4px; }
}

.back-btn {
  position: absolute;
  top: 24px;
  left: 24px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.3);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;

  &:hover { background: rgba(255,255,255,0.35); }
}

/* Body */
.detail-body {
  margin-top: -40px;
  position: relative;
  z-index: 5;
}

.body-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 32px;
  align-items: start;
}

/* Sidebar */
.detail-sidebar {
  position: sticky;
  top: 90px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(31,143,58,0.08);
  box-shadow: 0 8px 24px rgba(26,64,38,0.06);

  h3 {
    font-size: 1rem;
    font-weight: 700;
    color: var(--dark);
    margin-bottom: 14px;
  }
}

/* Itinerary */
.itinerary-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.itin-day {
  border-left: 3px solid var(--primary);
  padding-left: 14px;

  .day-head {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .day-num {
    background: linear-gradient(135deg, var(--primary), var(--primary-dark));
    color: #fff;
    font-size: 0.7rem;
    font-weight: 800;
    padding: 2px 10px;
    border-radius: 999px;
  }

  .day-title {
    font-weight: 700;
    color: var(--dark);
    font-size: 0.9rem;
  }

  .day-detail {
    font-size: 0.8rem;
    color: var(--text-light);
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .day-label {
    font-weight: 600;
    color: var(--text-light);
    margin-right: 6px;
  }

  .day-spots {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px;
  }

  .spot-chip {
    background: rgba(31,143,58,0.08);
    color: var(--primary-dark);
    padding: 2px 8px;
    border-radius: 6px;
    font-size: 0.75rem;
  }
}

/* Budget */
.budget-range {
  display: flex;
  align-items: baseline;
  gap: 6px;
  color: var(--primary);
  font-weight: 800;

  .budget-num { font-size: 1.4rem; }
  .budget-sep { color: var(--text-lighter); }
  .budget-unit { font-size: 0.8rem; color: var(--text-light); }
}

/* Tips */
.tips-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tip-item {
  strong {
    display: block;
    font-size: 0.85rem;
    color: var(--dark);
    margin-bottom: 2px;
  }
  p {
    font-size: 0.8rem;
    color: var(--text-light);
    margin: 0;
    line-height: 1.5;
  }
}

/* Related Attractions */
.related-attrs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.attr-link {
  padding: 4px 12px;
  background: rgba(31,143,58,0.06);
  color: var(--primary-dark);
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

/* Main Content */
.detail-content {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  border: 1px solid rgba(31,143,58,0.08);
  box-shadow: 0 10px 30px rgba(26,64,38,0.06);
  min-width: 0;
}

.markdown-body {
  :deep(h1) { font-size: 1.8rem; color: var(--dark); margin: 0 0 20px; }
  :deep(h2) {
    font-size: 1.35rem; color: var(--dark); margin: 36px 0 16px;
    padding-bottom: 8px; border-bottom: 2px solid rgba(31,143,58,0.12);
  }
  :deep(h3) { font-size: 1.1rem; color: var(--dark-tertiary); margin: 24px 0 12px; }
  :deep(p) { font-size: 0.95rem; line-height: 1.85; color: var(--text); margin: 0 0 16px; }
  :deep(strong) { color: var(--primary-dark); }
  :deep(blockquote) {
    margin: 16px 0; padding: 14px 20px;
    background: rgba(245,184,75,0.08);
    border-left: 4px solid var(--accent);
    border-radius: 0 8px 8px 0;
    p { margin: 0; font-size: 0.9rem; color: var(--dark-tertiary); }
    strong { color: var(--accent-dark); }
  }
  :deep(ul), :deep(ol) { padding-left: 24px; margin: 12px 0; }
  :deep(li) { font-size: 0.92rem; line-height: 1.8; color: var(--text); }
  :deep(img) { max-width: 100%; border-radius: 8px; margin: 16px 0; }
  :deep(hr) { border: none; border-top: 1px solid rgba(31,143,58,0.1); margin: 32px 0; }
}

.content-footer {
  display: flex;
  gap: 12px;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid rgba(31,143,58,0.1);

  .btn-secondary {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 24px;
  }
}

/* Related Section */
.related-section {
  margin-top: 48px;
}

.section-head {
  margin-bottom: 24px;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* States */
.loading-state { min-height: 100vh; }
.skeleton-hero { height: 420px; }
.empty-state {
  text-align: center;
  padding: 120px 20px;
  .empty-icon { font-size: 4rem; margin-bottom: 16px; }
  .empty-text { font-size: 1.1rem; color: var(--text); font-weight: 600; margin-bottom: 24px; }
  .btn-primary { display: inline-flex; }
}

@media (max-width: 992px) {
  .detail-hero { height: 340px; }
  .hero-info {
    padding: 24px;
    h1 { font-size: 1.6rem; }
  }
  .body-layout { grid-template-columns: 1fr; }
  .detail-sidebar { position: static; }
  .detail-content { padding: 24px; }
  .related-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 600px) {
  .detail-hero { height: 280px; }
  .hero-info h1 { font-size: 1.3rem; }
  .hero-stats { flex-wrap: wrap; gap: 10px; }
  .related-grid { grid-template-columns: 1fr; }
}
</style>
