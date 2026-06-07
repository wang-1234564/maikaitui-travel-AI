<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.orderNo" placeholder="订单编号" clearable style="width: 180px;" @keyup.enter="handleSearch" />
      <el-input v-model="searchForm.username" placeholder="用户名" clearable style="width: 140px;" @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.orderStatus" placeholder="订单状态" clearable style="width: 130px;">
        <el-option label="全部" value="" />
        <el-option label="待支付" value="pending" />
        <el-option label="已支付" value="paid" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-date-picker
        v-model="searchForm.dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 240px;"
      />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="orderNo" label="订单编号" width="200">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleViewDetail(row)">
            {{ row.orderNo }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="attractionName" label="景点名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="contactName" label="联系人" width="100" />
      <el-table-column prop="contactPhone" label="联系电话" width="130" />
      <el-table-column prop="quantity" label="数量" width="70" align="center" />
      <el-table-column prop="totalPrice" label="订单金额" width="110" align="center">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: 600;">&yen;{{ row.totalPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="orderStatus" label="订单状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.orderStatus)" size="small">
            {{ getStatusText(row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="visitDate" label="游览日期" width="120" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="canCancel(row.orderStatus)"
            type="danger"
            link
            size="small"
            @click="handleCancel(row)"
          >取消</el-button>
          <el-dropdown v-if="canChangeStatus(row.orderStatus)" @command="(cmd) => handleStatusChange(row, cmd)">
            <el-button type="primary" link size="small">
              状态变更 <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="next in getNextStatuses(row.orderStatus)"
                  :key="next.value"
                  :command="next.value"
                >{{ next.label }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </div>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px" :close-on-click-modal="false">
      <el-descriptions v-if="detailRow" :column="2" border>
        <el-descriptions-item label="订单编号">{{ detailRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(detailRow.orderStatus)" size="small">
            {{ getStatusText(detailRow.orderStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="景点名称">{{ detailRow.attractionName }}</el-descriptions-item>
        <el-descriptions-item label="门票单价">&yen;{{ detailRow.unitPrice }}</el-descriptions-item>
        <el-descriptions-item label="购买数量">{{ detailRow.quantity }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">&yen;{{ detailRow.totalPrice }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailRow.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailRow.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="游览日期">{{ detailRow.visitDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailRow.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailRow.payTime" label="支付时间">{{ detailRow.payTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailRow.cancelTime" label="取消时间">{{ detailRow.cancelTime }}</el-descriptions-item>
        <el-descriptions-item v-if="detailRow.remark" label="备注" :span="2">{{ detailRow.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, ArrowDown } from '@element-plus/icons-vue'
import { getOrders, getOrder, updateOrderStatus, cancelOrder } from '@/api/tourism'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const searchForm = reactive({
  orderNo: '',
  username: '',
  orderStatus: '',
  dateRange: null
})

const detailDialogVisible = ref(false)
const detailRow = ref(null)

// Status constants
const STATUS_MAP = {
  pending: '待支付',
  paid: '已支付',
  completed: '已完成',
  cancelled: '已取消'
}

const STATUS_TYPE_MAP = {
  pending: 'warning',
  paid: '',
  completed: 'success',
  cancelled: 'info'
}

// Valid transitions
const TRANSITIONS = {
  pending: [{ value: 'paid', label: '标记已支付' }],
  paid: [{ value: 'completed', label: '标记已完成' }, { value: 'cancelled', label: '取消订单' }]
}

const getStatusText = (status) => STATUS_MAP[status] || status

const getStatusType = (status) => STATUS_TYPE_MAP[status] || 'info'

const canCancel = (status) => status === 'pending'

const canChangeStatus = (status) => {
  return TRANSITIONS[status] && TRANSITIONS[status].length > 0
}

const getNextStatuses = (status) => TRANSITIONS[status] || []

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...searchForm, page: pagination.page, size: pagination.size }
    if (params.dateRange && params.dateRange.length === 2) {
      params.startDate = params.dateRange[0]
      params.endDate = params.dateRange[1]
    }
    delete params.dateRange
    const res = await getOrders(params)
    tableData.value = res.data?.records || res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.username = ''
  searchForm.orderStatus = ''
  searchForm.dateRange = null
  handleSearch()
}

const handleViewDetail = async (row) => {
  try {
    const res = await getOrder(row.id)
    detailRow.value = res.data || row
    detailDialogVisible.value = true
  } catch (e) {
    detailRow.value = row
    detailDialogVisible.value = true
  }
}

const handleStatusChange = (row, newStatus) => {
  const statusText = getStatusText(newStatus)
  ElMessageBox.confirm(`确定将订单 "${row.orderNo}" 状态变更为"${statusText}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await updateOrderStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    row.orderStatus = newStatus
    fetchData()
  }).catch(() => {})
}

const handleCancel = (row) => {
  ElMessageBox.confirm(`确定要取消订单 "${row.orderNo}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await cancelOrder(row.id)
    ElMessage.success('订单已取消')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
