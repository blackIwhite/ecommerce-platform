<template>
  <div class="product-list-page">
    <div class="filter-section">
      <div class="search-wrapper" ref="searchWrapperRef">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索商品"
          clearable
          style="width: 360px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          @focus="showDropdown = true"
          @input="onKeywordInput"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>

        <div class="search-dropdown" v-if="showDropdown">
          <div v-if="suggestions.length && queryParams.keyword" class="dropdown-section">
            <div class="section-title">搜索建议</div>
            <div class="tag-list">
              <span
                v-for="s in suggestions"
                :key="s"
                class="keyword-tag"
                @click="searchKeyword(s)"
              >{{ s }}</span>
            </div>
          </div>

          <div v-if="history.length && !queryParams.keyword" class="dropdown-section">
            <div class="section-title">
              <span>搜索历史</span>
              <el-icon class="clear-btn" @click="handleClearHistory"><Delete /></el-icon>
            </div>
            <div class="tag-list">
              <span
                v-for="h in history"
                :key="h"
                class="keyword-tag"
                @click="searchKeyword(h)"
              >{{ h }}</span>
            </div>
          </div>

          <div v-if="hotSearches.length" class="dropdown-section">
            <div class="section-title">热门搜索</div>
            <div class="tag-list">
              <span
                v-for="(item, idx) in hotSearches"
                :key="item.id"
                class="keyword-tag"
                :class="{ hot: idx < 3 }"
                @click="searchKeyword(item.keyword)"
              >
                <span class="rank" v-if="idx < 3">{{ idx + 1 }}</span>
                {{ item.keyword }}
              </span>
            </div>
          </div>

          <el-empty
            v-if="!suggestions.length && !history.length && !hotSearches.length"
            description="暂无数据"
            :image-size="60"
          />
        </div>
      </div>

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
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { Search, Picture, Delete } from '@element-plus/icons-vue'
import { productApi, searchApi } from '@ecommerce/shared'
import type { SpuDTO, CategoryDTO, HotSearchDTO } from '@ecommerce/shared'

const loading = ref(false)
const tableData = ref<SpuDTO[]>([])
const total = ref(0)
const categoryTree = ref<CategoryDTO[]>([])

const showDropdown = ref(false)
const history = ref<string[]>([])
const hotSearches = ref<HotSearchDTO[]>([])
const suggestions = ref<string[]>([])
const searchWrapperRef = ref<HTMLElement | null>(null)
let suggestTimer: ReturnType<typeof setTimeout> | null = null

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
  showDropdown.value = false
  const kw = queryParams.keyword?.trim()
  if (kw) {
    searchApi.record(kw).catch(() => {})
  }
  queryParams.pageNum = 1
  fetchData()
}

function searchKeyword(kw: string) {
  queryParams.keyword = kw
  handleSearch()
}

function onKeywordInput() {
  if (suggestTimer) clearTimeout(suggestTimer)
  const kw = queryParams.keyword?.trim()
  if (!kw) {
    suggestions.value = []
    return
  }
  suggestTimer = setTimeout(async () => {
    try {
      suggestions.value = await searchApi.getSuggestions(kw)
    } catch {
      suggestions.value = []
    }
  }, 300)
}

async function loadHistory() {
  try {
    history.value = await searchApi.getHistory()
  } catch {
    history.value = []
  }
}

async function loadHotSearches() {
  try {
    hotSearches.value = await searchApi.getHotSearches()
  } catch {
    hotSearches.value = []
  }
}

async function handleClearHistory() {
  try {
    await searchApi.clearHistory()
    history.value = []
  } catch {
    // ignore
  }
}

function onClickOutside(e: MouseEvent) {
  if (searchWrapperRef.value && !searchWrapperRef.value.contains(e.target as Node)) {
    showDropdown.value = false
  }
}

onMounted(async () => {
  fetchData()
  loadHistory()
  loadHotSearches()
  document.addEventListener('click', onClickOutside)
  try {
    categoryTree.value = await productApi.getCategoryTree()
  } catch {
    // non-critical
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside)
  if (suggestTimer) clearTimeout(suggestTimer)
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

.search-wrapper {
  position: relative;
  display: inline-block;
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  width: 360px;
  margin-top: 4px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  z-index: 100;
  padding: 12px 16px;
  max-height: 400px;
  overflow-y: auto;
}

.dropdown-section {
  margin-bottom: 16px;
}

.dropdown-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.clear-btn {
  cursor: pointer;
  font-size: 14px;
  color: #ccc;
  transition: color 0.2s;
}

.clear-btn:hover {
  color: #e4393c;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  font-size: 13px;
  color: #666;
  background: #f5f7fa;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keyword-tag:hover {
  background: #e8eaed;
  color: #333;
}

.keyword-tag.hot {
  background: #fff5f0;
  color: #ff6600;
}

.keyword-tag.hot:hover {
  background: #ffe8d6;
}

.keyword-tag .rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  background: #ff6600;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>
