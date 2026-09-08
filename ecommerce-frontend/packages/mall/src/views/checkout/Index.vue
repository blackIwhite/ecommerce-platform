<template>
  <div class="checkout-page" v-loading="pageLoading">
    <h2>确认订单</h2>

    <div class="section" v-if="addresses.length > 0">
      <h3>收货地址</h3>
      <el-radio-group v-model="selectedAddressId" class="address-list">
        <el-radio
          v-for="addr in addresses"
          :key="addr.addressId"
          :value="addr.addressId"
          class="address-radio"
        >
          <div class="address-card">
            <div class="address-main">
              <strong>{{ addr.receiverName }}</strong>
              <span>{{ addr.receiverPhone }}</span>
              <el-tag v-if="addr.isDefault === 1" size="small" type="danger">默认</el-tag>
            </div>
            <div class="address-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}</div>
          </div>
        </el-radio>
      </el-radio-group>
      <el-button link type="primary" @click="showAddressDialog = true" style="margin-top: 8px">
        + 新增地址
      </el-button>
    </div>

    <div class="section" v-else>
      <h3>收货地址</h3>
      <el-empty description="暂无收货地址，请先添加" :image-size="60">
        <el-button type="primary" @click="showAddressDialog = true">添加地址</el-button>
      </el-empty>
    </div>

    <div class="section">
      <h3>商品清单</h3>
      <el-table :data="confirmData.items" v-if="confirmData.items.length">
        <el-table-column label="商品" min-width="250">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-image :src="row.image" fit="cover" style="width: 50px; height: 50px; border-radius: 4px; flex-shrink: 0" />
              <span>{{ row.skuName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.price?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="80" align="center" prop="quantity" />
        <el-table-column label="小计" width="120" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalPrice?.toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="section" v-if="myCoupons.length > 0">
      <h3>优惠券</h3>
      <el-select v-model="selectedCouponId" placeholder="不使用优惠券" clearable @change="handleCouponChange" style="width: 360px">
        <el-option
          v-for="c in usableCoupons"
          :key="c.id"
          :value="c.id"
          :label="`${c.couponName} - ${c.type === 1 ? '¥' + c.discountValue : c.discountValue + '%'}优惠，满¥${c.minPurchase}可用`"
        />
      </el-select>
    </div>

    <div class="section">
      <h3>订单备注</h3>
      <el-input v-model="remark" type="textarea" :rows="2" placeholder="选填" maxlength="200" show-word-limit />
    </div>

    <div class="footer">
      <div class="footer-total">
        <div>商品合计：<span>¥{{ confirmData.totalAmount?.toFixed(2) }}</span></div>
        <div v-if="confirmData.discountAmount > 0" class="discount-line">
          优惠券减免：<span class="discount">-¥{{ confirmData.discountAmount?.toFixed(2) }}</span>
        </div>
        <div class="payable-line">
          应付：<span class="total-price">¥{{ (confirmData.payableAmount || confirmData.totalAmount)?.toFixed(2) }}</span>
        </div>
      </div>
      <el-button type="danger" size="large" :disabled="!canSubmit" :loading="submitting" @click="handleSubmit">
        提交订单
      </el-button>
    </div>

    <el-dialog v-model="showAddressDialog" title="新增收货地址" width="500px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人" required>
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="省份" required>
          <el-input v-model="addressForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" required>
          <el-input v-model="addressForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区/县" required>
          <el-input v-model="addressForm.district" placeholder="请输入区/县" />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="addressForm.detailAddress" type="textarea" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingAddress" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi, userApi, couponApi } from '@ecommerce/shared'
import type { UserAddressDTO, UserCouponDTO } from '@ecommerce/shared'

const route = useRoute()
const router = useRouter()
const pageLoading = ref(true)
const submitting = ref(false)
const savingAddress = ref(false)
const showAddressDialog = ref(false)
const remark = ref('')
const selectedAddressId = ref<number | null>(null)
const addresses = ref<UserAddressDTO[]>([])

const confirmData = reactive({
  totalAmount: 0,
  discountAmount: 0,
  payableAmount: 0,
  items: [] as { skuId: number; skuName: string; price: number; quantity: number; totalPrice: number; image: string }[],
})

const orderItems = ref<{ skuId: number; quantity: number }[]>([])

const myCoupons = ref<UserCouponDTO[]>([])
const selectedCouponId = ref<number | undefined>(undefined)

const usableCoupons = computed(() => {
  const amount = confirmData.totalAmount || 0
  return myCoupons.value.filter((c) => c.status === 0 && amount >= c.minPurchase)
})
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0,
})

const canSubmit = computed(() => selectedAddressId.value && confirmData.items.length > 0)

function parseItemsFromQuery(): { skuId: number; quantity: number }[] {
  const skuId = route.query.skuId
  const quantity = route.query.quantity
  if (skuId) {
    return [{ skuId: Number(skuId), quantity: Number(quantity) || 1 }]
  }
  const itemsStr = route.query.items as string
  if (itemsStr) {
    return itemsStr.split(',').map((pair) => {
      const [sid, qty] = pair.split(':')
      return { skuId: Number(sid), quantity: Number(qty) || 1 }
    })
  }
  return []
}

async function loadAddresses() {
  try {
    addresses.value = await userApi.getAddressList()
    const defaultAddr = addresses.value.find((a) => a.isDefault === 1)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.addressId
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].addressId
    }
  } catch {
    addresses.value = []
  }
}

async function confirmOrder() {
  try {
    const res = await orderApi.confirm({
      addressId: selectedAddressId.value || 0,
      items: orderItems.value,
      remark: remark.value,
      userCouponId: selectedCouponId.value,
    })
    confirmData.totalAmount = res.totalAmount
    confirmData.discountAmount = res.discountAmount || 0
    confirmData.payableAmount = res.payableAmount || res.totalAmount
    confirmData.items = res.items
  } catch (e: any) {
    ElMessage.error(e.message || '确认订单失败')
  }
}

async function handleCouponChange() {
  await confirmOrder()
}

async function handleSaveAddress() {
  if (!addressForm.receiverName || !addressForm.receiverPhone || !addressForm.detailAddress) {
    ElMessage.warning('请填写完整地址信息')
    return
  }
  savingAddress.value = true
  try {
    const addr = await userApi.addAddress({
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province: addressForm.province,
      city: addressForm.city,
      district: addressForm.district,
      detailAddress: addressForm.detailAddress,
      isDefault: addressForm.isDefault,
    })
    addresses.value.push(addr)
    selectedAddressId.value = addr.addressId
    showAddressDialog.value = false
    ElMessage.success('地址已添加')
    Object.assign(addressForm, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 })
  } catch (e: any) {
    ElMessage.error(e.message || '保存地址失败')
  } finally {
    savingAddress.value = false
  }
}

async function handleSubmit() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  submitting.value = true
  try {
    await orderApi.submit({
      addressId: selectedAddressId.value,
      items: orderItems.value,
      remark: remark.value,
      userCouponId: selectedCouponId.value,
    })
    ElMessage.success('订单提交成功')
    router.push('/order')
  } catch (e: any) {
    ElMessage.error(e.message || '提交订单失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  orderItems.value = parseItemsFromQuery()
  if (!orderItems.value.length) {
    pageLoading.value = false
    ElMessage.warning('请先选择商品')
    router.push('/product')
    return
  }
  await loadAddresses()
  try {
    myCoupons.value = await couponApi.getMyCoupons(0) as any
  } catch {
    myCoupons.value = []
  }
  await confirmOrder()
  pageLoading.value = false
})
</script>

<style scoped>
.checkout-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.section {
  margin-bottom: 24px;
}
.section h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: #333;
}
.address-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.address-radio {
  width: 100%;
}
.address-card {
  padding: 8px 0;
}
.address-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}
.address-detail {
  color: #666;
  font-size: 13px;
}
.price {
  color: #e4393c;
  font-weight: 500;
}
.footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}
.footer-total {
  font-size: 14px;
  color: #333;
}
.discount-line {
  margin-top: 4px;
  font-size: 13px;
}
.discount {
  color: #52c41a;
}
.payable-line {
  margin-top: 8px;
}
.total-price {
  font-size: 24px;
  color: #e4393c;
  font-weight: bold;
}
</style>
