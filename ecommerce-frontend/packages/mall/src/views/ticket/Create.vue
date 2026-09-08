<template>
  <div class="ticket-create">
    <div class="page-header">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>创建工单</h2>
    </div>

    <el-card shadow="never">
      <el-form :model="form" label-width="100px">
        <el-form-item label="问题类型">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option label="账户问题" :value="1" />
            <el-option label="订单问题" :value="2" />
            <el-option label="商品问题" :value="3" />
            <el-option label="支付问题" :value="4" />
            <el-option label="物流问题" :value="5" />
            <el-option label="其他" :value="9" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio :value="0">低</el-radio>
            <el-radio :value="1">中</el-radio>
            <el-radio :value="2">高</el-radio>
            <el-radio :value="3">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.subject" placeholder="请简要描述您的问题" />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.content" type="textarea" :rows="4"
            placeholder="请详细描述您的问题，以便我们尽快为您解决" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交工单</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ticketApi } from '@ecommerce/shared'

const router = useRouter()
const submitting = ref(false)
const form = ref({
  type: 1,
  priority: 1,
  subject: '',
  content: '',
})

const handleSubmit = async () => {
  if (!form.value.subject.trim()) {
    ElMessage.warning('请输入工单标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入问题描述')
    return
  }
  submitting.value = true
  try {
    const id = await ticketApi.create({
      type: form.value.type,
      subject: form.value.subject.trim(),
      content: form.value.content.trim(),
      priority: form.value.priority,
    })
    ElMessage.success('工单已创建')
    router.push(`/ticket/${id}`)
  } catch (e: any) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.ticket-create { max-width: 600px; margin: 0 auto; padding: 20px; }
.page-header { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
</style>
