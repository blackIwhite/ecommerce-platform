<template>
  <div class="promotion-list">
    <div class="page-header">
      <h2>促销活动管理</h2>
      <el-button type="primary" @click="openDialog()">新建促销</el-button>
    </div>

    <el-form :inline="true" class="filter-form">
      <el-form-item label="类型">
        <el-select v-model="query.type" clearable placeholder="全部" @change="loadData">
          <el-option label="满减" :value="1" />
          <el-option label="秒杀" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" @change="loadData">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? '' : 'danger'">
            {{ row.type === 1 ? '满减' : '秒杀' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="规则" min-width="180">
        <template #default="{ row }">
          <template v-if="row.type === 1 && row.rules">
            <div v-for="(rule, i) in row.rules" :key="i" style="font-size: 12px">
              满{{ rule.minAmount }}减{{ rule.reduction }}
            </div>
          </template>
          <span v-else-if="row.type === 2">{{ row.items?.length || 0 }}件秒杀商品</span>
        </template>
      </el-table-column>
      <el-table-column label="有效期" width="180">
        <template #default="{ row }">
          {{ row.startTime?.substring(0, 10) }} ~ {{ row.endTime?.substring(0, 10) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button text :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pagination" v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize" :total="total" :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next" @change="loadData" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑促销' : '新建促销'" width="650px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="促销名称" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="form.type">
            <el-radio :value="1">满减</el-radio>
            <el-radio :value="2">秒杀</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="form.type === 1">
          <el-form-item label="满减规则">
            <div v-for="(rule, index) in form.rules" :key="index" style="display: flex; gap: 8px; margin-bottom: 8px">
              <span>满</span>
              <el-input-number v-model="rule.minAmount" :min="0" :precision="2" size="small" style="width: 120px" />
              <span>减</span>
              <el-input-number v-model="rule.reduction" :min="0" :precision="2" size="small" style="width: 120px" />
              <el-button text type="danger" @click="form.rules!.splice(index, 1)">删除</el-button>
            </div>
            <el-button type="primary" link @click="form.rules!.push({ minAmount: 0, reduction: 0 })">+ 添加规则</el-button>
          </el-form-item>
        </template>

        <template v-if="form.type === 2">
          <el-form-item label="秒杀商品">
            <div v-for="(item, index) in form.items" :key="index" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
              <span>SKU:</span>
              <el-input-number v-model="item.skuId" :min="1" size="small" style="width: 120px" />
              <span>SPU:</span>
              <el-input-number v-model="item.spuId" :min="1" size="small" style="width: 120px" />
              <span>价:</span>
              <el-input-number v-model="item.flashPrice" :min="0" :precision="2" size="small" style="width: 100px" />
              <span>库存:</span>
              <el-input-number v-model="item.totalStock" :min="1" size="small" style="width: 80px" />
              <el-button text type="danger" @click="form.items!.splice(index, 1)">删除</el-button>
            </div>
            <el-button type="primary" link @click="form.items!.push({ spuId: 0, skuId: 0, flashPrice: 0, totalStock: 0, limitPerUser: 1 })">+ 添加商品</el-button>
          </el-form-item>
        </template>

        <el-form-item label="有效期" required>
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="开始时间" />
          <span style="margin: 0 8px">~</span>
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="结束时间" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { promotionApi } from '@ecommerce/shared'
import type { PromotionDTO } from '@ecommerce/shared'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<PromotionDTO[]>([])
const total = ref(0)
const dialogVisible = ref(false)

const query = reactive({
  pageNum: 1, pageSize: 10,
  type: undefined as number | undefined,
  status: undefined as number | undefined,
})

const defaultForm = (): PromotionDTO => ({
  name: '', type: 1, rules: [], items: [],
  startTime: '', endTime: '', description: '',
})
const form = ref<PromotionDTO>(defaultForm())

const loadData = async () => {
  loading.value = true
  try {
    const res = await promotionApi.list(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const openDialog = (row?: PromotionDTO) => {
  if (row) {
    form.value = {
      ...row,
      rules: row.rules ? [...row.rules] : [],
      items: row.items ? [...row.items] : [],
    }
  } else {
    form.value = defaultForm()
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请填写必要信息')
    return
  }
  submitting.value = true
  try {
    if (form.value.id) {
      await promotionApi.update(form.value)
      ElMessage.success('更新成功')
    } else {
      await promotionApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: PromotionDTO) => {
  const newStatus = row.status === 1 ? 0 : 1
  await promotionApi.updateStatus(row.id!, newStatus)
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  loadData()
}

const handleDelete = async (id: number) => {
  await promotionApi.delete(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.promotion-list { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
