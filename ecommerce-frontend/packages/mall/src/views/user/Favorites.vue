<template>
  <div class="favorites-page" v-loading="loading">
    <h2>我的收藏</h2>

    <div class="favorite-grid" v-if="list.length">
      <div class="favorite-card" v-for="item in list" :key="item.spuId" @click="goDetail(item.spuId)">
        <el-image :src="item.image" fit="cover" class="favorite-img" />
        <div class="favorite-info">
          <div class="favorite-name">{{ item.spuName }}</div>
          <div class="favorite-price" v-if="item.minPrice">¥{{ item.minPrice.toFixed(2) }}</div>
        </div>
        <el-button link type="danger" @click.stop="handleRemove(item.spuId)">取消收藏</el-button>
      </div>
    </div>

    <el-empty v-if="!loading && !list.length" description="暂无收藏商品">
      <el-button type="primary" @click="$router.push('/product')">去逛逛</el-button>
    </el-empty>

    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { favoriteApi } from '@ecommerce/shared'
import type { UserFavoriteDTO } from '@ecommerce/shared'

const router = useRouter()
const loading = ref(true)
const list = ref<UserFavoriteDTO[]>([])
const pageNum = ref(1)
const pageSize = 20
const total = ref(0)

async function fetchData() {
  loading.value = true
  try {
    const res = await favoriteApi.list({ pageNum: pageNum.value, pageSize })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

function goDetail(spuId: number) {
  router.push(`/product/${spuId}`)
}

async function handleRemove(spuId: number) {
  try {
    await favoriteApi.toggle(spuId)
    list.value = list.value.filter((item) => item.spuId !== spuId)
    total.value = Math.max(0, total.value - 1)
    ElMessage.success('已取消收藏')
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.favorites-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.favorites-page h2 {
  margin-bottom: 20px;
}
.favorite-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.favorite-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.favorite-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.favorite-img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  flex-shrink: 0;
}
.favorite-info {
  flex: 1;
  min-width: 0;
}
.favorite-name {
  font-size: 15px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.favorite-price {
  font-size: 18px;
  color: #e4393c;
  font-weight: bold;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
