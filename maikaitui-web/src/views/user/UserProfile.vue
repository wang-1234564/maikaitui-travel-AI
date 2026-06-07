<template>
  <div class="user-profile-page">
    <div class="list-hero">
      <div class="container">
        <h1 class="list-hero-title">个人中心</h1>
      </div>
    </div>

    <div class="container section">
      <div class="profile-layout">
        <!-- Left Sidebar -->
        <aside class="profile-sidebar glass-card">
          <div class="sidebar-user">
            <div class="sidebar-avatar">
              <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="avatar" />
              <span v-else class="avatar-placeholder">{{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}</span>
            </div>
            <h3 class="sidebar-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</h3>
          </div>

          <nav class="sidebar-nav">
            <router-link to="/user/profile" class="nav-item active">
              <span class="nav-icon">👤</span> 个人信息
            </router-link>
            <router-link to="/user/orders" class="nav-item">
              <span class="nav-icon">📋</span> 我的订单
            </router-link>
            <router-link to="/user/favorites" class="nav-item">
              <span class="nav-icon">❤️</span> 我的收藏
            </router-link>
            <div class="nav-item logout" @click="handleLogout">
              <span class="nav-icon">🚪</span> 退出登录
            </div>
          </nav>
        </aside>

        <!-- Right Content -->
        <div class="profile-content">
          <div class="content-card glass-card">
            <h3 class="content-title">个人信息</h3>

            <div class="avatar-upload" @click="triggerUpload">
              <div class="avatar-preview">
                <img v-if="form.avatar || userStore.userInfo?.avatar" :src="form.avatar || userStore.userInfo?.avatar" alt="avatar" />
                <span v-else class="avatar-placeholder-lg">{{ (form.nickname || 'U').charAt(0).toUpperCase() }}</span>
              </div>
              <p class="upload-hint">点击更换头像</p>
              <input type="file" ref="fileInput" accept="image/*" @change="onFileChange" style="display:none" />
            </div>

            <form @submit.prevent="handleSave" class="profile-form">
              <div class="form-row">
                <div class="form-group">
                  <label>用户名</label>
                  <input type="text" :value="userStore.userInfo?.username" disabled class="form-input disabled" />
                  <span class="form-hint">用户名不可修改</span>
                </div>
                <div class="form-group">
                  <label>昵称</label>
                  <input v-model="form.nickname" type="text" placeholder="请输入昵称" class="form-input" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>手机号</label>
                  <input v-model="form.phone" type="tel" placeholder="请输入手机号" class="form-input" />
                </div>
                <div class="form-group">
                  <label>邮箱</label>
                  <input v-model="form.email" type="email" placeholder="请输入邮箱" class="form-input" />
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-primary" :disabled="saving">
                  {{ saving ? '保存中...' : '保存修改' }}
                </button>
              </div>

              <p v-if="saveMessage" class="save-message" :class="{ success: saveSuccess, error: !saveSuccess }">
                {{ saveMessage }}
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateProfile, uploadFile } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const fileInput = ref(null)
const saving = ref(false)
const saveMessage = ref('')
const saveSuccess = ref(false)
const selectedFile = ref(null)

const form = reactive({
  nickname: '',
  phone: '',
  email: '',
  avatar: ''
})

function triggerUpload() {
  fileInput.value?.click()
}

function onFileChange(e) {
  const file = e.target.files[0]
  if (file) {
    selectedFile.value = file
    form.avatar = URL.createObjectURL(file)
  }
}

async function handleSave() {
  saving.value = true
  saveMessage.value = ''
  try {
    // 如果选择了新头像，先上传到 OSS
    if (selectedFile.value) {
      const uploadRes = await uploadFile(selectedFile.value)
      form.avatar = uploadRes.fileUrl
    }

    await updateProfile({
      nickname: form.nickname,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar
    })
    saveSuccess.value = true
    saveMessage.value = '保存成功！'
    selectedFile.value = null
    // Refresh user info
    await userStore.fetchUserInfo()
  } catch (e) {
    saveSuccess.value = false
    saveMessage.value = '保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}

onMounted(() => {
  if (userStore.userInfo) {
    form.nickname = userStore.userInfo.nickname || ''
    form.phone = userStore.userInfo.phone || ''
    form.email = userStore.userInfo.email || ''
  }
})
</script>

<style lang="scss" scoped>
.user-profile-page {
  min-height: 100vh;
}

.list-hero {
  background: linear-gradient(135deg, var(--dark), var(--dark-secondary));
  padding: 50px 0;
  text-align: center;

  .list-hero-title {
    font-size: 2rem;
    font-weight: 800;
    color: var(--white);
  }
}

.profile-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

/* Sidebar */
.profile-sidebar {
  padding: 28px 20px;
  background: var(--white);
  height: fit-content;
  position: sticky;
  top: 90px;
}

.sidebar-user {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
}

.sidebar-avatar {
  width: 80px;
  height: 80px;
  margin: 0 auto 12px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-placeholder {
    color: white;
    font-size: 2rem;
    font-weight: 800;
  }
}

.sidebar-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--dark);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: var(--transition);

  .nav-icon {
    font-size: 1.1rem;
  }

  &:hover, &.active, &.router-link-active {
    background: rgba(255, 107, 53, 0.08);
    color: var(--primary);
    font-weight: 600;
  }

  &.logout {
    color: #DC3545;
    cursor: pointer;

    &:hover {
      background: rgba(220, 53, 69, 0.08);
    }
  }
}

/* Content */
.content-card {
  padding: 32px;
  background: var(--white);
}

.content-title {
  font-size: 1.3rem;
  font-weight: 700;
  margin-bottom: 28px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(255, 107, 53, 0.15);
}

.avatar-upload {
  text-align: center;
  margin-bottom: 32px;
  cursor: pointer;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  margin: 0 auto 10px;
  overflow: hidden;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);

  &:hover {
    opacity: 0.8;
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-placeholder-lg {
    color: white;
    font-size: 2.4rem;
    font-weight: 800;
  }
}

.upload-hint {
  font-size: 0.85rem;
  color: var(--text-light);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
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
}

.form-input {
  padding: 12px 16px;
  border: 1.5px solid rgba(0,0,0,0.1);
  border-radius: 10px;
  font-size: 0.95rem;
  transition: var(--transition);
  background: rgba(0,0,0,0.02);

  &:focus {
    border-color: var(--primary);
    background: var(--white);
    box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.08);
  }

  &.disabled {
    background: rgba(0,0,0,0.03);
    color: var(--text-lighter);
    cursor: not-allowed;
  }
}

.form-hint {
  font-size: 0.75rem;
  color: var(--text-lighter);
}

.form-actions {
  padding-top: 8px;
}

.save-message {
  padding: 10px;
  border-radius: 8px;
  font-size: 0.9rem;
  text-align: center;

  &.success {
    background: rgba(46, 196, 182, 0.08);
    color: var(--secondary-dark);
  }

  &.error {
    background: rgba(220, 53, 69, 0.08);
    color: #DC3545;
  }
}
</style>
