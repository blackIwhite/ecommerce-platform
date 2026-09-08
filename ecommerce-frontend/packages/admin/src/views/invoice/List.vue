<template>
  <div class="invoice-list">
    <div class="page-header">
      <h2>发票管理</h2>
    </div>

    <el-form :inline="true" class="filter-form">
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" @change="loadData">
          <el-option label="待开票" :value="0" />
          <el-option label="已开票" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === 2 ? 'warning' : ''">{{ row.type === 1 ? '个人' : '企业' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="抬头" min-width="150" show-overflow-tooltip />
      <el-table-column prop="taxNo" label="税号" width="160" show-overflow-tooltip />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'">
            {{ row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="invoiceNo" label="发票号" width="140" show-overflow-tooltip />
      <el-table-column label="申请时间" width="160">
        <template #default="{ row }">{{ row.applyTime?.substring(0, 16) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" text type="success" @click="openIssueDialog(row)">开票</el-button>
          <el-button v-if="row.status === 0" text type="danger" @click="openRejectDialog(row)">拒绝</el-button>
          <el-button v-if="row.invoiceUrl" text type="primary" @click="viewInvoice(row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pagination" v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize" :total="total" :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next" @change="loadData" />

    <el-dialog v-model="issueDialogVisible" title="开具发票" width="450px">
      <el-form :model="issueForm" label-width="90px">
        <el-form-item label="发票号码" required>
          <el-input v-model="issueForm.invoiceNo" placeholder="输入发票号码" />
        </el-form-item>
        <el-form-item label="发票URL">
          <el-input v-model="issueForm.invoiceUrl" placeholder="电子发票PDF地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleIssue">确定开票</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝开票" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleReject">确定拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { invoiceApi } from '@ecommerce/shared'
import type { InvoiceDTO } from '@ecommerce/shared'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<InvoiceDTO[]>([])
const total = ref(0)
const issueDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const currentInvoice = ref<InvoiceDTO | null>(null)

const query = reactive({ pageNum: 1, pageSize: 10, status: undefined as number | undefined })
const issueForm = reactive({ invoiceNo: '', invoiceUrl: '' })
const rejectForm = reactive({ reason: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await invoiceApi.listAdmin(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const openIssueDialog = (row: InvoiceDTO) => {
  currentInvoice.value = row
  issueForm.invoiceNo = ''
  issueForm.invoiceUrl = ''
  issueDialogVisible.value = true
}

const openRejectDialog = (row: InvoiceDTO) => {
  currentInvoice.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const handleIssue = async () => {
  if (!issueForm.invoiceNo) {
    ElMessage.warning('请输入发票号码')
    return
  }
  submitting.value = true
  try {
    await invoiceApi.issue(currentInvoice.value!.id, issueForm)
    ElMessage.success('开票成功')
    issueDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  submitting.value = true
  try {
    await invoiceApi.reject(currentInvoice.value!.id, rejectForm)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const viewInvoice = (row: InvoiceDTO) => {
  window.open(row.invoiceUrl, '_blank')
}

onMounted(loadData)
</script>

<style scoped>
.invoice-list { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
