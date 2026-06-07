<template>
  <div class="search-shell" :class="{ 'is-large': large }">
    <div class="search-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        :class="{ active: activeTab === tab.value }"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="search-bar">
      <div class="search-input-wrapper">
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <path d="M21 21l-4.35-4.35"/>
        </svg>
        <input
          v-model="keyword"
          type="text"
          :placeholder="placeholder"
          class="search-input"
          @keyup.enter="handleSearch"
        />
      </div>

      <select v-model="regionId" class="search-select" v-if="showFilters">
        <option value="">地点</option>
        <option v-for="region in regions" :key="region.id" :value="region.id">
          {{ region.name }}
        </option>
      </select>

      <select v-model="categoryId" class="search-select" v-if="showFilters">
        <option value="">类型</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">
          {{ cat.name }}
        </option>
      </select>

      <button class="search-btn" @click="handleSearch">
        搜索
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRegions, getCategories } from '@/api'
import { fallbackCategories, flattenTree } from '@/utils/travel'

const props = defineProps({
  large: {
    type: Boolean,
    default: false
  },
  showFilters: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['search'])

const router = useRouter()
const keyword = ref('')
const regionId = ref('')
const categoryId = ref('')
const regions = ref([])
const categories = ref([])
const activeTab = ref('scenic')

const tabs = [
  { label: '找景区', value: 'scenic', placeholder: '输入景区名称/关键词' },
  { label: '找景点', value: 'spot', placeholder: '输入景点名称/关键词' },
  { label: '找攻略', value: 'guide', placeholder: '输入城市、主题或攻略关键词' }
]

const placeholder = computed(() => tabs.find((item) => item.value === activeTab.value)?.placeholder || '搜索景区/景点/攻略')

function handleSearch() {
  const params = {}
  if (keyword.value.trim()) params.keyword = keyword.value.trim()
  if (regionId.value) params.regionId = regionId.value
  if (categoryId.value) params.categoryId = categoryId.value

  emit('search', params)

  // Navigate to attraction list with query params
  const query = {}
  if (params.keyword) query.keyword = params.keyword
  if (params.regionId) query.regionId = params.regionId
  if (params.categoryId) query.categoryId = params.categoryId

  router.push({ name: 'AttractionList', query })
}

async function fetchFilters() {
  try {
    const [regionsData, categoriesData] = await Promise.all([
      getRegions(),
      getCategories()
    ])
    regions.value = flattenTree(regionsData).filter((item) => item.id && item.name)
    categories.value = flattenTree(categoriesData).filter((item) => item.id && item.name)
  } catch (e) {
    regions.value = [
      { id: 2, name: '北京' },
      { id: 4, name: '浙江' },
      { id: 7, name: '四川' },
      { id: 9, name: '云南' },
      { id: 11, name: '海南' }
    ]
    categories.value = fallbackCategories
  }
}

onMounted(() => {
  if (props.showFilters) {
    fetchFilters()
  }
})
</script>

<style lang="scss" scoped>
.search-shell {
  width: 100%;
  max-width: 760px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--page-shadow);
  border: 1px solid rgba(31, 143, 58, 0.08);
  overflow: hidden;
}

.search-tabs {
  display: flex;
  align-items: center;
  height: 40px;
  background: rgba(246, 248, 244, 0.92);

  button {
    height: 100%;
    padding: 0 42px;
    color: var(--text-light);
    font-weight: 700;
    font-size: 0.9rem;
    border-right: 1px solid rgba(31, 143, 58, 0.08);

    &.active {
      background: var(--white);
      color: var(--primary-dark);
      box-shadow: inset 0 3px 0 var(--primary);
    }
  }
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  transition: var(--transition);
}

.search-shell.is-large .search-bar {
  padding: 12px 16px;
}

.search-shell.is-large {
  .search-input { font-size: 0.98rem; height: 40px; }
  .search-select { height: 40px; }
  .search-btn { height: 40px; padding: 0 30px; }
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  padding: 0 10px 0 2px;
}

.search-icon {
  color: var(--text-light);
  flex-shrink: 0;
  margin-right: 8px;
}

.search-input {
  flex: 1;
  height: 38px;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  color: var(--text);
  min-width: 0;

  &::placeholder {
    color: var(--text-lighter);
  }
}

.search-select {
  height: 38px;
  padding: 0 12px;
  border-radius: 0;
  background: var(--white);
  color: var(--text);
  font-size: 0.85rem;
  cursor: pointer;
  border-left: 1px solid rgba(31, 143, 58, 0.12);
  margin-right: 6px;
  min-width: 100px;
  appearance: none;
  -webkit-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1l4 4 4-4' stroke='%23636E72' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 28px;

  &:focus {
    border-color: var(--primary);
  }
}

.search-btn {
  height: 38px;
  padding: 0 24px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: var(--white);
  border: none;
  border-radius: 999px;
  font-size: 0.95rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: var(--transition);
  flex-shrink: 0;

  &:hover {
    opacity: 0.9;
    transform: scale(1.02);
  }
}

@media (max-width: 600px) {
  .search-tabs {
    overflow-x: auto;

    button {
      flex: 1;
      min-width: 96px;
      padding: 0 12px;
    }
  }

  .search-bar {
    flex-wrap: wrap;
    padding: 10px;
  }

  .search-input-wrapper {
    width: 100%;
  }

  .search-select {
    flex: 1;
    margin-top: 6px;
    margin-right: 6px;
  }

  .search-btn {
    margin-top: 6px;
  }
}
</style>
