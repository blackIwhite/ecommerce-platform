<template>
  <div class="browse-history">
    <div class="page-header">
      <h2>浏览历史</h2>
      <el-popconfirm title="确定清空所有浏览记录？" @confirm="clearHistory">
        <template #reference>
          <el-button type="danger" plain :disabled="items.length === 0">清空记录</el-button>
        </template>
      </el-popconfirm>
    </div>

    <div v-loading="loading">
      <el-empty v-if="items.length === 0" description="暂无浏览记录" />
      <div class="history-grid" v-else>
        <div v-for="item in items" :key="item.id" class="history-card" @click="goProduct(item.spuId)">
          <img :src="item.productImage || '/placeholder.png'" class="product-img" />
          <div class="product-info">
            <div class="product-name">{{ item.productName || `商品 #${item.spuId}` }}</div>
            <div class="product-price" v-if="item.price">¥{{ item.price.toFixed(2) }}</div>
            <div class="browse-time">{{ item.browseTime?.substring(0, 16) }}</div>
            <div class="browse-duration" v-if="item.duration > 0">浏览 {{ item.duration }} 秒</div>
          </div>
        </div>
      </div>
    </div>

    <el-pagination v-if="total > 20" class="pagination" v-model:current-page="query.pageNum"
      :page-size="query.pageSize" :total="total" layout="prev, pager, next" @change="loadData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { behaviorApi } from '@ecommerce/shared'
import type { BrowseHistoryDTO } from '@ecommerce/shared'

const router = useRouter()
const loading = ref(false)
const items = ref<BrowseHistoryDTO[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 20 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await behaviorApi.getBrowseHistory(query)
    items.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const clearHistory = async () => {
  await behaviorApi.clearBrowseHistory()
  ElMessage.success('已清空')
  loadData()
}

const goProduct = (spuId: number) => {
  router.push(`/product/${spuId}`)
}

onMounted(loadData)
</script>

<style scoped>
.browse-history { padding: 20px; max-width: 1000px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.history-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; }
.history-card { border: 1px solid #eee; border-radius: 8px; overflow: hidden; cursor: pointer; transition: box-shadow 0.2s; }
.history-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.product-img { width: 100%; height: 180px; object-fit: cover; background: #f5f5f5; }
.product-info { padding: 10px; }
.product-name { font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price { color: #f56c6c; font-weight: bold; margin-top: 4px; }
.browse-time { color: #999; font-size: 12px; margin-top: 4px; }
.browse-duration { color: #bbb; font-size: 11px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
