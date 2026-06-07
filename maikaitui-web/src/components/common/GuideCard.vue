<template>
  <div class="guide-card card-hover" @click="goDetail">
    <div class="card-img">
      <img :src="guide.coverImage || defaultCover" :alt="guide.title" loading="lazy" />
      <span class="duration-badge" v-if="guide.durationDays">{{ guide.durationDays }}天</span>
    </div>
    <div class="card-body">
      <h3 class="card-title">{{ guide.title }}</h3>
      <div class="card-meta">
        <span class="meta-dest" v-if="guide.destination">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
          {{ guide.destination }}
        </span>
        <span class="meta-season" v-if="guide.season">{{ seasonLabel(guide.season) }}</span>
      </div>
      <div class="card-footer">
        <span class="card-views">{{ viewCountText }}</span>
        <span class="card-arrow">去看看 →</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  guide: { type: Object, required: true }
})

const router = useRouter()
const defaultCover = 'https://picsum.photos/seed/guide-default/600/400'

const viewCountText = computed(() => {
  const v = props.guide.viewCount || 0
  if (v >= 10000) return `${(v / 10000).toFixed(1)}万次浏览`
  if (v >= 1000) return `${(v / 1000).toFixed(1)}k次浏览`
  return `${v}次浏览`
})

const seasonLabel = (s) => {
  const map = { '春': '🌸 春', '夏': '☀️ 夏', '秋': '🍂 秋', '冬': '❄️ 冬', '全年': '📅 全年' }
  return map[s] || s
}

function goDetail() {
  router.push(`/guide/${props.guide.id}`)
}
</script>

<style lang="scss" scoped>
.guide-card {
  cursor: pointer;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(31, 143, 58, 0.08);
  box-shadow: 0 10px 28px rgba(26, 64, 38, 0.07);
  overflow: hidden;
  transition: all 0.22s ease;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 18px 44px rgba(26, 64, 38, 0.15);
    border-color: rgba(31, 143, 58, 0.18);

    img { transform: scale(1.08); }
  }
}

.card-img {
  position: relative;
  overflow: hidden;
  height: 180px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.45s ease;
  }
}

.duration-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(20, 37, 27, 0.75);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 0.78rem;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 999px;
}

.card-body {
  padding: 16px 18px 18px;
}

.card-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--dark);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 10px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  span {
    font-size: 0.8rem;
    color: var(--text-light);
    display: inline-flex;
    align-items: center;
    gap: 4px;

    svg { flex-shrink: 0; }
  }

  .meta-dest { color: var(--primary-dark); font-weight: 600; }
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-views {
  font-size: 0.78rem;
  color: var(--text-lighter);
}

.card-arrow {
  font-size: 0.78rem;
  color: var(--primary);
  font-weight: 700;
}
</style>
