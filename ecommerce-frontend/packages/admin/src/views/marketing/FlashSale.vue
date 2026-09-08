<template>
  <div class="flash-sale-page">
    <div class="page-header">
      <h2>秒杀商品管理</h2>
      <el-button type="primary" @click="openDialog">添加秒杀商品</el-button>
    </div>

    <el-form :inline="true" class="filter-form">
      <el-form-item label="促销活动">
        <el-select v-model="promotionId" placeholder="选择活动" @change="loadData">
          <el-option v-for="p in promotions" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="skuId" label="SKU ID" width="100" />
      <el-table-column prop="productName" label="商品名称" min-width="180" />
      <el-table-column label="秒杀价" width="100">
        <template #default="{ row }">¥{{ row.flashPrice }}</template>
      </el-table-column>
      <el-table-column label="库存" width="120">
        <template #default="{ row }">{{ row.availableStock }}/{{ row.totalStock }}</template>
      </el-table-column>
      <el-table-column prop="limitPerUser" label="限购" width="70" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑秒杀商品' : '添加秒杀商品'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="SKU ID" required>
          <el-input-number v-model="form.skuId" :min="1" :disabled="!!form.id" style="width: 100%" />
        </el-form-item>
        <el-form-item label="秒杀价" required>
          <el-input-number v-model="form.flashPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="总库存" required>
          <el-input-number v-model="form.totalStock" :min="1" />
        </el-form-item>
        <el-form-item label="每人限购" required>
          <el-input-number v-model="form.limitPerUser" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { flashSaleApi, promotionApi } from '@ecommerce/shared'
import type { FlashSaleItemDTO, PromotionDTO } from '@ecommerce/shared'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<FlashSaleItemDTO[]>([])
const promotions = ref<PromotionDTO[]>([])
const promotionId = ref<number | undefined>()
const dialogVisible = ref(false)

const defaultForm = (): FlashSaleItemDTO => ({
  spuId: 0, skuId: 0, flashPrice: 0, totalStock: 0, limitPerUser: 1,
})
const form = ref<FlashSaleItemDTO>(defaultForm())

const loadPromotions = async () => {
  promotions.value = await promotionApi.listActive()
  if (promotions.value.length > 0) {
    promotionId.value = promotions.value[0].id
    loadData()
  }
}

const loadData = async () => {
  if (!promotionId.value) return
  loading.value = true
  try {
    tableData.value = await flashSaleApi.listItems(promotionId.value)
  } finally {
    loading.value = false
  }
}

const openDialog = (row?: FlashSaleItemDTO) => {
  form.value = row ? { ...row } : defaultForm()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!promotionId.value || !form.value.skuId) {
    ElMessage.warning('请填写必要信息')
    return
  }
  submitting.value = true
  try {
    const data = { ...form.value, promotionId: promotionId.value }
    if (form.value.id) {
      await flashSaleApi.updateItem(form.value.id, data)
      ElMessage.success('更新成功')
    } else {
      await flashSaleApi.addItem(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  await flashSaleApi.deleteItem(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadPromotions)
</script>

<style scoped>
.flash-sale-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-form { margin-bottom: 16px; }
</style>
