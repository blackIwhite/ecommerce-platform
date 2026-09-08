<template>
  <div class="ticket-detail">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>工单详情</h2>
    </div>

    <div v-if="loading" v-loading="true" style="height: 300px" />
    <template v-else-if="detail">
      <div class="info-card">
        <div class="info-top">
          <span class="ticket-no">{{ detail.ticketNo }}</span>
          <el-tag :type="statusTagType(detail.status)">{{ detail.statusName }}</el-tag>
        </div>
        <div class="subject">{{ detail.subject }}</div>
        <div class="meta-row">
          <span>{{ detail.typeName }}</span>
          <el-tag size="small" :type="priorityTagType(detail.priority)">{{ detail.priorityName }}</el-tag>
          <span v-if="detail.assignedTo">客服：{{ detail.assignedTo }}</span>
          <span class="time">{{ detail.createTime }}</span>
        </div>
      </div>

      <div class="messages-section">
        <div class="messages-header">对话记录</div>
        <div v-loading="messagesLoading" class="messages-list">
          <div v-for="msg in messages" :key="msg.id"
            :class="['message-item', msg.senderType === 1 ? 'from-user' : 'from-agent']">
            <div class="message-avatar">
              {{ msg.senderType === 1 ? '我' : (msg.senderType === 2 ? '客服' : '系统') }}
            </div>
            <div class="message-body">
              <div class="message-sender">
                {{ msg.senderName }}
                <span class="message-time">{{ msg.createTime }}</span>
              </div>
              <div class="message-content">{{ msg.content }}</div>
            </div>
          </div>
          <div v-if="messages.length === 0 && !messagesLoading" class="no-messages">
            暂无消息
          </div>
        </div>

        <div v-if="detail.status !== 4" class="message-input">
          <el-input v-model="newMessage" type="textarea" :rows="2" placeholder="请输入消息..." />
          <el-button type="primary" @click="handleSend" :loading="sending"
            :disabled="!newMessage.trim()">
            发送
          </el-button>
        </div>
        <div v-else class="closed-hint">工单已关闭</div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ticketApi } from '@ecommerce/shared'
import type { ServiceTicketDTO, TicketMessageDTO } from '@ecommerce/shared'

const route = useRoute()
const loading = ref(false)
const messagesLoading = ref(false)
const sending = ref(false)
const detail = ref<ServiceTicketDTO | null>(null)
const messages = ref<TicketMessageDTO[]>([])
const newMessage = ref('')

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

const loadDetail = async () => {
  loading.value = true
  try {
    detail.value = await ticketApi.getDetail(Number(route.params.id))
  } finally {
    loading.value = false
  }
}

const loadMessages = async () => {
  messagesLoading.value = true
  try {
    messages.value = await ticketApi.getMessages(Number(route.params.id))
  } finally {
    messagesLoading.value = false
  }
}

const handleSend = async () => {
  if (!newMessage.value.trim()) return
  sending.value = true
  try {
    await ticketApi.sendMessage(Number(route.params.id), newMessage.value.trim())
    newMessage.value = ''
    await loadMessages()
  } catch (e: any) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  loadDetail()
  loadMessages()
})
</script>

<style scoped>
.ticket-detail { max-width: 800px; margin: 0 auto; padding: 20px; }
.page-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.info-card {
  background: #fff; border: 1px solid #eee; border-radius: 8px;
  padding: 16px; margin-bottom: 16px;
}
.info-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.ticket-no { font-size: 13px; color: #999; }
.subject { font-size: 16px; font-weight: 500; margin-bottom: 8px; }
.meta-row { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #999; }
.time { margin-left: auto; }
.messages-section {
  background: #fff; border: 1px solid #eee; border-radius: 8px; overflow: hidden;
}
.messages-header {
  padding: 12px 16px; font-weight: 500; border-bottom: 1px solid #eee;
}
.messages-list { padding: 16px; min-height: 200px; max-height: 400px; overflow-y: auto; }
.no-messages { text-align: center; color: #999; padding: 40px 0; }
.message-item { display: flex; gap: 12px; margin-bottom: 16px; }
.message-item.from-agent { flex-direction: row-reverse; }
.message-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; flex-shrink: 0;
  background: #409eff; color: #fff;
}
.from-agent .message-avatar { background: #67c23a; }
.from-agent .message-body { text-align: right; }
.message-body { flex: 1; }
.message-sender { font-size: 12px; color: #999; margin-bottom: 4px; }
.message-time { margin-left: 8px; }
.message-content {
  display: inline-block; padding: 8px 12px; border-radius: 8px;
  background: #f0f2f5; font-size: 14px; text-align: left;
  max-width: 80%; word-break: break-word;
}
.from-agent .message-content { background: #e1f3d8; }
.message-input {
  display: flex; gap: 12px; padding: 16px; border-top: 1px solid #eee;
  align-items: flex-end;
}
.message-input .el-input { flex: 1; }
.closed-hint { padding: 16px; text-align: center; color: #999; border-top: 1px solid #eee; }
</style>
