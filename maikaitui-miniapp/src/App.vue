<script>
export default {
  onLaunch() {
    const token = uni.getStorageSync('token')
    if (token) {
      import('@/store/index.js').then(({ store }) => store.checkLogin())
    }
  }
}
</script>

<style lang="scss">
@import '@/styles/theme.scss';

page {
  background-color: $bg-page;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
  color: $text-primary;
  font-size: 28rpx;
  line-height: 1.6;
}

button::after {
  border: none;
}
</style>
