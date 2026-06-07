<template>
  <div class="home-page">
    <section class="hero-wrap">
      <div class="hero-section">
        <div class="hero-media"></div>
        <div class="hero-mask"></div>
        <div class="paper-plane" aria-hidden="true">
          <svg viewBox="0 0 86 54" fill="none">
            <path
              d="M4 31C26 19 48 13 80 5"
              stroke="#2a9845"
              stroke-width="2"
              stroke-dasharray="6 6"
            />
            <path
              d="M58 7 82 4 68 24 64 14 58 7Z"
              fill="white"
              stroke="#2a9845"
              stroke-width="2"
            />
          </svg>
        </div>
        <div class="hero-content">
          <h1>
            <span class="brand-script">迈开腿</span>
            <span>去热爱这个世界</span>
          </h1>
          <p>探索自然 · 遇见美好 · 开启旅程</p>
        </div>
        <div class="hero-search">
          <SearchBar :large="true" @search="onSearch" />
        </div>
      </div>
    </section>

    <section class="container home-board">
      <div class="board-main">
        <div class="section-head">
          <h2 class="section-title">热门景区</h2>
          <router-link to="/attractions" class="text-link"
            >查看更多</router-link
          >
        </div>
        <div class="hot-grid" v-if="hotAttractions.length">
          <router-link
            v-for="item in hotAttractions"
            :key="item.id"
            :to="`/attraction/${item.id}`"
            class="hot-card"
          >
            <img :src="item.coverImage" :alt="item.name" loading="lazy" />
            <div class="hot-card-body">
              <strong>{{ item.name }}</strong>
              <span>{{ item.regionName }}</span>
              <div>
                <small>★ {{ item.rating.toFixed(1) }}</small>
                <small>{{ formatPrice(item.price) }}</small>
              </div>
            </div>
          </router-link>
        </div>
        <div class="hot-grid" v-else>
          <div v-for="n in 5" :key="n" class="skeleton-card skeleton"></div>
        </div>

        <div class="category-panel">
          <div class="section-head compact">
            <h2 class="section-title">景区分类</h2>
          </div>
          <div class="category-grid">
            <router-link
              v-for="cat in categories"
              :key="cat.id"
              :to="`/attractions?categoryId=${cat.id}`"
              class="category-card"
            >
              <span
                class="category-icon"
                aria-hidden="true"
                v-html="cat.iconSvg"
              ></span>
              <strong>{{ cat.name }}</strong>
              <small>{{ cat.desc }}</small>
            </router-link>
          </div>
        </div>

        <!-- 精选攻略 -->
        <div class="guide-panel" v-if="hotGuides.length">
          <div class="section-head">
            <h2 class="section-title">精选攻略</h2>
            <router-link to="/guides" class="text-link">查看更多</router-link>
          </div>
          <div class="guide-grid">
            <div
              v-for="item in hotGuides"
              :key="item.id"
              class="guide-mini-card"
              @click="$router.push(`/guide/${item.id}`)"
            >
              <div class="guide-mini-img">
                <img :src="item.coverImage" :alt="item.title" loading="lazy" />
                <span class="guide-mini-days" v-if="item.durationDays">{{ item.durationDays }}天</span>
              </div>
              <div class="guide-mini-body">
                <strong>{{ item.title }}</strong>
                <span>{{ item.destination }} · {{ item.viewCount || 0 }}次浏览</span>
              </div>
            </div>
          </div>
        </div>

        <div class="growth-banner">
          <div>
            <strong>每一次旅行，都是成长</strong>
            <span>迈开腿，去遇见更好的风景</span>
          </div>
          <router-link to="/ai" class="banner-btn">立即出发</router-link>
        </div>
      </div>

      <aside class="board-side">
        <div class="ai-card">
          <div>
            <span class="side-label">AI 旅行助手</span>
            <h3>不确定去哪？让 AI 帮你规划。</h3>
            <p>输入预算、天数、同行人和兴趣偏好，快速生成目的地与路线建议。</p>
            <router-link to="/ai" class="ai-btn">开始对话</router-link>
          </div>
          <div class="robot">
            <img src="/images/ai-robot.png" alt="AI 旅行助手" />
          </div>
        </div>

        <div class="notice-card">
          <div class="section-head compact">
            <h2 class="section-title">平台公告</h2>
            <span class="text-link muted">查看更多</span>
          </div>
          <ul>
            <li v-for="notice in notices" :key="notice.title">
              <span>{{ notice.title }}</span>
              <time>{{ notice.date }}</time>
            </li>
          </ul>
        </div>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import SearchBar from "@/components/common/SearchBar.vue";
import { getCategories, getHotAttractions, getHotGuides } from "@/api";
import {
  fallbackAttractions,
  fallbackCategories,
  flattenTree,
  formatPrice,
  normalizeAttractionList,
} from "@/utils/travel";

const router = useRouter();
const hotAttractions = ref([]);
const categories = ref(fallbackCategories);
const hotGuides = ref([]);

const notices = [
  { title: "五一假期热门景区出行提示", date: "04-28" },
  { title: "关于景区预约政策调整通知", date: "04-20" },
  { title: "迈开腿 AI 对话功能上线啦", date: "04-15" },
];

const iconSvgMap = [
  '<svg viewBox="0 0 36 36" fill="none"><path d="M5 28 15 10l7 12 4-7 6 13H5Z" fill="currentColor" opacity=".9"/><circle cx="27" cy="8" r="3" fill="currentColor" opacity=".35"/></svg>',
  '<svg viewBox="0 0 36 36" fill="none"><path d="M6 14 18 7l12 7H6Z" fill="currentColor"/><path d="M9 16h18M10 27h16M12 17v10M18 17v10M24 17v10" stroke="currentColor" stroke-width="2.6" stroke-linecap="round"/></svg>',
  '<svg viewBox="0 0 36 36" fill="none"><path d="M8 27V13h8v14M20 27V8h8v19" stroke="currentColor" stroke-width="3" stroke-linejoin="round"/><path d="M11 17h2M11 21h2M23 13h2M23 17h2M23 21h2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>',
  '<svg viewBox="0 0 36 36" fill="none"><path d="M6 18a12 12 0 0 1 24 0H6Z" fill="currentColor"/><path d="M18 18v9a4 4 0 0 0 7 2" stroke="currentColor" stroke-width="2.8" stroke-linecap="round"/></svg>',
  '<svg viewBox="0 0 36 36" fill="none"><circle cx="14" cy="13" r="4" fill="currentColor"/><circle cx="24" cy="14" r="3.5" fill="currentColor" opacity=".72"/><path d="M7 28c1.2-5 4-8 7-8s5.8 3 7 8M19 28c.8-4 2.8-6.5 5-6.5s4.2 2.5 5 6.5" stroke="currentColor" stroke-width="2.6" stroke-linecap="round"/></svg>',
  '<svg viewBox="0 0 36 36" fill="none"><circle cx="18" cy="18" r="12" stroke="currentColor" stroke-width="3"/><path d="m22 12-2.6 9.4L14 24l2.6-9.4L22 12Z" fill="currentColor"/></svg>',
];

function onSearch(params) {
  router.push({ name: "AttractionList", query: params });
}

function decorateCategories(list) {
  const source = list.length ? list : fallbackCategories;
  return source.slice(0, 6).map((item, index) => ({
    ...item,
    iconSvg: iconSvgMap[index % iconSvgMap.length],
    desc:
      item.desc ||
      fallbackCategories[index % fallbackCategories.length]?.desc ||
      "精选玩法",
  }));
}

onMounted(async () => {
  try {
    const [hotData, categoryData, guideData] = await Promise.all([
      getHotAttractions(5),
      getCategories().catch(() => []),
      getHotGuides(3).catch(() => []),
    ]);
    hotAttractions.value = normalizeAttractionList(
      hotData,
      fallbackAttractions,
    ).slice(0, 5);
    categories.value = decorateCategories(flattenTree(categoryData));
    hotGuides.value = guideData?.data || guideData || [];
  } catch {
    hotAttractions.value = normalizeAttractionList(
      [],
      fallbackAttractions,
    ).slice(0, 5);
    categories.value = decorateCategories([]);
    hotGuides.value = [];
  }
});
</script>

// ... existing code ...

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fbfcfa 0%, #f6f9f3 42%, #ffffff 100%);
}

.hero-wrap {
  padding-top: 0;
}

.hero-section {
  position: relative;
  min-height: 420px;
  overflow: visible; /* 或者删掉 overflow: hidden */
  border-radius: 0;
  box-shadow: 0 20px 56px rgba(26, 64, 38, 0.1);
  background: #e8f4e2;
  padding-bottom: 50px; /* 给搜索栏预留高度 */
}

.hero-media,
.hero-mask {
  position: absolute;
  inset: 0;
}

.hero-media {
  background: url("public/images/home-hero.png") center / cover no-repeat;
}

.hero-mask {
  background:
    linear-gradient(
      90deg,
      rgba(229, 244, 222, 0.6),
      transparent 22%,
      transparent 78%,
      rgba(229, 244, 222, 0.45)
    ),
    linear-gradient(
      180deg,
      rgba(255, 255, 255, 0.06),
      rgba(246, 248, 244, 0.62) 100%
    );
}

.hero-content {
  position: relative;
  z-index: 2;
  min-height: 380px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding-bottom: 60px;

  h1 {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #111f16;
    font-size: 2.4rem;
    line-height: 1.15;
    font-weight: 800;
    letter-spacing: 0;
    margin: 0;
  }

  p {
    margin-top: 18px;
    display: inline-flex;
    padding: 10px 28px;
    border-radius: 999px;
    color: #fff;
    background: linear-gradient(135deg, var(--primary), var(--primary-dark));
    font-weight: 800;
    font-size: 1rem;
    box-shadow: 0 8px 24px rgba(31, 143, 58, 0.25);
  }
}

.brand-script {
  font-family: "KaiTi", "STKaiti", "Noto Serif SC", serif;
  color: var(--primary-dark);
  font-size: 4.8rem;
  line-height: 1;
  letter-spacing: 0;
  text-shadow: 0 3px 0 rgba(255, 255, 255, 0.65);
}

.paper-plane {
  position: absolute;
  z-index: 2;
  top: 62px;
  left: 86px;
  width: 112px;
  opacity: 0.9;
}

.hero-search {
  /* 删掉 position: absolute; 相关的定位属性 */
  position: relative;
  z-index: 4;
  display: flex;
  justify-content: center;
  margin-top: -34px; /* 只保留这个负边距，让它往上贴一点 */
  padding: 0 108px; /* 原来的左右间距改为内边距 */
}

.home-board {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 32px;
  margin-top: 72px;
  padding-bottom: 64px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  &.compact {
    margin-bottom: 16px;
  }
}

.text-link {
  color: var(--text-light);
  font-size: 0.88rem;
  font-weight: 700;
  transition: var(--transition);

  &:hover {
    color: var(--primary);
  }
}

.text-link:not(.muted):hover {
  color: var(--primary);
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.hot-card {
  min-width: 0;
  overflow: hidden;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(31, 143, 58, 0.08);
  box-shadow: 0 10px 28px rgba(26, 64, 38, 0.07);
  transition: var(--transition);

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 18px 44px rgba(26, 64, 38, 0.15);
    border-color: rgba(31, 143, 58, 0.18);

    img {
      transform: scale(1.08);
    }
  }

  img {
    width: 100%;
    height: 140px;
    object-fit: cover;
    transition: transform 0.45s ease;
  }
}

.hot-card-body {
  padding: 12px 12px 14px;

  strong,
  span {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: var(--dark);
    font-size: 0.95rem;
    line-height: 1.35;
    font-weight: 700;
  }

  span {
    color: var(--text-light);
    font-size: 0.8rem;
    margin-top: 4px;
  }

  div {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 10px;
  }

  small {
    color: var(--primary);
    font-size: 0.8rem;
    font-weight: 800;
  }
}

.skeleton-card {
  height: 250px;
}

.category-panel,
.notice-card,
.ai-card,
.growth-banner {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(31, 143, 58, 0.08);
  box-shadow: var(--page-shadow);
}

/* 精选攻略 */
.guide-panel {
  margin-top: 32px;
}

.guide-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.guide-mini-card {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(31, 143, 58, 0.08);
  box-shadow: 0 10px 28px rgba(26, 64, 38, 0.07);
  transition: var(--transition);
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 18px 44px rgba(26, 64, 38, 0.15);
    img { transform: scale(1.08); }
  }
}

.guide-mini-img {
  position: relative;
  height: 140px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.45s ease;
  }
}

.guide-mini-days {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(20,37,27,0.7);
  color: #fff;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 999px;
}

.guide-mini-body {
  padding: 12px 14px 14px;

  strong {
    display: block;
    font-size: 0.9rem;
    font-weight: 700;
    color: var(--dark);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-bottom: 4px;
  }

  span {
    font-size: 0.75rem;
    color: var(--text-lighter);
  }
}

.category-panel {
  margin-top: 32px;
  padding: 0;
  background: transparent;
  border: 0;
  box-shadow: none;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.category-card {
  min-height: 96px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: linear-gradient(180deg, #f4faf3, #ffffff);
  border: 1px solid rgba(31, 143, 58, 0.08);
  color: var(--dark);
  transition: var(--transition);
  padding: 12px 8px;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba(31, 143, 58, 0.22);
    box-shadow: 0 12px 32px rgba(31, 143, 58, 0.1);
  }

  strong {
    font-size: 0.95rem;
    font-weight: 700;
  }

  small {
    color: var(--text-light);
    font-size: 0.75rem;
  }
}

.category-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(
    135deg,
    rgba(31, 143, 58, 0.14),
    rgba(245, 184, 75, 0.16)
  );
  color: var(--primary);

  :deep(svg) {
    width: 24px;
    height: 24px;
    display: block;
  }
}

.growth-banner {
  width: 1230px;
  margin-top: 28px;
  min-height: 100px;
  padding: 24px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background:
    linear-gradient(
      90deg,
      rgba(211, 236, 189, 0.96),
      rgba(255, 255, 255, 0.46)
    ),
    url("/images/growth-banner.png") center / cover no-repeat,
    url("https://picsum.photos/seed/maikaitui-camp/1200/260") center / cover
      no-repeat;
  border-radius: 12px;

  strong {
    display: block;
    color: var(--primary-dark);
    font-size: 1.5rem;
    font-weight: 900;
    line-height: 1.3;
  }

  span {
    color: var(--dark-tertiary);
    font-size: 0.9rem;
    margin-top: 4px;
    display: block;
  }
}

.banner-btn,
.ai-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  padding: 0 24px;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: var(--white);
  font-weight: 800;
  font-size: 0.92rem;
  transition: var(--transition);
  box-shadow: 0 6px 20px rgba(31, 143, 58, 0.25);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 28px rgba(31, 143, 58, 0.35);
  }
}

.board-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ai-card {
  min-height: 200px;
  padding: 24px 22px;
  background: linear-gradient(135deg, #f4fbf3, #e3f5f1);
  display: grid;
  grid-template-columns: 1fr 100px;
  gap: 12px;
  overflow: hidden;
  border-radius: 12px;

  h3 {
    color: var(--dark);
    font-size: 1.12rem;
    line-height: 1.4;
    margin: 10px 0;
    font-weight: 700;
  }

  p {
    color: var(--text-light);
    font-size: 0.86rem;
    margin-bottom: 16px;
    line-height: 1.6;
  }
}

.side-label {
  color: var(--primary-dark);
  font-size: 0.9rem;
  font-weight: 900;
}

.robot {
  align-self: end;
  width: 110px;
  height: 110px;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

.notice-card {
  padding: 22px;
  border-radius: 12px;

  li {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    padding: 14px 0;
    border-top: 1px solid rgba(31, 143, 58, 0.08);
    color: var(--dark-tertiary);
    font-size: 0.88rem;
    transition: var(--transition);

    &:first-child {
      border-top: none;
    }

    &:hover {
      color: var(--primary);
    }

    time {
      color: var(--text-lighter);
      flex-shrink: 0;
      font-size: 0.82rem;
    }
  }
}

@media (max-width: 1200px) {
  .home-board {
    grid-template-columns: 1fr;
  }

  .board-side {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }

  .hot-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 992px) {
  .hero-section {
    min-height: 380px;
  }

  .hero-content {
    min-height: 340px;
  }

  .brand-script {
    font-size: 4rem;
  }

  .hero-content h1 {
    font-size: 2rem;
  }

  .hot-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .category-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-wrap {
    padding-top: 0;
  }

  .hero-section {
    min-height: 450px;
  }

  .hero-content {
    min-height: 380px;
    padding: 24px 20px 100px;
  }

  .hero-content h1 {
    font-size: 1.75rem;
  }

  .brand-script {
    font-size: 3.2rem;
  }

  .paper-plane {
    left: 24px;
    top: 48px;
    width: 86px;
  }

  .hero-search {
    left: 16px;
    right: 16px;
    bottom: 18px;
  }

  .board-side {
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .home-board {
    margin-top: 36px;
    gap: 24px;
  }

  .hot-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .category-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .growth-banner {
    align-items: flex-start;
    flex-direction: column;
    gap: 16px;
    padding: 20px 24px;
  }

  .growth-banner strong {
    font-size: 1.3rem;
  }
}

@media (max-width: 480px) {
  .hero-section {
    min-height: 420px;
  }

  .hero-content h1 {
    font-size: 1.5rem;
  }

  .brand-script {
    font-size: 2.8rem;
  }

  .hot-grid {
    grid-template-columns: 1fr;
  }

  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
