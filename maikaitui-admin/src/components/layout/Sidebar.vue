<template>
  <div class="sidebar-container">
    <div class="sidebar-logo" @click="$router.push('/dashboard')">
      <svg class="sidebar-logo-img" viewBox="0 0 32 32" width="32" height="32">
        <circle cx="16" cy="16" r="14" fill="#667eea"/>
        <path d="M10 21 Q16 6 22 21" fill="none" stroke="white" stroke-width="2"/>
        <path d="M8 15 L24 15" fill="none" stroke="white" stroke-width="1.5" opacity="0.6"/>
        <circle cx="16" cy="8" r="3" fill="white" opacity="0.8"/>
      </svg>
      <span v-show="!collapsed" class="sidebar-logo-text">迈开腿</span>
    </div>
    <el-scrollbar>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        background-color="var(--sidebar-bg)"
        text-color="var(--sidebar-text)"
        active-text-color="var(--sidebar-active)"
        router
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <template v-if="route.children && route.children.length > 0 && !route.hidden">
            <el-sub-menu :index="resolvePath(route.path)">
              <template #title>
                <el-icon v-if="route.meta?.icon">
                  <component :is="route.meta.icon" />
                </el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in route.children"
                :key="child.path"
                :index="resolvePath(route.path, child.path)"
              >
                <el-icon v-if="child.meta?.icon">
                  <component :is="child.meta.icon" />
                </el-icon>
                <span>{{ child.meta?.title }}</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
          <template v-else-if="!route.hidden">
            <el-menu-item :index="resolvePath(route.path)">
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </el-menu-item>
          </template>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const appStore = useAppStore()

const props = defineProps({
  menuRoutes: {
    type: Array,
    required: true
  }
})

const collapsed = computed(() => appStore.sidebarCollapsed)

const activeMenu = computed(() => {
  const { path } = route
  if (path.startsWith('/system/')) return '/system/' + path.split('/')[2]
  if (path.startsWith('/tourism/')) return '/tourism/' + path.split('/')[2]
  return path
})

function resolvePath(parent, child) {
  if (child) {
    const base = parent.replace(/\/$/, '')
    return base + '/' + child.replace(/^\//, '')
  }
  return parent.replace(/\/$/, '') || '/'
}
</script>

<style scoped>
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--sidebar-bg);
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  overflow: hidden;
  white-space: nowrap;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  cursor: pointer;
  transition: background 0.2s;
}
.sidebar-logo:hover {
  background: rgba(255, 255, 255, 0.06);
}

.sidebar-logo-img {
  width: 32px;
  height: 32px;
  margin-right: 8px;
  border-radius: 4px;
}

.sidebar-logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 2px;
}

.el-menu {
  border-right: none;
}

.el-menu-item.is-active {
  background-color: var(--primary-color) !important;
  color: #fff !important;
}

:deep(.el-sub-menu__title:hover),
:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
}
</style>
