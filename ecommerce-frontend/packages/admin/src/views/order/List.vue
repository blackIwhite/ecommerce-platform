<template>
  <div>
    <h2>订单管理</h2>
    <el-table :data="[]" border>
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="userId" label="用户" />
      <el-table-column prop="totalAmount" label="金额" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="200">
        <template #default>
          <el-button size="small" @click="$router.push('/order/0')">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
