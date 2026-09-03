<template>
  <div>
    <h2>订单详情</h2>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="订单号">-</el-descriptions-item>
      <el-descriptions-item label="状态">-</el-descriptions-item>
      <el-descriptions-item label="用户">-</el-descriptions-item>
      <el-descriptions-item label="金额">-</el-descriptions-item>
      <el-descriptions-item label="收货人">-</el-descriptions-item>
      <el-descriptions-item label="收货地址">-</el-descriptions-item>
    </el-descriptions>
    <h3 style="margin-top: 20px;">商品明细</h3>
    <el-table :data="[]" border>
      <el-table-column prop="skuName" label="商品" />
      <el-table-column prop="price" label="单价" />
      <el-table-column prop="quantity" label="数量" />
      <el-table-column prop="totalPrice" label="小计" />
    </el-table>
  </div>
</template>
