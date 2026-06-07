import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo as getUserInfoApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')

  // 安全解析：失败就给 null
  const safeParse = (str) => {
    try {
      const val = str === null ? null : JSON.parse(str)
      return val ?? null
    } catch (e) {
      console.error('userInfo parse error:', e)
      return null
    }
  }

  const userInfo = ref(safeParse(localStorage.getItem('userInfo')))

  const isLoggedIn = computed(() => !!token.value)

  async function login(credentials) {
    const data = await loginApi(credentials)
    token.value = data.token || data.accessToken || ''
    userInfo.value = data.user || data.userInfo || data
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return data
  }

  async function register(formData) {
    const data = await registerApi(formData)
    return data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const data = await getUserInfoApi()
      userInfo.value = data || null
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } catch (e) {
      logout()
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    logout,
    fetchUserInfo
  }
})