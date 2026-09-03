<template>
  <div>
    <h2>库存管理</h2>
    <el-table :data="[]" border>
      <el-table-column prop="skuId" label="SKU ID" />
      <el-table-column prop="skuName" label="商品名称" />
      <el-table-column prop="availableStock" label="可用库存" />
      <el-table-column prop="lockedStock" label="锁定库存" />
      <el-table-column label="操作" width="150">
        <template #default>
          <el-button size="small">调整</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
