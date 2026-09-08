<template>
  <div class="product-list-page">
    <div class="filter-section">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索商品"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>

      <div class="category-filter" v-if="categoryTree.length > 0">
        <el-check-tag
          :checked="!queryParams.categoryId"
          @change="selectCategory(undefined)"
          style="margin-right: 8px; margin-bottom: 8px;"
        >
          全部
        </el-check-tag>
        <el-check-tag
          v-for="cat in flatCategories"
          :key="cat.id"
          :checked="queryParams.categoryId === cat.id"
          @change="selectCategory(cat.id)"
          style="margin-right: 8px; margin-bottom: 8px;"
        >
          {{ cat.name }}
        </el-check-tag>
      </div>

      <div class="sort-bar">
        <span
          v-for="s in sortOptions"
          :key="s.value"
          class="sort-item"
          :class="{ active: currentSort === s.value }"
          @click="handleSort(s.value)"
        >
          {{ s.label }}
        </span>
      </div>
    </div>

    <div v-loading="loading">
      <el-row :gutter="16" v-if="tableData.length > 0">
        <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="item in tableData" :key="item.spuId">
          <div class="product-card" @click="$router.push(`/product/${item.spuId}`)">
            <el-image
              :src="getFirstImage(item.images)"
              fit="cover"
              class="product-image"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="32"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="product-info">
              <div class="product-name">{{ item.name }}</div>
              <div class="product-category">{{ item.brandName }}</div>
              <div class="product-price" v-if="item.minPrice">¥{{ item.minPrice.toFixed(2) }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-else-if="!loading" description="暂无商品" />
    </div>

    <div style="display: flex; justify-content: center; margin-top: 24px;" v-if="total > 0">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[12, 24, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Picture } from '@element-plus/icons-vue'
import { productApi } from '@ecommerce/shared'
import type { SpuDTO, CategoryDTO } from '@ecommerce/shared'

const loading = ref(false)
const tableData = ref<SpuDTO[]>([])
const total = ref(0)
const categoryTree = ref<CategoryDTO[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  keyword: '',
  categoryId: undefined as number | undefined,
  status: 1,
  sort: '',
})

const sortOptions = [
  { label: '综合', value: '' },
  { label: '销量', value: 'sales_desc' },
  { label: '价格↑', value: 'price_asc' },
  { label: '价格↓', value: 'price_desc' },
  { label: '最新', value: 'newest' },
]

const currentSort = ref('')

function handleSort(value: string) {
  currentSort.value = value
  queryParams.sort = value
  queryParams.pageNum = 1
  fetchData()
}

const flatCategories = computed(() => {
  const result: CategoryDTO[] = []
  function flatten(nodes: CategoryDTO[]) {
    for (const node of nodes) {
      result.push(node)
      if (node.children?.length) {
        flatten(node.children)
      }
    }
  }
  flatten(categoryTree.value)
  return result
})

function getFirstImage(images: string): string {
  try {
    const arr = JSON.parse(images)
    return arr.length > 0 ? arr[0] : ''
  } catch {
    return ''
  }
}

function selectCategory(id: number | undefined) {
  queryParams.categoryId = id
  queryParams.pageNum = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await productApi.getSpuPage(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchData()
}

onMounted(async () => {
  fetchData()
  try {
    categoryTree.value = await productApi.getCategoryTree()
  } catch {
    // non-critical
  }
})
</script>

<style scoped>
.product-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.filter-section {
  margin-bottom: 24px;
}

.category-filter {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
}

.sort-bar {
  margin-top: 12px;
  display: flex;
  gap: 20px;
}

.sort-item {
  cursor: pointer;
  font-size: 14px;
  color: #666;
  padding: 4px 0;
  transition: color 0.2s;
}

.sort-item:hover {
  color: #409eff;
}

.sort-item.active {
  color: #409eff;
  font-weight: 600;
}

.product-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eaeaea;
  transition: box-shadow 0.2s;
  margin-bottom: 16px;
  background: #fff;
}

.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  aspect-ratio: 1;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #ccc;
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  line-height: 1.4;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-category {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.product-price {
  font-size: 16px;
  color: #e4393c;
  font-weight: 600;
  margin-top: 6px;
}
</style>
