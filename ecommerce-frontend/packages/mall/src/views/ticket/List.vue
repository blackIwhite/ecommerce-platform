<template>
  <div class="ticket-list">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>服务工单</h2>
      <el-button type="primary" style="margin-left: auto" @click="$router.push('/ticket/create')">
        创建工单
      </el-button>
    </div>

    <div v-loading="loading" class="list-container">
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无工单">
          <el-button type="primary" @click="$router.push('/ticket/create')">创建工单</el-button>
        </el-empty>
      </div>

      <div v-for="item in list" :key="item.id" class="ticket-card" @click="$router.push(`/ticket/${item.id}`)">
        <div class="ticket-top">
          <span class="ticket-no">{{ item.ticketNo }}</span>
          <el-tag :type="statusTagType(item.status)" size="small">{{ item.statusName }}</el-tag>
        </div>
        <div class="ticket-subject">{{ item.subject }}</div>
        <div class="ticket-meta">
          <span>{{ item.typeName }}</span>
          <el-tag size="small" :type="priorityTagType(item.priority)">{{ item.priorityName }}</el-tag>
          <span class="time">{{ item.createTime }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ticketApi } from '@ecommerce/shared'
import type { ServiceTicketDTO } from '@ecommerce/shared'

const loading = ref(false)
const list = ref<ServiceTicketDTO[]>([])

const statusTagType = (status: number) => {
  if (status === 0) return 'warning'
  if (status === 3) return 'success'
  if (status === 4) return 'info'
  return 'primary'
}

const priorityTagType = (priority: number) => {
  if (priority === 3) return 'danger'
  if (priority === 2) return 'warning'
  return 'info'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await ticketApi.list({ pageNum: 1, pageSize: 50 })
    list.value = res.list
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.ticket-list { max-width: 800px; margin: 0 auto; padding: 20px; }
.page-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.list-container { min-height: 200px; }
.empty-state { padding: 60px 0; }
.ticket-card {
  background: #fff; border: 1px solid #eee; border-radius: 8px;
  padding: 16px; margin-bottom: 12px; cursor: pointer;
  transition: border-color 0.2s;
}
.ticket-card:hover { border-color: #409eff; }
.ticket-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.ticket-no { font-size: 13px; color: #999; }
.ticket-subject { font-size: 15px; font-weight: 500; margin-bottom: 8px; }
.ticket-meta { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #999; }
.time { margin-left: auto; }
</style>
