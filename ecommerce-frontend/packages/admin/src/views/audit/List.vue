<template>
  <div v-loading="loading">
    <h2>审计日志</h2>

    <div class="search-bar">
      <el-select v-model="search.serviceName" placeholder="服务" clearable style="width: 150px" @change="handleSearch">
        <el-option label="认证/用户" value="ecommerce-auth" />
        <el-option label="商品" value="ecommerce-product" />
        <el-option label="库存" value="ecommerce-inventory" />
        <el-option label="订单" value="ecommerce-order" />
        <el-option label="营销" value="ecommerce-marketing" />
        <el-option label="售后" value="ecommerce-aftersales" />
      </el-select>
      <el-input v-model="search.module" placeholder="模块" clearable style="width: 120px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-input v-model="search.userId" placeholder="用户ID" clearable style="width: 120px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="width: 380px"
        @change="handleDateChange"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="logs" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="serviceName" label="服务" width="140" />
      <el-table-column prop="module" label="模块" width="80" />
      <el-table-column prop="operation" label="操作" width="120" />
      <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column label="耗时" width="80">
        <template #default="{ row }">
          <span :class="{ 'slow': row.duration > 1000 }">{{ row.duration }}ms</span>
        </template>
      </el-table-column>
      <el-table-column label="响应码" width="80">
        <template #default="{ row }">
          <el-tag :type="row.responseCode === 200 ? 'success' : 'danger'" size="small">
            {{ row.responseCode }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="参数" width="80">
        <template #default="{ row }">
          <el-button v-if="row.requestParams" size="small" text type="primary" @click="showParams(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[20, 50, 100]"
        @current-change="loadLogs"
        @size-change="handleSearch"
      />
    </div>

    <el-dialog v-model="paramsVisible" title="请求参数" width="600px">
      <pre class="params-content">{{ paramsText }}</pre>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { auditLogApi } from '@ecommerce/shared'
import type { AuditLogItem } from '@ecommerce/shared'

const loading = ref(true)
const logs = ref<AuditLogItem[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const dateRange = ref<[string, string] | null>(null)

const search = reactive({
  serviceName: '',
  module: '',
  userId: '',
  startTime: '',
  endTime: '',
})

const paramsVisible = ref(false)
const paramsText = ref('')

const loadLogs = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (search.serviceName) params.serviceName = search.serviceName
    if (search.module) params.module = search.module
    if (search.userId) params.userId = search.userId
    if (search.startTime) params.startTime = search.startTime
    if (search.endTime) params.endTime = search.endTime
    const res = await auditLogApi.list(params)
    logs.value = res || []
    total.value = logs.value.length > 0 ? (pageNum.value * pageSize.value + 1) : 0
  } catch {
    ElMessage.error('加载审计日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadLogs()
}

const handleDateChange = (val: [string, string] | null) => {
  if (val) {
    search.startTime = val[0]
    search.endTime = val[1]
  } else {
    search.startTime = ''
    search.endTime = ''
  }
  handleSearch()
}

const showParams = (row: AuditLogItem) => {
  try {
    paramsText.value = JSON.stringify(JSON.parse(row.requestParams), null, 2)
  } catch {
    paramsText.value = row.requestParams
  }
  paramsVisible.value = true
}

onMounted(loadLogs)
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.slow {
  color: #e6a23c;
  font-weight: bold;
}
.params-content {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  max-height: 400px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
