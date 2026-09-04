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
import { ref, reactive, onMounted } from 'vue'
import { productApi, orderApi, userApi, formatPrice, formatDate } from '@ecommerce/shared'
import type { OrderDTO, UserDTO } from '@ecommerce/shared'

const loading = ref(true)
const recentOrders = ref<OrderDTO[]>([])
const recentUsers = ref<UserDTO[]>([])

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
})
</script>
