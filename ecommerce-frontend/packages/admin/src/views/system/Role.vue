<template>
  <div class="role-page">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="openDialog()">新增角色</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="roleName" label="角色名称" width="120" />
      <el-table-column prop="roleKey" label="角色标识" width="120" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.roleName" placeholder="如：运营管理员" />
        </el-form-item>
        <el-form-item label="角色标识" required>
          <el-input v-model="form.roleKey" placeholder="如：operator" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="权限">
          <div class="permission-tree">
            <div v-for="group in permissionGroups" :key="group.module" class="permission-group">
              <div class="group-title">{{ group.module }}</div>
              <el-checkbox-group v-model="form.permissionIds">
                <el-checkbox v-for="p in group.items" :key="p.id" :value="p.id" :label="p.name" />
              </el-checkbox-group>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@ecommerce/shared'
import type { RoleDTO, PermissionDTO } from '@ecommerce/shared'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<RoleDTO[]>([])
const dialogVisible = ref(false)
const allPermissions = ref<PermissionDTO[]>([])

const defaultForm = (): RoleDTO => ({
  id: 0, roleName: '', roleKey: '', description: '', status: 1, sort: 0, permissionIds: [],
})
const form = ref<RoleDTO>(defaultForm())

const permissionGroups = computed(() => {
  const map = new Map<string, PermissionDTO[]>()
  allPermissions.value.forEach(p => {
    if (!map.has(p.module)) map.set(p.module, [])
    map.get(p.module)!.push(p)
  })
  return Array.from(map.entries()).map(([module, items]) => ({ module, items }))
})

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await adminApi.listRoles()
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  allPermissions.value = await adminApi.listPermissions()
}

const openDialog = (row?: RoleDTO) => {
  form.value = row ? { ...row, permissionIds: [...(row.permissionIds || [])] } : defaultForm()
  dialogVisible.value = true
  loadPermissions()
}

const handleSubmit = async () => {
  if (!form.value.roleName || !form.value.roleKey) {
    ElMessage.warning('请填写必要信息')
    return
  }
  submitting.value = true
  try {
    if (form.value.id) {
      await adminApi.updateRole(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await adminApi.createRole(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  await adminApi.deleteRole(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.role-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.permission-tree { max-height: 300px; overflow-y: auto; width: 100%; }
.permission-group { margin-bottom: 12px; }
.group-title { font-weight: bold; margin-bottom: 4px; color: #606266; }
</style>
