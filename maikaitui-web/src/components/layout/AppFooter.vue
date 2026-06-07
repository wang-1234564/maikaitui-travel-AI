<template>
  <footer class="app-footer">
    <div class="container">
      <div class="footer-grid">
        <!-- About -->
        <div class="footer-col">
          <h4 class="footer-title">迈开腿</h4>
          <p class="footer-about">
            迈开腿致力于为旅行爱好者提供全面的旅游信息与智能服务，让景区发现、攻略查询、
            AI 咨询和行程预订更高效、更安心。
          </p>
          <div class="footer-logo">
            <span class="logo-icon">M</span>
            <span class="logo-name">迈开腿</span>
          </div>
        </div>

        <!-- Quick Links -->
        <div class="footer-col">
          <h4 class="footer-title">快速导航</h4>
          <ul class="footer-links">
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/attractions">景区</router-link></li>
            <li><router-link to="/guides">攻略</router-link></li>
            <li><router-link to="/ai">AI对话</router-link></li>
            <li><router-link to="/user/orders">我的订单</router-link></li>
            <li><router-link to="/user/favorites">我的收藏</router-link></li>
          </ul>
        </div>

        <!-- Contact -->
        <div class="footer-col">
          <h4 class="footer-title">帮助中心</h4>
          <ul class="footer-contact">
            <li>
              <span class="contact-icon">01</span>
              <span>使用帮助</span>
            </li>
            <li>
              <span class="contact-icon">02</span>
              <span>常见问题</span>
            </li>
            <li>
              <span class="contact-icon">03</span>
              <span>意见反馈</span>
            </li>
            <li>
              <span class="contact-icon">04</span>
              <span>联系我们</span>
            </li>
          </ul>
        </div>

        <!-- Social -->
        <div class="footer-col">
          <h4 class="footer-title">关注我们</h4>
          <div class="social-icons">
            <a href="#" class="social-icon" aria-label="微信" title="微信">微</a>
            <a href="#" class="social-icon" aria-label="微博" title="微博">博</a>
            <a href="#" class="social-icon" aria-label="抖音" title="抖音">音</a>
          </div>
          <p class="social-hint">关注我们获取最新旅游资讯与活动信息</p>
        </div>
      </div>

      <div class="footer-bottom">
        <p>&copy; 2024 迈开腿 旅行平台 · 蜀ICP备2024000000号-1</p>
        <div class="footer-bottom-links">
          <a href="#">隐私政策</a>
          <a href="#">用户协议</a>
          <a href="#">帮助中心</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.app-footer {
  background:
    linear-gradient(135deg, rgba(18, 108, 42, 0.98), rgba(31, 143, 58, 0.94)),
    radial-gradient(circle at 10% 0%, rgba(245, 184, 75, 0.18), transparent 30%);
  color: rgba(255, 255, 255, 0.8);
  padding: 34px 0 0;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  gap: 44px;

  @media (max-width: 1024px) {
    grid-template-columns: 1fr 1fr;
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
    gap: 36px;
  }
}

.footer-col {
  min-width: 0;
}

.footer-title {
  color: var(--white);
  font-size: 1.1rem;
  font-weight: 800;
  margin-bottom: 20px;
  position: relative;

  &::after {
    content: '';
    display: block;
    width: 32px;
    height: 3px;
    background: rgba(255, 255, 255, 0.5);
    margin-top: 8px;
    border-radius: 2px;
  }
}

.footer-about {
  font-size: 0.9rem;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 20px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.2rem;
  font-weight: 700;

  .logo-icon {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: var(--white);
    color: var(--primary-dark);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
  }

  .logo-name {
    color: var(--white);
  }
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 10px;

  li a {
    color: rgba(255, 255, 255, 0.6);
    font-size: 0.9rem;
    transition: var(--transition);

    &:hover {
      color: var(--white);
      padding-left: 4px;
    }
  }
}

.footer-contact {
  display: flex;
  flex-direction: column;
  gap: 12px;

  li {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 0.9rem;
    color: rgba(255, 255, 255, 0.6);

    .contact-icon {
      width: 26px;
      height: 26px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.12);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 0.72rem;
      color: var(--white);
      flex-shrink: 0;
    }
  }
}

.social-icons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.social-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
  font-weight: 800;
  transition: var(--transition);

  &:hover {
    background: rgba(255, 255, 255, 0.16);
    transform: translateY(-3px);
  }
}

.social-hint {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.4);
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
  margin-top: 48px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.4);

  @media (max-width: 600px) {
    flex-direction: column;
    gap: 12px;
  }
}

.footer-bottom-links {
  display: flex;
  gap: 24px;

  a {
    transition: var(--transition);

    &:hover {
      color: rgba(255, 255, 255, 0.7);
    }
  }
}
</style>
