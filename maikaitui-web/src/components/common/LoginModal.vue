<template>
  <teleport to="body">
    <div class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-container animate__animated animate__zoomIn animate__faster">
        <button class="modal-close" @click="$emit('close')" aria-label="关闭">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>

        <!-- Header -->
        <div class="modal-header">
          <div class="modal-logo">
            <span>🏃</span> 迈开腿
          </div>
        </div>

        <!-- Tabs -->
        <div class="modal-tabs">
          <button
            :class="{ active: activeTab === 'login' }"
            @click="activeTab = 'login'"
          >登录</button>
          <button
            :class="{ active: activeTab === 'register' }"
            @click="activeTab = 'register'"
          >注册</button>
        </div>

        <!-- Login Form -->
        <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="modal-form">
          <div class="form-group">
            <label>用户名</label>
            <input v-model="loginForm.username" type="text" placeholder="请输入用户名" required />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input v-model="loginForm.password" type="password" placeholder="请输入密码" required />
          </div>
          <div class="form-footer">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.remember" /> 记住我
            </label>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>
          <button type="submit" class="btn-primary submit-btn" :disabled="loginLoading">
            {{ loginLoading ? '登录中...' : '登录' }}
          </button>
        </form>

        <!-- Register Form -->
        <form v-if="activeTab === 'register'" @submit.prevent="handleRegister" class="modal-form">
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
            <input v-model="registerForm.password" type="password" placeholder="请输入密码（至少6位）" required minlength="6" />
          </div>
          <div class="form-group">
            <label>确认密码</label>
            <input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" required />
          </div>
          <button type="submit" class="btn-primary submit-btn" :disabled="registerLoading">
            {{ registerLoading ? '注册中...' : '注册' }}
          </button>
        </form>

        <!-- Social Login -->
        <div class="social-section">
          <div class="divider"><span>其他方式登录</span></div>
          <div class="social-buttons">
            <button class="social-btn" disabled title="微信登录（即将上线）">
              <span>💚</span> 微信
            </button>
            <button class="social-btn" disabled title="手机号登录（即将上线）">
              <span>📱</span> 手机号
            </button>
          </div>
        </div>

        <!-- Message -->
        <p v-if="message" class="form-message" :class="{ error: isError, success: !isError }">
          {{ message }}
        </p>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'

const emit = defineEmits(['close'])
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
    emit('close')
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
    // Clear register form
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
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.modal-container {
  position: relative;
  width: 100%;
  max-width: 420px;
  max-height: 90vh;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--card-radius-lg);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  padding: 40px 32px;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-light);
  transition: var(--transition);

  &:hover {
    background: rgba(0, 0, 0, 0.05);
    color: var(--text);
  }
}

.modal-header {
  text-align: center;
  margin-bottom: 28px;
}

.modal-logo {
  font-size: 1.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.modal-tabs {
  display: flex;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 28px;

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
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }

    &:hover:not(.active) {
      color: var(--text);
    }
  }
}

.modal-form {
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
    border: 1.5px solid rgba(0, 0, 0, 0.1);
    border-radius: 12px;
    font-size: 0.95rem;
    transition: var(--transition);
    background: rgba(0, 0, 0, 0.02);

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

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;

  .remember-me {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    color: var(--text-light);

    input[type="checkbox"] {
      accent-color: var(--primary);
    }
  }

  .forgot-password {
    color: var(--primary);

    &:hover {
      text-decoration: underline;
    }
  }
}

.submit-btn {
  width: 100%;
  padding: 14px;
  font-size: 1rem;
  margin-top: 4px;
}

.social-section {
  margin-top: 24px;
}

.divider {
  display: flex;
  align-items: center;
  color: var(--text-lighter);
  font-size: 0.8rem;
  margin-bottom: 16px;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: rgba(0, 0, 0, 0.1);
  }

  span {
    padding: 0 16px;
  }
}

.social-buttons {
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
  border: 1.5px solid rgba(0, 0, 0, 0.1);
  font-size: 0.85rem;
  color: var(--text-light);
  background: var(--white);
  transition: var(--transition);

  &:not(:disabled):hover {
    border-color: var(--primary);
    background: rgba(255, 107, 53, 0.04);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.form-message {
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

@media (max-width: 480px) {
  .modal-container {
    padding: 28px 20px;
  }
}
</style>
