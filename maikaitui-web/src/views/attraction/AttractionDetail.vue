<template>
  <div class="detail-page">
    <div v-if="loading" class="container section">
      <div class="skeleton detail-skeleton"></div>
    </div>

    <div v-else-if="error" class="container section">
      <div class="empty-state">
        <div class="empty-icon">!</div>
        <p class="empty-text">{{ error }}</p>
        <router-link to="/attractions" class="btn-primary">返回景区列表</router-link>
      </div>
    </div>

    <template v-else-if="attraction">
      <section class="container detail-hero-wrap">
        <div class="detail-hero">
          <img :src="activeImage" :alt="attraction.name" />
          <div class="hero-shade"></div>
          <span class="hero-hot-badge">🔥 热门景点</span>
        </div>
        <div class="hero-actions">
          <button @click="router.back()" class="round-btn" aria-label="返回">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 18l-6-6 6-6"/>
            </svg>
          </button>
          <div>
            <button class="ghost-action" :class="{ favorited: isFavorited }" @click="toggleFavorite" :disabled="favoriteToggling">
              {{ isFavorited ? '♥' : '♡' }} {{ isFavorited ? '已收藏' : '收藏' }}
            </button>
            <button class="ghost-action" @click="copyLink">分享</button>
          </div>
        </div>
        <span class="image-count">{{ currentImageIndex + 1 }}/{{ gallery.length }}</span>
      </section>

      <main class="container detail-shell">
        <section class="detail-card">
          <div class="title-row">
            <div>
              <h1>{{ attraction.name }}</h1>
              <div class="tag-line">
                <span>{{ attraction.categoryName }}</span>
                <span>国家5A景区</span>
              </div>
              <div class="meta-line">
                <span>⌖ {{ attraction.regionName }}</span>
                <span v-if="attraction.address">{{ attraction.address }}</span>
              </div>
            </div>
            <div class="rating-box">
              <strong>{{ attraction.rating.toFixed(1) }}</strong>
              <StarRating :rating="attraction.rating" size="small" />
              <small>{{ formatCount(attraction.viewCount) }}次浏览</small>
            </div>
          </div>

          <div class="summary-row">
            <p class="summary">
              {{ attraction.description || `${attraction.name} 是适合自然观光、休闲度假与亲友出行的精选目的地。` }}
            </p>
            <div class="action-btns">
              <button v-if="attraction.price > 0" class="ticket-btn" @click="showTicketModal = true">
                <span>🎫</span>
                立即购票
              </button>
              <button class="nav-btn" @click="$router.push(`/attraction/${attraction.id}/nav`)">
                <span>➤</span>
                导航
              </button>
            </div>
          </div>

          <div class="info-grid">
            <div>
              <span>景区类别</span>
              <strong>{{ attraction.categoryName }}</strong>
            </div>
            <div>
              <span>详细地址</span>
              <strong>{{ attraction.address || attraction.regionName }}</strong>
            </div>
            <div>
              <span>经度</span>
              <strong>{{ attraction.longitude || '--' }}</strong>
            </div>
            <div>
              <span>纬度</span>
              <strong>{{ attraction.latitude || '--' }}</strong>
            </div>
            <div>
              <span>票价</span>
              <strong class="green">{{ formatPrice(attraction.price) }}<em v-if="attraction.price"> 起</em></strong>
            </div>
            <div>
              <span>景区等级</span>
              <strong>AAAAA</strong>
            </div>
            <div>
              <span>人流量</span>
              <strong>{{ trafficLevel }}</strong>
            </div>
            <div>
              <span>开放时间</span>
              <strong>{{ attraction.openTime || '07:00 - 18:00' }}</strong>
            </div>
          </div>

          <div class="gallery-title-row">
            <h2>景区图片</h2>
          </div>
          <div class="gallery-strip">
            <button
              v-for="(img, idx) in visibleGallery"
              :key="img"
              :class="{ active: idx === currentImageIndex }"
              @click="currentImageIndex = idx"
            >
              <img :src="img" :alt="`${attraction.name}-${idx + 1}`" />
            </button>
          </div>
          <button class="all-images">查看全部图片（{{ gallery.length }}张）〉</button>
        </section>
      </main>

      <section class="container comments-section">
        <div class="comments-card">
          <div class="section-head">
            <h2 class="section-title">游客评价</h2>
            <span>{{ commentTotal }}</span>
          </div>
          <div v-if="userStore.isLoggedIn" class="add-comment">
            <div class="star-input">
              <button
                v-for="s in 5"
                :key="s"
                :class="{ active: s <= commentForm.rating }"
                @click="commentForm.rating = s"
              >★</button>
            </div>
            <textarea v-model="commentForm.content" rows="3" placeholder="分享你的旅行体验"></textarea>
            <button class="comment-submit" :disabled="commentSubmitting || !commentForm.content.trim()" @click="submitComment">
              {{ commentSubmitting ? '提交中...' : '发表评价' }}
            </button>
          </div>
          <router-link v-else to="/login" class="login-hint">登录后可发表评价</router-link>
          <div v-for="comment in comments" :key="comment.id" class="comment">
            <div class="comment-head">
              <div class="comment-user">
                <span class="comment-avatar">{{ (comment.username || '游客').charAt(0) }}</span>
                <strong>{{ comment.username || '游客' }}</strong>
              </div>
              <div class="comment-stars">
                <span v-for="s in 5" :key="s" :class="{ active: s <= (comment.rating || 5) }">★</span>
              </div>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
            </div>
            <p class="comment-body">{{ comment.content }}</p>
          </div>
          <p v-if="!comments.length" class="empty-comments">暂无评价，成为第一个分享体验的人吧。</p>
        </div>
      </section>

      <!-- 购票弹窗 -->
      <TicketModal
        v-if="showTicketModal && attraction"
        :attraction="attraction"
        @close="showTicketModal = false"
        @success="showTicketModal = false"
      />

      <section class="container recommend-section" v-if="recommendations.length">
        <div class="section-head">
          <h2 class="section-title">相关推荐</h2>
        </div>
        <div class="recommend-grid">
          <AttractionCard v-for="item in recommendations" :key="item.id" :attraction="item" />
        </div>
      </section>

      <!-- 相关攻略 -->
      <section class="container guide-section" v-if="relatedGuides.length">
        <div class="section-head">
          <h2 class="section-title">相关攻略</h2>
          <router-link to="/guides" class="text-link">更多攻略</router-link>
        </div>
        <div class="guide-grid-detail">
          <div
            v-for="item in relatedGuides"
            :key="item.id"
            class="guide-mini-card"
            @click="$router.push(`/guide/${item.id}`)"
          >
            <div class="guide-mini-img">
              <img :src="item.coverImage" :alt="item.title" loading="lazy" />
              <span class="guide-mini-days" v-if="item.durationDays">{{ item.durationDays }}天</span>
            </div>
            <div class="guide-mini-body">
              <strong>{{ item.title }}</strong>
              <span>{{ item.destination || '' }}</span>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAiChatStore } from '@/stores/aiChat'
import { addComment, addFavorite, checkFavorited, getAttractionById, getComments, getRecommendations, removeFavorite, getGuidesByAttraction } from '@/api'
import StarRating from '@/components/common/StarRating.vue'
import AttractionCard from '@/components/common/AttractionCard.vue'
import TicketModal from '@/components/common/TicketModal.vue'
import {
  fallbackAttractions,
  formatCount,
  formatPrice,
  getPageRows,
  normalizeAttraction,
  normalizeAttractionList
} from '@/utils/travel'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const attraction = ref(null)
const recommendations = ref([])
const relatedGuides = ref([])
const comments = ref([])
const commentTotal = ref(0)
const loading = ref(true)
const error = ref('')
const commentSubmitting = ref(false)
const currentImageIndex = ref(0)
const isFavorited = ref(false)
const favoriteToggling = ref(false)
const showTicketModal = ref(false)

const commentForm = reactive({
  rating: 5,
  content: ''
})

const gallery = computed(() => attraction.value?.images?.length ? attraction.value.images : [])
const visibleGallery = computed(() => gallery.value.slice(0, 5))
const activeImage = computed(() => gallery.value[currentImageIndex.value] || attraction.value?.coverImage)
const trafficLevel = computed(() => {
  const views = attraction.value?.viewCount || 0
  if (views > 30000) return '较多（每日约3.2万人次）'
  if (views > 10000) return '适中（每日约1.2万人次）'
  return '舒适（每日约5000人次）'
})

async function fetchDetail() {
  loading.value = true
  error.value = ''
  currentImageIndex.value = 0
  isFavorited.value = false

  try {
    const id = route.params.id
    const [detail, recData, commentData, favStatus, guideData] = await Promise.all([
      getAttractionById(id),
      getRecommendations(id, 4).catch(() => []),
      getComments(id, { page: 1, pageSize: 10 }).catch(() => []),
      checkFavorited(id).catch(() => false),
      getGuidesByAttraction(id, 4).catch(() => [])
    ])
    attraction.value = normalizeAttraction(detail)
    recommendations.value = normalizeAttractionList(recData, []).slice(0, 4)
    relatedGuides.value = (guideData?.data || guideData || []).slice(0, 4)
    const { rows, total } = getPageRows(commentData)
    comments.value = rows
    commentTotal.value = total
    isFavorited.value = !!favStatus
  } catch {
    const fallback = fallbackAttractions[2]
    attraction.value = normalizeAttraction({ ...fallback, id: route.params.id })
    recommendations.value = normalizeAttractionList(fallbackAttractions.filter((item) => item.id !== fallback.id), []).slice(0, 4)
    comments.value = []
    commentTotal.value = 0
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    router.push('/login')
    return
  }
  if (favoriteToggling.value || !attraction.value) return
  favoriteToggling.value = true
  try {
    if (isFavorited.value) {
      await removeFavorite(attraction.value.id)
      isFavorited.value = false
    } else {
      await addFavorite(attraction.value.id)
      isFavorited.value = true
    }
  } catch {
    // handled by interceptor
  } finally {
    favoriteToggling.value = false
  }
}

async function submitComment() {
  if (!commentForm.content.trim()) return
  commentSubmitting.value = true
  try {
    await addComment({
      attractionId: attraction.value.id,
      rating: commentForm.rating,
      content: commentForm.content
    })
    const data = await getComments(attraction.value.id, { page: 1, pageSize: 10 })
    const { rows, total } = getPageRows(data)
    comments.value = rows
    commentTotal.value = total
    commentForm.content = ''
    commentForm.rating = 5
    alert('评价成功')
  } catch {
    alert('评价失败，请稍后重试')
  } finally {
    commentSubmitting.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`
  return d.toLocaleDateString('zh-CN')
}

function copyLink() {
  navigator.clipboard?.writeText(window.location.href)
  alert('链接已复制')
}

const aiChatStore = useAiChatStore()

// 景区数据加载后设置 AI 上下文
watch(attraction, (val) => {
  if (val && val.name) {
    aiChatStore.setContext({
      name: val.name,
      price: val.price || 0,
      rating: val.rating || 0,
      desc: (val.description || '').substring(0, 200)
    })
  }
})

onMounted(fetchDetail)
onUnmounted(() => aiChatStore.clearContext())
watch(() => route.params.id, () => route.params.id && fetchDetail())
</script>

<style lang="scss" scoped>
.detail-page {
  background: linear-gradient(180deg, #f8fbf6 0%, #ffffff 42%);
  min-height: 100vh;
  padding-top: 18px;
}

.detail-skeleton {
  height: 560px;
}

.detail-hero-wrap {
  position: relative;
}

.detail-hero {
  position: relative;
  height: 360px;
  overflow: hidden;
  border-radius: 8px 8px 0 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.hero-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.28), rgba(0,0,0,0.04) 58%, rgba(0,0,0,0.12));
}

.hero-actions {
  position: absolute;
  top: 22px;
  left: 22px;
  right: 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.round-btn,
.round-btn,
.ghost-action {
  height: 40px;
  border-radius: 999px;
  color: #fff;
  background: rgba(20, 37, 27, 0.34);
  backdrop-filter: blur(12px);
}

.round-btn {
  width: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.ghost-action {
  padding: 0 14px;
  margin-left: 8px;
  font-weight: 700;
  transition: var(--transition);

  &.favorited {
    color: #ff4757;
    background: rgba(255, 71, 87, 0.18);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.image-count {
  position: absolute;
  right: 24px;
  bottom: 20px;
  color: #fff;
  background: rgba(20, 37, 27, 0.42);
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 0.86rem;
}

.detail-shell {
  margin-top: 0;
  position: relative;
  z-index: 2;
  background: #fff;
  border: 1px solid rgba(31,143,58,0.08);
  border-top: 0;
  border-radius: 0 0 8px 8px;
  padding: 24px 24px 28px;
  box-shadow: var(--page-shadow);
}

.detail-card,
.comments-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(31, 143, 58, 0.1);
  border-radius: 8px;
  box-shadow: none;
}

.detail-card {
  padding: 0;
  border: 0;
}

.title-row {
  display: grid;
  grid-template-columns: 1fr 158px;
  gap: 22px;

  h1 {
    color: var(--dark);
    font-size: 2rem;
    margin: 10px 0 8px;
    letter-spacing: 0;
  }
}

.hero-hot-badge {
  position: absolute;
  left: 22px;
  top: 86px;
  display: inline-flex;
  border-radius: 999px;
  padding: 7px 13px;
  background: rgba(255, 245, 225, 0.95);
  color: #b96a00;
  font-weight: 800;
  font-size: 0.84rem;
  z-index: 2;
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--text-light);
  font-size: 0.92rem;

  span {
    display: inline-flex;
    align-items: center;
  }
}

.tag-line {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 4px 0 12px;

  span {
    display: inline-flex;
    align-items: center;
    min-height: 26px;
    padding: 0 10px;
    border-radius: 999px;
    color: var(--primary-dark);
    background: rgba(31, 143, 58, 0.1);
    font-size: 0.82rem;
    font-weight: 800;
  }
}

.rating-box {
  border: 1px solid rgba(31, 143, 58, 0.1);
  border-radius: 8px;
  padding: 16px;
  text-align: center;

  strong {
    display: block;
    color: var(--primary-dark);
    font-size: 2rem;
    line-height: 1;
    margin-bottom: 8px;
  }

  small {
    display: block;
    color: var(--text-light);
    margin-top: 8px;
  }
}

.summary-row {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 22px;
  margin: 24px 0;
}

.action-btns {
  display: flex;
  gap: 10px;
  align-items: center;
}

.nav-btn,
.ticket-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 86px;
  height: 38px;
  justify-content: center;
  font-weight: 800;
  border-radius: 999px;
  white-space: nowrap;
}

.ticket-btn {
  padding: 0 18px;
  color: #fff;
  background: linear-gradient(135deg, var(--accent), var(--accent-dark));
  box-shadow: 0 4px 14px rgba(245, 184, 75, 0.3);
  transition: var(--transition);

  span { font-size: 0.9rem; }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(245, 184, 75, 0.4);
  }
}

.summary {
  color: var(--dark-tertiary);
  line-height: 1.9;
  margin: 0;
}

.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 86px;
  height: 38px;
  justify-content: center;
  color: var(--primary-dark);
  font-weight: 800;
  border-radius: 999px;
  background: rgba(31, 143, 58, 0.08);

  span {
    color: var(--primary);
    font-size: 0.85rem;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border: 1px solid rgba(31, 143, 58, 0.1);
  border-radius: 8px;
  overflow: hidden;

  div {
    min-height: 82px;
    padding: 14px 16px;
    border-right: 1px solid rgba(31, 143, 58, 0.08);
    border-bottom: 1px solid rgba(31, 143, 58, 0.08);

    &:nth-child(4n) {
      border-right: 0;
    }
  }

  span {
    display: block;
    color: var(--text-lighter);
    font-size: 0.78rem;
    margin-bottom: 8px;
  }

  strong {
    color: var(--dark);
    font-size: 0.9rem;
  }

  em {
    color: var(--text-light);
    font-style: normal;
    font-size: 0.78rem;
  }

  .green {
    color: var(--primary);
  }
}

.gallery-title-row {
  margin-top: 26px;

  h2 {
    color: var(--dark);
    font-size: 1.18rem;
    font-weight: 900;
  }
}

.gallery-strip {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;

  button {
    height: 76px;
    border-radius: 8px;
    overflow: hidden;
    border: 2px solid transparent;

    &.active {
      border-color: var(--primary);
    }
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.all-images {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  margin: 12px auto 0;
  padding: 0 18px;
  border-radius: 999px;
  color: var(--primary-dark);
  font-size: 0.86rem;
  font-weight: 800;
}

.comment-submit {
  width: 100%;
  height: 42px;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;
  font-weight: 800;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;

  .section-title {
    margin-bottom: 0;
  }
}

.comments-section {
  margin-top: 24px;
}

.comments-card {
  padding: 20px 22px;
  box-shadow: var(--page-shadow);
}

.add-comment {
  textarea {
    width: 100%;
    border: 1px solid rgba(31, 143, 58, 0.14);
    border-radius: 8px;
    padding: 10px;
    resize: vertical;
    margin: 8px 0;
  }
}

.star-input {
  display: flex;
  gap: 4px;

  button {
    color: #d4ddd5;
    font-size: 1.2rem;

    &.active {
      color: var(--accent);
    }
  }
}

.login-hint {
  display: block;
  padding: 14px;
  border-radius: 8px;
  background: #f5faf4;
  color: var(--primary-dark);
  text-align: center;
  font-weight: 800;
}

.comment {
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid rgba(31, 143, 58, 0.08);
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;

  strong {
    color: var(--dark);
    font-size: 0.92rem;
  }
}

.comment-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 0.78rem;
}

.comment-stars {
  display: flex;
  gap: 2px;

  span {
    color: #d4ddd5;
    font-size: 0.9rem;

    &.active {
      color: #f5a623;
    }
  }
}

.comment-time {
  color: var(--text-lighter);
  font-size: 0.76rem;
  margin-left: auto;
}

.comment-body {
  color: var(--text);
  font-size: 0.9rem;
  line-height: 1.7;
  margin-top: 8px;
}

.empty-comments {
  color: var(--text-light);
  font-size: 0.86rem;
  margin-top: 14px;
}

.recommend-section {
  padding-top: 32px;
  padding-bottom: 64px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

/* 相关攻略 */
.guide-section {
  padding-bottom: 64px;
}

.guide-grid-detail {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.guide-mini-card {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(31, 143, 58, 0.08);
  box-shadow: 0 10px 28px rgba(26, 64, 38, 0.07);
  transition: var(--transition);
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 18px 44px rgba(26, 64, 38, 0.15);
    img { transform: scale(1.08); }
  }
}

.guide-mini-img {
  position: relative;
  height: 140px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.45s ease;
  }
}

.guide-mini-days {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(20,37,27,0.7);
  color: #fff;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 999px;
}

.guide-mini-body {
  padding: 12px 14px 14px;

  strong {
    display: block;
    font-size: 0.9rem;
    font-weight: 700;
    color: var(--dark);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-bottom: 4px;
  }

  span {
    font-size: 0.75rem;
    color: var(--text-lighter);
  }
}

@media (max-width: 768px) {
  .detail-hero {
    height: 300px;
  }

  .detail-shell {
    padding: 18px;
  }

  .title-row,
  .summary-row {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: repeat(2, 1fr);

    div:nth-child(2n) {
      border-right: 0;
    }
  }

  .gallery-strip,
  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
