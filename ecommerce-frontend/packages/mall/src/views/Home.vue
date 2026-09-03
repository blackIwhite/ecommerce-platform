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
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
        </div>
      </div>
    </el-header>
    <el-main>
      <div class="banner">
        <h2>欢迎来到电商商城</h2>
        <p>精选好物，品质生活</p>
      </div>
      <div class="product-section">
        <h3>热门商品</h3>
        <el-empty description="暂无商品" />
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
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
.banner {
  text-align: center;
  padding: 60px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  margin-bottom: 30px;
}
.product-section {
  padding: 20px 0;
}
</style>
