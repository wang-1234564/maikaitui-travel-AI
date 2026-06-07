<template>
  <div id="app">
    <AppHeader v-if="!isImmersive" />
    <main class="main-content" :class="{ 'is-immersive': isImmersive }">
      <router-view v-slot="{ Component, route }">
        <transition :name="route.meta.transition || 'fade'" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <AppFooter v-if="!isImmersive" />
    <BackToTop />
    <FloatingAiChat />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import BackToTop from '@/components/common/BackToTop.vue'
import FloatingAiChat from '@/components/common/FloatingAiChat.vue'

const route = useRoute()
const isImmersive = computed(() => Boolean(route.meta.immersive))
</script>

<style lang="scss">
#app {
  display: flex;
  flex-direction: column;
  max-width: 1280px;
  min-height: calc(100vh - 28px);
  margin: 14px auto;
  overflow: hidden;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 20px 60px rgba(26, 64, 38, 0.1);
}

.main-content {
  flex: 1;
  min-width: 0;

  &.is-immersive {
    display: flex;
    flex-direction: column;
  }
}

@media (max-width: 1280px) {
  #app {
    margin: 0;
    border-radius: 0;
    min-height: 100vh;
  }
}
</style>
