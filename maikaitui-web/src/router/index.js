import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/Home.vue'),
    meta: { transition: 'fade' }
  },
  {
    path: '/attractions',
    name: 'AttractionList',
    component: () => import('@/views/attraction/AttractionList.vue'),
    meta: { transition: 'slide' }
  },
  {
    path: '/attraction/:id',
    name: 'AttractionDetail',
    component: () => import('@/views/attraction/AttractionDetail.vue'),
    meta: { transition: 'slide', immersive: true }
  },
  {
    path: '/attraction/:id/nav',
    name: 'MapNav',
    component: () => import('@/views/attraction/MapNav.vue'),
    meta: { transition: 'slide', immersive: true }
  },
  {
    path: '/guides',
    name: 'GuideList',
    component: () => import('@/views/guide/GuideList.vue'),
    meta: { transition: 'slide' }
  },
  {
    path: '/guide/:id',
    name: 'GuideDetail',
    component: () => import('@/views/guide/GuideDetail.vue'),
    meta: { transition: 'slide', immersive: true }
  },
  {
    path: '/user/profile',
    name: 'UserProfile',
    component: () => import('@/views/user/UserProfile.vue'),
    meta: { requiresAuth: true, transition: 'fade' }
  },
  {
    path: '/user/orders',
    name: 'UserOrders',
    component: () => import('@/views/user/UserOrders.vue'),
    meta: { requiresAuth: true, transition: 'fade' }
  },
  {
    path: '/user/favorites',
    name: 'UserFavorites',
    component: () => import('@/views/user/UserFavorites.vue'),
    meta: { requiresAuth: true, transition: 'fade' }
  },
  {
    path: '/ai',
    name: 'AiChat',
    component: () => import('@/views/ai/AiChat.vue'),
    meta: { transition: 'fade', immersive: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { transition: 'fade' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { transition: 'fade' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0, behavior: 'smooth' }
  }
})

// Navigation guard for auth-required routes
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})

export default router
