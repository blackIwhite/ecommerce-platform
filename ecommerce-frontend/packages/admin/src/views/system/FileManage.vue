<template>
  <div class="file-page">
    <div class="page-header">
      <h2>文件管理</h2>
      <el-upload :show-file-list="false" :before-upload="handleUpload" accept="image/*">
        <el-button type="primary">上传文件</el-button>
      </el-upload>
    </div>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="预览" width="80">
        <template #default="{ row }">
          <el-image v-if="row.contentType?.startsWith('image/')" :src="row.url"
            style="width: 40px; height: 40px" fit="cover" :preview-src-list="[row.url]" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
      <el-table-column label="大小" width="100">
        <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="contentType" label="类型" width="120" />
      <el-table-column prop="createTime" label="上传时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="copyUrl(row.url)">复制链接</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pagination" v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize" :total="total" :page-sizes="[20, 50, 100]"
      layout="total, sizes, prev, pager, next" @change="loadData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fileApi } from '@ecommerce/shared'
import type { FileDTO } from '@ecommerce/shared'

const loading = ref(false)
const tableData = ref<FileDTO[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20 })

const formatSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await fileApi.list(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleUpload = async (file: File) => {
  try {
    await fileApi.upload(file)
    ElMessage.success('上传成功')
    loadData()
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  }
  return false
}

const copyUrl = (url: string) => {
  navigator.clipboard.writeText(url)
  ElMessage.success('链接已复制')
}

const handleDelete = async (id: number) => {
  await fileApi.delete(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.file-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
