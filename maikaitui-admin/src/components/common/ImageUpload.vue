<template>
  <div class="image-upload">
    <div class="upload-list">
      <div v-for="(url, index) in urlList" :key="index" class="upload-item">
        <el-image :src="url" fit="cover" class="upload-preview" />
        <div class="upload-actions">
          <el-icon class="upload-preview-icon" @click="previewImage(url)"><ZoomIn /></el-icon>
          <el-icon class="upload-delete-icon" @click="removeImage(index)"><Delete /></el-icon>
        </div>
      </div>
      <div v-if="urlList.length < limit" class="upload-trigger" @click="triggerUpload">
        <el-icon class="upload-plus"><Plus /></el-icon>
        <span>{{ urlList.length === 0 ? '上传图片' : '继续上传' }}</span>
      </div>
    </div>
    <input ref="fileInput" type="file" :accept="accept" @change="handleFileChange" style="display: none" />
    <el-dialog v-model="previewVisible" title="图片预览" width="600px" :close-on-click-modal="true">
      <img :src="previewUrl" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, ZoomIn, Delete } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/file'

const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  limit: { type: Number, default: 1 },
  accept: { type: String, default: 'image/*' },
  autoUpload: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue'])

const fileInput = ref(null)
const previewVisible = ref(false)
const previewUrl = ref('')
const uploading = ref(false)

// 延迟模式：blobUrl → File 映射，等父组件调用 uploadAll 时上传
const pendingMap = ref(new Map())

const urlList = computed(() => {
  if (!props.modelValue) return []
  if (Array.isArray(props.modelValue)) return props.modelValue.filter(Boolean)
  return [props.modelValue].filter(Boolean)
})

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFileChange(e) {
  const file = e.target.files[0]
  if (!file) return

  if (props.autoUpload) {
    await doUpload(file)
  } else {
    const blobUrl = URL.createObjectURL(file)
    pendingMap.value.set(blobUrl, file)
    if (props.limit === 1) {
      emit('update:modelValue', blobUrl)
    } else {
      emit('update:modelValue', [...urlList.value, blobUrl])
    }
  }

  if (fileInput.value) fileInput.value.value = ''
}

async function doUpload(file) {
  uploading.value = true
  try {
    const res = await uploadFile(file)
    const newUrl = res.fileUrl || res.data?.fileUrl
    if (!newUrl) {
      ElMessage.error('上传失败，未获取到文件URL')
      return null
    }
    if (props.limit === 1) {
      emit('update:modelValue', newUrl)
    } else {
      emit('update:modelValue', [...urlList.value, newUrl])
    }
    ElMessage.success('上传成功')
    return newUrl
  } catch {
    ElMessage.error('上传失败，请重试')
    return null
  } finally {
    uploading.value = false
  }
}

function removeImage(index) {
  const removedUrl = urlList.value[index]
  if (pendingMap.value.has(removedUrl)) {
    pendingMap.value.delete(removedUrl)
    URL.revokeObjectURL(removedUrl)
  }
  if (props.limit === 1) {
    emit('update:modelValue', '')
  } else {
    const list = [...urlList.value]
    list.splice(index, 1)
    emit('update:modelValue', list)
  }
}

function previewImage(url) {
  previewUrl.value = url
  previewVisible.value = true
}

/**
 * 延迟模式：上传所有待上传文件，返回 { blobUrl → ossUrl } 的映射
 */
async function uploadAll() {
  const result = new Map()
  for (const [blobUrl, file] of pendingMap.value.entries()) {
    const ossUrl = await doUploadForLazy(blobUrl, file)
    if (ossUrl) {
      result.set(blobUrl, ossUrl)
    }
  }
  return result
}

async function doUploadForLazy(blobUrl, file) {
  uploading.value = true
  try {
    const res = await uploadFile(file)
    const ossUrl = res.fileUrl || res.data?.fileUrl
    if (!ossUrl) {
      ElMessage.error('上传失败')
      return null
    }
    pendingMap.value.delete(blobUrl)
    URL.revokeObjectURL(blobUrl)
    return ossUrl
  } catch {
    ElMessage.error('上传失败，请重试')
    return null
  } finally {
    uploading.value = false
  }
}

/**
 * 延迟模式：用上传后的 OSS URL 替换 blob URL
 */
function applyUrls(urlMap) {
  if (props.limit === 1) {
    for (const [blobUrl, ossUrl] of urlMap) {
      if (props.modelValue === blobUrl) {
        emit('update:modelValue', ossUrl)
        return
      }
    }
  } else {
    const list = [...urlList.value]
    for (let i = 0; i < list.length; i++) {
      const ossUrl = urlMap.get(list[i])
      if (ossUrl) list[i] = ossUrl
    }
    emit('update:modelValue', list)
  }
}

function getPendingCount() {
  return pendingMap.value.size
}

defineExpose({ uploadAll, applyUrls, getPendingCount })
</script>

<style scoped>
.image-upload {
  width: 100%;
}

.upload-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.upload-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
}

.upload-preview {
  width: 100%;
  height: 100%;
}

.upload-actions {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.upload-item:hover .upload-actions {
  opacity: 1;
}

.upload-preview-icon,
.upload-delete-icon {
  font-size: 20px;
  color: #fff;
  cursor: pointer;
}

.upload-preview-icon:hover {
  color: #409eff;
}

.upload-delete-icon:hover {
  color: #f56c6c;
}

.upload-trigger {
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  color: #909399;
  font-size: 13px;
  transition: all 0.2s;
}

.upload-trigger:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-plus {
  font-size: 24px;
}
</style>
