<template>
  <div class="user-orders-page">
    <div class="list-hero">
      <div class="container">
        <h1 class="list-hero-title">我的订单</h1>
      </div>
    </div>

    <div class="container section">
      <!-- Tabs -->
      <div class="order-tabs glass-card">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value; fetchOrders(1)"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-skeleton">
        <div v-for="n in 3" :key="n" class="skeleton skeleton-order"></div>
      </div>

      <!-- Orders List -->
      <div v-else-if="orders.length > 0" class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card glass-card">
          <div class="order-header">
            <div class="order-info">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-date">{{ formatDate(order.createTime) }}</span>
            </div>
            <span class="order-status" :class="statusClass(order.orderStatus)">
              {{ statusText(order.orderStatus) }}
            </span>
          </div>

          <div class="order-body">
            <div class="order-image">
              <img :src="getCoverImage(order)" :alt="order.attractionName" />
            </div>
            <div class="order-details">
              <h4 class="order-attraction">{{ order.attractionName }}</h4>
              <div class="order-meta">
                <span v-if="order.visitDate">📅 {{ order.visitDate }}</span>
                <span>🎫 数量：{{ order.quantity }}</span>
              </div>
              <div class="order-contact" v-if="order.contactName">
                <span>👤 {{ order.contactName }}</span>
                <span v-if="order.contactPhone">📱 {{ order.contactPhone }}</span>
              </div>
            </div>
            <div class="order-price">
              <span class="price-amount">¥{{ order.totalPrice }}</span>
            </div>
          </div>

          <div class="order-footer" v-if="order.orderStatus === 'pending'">
            <button class="btn-pay btn-sm-pay" @click="handlePay(order)">
              💰 立即支付
            </button>
            <button class="btn-cancel btn-sm-cancel" @click="handleCancel(order)">
              取消订单
            </button>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div v-else class="empty-state">
        <div class="empty-icon">📋</div>
        <p class="empty-text">暂无订单</p>
        <p class="empty-hint">快去探索景点并下单吧</p>
        <router-link to="/attractions" class="btn-primary" style="margin-top: 20px;">探索景点</router-link>
      </div>

      <!-- Pagination -->
      <div class="pagination" v-if="total > pageSize && !loading">
        <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
        <span class="page-info">{{ currentPage }} / {{ Math.ceil(total / pageSize) }}</span>
        <button :disabled="currentPage >= Math.ceil(total / pageSize)" @click="changePage(currentPage + 1)">下一页</button>
      </div>
    </div>
  </div>

  <!-- 支付确认弹窗（Teleport 独立于列表的 v-if 链） -->
  <Teleport to="body">
    <div class="modal-overlay" v-if="payModalVisible" @click.self="payModalVisible = false">
      <div class="modal-card">
        <button class="modal-close" @click="payModalVisible = false" aria-label="关闭">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
        <div class="modal-header">💰 确认支付</div>
        <div class="modal-body">
          <p>确认支付以下订单？</p>
          <div class="pay-info" v-if="payingOrder">
            <strong>{{ payingOrder.attractionName }}</strong>
            <span>数量：{{ payingOrder.quantity }} 张</span>
            <span class="pay-amount">合计：¥{{ payingOrder.totalPrice }}</span>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel-action" @click="payModalVisible = false">取消</button>
          <button class="btn-confirm-pay" @click="confirmPay" :disabled="paySubmitting">
            {{ paySubmitting ? '支付中...' : '确认支付' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders, cancelOrder, payOrder } from '@/api'

const tabs = [
  { label: '全部订单', value: 'ALL' },
  { label: '待支付', value: 'pending' },
  { label: '已支付', value: 'paid' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' }
]

const orders = ref([])
const loading = ref(true)
const activeTab = ref('ALL')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const payModalVisible = ref(false)
const payingOrder = ref(null)
const paySubmitting = ref(false)

function statusClass(status) {
  const s = (status || '').toLowerCase()
  if (s === 'pending') return 'warning'
  if (s === 'paid') return 'info'
  if (s === 'completed') return 'success'
  if (s === 'cancelled') return 'error'
  return ''
}

function statusText(status) {
  const s = (status || '').toLowerCase()
  if (s === 'pending') return '待支付'
  if (s === 'paid') return '已支付'
  if (s === 'completed') return '已完成'
  if (s === 'cancelled') return '已取消'
  return status || '未知'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function getCoverImage(order) {
  if (order.coverImage) return order.coverImage
  return `https://picsum.photos/seed/order-${order.attractionId || order.id}/200/140`
}

async function fetchOrders(page = 1) {
  loading.value = true
  try {
    const params = { page, pageSize: pageSize.value }
    if (activeTab.value !== 'ALL') {
      params.status = activeTab.value
    }

    const data = await getMyOrders(params)
    const records = Array.isArray(data) ? data : (data.records || data.list || [])
    orders.value = records
    total.value = data.total || data.totalCount || records.length
    currentPage.value = page
  } catch (e) {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  fetchOrders(page)
  window.scrollTo({ top: 200, behavior: 'smooth' })
}

function handlePay(order) {
  payingOrder.value = order
  payModalVisible.value = true
}

async function confirmPay() {
  if (!payingOrder.value) return
  paySubmitting.value = true
  try {
    await payOrder(payingOrder.value.id)
    alert('支付成功！')
    payModalVisible.value = false
    payingOrder.value = null
    fetchOrders(currentPage.value)
  } catch (e) {
    // handled by interceptor
  } finally {
    paySubmitting.value = false
  }
}

async function handleCancel(order) {
  if (!confirm('确定要取消这个订单吗？')) return
  try {
    await cancelOrder(order.id)
    alert('订单已取消')
    fetchOrders(currentPage.value)
  } catch (e) {
    alert('取消失败，请稍后重试')
  }
}

onMounted(() => {
  fetchOrders(1)
})
</script>

<style lang="scss" scoped>
.user-orders-page {
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

/* Tabs */
.order-tabs {
  display: flex;
  gap: 4px;
  padding: 6px;
  margin-bottom: 28px;
  background: var(--white);
  flex-wrap: wrap;

  button {
    padding: 10px 22px;
    border-radius: 10px;
    font-size: 0.9rem;
    font-weight: 500;
    color: var(--text-light);
    transition: var(--transition);

    &:hover {
      color: var(--text);
    }

    &.active {
      background: linear-gradient(135deg, var(--primary), var(--primary-light));
      color: var(--white);
      font-weight: 600;
    }
  }
}

/* Loading */
.loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-order {
  height: 140px;
}

/* Order Card */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  padding: 20px 24px;
  background: var(--white);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
}

.order-info {
  display: flex;
  gap: 16px;
  font-size: 0.8rem;
  color: var(--text-light);

  @media (max-width: 480px) {
    flex-direction: column;
    gap: 4px;
  }
}

.order-no {
  font-family: monospace;
}

.order-status {
  font-size: 0.8rem;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;

  &.warning {
    background: rgba(245, 184, 75, 0.15);
    color: var(--accent-dark);
  }
  &.info {
    background: rgba(31, 143, 58, 0.12);
    color: var(--primary-dark);
  }
  &.success {
    background: rgba(46, 196, 182, 0.12);
    color: var(--secondary);
  }
  &.error {
    background: rgba(220, 53, 69, 0.1);
    color: #DC3545;
  }
}

.order-body {
  display: flex;
  gap: 16px;
  align-items: center;
}

.order-image {
  width: 100px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  @media (max-width: 480px) {
    display: none;
  }
}

.order-details {
  flex: 1;
}

.order-attraction {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--dark);
  margin-bottom: 6px;
}

.order-meta {
  display: flex;
  gap: 16px;
  font-size: 0.85rem;
  color: var(--text-light);
  margin-bottom: 4px;
}

.order-contact {
  display: flex;
  gap: 14px;
  font-size: 0.82rem;
  color: var(--text-lighter);
  margin-top: 4px;
}

.order-price {
  flex-shrink: 0;
}

.price-amount {
  font-size: 1.3rem;
  font-weight: 800;
  color: var(--primary);
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.04);
}

.btn-pay,
.btn-cancel {
  padding: 8px 18px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  transition: var(--transition);
}

.btn-pay {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(31, 143, 58, 0.25);
  }
}

.btn-cancel {
  border: 1px solid #DC3545;
  color: #DC3545;
  background: transparent;

  &:hover {
    background: #DC3545;
    color: white;
  }
}

/* 支付确认弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
  background: rgba(20, 37, 27, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-card {
  position: relative;
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 28px 24px 24px;
  box-shadow: 0 20px 60px rgba(20, 37, 27, 0.2);
  animation: modalIn 0.2s ease;
}

@keyframes modalIn {
  from { opacity: 0; transform: translateY(16px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.modal-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-lighter);
  transition: var(--transition);

  &:hover {
    background: rgba(0,0,0,0.06);
    color: var(--text);
  }
}

.modal-header {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--dark);
  margin-bottom: 16px;
}

.modal-body {
  p {
    color: var(--text-light);
    font-size: 0.9rem;
    margin-bottom: 14px;
  }
}

.pay-info {
  background: #f6faf5;
  border-radius: 10px;
  padding: 14px;

  strong {
    display: block;
    font-size: 0.95rem;
    color: var(--dark);
    margin-bottom: 6px;
  }

  span {
    display: block;
    font-size: 0.85rem;
    color: var(--text-light);
    margin-bottom: 4px;
  }

  .pay-amount {
    font-size: 1.1rem;
    font-weight: 700;
    color: var(--primary);
    margin-top: 8px;
  }
}

.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel-action,
.btn-confirm-pay {
  flex: 1;
  height: 42px;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  transition: var(--transition);
}

.btn-cancel-action {
  background: #f0f2f0;
  color: var(--text);

  &:hover { background: #e4e8e4; }
}

.btn-confirm-pay {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(31, 143, 58, 0.25);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;

  button {
    padding: 8px 20px;
    border: 1.5px solid rgba(0,0,0,0.1);
    border-radius: 8px;
    font-size: 0.9rem;
    transition: var(--transition);

    &:hover:not(:disabled) {
      border-color: var(--primary);
      color: var(--primary);
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }

  .page-info {
    font-size: 0.9rem;
    color: var(--text-light);
  }
}
</style>
