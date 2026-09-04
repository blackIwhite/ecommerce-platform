<template>
  <div v-loading="loading">
    <h2>订单管理</h2>

    <div class="search-bar">
      <el-input v-model="search.orderNo" placeholder="订单号" clearable style="width: 200px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-select v-model="search.status" placeholder="状态" clearable style="width: 140px" @change="handleSearch">
        <el-option label="待付款" :value="0" />
        <el-option label="已付款" :value="1" />
        <el-option label="待发货" :value="2" />
        <el-option label="已发货" :value="3" />
        <el-option label="已完成" :value="4" />
        <el-option label="已取消" :value="5" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="orders" border>
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column label="金额" width="120">
        <template #default="{ row }">{{ formatPrice(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="200">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/order/${row.orderId}`)">详情</el-button>
          <el-button v-if="row.status === 2" size="small" type="primary" @click="handleShip(row)">发货</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]"
        @current-change="loadOrders"
        @size-change="handleSearch"
      />
    </div>

    <el-dialog v-model="shipDialogVisible" title="订单发货" width="400px">
      <el-form label-width="80px">
        <el-form-item label="快递单号">
          <el-input v-model="trackingNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi, formatPrice, formatDate } from '@ecommerce/shared'
import type { OrderDTO } from '@ecommerce/shared'

const loading = ref(true)
const orders = ref<OrderDTO[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const search = reactive({
  orderNo: '',
  status: undefined as number | undefined,
})

const shipDialogVisible = ref(false)
const trackingNo = ref('')
const currentShipOrder = ref<OrderDTO | null>(null)

const statusMap: Record<number, string> = {
  0: '待付款', 1: '已付款', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消',
}

const statusText = (status: number) => statusMap[status] || '未知'

const statusTagType = (status: number): string => {
  const map: Record<number, string> = { 0: 'warning', 2: '', 3: '', 4: 'success', 5: 'info' }
  return map[status] || 'info'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (search.orderNo) params.orderNo = search.orderNo
    if (search.status !== undefined) params.status = search.status
    const res = await orderApi.adminList(params)
    orders.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadOrders()
}

const handleShip = (order: OrderDTO) => {
  currentShipOrder.value = order
  trackingNo.value = ''
  shipDialogVisible.value = true
}

const confirmShip = async () => {
  if (!trackingNo.value) {
    ElMessage.warning('请输入快递单号')
    return
  }
  try {
    await orderApi.adminShip(currentShipOrder.value!.orderId, trackingNo.value)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    loadOrders()
  } catch {
    ElMessage.error('发货失败')
  }
}

onMounted(loadOrders)
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
</style>
