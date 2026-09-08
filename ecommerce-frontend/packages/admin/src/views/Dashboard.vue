<template>
  <div v-loading="loading">
    <h2>数据概览</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="商品总数" :value="stats.productCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="用户总数" :value="stats.userCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="订单总数" :value="stats.orderCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="待发货订单" :value="stats.pendingShipCount" />
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>服务健康状态</span>
          <div>
            <el-tag :type="healthSummaryType" size="small" style="margin-right: 8px">
              {{ healthyCount }}/{{ services.length }} 正常
            </el-tag>
            <el-button size="small" :loading="healthLoading" @click="fetchHealth">
              刷新
            </el-button>
          </div>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="svc in services" :key="svc.name" style="margin-bottom: 16px">
          <div class="health-card" :class="'health-' + svc.status.toLowerCase()">
            <div class="health-dot" :class="'dot-' + svc.status.toLowerCase()"></div>
            <div class="health-info">
              <div class="health-name">{{ svc.displayName }}</div>
              <div class="health-status">{{ statusLabel(svc.status) }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>最近订单</template>
          <el-table :data="recentOrders" size="small">
            <el-table-column prop="orderNo" label="订单号" />
            <el-table-column label="金额" width="100">
              <template #default="{ row }">{{ formatPrice(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>最近用户</template>
          <el-table :data="recentUsers" size="small">
            <el-table-column prop="phone" label="手机号" />
            <el-table-column prop="nickname" label="昵称" />
            <el-table-column label="注册时间" width="160">
              <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { productApi, orderApi, userApi, monitorApi, formatPrice, formatDate } from '@ecommerce/shared'
import type { OrderDTO, UserDTO, ServiceHealthStatus } from '@ecommerce/shared'

const loading = ref(true)
const healthLoading = ref(false)
const recentOrders = ref<OrderDTO[]>([])
const recentUsers = ref<UserDTO[]>([])
const services = ref<ServiceHealthStatus[]>([])
let healthTimer: ReturnType<typeof setInterval> | null = null

const stats = reactive({
  productCount: 0,
  userCount: 0,
  orderCount: 0,
  pendingShipCount: 0,
})

const statusMap: Record<number, string> = {
  0: '待付款', 1: '已付款', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消',
}
const statusText = (s: number) => statusMap[s] || '未知'
const statusTagType = (s: number): string => {
  const m: Record<number, string> = { 0: 'warning', 2: '', 3: '', 4: 'success', 5: 'info' }
  return m[s] || 'info'
}

const healthyCount = computed(() => services.value.filter(s => s.status === 'UP').length)
const healthSummaryType = computed(() => {
  if (healthyCount.value === services.value.length) return 'success'
  if (healthyCount.value > 0) return 'warning'
  return 'danger'
})

const statusLabel = (status: string) => {
  const m: Record<string, string> = { UP: '运行中', DOWN: '已停止', UNKNOWN: '未知' }
  return m[status] || status
}

const fetchHealth = async () => {
  healthLoading.value = true
  try {
    services.value = await monitorApi.health()
  } catch {
    // ignore
  }
  healthLoading.value = false
}

onMounted(async () => {
  try {
    const [products, orders, users, pendingShip] = await Promise.all([
      productApi.getSpuPage({ pageNum: 1, pageSize: 1 }),
      orderApi.adminList({ pageNum: 1, pageSize: 5 }),
      userApi.page({ pageNum: 1, pageSize: 5 }),
      orderApi.adminList({ pageNum: 1, pageSize: 1, status: 2 }),
    ])

    stats.productCount = products.total
    stats.orderCount = orders.total
    stats.userCount = users.total
    stats.pendingShipCount = pendingShip.total
    recentOrders.value = orders.list
    recentUsers.value = users.list
  } catch {}
  loading.value = false

  await fetchHealth()
  healthTimer = setInterval(fetchHealth, 30000)
})

onUnmounted(() => {
  if (healthTimer) clearInterval(healthTimer)
})
</script>

<style scoped>
.health-card {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  transition: all 0.2s;
}
.health-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.health-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 12px;
  flex-shrink: 0;
}
.dot-up {
  background-color: #67c23a;
  box-shadow: 0 0 6px rgba(103, 194, 58, 0.5);
}
.dot-down {
  background-color: #f56c6c;
  box-shadow: 0 0 6px rgba(245, 108, 108, 0.5);
}
.dot-unknown {
  background-color: #909399;
}
.health-info {
  flex: 1;
  min-width: 0;
}
.health-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.health-status {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.health-up {
  border-left: 3px solid #67c23a;
}
.health-down {
  border-left: 3px solid #f56c6c;
}
.health-unknown {
  border-left: 3px solid #909399;
}
</style>
