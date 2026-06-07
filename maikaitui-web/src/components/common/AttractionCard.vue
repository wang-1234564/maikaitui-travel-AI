<template>
  <div class="attraction-card card-hover" @click="goDetail">
    <div class="card-image">
      <img :src="viewData.coverImage || defaultImage" :alt="viewData.name" loading="lazy" />
      <div class="image-overlay"></div>

      <span class="category-badge" v-if="viewData.categoryName">{{ viewData.categoryName }}</span>

      <!-- Favorite Button -->
      <button
        class="fav-btn"
        :class="{ favorited: isFav }"
        @click.stop="toggleFavorite"
        :title="isFav ? '取消收藏' : '收藏'"
      >
        {{ isFav ? '♥' : '♡' }}
      </button>
    </div>

    <div class="card-info">
      <h3 class="card-name">{{ viewData.name }}</h3>
      <p class="card-region" v-if="viewData.regionName">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 10c0 5-8 12-8 12S4 15 4 10a8 8 0 1 1 16 0Z"/>
          <circle cx="12" cy="10" r="3"/>
        </svg>
        {{ viewData.regionName }}
      </p>
      <div class="card-bottom">
        <div class="rating" v-if="viewData.rating !== undefined">
          <StarRating :rating="viewData.rating" size="small" />
          <span class="rating-score">{{ viewData.rating.toFixed(1) }}</span>
        </div>
        <div class="price" :class="{ free: !viewData.price }">
          <span>{{ formatPrice(viewData.price) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { addFavorite, removeFavorite } from '@/api'
import { normalizeAttraction, formatPrice } from '@/utils/travel'
import StarRating from './StarRating.vue'

const props = defineProps({
  attraction: {
    type: Object,
    required: true
  },
  favorited: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['toggle-favorite'])

const router = useRouter()
const userStore = useUserStore()

const isFav = ref(props.favorited)
const viewData = computed(() => normalizeAttraction(props.attraction))

const defaultImage = computed(() => {
  const seed = props.attraction.id || props.attraction.name || 'default'
  return `https://picsum.photos/seed/${seed}/600/400`
})

function goDetail() {
  router.push(`/attraction/${viewData.value.id}`)
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    alert('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isFav.value) {
      await removeFavorite(props.attraction.id)
      isFav.value = false
    } else {
      await addFavorite(props.attraction.id)
      isFav.value = true
    }
    emit('toggle-favorite', { id: props.attraction.id, favorited: isFav.value })
  } catch (e) {
    // Error handled by request interceptor
  }
}
</script>

<style lang="scss" scoped>
.attraction-card {
  background: var(--white);
  border-radius: var(--card-radius);
  overflow: hidden;
  border: 1px solid rgba(31, 143, 58, 0.08);
  box-shadow: 0 10px 30px rgba(26, 64, 38, 0.08);
}

.card-image {
  position: relative;
  width: 100%;
  height: 150px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;
  }

  .image-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.5) 0%, transparent 50%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    img {
      transform: scale(1.08);
    }

    .image-overlay {
      opacity: 1;
    }
  }
}

.category-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--primary-dark);
}

.fav-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.15rem;
  color: var(--primary);
  transition: var(--transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &:hover {
    transform: scale(1.15);
    background: var(--white);
  }

  &.favorited {
    animation: heartBeat 0.4s ease;
  }
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  30% { transform: scale(1.3); }
  60% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

.card-info {
  padding: 14px;
}

.card-name {
  font-size: 0.98rem;
  font-weight: 800;
  color: var(--dark);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.card-region {
  font-size: 0.8rem;
  color: var(--text-light);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 4px;

  min-height: 20px;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rating {
  display: flex;
  align-items: center;
  gap: 6px;

  .rating-score {
    font-size: 0.85rem;
    font-weight: 600;
    color: var(--primary);
  }
}

.price {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--primary);

  &.free {
    color: var(--secondary);
    font-size: 0.95rem;
  }
}
</style>
