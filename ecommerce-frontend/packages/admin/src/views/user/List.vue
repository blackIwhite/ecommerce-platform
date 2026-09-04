<template>
  <div v-loading="loading">
    <h2>用户管理</h2>

    <div class="search-bar">
      <el-input v-model="search.keyword" placeholder="手机号/昵称" clearable style="width: 200px" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-select v-model="search.status" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="users" border>
      <el-table-column prop="userId" label="ID" width="80" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50]"
        @current-change="loadUsers"
        @size-change="handleSearch"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi, formatDate } from '@ecommerce/shared'
import type { UserDTO } from '@ecommerce/shared'

const loading = ref(true)
const users = ref<UserDTO[]>([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const search = reactive({
  keyword: '',
  status: undefined as number | undefined,
})

const loadUsers = async () => {
  loading.value = true
  try {
    const params: any = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (search.keyword) params.keyword = search.keyword
    if (search.status !== undefined) params.status = search.status
    const res = await userApi.page(params)
    users.value = res.list
    total.value = res.total
  } catch {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadUsers()
}

const handleToggleStatus = async (user: UserDTO) => {
  const action = user.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定${action}该用户？`, '提示')
    await userApi.updateStatus(user.userId, user.status === 1 ? 0 : 1)
    ElMessage.success(`已${action}`)
    loadUsers()
  } catch {}
}

onMounted(loadUsers)
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
