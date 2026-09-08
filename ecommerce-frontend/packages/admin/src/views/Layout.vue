<template>
  <el-container class="layout">
    <el-aside width="200px">
      <div class="logo">管理后台</div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/dashboard">
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/product">
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/inventory">
          <span>库存管理</span>
        </el-menu-item>
        <el-menu-item index="/coupon">
          <span>优惠券管理</span>
        </el-menu-item>
        <el-menu-item index="/promotion">
          <span>促销活动</span>
        </el-menu-item>
        <el-menu-item index="/points-rules">
          <span>积分规则</span>
        </el-menu-item>
        <el-menu-item index="/flash-sale">
          <span>秒杀管理</span>
        </el-menu-item>
        <el-menu-item index="/aftersales">
          <span>售后管理</span>
        </el-menu-item>
        <el-menu-item index="/ticket">
          <span>工单管理</span>
        </el-menu-item>
        <el-menu-item index="/audit-log">
          <span>审计日志</span>
        </el-menu-item>
        <el-menu-item index="/cms/article">
          <span>内容管理</span>
        </el-menu-item>
        <el-menu-item index="/report">
          <span>数据报表</span>
        </el-menu-item>
        <el-menu-item index="/invoice">
          <span>发票管理</span>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title>系统管理</template>
          <el-menu-item index="/system/admin-user">管理员</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/file">文件管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-right">
          <span>管理员</span>
          <el-button text @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
const handleLogout = () => {
  localStorage.removeItem('token')
  window.location.href = '/login'
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #eee;
}
.el-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #eee;
}
.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
