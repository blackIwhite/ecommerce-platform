<template>
  <div>
    <h2>用户管理</h2>
    <el-table :data="[]" border>
      <el-table-column prop="userId" label="ID" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createTime" label="注册时间" />
      <el-table-column label="操作" width="150">
        <template #default>
          <el-button size="small">编辑</el-button>
          <el-button size="small" type="danger">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
