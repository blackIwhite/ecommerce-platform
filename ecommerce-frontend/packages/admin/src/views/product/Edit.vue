<template>
  <div v-loading="pageLoading">
    <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px;">
      <el-button @click="$router.back()">返回</el-button>
      <h2>{{ isEdit ? '编辑商品' : '新增商品' }}</h2>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 800px;">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入商品名称" />
      </el-form-item>

      <el-form-item label="商品分类" prop="categoryId">
        <el-cascader
          v-model="categoryPath"
          :options="categories"
          :props="{ value: 'id', label: 'name', children: 'children', emitPath: true }"
          placeholder="请选择分类"
          clearable
          @change="onCategoryChange"
        />
      </el-form-item>

      <el-form-item label="品牌" prop="brandId">
        <el-select v-model="form.brandId" placeholder="请选择品牌" clearable filterable>
          <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="商品图片" prop="images">
        <el-input v-model="form.images" placeholder="图片URL，多张用逗号分隔" />
      </el-form-item>

      <el-form-item label="商品描述">
        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品描述" />
      </el-form-item>

      <el-divider>SKU 规格</el-divider>

      <el-table :data="form.skuList" border style="margin-bottom: 12px;">
        <el-table-column label="SKU名称" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.skuName" placeholder="如：红色/128GB" />
          </template>
        </el-table-column>
        <el-table-column label="价格" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.price" :min="0" :precision="2" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="库存" width="140">
          <template #default="{ row }">
            <el-input-number v-model="row.stock" :min="0" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="规格" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.specs" placeholder="如：颜色:红色" />
          </template>
        </el-table-column>
        <el-table-column label="图片" width="180">
          <template #default="{ row }">
            <el-input v-model="row.image" placeholder="图片URL" />
          </template>
        </el-table-column>
        <el-table-column label="" width="60">
          <template #default="{ $index }">
            <el-button type="danger" link @click="removeSku($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-button type="primary" link @click="addSku" style="margin-bottom: 20px;">+ 添加 SKU</el-button>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { productApi } from '@ecommerce/shared'
import type { CategoryDTO, BrandDTO } from '@ecommerce/shared'

interface SkuForm {
  skuName: string
  price: number
  stock: number
  image: string
  specs: string
}

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const pageLoading = ref(false)
const saving = ref(false)

const categories = ref<CategoryDTO[]>([])
const brands = ref<BrandDTO[]>([])
const categoryPath = ref<number[]>([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  categoryId: undefined as number | undefined,
  brandId: undefined as number | undefined,
  description: '',
  images: '',
  skuList: [{ skuName: '', price: 0, stock: 0, image: '', specs: '' }] as SkuForm[],
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  brandId: [{ required: true, message: '请选择品牌', trigger: 'change' }],
}

const onCategoryChange = (path: number[]) => {
  form.categoryId = path[path.length - 1]
}

const addSku = () => {
  form.skuList.push({ skuName: '', price: 0, stock: 0, image: '', specs: '' })
}

const removeSku = (index: number) => {
  if (form.skuList.length <= 1) {
    ElMessage.warning('至少保留一个 SKU')
    return
  }
  form.skuList.splice(index, 1)
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!form.skuList.some((s) => s.skuName)) {
    ElMessage.warning('请至少填写一个 SKU 名称')
    return
  }

  saving.value = true
  try {
    const payload = {
      ...(isEdit.value ? { id: form.id } : {}),
      name: form.name,
      categoryId: form.categoryId,
      brandId: form.brandId,
      description: form.description,
      images: form.images,
      skuList: form.skuList.filter((s) => s.skuName),
    }

    if (isEdit.value) {
      await productApi.updateSpu(payload)
      ElMessage.success('更新成功')
    } else {
      await productApi.createSpu(payload)
      ElMessage.success('创建成功')
    }
    router.push('/product')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  pageLoading.value = true
  try {
    const [catTree, brandList] = await Promise.all([
      productApi.getCategoryTree(),
      productApi.getBrandList(),
    ])
    categories.value = catTree
    brands.value = brandList

    if (route.params.id) {
      const spu = await productApi.getSpuDetail(Number(route.params.id))
      form.id = spu.spuId
      form.name = spu.name
      form.categoryId = spu.categoryId
      form.brandId = spu.brandId
      form.description = spu.description
      form.images = spu.images
      if (spu.skuList?.length) {
        form.skuList = spu.skuList.map((s) => ({
          skuName: s.skuName,
          price: s.price,
          stock: s.stock,
          image: s.image,
          specs: s.specs,
        }))
      }
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    pageLoading.value = false
  }
})
</script>
