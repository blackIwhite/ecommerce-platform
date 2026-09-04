<template>
  <div v-loading="loading">
    <h2>库存管理</h2>

    <div class="search-bar">
      <el-input v-model="search.skuId" placeholder="SKU ID" clearable style="width: 160px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="items" border>
      <el-table-column prop="skuId" label="SKU ID" width="100" />
      <el-table-column label="可用库存" width="120">
        <template #default="{ row }">
          <span :class="{ 'low-stock': row.availableStock < 10 }">{{ row.availableStock }}</span>
        </template>
      </el-table-column>
      <el-table-column label="锁定库存" width="120">
        <template #default="{ row }">{{ row.lockedStock }}</template>
      </el-table-column>
      <el-table-column label="总库存" width="120">
        <template #default="{ row }">{{ row.availableStock + row.lockedStock }}</template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDate(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openAdjust(row)">调整</el-button>
          <el-button size="small" type="primary" @click="openSet(row)">设置</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadItems"
      />
    </div>

    <el-dialog v-model="adjustDialogVisible" title="库存调整" width="360px">
      <el-form label-width="80px">
        <el-form-item label="SKU ID">{{ currentSkuId }}</el-form-item>
        <el-form-item label="变动数量">
          <el-input-number v-model="adjustDelta" placeholder="正数增加，负数减少" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdjust">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="setDialogVisible" title="设置库存" width="360px">
      <el-form label-width="80px">
        <el-form-item label="SKU ID">{{ currentSkuId }}</el-form-item>
        <el-form-item label="库存数量">
          <el-input-number v-model="setStock" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="setDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSet">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { inventoryApi, formatDate } from '@ecommerce/shared'
import type { InventoryDTO } from '@ecommerce/shared'

const loading = ref(true)
const items = ref<InventoryDTO[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const search = reactive({ skuId: '' })

const adjustDialogVisible = ref(false)
const setDialogVisible = ref(false)
const currentSkuId = ref(0)
const adjustDelta = ref(0)
const setStock = ref(0)

const loadItems = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (search.skuId) params.skuId = Number(search.skuId)
    const res = await inventoryApi.list(params)
    items.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载库存失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadItems()
}

const openAdjust = (row: InventoryDTO) => {
  currentSkuId.value = row.skuId
  adjustDelta.value = 0
  adjustDialogVisible.value = true
}

const openSet = (row: InventoryDTO) => {
  currentSkuId.value = row.skuId
  setStock.value = row.availableStock
  setDialogVisible.value = true
}

const confirmAdjust = async () => {
  if (adjustDelta.value === 0) return
  try {
    await inventoryApi.adjustStock(currentSkuId.value, adjustDelta.value)
    ElMessage.success('调整成功')
    adjustDialogVisible.value = false
    loadItems()
  } catch {
    ElMessage.error('调整失败')
  }
}

const confirmSet = async () => {
  try {
    await inventoryApi.setStock(currentSkuId.value, setStock.value)
    ElMessage.success('设置成功')
    setDialogVisible.value = false
    loadItems()
  } catch {
    ElMessage.error('设置失败')
  }
}

onMounted(loadItems)
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.low-stock {
  color: #e4393c;
  font-weight: bold;
}
</style>
