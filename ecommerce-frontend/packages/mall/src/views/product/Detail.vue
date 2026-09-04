<template>
  <div class="product-detail" v-loading="loading">
    <template v-if="spu">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/product' }">商品</el-breadcrumb-item>
        <el-breadcrumb-item>{{ spu.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-main">
        <div class="detail-gallery">
          <el-carousel height="400px" v-if="imageList.length">
            <el-carousel-item v-for="(img, i) in imageList" :key="i">
              <el-image :src="img" fit="contain" style="width: 100%; height: 100%;" />
            </el-carousel-item>
          </el-carousel>
          <el-empty v-else description="暂无图片" />
        </div>

        <div class="detail-info">
          <h1>{{ spu.name }}</h1>
          <div class="price-range">
            <span class="price">{{ minPrice }} - {{ maxPrice }}</span>
          </div>
          <div class="meta">
            <span>品牌：{{ spu.brandName }}</span>
            <span>分类：{{ spu.categoryName }}</span>
          </div>

          <div class="sku-select" v-if="skuList.length">
            <h3>规格</h3>
            <div class="sku-options">
              <el-tag
                v-for="sku in skuList"
                :key="sku.skuId"
                :type="selectedSku?.skuId === sku.skuId ? '' : 'info'"
                class="sku-tag"
                @click="selectedSku = sku"
              >
                {{ sku.skuName }}
              </el-tag>
            </div>
          </div>

          <div class="quantity">
            <span>数量：</span>
            <el-input-number v-model="quantity" :min="1" :max="selectedSku?.stock || 99" />
            <span class="stock" v-if="selectedSku">库存 {{ selectedSku.stock }}</span>
          </div>

          <div class="actions">
            <el-button type="primary" size="large" :disabled="!selectedSku" @click="addToCart">
              加入购物车
            </el-button>
            <el-button type="danger" size="large" :disabled="!selectedSku" @click="buyNow">
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <div class="detail-desc" v-if="spu.description">
        <h2>商品详情</h2>
        <div class="desc-content">{{ spu.description }}</div>
      </div>
    </template>

    <el-empty v-if="!loading && !spu" description="商品不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi, cartApi, formatPrice } from '@ecommerce/shared'
import type { SpuDTO, SkuDTO } from '@ecommerce/shared'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const spu = ref<SpuDTO | null>(null)
const selectedSku = ref<SkuDTO | null>(null)
const quantity = ref(1)

const skuList = computed(() => spu.value?.skuList || [])
const imageList = computed(() =>
  spu.value?.images ? spu.value.images.split(',').filter(Boolean) : []
)

const minPrice = computed(() => {
  const list = skuList.value
  if (!list.length) return '¥0.00'
  return formatPrice(Math.min(...list.map((s) => s.price)))
})

const maxPrice = computed(() => {
  const list = skuList.value
  if (!list.length) return '¥0.00'
  return formatPrice(Math.max(...list.map((s) => s.price)))
})

const addToCart = async () => {
  if (!selectedSku.value) return
  try {
    await cartApi.add({ skuId: selectedSku.value.skuId, quantity: quantity.value })
    ElMessage.success(`已加入购物车：${selectedSku.value.skuName} x${quantity.value}`)
  } catch (e: any) {
    ElMessage.error(e.message || '加入购物车失败')
  }
}

const buyNow = () => {
  if (!selectedSku.value) return
  router.push('/order')
}

onMounted(async () => {
  const spuId = Number(route.params.id)
  if (!spuId) {
    loading.value = false
    return
  }
  try {
    const data = await productApi.getSpuDetail(spuId)
    spu.value = data
    if (data.skuList?.length) {
      selectedSku.value = data.skuList[0]
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载商品失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.product-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.el-breadcrumb {
  margin-bottom: 20px;
}
.detail-main {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}
.detail-gallery {
  width: 400px;
  flex-shrink: 0;
}
.detail-info {
  flex: 1;
}
.detail-info h1 {
  font-size: 24px;
  margin-bottom: 16px;
}
.price-range {
  margin-bottom: 16px;
}
.price {
  font-size: 28px;
  color: #e4393c;
  font-weight: bold;
}
.meta {
  display: flex;
  gap: 20px;
  color: #999;
  margin-bottom: 20px;
}
.sku-select h3 {
  font-size: 14px;
  margin-bottom: 8px;
}
.sku-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}
.sku-tag {
  cursor: pointer;
}
.quantity {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.stock {
  color: #999;
  font-size: 13px;
}
.actions {
  display: flex;
  gap: 12px;
}
.detail-desc {
  border-top: 1px solid #eee;
  padding-top: 20px;
}
.detail-desc h2 {
  font-size: 18px;
  margin-bottom: 12px;
}
.desc-content {
  color: #666;
  line-height: 1.8;
}
</style>
