<template>
  <div class="admin-user-page">
    <div class="page-header">
      <h2>管理员管理</h2>
      <el-button type="primary" @click="openCreateDialog">新增管理员</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="160" />
      <el-table-column label="角色" min-width="150">
        <template #default="{ row }">
          <el-tag v-for="role in row.roles" :key="role.id" size="small" style="margin-right: 4px">
            {{ role.roleName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openResetPwdDialog(row)">重置密码</el-button>
          <el-button text :type="row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pagination" v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize" :total="total" :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next" @change="loadData" />

    <el-dialog v-model="createDialogVisible" title="新增管理员" width="500px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="用户名" required>
          <el-input v-model="createForm.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="createForm.password" type="password" placeholder="登录密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="createForm.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="createForm.roleIds" multiple placeholder="选择角色" style="width: 100%">
            <el-option v-for="role in allRoles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetPwdDialogVisible" title="重置密码" width="400px">
      <el-form :model="resetPwdForm" label-width="80px">
        <el-form-item label="新密码" required>
          <el-input v-model="resetPwdForm.newPassword" type="password" placeholder="输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@ecommerce/shared'
import type { AdminUserDTO, RoleDTO } from '@ecommerce/shared'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<AdminUserDTO[]>([])
const total = ref(0)
const allRoles = ref<RoleDTO[]>([])
const createDialogVisible = ref(false)
const resetPwdDialogVisible = ref(false)

const query = reactive({ pageNum: 1, pageSize: 10 })
const createForm = reactive({
  username: '', password: '', realName: '', phone: '', email: '', roleIds: [] as number[],
})
const resetPwdForm = reactive({ adminId: 0, newPassword: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.listAdmins(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  allRoles.value = await adminApi.listRoles()
}

const openCreateDialog = () => {
  createForm.username = ''
  createForm.password = ''
  createForm.realName = ''
  createForm.phone = ''
  createForm.email = ''
  createForm.roleIds = []
  createDialogVisible.value = true
  loadRoles()
}

const handleCreate = async () => {
  if (!createForm.username || !createForm.password || !createForm.realName || createForm.roleIds.length === 0) {
    ElMessage.warning('请填写必要信息')
    return
  }
  submitting.value = true
  try {
    await adminApi.createAdmin({ ...createForm })
    ElMessage.success('创建成功')
    createDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row: AdminUserDTO) => {
  const newStatus = row.status === 1 ? 0 : 1
  await adminApi.updateAdminStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  loadData()
}

const openResetPwdDialog = (row: AdminUserDTO) => {
  resetPwdForm.adminId = row.id
  resetPwdForm.newPassword = ''
  resetPwdDialogVisible.value = true
}

const handleResetPassword = async () => {
  if (!resetPwdForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  await adminApi.resetPassword(resetPwdForm.adminId, resetPwdForm.newPassword)
  ElMessage.success('密码已重置')
  resetPwdDialogVisible.value = false
}

const handleDelete = async (id: number) => {
  await adminApi.deleteAdmin(id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.admin-user-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
