<template>
  <div class="order-list-page" v-loading="loading">
    <h2>我的订单</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" :name="-1" />
      <el-tab-pane label="待付款" :name="0" />
      <el-tab-pane label="待发货" :name="2" />
      <el-tab-pane label="已发货" :name="3" />
      <el-tab-pane label="已完成" :name="4" />
    </el-tabs>

    <div v-if="orders.length" class="order-cards">
      <div v-for="order in orders" :key="order.orderId" class="order-card">
        <div class="order-header">
          <router-link :to="'/order/' + order.orderId" class="order-no">订单号：{{ order.orderNo }}</router-link>
          <span class="order-status" :class="'status-' + order.status">
            {{ statusText(order.status) }}
          </span>
        </div>

        <div class="order-items">
          <div v-for="item in order.items" :key="item.skuId" class="order-item">
            <el-image :src="item.image" fit="cover" class="item-img" />
            <div class="item-info">
              <span class="item-name">{{ item.skuName }}</span>
              <span class="item-spec">x{{ item.quantity }}</span>
            </div>
            <span class="item-price">{{ formatPrice(item.totalPrice) }}</span>
          </div>
        </div>

        <div class="order-footer">
          <span class="order-total">合计：<strong>{{ formatPrice(order.totalAmount) }}</strong></span>
          <div class="order-actions">
            <el-button v-if="order.status === 0" type="primary" size="small" @click="handlePay(order)">
              去付款
            </el-button>
            <el-button v-if="order.status === 0" size="small" @click="handleCancel(order)">
              取消订单
            </el-button>
            <el-button v-if="order.status === 3" type="primary" size="small" @click="handleReceive(order)">
              确认收货
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="暂无订单" />

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadOrders"
      />
    </div>

    <el-dialog v-model="payResultVisible" title="支付成功" width="400px">
      <div class="pay-result" v-if="payResult">
        <div class="pay-result-icon">
          <el-icon :size="48" color="#67c23a"><CircleCheckFilled /></el-icon>
        </div>
        <div class="pay-result-info">
          <div class="pay-result-row">
            <span class="label">支付单号：</span>
            <span>{{ payResult.paymentNo }}</span>
          </div>
          <div class="pay-result-row">
            <span class="label">支付金额：</span>
            <span class="amount">¥{{ payResult.amount?.toFixed(2) }}</span>
          </div>
          <div class="pay-result-row">
            <span class="label">支付方式：</span>
            <el-tag type="info">{{ payResult.payMethod }}</el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="payResultVisible = false">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { orderApi, formatPrice } from '@ecommerce/shared'
import type { OrderDTO, PaymentDTO } from '@ecommerce/shared'

const loading = ref(true)
const orders = ref<OrderDTO[]>([])
const activeTab = ref(-1)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const payResultVisible = ref(false)
const payResult = ref<PaymentDTO | null>(null)

const statusMap: Record<number, string> = {
  0: '待付款', 1: '已付款', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消',
}

const statusText = (status: number) => statusMap[status] || '未知'

const loadOrders = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (activeTab.value >= 0) params.status = activeTab.value
    const res = await orderApi.list(params)
    orders.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pageNum.value = 1
  loadOrders()
}

const handlePay = async (order: OrderDTO) => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '支付')
    await orderApi.pay(order.orderId)
    try {
      const payment = await orderApi.getPayment(order.orderId)
      payResult.value = payment
      payResultVisible.value = true
    } catch {
      ElMessage.success('支付成功')
    }
    loadOrders()
  } catch {}
}

const handleCancel = async (order: OrderDTO) => {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '取消订单')
    await orderApi.cancel(order.orderId)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch {}
}

const handleReceive = async (order: OrderDTO) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货')
    await orderApi.receive(order.orderId)
    ElMessage.success('已确认收货')
    loadOrders()
  } catch {}
}

onMounted(loadOrders)
</script>

<style scoped>
.order-list-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.order-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.order-card {
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  overflow: hidden;
}
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e5e5e5;
}
.order-no {
  color: #666;
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
}
.order-no:hover {
  color: #409eff;
}
.order-status {
  font-weight: 500;
  font-size: 14px;
}
.status-0 { color: #e6a23c; }
.status-2 { color: #409eff; }
.status-3 { color: #409eff; }
.status-4 { color: #67c23a; }
.status-5 { color: #999; }
.order-items {
  padding: 12px 16px;
}
.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}
.order-item + .order-item {
  border-top: 1px solid #f0f0f0;
}
.item-img {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  flex-shrink: 0;
}
.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.item-name {
  font-size: 14px;
}
.item-spec {
  font-size: 12px;
  color: #999;
}
.item-price {
  color: #e4393c;
  font-weight: 500;
}
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #e5e5e5;
}
.order-total strong {
  color: #e4393c;
  font-size: 16px;
}
.order-actions {
  display: flex;
  gap: 8px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.pay-result {
  text-align: center;
  padding: 16px 0;
}
.pay-result-icon {
  margin-bottom: 16px;
}
.pay-result-info {
  text-align: left;
}
.pay-result-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.pay-result-row:last-child {
  border-bottom: none;
}
.pay-result-row .label {
  color: #666;
}
.pay-result-row .amount {
  color: #e4393c;
  font-size: 18px;
  font-weight: bold;
}
</style>
