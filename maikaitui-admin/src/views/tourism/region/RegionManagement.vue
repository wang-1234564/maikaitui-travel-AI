<template>
  <div class="page-container">
    <div class="page-header">
      <h2>地区管理</h2>
      <div class="header-actions">
        <el-button @click="toggleExpandAll">
          {{ isExpandAll ? '折叠全部' : '展开全部' }}
        </el-button>
        <el-button type="primary" @click="handleAdd(null)">
          <el-icon><Plus /></el-icon> 添加地区
        </el-button>
      </div>
    </div>

    <el-table
      ref="tableRef"
      :data="tableData"
      row-key="id"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      default-expand-all
    >
      <el-table-column prop="name" label="地区名称" min-width="200" />
      <el-table-column prop="level" label="级别" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getLevelType(row.level)" size="small">
            {{ getLevelLabel(row.level) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            :active-value="true"
            :inactive-value="false"
            inline-prompt
            size="small"
            active-text="启用"
            inactive-text="禁用"
            @change="(val) => handleStatusChange(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleAdd(row)">添加子级</el-button>
          <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="上级地区" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="regionTreeSelectData"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择上级地区 (留空为顶级)"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地区名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入地区名称" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="form.level" placeholder="请选择级别" style="width: 100%">
            <el-option label="国家" :value="1" />
            <el-option label="省份" :value="2" />
            <el-option label="城市" :value="3" />
            <el-option label="区县" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
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
import { Plus } from '@element-plus/icons-vue'
import { getRegionTree, addRegion, updateRegion, deleteRegion } from '@/api/tourism'

const loading = ref(false)
const tableData = ref([])
const tableRef = ref(null)
const isExpandAll = ref(true)
const regionTreeSelectData = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('添加地区')
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  parentId: null,
  name: '',
  level: 1,
  sortOrder: 0,
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入地区名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择级别', trigger: 'change' }]
}

const getLevelLabel = (level) => {
  const map = { 1: '国家', 2: '省份', 3: '城市', 4: '区县' }
  return map[level] || level
}

const getLevelType = (level) => {
  const map = { 1: 'danger', 2: 'warning', 3: '', 4: 'info' }
  return map[level] || 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRegionTree()
    tableData.value = res.data || []
    regionTreeSelectData.value = JSON.parse(JSON.stringify(res.data || []))
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const toggleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
  toggleRowsExpansion(tableData.value, isExpandAll.value)
}

const toggleRowsExpansion = (rows, expanded) => {
  rows.forEach((row) => {
    if (row.children && row.children.length > 0) {
      tableRef.value?.toggleRowExpansion(row, expanded)
      toggleRowsExpansion(row.children, expanded)
    }
  })
}

const handleAdd = (parentRow) => {
  resetForm()
  if (parentRow) {
    form.parentId = parentRow.id
  }
  dialogTitle.value = '添加地区'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑地区'
  form.id = row.id
  form.parentId = row.parentId || null
  form.name = row.name
  form.level = row.level
  form.sortOrder = row.sortOrder || 0
  form.status = row.status
  dialogVisible.value = true
}

const resetForm = () => {
  form.id = null
  form.parentId = null
  form.name = ''
  form.level = 1
  form.sortOrder = 0
  form.status = 1
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await updateRegion({ ...form })
      ElMessage.success('更新成功')
    } else {
      await addRegion({ ...form })
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
  ElMessageBox.confirm(
    `确定要删除地区 "${row.name}" 吗？如果存在子地区，也会一并删除。`,
    '警告',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteRegion(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleStatusChange = async (row, val) => {
  const newStatus = val ? 1 : 0
  try {
    await updateRegion({ id: row.id, status: newStatus })
    row.status = newStatus
    ElMessage.success('状态更新成功')
  } catch (e) {
    // handled by interceptor
  }
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
