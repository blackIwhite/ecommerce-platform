<template>
  <div class="message-center">
    <div class="page-header">
      <h2>消息中心</h2>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0">
        <el-button @click="markAllRead" :disabled="unreadCount === 0">全部已读</el-button>
      </el-badge>
    </div>

    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane name="unread">
        <template #label>
          未读 <el-badge v-if="unreadCount" :value="unreadCount" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="系统通知" name="1" />
      <el-tab-pane label="订单消息" name="2" />
      <el-tab-pane label="促销消息" name="3" />
      <el-tab-pane label="售后消息" name="4" />
    </el-tabs>

    <div v-loading="loading" class="message-list">
      <div v-if="messages.length === 0" class="empty-state">
        <el-empty description="暂无消息" />
      </div>
      <div v-for="msg in messages" :key="msg.id"
        class="message-item" :class="{ unread: msg.isRead === 0 }"
        @click="openMessage(msg)">
        <div class="msg-header">
          <el-tag size="small" :type="typeTagMap[msg.type]">{{ typeNameMap[msg.type] }}</el-tag>
          <span class="msg-title">{{ msg.title }}</span>
          <span class="msg-time">{{ msg.createTime?.substring(0, 16) }}</span>
          <el-badge v-if="msg.isRead === 0" is-dot class="unread-dot" />
        </div>
        <div class="msg-content">{{ msg.content }}</div>
      </div>
    </div>

    <el-pagination v-if="total > 0" class="pagination" v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize" :total="total" layout="prev, pager, next" @change="loadData" />

    <el-dialog v-model="detailVisible" :title="currentMsg?.title" width="500px">
      <div style="color: #666; font-size: 13px; margin-bottom: 12px">
        {{ currentMsg?.createTime?.substring(0, 16) }}
      </div>
      <div style="line-height: 1.8">{{ currentMsg?.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { messageApi } from '@ecommerce/shared'
import type { UserMessageDTO } from '@ecommerce/shared'

const typeNameMap: Record<number, string> = { 1: '系统', 2: '订单', 3: '促销', 4: '售后' }
const typeTagMap: Record<number, string> = { 1: '', 2: 'warning', 3: 'success', 4: 'danger' }

const loading = ref(false)
const messages = ref<UserMessageDTO[]>([])
const total = ref(0)
const unreadCount = ref(0)
const activeTab = ref('all')
const detailVisible = ref(false)
const currentMsg = ref<UserMessageDTO | null>(null)

const query = reactive({ pageNum: 1, pageSize: 20, type: undefined as number | undefined, isRead: undefined as number | undefined })

const loadData = async () => {
  if (activeTab.value === 'all') {
    query.type = undefined
    query.isRead = undefined
  } else if (activeTab.value === 'unread') {
    query.type = undefined
    query.isRead = 0
  } else {
    query.type = Number(activeTab.value)
    query.isRead = undefined
  }
  loading.value = true
  try {
    const res = await messageApi.listMessages(query)
    messages.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const loadUnread = async () => {
  const res = await messageApi.getUnreadCount()
  unreadCount.value = res.total
}

const openMessage = async (msg: UserMessageDTO) => {
  currentMsg.value = msg
  detailVisible.value = true
  if (msg.isRead === 0) {
    await messageApi.markAsRead(msg.id)
    msg.isRead = 1
    loadUnread()
  }
}

const markAllRead = async () => {
  await messageApi.markAllAsRead()
  ElMessage.success('已全部标记为已读')
  loadUnread()
  loadData()
}

onMounted(() => {
  loadUnread()
  loadData()
})
</script>

<style scoped>
.message-center { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.tab-badge { margin-left: 4px; }
.message-list { min-height: 200px; }
.message-item {
  padding: 12px 16px;
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.message-item:hover { border-color: #409eff; background: #f5f9ff; }
.message-item.unread { border-left: 3px solid #409eff; background: #fafcff; }
.msg-header { display: flex; align-items: center; gap: 8px; }
.msg-title { font-weight: 500; flex: 1; }
.msg-time { color: #999; font-size: 12px; }
.unread-dot { margin-left: 4px; }
.msg-content { color: #666; font-size: 13px; margin-top: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty-state { padding: 40px 0; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
