<template>
  <div class="coupons-page">
    <div class="page-header">
      <el-button text @click="$router.push('/user')">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>我的优惠券</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="loadCoupons">
      <el-tab-pane label="未使用" :name="0" />
      <el-tab-pane label="已使用" :name="1" />
      <el-tab-pane label="已过期" :name="2" />
      <el-tab-pane label="领取优惠券" name="claim" />
    </el-tabs>

    <div v-if="activeTab !== 'claim'">
      <div v-if="loading" v-loading="true" style="height: 200px" />
      <el-empty v-else-if="!coupons.length" description="暂无优惠券" />
      <div v-else class="coupon-grid">
        <div v-for="c in coupons" :key="c.id" class="coupon-card"
          :class="{ disabled: c.status !== 0 }">
          <div class="coupon-value">
            <template v-if="c.type === 1">
              <span class="symbol">¥</span>{{ c.discountValue }}
            </template>
            <template v-else>
              {{ c.discountValue }}<span class="symbol">%</span>
            </template>
          </div>
          <div class="coupon-info">
            <div class="coupon-name">{{ c.couponName }}</div>
            <div class="coupon-condition">满¥{{ c.minPurchase }}可用</div>
            <div class="coupon-expire">
              {{ c.status === 0 ? `有效期至 ${c.expireTime?.substring(0, 10)}` : '' }}
              {{ c.status === 1 ? '已使用' : '' }}
              {{ c.status === 2 ? '已过期' : '' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else>
      <div v-if="loadingAvailable" v-loading="true" style="height: 200px" />
      <el-empty v-else-if="!availableCoupons.length" description="暂无可领优惠券" />
      <div v-else class="coupon-grid">
        <div v-for="t in availableCoupons" :key="t.id" class="coupon-card claimable">
          <div class="coupon-value">
            <template v-if="t.type === 1">
              <span class="symbol">¥</span>{{ t.discountValue }}
            </template>
            <template v-else>
              {{ t.discountValue }}<span class="symbol">%</span>
            </template>
          </div>
          <div class="coupon-info">
            <div class="coupon-name">{{ t.name }}</div>
            <div class="coupon-condition">满¥{{ t.minPurchase }}可用</div>
            <div class="coupon-expire">{{ t.startTime?.substring(0, 10) }} ~ {{ t.endTime?.substring(0, 10) }}</div>
            <el-button type="primary" size="small" @click="handleClaim(t.id!)" style="margin-top: 8px">
              立即领取
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { couponApi } from '@ecommerce/shared'
import type { UserCouponDTO, CouponTemplateDTO } from '@ecommerce/shared'

const activeTab = ref<number | string>(0)
const loading = ref(false)
const loadingAvailable = ref(false)
const coupons = ref<UserCouponDTO[]>([])
const availableCoupons = ref<CouponTemplateDTO[]>([])

const loadCoupons = async () => {
  if (activeTab.value === 'claim') {
    loadAvailable()
    return
  }
  loading.value = true
  try {
    const res = await couponApi.getMyCoupons(activeTab.value as number)
    coupons.value = res
  } finally {
    loading.value = false
  }
}

const loadAvailable = async () => {
  loadingAvailable.value = true
  try {
    const res = await couponApi.listAvailable()
    availableCoupons.value = res
  } finally {
    loadingAvailable.value = false
  }
}

const handleClaim = async (templateId: number) => {
  try {
    await couponApi.claim(templateId)
    ElMessage.success('领取成功')
    loadAvailable()
  } catch (e: any) {
    ElMessage.error(e.message || '领取失败')
  }
}

onMounted(loadCoupons)
</script>

<style scoped>
.coupons-page { max-width: 800px; margin: 0 auto; padding: 20px; }
.page-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.coupon-grid { display: flex; flex-direction: column; gap: 12px; }
.coupon-card {
  display: flex; border: 1px solid #eee; border-radius: 8px; overflow: hidden;
  transition: box-shadow 0.2s;
}
.coupon-card:hover:not(.disabled) { box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.coupon-card.disabled { opacity: 0.5; }
.coupon-card.claimable { border-color: #ff6600; }
.coupon-value {
  width: 120px; display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: bold; color: #ff4400;
  background: linear-gradient(135deg, #fff5f0, #ffe8d6);
  flex-shrink: 0;
}
.coupon-value .symbol { font-size: 16px; margin: 0 2px; }
.coupon-info { padding: 12px 16px; flex: 1; }
.coupon-name { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.coupon-condition { font-size: 13px; color: #666; }
.coupon-expire { font-size: 12px; color: #999; margin-top: 4px; }
</style>
