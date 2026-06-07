import { reactive } from 'vue'

const safeGetStorage = (key) => {
  try { return uni.getStorageSync(key) || '' } catch (e) { return '' }
}
const safeSetStorage = (key, value) => {
  try { uni.setStorageSync(key, value) } catch (e) { /* ignore */ }
}
const safeRemoveStorage = (key) => {
  try { uni.removeStorageSync(key) } catch (e) { /* ignore */ }
}

export const store = reactive({
  token: safeGetStorage('token'),
  userInfo: (() => { try { return JSON.parse(safeGetStorage('userInfo') || 'null') } catch (e) { return null } })(),
  isLoggedIn: false,

  setToken(token) {
    this.token = token
    this.isLoggedIn = !!token
    safeSetStorage('token', token)
  },

  setUserInfo(info) {
    this.userInfo = info
    safeSetStorage('userInfo', JSON.stringify(info))
  },

  login(token, userInfo) {
    this.setToken(token)
    if (userInfo) this.setUserInfo(userInfo)
    this.isLoggedIn = !!token
  },

  logout() {
    this.token = ''
    this.userInfo = null
    this.isLoggedIn = false
    safeRemoveStorage('token')
    safeRemoveStorage('userInfo')
  },

  checkLogin() {
    this.isLoggedIn = !!this.token
  },

  // Guest mode: user can browse without login
  isGuestMode: true
})

// Init login state on load
store.checkLogin()
