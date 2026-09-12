<template>
  <div class="dict-page">
    <div class="page-header">
      <h2>数据字典</h2>
      <el-button type="primary" @click="openTypeDialog()">新增类型</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span style="font-weight: 600">字典类型</span></template>
          <el-table :data="typeList" border v-loading="typeLoading" highlight-current-row
            @current-change="handleTypeSelect" height="500">
            <el-table-column prop="typeCode" label="类型编码" width="140" />
            <el-table-column prop="typeName" label="类型名称" min-width="120" />
            <el-table-column label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click.stop="openTypeDialog(row)">编辑</el-button>
                <el-popconfirm title="删除将同时删除该类型下所有字典项，确定？" @confirm="handleDeleteType(row.id)">
                  <template #reference>
                    <el-button text type="danger" size="small" @click.stop>删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: 600">字典项 {{ selectedType ? `- ${selectedType.typeName}` : '' }}</span>
              <el-button type="primary" size="small" @click="openItemDialog()"
                :disabled="!selectedType">新增字典项</el-button>
            </div>
          </template>
          <el-table :data="itemList" border v-loading="itemLoading" height="500">
            <el-table-column prop="itemValue" label="值" width="120" />
            <el-table-column prop="itemLabel" label="标签" min-width="120" />
            <el-table-column prop="sortOrder" label="排序" width="70" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-switch :model-value="row.status === 1" size="small"
                  @change="(val: boolean) => handleItemStatusChange(row, val)" />
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="openItemDialog(row)">编辑</el-button>
                <el-popconfirm title="确定删除？" @confirm="handleDeleteItem(row.id)">
                  <template #reference>
                    <el-button text type="danger" size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="typeDialogVisible" :title="typeForm.id ? '编辑类型' : '新增类型'" width="450px">
      <el-form :model="typeForm" label-width="80px">
        <el-form-item label="类型编码" required>
          <el-input v-model="typeForm.typeCode" placeholder="如 order_status" :disabled="!!typeForm.id" />
        </el-form-item>
        <el-form-item label="类型名称" required>
          <el-input v-model="typeForm.typeName" placeholder="如 订单状态" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="typeForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSubmitting" @click="handleSubmitType">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialogVisible" :title="itemForm.id ? '编辑字典项' : '新增字典项'" width="450px">
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="值" required>
          <el-input v-model="itemForm.itemValue" placeholder="如 1" />
        </el-form-item>
        <el-form-item label="标签" required>
          <el-input v-model="itemForm.itemLabel" placeholder="如 待支付" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="itemForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="itemForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="itemForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="itemSubmitting" @click="handleSubmitItem">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { dictApi } from '@ecommerce/shared'
import type { DictTypeDTO, DictItemDTO } from '@ecommerce/shared'

const typeLoading = ref(false)
const typeList = ref<DictTypeDTO[]>([])
const selectedType = ref<DictTypeDTO | null>(null)
const typeDialogVisible = ref(false)
const typeSubmitting = ref(false)

const itemLoading = ref(false)
const itemList = ref<DictItemDTO[]>([])
const itemDialogVisible = ref(false)
const itemSubmitting = ref(false)

const typeForm = reactive({
  id: undefined as number | undefined,
  typeCode: '', typeName: '', remark: '', status: 1,
})

const itemForm = reactive({
  id: undefined as number | undefined,
  typeCode: '', itemValue: '', itemLabel: '', sortOrder: 0, status: 1, remark: '',
})

const loadTypes = async () => {
  typeLoading.value = true
  try {
    const res = await dictApi.pageTypes({ pageNum: 1, pageSize: 200 })
    typeList.value = res.list
  } finally {
    typeLoading.value = false
  }
}

const loadItems = async (typeCode: string) => {
  itemLoading.value = true
  try {
    itemList.value = await dictApi.listItems(typeCode)
  } finally {
    itemLoading.value = false
  }
}

const handleTypeSelect = (row: DictTypeDTO | null) => {
  selectedType.value = row
  if (row) loadItems(row.typeCode)
  else itemList.value = []
}

const openTypeDialog = (row?: DictTypeDTO) => {
  if (row) {
    Object.assign(typeForm, { id: row.id, typeCode: row.typeCode, typeName: row.typeName, remark: row.remark, status: row.status })
  } else {
    Object.assign(typeForm, { id: undefined, typeCode: '', typeName: '', remark: '', status: 1 })
  }
  typeDialogVisible.value = true
}

const handleSubmitType = async () => {
  if (!typeForm.typeCode || !typeForm.typeName) {
    ElMessage.warning('请填写必要信息'); return
  }
  typeSubmitting.value = true
  try {
    if (typeForm.id) {
      await dictApi.updateType({ ...typeForm })
    } else {
      await dictApi.createType({ ...typeForm })
    }
    ElMessage.success(typeForm.id ? '更新成功' : '创建成功')
    typeDialogVisible.value = false
    loadTypes()
  } finally {
    typeSubmitting.value = false
  }
}

const handleDeleteType = async (id: number) => {
  await dictApi.deleteType(id)
  ElMessage.success('已删除')
  if (selectedType.value?.id === id) {
    selectedType.value = null
    itemList.value = []
  }
  loadTypes()
}

const openItemDialog = (row?: DictItemDTO) => {
  if (row) {
    Object.assign(itemForm, { id: row.id, typeCode: row.typeCode, itemValue: row.itemValue, itemLabel: row.itemLabel, sortOrder: row.sortOrder, status: row.status, remark: row.remark })
  } else {
    Object.assign(itemForm, { id: undefined, typeCode: selectedType.value?.typeCode || '', itemValue: '', itemLabel: '', sortOrder: 0, status: 1, remark: '' })
  }
  itemDialogVisible.value = true
}

const handleSubmitItem = async () => {
  if (!itemForm.itemValue || !itemForm.itemLabel) {
    ElMessage.warning('请填写必要信息'); return
  }
  itemSubmitting.value = true
  try {
    if (itemForm.id) {
      await dictApi.updateItem({ ...itemForm })
    } else {
      await dictApi.createItem({ ...itemForm })
    }
    ElMessage.success(itemForm.id ? '更新成功' : '创建成功')
    itemDialogVisible.value = false
    if (selectedType.value) loadItems(selectedType.value.typeCode)
  } finally {
    itemSubmitting.value = false
  }
}

const handleItemStatusChange = async (row: DictItemDTO, val: boolean) => {
  await dictApi.updateItemStatus(row.id, val ? 1 : 0)
  ElMessage.success('状态已更新')
  if (selectedType.value) loadItems(selectedType.value.typeCode)
}

const handleDeleteItem = async (id: number) => {
  await dictApi.deleteItem(id)
  ElMessage.success('已删除')
  if (selectedType.value) loadItems(selectedType.value.typeCode)
}

onMounted(loadTypes)
</script>

<style scoped>
.dict-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
</style>
