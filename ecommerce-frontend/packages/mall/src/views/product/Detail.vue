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
            <el-button
              size="large"
              :type="isFavorited ? 'warning' : 'default'"
              @click="toggleFavorite"
            >
              <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="detail-desc" v-if="spu.description">
        <h2>商品详情</h2>
        <div class="desc-content">{{ spu.description }}</div>
      </div>

      <div class="review-section">
        <h2>商品评价</h2>
        <div v-if="reviewStats" class="review-stats">
          <div class="review-avg">
            <span class="avg-number">{{ reviewStats.avgRating?.toFixed(1) || '-' }}</span>
            <div class="avg-stars">
              <el-icon v-for="i in 5" :key="i" :size="16"
                :color="i <= Math.round(reviewStats.avgRating || 0) ? '#ff9900' : '#ddd'">
                <StarFilled />
              </el-icon>
            </div>
            <span class="review-count">共 {{ reviewStats.reviewCount }} 条评价</span>
          </div>
          <div class="rating-dist" v-if="reviewStats.ratingDistribution">
            <div v-for="star in [5,4,3,2,1]" :key="star" class="dist-row">
              <span>{{ star }}星</span>
              <div class="dist-bar">
                <div class="dist-fill"
                  :style="{ width: distPercent(star) + '%' }"></div>
              </div>
              <span class="dist-count">{{ reviewStats.ratingDistribution[star] || 0 }}</span>
            </div>
          </div>
        </div>

        <div v-if="reviews.length" class="review-list">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-header">
              <el-avatar :size="32" :src="review.avatar">
                {{ review.nickname?.charAt(0) || '?' }}
              </el-avatar>
              <div class="review-user">
                <span class="review-nickname">{{ review.nickname }}</span>
                <div class="review-stars">
                  <el-icon v-for="i in 5" :key="i" :size="12"
                    :color="i <= review.rating ? '#ff9900' : '#ddd'">
                    <StarFilled />
                  </el-icon>
                </div>
              </div>
              <span class="review-time">{{ review.createTime }}</span>
            </div>
            <div class="review-content" v-if="review.content">{{ review.content }}</div>
          </div>
        </div>
        <el-empty v-else-if="reviewLoaded" description="暂无评价" :image-size="60" />

        <div v-if="reviewTotal > reviews.length" class="review-load-more">
          <el-button text type="primary" @click="loadMoreReviews">加载更多</el-button>
        </div>
      </div>
    </template>

    <el-empty v-if="!loading && !spu" description="商品不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { productApi, cartApi, favoriteApi, reviewApi, formatPrice } from '@ecommerce/shared'
import type { SpuDTO, SkuDTO, ReviewDTO, ReviewStatsDTO } from '@ecommerce/shared'
import { Star, StarFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const spu = ref<SpuDTO | null>(null)
const selectedSku = ref<SkuDTO | null>(null)
const quantity = ref(1)
const isFavorited = ref(false)
const reviews = ref<ReviewDTO[]>([])
const reviewStats = ref<ReviewStatsDTO | null>(null)
const reviewLoaded = ref(false)
const reviewTotal = ref(0)
const reviewPage = ref(1)

const distPercent = (star: number) => {
  if (!reviewStats.value?.reviewCount) return 0
  const count = reviewStats.value.ratingDistribution?.[star] || 0
  return Math.round((count / reviewStats.value.reviewCount) * 100)
}

const loadReviews = async (spuId: number) => {
  try {
    const [listResult, statsResult] = await Promise.all([
      reviewApi.list({ spuId, pageNum: 1, pageSize: 5 }),
      reviewApi.stats(spuId),
    ])
    reviews.value = listResult.list || []
    reviewTotal.value = listResult.total || 0
    reviewPage.value = 1
    reviewStats.value = statsResult
    reviewLoaded.value = true
  } catch {
    reviewLoaded.value = true
  }
}

const loadMoreReviews = async () => {
  const spuId = Number(route.params.id)
  if (!spuId) return
  reviewPage.value++
  try {
    const result = await reviewApi.list({ spuId, pageNum: reviewPage.value, pageSize: 5 })
    reviews.value.push(...(result.list || []))
  } catch {}
}

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

const toggleFavorite = async () => {
  const spuId = Number(route.params.id)
  if (!spuId) return
  try {
    const result = await favoriteApi.toggle(spuId)
    isFavorited.value = result
    ElMessage.success(result ? '已收藏' : '已取消收藏')
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const buyNow = () => {
  if (!selectedSku.value) return
  router.push(`/checkout?skuId=${selectedSku.value.skuId}&quantity=${quantity.value}`)
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
    try {
      isFavorited.value = await favoriteApi.check(spuId)
    } catch {}
    loadReviews(spuId)
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
.review-section {
  border-top: 1px solid #eee;
  padding-top: 20px;
  margin-top: 20px;
}
.review-section h2 {
  font-size: 18px;
  margin-bottom: 16px;
}
.review-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}
.review-avg {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.avg-number {
  font-size: 36px;
  font-weight: bold;
  color: #ff9900;
}
.avg-stars {
  display: flex;
  gap: 2px;
}
.review-count {
  font-size: 12px;
  color: #999;
}
.rating-dist {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 300px;
}
.dist-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666;
}
.dist-bar {
  flex: 1;
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
}
.dist-fill {
  height: 100%;
  background: #ff9900;
  border-radius: 4px;
  transition: width 0.3s;
}
.dist-count {
  min-width: 20px;
  text-align: right;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.review-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.review-item:last-child {
  border-bottom: none;
}
.review-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.review-user {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.review-nickname {
  font-size: 14px;
  font-weight: 500;
}
.review-stars {
  display: flex;
  gap: 1px;
}
.review-time {
  font-size: 12px;
  color: #999;
}
.review-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}
.review-load-more {
  text-align: center;
  padding: 12px 0;
}
</style>
