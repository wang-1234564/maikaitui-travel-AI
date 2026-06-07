<template>
  <div class="page-container">
    <div class="page-header">
      <h2>菜单管理</h2>
      <div class="header-actions">
        <el-button @click="toggleExpandAll">
          {{ isExpandAll ? '折叠全部' : '展开全部' }}
        </el-button>
        <el-button type="primary" @click="handleAdd(null)">
          <el-icon><Plus /></el-icon> 添加菜单
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
      <el-table-column prop="menuName" label="菜单名称" min-width="180" />
      <el-table-column prop="icon" label="图标" width="80" align="center">
        <template #default="{ row }">
          <el-icon v-if="row.icon" :size="18">
            <component :is="row.icon" />
          </el-icon>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="menuType" label="菜单类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.menuType === 'M' ? '' : 'success'" size="small">
            {{ row.menuType === 'M' ? '目录' : '按钮' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由路径" width="160" show-overflow-tooltip />
      <el-table-column prop="permission" label="权限标识" width="180" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
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
          <el-button type="primary" link size="small" @click="handleAdd(row)">添加子菜单</el-button>
          <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="menuTreeSelectData"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            placeholder="请选择上级菜单 (留空为顶级)"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="B">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="form.menuType === 'M'">
          <el-form-item label="路由路径" prop="path">
            <el-input v-model="form.path" placeholder="请输入路由路径" />
          </el-form-item>
          <el-form-item label="组件路径">
            <el-input v-model="form.component" placeholder="请输入组件路径" />
          </el-form-item>
          <el-form-item label="图标">
            <el-input v-model="form.icon" placeholder="请输入Element Plus图标名 (如: HomeFilled)" />
          </el-form-item>
        </template>
        <template v-if="form.menuType === 'B'">
          <el-form-item label="权限标识" prop="permission">
            <el-input v-model="form.permission" placeholder="请输入权限标识 (如: system:user:add)" />
          </el-form-item>
        </template>
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
import { getMenuTree, addMenu, updateMenu, deleteMenu } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const tableRef = ref(null)
const isExpandAll = ref(true)
const menuTreeSelectData = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('添加菜单')
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  parentId: null,
  menuName: '',
  menuType: 'M',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  permission: '',
  status: 1
})

const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序号', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    tableData.value = res.data || []
    menuTreeSelectData.value = JSON.parse(JSON.stringify(res.data || []))
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
  dialogTitle.value = '添加菜单'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑菜单'
  form.id = row.id
  form.parentId = row.parentId || null
  form.menuName = row.menuName
  form.menuType = row.menuType
  form.path = row.path || ''
  form.component = row.component || ''
  form.icon = row.icon || ''
  form.sortOrder = row.sortOrder || 0
  form.permission = row.permission || ''
  form.status = row.status
  dialogVisible.value = true
}

const resetForm = () => {
  form.id = null
  form.parentId = null
  form.menuName = ''
  form.menuType = 'M'
  form.path = ''
  form.component = ''
  form.icon = ''
  form.sortOrder = 0
  form.permission = ''
  form.status = 1
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await updateMenu({ ...form })
      ElMessage.success('更新成功')
    } else {
      await addMenu({ ...form })
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
    `确定要删除菜单 "${row.menuName}" 吗？如果存在子菜单，也会一并删除。`,
    '警告',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleStatusChange = async (row, val) => {
  const newStatus = val ? 1 : 0
  try {
    await updateMenu({ id: row.id, status: newStatus })
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
