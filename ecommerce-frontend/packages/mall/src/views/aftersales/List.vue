<template>
  <div class="aftersales-page">
    <div class="page-header">
      <el-button text @click="$router.push('/user')">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>售后服务</h2>
    </div>

    <el-tabs v-model="activeStatus" @tab-change="loadList">
      <el-tab-pane label="全部" :name="-1" />
      <el-tab-pane label="待审核" :name="0" />
      <el-tab-pane label="处理中" :name="1" />
      <el-tab-pane label="已完成" :name="6" />
      <el-tab-pane label="已拒绝" :name="7" />
    </el-tabs>

    <div v-if="loading" v-loading="true" style="height: 200px" />
    <el-empty v-else-if="!list.length" description="暂无售后记录" />
    <div v-else class="aftersales-list">
      <div v-for="item in list" :key="item.id" class="aftersales-card"
        @click="$router.push(`/aftersales/${item.id}`)">
        <div class="card-header">
          <span class="no">{{ item.aftersalesNo }}</span>
          <el-tag :type="statusTagType(item.status)" size="small">{{ item.statusName }}</el-tag>
        </div>
        <div class="card-body">
          <div class="info-row">
            <span class="label">类型：</span>{{ item.typeName }}
          </div>
          <div class="info-row">
            <span class="label">原因：</span>{{ item.reason }}
          </div>
          <div class="info-row">
            <span class="label">退款金额：</span>
            <span class="amount">¥{{ item.refundAmount?.toFixed(2) }}</span>
          </div>
        </div>
        <div class="card-footer">
          <span class="time">{{ item.createTime }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { aftersalesApi } from '@ecommerce/shared'
import type { AftersalesOrderDTO } from '@ecommerce/shared'

const activeStatus = ref<number>(-1)
const loading = ref(false)
const list = ref<AftersalesOrderDTO[]>([])

const loadList = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: 1, pageSize: 50 }
    if (activeStatus.value >= 0) params.status = activeStatus.value
    const res = await aftersalesApi.list(params)
    list.value = res.list
  } finally {
    loading.value = false
  }
}

const statusTagType = (status: number) => {
  if (status === 0) return 'warning'
  if (status === 6) return 'success'
  if (status === 7 || status === 8) return 'danger'
  return 'primary'
}

onMounted(loadList)
</script>

<style scoped>
.aftersales-page { max-width: 800px; margin: 0 auto; padding: 20px; }
.page-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.aftersales-list { display: flex; flex-direction: column; gap: 12px; }
.aftersales-card {
  border: 1px solid #eee; border-radius: 8px; padding: 16px;
  cursor: pointer; transition: box-shadow 0.2s;
}
.aftersales-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.no { font-weight: 500; font-size: 14px; }
.info-row { font-size: 14px; color: #666; margin-bottom: 4px; }
.label { color: #999; }
.amount { color: #e4393c; font-weight: 500; }
.card-footer { display: flex; justify-content: flex-end; margin-top: 8px; }
.time { font-size: 12px; color: #999; }
</style>
