<template>
  <div class="page-container">
    <div class="page-header">
      <h2>评论管理</h2>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.attractionName" placeholder="景点名称" clearable style="width: 180px;" @keyup.enter="handleSearch" />
      <el-input v-model="searchForm.keyword" placeholder="评论内容关键字" clearable style="width: 200px;" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="attractionName" label="所属景点" width="150" show-overflow-tooltip />
      <el-table-column prop="username" label="用户名" width="110" />
      <el-table-column label="评论内容" min-width="250">
        <template #default="{ row }">
          <span class="comment-content">{{ truncateContent(row.content) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="rating" label="评分" width="120" align="center">
        <template #default="{ row }">
          <span class="star-rating">{{ '★'.repeat(Math.floor(row.rating || 0)) }}{{ '☆'.repeat(5 - Math.floor(row.rating || 0)) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="评论时间" width="170" />
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <!-- 评论详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评论详情" width="600px" :close-on-click-modal="false">
      <div v-if="detailRow" class="comment-detail">
        <div class="comment-header">
          <el-avatar :size="48" :src="detailRow.avatar" />
          <div class="comment-user-info">
            <div class="comment-username">{{ detailRow.username }}</div>
            <div class="comment-time">{{ detailRow.createTime }}</div>
          </div>
          <div class="comment-rating">
            <span class="star-rating">{{ '★'.repeat(Math.floor(detailRow.rating || 0)) }}{{ '☆'.repeat(5 - Math.floor(detailRow.rating || 0)) }}</span>
          </div>
        </div>
        <div class="comment-body">
          <div class="comment-label">评论内容</div>
          <div class="comment-text">{{ detailRow.content }}</div>
        </div>
        <div v-if="detailRow.images && detailRow.images.length > 0" class="comment-images">
          <div class="comment-label">评论图片</div>
          <div class="image-list">
            <el-image
              v-for="(img, idx) in detailImageList"
              :key="idx"
              :src="img"
              style="width: 120px; height: 120px; border-radius: 6px; margin-right: 10px; margin-bottom: 10px;"
              fit="cover"
              :preview-src-list="detailImageList"
              :initial-index="idx"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAllComments, deleteComment } from '@/api/tourism'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const searchForm = reactive({
  attractionName: '',
  keyword: ''
})

const detailDialogVisible = ref(false)
const detailRow = ref(null)

const detailImageList = computed(() => {
  if (!detailRow.value || !detailRow.value.images) return []
  if (typeof detailRow.value.images === 'string') {
    try {
      return JSON.parse(detailRow.value.images)
    } catch (e) {
      return [detailRow.value.images]
    }
  }
  return detailRow.value.images
})

const truncateContent = (content) => {
  if (!content) return ''
  return content.length > 50 ? content.substring(0, 50) + '...' : content
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getAllComments(params)
    const data = res?.data || res
    tableData.value = data?.records || data?.list || []
    pagination.total = data?.total || 0
  } catch (e) {
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const resetSearch = () => {
  searchForm.attractionName = ''
  searchForm.keyword = ''
  handleSearch()
}

const handleView = (row) => {
  detailRow.value = row
  detailDialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该评论吗？删除后不可恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteComment(row.id)
    ElMessage.success('删除成功')
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

.star-rating {
  color: #e6a23c;
  font-size: 14px;
  letter-spacing: 2px;
}

.comment-content {
  color: #606266;
  line-height: 1.5;
}

.comment-detail {
  padding: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.comment-user-info {
  flex: 1;
}

.comment-username {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.comment-time {
  font-size: 13px;
  color: #909399;
}

.comment-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.comment-body {
  margin-bottom: 20px;
}

.comment-text {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  background: #f5f7fa;
  padding: 14px;
  border-radius: 6px;
}

.comment-images {
  margin-bottom: 10px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
}
</style>
