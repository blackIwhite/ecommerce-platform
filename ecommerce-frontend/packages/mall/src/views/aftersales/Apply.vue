<template>
  <div class="apply-page">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>申请售后</h2>
    </div>

    <el-card shadow="never">
      <div v-if="orderInfo" class="order-summary">
        <div class="order-no">订单号：{{ orderInfo.orderNo }}</div>
        <div class="order-amount">¥{{ orderInfo.totalAmount?.toFixed(2) }}</div>
      </div>

      <el-form :model="form" label-width="100px" style="margin-top: 20px">
        <el-form-item label="售后类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">退货退款</el-radio>
            <el-radio :value="2">换货</el-radio>
            <el-radio :value="3">仅退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="原因">
          <el-select v-model="form.reason" placeholder="请选择原因" style="width: 100%">
            <el-option label="商品质量问题" value="商品质量问题" />
            <el-option label="与描述不符" value="与描述不符" />
            <el-option label="收到商品破损" value="收到商品破损" />
            <el-option label="不想要了" value="不想要了" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.description" type="textarea" :rows="3"
            placeholder="请详细描述您的问题（选填）" />
        </el-form-item>
        <el-form-item v-if="form.type === 2" label="换货收货地址">
          <el-input v-model="form.exchangeAddress" placeholder="请输入换货后收货地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { aftersalesApi, orderApi } from '@ecommerce/shared'
import type { OrderDTO } from '@ecommerce/shared'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const orderInfo = ref<OrderDTO | null>(null)
const form = ref({
  type: 1,
  reason: '',
  description: '',
  exchangeAddress: '',
})

const orderId = Number(route.query.orderId)

const loadOrder = async () => {
  if (orderId) {
    try {
      orderInfo.value = await orderApi.getDetail(orderId)
    } catch {}
  }
}

const handleSubmit = async () => {
  if (!orderId) {
    ElMessage.warning('缺少订单信息')
    return
  }
  if (!form.value.reason) {
    ElMessage.warning('请选择售后原因')
    return
  }
  if (form.value.type === 2 && !form.value.exchangeAddress.trim()) {
    ElMessage.warning('换货需填写收货地址')
    return
  }
  submitting.value = true
  try {
    const id = await aftersalesApi.apply({
      orderId,
      type: form.value.type,
      reason: form.value.reason,
      description: form.value.description,
      exchangeAddress: form.value.type === 2 ? form.value.exchangeAddress.trim() : undefined,
    })
    ElMessage.success('申请已提交')
    router.push(`/aftersales/${id}`)
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadOrder)
</script>

<style scoped>
.apply-page { max-width: 600px; margin: 0 auto; padding: 20px; }
.page-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.order-summary {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: #f5f7fa; border-radius: 8px;
}
.order-no { font-size: 14px; color: #666; }
.order-amount { font-size: 18px; font-weight: bold; color: #e4393c; }
</style>
