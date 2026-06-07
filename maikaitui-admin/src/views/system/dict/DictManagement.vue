<template>
  <div class="page-container">
    <div class="page-header">
      <h2>字典管理</h2>
    </div>

    <div class="dict-layout">
      <!-- 左侧字典类型 -->
      <div class="dict-left">
        <div class="panel-header">
          <h3>字典类型</h3>
        </div>
        <div class="search-bar-small">
          <el-input v-model="dictTypeSearch.keyword" placeholder="搜索字典名称/编码" clearable size="small" style="width: 180px;" @keyup.enter="fetchDictTypes" />
          <el-button type="primary" size="small" @click="fetchDictTypes">搜索</el-button>
          <el-button type="primary" size="small" @click="handleAddDictType">
            <el-icon><Plus /></el-icon> 添加字典
          </el-button>
        </div>
        <el-table
          :data="dictTypeList"
          border
          stripe
          v-loading="dictTypeLoading"
          highlight-current-row
          style="width: 100%"
          @row-click="handleSelectDictType"
        >
          <el-table-column prop="dictName" label="字典名称" min-width="120" />
          <el-table-column prop="dictType" label="字典编码" width="130" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="60" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click.stop="handleEditDictType(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click.stop="handleDeleteDictType(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-small">
          <el-pagination
            v-model:current-page="dictTypePagination.page"
            v-model:page-size="dictTypePagination.size"
            :page-sizes="[5, 10, 20]"
            :total="dictTypePagination.total"
            layout="total, prev, pager, next"
            small
            @size-change="fetchDictTypes"
            @current-change="fetchDictTypes"
          />
        </div>
      </div>

      <!-- 右侧字典数据 -->
      <div class="dict-right">
        <div class="panel-header">
          <h3>{{ currentDictTypeName || '字典数据' }}</h3>
          <el-button v-if="currentDictType" type="primary" size="small" @click="handleAddDictData">
            <el-icon><Plus /></el-icon> 添加数据
          </el-button>
        </div>
        <el-empty v-if="!currentDictType" description="请在左侧选择一个字典类型" />
        <template v-else>
          <el-table :data="dictDataList" border stripe v-loading="dictDataLoading" style="width: 100%">
            <el-table-column prop="dictLabel" label="字典标签" min-width="140" />
            <el-table-column prop="dictValue" label="字典键值" width="120" />
            <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
            <el-table-column prop="status" label="状态" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditDictData(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteDictData(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </div>
    </div>

    <!-- 字典类型添加/编辑对话框 -->
    <el-dialog
      v-model="dictTypeDialogVisible"
      :title="dictTypeDialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="resetDictTypeForm"
    >
      <el-form ref="dictTypeFormRef" :model="dictTypeForm" :rules="dictTypeFormRules" label-width="80px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="dictTypeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictType">
          <el-input v-model="dictTypeForm.dictType" placeholder="请输入字典编码" :disabled="!!dictTypeForm.id" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="dictTypeForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dictTypeDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dictTypeSubmitLoading" @click="handleSubmitDictType">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 字典数据添加/编辑对话框 -->
    <el-dialog
      v-model="dictDataDialogVisible"
      :title="dictDataDialogTitle"
      width="500px"
      :close-on-click-modal="false"
      @close="resetDictDataForm"
    >
      <el-form ref="dictDataFormRef" :model="dictDataForm" :rules="dictDataFormRules" label-width="80px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dictDataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="dictDataForm.dictValue" placeholder="请输入字典键值" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="dictDataForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="样式类名">
          <el-input v-model="dictDataForm.cssClass" placeholder="请输入CSS类名" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="dictDataForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dictDataDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="dictDataSubmitLoading" @click="handleSubmitDictData">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getDictTypes, addDictType, updateDictType, deleteDictType,
  getDictDataByType, addDictData, updateDictData, deleteDictData
} from '@/api/system'

// ============ 字典类型 ============
const dictTypeLoading = ref(false)
const dictTypeList = ref([])
const dictTypeSearch = reactive({ keyword: '' })
const dictTypePagination = reactive({ page: 1, size: 10, total: 0 })
const currentDictType = ref(null)
const currentDictTypeName = ref('')

const dictTypeDialogVisible = ref(false)
const dictTypeDialogTitle = ref('添加字典类型')
const dictTypeSubmitLoading = ref(false)
const dictTypeFormRef = ref(null)
const dictTypeForm = reactive({
  id: null,
  dictName: '',
  dictType: '',
  status: 1
})
const dictTypeFormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典编码', trigger: 'blur' }]
}

// ============ 字典数据 ============
const dictDataLoading = ref(false)
const dictDataList = ref([])

const dictDataDialogVisible = ref(false)
const dictDataDialogTitle = ref('添加字典数据')
const dictDataSubmitLoading = ref(false)
const dictDataFormRef = ref(null)
const dictDataForm = reactive({
  id: null,
  dictLabel: '',
  dictValue: '',
  sortOrder: 0,
  cssClass: '',
  status: 1
})
const dictDataFormRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序号', trigger: 'blur' }]
}

// ============ 字典类型方法 ============
const fetchDictTypes = async () => {
  dictTypeLoading.value = true
  try {
    const res = await getDictTypes({ ...dictTypeSearch, page: dictTypePagination.page, size: dictTypePagination.size })
    dictTypeList.value = res.data?.records || res.data?.list || []
    dictTypePagination.total = res.data?.total || 0
  } catch (e) {
    // handled by interceptor
  } finally {
    dictTypeLoading.value = false
  }
}

const handleSelectDictType = (row) => {
  currentDictType.value = row.dictType
  currentDictTypeName.value = row.dictName
  fetchDictData()
}

const handleAddDictType = () => {
  resetDictTypeForm()
  dictTypeDialogTitle.value = '添加字典类型'
  dictTypeDialogVisible.value = true
}

const handleEditDictType = (row) => {
  dictTypeDialogTitle.value = '编辑字典类型'
  dictTypeForm.id = row.id
  dictTypeForm.dictName = row.dictName
  dictTypeForm.dictType = row.dictType
  dictTypeForm.status = row.status
  dictTypeDialogVisible.value = true
}

const resetDictTypeForm = () => {
  dictTypeForm.id = null
  dictTypeForm.dictName = ''
  dictTypeForm.dictType = ''
  dictTypeForm.status = 1
  dictTypeFormRef.value?.resetFields()
}

const handleSubmitDictType = async () => {
  const valid = await dictTypeFormRef.value.validate().catch(() => false)
  if (!valid) return

  dictTypeSubmitLoading.value = true
  try {
    if (dictTypeForm.id) {
      await updateDictType({ ...dictTypeForm })
      ElMessage.success('更新成功')
    } else {
      await addDictType({ ...dictTypeForm })
      ElMessage.success('添加成功')
    }
    dictTypeDialogVisible.value = false
    fetchDictTypes()
  } catch (e) {
    // handled by interceptor
  } finally {
    dictTypeSubmitLoading.value = false
  }
}

const handleDeleteDictType = (row) => {
  ElMessageBox.confirm(`确定要删除字典类型 "${row.dictName}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDictType(row.id)
    ElMessage.success('删除成功')
    if (currentDictType.value === row.dictType) {
      currentDictType.value = null
      currentDictTypeName.value = ''
      dictDataList.value = []
    }
    fetchDictTypes()
  }).catch(() => {})
}

// ============ 字典数据方法 ============
const fetchDictData = async () => {
  if (!currentDictType.value) return
  dictDataLoading.value = true
  try {
    const res = await getDictDataByType(currentDictType.value)
    dictDataList.value = res.data || []
  } catch (e) {
    // handled by interceptor
  } finally {
    dictDataLoading.value = false
  }
}

const handleAddDictData = () => {
  resetDictDataForm()
  dictDataDialogTitle.value = '添加字典数据'
  dictDataDialogVisible.value = true
}

const handleEditDictData = (row) => {
  dictDataDialogTitle.value = '编辑字典数据'
  dictDataForm.id = row.id
  dictDataForm.dictLabel = row.dictLabel
  dictDataForm.dictValue = row.dictValue
  dictDataForm.sortOrder = row.sortOrder || 0
  dictDataForm.cssClass = row.cssClass || ''
  dictDataForm.status = row.status
  dictDataDialogVisible.value = true
}

const resetDictDataForm = () => {
  dictDataForm.id = null
  dictDataForm.dictLabel = ''
  dictDataForm.dictValue = ''
  dictDataForm.sortOrder = 0
  dictDataForm.cssClass = ''
  dictDataForm.status = 1
  dictDataFormRef.value?.resetFields()
}

const handleSubmitDictData = async () => {
  const valid = await dictDataFormRef.value.validate().catch(() => false)
  if (!valid) return

  dictDataSubmitLoading.value = true
  try {
    const payload = { ...dictDataForm, dictType: currentDictType.value }
    if (dictDataForm.id) {
      await updateDictData(payload)
      ElMessage.success('更新成功')
    } else {
      await addDictData(payload)
      ElMessage.success('添加成功')
    }
    dictDataDialogVisible.value = false
    fetchDictData()
  } catch (e) {
    // handled by interceptor
  } finally {
    dictDataSubmitLoading.value = false
  }
}

const handleDeleteDictData = (row) => {
  ElMessageBox.confirm(`确定要删除字典数据 "${row.dictLabel}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDictData(row.id)
    ElMessage.success('删除成功')
    fetchDictData()
  }).catch(() => {})
}

onMounted(() => {
  fetchDictTypes()
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

.dict-layout {
  display: flex;
  gap: 16px;
  height: calc(100vh - 200px);
}

.dict-left {
  width: 420px;
  min-width: 420px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

.dict-right {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.search-bar-small {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.pagination-small {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
