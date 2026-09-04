<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
      <h2>商品管理</h2>
      <div>
        <el-button :loading="reindexing" @click="handleReindex">重建索引</el-button>
        <el-button type="primary" @click="$router.push('/product/edit')">新增商品</el-button>
      </div>
    </div>

    <el-form :inline="true" :model="queryParams" class="filter-bar" @submit.prevent="handleSearch">
      <el-form-item label="关键词">
        <el-input v-model="queryParams.keyword" placeholder="商品名称" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="分类">
        <el-cascader
          v-model="selectedCategory"
          :options="categoryTree"
          :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
          placeholder="请选择分类"
          clearable
          style="width: 180px"
          @change="onCategoryChange"
        />
      </el-form-item>
      <el-form-item label="品牌">
        <el-select v-model="queryParams.brandId" placeholder="请选择品牌" clearable style="width: 150px">
          <el-option v-for="brand in brandList" :key="brand.id" :label="brand.name" :value="brand.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
          <el-option label="下架" :value="0" />
          <el-option label="上架" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border v-loading="loading" style="width: 100%">
      <el-table-column label="商品图片" width="80">
        <template #default="{ row }">
          <el-image
            v-if="getFirstImage(row.images)"
            :src="getFirstImage(row.images)"
            style="width: 50px; height: 50px"
            fit="cover"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="brandName" label="品牌" width="120" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/product/edit/${row.spuId}`)">编辑</el-button>
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productApi, formatDate } from '@ecommerce/shared'
import type { SpuDTO, CategoryDTO, BrandDTO } from '@ecommerce/shared'

const loading = ref(false)
const tableData = ref<SpuDTO[]>([])
const total = ref(0)
const categoryTree = ref<CategoryDTO[]>([])
const brandList = ref<BrandDTO[]>([])
const selectedCategory = ref<number | undefined>()
const reindexing = ref(false)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  categoryId: undefined as number | undefined,
  brandId: undefined as number | undefined,
  status: undefined as number | undefined,
})

function getFirstImage(images: string): string {
  try {
    const arr = JSON.parse(images)
    return arr.length > 0 ? arr[0] : ''
  } catch {
    return ''
  }
}

function onCategoryChange(val: number | undefined) {
  queryParams.categoryId = val
}

async function fetchData() {
  loading.value = true
  try {
    const res = await productApi.getSpuPage(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (e: any) {
    ElMessage.error(e.message || '加载商品列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchData()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.categoryId = undefined
  queryParams.brandId = undefined
  queryParams.status = undefined
  selectedCategory.value = undefined
  queryParams.pageNum = 1
  fetchData()
}

async function handleToggleStatus(row: SpuDTO) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await productApi.updateSpuStatus(row.spuId, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || `${action}失败`)
    }
  }
}

async function handleReindex() {
  reindexing.value = true
  try {
    await productApi.reindexAll()
    ElMessage.success('索引重建完成')
  } catch (e: any) {
    ElMessage.error(e.message || '索引重建失败')
  } finally {
    reindexing.value = false
  }
}

async function loadFilters() {
  try {
    const [categories, brands] = await Promise.all([
      productApi.getCategoryTree(),
      productApi.getBrandList(),
    ])
    categoryTree.value = categories
    brandList.value = brands
  } catch {
    // filter loading failure is non-critical
  }
}

onMounted(() => {
  fetchData()
  loadFilters()
})
</script>

<style scoped>
.filter-bar {
  margin-bottom: 16px;
}
</style>
