import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    activeMenu: ''
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setActiveMenu(menu) {
      this.activeMenu = menu
    }
  },
  persist: {
    key: 'maikaitui-app',
    storage: localStorage,
    paths: ['sidebarCollapsed']
  }
})
