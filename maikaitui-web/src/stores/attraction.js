import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getHotAttractions as getHotApi,
  getAttractionById as getByIdApi,
  getRecommendations as getRecApi,
  getAttractions as getListApi
} from '@/api'

export const useAttractionStore = defineStore('attraction', () => {
  const hotAttractions = ref([])
  const currentAttraction = ref(null)
  const recommendations = ref([])

  async function fetchHotAttractions(limit = 10) {
    const data = await getHotApi(limit)
    hotAttractions.value = Array.isArray(data) ? data : (data.records || data.list || [])
    return hotAttractions.value
  }

  async function fetchAttractionById(id) {
    const data = await getByIdApi(id)
    currentAttraction.value = data
    return data
  }

  async function fetchRecommendations(attractionId, limit = 6) {
    const data = await getRecApi(attractionId, limit)
    recommendations.value = Array.isArray(data) ? data : (data.records || data.list || [])
    return recommendations.value
  }

  async function fetchAttractions(params = {}) {
    const data = await getListApi(params)
    return data
  }

  return {
    hotAttractions,
    currentAttraction,
    recommendations,
    fetchHotAttractions,
    fetchAttractionById,
    fetchRecommendations,
    fetchAttractions
  }
})
