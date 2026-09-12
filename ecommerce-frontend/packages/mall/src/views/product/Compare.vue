<template>
  <div class="compare-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>商品对比</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">商品对比</h2>

    <div v-if="products.length === 0" class="empty-state">
      <el-empty description="暂无对比商品，请从商品列表添加">
        <el-button type="primary" @click="$router.push('/product')">去选购</el-button>
      </el-empty>
    </div>

    <div v-else class="compare-wrapper">
      <el-table :data="compareRows" border style="width: 100%">
        <el-table-column prop="label" label="对比项" width="120" fixed
          style="font-weight: 600; background: #fafafa" />
        <el-table-column v-for="p in products" :key="p.spuId" :label="p.name" min-width="200">
          <template #default="{ row }">
            <template v-if="row.key === 'image'">
              <el-image :src="firstImage(p)" fit="cover"
                style="width: 100px; height: 100px" :preview-src-list="[firstImage(p)]" />
            </template>
            <template v-else-if="row.key === 'price'">
              <span class="price">¥{{ p.minPrice?.toFixed(2) }}</span>
            </template>
            <template v-else-if="row.key === 'status'">
              <el-tag :type="p.status === 1 ? 'success' : 'info'" size="small">
                {{ p.status === 1 ? '在售' : '下架' }}
              </el-tag>
            </template>
            <template v-else-if="row.key === 'rating'">
              <el-rate :model-value="p.avgRating || 0" disabled show-score text-color="#ff9900" />
              <span style="margin-left: 4px; color: #999">({{ p.reviewCount || 0 }})</span>
            </template>
            <template v-else-if="row.key === 'action'">
              <el-button type="primary" size="small" @click="$router.push(`/product/${p.spuId}`)">
                查看详情
              </el-button>
              <el-button type="danger" size="small" text @click="removeProduct(p.spuId)">移除</el-button>
            </template>
            <template v-else>
              {{ row.values[p.spuId] ?? '-' }}
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="compare-actions">
        <el-button @click="clearAll">清空对比</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { productApi } from '@ecommerce/shared'
import type { SpuDTO } from '@ecommerce/shared'

const COMPARE_KEY = 'compare_spu_ids'

const products = ref<SpuDTO[]>([])

const loadProducts = async () => {
  const raw = localStorage.getItem(COMPARE_KEY)
  if (!raw) return
  const ids: number[] = JSON.parse(raw)
  if (!ids.length) return
  try {
    const results = await Promise.all(ids.map((id) => productApi.getSpuDetail(id)))
    products.value = results
  } catch {
    ElMessage.error('加载对比商品失败')
  }
}

const firstImage = (p: SpuDTO) => {
  if (!p.images) return ''
  return p.images.split(',')[0]
}

const compareRows = computed(() => {
  const valMap: Record<string, (p: SpuDTO) => any> = {
    brand: (p) => p.brandName,
    category: (p) => p.categoryName,
    salesCount: (p) => `${p.salesCount || 0} 件`,
    viewCount: (p) => `${p.viewCount || 0} 次`,
    description: (p) => p.description,
  }

  const rows: any[] = [
    { key: 'image', label: '商品图片', values: {} },
    { key: 'price', label: '价格', values: {} },
    { key: 'status', label: '状态', values: {} },
    { key: 'rating', label: '评分', values: {} },
  ]

  for (const [key, getter] of Object.entries(valMap)) {
    const values: Record<number, any> = {}
    for (const p of products.value) {
      values[p.spuId] = getter(p)
    }
    rows.push({ key, label: labelMap[key], values })
  }

  rows.push({ key: 'action', label: '操作', values: {} })
  return rows
})

const labelMap: Record<string, string> = {
  brand: '品牌',
  category: '分类',
  salesCount: '销量',
  viewCount: '浏览量',
  description: '描述',
}

const removeProduct = (spuId: number) => {
  const raw = localStorage.getItem(COMPARE_KEY)
  if (!raw) return
  const ids: number[] = JSON.parse(raw).filter((id: number) => id !== spuId)
  localStorage.setItem(COMPARE_KEY, JSON.stringify(ids))
  products.value = products.value.filter((p) => p.spuId !== spuId)
}

const clearAll = () => {
  localStorage.removeItem(COMPARE_KEY)
  products.value = []
}

onMounted(loadProducts)
</script>

<style scoped>
.compare-page { padding: 20px; max-width: 1200px; margin: 0 auto; }
.empty-state { padding: 60px 0; }
.price { color: #e4393c; font-size: 16px; font-weight: 600; }
.compare-actions { margin-top: 16px; text-align: right; }
</style>
