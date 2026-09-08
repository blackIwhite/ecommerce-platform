<template>
  <div class="report-dashboard">
    <div class="page-header">
      <h2>数据报表</h2>
    </div>

    <el-form :inline="true" class="filter-form">
      <el-form-item label="日期范围">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="~"
          start-placeholder="开始日期" end-placeholder="结束日期"
          value-format="YYYY-MM-DD" @change="loadAll" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadAll">查询</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="16" class="stats-row" v-if="stats">
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.orderCount }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">¥{{ stats.totalAmount?.toFixed(2) }}</div>
            <div class="stat-label">总金额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">¥{{ stats.avgAmount?.toFixed(2) }}</div>
            <div class="stat-label">平均金额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.paidCount }}</div>
            <div class="stat-label">已支付</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.refundedCount }}</div>
            <div class="stat-label">已退款</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stats.cancelCount }}</div>
            <div class="stat-label">已取消</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
              <el-radio-group v-model="groupBy" size="small" @change="loadSales">
                <el-radio-button value="day">日</el-radio-button>
                <el-radio-button value="week">周</el-radio-button>
                <el-radio-button value="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <el-table :data="salesData" border size="small" v-loading="salesLoading" max-height="300">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="orderCount" label="订单数" width="100" />
            <el-table-column label="销售额">
              <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>
            <span>热销商品 TOP 10</span>
          </template>
          <el-table :data="productData" border size="small" v-loading="productLoading" max-height="300">
            <el-table-column type="index" label="#" width="40" />
            <el-table-column prop="productName" label="商品" min-width="150" show-overflow-tooltip />
            <el-table-column prop="salesCount" label="销量" width="80" />
            <el-table-column label="金额" width="100">
              <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据导出</span>
            </div>
          </template>
          <el-space>
            <el-button type="primary" @click="exportOrders">导出订单数据 (Excel)</el-button>
            <el-button type="success" @click="exportProducts">导出商品销售 (Excel)</el-button>
          </el-space>
          <div style="margin-top: 8px; color: #999; font-size: 12px">
            导出当前选择日期范围内的数据
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { reportApi } from '@ecommerce/shared'
import type { OrderStatsDTO, SalesReportDTO, ProductSalesDTO } from '@ecommerce/shared'

const stats = ref<OrderStatsDTO | null>(null)
const salesData = ref<SalesReportDTO[]>([])
const productData = ref<ProductSalesDTO[]>([])
const salesLoading = ref(false)
const productLoading = ref(false)
const groupBy = ref('day')

const today = new Date()
const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
const formatDate = (d: Date) => d.toISOString().split('T')[0]
const dateRange = ref<string[]>([formatDate(weekAgo), formatDate(today)])

const getParams = () => ({
  startDate: dateRange.value?.[0] || '',
  endDate: dateRange.value?.[1] || '',
})

const loadStats = async () => {
  stats.value = await reportApi.getOrderStats(getParams())
}

const loadSales = async () => {
  salesLoading.value = true
  try {
    salesData.value = await reportApi.getSalesReport({ ...getParams(), groupBy: groupBy.value })
  } finally {
    salesLoading.value = false
  }
}

const loadProducts = async () => {
  productLoading.value = true
  try {
    productData.value = await reportApi.getProductSalesReport(getParams())
  } finally {
    productLoading.value = false
  }
}

const loadAll = () => {
  loadStats()
  loadSales()
  loadProducts()
}

const exportOrders = async () => {
  await reportApi.exportOrders(getParams())
}

const exportProducts = async () => {
  await reportApi.exportProducts(getParams())
}

onMounted(loadAll)
</script>

<style scoped>
.report-dashboard { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-form { margin-bottom: 16px; }
.stats-row { margin-bottom: 8px; }
.stat-item { text-align: center; }
.stat-value { font-size: 24px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 12px; color: #999; margin-top: 4px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
