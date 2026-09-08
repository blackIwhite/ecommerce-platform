<template>
  <div class="invoice-page">
    <h2>我的发票</h2>

    <el-button type="primary" @click="applyDialogVisible = true" style="margin-bottom: 16px">申请开票</el-button>

    <div v-loading="loading">
      <el-empty v-if="invoices.length === 0" description="暂无发票记录" />
      <div v-for="inv in invoices" :key="inv.id" class="invoice-card">
        <div class="inv-header">
          <el-tag :type="inv.status === 1 ? 'success' : inv.status === 0 ? 'warning' : 'danger'" size="small">
            {{ inv.statusName }}
          </el-tag>
          <span class="inv-amount">¥{{ inv.amount?.toFixed(2) }}</span>
        </div>
        <div class="inv-body">
          <div><strong>抬头：</strong>{{ inv.title }} <el-tag v-if="inv.type === 2" size="small">企业</el-tag></div>
          <div v-if="inv.taxNo"><strong>税号：</strong>{{ inv.taxNo }}</div>
          <div v-if="inv.invoiceNo"><strong>发票号：</strong>{{ inv.invoiceNo }}</div>
          <div v-if="inv.rejectReason" style="color: #f56c6c"><strong>拒绝原因：</strong>{{ inv.rejectReason }}</div>
          <div class="inv-time">申请时间：{{ inv.applyTime?.substring(0, 16) }}</div>
        </div>
        <div class="inv-actions" v-if="inv.invoiceUrl">
          <el-button type="primary" size="small" @click="downloadInvoice(inv.invoiceUrl)">下载发票</el-button>
        </div>
      </div>
    </div>

    <el-pagination v-if="total > 10" class="pagination" v-model:current-page="query.pageNum"
      :page-size="query.pageSize" :total="total" layout="prev, pager, next" @change="loadData" />

    <el-dialog v-model="applyDialogVisible" title="申请开票" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="订单ID" required>
          <el-input-number v-model="form.orderId" :min="1" placeholder="输入订单ID" />
        </el-form-item>
        <el-form-item label="发票类型" required>
          <el-radio-group v-model="form.type">
            <el-radio :value="1">个人</el-radio>
            <el-radio :value="2">企业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发票抬头" required>
          <el-input v-model="form.title" placeholder="个人姓名或公司名称" />
        </el-form-item>
        <el-form-item v-if="form.type === 2" label="税号" required>
          <el-input v-model="form.taxNo" placeholder="纳税人识别号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="接收电子发票的邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { invoiceApi } from '@ecommerce/shared'
import type { InvoiceDTO, InvoiceApplyRequest } from '@ecommerce/shared'

const loading = ref(false)
const submitting = ref(false)
const invoices = ref<InvoiceDTO[]>([])
const total = ref(0)
const applyDialogVisible = ref(false)

const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive<InvoiceApplyRequest>({
  orderId: 0, type: 1, title: '', taxNo: '', email: '', phone: '', remark: '',
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await invoiceApi.getMyInvoices(query)
    invoices.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleApply = async () => {
  if (!form.orderId || !form.title) {
    ElMessage.warning('请填写订单ID和发票抬头')
    return
  }
  if (form.type === 2 && !form.taxNo) {
    ElMessage.warning('企业发票需填写税号')
    return
  }
  submitting.value = true
  try {
    await invoiceApi.apply(form)
    ElMessage.success('申请已提交')
    applyDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const downloadInvoice = (url: string) => {
  window.open(url, '_blank')
}

onMounted(loadData)
</script>

<style scoped>
.invoice-page { padding: 20px; max-width: 800px; margin: 0 auto; }
.invoice-page h2 { margin-bottom: 16px; }
.invoice-card { border: 1px solid #eee; border-radius: 8px; padding: 16px; margin-bottom: 12px; }
.inv-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.inv-amount { font-size: 18px; font-weight: bold; color: #f56c6c; }
.inv-body div { margin-bottom: 4px; font-size: 14px; color: #666; }
.inv-time { color: #999; font-size: 12px; margin-top: 8px; }
.inv-actions { margin-top: 10px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
