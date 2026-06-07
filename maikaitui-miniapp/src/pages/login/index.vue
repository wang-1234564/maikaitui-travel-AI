<template>
  <view class="login">
    <view class="status-bar" :style="{ height: statusBarHeight + 'px' }" />
    <view class="bg-layer" />
    <view class="float f1" /><view class="float f2" />

    <view class="content">
      <text class="logo-mark">🚶</text>
      <text class="brand">迈开腿</text>
      <text class="tagline">探索世界，从脚下开始</text>

      <view class="card">
        <view class="tabs">
          <text :class="{ on: tab === 0 }" @click="tab = 0">登录</text>
          <text :class="{ on: tab === 1 }" @click="tab = 1">注册</text>
        </view>

        <view v-if="tab === 0" class="form">
          <view class="field"><text class="ico">👤</text><input v-model="loginForm.username" placeholder="用户名" /></view>
          <view class="field"><text class="ico">🔒</text><input v-model="loginForm.password" password placeholder="密码" /></view>
          <button class="btn" :disabled="loading" @click="doLogin">{{ loading ? '登录中…' : '登 录' }}</button>
        </view>

        <view v-else class="form">
          <view class="field"><text class="ico">👤</text><input v-model="reg.username" placeholder="用户名" /></view>
          <view class="field"><text class="ico">🔒</text><input v-model="reg.password" password placeholder="密码" /></view>
          <view class="field"><text class="ico">🔒</text><input v-model="reg.confirmPassword" password placeholder="确认密码" /></view>
          <view class="field"><text class="ico">😊</text><input v-model="reg.nickname" placeholder="昵称" /></view>
          <view class="field"><text class="ico">📱</text><input v-model="reg.phone" type="number" maxlength="11" placeholder="手机号" /></view>
          <button class="btn" :disabled="loading" @click="doRegister">{{ loading ? '提交中…' : '注 册' }}</button>
        </view>
      </view>

      <text class="skip" @click="skip">先逛逛，暂不登录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { login, register, getUserInfo } from '@/api/index.js'
import { store } from '@/store/index.js'

const statusBarHeight = ref(20)
try {
  statusBarHeight.value = uni.getWindowInfo?.()?.statusBarHeight || uni.getSystemInfoSync().statusBarHeight || 20
} catch (e) { /* */ }

const tab = ref(0)
const loading = ref(false)
const loginForm = ref({ username: '', password: '' })
const reg = ref({ username: '', password: '', confirmPassword: '', nickname: '', phone: '' })

const afterAuth = async (token, partial) => {
  store.setToken(token)
  try { store.setUserInfo(await getUserInfo()) } catch { store.setUserInfo(partial) }
}

const doLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) return
  loading.value = true
  try {
    const data = await login(loginForm.value)
    await afterAuth(data.token, data)
    uni.showToast({ title: '欢迎回来', icon: 'success' })
    setTimeout(back, 600)
  } catch (e) { /* */ }
  finally { loading.value = false }
}

const doRegister = async () => {
  const f = reg.value
  if (f.password.length < 6) return uni.showToast({ title: '密码至少6位', icon: 'none' })
  if (f.password !== f.confirmPassword) return uni.showToast({ title: '两次密码不一致', icon: 'none' })
  if (!/^1[3-9]\d{9}$/.test(f.phone)) return uni.showToast({ title: '手机号格式错误', icon: 'none' })
  loading.value = true
  try {
    await register({ username: f.username, password: f.password, nickname: f.nickname, phone: f.phone })
    uni.showToast({ title: '注册成功', icon: 'success' })
    tab.value = 0
    loginForm.value.username = f.username
    loginForm.value.password = f.password
  } catch (e) { /* */ }
  finally { loading.value = false }
}

const back = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) uni.navigateBack()
  else uni.switchTab({ url: '/pages/index/index' })
}
const skip = () => back()
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';

.login { min-height: 100vh; position: relative; overflow: hidden; }
.bg-layer {
  position: absolute;
  inset: 0;
  @include hero-bg;
}
.float {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
  &.f1 { width: 320rpx; height: 320rpx; top: -80rpx; right: -60rpx; }
  &.f2 { width: 200rpx; height: 200rpx; bottom: 200rpx; left: -40rpx; }
}
.content {
  position: relative;
  z-index: 2;
  padding: 40rpx 40rpx 60rpx;
  text-align: center;
}
.logo-mark { font-size: 88rpx; display: block; margin-bottom: 12rpx; }
.brand {
  font-size: 52rpx;
  font-weight: 900;
  color: #fff;
  display: block;
  letter-spacing: 4rpx;
}
.tagline {
  font-size: 26rpx;
  color: rgba(255,255,255,0.85);
  margin: 12rpx 0 48rpx;
  display: block;
}
.card {
  @include card-glass;
  background: rgba(255,255,255,0.97);
  padding: 36rpx 32rpx 40rpx;
  text-align: left;
  box-shadow: 0 20rpx 60rpx rgba(26,26,46,0.15);
}
.tabs {
  display: flex;
  margin-bottom: 32rpx;
  background: $bg-page;
  border-radius: 999rpx;
  padding: 6rpx;
  text {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    font-size: 28rpx;
    color: $text-secondary;
    border-radius: 999rpx;
    transition: all 0.25s;
    &.on {
      background: $gradient-brand;
      color: #fff;
      font-weight: 700;
      box-shadow: $shadow-primary;
    }
  }
}
.field {
  display: flex;
  align-items: center;
  background: $bg-page;
  border-radius: $radius-md;
  padding: 0 24rpx;
  margin-bottom: 20rpx;
  border: 2rpx solid transparent;
  &:focus-within { border-color: $primary; background: #fff; }
  .ico { margin-right: 16rpx; font-size: 32rpx; }
  input { flex: 1; padding: 26rpx 0; font-size: 28rpx; }
}
.btn {
  @include btn-primary;
  width: 100%;
  height: 92rpx;
  line-height: 92rpx;
  margin-top: 12rpx;
  font-size: 32rpx;
}
.skip {
  display: block;
  margin-top: 40rpx;
  color: rgba(255,255,255,0.95);
  font-size: 28rpx;
  text-decoration: underline;
  text-underline-offset: 6rpx;
}
</style>
