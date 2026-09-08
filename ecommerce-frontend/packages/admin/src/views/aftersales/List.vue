<template>
  <div class="aftersales-admin">
    <div class="page-toolbar">
      <el-select v-model="filters.type" placeholder="售后类型" clearable style="width: 140px"
        @change="loadList">
        <el-option label="退货退款" :value="1" />
        <el-option label="换货" :value="2" />
        <el-option label="仅退款" :value="3" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px"
        @change="loadList">
        <el-option label="待审核" :value="0" />
        <el-option label="已批准" :value="1" />
        <el-option label="退货中" :value="2" />
        <el-option label="已收货" :value="3" />
        <el-option label="已退款" :value="5" />
        <el-option label="已完成" :value="6" />
        <el-option label="已拒绝" :value="7" />
        <el-option label="已关闭" :value="8" />
      </el-select>
      <el-button @click="loadList">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="aftersalesNo" label="售后编号" width="180" />
      <el-table-column prop="orderId" label="订单ID" width="100" />
      <el-table-column prop="typeName" label="类型" width="100" />
      <el-table-column prop="reason" label="原因" min-width="140" />
      <el-table-column label="退款金额" width="120">
        <template #default="{ row }">
          <span style="color: #e4393c">¥{{ row.refundAmount?.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">查看</el-button>
          <el-button v-if="row.status === 0" type="success" size="small" @click="handleApprove(row)">
            通过
          </el-button>
          <el-button v-if="row.status === 0" type="danger" size="small" @click="handleReject(row)">
            拒绝
          </el-button>
          <el-button v-if="row.status === 2" type="primary" size="small" @click="handleReceive(row)">
            确认收货
          </el-button>
          <el-button v-if="row.status === 1 || row.status === 3" type="warning" size="small"
            @click="handleRefund(row)">
            退款
          </el-button>
          <el-button v-if="row.type === 2 && row.status === 3" type="success" size="small"
            @click="handleShipExchange(row)">
            换货发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > 0" :current-page="pageNum" :page-size="pageSize" :total="total"
      layout="total, prev, pager, next" style="margin-top: 16px; justify-content: flex-end"
      @current-change="handlePageChange" />

    <el-drawer v-model="drawerVisible" title="售后详情" size="500px">
      <template v-if="currentDetail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="售后编号">{{ currentDetail.aftersalesNo }}</el-descriptions-item>
          <el-descriptions-item label="订单ID">{{ currentDetail.orderId }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ currentDetail.typeName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentDetail.status)">{{ currentDetail.statusName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="原因">{{ currentDetail.reason }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ currentDetail.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">
            <span style="color: #e4393c">¥{{ currentDetail.refundAmount?.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="退货物流" v-if="currentDetail.returnTrackingNo">
            {{ currentDetail.returnCompany }} {{ currentDetail.returnTrackingNo }}
          </el-descriptions-item>
          <el-descriptions-item label="换货地址" v-if="currentDetail.exchangeAddress">
            {{ currentDetail.exchangeAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="换货物流" v-if="currentDetail.exchangeTrackingNo">
            {{ currentDetail.exchangeCompany }} {{ currentDetail.exchangeTrackingNo }}
          </el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentDetail.handler || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注">{{ currentDetail.handleRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentDetail.createTime }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px">商品明细</h4>
        <div v-for="item in currentDetail.items" :key="item.id" class="item-row">
          <img :src="item.image" class="item-img" />
          <div>
            <div class="item-name">{{ item.skuName }}</div>
            <div class="item-meta">¥{{ item.price }} × {{ item.quantity }}</div>
          </div>
        </div>

        <h4 style="margin: 16px 0 8px">处理记录</h4>
        <el-timeline>
          <el-timeline-item v-for="(log, i) in currentDetail.logs" :key="i"
            :timestamp="log.createTime">
            <el-tag size="small" :type="statusTagType(log.toStatus)">{{ log.toStatusName }}</el-tag>
            {{ log.remark }}
            <span style="color: #999; font-size: 12px">（{{ log.operator }}）</span>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>

    <el-dialog v-model="shipExchangeVisible" title="换货发货" width="450px">
      <el-form :model="shipExchangeForm" label-width="80px">
        <el-form-item label="物流公司">
          <el-input v-model="shipExchangeForm.company" placeholder="如：顺丰快递" />
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="shipExchangeForm.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipExchangeVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShipExchange">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aftersalesApi } from '@ecommerce/shared'
import type { AftersalesOrderDTO } from '@ecommerce/shared'

const loading = ref(false)
const list = ref<AftersalesOrderDTO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filters = ref<{ type?: number; status?: number }>({})
const drawerVisible = ref(false)
const currentDetail = ref<AftersalesOrderDTO | null>(null)

const statusTagType = (status: number) => {
  if (status === 0) return 'warning'
  if (status === 6) return 'success'
  if (status === 7 || status === 8) return 'danger'
  return 'primary'
}

const loadList = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filters.value.type != null) params.type = filters.value.type
    if (filters.value.status != null) params.status = filters.value.status
    const res = await aftersalesApi.adminList(params)
    list.value = res.list
    total.value = Number(res.total)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pageNum.value = page
  loadList()
}

const showDetail = async (row: AftersalesOrderDTO) => {
  currentDetail.value = await aftersalesApi.adminDetail(row.id)
  drawerVisible.value = true
}

const handleApprove = async (row: AftersalesOrderDTO) => {
  await ElMessageBox.confirm(`确认通过售后申请 ${row.aftersalesNo}？`, '审核通过')
  await aftersalesApi.adminApprove(row.id)
  ElMessage.success('已通过')
  loadList()
}

const handleReject = async (row: AftersalesOrderDTO) => {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
  await aftersalesApi.adminReject(row.id, value)
  ElMessage.success('已拒绝')
  loadList()
}

const handleReceive = async (row: AftersalesOrderDTO) => {
  await ElMessageBox.confirm('确认已收到退货？', '确认收货')
  await aftersalesApi.adminReceive(row.id)
  ElMessage.success('已确认收货')
  loadList()
}

const handleRefund = async (row: AftersalesOrderDTO) => {
  await ElMessageBox.confirm(`确认退款 ¥${row.refundAmount?.toFixed(2)} 到用户账户？`, '执行退款')
  await aftersalesApi.adminRefund(row.id)
  ElMessage.success('退款完成')
  loadList()
}

const shipExchangeVisible = ref(false)
const shipExchangeForm = ref({ company: '', trackingNo: '' })
const shipExchangeId = ref(0)

const handleShipExchange = (row: AftersalesOrderDTO) => {
  shipExchangeId.value = row.id
  shipExchangeForm.value = { company: '', trackingNo: '' }
  shipExchangeVisible.value = true
}

const confirmShipExchange = async () => {
  if (!shipExchangeForm.value.company || !shipExchangeForm.value.trackingNo) {
    ElMessage.warning('请填写完整物流信息')
    return
  }
  await aftersalesApi.adminShipExchange(
    shipExchangeId.value,
    shipExchangeForm.value.trackingNo,
    shipExchangeForm.value.company
  )
  ElMessage.success('换货已发出')
  shipExchangeVisible.value = false
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.aftersales-admin { padding: 20px; }
.page-toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }
.item-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.item-row:last-child { border-bottom: none; }
.item-img { width: 50px; height: 50px; object-fit: cover; border-radius: 4px; }
.item-name { font-size: 14px; font-weight: 500; }
.item-meta { font-size: 13px; color: #999; margin-top: 2px; }
</style>
