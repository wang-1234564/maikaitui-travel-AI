import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: {
      id: null,
      username: '',
      nickname: '',
      avatar: '',
      roles: [],
    },
    permissions: []
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    async login(username, password) {
      try {
        const res = await loginApi({ username, password })
        if (res.code === 200) {
          this.token = res.data.token
          this.userInfo = res.data.userInfo || res.data
          localStorage.setItem('token', this.token)
          ElMessage.success('登录成功')
          return true
        }
        return false
      } catch (error) {
        return false
      }
    },

    async getUserInfo() {
      try {
        const res = await getUserInfoApi()
        if (res.code === 200) {
          this.userInfo = res.data
          this.permissions = res.data.permissions || []
        }
      } catch (error) {
        // ignore
      }
    },

    logout() {
      this.token = ''
      this.userInfo = { id: null, username: '', nickname: '', avatar: '', roles: [] }
      this.permissions = []
      localStorage.removeItem('token')
      ElMessage.success('已退出登录')
    }
  },
  persist: {
    key: 'maikaitui-user',
    storage: localStorage,
    paths: ['token', 'userInfo', 'permissions']
  }
})
