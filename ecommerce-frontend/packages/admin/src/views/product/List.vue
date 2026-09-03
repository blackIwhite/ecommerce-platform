<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px;">
      <h2>商品管理</h2>
      <el-button type="primary" @click="$router.push('/product/edit')">新增商品</el-button>
    </div>
    <el-table :data="[]" border>
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="categoryId" label="分类" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="200">
        <template #default>
          <el-button size="small">编辑</el-button>
          <el-button size="small" type="danger">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
