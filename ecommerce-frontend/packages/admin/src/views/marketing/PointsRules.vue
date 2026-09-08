<template>
  <div class="points-rules-page">
    <div class="page-header">
      <h2>积分规则管理</h2>
    </div>

    <el-table :data="rules" v-loading="loading" style="width: 100%">
      <el-table-column prop="ruleKey" label="规则标识" width="160" />
      <el-table-column prop="description" label="说明" width="200" />
      <el-table-column label="规则值" min-width="200">
        <template #default="{ row }">
          <el-input
            v-if="editingId === row.id"
            v-model="editForm.ruleValue"
            size="small"
            placeholder="JSON格式"
          />
          <code v-else>{{ row.ruleValue }}</code>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <template v-if="editingId === row.id">
            <el-button link type="primary" @click="handleSave(row.id)">保存</el-button>
            <el-button link @click="editingId = 0">取消</el-button>
          </template>
          <template v-else>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pointsApi } from '@ecommerce/shared'
import type { PointsRuleDTO } from '@ecommerce/shared'

const loading = ref(true)
const rules = ref<PointsRuleDTO[]>([])
const editingId = ref(0)
const editForm = ref({ ruleValue: '' })

async function loadRules() {
  loading.value = true
  try {
    rules.value = await pointsApi.listRules()
  } finally {
    loading.value = false
  }
}

function handleEdit(row: PointsRuleDTO) {
  editingId.value = row.id
  editForm.value.ruleValue = row.ruleValue
}

async function handleSave(id: number) {
  try {
    await pointsApi.updateRule(id, { ruleValue: editForm.value.ruleValue })
    ElMessage.success('保存成功')
    editingId.value = 0
    await loadRules()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function handleStatusChange(row: PointsRuleDTO) {
  try {
    await pointsApi.updateRule(row.id, { status: row.status })
    ElMessage.success('状态已更新')
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
    await loadRules()
  }
}

onMounted(loadRules)
</script>

<style scoped>
.points-rules-page { padding: 20px; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
code { background: #f5f5f5; padding: 2px 6px; border-radius: 3px; font-size: 13px; }
</style>
