<template>
  <div v-loading="loading">
    <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px;">
      <el-button @click="$router.back()">返回</el-button>
      <h2>订单详情</h2>
    </div>

    <template v-if="order">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(order.status)">{{ statusText(order.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ order.userId }}</el-descriptions-item>
        <el-descriptions-item label="金额">
          <span style="color: #e4393c; font-weight: bold;">{{ formatPrice(order.totalAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="收货电话">{{ order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ order.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ order.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(order.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <h3 style="margin-top: 20px;">商品明细</h3>
      <el-table :data="order.items || []" border>
        <el-table-column label="商品" min-width="250">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-image :src="row.image" fit="cover" style="width: 40px; height: 40px; border-radius: 4px;" />
              <span>{{ row.skuName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">{{ formatPrice(row.totalPrice) }}</template>
        </el-table-column>
      </el-table>
    </template>

    <el-empty v-if="!loading && !order" description="订单不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi, formatPrice, formatDate } from '@ecommerce/shared'
import type { OrderDTO } from '@ecommerce/shared'

const route = useRoute()
const loading = ref(true)
const order = ref<OrderDTO | null>(null)

const statusMap: Record<number, string> = {
  0: '待付款', 1: '已付款', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消',
}

const statusText = (status: number) => statusMap[status] || '未知'

const statusTagType = (status: number): string => {
  const map: Record<number, string> = { 0: 'warning', 2: '', 3: '', 4: 'success', 5: 'info' }
  return map[status] || 'info'
}

onMounted(async () => {
  const orderId = Number(route.params.id)
  if (!orderId) {
    loading.value = false
    return
  }
  try {
    order.value = await orderApi.adminDetail(orderId)
  } catch {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
})
</script>
