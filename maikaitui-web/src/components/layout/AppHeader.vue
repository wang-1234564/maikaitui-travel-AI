<template>
  <header
    class="app-header"
    :class="{ 'is-scrolled': isScrolled, 'menu-open': mobileMenuOpen }"
  >
    <div class="header-inner container">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <span class="logo-icon">
          <svg
            viewBox="0 0 48 48"
            width="28"
            height="28"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle cx="24" cy="24" r="22" fill="url(#logoGrad)" />
            <path
              d="M12 31c4-10 8-15 12-15s8 5 12 15"
              stroke="white"
              stroke-width="3"
              fill="none"
              stroke-linecap="round"
            />
            <path
              d="M17 29l5-8 5 8"
              stroke="white"
              stroke-width="3"
              fill="none"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <circle cx="20" cy="16" r="2" fill="white" opacity="0.7" />
            <circle cx="28" cy="16" r="2" fill="white" opacity="0.7" />
            <defs>
              <linearGradient id="logoGrad" x1="0" y1="0" x2="48" y2="48">
                <stop offset="0%" stop-color="#31A84A" />
                <stop offset="100%" stop-color="#126C2A" />
              </linearGradient>
            </defs>
          </svg>
        </span>
        <span class="logo-text">
          <strong>迈开腿</strong>
          <small>遇见世界，遇见更好的自己</small>
        </span>
      </router-link>

      <!-- Desktop Navigation -->
      <nav class="nav-links hide-mobile">
        <router-link to="/" class="nav-link" exact-active-class="active"
          >首页</router-link
        >
        <router-link to="/attractions" class="nav-link" active-class="active"
          >景区</router-link
        >
        <!-- <router-link to="/attractions?sort=rating" class="nav-link">景点</router-link> -->
        <router-link to="/guides" class="nav-link" active-class="active"
          >攻略</router-link
        >
        <a class="nav-link" @click.prevent="openAiChat" href="#"> AI对话 </a>
        <router-link to="/" class="nav-link">关于我们</router-link>
      </nav>

      <!-- Desktop Right Actions -->
      <div class="header-actions hide-mobile">
        <label class="header-search">
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
          </svg>
          <input
            v-model="quickKeyword"
            placeholder="搜索景区/景点/攻略"
            @keyup.enter="handleHeaderSearch"
          />
        </label>
        <template v-if="userStore.isLoggedIn">
          <div class="user-dropdown" @click="toggleDropdown">
            <div class="user-avatar">
              <img
                v-if="userStore.userInfo?.avatar"
                :src="userStore.userInfo.avatar"
                alt="avatar"
              />
              <span v-else>{{
                (
                  userStore.userInfo?.nickname ||
                  userStore.userInfo?.username ||
                  "U"
                ).charAt(0)
              }}</span>
            </div>
            <span class="user-name">{{
              userStore.userInfo?.nickname ||
              userStore.userInfo?.username ||
              "用户"
            }}</span>
            <svg
              class="dropdown-arrow"
              :class="{ rotated: dropdownOpen }"
              width="12"
              height="12"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M6 9l6 6 6-6" />
            </svg>
            <div class="dropdown-menu" v-show="dropdownOpen" @click.stop>
              <router-link
                to="/user/profile"
                class="dropdown-item"
                @click="dropdownOpen = false"
              >
                <span>👤</span> 个人中心
              </router-link>
              <router-link
                to="/user/orders"
                class="dropdown-item"
                @click="dropdownOpen = false"
              >
                <span>📋</span> 我的订单
              </router-link>
              <router-link
                to="/user/favorites"
                class="dropdown-item"
                @click="dropdownOpen = false"
              >
                <span>❤️</span> 我的收藏
              </router-link>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item logout" @click="handleLogout">
                <span>🚪</span> 退出登录
              </div>
            </div>
          </div>
        </template>
        <template v-else>
          <button class="login-btn" @click="showLoginModal = true">登录</button>
          <button class="register-btn" @click="showLoginModal = true">
            注册
          </button>
        </template>
      </div>

      <!-- Mobile Menu Toggle -->
      <button
        class="menu-toggle show-mobile"
        @click="mobileMenuOpen = !mobileMenuOpen"
        aria-label="菜单"
      >
        <span :class="{ open: mobileMenuOpen }"></span>
        <span :class="{ open: mobileMenuOpen }"></span>
        <span :class="{ open: mobileMenuOpen }"></span>
      </button>
    </div>

    <!-- Mobile Menu -->
    <transition name="slide-down">
      <div class="mobile-menu" v-if="mobileMenuOpen">
        <router-link
          to="/"
          class="mobile-nav-link"
          @click="mobileMenuOpen = false"
          >首页</router-link
        >
        <router-link
          to="/attractions"
          class="mobile-nav-link"
          @click="mobileMenuOpen = false"
          >景区</router-link
        >
        <router-link
          to="/attractions?sort=rating"
          class="mobile-nav-link"
          @click="mobileMenuOpen = false"
          >景点</router-link
        >
        <router-link
          to="/guides"
          class="mobile-nav-link"
          @click="mobileMenuOpen = false"
          >攻略</router-link
        >
        <a
          class="mobile-nav-link"
          @click.prevent="
            openAiChat();
            mobileMenuOpen = false;
          "
          href="#"
          >AI对话</a
        >
        <div class="mobile-divider"></div>
        <template v-if="userStore.isLoggedIn">
          <router-link
            to="/user/profile"
            class="mobile-nav-link"
            @click="mobileMenuOpen = false"
            >个人中心</router-link
          >
          <router-link
            to="/user/orders"
            class="mobile-nav-link"
            @click="mobileMenuOpen = false"
            >我的订单</router-link
          >
          <router-link
            to="/user/favorites"
            class="mobile-nav-link"
            @click="mobileMenuOpen = false"
            >我的收藏</router-link
          >
          <div class="mobile-nav-link logout" @click="handleLogout">
            退出登录
          </div>
        </template>
        <template v-else>
          <button
            class="btn-primary mobile-login-btn"
            @click="
              showLoginModal = true;
              mobileMenuOpen = false;
            "
          >
            登录 / 注册
          </button>
        </template>
      </div>
    </transition>

    <!-- Login Modal -->
    <LoginModal v-if="showLoginModal" @close="showLoginModal = false" />
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { useAiChatStore } from "@/stores/aiChat";
import LoginModal from "@/components/common/LoginModal.vue";

const userStore = useUserStore();
const { open: openAiChat } = useAiChatStore();
const router = useRouter();

const isScrolled = ref(false);
const dropdownOpen = ref(false);
const mobileMenuOpen = ref(false);
const showLoginModal = ref(false);
const quickKeyword = ref("");

function onScroll() {
  isScrolled.value = window.scrollY > 50;
}

function toggleDropdown() {
  if (userStore.isLoggedIn) {
    dropdownOpen.value = !dropdownOpen.value;
  }
}

function handleLogout() {
  userStore.logout();
  dropdownOpen.value = false;
  mobileMenuOpen.value = false;
  router.push("/");
}

function handleHeaderSearch() {
  const keyword = quickKeyword.value.trim();
  if (!keyword) return;
  router.push({ name: "AttractionList", query: { keyword } });
  quickKeyword.value = "";
}

function handleClickOutside(e) {
  if (!e.target.closest(".user-dropdown")) {
    dropdownOpen.value = false;
  }
}

onMounted(() => {
  window.addEventListener("scroll", onScroll);
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  window.removeEventListener("scroll", onScroll);
  document.removeEventListener("click", handleClickOutside);
});
</script>

<style lang="scss" scoped>
.app-header {
  position: sticky;
  top: 0;
  height: 70px;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(31, 143, 58, 0.08);
  transition: var(--transition);

  &.is-scrolled {
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 0 10px 30px rgba(26, 64, 38, 0.08);
  }

  &.menu-open {
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(20px);
  }
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

// Logo
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.2rem;
  font-weight: 800;
  color: var(--dark);
  z-index: 10;

  .logo-icon {
    display: flex;
    align-items: center;
  }

  .logo-text {
    display: flex;
    flex-direction: column;
    line-height: 1.1;

    strong {
      color: var(--dark);
      font-size: 1.05rem;
      letter-spacing: 0;
    }

    small {
      color: var(--text-light);
      font-size: 0.68rem;
      font-weight: 500;
      margin-top: 2px;
      letter-spacing: 0;
    }
  }
}

// Nav Links
.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-link {
  padding: 8px 13px;
  border-radius: 8px;
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--text);
  transition: var(--transition);
  position: relative;

  &:hover {
    color: var(--primary);
  }

  &.active {
    color: var(--primary-dark);
    background: transparent;
    font-weight: 800;
  }
}

// Header Actions
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-search {
  width: 220px;
  height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: #f3f6f3;
  color: var(--text-light);
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid transparent;

  &:focus-within {
    border-color: rgba(31, 143, 58, 0.25);
    background: var(--white);
  }

  input {
    min-width: 0;
    flex: 1;
    font-size: 0.82rem;
    color: var(--text);
  }
}

.login-btn,
.register-btn {
  height: 34px;
  padding: 0 18px;
  border-radius: 999px;
  font-size: 0.86rem;
  font-weight: 700;
}

.login-btn {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: var(--white);
}

.register-btn {
  color: var(--primary-dark);
  border: 1px solid rgba(31, 143, 58, 0.35);
  background: var(--white);
}

.btn-sm {
  padding: 8px 22px;
  font-size: 0.9rem;
}

// User Dropdown
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px 6px 6px;
  border-radius: 50px;
  transition: var(--transition);
  position: relative;

  &:hover {
    background: rgba(31, 143, 58, 0.08);
  }
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 0.9rem;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.user-name {
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  transition: var(--transition);
  opacity: 0.5;

  &.rotated {
    transform: rotate(180deg);
  }
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 8px;
  box-shadow: var(--page-shadow);
  padding: 8px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: var(--transition);
  cursor: pointer;

  &:hover {
    background: rgba(31, 143, 58, 0.08);
  }

  &.logout {
    color: #dc3545;

    &:hover {
      background: rgba(220, 53, 69, 0.08);
    }
  }
}

.dropdown-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.08);
  margin: 4px 0;
}

// Mobile Menu Toggle
.menu-toggle {
  display: none;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;
  width: 36px;
  height: 36px;
  z-index: 10;
  border-radius: 8px;

  @media (max-width: 768px) {
    display: flex;
  }

  span {
    display: block;
    width: 22px;
    height: 2px;
    background: var(--dark);
    border-radius: 2px;
    transition: var(--transition);
  }

  span.open:first-child {
    transform: rotate(45deg) translate(5px, 5px);
  }

  span.open:nth-child(2) {
    opacity: 0;
  }

  span.open:last-child {
    transform: rotate(-45deg) translate(5px, -5px);
  }
}

// Mobile Menu
.mobile-menu {
  display: none;
  position: fixed;
  top: 70px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  padding: 24px;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  z-index: 999;

  @media (max-width: 768px) {
    display: flex;
  }
}

.mobile-nav-link {
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 1.05rem;
  font-weight: 500;
  transition: var(--transition);

  &:hover,
  &.router-link-active {
    background: rgba(31, 143, 58, 0.08);
    color: var(--primary);
  }

  &.logout {
    color: #dc3545;
  }
}

.mobile-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.08);
  margin: 8px 0;
}

.mobile-login-btn {
  width: 100%;
  margin-top: 12px;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// Responsive medias
@media (max-width: 768px) {
  .hide-mobile {
    display: none;
  }
}
</style>
