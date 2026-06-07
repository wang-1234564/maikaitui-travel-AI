<template>
  <div class="page-container">
    <div class="page-header">
      <h2>景点管理</h2>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="景点名称" clearable style="width: 180px;" @keyup.enter="handleSearch" />
      <el-cascader
        v-model="searchForm.regionId"
        :options="regionOptions"
        :props="{ label: 'name', value: 'id', children: 'children', checkStrictly: true }"
        placeholder="选择地区"
        clearable
        style="width: 200px;"
      />
      <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable style="width: 160px;">
        <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="全部" value="" />
        <el-option label="已发布" value="1" />
        <el-option label="草稿" value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
      <el-button @click="resetSearch">重置</el-button>
      <div style="flex:1" />
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 添加景点
      </el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column label="封面图片" width="80" align="center">
        <template #default="{ row }">
          <el-image
            v-if="row.coverImage"
            :src="row.coverImage"
            style="width: 50px; height: 50px; border-radius: 4px;"
            fit="cover"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="景点名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="regionName" label="所属地区" width="120" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="100" align="center">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: 600;">&yen;{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="rating" label="评分" width="100" align="center">
        <template #default="{ row }">
          <span class="star-rating">{{ '★'.repeat(Math.floor(row.rating || 0)) }}{{ '☆'.repeat(5 - Math.floor(row.rating || 0)) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="isHot" label="热门" width="70" align="center">
        <template #default="{ row }">
          <el-tag :type="row.isHot === 1 ? 'danger' : 'info'" size="small">
            {{ row.isHot === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览次数" width="90" align="center" sortable />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="景点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入景点名称" />
        </el-form-item>
        <el-form-item label="景点描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请输入景点描述" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>

        <el-divider content-position="left">分类信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属地区" prop="regionId">
              <el-cascader
                v-model="form.regionId"
                :options="regionOptions"
                :props="{ label: 'name', value: 'id', children: 'children', checkStrictly: true }"
                placeholder="请选择地区"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">位置信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="form.longitude" :precision="6" :min="-180" :max="180" placeholder="经度" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="form.latitude" :precision="6" :min="-90" :max="90" placeholder="纬度" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">价格设置</el-divider>
        <el-form-item label="门票价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" placeholder="请输入价格" style="width: 200px" />
        </el-form-item>

        <el-divider content-position="left">图片设置</el-divider>
        <el-form-item label="封面图片" prop="coverImage">
          <ImageUpload ref="coverUploadRef" v-model="form.coverImage" :limit="1" :auto-upload="false" />
        </el-form-item>
        <el-form-item label="图片列表">
          <ImageUpload ref="imagesUploadRef" v-model="form.images" :limit="9" :auto-upload="false" />
        </el-form-item>

        <el-divider content-position="left">其他设置</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="热门推荐">
              <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开放时间">
              <el-input v-model="form.openTime" placeholder="如: 08:00-18:00" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="发布状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="草稿" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getAttractions, addAttraction, updateAttraction, deleteAttraction, getRegionTree, getCategoryTree } from '@/api/tourism'
import ImageUpload from '@/components/common/ImageUpload.vue'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const searchForm = reactive({
  keyword: '',
  regionId: null,
  categoryId: '',
  status: ''
})

const regionOptions = ref([])
const categoryOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('添加景点')
const submitLoading = ref(false)
const formRef = ref(null)
const coverUploadRef = ref(null)
const imagesUploadRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  description: '',
  address: '',
  regionId: null,
  categoryId: '',
  longitude: undefined,
  latitude: undefined,
  price: 0,
  coverImage: '',
  images: [],
  isHot: 0,
  openTime: '',
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }],
  regionId: [{ required: true, message: '请选择地区', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const loadOptions = async () => {
  try {
    const [regionRes, categoryRes] = await Promise.all([getRegionTree(), getCategoryTree()])
    regionOptions.value = regionRes.data || []
    categoryOptions.value = categoryRes.data || []
  } catch (e) {
    // handled by interceptor
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...searchForm, page: pagination.page, size: pagination.size }
    if (params.regionId && Array.isArray(params.regionId)) {
      params.regionId = params.regionId[params.regionId.length - 1]
    }
    const res = await getAttractions(params)
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
  searchForm.keyword = ''
  searchForm.regionId = null
  searchForm.categoryId = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '添加景点'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑景点'
  form.id = row.id
  form.name = row.name
  form.description = row.description || ''
  form.address = row.address || ''
  form.regionId = row.regionId || null
  form.categoryId = row.categoryId || ''
  form.longitude = row.longitude
  form.latitude = row.latitude
  form.price = row.price || 0
  form.coverImage = row.coverImage || ''
  if (row.images) {
    form.images = typeof row.images === 'string' ? JSON.parse(row.images) : row.images
  } else {
    form.images = []
  }
  form.isHot = row.isHot || 0
  form.openTime = row.openTime || ''
  form.status = row.status
  dialogVisible.value = true
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.description = ''
  form.address = ''
  form.regionId = null
  form.categoryId = ''
  form.longitude = undefined
  form.latitude = undefined
  form.price = 0
  form.coverImage = ''
  form.images = []
  form.isHot = 0
  form.openTime = ''
  form.status = 1
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    // 延迟上传：先上传所有待上传的图片到 OSS
    const [coverMap, imagesMap] = await Promise.all([
      coverUploadRef.value?.uploadAll() || Promise.resolve(new Map()),
      imagesUploadRef.value?.uploadAll() || Promise.resolve(new Map())
    ])
    coverUploadRef.value?.applyUrls(coverMap)
    imagesUploadRef.value?.applyUrls(imagesMap)

    const payload = { ...form }
    if (payload.regionId && Array.isArray(payload.regionId)) {
      payload.regionId = payload.regionId[payload.regionId.length - 1]
    }
    // 后端 images 字段是 JSON 字符串，需序列化
    if (Array.isArray(payload.images)) {
      payload.images = JSON.stringify(payload.images)
    }
    if (form.id) {
      await updateAttraction(payload)
      ElMessage.success('更新成功')
    } else {
      await addAttraction(payload)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    // handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除景点 "${row.name}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteAttraction(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  loadOptions()
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
</style>
