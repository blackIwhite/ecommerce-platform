<template>
  <div class="cart-page" v-loading="loading">
    <h2>购物车</h2>

    <template v-if="cartItems.length">
      <el-table :data="cartItems" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image :src="row.image" fit="cover" class="product-img" />
              <div class="product-info">
                <span class="product-name">{{ row.skuName }}</span>
                <span class="product-specs" v-if="row.specs">{{ row.specs }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120" align="center">
          <template #default="{ row }">
            <span class="price">{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="160" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="row.stock"
              size="small"
              @change="(val: number) => onQuantityChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120" align="center">
          <template #default="{ row }">
            <span class="subtotal">{{ formatPrice(row.price * row.quantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleRemove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <div class="footer-left">
          <el-button type="danger" link @click="handleClear">清空购物车</el-button>
        </div>
        <div class="footer-right">
          <span class="total-label">
            已选 <strong>{{ selectedItems.length }}</strong> 件商品，合计：
          </span>
          <span class="total-price">{{ formatPrice(totalAmount) }}</span>
          <el-button type="danger" size="large" :disabled="!selectedItems.length" @click="handleCheckout">
            去结算
          </el-button>
        </div>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="购物车为空">
      <el-button type="primary" @click="$router.push('/product')">去逛逛</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cartApi, formatPrice } from '@ecommerce/shared'
import type { CartItemDTO } from '@ecommerce/shared'

const router = useRouter()
const loading = ref(true)
const cartItems = ref<CartItemDTO[]>([])
const selectedItems = ref<CartItemDTO[]>([])

const totalAmount = computed(() =>
  selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
)

const handleSelectionChange = (items: CartItemDTO[]) => {
  selectedItems.value = items
}

const onQuantityChange = async (item: CartItemDTO, val: number) => {
  try {
    await cartApi.updateQuantity(item.id, val)
  } catch {
    ElMessage.error('更新数量失败')
  }
}

const handleRemove = async (cartItemId: number) => {
  try {
    await cartApi.remove(cartItemId)
    cartItems.value = cartItems.value.filter((i) => i.id !== cartItemId)
    ElMessage.success('已删除')
  } catch {
    ElMessage.error('删除失败')
  }
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定清空购物车？', '提示')
    await cartApi.clear()
    cartItems.value = []
    ElMessage.success('已清空')
  } catch {}
}

const handleCheckout = () => {
  router.push('/order')
}

const loadCart = async () => {
  loading.value = true
  try {
    cartItems.value = await cartApi.list()
  } catch {
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadCart)
</script>

<style scoped>
.cart-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.product-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  flex-shrink: 0;
}
.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.product-name {
  font-weight: 500;
}
.product-specs {
  font-size: 12px;
  color: #999;
}
.price, .subtotal {
  color: #e4393c;
  font-weight: 500;
}
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px 20px;
  background: #f5f5f5;
  border-radius: 8px;
}
.footer-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.total-label {
  color: #666;
}
.total-price {
  font-size: 24px;
  color: #e4393c;
  font-weight: bold;
}
</style>
