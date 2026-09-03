<template>
  <div>
    <h2>数据概览</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card><el-statistic title="今日订单" :value="0" /></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><el-statistic title="今日销售额" :value="0" prefix="¥" /></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><el-statistic title="商品总数" :value="0" /></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><el-statistic title="用户总数" :value="0" /></el-card>
      </el-col>
    </el-row>
  </div>
</template>
