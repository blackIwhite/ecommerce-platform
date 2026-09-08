<template>
  <div class="home">
    <el-header>
      <div class="header-content">
        <h1 class="logo">电商商城</h1>
        <el-menu mode="horizontal" :ellipsis="false">
          <el-menu-item index="1" @click="$router.push('/')">首页</el-menu-item>
          <el-menu-item index="2" @click="$router.push('/product')">商品</el-menu-item>
          <el-menu-item index="3" @click="$router.push('/cart')">购物车</el-menu-item>
          <el-menu-item index="4" @click="$router.push('/order')">我的订单</el-menu-item>
        </el-menu>
        <div class="user-area">
          <template v-if="isLoggedIn">
            <el-dropdown @command="handleUserCommand">
              <span class="user-nickname">{{ nickname }} <el-icon><ArrowDown /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="user">用户中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main>
      <div class="home-content">
        <el-carousel height="280px" class="banner-carousel">
          <el-carousel-item v-for="(banner, idx) in banners" :key="idx">
            <div class="banner-slide" :style="{ background: banner.bg }">
              <div class="banner-text">
                <h2>{{ banner.title }}</h2>
                <p>{{ banner.subtitle }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>

        <div class="category-nav" v-if="topCategories.length > 0">
          <div
            class="category-item"
            v-for="cat in topCategories"
            :key="cat.id"
            @click="$router.push(`/product?categoryId=${cat.id}`)"
          >
            <div class="category-icon">{{ cat.name.charAt(0) }}</div>
            <span>{{ cat.name }}</span>
          </div>
        </div>

        <div class="product-section" v-if="newestProducts.length > 0">
          <div class="section-header">
            <h3>新品上市</h3>
            <el-button text type="primary" @click="$router.push('/product?sort=newest')">查看更多 →</el-button>
          </div>
          <el-row :gutter="16">
            <el-col :xs="12" :sm="8" :md="4" v-for="item in newestProducts" :key="item.spuId">
              <div class="product-card" @click="$router.push(`/product/${item.spuId}`)">
                <el-image :src="getFirstImage(item.images)" fit="cover" class="product-image">
                  <template #error>
                    <div class="image-placeholder"><el-icon :size="32"><Picture /></el-icon></div>
                  </template>
                </el-image>
                <div class="product-info">
                  <div class="product-name">{{ item.name }}</div>
                  <div class="product-price" v-if="item.minPrice">¥{{ item.minPrice.toFixed(2) }}</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="product-section" v-if="hotProducts.length > 0">
          <div class="section-header">
            <h3>热销商品</h3>
            <el-button text type="primary" @click="$router.push('/product?sort=sales_desc')">查看更多 →</el-button>
          </div>
          <el-row :gutter="16">
            <el-col :xs="12" :sm="8" :md="4" v-for="item in hotProducts" :key="item.spuId">
              <div class="product-card" @click="$router.push(`/product/${item.spuId}`)">
                <el-image :src="getFirstImage(item.images)" fit="cover" class="product-image">
                  <template #error>
                    <div class="image-placeholder"><el-icon :size="32"><Picture /></el-icon></div>
                  </template>
                </el-image>
                <div class="product-info">
                  <div class="product-name">{{ item.name }}</div>
                  <div class="product-price" v-if="item.minPrice">¥{{ item.minPrice.toFixed(2) }}</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <el-empty v-if="!loading && newestProducts.length === 0 && hotProducts.length === 0" description="暂无商品" />
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Picture } from '@element-plus/icons-vue'
import { productApi, authApi, getToken, removeToken } from '@ecommerce/shared'
import type { SpuDTO, CategoryDTO } from '@ecommerce/shared'

const router = useRouter()
const loading = ref(false)
const newestProducts = ref<SpuDTO[]>([])
const hotProducts = ref<SpuDTO[]>([])
const categoryTree = ref<CategoryDTO[]>([])
const nickname = ref('')

const isLoggedIn = computed(() => !!getToken())

const topCategories = computed(() => {
  return categoryTree.value.filter(c => c.status === 1).slice(0, 10)
})

const banners = [
  { title: '精选好物', subtitle: '品质生活，从这里开始', bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '新品首发', subtitle: '潮流新品，抢先体验', bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { title: '限时特惠', subtitle: '超值优惠，不容错过', bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
]

function getFirstImage(images: string): string {
  try {
    const arr = JSON.parse(images)
    return arr.length > 0 ? arr[0] : ''
  } catch {
    return ''
  }
}

async function fetchProducts() {
  loading.value = true
  try {
    const [newest, hot] = await Promise.all([
      productApi.getSpuPage({ pageNum: 1, pageSize: 6, status: 1, sort: 'newest' }),
      productApi.getSpuPage({ pageNum: 1, pageSize: 6, status: 1, sort: 'sales_desc' }),
    ])
    newestProducts.value = newest.list
    hotProducts.value = hot.list
  } catch {
    newestProducts.value = []
    hotProducts.value = []
  } finally {
    loading.value = false
  }
}

async function fetchUserInfo() {
  if (!getToken()) return
  try {
    const info = await authApi.getUserInfo()
    nickname.value = info.nickname || info.phone
  } catch {
    nickname.value = '用户'
  }
}

function handleUserCommand(command: string) {
  switch (command) {
    case 'user':
      router.push('/user')
      break
    case 'orders':
      router.push('/order')
      break
    case 'logout':
      authApi.logout().catch(() => {})
      removeToken()
      nickname.value = ''
      ElMessage.success('已退出登录')
      router.push('/')
      break
  }
}

onMounted(async () => {
  fetchProducts()
  fetchUserInfo()
  try {
    categoryTree.value = await productApi.getCategoryTree()
  } catch {
    // non-critical
  }
})
</script>

<style scoped>
.header-content {
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.logo {
  font-size: 20px;
  margin-right: 40px;
}
.user-area {
  margin-left: auto;
}
.user-nickname {
  cursor: pointer;
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 4px;
}
.home-content {
  max-width: 1200px;
  margin: 0 auto;
}
.banner-carousel {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 24px;
}
.banner-slide {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.banner-text {
  text-align: center;
  color: white;
}
.banner-text h2 {
  font-size: 36px;
  margin: 0 0 12px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.banner-text p {
  font-size: 18px;
  margin: 0;
  opacity: 0.9;
}
.category-nav {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  margin-bottom: 24px;
  overflow-x: auto;
}
.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  min-width: 72px;
  transition: transform 0.2s;
}
.category-item:hover {
  transform: translateY(-2px);
}
.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  color: #4338ca;
  margin-bottom: 6px;
}
.category-item span {
  font-size: 12px;
  color: #666;
}
.product-section {
  padding: 20px 0;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-header h3 {
  font-size: 20px;
  margin: 0;
  color: #333;
}
.product-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eaeaea;
  transition: box-shadow 0.2s;
  margin-bottom: 16px;
  background: #fff;
}
.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.product-image {
  width: 100%;
  aspect-ratio: 1;
}
.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #ccc;
}
.product-info {
  padding: 12px;
}
.product-name {
  font-size: 14px;
  line-height: 1.4;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-price {
  font-size: 16px;
  color: #e4393c;
  font-weight: bold;
  margin-top: 4px;
}
</style>
