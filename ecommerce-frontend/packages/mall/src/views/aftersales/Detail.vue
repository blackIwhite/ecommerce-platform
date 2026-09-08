<template>
  <div class="aftersales-detail">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>售后详情</h2>
    </div>

    <div v-if="loading" v-loading="true" style="height: 300px" />
    <template v-else-if="detail">
      <div class="status-card">
        <el-steps :active="stepActive" finish-status="success" align-center>
          <el-step title="提交申请" />
          <el-step title="审核" />
          <el-step title="退货" />
          <el-step title="退款" />
          <el-step title="完成" />
        </el-steps>
        <div class="status-info">
          <el-tag :type="statusTagType(detail.status)" size="large">{{ detail.statusName }}</el-tag>
          <span class="reason" v-if="detail.handleRemark">处理备注：{{ detail.handleRemark }}</span>
        </div>
      </div>

      <el-card shadow="never" class="section-card">
        <template #header><span>基本信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="售后编号">{{ detail.aftersalesNo }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.typeName }}</el-descriptions-item>
          <el-descriptions-item label="原因">{{ detail.reason }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">
            <span style="color: #e4393c">¥{{ detail.refundAmount?.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退货物流" :span="2" v-if="detail.returnTrackingNo">
            {{ detail.returnCompany }} {{ detail.returnTrackingNo }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="section-card" v-if="detail.items?.length">
        <template #header><span>商品明细</span></template>
        <div v-for="item in detail.items" :key="item.id" class="item-row">
          <img :src="item.image" class="item-img" />
          <div class="item-info">
            <div class="item-name">{{ item.skuName }}</div>
            <div class="item-meta">¥{{ item.price }} × {{ item.quantity }}</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="section-card">
        <template #header><span>处理记录</span></template>
        <el-timeline>
          <el-timeline-item v-for="(log, i) in detail.logs" :key="i"
            :timestamp="log.createTime" placement="top">
            <div class="log-item">
              <el-tag size="small" :type="statusTagType(log.toStatus)">{{ log.toStatusName }}</el-tag>
              <span>{{ log.remark }}</span>
              <span class="log-operator">（{{ log.operator }}）</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <div class="actions">
        <el-button v-if="detail.status === 0" @click="handleCancel">取消申请</el-button>
        <el-button v-if="detail.status === 1" type="primary" @click="showTrackingDialog = true">
          填写退货物流
        </el-button>
      </div>
    </template>

    <el-dialog v-model="showTrackingDialog" title="填写退货物流" width="400px">
      <el-form :model="trackingForm" label-width="80px">
        <el-form-item label="物流公司">
          <el-input v-model="trackingForm.company" placeholder="如：顺丰快递" />
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="trackingForm.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTrackingDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTracking">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { aftersalesApi } from '@ecommerce/shared'
import type { AftersalesOrderDTO } from '@ecommerce/shared'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<AftersalesOrderDTO | null>(null)
const showTrackingDialog = ref(false)
const trackingForm = ref({ company: '', trackingNo: '' })

const stepActive = computed(() => {
  const s = detail.value?.status ?? -1
  if (s === 7 || s === 8) return 1
  if (s >= 6) return 4
  if (s >= 5) return 3
  if (s >= 2) return 2
  if (s >= 1) return 1
  return 0
})

const statusTagType = (status: number) => {
  if (status === 0) return 'warning'
  if (status === 6) return 'success'
  if (status === 7 || status === 8) return 'danger'
  return 'primary'
}

const loadDetail = async () => {
  loading.value = true
  try {
    detail.value = await aftersalesApi.getDetail(Number(route.params.id))
  } finally {
    loading.value = false
  }
}

const handleCancel = async () => {
  await ElMessageBox.confirm('确定取消该售后申请？', '提示')
  await aftersalesApi.cancel(Number(route.params.id))
  ElMessage.success('已取消')
  loadDetail()
}

const handleSubmitTracking = async () => {
  if (!trackingForm.value.trackingNo || !trackingForm.value.company) {
    ElMessage.warning('请填写完整物流信息')
    return
  }
  await aftersalesApi.fillTracking(
    Number(route.params.id),
    trackingForm.value.trackingNo,
    trackingForm.value.company
  )
  ElMessage.success('物流信息已提交')
  showTrackingDialog.value = false
  loadDetail()
}

onMounted(loadDetail)
</script>

<style scoped>
.aftersales-detail { max-width: 800px; margin: 0 auto; padding: 20px; }
.page-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.status-card {
  background: #fff; border: 1px solid #eee; border-radius: 8px;
  padding: 24px; margin-bottom: 16px;
}
.status-info { display: flex; align-items: center; gap: 12px; margin-top: 16px; justify-content: center; }
.reason { font-size: 14px; color: #666; }
.section-card { margin-bottom: 16px; }
.item-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.item-row:last-child { border-bottom: none; }
.item-img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
.item-name { font-size: 14px; font-weight: 500; }
.item-meta { font-size: 13px; color: #999; margin-top: 4px; }
.log-item { display: flex; align-items: center; gap: 8px; }
.log-operator { font-size: 12px; color: #999; }
.actions { display: flex; gap: 12px; justify-content: center; margin-top: 20px; }
</style>
