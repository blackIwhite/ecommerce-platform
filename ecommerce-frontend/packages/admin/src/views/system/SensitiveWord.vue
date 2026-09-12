<template>
  <div class="sensitive-page">
    <div class="page-header">
      <h2>敏感词管理</h2>
      <div>
        <el-button @click="handleReload">重载词库</el-button>
        <el-button type="primary" @click="batchDialogVisible = true">批量添加</el-button>
      </div>
    </div>

    <el-card shadow="never" style="margin-bottom: 16px">
      <template #header><span style="font-weight: 600">过滤测试</span></template>
      <el-input v-model="testInput" type="textarea" :rows="3" placeholder="输入文本测试敏感词过滤效果" />
      <el-button type="primary" style="margin-top: 8px" @click="handleTest" :loading="testLoading">测试</el-button>
      <div v-if="testResult" style="margin-top: 12px">
        <el-tag :type="testResult.containsSensitiveWord ? 'danger' : 'success'" size="small">
          {{ testResult.containsSensitiveWord ? '包含敏感词' : '未检测到敏感词' }}
        </el-tag>
        <div v-if="testResult.containsSensitiveWord" style="margin-top: 8px">
          <span style="color: #666; font-size: 13px">过滤后：</span>
          <span style="color: #e6a23c">{{ testResult.filteredText }}</span>
        </div>
      </div>
    </el-card>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="word" label="敏感词" min-width="200" />
      <el-table-column prop="createTime" label="添加时间" width="170" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
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

    <el-dialog v-model="batchDialogVisible" title="批量添加敏感词" width="500px">
      <el-input v-model="batchWords" type="textarea" :rows="6"
        placeholder="每行一个敏感词，或用逗号分隔" />
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="handleBatchAdd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { sensitiveWordApi } from '@ecommerce/shared'
import type { SensitiveWordDTO } from '@ecommerce/shared'

const loading = ref(false)
const tableData = ref<SensitiveWordDTO[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20 })

const testInput = ref('')
const testResult = ref<{ containsSensitiveWord: boolean; filteredText: string } | null>(null)
const testLoading = ref(false)

const batchDialogVisible = ref(false)
const batchWords = ref('')
const batchLoading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await sensitiveWordApi.page(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleTest = async () => {
  if (!testInput.value.trim()) { ElMessage.warning('请输入测试文本'); return }
  testLoading.value = true
  try {
    testResult.value = await sensitiveWordApi.test(testInput.value)
  } finally {
    testLoading.value = false
  }
}

const handleBatchAdd = async () => {
  if (!batchWords.value.trim()) { ElMessage.warning('请输入敏感词'); return }
  batchLoading.value = true
  try {
    await sensitiveWordApi.batchAdd(batchWords.value)
    ElMessage.success('添加成功')
    batchDialogVisible.value = false
    batchWords.value = ''
    loadData()
  } finally {
    batchLoading.value = false
  }
}

const handleReload = async () => {
  await sensitiveWordApi.reload()
  ElMessage.success('词库已重载')
}

const handleDelete = async (id: number) => {
  await sensitiveWordApi.remove(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.sensitive-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
