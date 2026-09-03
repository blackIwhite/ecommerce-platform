import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/product',
    name: 'ProductList',
    component: () => import('@/views/product/List.vue'),
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/product/Detail.vue'),
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue'),
  },
  {
    path: '/order',
    name: 'OrderList',
    component: () => import('@/views/order/List.vue'),
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('@/views/user/Center.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
