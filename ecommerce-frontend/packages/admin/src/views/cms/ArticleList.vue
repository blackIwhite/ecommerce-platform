<template>
  <div class="article-list">
    <div class="page-header">
      <h2>内容管理</h2>
      <div>
        <el-button @click="categoryDialogVisible = true">分类管理</el-button>
        <el-button type="primary" @click="openDialog()">新建文章</el-button>
      </div>
    </div>

    <el-form :inline="true" class="filter-form">
      <el-form-item label="分类">
        <el-select v-model="query.categoryId" clearable placeholder="全部" @change="loadData">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" @change="loadData">
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已归档" :value="2" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          {{ getCategoryName(row.categoryId) }}
        </template>
      </el-table-column>
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'warning'">
            {{ statusMap[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column label="发布时间" width="160">
        <template #default="{ row }">{{ row.publishTime?.substring(0, 16) || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button v-if="row.status === 0" text type="success" @click="publish(row)">发布</el-button>
          <el-button v-if="row.status === 1" text type="warning" @click="archive(row)">归档</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑文章' : '新建文章'" width="700px" top="5vh">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="文章标题" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="作者名" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="文章正文（支持HTML）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button @click="handleSubmit(0)">存为草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit(1)">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" title="分类管理" width="500px">
      <el-table :data="categories" border size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="code" label="编码" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openCategoryDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="deleteCategory(row.id)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 12px">
        <el-button type="primary" size="small" @click="openCategoryDialog()">新建分类</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="catFormVisible" :title="catForm.id ? '编辑分类' : '新建分类'" width="400px">
      <el-form :model="catForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="catForm.name" />
        </el-form-item>
        <el-form-item label="编码" required>
          <el-input v-model="catForm.code" placeholder="英文编码" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="catForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCategory">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { articleApi } from '@ecommerce/shared'
import type { ArticleDTO, ArticleCategoryDTO } from '@ecommerce/shared'

const statusMap: Record<number, string> = { 0: '草稿', 1: '已发布', 2: '已归档' }

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ArticleDTO[]>([])
const total = ref(0)
const categories = ref<ArticleCategoryDTO[]>([])
const dialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const catFormVisible = ref(false)

const query = reactive({ pageNum: 1, pageSize: 10, categoryId: undefined as number | undefined, status: undefined as number | undefined })

const defaultForm = (): ArticleDTO => ({
  id: 0, title: '', slug: '', content: '', summary: '', coverImage: '',
  categoryId: 0, author: '', status: 0, sortOrder: 0, viewCount: 0, publishTime: '', createTime: '',
})
const form = ref<ArticleDTO>(defaultForm())

const defaultCatForm = (): ArticleCategoryDTO => ({ id: 0, name: '', code: '', parentId: 0, sortOrder: 0, status: 1 })
const catForm = ref<ArticleCategoryDTO>(defaultCatForm())

const getCategoryName = (id: number) => categories.value.find(c => c.id === id)?.name || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res = await articleApi.listArticles(query)
    tableData.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  categories.value = await articleApi.listCategories()
}

const openDialog = (row?: ArticleDTO) => {
  form.value = row ? { ...row } : defaultForm()
  dialogVisible.value = true
}

const handleSubmit = async (status: number) => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  submitting.value = true
  try {
    form.value.status = status
    if (form.value.id) {
      await articleApi.updateArticle(form.value)
      ElMessage.success('更新成功')
    } else {
      await articleApi.createArticle(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const publish = async (row: ArticleDTO) => {
  await articleApi.updateArticle({ ...row, status: 1 })
  ElMessage.success('已发布')
  loadData()
}

const archive = async (row: ArticleDTO) => {
  await articleApi.updateArticle({ ...row, status: 2 })
  ElMessage.success('已归档')
  loadData()
}

const handleDelete = async (id: number) => {
  await articleApi.deleteArticle(id)
  ElMessage.success('已删除')
  loadData()
}

const openCategoryDialog = (row?: ArticleCategoryDTO) => {
  catForm.value = row ? { ...row } : defaultCatForm()
  catFormVisible.value = true
}

const submitCategory = async () => {
  if (!catForm.value.name || !catForm.value.code) {
    ElMessage.warning('请填写名称和编码')
    return
  }
  if (catForm.value.id) {
    await articleApi.updateCategory(catForm.value)
  } else {
    await articleApi.createCategory(catForm.value)
  }
  ElMessage.success('操作成功')
  catFormVisible.value = false
  loadCategories()
}

const deleteCategory = async (id: number) => {
  await articleApi.deleteCategory(id)
  ElMessage.success('已删除')
  loadCategories()
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.article-list { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.filter-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
