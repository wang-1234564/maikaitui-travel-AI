<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2>数据概览</h2>
      <span class="header-date">{{ currentDate }}</span>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-info">
              <div class="stat-label">总用户数</div>
              <div class="stat-value">{{ stats.totalUsers ?? '--' }}</div>
            </div>
            <div class="stat-icon icon-users">
              <el-icon :size="40"><UserFilled /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-info">
              <div class="stat-label">景点总数</div>
              <div class="stat-value">{{ stats.totalAttractions ?? '--' }}</div>
            </div>
            <div class="stat-icon icon-attractions">
              <el-icon :size="40"><PictureFilled /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-info">
              <div class="stat-label">今日订单</div>
              <div class="stat-value">{{ stats.todayOrders ?? '--' }}</div>
            </div>
            <div class="stat-icon icon-orders">
              <el-icon :size="40"><Tickets /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-card-inner">
            <div class="stat-info">
              <div class="stat-label">总营收</div>
              <div class="stat-value">{{ formatRevenue(stats.totalRevenue) }}</div>
            </div>
            <div class="stat-icon icon-revenue">
              <el-icon :size="40"><Money /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表行 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <div class="chart-card">
          <div class="chart-card-header"><h3>近7天订单趋势</h3></div>
          <div class="chart-body">
            <v-chart class="chart" :option="orderChartOption" autoresize />
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="chart-card-header"><h3>景点分类分布</h3></div>
          <div class="chart-body">
            <v-chart class="chart" :option="categoryPieOption" autoresize />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近订单 -->
    <div class="table-card">
      <div class="chart-card-header">
        <h3>最近订单</h3>
        <el-button text type="primary" @click="$router.push('/tourism/order')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="recentOrders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="200" />
        <el-table-column prop="attractionName" label="景点名称" min-width="140" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="totalPrice" label="金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600;">{{ formatRevenue(row.totalPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.orderStatus)" size="small">
              {{ getOrderStatusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'
import { getDashboardData } from '@/api/tourism'

use([CanvasRenderer, LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const currentDate = computed(() => dayjs().format('YYYY年MM月DD日 dddd'))

const stats = reactive({
  totalUsers: null,
  totalAttractions: null,
  todayOrders: null,
  totalRevenue: null
})

const recentOrders = ref([])
const orderTrendData = ref([])
const categoryDistData = ref([])

const formatRevenue = (val) => {
  if (val == null) return '--'
  const n = Number(val)
  return n >= 10000 ? `¥${(n / 10000).toFixed(1)}万` : `¥${n}`
}

const getOrderStatusText = (status) => {
  const map = { pending: '待支付', paid: '已支付', completed: '已完成', cancelled: '已取消' }
  return map[status] || status || '--'
}

const getOrderStatusType = (status) => {
  const map = { pending: 'warning', paid: 'success', completed: '', cancelled: 'info' }
  return map[status] || 'info'
}

const orderChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: orderTrendData.value.map(d => d.date)
  },
  yAxis: { type: 'value', minInterval: 1 },
  series: [{
    name: '订单数量',
    type: 'line',
    smooth: true,
    data: orderTrendData.value.map(d => d.count),
    itemStyle: { color: '#667eea' },
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0.02)' }
        ]
      }
    }
  }]
}))

const categoryPieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', right: '5%', top: 'center' },
  series: [{
    name: '景点分类',
    type: 'pie',
    radius: ['45%', '75%'],
    center: ['35%', '50%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
    data: categoryDistData.value,
    color: ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#fa709a', '#f5a623', '#fc7b7b']
  }]
}))

onMounted(async () => {
  try {
    const res = await getDashboardData()
    const data = res?.data || res
    if (data) {
      stats.totalUsers = data.totalUsers
      stats.totalAttractions = data.totalAttractions
      stats.todayOrders = data.todayOrders
      stats.totalRevenue = data.totalRevenue
      orderTrendData.value = data.orderTrend || []
      categoryDistData.value = data.categoryDistribution || []
      recentOrders.value = data.recentOrders || []
    }
  } catch {
    // keep -- display
  }
})
</script>

<style scoped>
.dashboard-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; color: #303133; }
.header-date { font-size: 14px; color: #909399; }
.stat-row { margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: transform .2s, box-shadow .2s; cursor: pointer; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.stat-card-inner { display: flex; align-items: center; justify-content: space-between; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; margin-bottom: 8px; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; opacity: 0.8; }
.icon-users { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; }
.icon-attractions { background: linear-gradient(135deg, #f093fb, #f5576c); color: #fff; }
.icon-orders { background: linear-gradient(135deg, #4facfe, #00f2fe); color: #fff; }
.icon-revenue { background: linear-gradient(135deg, #43e97b, #38f9d7); color: #fff; }
.chart-row { margin-bottom: 20px; }
.chart-card, .table-card { background: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.chart-card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.chart-card-header h3 { font-size: 16px; font-weight: 600; color: #303133; }
.chart-body { width: 100%; }
.chart { height: 300px; width: 100%; }
.table-card { margin-bottom: 20px; }
</style>
