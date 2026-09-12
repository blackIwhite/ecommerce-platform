import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken } from '@ecommerce/shared'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
      },
      {
        path: 'product',
        name: 'ProductList',
        component: () => import('@/views/product/List.vue'),
      },
      {
        path: 'product/edit/:id?',
        name: 'ProductEdit',
        component: () => import('@/views/product/Edit.vue'),
      },
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/order/List.vue'),
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/Detail.vue'),
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/user/List.vue'),
      },
      {
        path: 'inventory',
        name: 'InventoryList',
        component: () => import('@/views/inventory/List.vue'),
      },
      {
        path: 'coupon',
        name: 'CouponList',
        component: () => import('@/views/marketing/CouponList.vue'),
      },
      {
        path: 'promotion',
        name: 'PromotionList',
        component: () => import('@/views/marketing/PromotionList.vue'),
      },
      {
        path: 'points-rules',
        name: 'PointsRules',
        component: () => import('@/views/marketing/PointsRules.vue'),
      },
      {
        path: 'aftersales',
        name: 'AftersalesList',
        component: () => import('@/views/aftersales/List.vue'),
      },
      {
        path: 'ticket',
        name: 'TicketList',
        component: () => import('@/views/ticket/List.vue'),
      },
      {
        path: 'audit-log',
        name: 'AuditLog',
        component: () => import('@/views/audit/List.vue'),
      },
      {
        path: 'flash-sale',
        name: 'FlashSale',
        component: () => import('@/views/marketing/FlashSale.vue'),
      },
      {
        path: 'system/admin-user',
        name: 'AdminUser',
        component: () => import('@/views/system/AdminUser.vue'),
      },
      {
        path: 'system/role',
        name: 'RoleManage',
        component: () => import('@/views/system/Role.vue'),
      },
      {
        path: 'system/file',
        name: 'FileManage',
        component: () => import('@/views/system/FileManage.vue'),
      },
      {
        path: 'system/shipping-template',
        name: 'ShippingTemplate',
        component: () => import('@/views/system/ShippingTemplate.vue'),
      },
      {
        path: 'system/dict',
        name: 'DictManage',
        component: () => import('@/views/system/DictManage.vue'),
      },
      {
        path: 'system/sensitive-word',
        name: 'SensitiveWord',
        component: () => import('@/views/system/SensitiveWord.vue'),
      },
      {
        path: 'cms/article',
        name: 'ArticleList',
        component: () => import('@/views/cms/ArticleList.vue'),
      },
      {
        path: 'report',
        name: 'ReportDashboard',
        component: () => import('@/views/report/ReportDashboard.vue'),
      },
      {
        path: 'invoice',
        name: 'InvoiceList',
        component: () => import('@/views/invoice/List.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !getToken()) {
    return '/login'
  }
})

export default router
