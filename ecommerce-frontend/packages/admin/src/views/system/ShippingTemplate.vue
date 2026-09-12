<template>
  <div class="shipping-page">
    <div class="page-header">
      <h2>运费模板</h2>
      <el-button type="primary" @click="openDialog()">新增模板</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="模板名称" min-width="150" />
      <el-table-column label="计费方式" width="100">
        <template #default="{ row }">
          {{ chargeTypeMap[row.chargeType] || row.chargeType }}
        </template>
      </el-table-column>
      <el-table-column label="默认运费" width="100">
        <template #default="{ row }">¥{{ row.defaultFee?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="免运费门槛" width="120">
        <template #default="{ row }">
          {{ row.freeThreshold ? `¥${row.freeThreshold.toFixed(2)}` : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button text :type="row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(row)">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑模板' : '新增模板'" width="700px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input v-model="form.name" placeholder="如：全国包邮" />
        </el-form-item>
        <el-form-item label="计费方式" required>
          <el-select v-model="form.chargeType" style="width: 100%">
            <el-option :value="1" label="按件数" />
            <el-option :value="2" label="按重量" />
            <el-option :value="3" label="按体积" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认运费" required>
          <el-input-number v-model="form.defaultFee" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="免运费门槛">
          <el-input-number v-model="form.freeThreshold" :min="0" :precision="2" :step="10" />
          <span style="margin-left: 8px; color: #999; font-size: 12px">0 表示无免运费</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-divider>区域规则</el-divider>
        <div v-for="(rule, idx) in form.rules" :key="idx" class="rule-row">
          <el-input v-model="rule.regionNames" placeholder="区域名称" style="width: 120px" />
          <el-input v-model="rule.regionCodes" placeholder="区域编码" style="width: 120px" />
          <el-input-number v-model="rule.startThreshold" :min="0" :precision="2" placeholder="首费阈值" controls-position="right" />
          <el-input-number v-model="rule.startFee" :min="0" :precision="2" placeholder="首费" controls-position="right" />
          <el-input-number v-model="rule.additionalThreshold" :min="0" :precision="2" placeholder="续费单位" controls-position="right" />
          <el-input-number v-model="rule.additionalFee" :min="0" :precision="2" placeholder="续费" controls-position="right" />
          <el-button type="danger" text size="small" @click="form.rules.splice(idx, 1)">删除</el-button>
        </div>
        <el-button type="primary" link @click="addRule" style="margin-top: 8px">+ 添加区域规则</el-button>
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
import { shippingApi } from '@ecommerce/shared'
import type { ShippingTemplateDTO } from '@ecommerce/shared'

const chargeTypeMap: Record<number, string> = { 1: '按件数', 2: '按重量', 3: '按体积' }

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ShippingTemplateDTO[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)

const defaultForm = () => ({
  id: undefined as number | undefined,
  name: '',
  chargeType: 1,
  defaultFee: 0,
  freeThreshold: 0,
  status: 1,
  rules: [] as any[],
})
const form = reactive(defaultForm())

const loadData = async () => {
  loading.value = true
  try {
    const res = await shippingApi.page(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const openDialog = async (row?: ShippingTemplateDTO) => {
  if (row) {
    const detail = await shippingApi.getDetail(row.id)
    Object.assign(form, {
      id: detail.id,
      name: detail.name,
      chargeType: detail.chargeType,
      defaultFee: detail.defaultFee,
      freeThreshold: detail.freeThreshold,
      status: detail.status,
      rules: (detail.rules || []).map(r => ({
        id: r.id,
        regionCodes: r.regionCodes,
        regionNames: r.regionNames,
        startThreshold: r.startThreshold,
        startFee: r.startFee,
        additionalThreshold: r.additionalThreshold,
        additionalFee: r.additionalFee,
      })),
    })
  } else {
    Object.assign(form, defaultForm())
  }
  dialogVisible.value = true
}

const addRule = () => {
  form.rules.push({
    regionCodes: '', regionNames: '',
    startThreshold: 1, startFee: 0,
    additionalThreshold: 1, additionalFee: 0,
  })
}

const handleSubmit = async () => {
  if (!form.name) { ElMessage.warning('请填写模板名称'); return }
  submitting.value = true
  try {
    if (form.id) {
      await shippingApi.update({ ...form })
    } else {
      await shippingApi.create({ ...form })
    }
    ElMessage.success(form.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row: ShippingTemplateDTO) => {
  await shippingApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  loadData()
}

const handleDelete = async (id: number) => {
  await shippingApi.delete(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.shipping-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
.rule-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
</style>
