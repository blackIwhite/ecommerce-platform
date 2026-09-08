<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>订单详情</h2>
    </div>

    <template v-if="order">
      <div class="section-card">
        <h3>收货信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">收货人</span>
            <span>{{ order.receiverName }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系电话</span>
            <span>{{ order.receiverPhone }}</span>
          </div>
          <div class="info-item full-width">
            <span class="label">收货地址</span>
            <span>{{ order.receiverAddress }}</span>
          </div>
        </div>
      </div>

      <div class="section-card">
        <h3>订单信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">订单编号</span>
            <span>{{ order.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="label">下单时间</span>
            <span>{{ order.createTime }}</span>
          </div>
          <div class="info-item">
            <span class="label">订单状态</span>
            <el-tag :type="statusTagType(order.status)">{{ statusText(order.status) }}</el-tag>
          </div>
          <div class="info-item">
            <span class="label">订单金额</span>
            <span class="amount">¥{{ order.totalAmount?.toFixed(2) }}</span>
          </div>
          <div v-if="order.remark" class="info-item full-width">
            <span class="label">备注</span>
            <span>{{ order.remark }}</span>
          </div>
        </div>
      </div>

      <div class="section-card">
        <h3>商品信息</h3>
        <div class="order-items">
          <div v-for="item in order.items" :key="item.skuId" class="order-item">
            <el-image :src="item.image" fit="cover" class="item-img">
              <template #error>
                <div class="item-img-placeholder">
                  <el-icon :size="20"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="item-info">
              <span class="item-name">{{ item.skuName }}</span>
            </div>
            <span class="item-qty">x{{ item.quantity }}</span>
            <span class="item-price">¥{{ item.totalPrice?.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <div v-if="order.statusLogs && order.statusLogs.length" class="section-card">
        <h3>订单进度</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(log, idx) in [...order.statusLogs].reverse()"
            :key="idx"
            :timestamp="log.createTime"
            placement="top"
            :type="idx === 0 ? 'primary' : ''"
          >
            <div class="log-content">
              <span class="log-status">{{ statusText(log.toStatus) }}</span>
              <span class="log-remark" v-if="log.remark">{{ log.remark }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div v-if="logistics && logistics.traces && logistics.traces.length" class="section-card">
        <h3>物流信息</h3>
        <div class="logistics-header">
          <span>{{ logistics.logisticsCompany }}</span>
          <span class="tracking-no">{{ logistics.trackingNo }}</span>
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="(trace, idx) in [...logistics.traces].reverse()"
            :key="idx"
            :timestamp="trace.time"
            placement="top"
            :type="idx === 0 ? 'primary' : ''"
          >
            {{ trace.description }}
          </el-timeline-item>
        </el-timeline>
      </div>

      <div class="section-card actions-card">
        <el-button v-if="order.status === 0" type="primary" @click="handlePay">去付款</el-button>
        <el-button v-if="order.status === 0" @click="handleCancel">取消订单</el-button>
        <el-button v-if="order.status === 3" type="primary" @click="handleReceive">确认收货</el-button>
        <span v-if="order.status === 4" class="completed-hint">交易完成</span>
        <el-button v-if="order.status === 4" type="warning" @click="openReviewDialog">评价</el-button>
        <span v-if="order.status === 5" class="cancelled-hint">订单已取消</span>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="订单不存在" />

    <el-dialog v-model="reviewDialogVisible" title="商品评价" width="500px">
      <div v-if="order" class="review-form">
        <div v-for="(item, idx) in order.items" :key="item.skuId" class="review-form-item">
          <div class="review-form-product">
            <el-image :src="item.image" fit="cover" class="review-item-img">
              <template #error>
                <div class="item-img-placeholder">
                  <el-icon :size="16"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <span>{{ item.skuName }}</span>
          </div>
          <div class="review-form-rating">
            <span>评分：</span>
            <el-icon v-for="i in 5" :key="i" :size="20" class="rating-star"
              :color="i <= reviewForms[idx].rating ? '#ff9900' : '#ddd'"
              @click="reviewForms[idx].rating = i">
              <StarFilled />
            </el-icon>
          </div>
          <el-input v-model="reviewForms[idx].content" type="textarea" :rows="2"
            placeholder="分享您的使用体验..." maxlength="500" show-word-limit />
        </div>
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewSubmitting" @click="submitReviews">提交评价</el-button>
      </template>
    </el-dialog>

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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, CircleCheckFilled, Picture, StarFilled } from '@element-plus/icons-vue'
import { orderApi, reviewApi } from '@ecommerce/shared'
import type { OrderDTO, LogisticsTraceDTO, PaymentDTO } from '@ecommerce/shared'

const route = useRoute()
const loading = ref(true)
const order = ref<OrderDTO | null>(null)
const logistics = ref<LogisticsTraceDTO | null>(null)
const payResultVisible = ref(false)
const payResult = ref<PaymentDTO | null>(null)
const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewForms = ref<{ skuId: number; spuId: number; rating: number; content: string }[]>([])

const openReviewDialog = () => {
  if (!order.value?.items) return
  reviewForms.value = order.value.items.map((item) => ({
    skuId: item.skuId,
    spuId: item.spuId || 0,
    rating: 5,
    content: '',
  }))
  reviewDialogVisible.value = true
}

const submitReviews = async () => {
  if (!order.value) return
  reviewSubmitting.value = true
  try {
    for (const form of reviewForms.value) {
      await reviewApi.create({
        spuId: form.spuId,
        skuId: form.skuId,
        orderId: order.value.orderId,
        rating: form.rating,
        content: form.content || undefined,
      })
    }
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
  } catch (e: any) {
    ElMessage.error(e.message || '评价失败')
  } finally {
    reviewSubmitting.value = false
  }
}

const statusMap: Record<number, string> = {
  0: '待付款', 1: '已付款', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消',
}
const statusText = (s: number) => statusMap[s] || '未知'
const statusTagType = (s: number): '' | 'warning' | 'success' | 'info' | 'danger' => {
  const map: Record<number, '' | 'warning' | 'success' | 'info' | 'danger'> = {
    0: 'warning', 2: '', 3: '', 4: 'success', 5: 'info',
  }
  return map[s] ?? ''
}

const loadOrder = async () => {
  loading.value = true
  try {
    const orderId = Number(route.params.id)
    order.value = await orderApi.getDetail(orderId)
    if (order.value.status >= 3) {
      try {
        logistics.value = await orderApi.getLogistics(orderId)
      } catch {}
    }
  } catch {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '支付')
    await orderApi.pay(order.value!.orderId)
    try {
      const payment = await orderApi.getPayment(order.value!.orderId)
      payResult.value = payment
      payResultVisible.value = true
    } catch {
      ElMessage.success('支付成功')
    }
    loadOrder()
  } catch {}
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '取消订单')
    await orderApi.cancel(order.value!.orderId)
    ElMessage.success('订单已取消')
    loadOrder()
  } catch {}
}

const handleReceive = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货')
    await orderApi.receive(order.value!.orderId)
    ElMessage.success('已确认收货')
    loadOrder()
  } catch {}
}

onMounted(loadOrder)
</script>

<style scoped>
.order-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
}
.section-card {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}
.section-card h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #333;
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-item.full-width {
  grid-column: 1 / -1;
}
.info-item .label {
  font-size: 12px;
  color: #999;
}
.info-item .amount {
  color: #e4393c;
  font-size: 18px;
  font-weight: bold;
}
.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.order-item:last-child {
  border-bottom: none;
}
.item-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  flex-shrink: 0;
}
.item-img-placeholder {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 4px;
  color: #ccc;
}
.item-info {
  flex: 1;
}
.item-name {
  font-size: 14px;
}
.item-qty {
  color: #999;
  font-size: 13px;
}
.item-price {
  color: #e4393c;
  font-weight: 500;
  min-width: 80px;
  text-align: right;
}
.log-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.log-status {
  font-weight: 500;
}
.log-remark {
  font-size: 13px;
  color: #666;
}
.logistics-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 14px;
}
.tracking-no {
  color: #666;
  font-family: monospace;
}
.actions-card {
  display: flex;
  align-items: center;
  gap: 12px;
}
.completed-hint {
  color: #67c23a;
  font-weight: 500;
}
.cancelled-hint {
  color: #999;
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
.review-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.review-form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}
.review-form-product {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.review-item-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  flex-shrink: 0;
}
.review-form-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}
.rating-star {
  cursor: pointer;
}
</style>
