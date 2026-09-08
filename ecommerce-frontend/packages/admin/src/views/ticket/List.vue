<template>
  <div class="ticket-admin">
    <div class="page-toolbar">
      <el-select v-model="filters.type" placeholder="问题类型" clearable style="width: 140px"
        @change="loadList">
        <el-option label="账户问题" :value="1" />
        <el-option label="订单问题" :value="2" />
        <el-option label="商品问题" :value="3" />
        <el-option label="支付问题" :value="4" />
        <el-option label="物流问题" :value="5" />
        <el-option label="其他" :value="9" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px"
        @change="loadList">
        <el-option label="待处理" :value="0" />
        <el-option label="已分配" :value="1" />
        <el-option label="处理中" :value="2" />
        <el-option label="已解决" :value="3" />
        <el-option label="已关闭" :value="4" />
      </el-select>
      <el-select v-model="filters.assignedTo" placeholder="客服" clearable style="width: 140px"
        @change="loadList">
        <el-option label="客服A" value="客服A" />
        <el-option label="客服B" value="客服B" />
        <el-option label="admin" value="admin" />
      </el-select>
      <el-button @click="loadList">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="ticketNo" label="工单编号" width="180" />
      <el-table-column prop="subject" label="标题" min-width="180" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ row.typeName }}</template>
      </el-table-column>
      <el-table-column label="优先级" width="80">
        <template #default="{ row }">
          <el-tag :type="priorityTagType(row.priority)" size="small">{{ row.priorityName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="assignedTo" label="客服" width="100">
        <template #default="{ row }">{{ row.assignedTo || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">查看</el-button>
          <el-button v-if="row.status === 0" type="primary" size="small" @click="handleAssign(row)">
            分配
          </el-button>
          <el-button v-if="row.status >= 1 && row.status <= 2" type="success" size="small"
            @click="handleReply(row)">
            回复
          </el-button>
          <el-button v-if="row.status >= 1 && row.status <= 2" type="warning" size="small"
            @click="handleResolve(row)">
            解决
          </el-button>
          <el-button v-if="row.status < 4" type="info" size="small" @click="handleClose(row)">
            关闭
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > 0" :current-page="pageNum" :page-size="pageSize" :total="total"
      layout="total, prev, pager, next" style="margin-top: 16px; justify-content: flex-end"
      @current-change="handlePageChange" />

    <el-drawer v-model="drawerVisible" title="工单详情" size="550px">
      <template v-if="currentDetail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单编号">{{ currentDetail.ticketNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ currentDetail.subject }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ currentDetail.typeName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="priorityTagType(currentDetail.priority)" size="small">
              {{ currentDetail.priorityName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentDetail.status)">{{ currentDetail.statusName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客服">{{ currentDetail.assignedTo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ currentDetail.content }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentDetail.createTime }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px">对话记录</h4>
        <div v-if="currentDetail.messages?.length" class="drawer-messages">
          <div v-for="msg in currentDetail.messages" :key="msg.id" class="drawer-msg-item">
            <div class="drawer-msg-sender">
              <span>{{ msg.senderName }}</span>
              <span class="drawer-msg-time">{{ msg.createTime }}</span>
            </div>
            <div class="drawer-msg-content">{{ msg.content }}</div>
          </div>
        </div>
        <div v-else style="color: #999; font-size: 14px;">暂无消息</div>
      </template>
    </el-drawer>

    <el-dialog v-model="assignDialogVisible" title="分配客服" width="400px">
      <el-form label-width="80px">
        <el-form-item label="客服">
          <el-select v-model="assignAgent" placeholder="请选择客服" style="width: 100%">
            <el-option label="客服A" value="客服A" />
            <el-option label="客服B" value="客服B" />
            <el-option label="admin" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="replyDialogVisible" title="回复工单" width="500px">
      <el-form label-width="80px">
        <el-form-item label="客服">
          <el-select v-model="replyAgent" placeholder="请选择" style="width: 100%">
            <el-option label="客服A" value="客服A" />
            <el-option label="客服B" value="客服B" />
            <el-option label="admin" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input v-model="replyContent" type="textarea" :rows="3" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReply">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ticketApi } from '@ecommerce/shared'
import type { ServiceTicketDTO } from '@ecommerce/shared'

const loading = ref(false)
const list = ref<ServiceTicketDTO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filters = ref<{ type?: number; status?: number; assignedTo?: string }>({})
const drawerVisible = ref(false)
const currentDetail = ref<ServiceTicketDTO | null>(null)

const assignDialogVisible = ref(false)
const assignAgent = ref('')
const assignTicketId = ref(0)

const replyDialogVisible = ref(false)
const replyAgent = ref('admin')
const replyContent = ref('')
const replyTicketId = ref(0)

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
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filters.value.type != null) params.type = filters.value.type
    if (filters.value.status != null) params.status = filters.value.status
    if (filters.value.assignedTo) params.assignedTo = filters.value.assignedTo
    const res = await ticketApi.adminList(params)
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

const showDetail = async (row: ServiceTicketDTO) => {
  currentDetail.value = await ticketApi.adminDetail(row.id)
  drawerVisible.value = true
}

const handleAssign = (row: ServiceTicketDTO) => {
  assignTicketId.value = row.id
  assignAgent.value = ''
  assignDialogVisible.value = true
}

const confirmAssign = async () => {
  if (!assignAgent.value) {
    ElMessage.warning('请选择客服')
    return
  }
  await ticketApi.adminAssign(assignTicketId.value, assignAgent.value)
  ElMessage.success('已分配')
  assignDialogVisible.value = false
  loadList()
}

const handleReply = (row: ServiceTicketDTO) => {
  replyTicketId.value = row.id
  replyAgent.value = 'admin'
  replyContent.value = ''
  replyDialogVisible.value = true
}

const confirmReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  await ticketApi.adminReply(replyTicketId.value, replyAgent.value, replyContent.value.trim())
  ElMessage.success('已回复')
  replyDialogVisible.value = false
  loadList()
}

const handleResolve = async (row: ServiceTicketDTO) => {
  await ElMessageBox.confirm('确认该工单已解决？', '解决工单')
  await ticketApi.adminResolve(row.id)
  ElMessage.success('已标记为已解决')
  loadList()
}

const handleClose = async (row: ServiceTicketDTO) => {
  await ElMessageBox.confirm('确认关闭该工单？', '关闭工单')
  await ticketApi.adminClose(row.id)
  ElMessage.success('已关闭')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.ticket-admin { padding: 20px; }
.page-toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }
.drawer-messages { max-height: 300px; overflow-y: auto; }
.drawer-msg-item { padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.drawer-msg-item:last-child { border-bottom: none; }
.drawer-msg-sender { font-size: 13px; color: #666; margin-bottom: 4px; display: flex; justify-content: space-between; }
.drawer-msg-time { color: #999; font-size: 12px; }
.drawer-msg-content { font-size: 14px; color: #333; }
</style>
