<template>
  <span class="star-rating" :class="[`size-${size}`]" :title="`${rating} / 5`">
    <span
      v-for="star in 5"
      :key="star"
      class="star"
      :class="{ filled: star <= filledStars, half: star === halfStar, empty: star > Math.ceil(rating) }"
    >
      <svg viewBox="0 0 24 24" :width="starSize" :height="starSize">
        <defs>
          <linearGradient :id="`starGrad-${star}`" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="#FFB563" />
            <stop offset="100%" stop-color="#FF8C42" />
          </linearGradient>
        </defs>
        <path
          d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"
          :fill="star <= filledStars ? `url(#starGrad-${star})` : (star === halfStar ? 'url(#starGradHalf)' : '#E2E8F0')"
          stroke="none"
        />
      </svg>
    </span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  rating: {
    type: Number,
    default: 0
  },
  size: {
    type: String,
    default: 'medium',
    validator: (v) => ['small', 'medium', 'large'].includes(v)
  }
})

const filledStars = computed(() => Math.floor(props.rating))
const halfStar = computed(() => props.rating % 1 >= 0.5 ? filledStars.value + 1 : 0)

const starSize = computed(() => {
  switch (props.size) {
    case 'small': return 14
    case 'large': return 24
    default: return 18
  }
})
</script>

<style lang="scss" scoped>
.star-rating {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.star {
  display: inline-flex;
  align-items: center;
  transition: var(--transition);
}

.size-large .star svg {
  width: 24px;
  height: 24px;
}

.size-medium .star svg {
  width: 18px;
  height: 18px;
}

.size-small .star svg {
  width: 14px;
  height: 14px;
}
</style>
