<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="handleClose">
      <div class="modal-card">
        <!-- 右上角关闭按钮 -->
        <button class="modal-close" @click="handleClose" aria-label="关闭">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>

        <!-- 标题 -->
        <div class="modal-header">
          <span class="modal-icon">🎫</span>
          <h3>购买门票</h3>
        </div>

        <!-- 景区信息 -->
        <div class="attraction-info">
          <img :src="attraction.coverImage || defaultImage" :alt="attraction.name" class="attraction-thumb" />
          <div>
            <strong class="attraction-name">{{ attraction.name }}</strong>
            <span class="attraction-price">¥{{ formatPrice(attraction.price) }} / 张</span>
          </div>
        </div>

        <!-- 数量选择 -->
        <div class="form-group">
          <label>数量</label>
          <div class="quantity-selector">
            <button @click="form.quantity = Math.max(1, form.quantity - 1)" :disabled="form.quantity <= 1">−</button>
            <span class="quantity-value">{{ form.quantity }}</span>
            <button @click="form.quantity = Math.min(10, form.quantity + 1)" :disabled="form.quantity >= 10">+</button>
          </div>
        </div>

        <!-- 游览日期 -->
        <div class="form-group">
          <label>📅 游览日期</label>
          <input type="date" v-model="form.visitDate" :min="minDate" class="form-input" />
        </div>

        <!-- 联系人 -->
        <div class="form-row">
          <div class="form-group flex-1">
            <label>👤 联系人</label>
            <input type="text" v-model="form.contactName" placeholder="请输入姓名" class="form-input" maxlength="20" />
          </div>
          <div class="form-group flex-1">
            <label>📱 手机号</label>
            <input type="tel" v-model="form.contactPhone" placeholder="请输入手机号" class="form-input" maxlength="11" />
          </div>
        </div>

        <!-- 合计 -->
        <div class="total-row">
          <span>合计</span>
          <strong>¥{{ totalPrice }}</strong>
        </div>

        <!-- 操作按钮 -->
        <div class="modal-actions">
          <button class="btn-pay-now" @click="handleSubmit(true)" :disabled="submitting">
            {{ submitting ? '提交中...' : '💰 立即支付' }}
          </button>
          <button class="btn-pay-later" @click="handleSubmit(false)" :disabled="submitting">
            {{ submitting ? '提交中...' : '📋 稍后支付' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { createOrder } from '@/api'
import { formatPrice } from '@/utils/travel'

const props = defineProps({
  attraction: { type: Object, required: true }
})

const emit = defineEmits(['close', 'success'])

const submitting = ref(false)

const minDate = computed(() => new Date().toISOString().split('T')[0])

const defaultImage = computed(() =>
  `https://picsum.photos/seed/${props.attraction.id || 'ticket'}/200/140`
)

const form = reactive({
  quantity: 1,
  visitDate: minDate.value,
  contactName: '',
  contactPhone: ''
})

const totalPrice = computed(() => {
  const price = Number(props.attraction.price || 0)
  return (price * form.quantity).toFixed(2)
})

function handleClose() {
  if (!submitting.value) emit('close')
}

async function handleSubmit(payNow) {
  if (!form.contactName.trim()) {
    alert('请输入联系人姓名')
    return
  }
  if (!form.contactPhone.trim() || !/^1\d{10}$/.test(form.contactPhone.trim())) {
    alert('请输入正确的11位手机号')
    return
  }

  submitting.value = true
  try {
    await createOrder({
      attractionId: props.attraction.id,
      attractionName: props.attraction.name,
      quantity: form.quantity,
      totalPrice: parseFloat(totalPrice.value),
      visitDate: form.visitDate || null,
      contactName: form.contactName.trim(),
      contactPhone: form.contactPhone.trim()
    }, payNow)

    alert(payNow ? '支付成功！' : '下单成功，请尽快完成支付')
    emit('success')
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(20, 37, 27, 0.45);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-card {
  position: relative;
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 16px;
  padding: 32px 28px 28px;
  box-shadow: 0 24px 64px rgba(20, 37, 27, 0.2);
  animation: modalIn 0.25s ease;
}

@keyframes modalIn {
  from { opacity: 0; transform: translateY(20px) scale(0.96); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

/* 关闭按钮 */
.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
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

/* 标题 */
.modal-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;

  .modal-icon { font-size: 1.5rem; }

  h3 {
    font-size: 1.25rem;
    font-weight: 800;
    color: var(--dark);
  }
}

/* 景区信息 */
.attraction-info {
  display: flex;
  gap: 14px;
  padding: 14px;
  background: #f6faf5;
  border-radius: 12px;
  margin-bottom: 22px;
  align-items: center;
}

.attraction-thumb {
  width: 80px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.attraction-name {
  display: block;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--dark);
  margin-bottom: 6px;
}

.attraction-price {
  font-size: 0.85rem;
  color: var(--primary);
  font-weight: 600;
}

/* 表单 */
.form-group {
  margin-bottom: 18px;

  label {
    display: block;
    font-size: 0.85rem;
    font-weight: 600;
    color: var(--text);
    margin-bottom: 8px;
  }
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid rgba(0,0,0,0.1);
  border-radius: 10px;
  font-size: 0.95rem;
  transition: var(--transition);
  background: #fafbfa;

  &:focus {
    border-color: var(--primary);
    background: #fff;
    box-shadow: 0 0 0 3px rgba(31, 143, 58, 0.08);
  }
}

.form-row {
  display: flex;
  gap: 14px;

  .flex-1 { flex: 1; }
}

/* 数量选择 */
.quantity-selector {
  display: inline-flex;
  align-items: center;
  border: 1.5px solid rgba(0,0,0,0.1);
  border-radius: 10px;
  overflow: hidden;

  button {
    width: 42px;
    height: 40px;
    font-size: 1.2rem;
    font-weight: 600;
    color: var(--text);
    transition: var(--transition);

    &:hover:not(:disabled) {
      background: rgba(31, 143, 58, 0.08);
      color: var(--primary);
    }

    &:disabled {
      opacity: 0.3;
      cursor: not-allowed;
    }
  }

  .quantity-value {
    width: 50px;
    text-align: center;
    font-size: 1.1rem;
    font-weight: 700;
    color: var(--dark);
  }
}

/* 合计 */
.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  margin: 6px 0 20px;
  border-top: 1px solid rgba(0,0,0,0.06);

  span { font-size: 0.95rem; color: var(--text-light); }
  strong {
    font-size: 1.5rem;
    font-weight: 800;
    color: var(--primary);
  }
}

/* 按钮 */
.modal-actions {
  display: flex;
  gap: 12px;
}

.btn-pay-now,
.btn-pay-later {
  flex: 1;
  height: 46px;
  border-radius: 12px;
  font-size: 0.95rem;
  font-weight: 700;
  transition: var(--transition);

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.btn-pay-now {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;
  box-shadow: 0 4px 16px rgba(31, 143, 58, 0.25);

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(31, 143, 58, 0.35);
  }
}

.btn-pay-later {
  background: #f5f7f5;
  color: var(--text);
  border: 1px solid rgba(0,0,0,0.08);

  &:hover:not(:disabled) {
    background: #eef2ed;
    border-color: rgba(0,0,0,0.15);
  }
}
</style>
