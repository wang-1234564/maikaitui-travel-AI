<template>
  <div class="not-found-page">
    <div class="container section">
      <div class="empty-state">
        <div class="empty-icon">🔍</div>
        <h1 class="not-found-code">404</h1>
        <p class="empty-text">页面未找到</p>
        <p class="empty-hint">您访问的页面不存在或已被移除</p>
        <router-link to="/" class="btn-primary" style="margin-top: 28px;">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.not-found-code {
  font-size: 6rem;
  font-weight: 900;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 12px;
}
</style>
