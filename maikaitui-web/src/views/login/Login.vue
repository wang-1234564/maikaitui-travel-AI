<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <div class="login-container-wrapper">
      <div class="login-card glass-card">
        <!-- Close / Back -->
        <router-link to="/" class="login-back">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回首页
        </router-link>

        <!-- Logo -->
        <div class="login-logo">
          <span class="logo-icon">🏃</span>
          <span class="logo-text">迈开腿</span>
        </div>
        <p class="login-slogan">发现世界的美好</p>

        <!-- Tabs -->
        <div class="login-tabs">
          <button :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'; clearMessage()">登录</button>
          <button :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'; clearMessage()">注册</button>
        </div>

        <!-- Login Form -->
        <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="loginForm.username" type="text" placeholder="请输入用户名" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="loginForm.password" type="password" placeholder="请输入密码" required />
          </div>
          <div class="form-footer-row">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.remember" /> 记住我
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>
          <button type="submit" class="btn-primary submit-btn" :disabled="loginLoading">
            {{ loginLoading ? '登录中...' : '登录' }}
          </button>
        </form>

        <!-- Register Form -->
        <form v-if="activeTab === 'register'" @submit.prevent="handleRegister" class="login-form">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="registerForm.username" type="text" placeholder="请输入用户名" required />
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input v-model="registerForm.nickname" type="text" placeholder="请输入昵称" required />
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input v-model="registerForm.phone" type="tel" placeholder="请输入手机号" />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="registerForm.password" type="password" placeholder="至少6位密码" required minlength="6" />
          </div>
          <div class="form-group">
            <label>确认密码</label>
            <input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" required />
          </div>
          <button type="submit" class="btn-primary submit-btn" :disabled="registerLoading">
            {{ registerLoading ? '注册中...' : '注册' }}
          </button>
        </form>

        <!-- Message -->
        <p v-if="message" class="login-message" :class="{ error: isError, success: !isError }">
          {{ message }}
        </p>

        <!-- Divider -->
        <div class="login-divider"><span>其他方式登录</span></div>

        <!-- Social -->
        <div class="social-btns">
          <button class="social-btn" disabled><span>💚</span> 微信</button>
          <button class="social-btn" disabled><span>📱</span> 手机号</button>
        </div>

        <!-- Register success hint -->
        <p class="login-footer-text" v-if="activeTab === 'login'">
          还没有账号？<button class="switch-link" @click="activeTab = 'register'">立即注册</button>
        </p>
        <p class="login-footer-text" v-else>
          已有账号？<button class="switch-link" @click="activeTab = 'login'">立即登录</button>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('login')
const message = ref('')
const isError = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: ''
})

function clearMessage() {
  message.value = ''
}

async function handleLogin() {
  clearMessage()
  if (!loginForm.username || !loginForm.password) {
    message.value = '请填写用户名和密码'
    isError.value = true
    return
  }

  loginLoading.value = true
  try {
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password
    })
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    message.value = e?.response?.data?.message || '登录失败，请检查用户名和密码'
    isError.value = true
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  clearMessage()
  if (!registerForm.username || !registerForm.password || !registerForm.confirmPassword) {
    message.value = '请填写所有必填项'
    isError.value = true
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    message.value = '两次密码输入不一致'
    isError.value = true
    return
  }
  if (registerForm.password.length < 6) {
    message.value = '密码长度不能少于6位'
    isError.value = true
    return
  }

  registerLoading.value = true
  try {
    await userStore.register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname || registerForm.username,
      phone: registerForm.phone
    })
    message.value = '注册成功！请登录'
    isError.value = false
    activeTab.value = 'login'
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.nickname = ''
    registerForm.phone = ''
  } catch (e) {
    message.value = e?.response?.data?.message || '注册失败，请稍后重试'
    isError.value = true
  } finally {
    registerLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 40px 20px;
  margin-top: -70px;
  padding-top: 110px;
}

.login-bg {
  position: fixed;
  inset: 0;
  background:
    linear-gradient(135deg, rgba(31, 143, 58, 0.92), rgba(18, 108, 42, 0.88)),
    url('https://picsum.photos/seed/maikaitui-login/1400/900') center / cover no-repeat;
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 30% 50%, rgba(0,0,0,0.2) 0%, rgba(0,0,0,0.6) 100%);
  }
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.login-container-wrapper {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 440px;
}

.login-card {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--card-radius-lg);
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.4);

  @media (max-width: 480px) {
    padding: 30px 22px;
  }
}

.login-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  color: var(--text-light);
  transition: var(--transition);
  margin-bottom: 24px;

  &:hover {
    color: var(--primary);
  }
}

.login-logo {
  text-align: center;
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 4px;

  .logo-icon {
    margin-right: 6px;
  }

  .logo-text {
    background: linear-gradient(135deg, var(--primary), var(--accent));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.login-slogan {
  text-align: center;
  color: var(--text-light);
  font-size: 0.9rem;
  margin-bottom: 28px;
}

/* Tabs */
.login-tabs {
  display: flex;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 24px;

  button {
    flex: 1;
    padding: 10px;
    border-radius: 10px;
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--text-light);
    transition: var(--transition);

    &.active {
      background: var(--white);
      color: var(--primary);
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
    }
  }
}

/* Form */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;

  label {
    font-size: 0.85rem;
    font-weight: 600;
    color: var(--text);
  }

  input {
    width: 100%;
    padding: 12px 16px;
    border: 1.5px solid rgba(0,0,0,0.1);
    border-radius: 12px;
    font-size: 0.95rem;
    transition: var(--transition);
    background: rgba(0,0,0,0.02);

    &:focus {
      border-color: var(--primary);
      background: var(--white);
      box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
    }

    &::placeholder {
      color: var(--text-lighter);
    }
  }
}

.form-footer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;

  .remember-me {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--text-light);
    cursor: pointer;

    input[type="checkbox"] {
      accent-color: var(--primary);
    }
  }

  .forgot-link {
    color: var(--primary);
  }
}

.submit-btn {
  width: 100%;
  padding: 14px;
  font-size: 1rem;
  margin-top: 4px;
}

/* Message */
.login-message {
  text-align: center;
  margin-top: 16px;
  padding: 10px;
  border-radius: 8px;
  font-size: 0.85rem;

  &.error {
    background: rgba(220, 53, 69, 0.08);
    color: #DC3545;
  }

  &.success {
    background: rgba(46, 196, 182, 0.08);
    color: var(--secondary-dark);
  }
}

/* Divider */
.login-divider {
  display: flex;
  align-items: center;
  color: var(--text-lighter);
  font-size: 0.8rem;
  margin: 24px 0 16px;

  &::before, &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: rgba(0,0,0,0.1);
  }

  span {
    padding: 0 16px;
  }
}

/* Social */
.social-btns {
  display: flex;
  gap: 12px;
}

.social-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  border-radius: 12px;
  border: 1.5px solid rgba(0,0,0,0.1);
  font-size: 0.85rem;
  color: var(--text-light);
  background: var(--white);
  transition: var(--transition);

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

/* Footer */
.login-footer-text {
  text-align: center;
  margin-top: 20px;
  font-size: 0.9rem;
  color: var(--text-light);
}

.switch-link {
  color: var(--primary);
  font-weight: 600;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}
</style>
