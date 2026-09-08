<template>
  <div class="coupon-list">
    <div class="page-header">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="openDialog()">新建优惠券</el-button>
    </div>

    <el-form :inline="true" class="filter-form">
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
          <el-tag :type="row.type === 1 ? '' : 'warning'">
            {{ row.type === 1 ? '满减券' : '折扣券' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="优惠" width="120">
        <template #default="{ row }">
          {{ row.type === 1 ? `¥${row.discountValue}` : `${row.discountValue}%` }}
        </template>
      </el-table-column>
      <el-table-column label="门槛" width="100">
        <template #default="{ row }">¥{{ row.minPurchase }}</template>
      </el-table-column>
      <el-table-column label="已领/总量" width="100">
        <template #default="{ row }">
          {{ row.claimedCount }}/{{ row.totalCount === -1 ? '∞' : row.totalCount }}
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑优惠券' : '新建优惠券'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="优惠券名称" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="form.type">
            <el-radio :value="1">满减券</el-radio>
            <el-radio :value="2">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.type === 1 ? '减免金额' : '折扣(%)'" required>
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="最低消费">
          <el-input-number v-model="form.minPurchase" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item v-if="form.type === 2" label="最高减免">
          <el-input-number v-model="form.maxDiscount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="发放总量">
          <el-input-number v-model="form.totalCount" :min="-1" />
          <span style="margin-left: 8px; color: #999; font-size: 12px">-1 表示不限</span>
        </el-form-item>
        <el-form-item label="每人限领">
          <el-input-number v-model="form.perLimit" :min="1" />
        </el-form-item>
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
import { couponApi } from '@ecommerce/shared'
import type { CouponTemplateDTO } from '@ecommerce/shared'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<CouponTemplateDTO[]>([])
const total = ref(0)
const dialogVisible = ref(false)

const query = reactive({ pageNum: 1, pageSize: 10, status: undefined as number | undefined })

const defaultForm = (): CouponTemplateDTO => ({
  name: '', type: 1, discountValue: 0, minPurchase: 0,
  maxDiscount: undefined, totalCount: -1, perLimit: 1,
  startTime: '', endTime: '', description: '',
})
const form = ref<CouponTemplateDTO>(defaultForm())

const loadData = async () => {
  loading.value = true
  try {
    const res = await couponApi.listTemplates(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const openDialog = (row?: CouponTemplateDTO) => {
  form.value = row ? { ...row } : defaultForm()
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
      await couponApi.updateTemplate(form.value)
      ElMessage.success('更新成功')
    } else {
      await couponApi.createTemplate(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row: CouponTemplateDTO) => {
  const newStatus = row.status === 1 ? 0 : 1
  await couponApi.updateTemplateStatus(row.id!, newStatus)
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  loadData()
}

const handleDelete = async (id: number) => {
  await couponApi.deleteTemplate(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.coupon-list { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
