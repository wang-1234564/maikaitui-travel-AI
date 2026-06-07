<template>
  <el-container class="layout-container">
    <el-aside :width="asideWidth" class="layout-aside">
      <Sidebar :menu-routes="menuRoutes" />
    </el-aside>
    <el-container class="layout-right">
      <el-header height="60px" class="layout-header">
        <HeaderBar />
      </el-header>
      <el-main class="layout-main">
        <router-view v-slot="{ Component, route: r }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" :key="r.fullPath" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import Sidebar from './Sidebar.vue'
import HeaderBar from './HeaderBar.vue'

const router = useRouter()
const appStore = useAppStore()

const asideWidth = computed(() => (appStore.sidebarCollapsed ? '64px' : '220px'))

const cachedViews = computed(() => {
  return [
    'Dashboard',
    'UserManagement',
    'RoleManagement',
    'MenuManagement',
    'DictManagement',
    'LogManagement',
    'AttractionManagement',
    'RegionManagement',
    'CategoryManagement',
    'OrderManagement',
    'CommentManagement',
    'AiChat'
  ]
})

const menuRoutes = computed(() => {
  return router.options.routes.filter((r) => r.name !== 'Login')
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.layout-aside {
  background: var(--sidebar-bg);
  overflow: hidden;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.layout-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  padding: 0;
  height: 60px;
  line-height: 60px;
}

.layout-main {
  background: var(--main-bg);
  padding: 16px;
  overflow-y: auto;
  flex: 1;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.2s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>
