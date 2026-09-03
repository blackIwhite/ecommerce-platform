<template>
  <div>
    <h2>编辑商品</h2>
    <el-form label-width="100px">
      <el-form-item label="商品名称">
        <el-input placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="商品分类">
        <el-cascader placeholder="请选择分类" :options="[]" />
      </el-form-item>
      <el-form-item label="商品价格">
        <el-input-number :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="商品描述">
        <el-input type="textarea" :rows="4" placeholder="请输入商品描述" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
