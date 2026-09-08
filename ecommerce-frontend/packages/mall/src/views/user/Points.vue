<template>
  <div class="points-page">
    <div class="page-header">
      <el-button text @click="$router.push('/user')">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>积分中心</h2>
    </div>

    <div class="points-card" v-loading="loading">
      <div class="points-balance">
        <div class="balance-label">可用积分</div>
        <div class="balance-value">{{ account?.availablePoints ?? 0 }}</div>
        <div class="balance-level">
          <el-tag :type="levelTagType">{{ account?.levelName ?? '普通用户' }}</el-tag>
        </div>
      </div>
      <div class="points-stats">
        <div class="stat-item">
          <div class="stat-value">{{ account?.totalPoints ?? 0 }}</div>
          <div class="stat-label">累计获得</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ account?.usedPoints ?? 0 }}</div>
          <div class="stat-label">已使用</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ account?.expiredPoints ?? 0 }}</div>
          <div class="stat-label">已过期</div>
        </div>
      </div>
    </div>

    <h3 style="margin: 20px 0 12px">积分明细</h3>

    <el-tabs v-model="logFilter" @tab-change="loadLogs">
      <el-tab-pane label="全部" :name="undefined" />
      <el-tab-pane label="获得" :name="1" />
      <el-tab-pane label="消费" :name="2" />
      <el-tab-pane label="过期" :name="3" />
    </el-tabs>

    <el-table :data="logs" v-loading="logsLoading" style="width: 100%">
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'success' : row.type === 2 ? 'warning' : 'info'" size="small">
            {{ row.typeName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分" width="100">
        <template #default="{ row }">
          <span :style="{ color: row.type === 1 ? '#67c23a' : '#f56c6c' }">
            {{ row.type === 1 ? '+' : '-' }}{{ row.points }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="余额" width="100" />
      <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="170" />
    </el-table>

    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadLogs"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { pointsApi } from '@ecommerce/shared'
import type { PointsAccountDTO, PointsLogDTO } from '@ecommerce/shared'

const loading = ref(true)
const logsLoading = ref(false)
const account = ref<PointsAccountDTO | null>(null)
const logs = ref<PointsLogDTO[]>([])
const logFilter = ref<number | undefined>(undefined)
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)

const levelTagType = computed(() => {
  const level = account.value?.level ?? 1
  if (level >= 4) return 'danger'
  if (level >= 3) return 'warning'
  if (level >= 2) return ''
  return 'info'
})

async function loadAccount() {
  loading.value = true
  try {
    account.value = await pointsApi.getAccount()
  } finally {
    loading.value = false
  }
}

async function loadLogs() {
  logsLoading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize }
    if (logFilter.value !== undefined) {
      params.type = logFilter.value
    }
    const res = await pointsApi.getLogs(params)
    logs.value = res.list
    total.value = res.total
  } finally {
    logsLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadAccount(), loadLogs()])
})
</script>

<style scoped>
.points-page { max-width: 800px; margin: 0 auto; padding: 20px; }
.page-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.points-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px; padding: 24px; color: #fff;
}
.points-balance { text-align: center; margin-bottom: 20px; }
.balance-label { font-size: 14px; opacity: 0.8; }
.balance-value { font-size: 42px; font-weight: bold; margin: 4px 0; }
.balance-level { margin-top: 8px; }
.points-stats { display: flex; justify-content: space-around; }
.stat-item { text-align: center; }
.stat-value { font-size: 20px; font-weight: 600; }
.stat-label { font-size: 12px; opacity: 0.7; margin-top: 4px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 16px; }
</style>
