import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken } from '@ecommerce/shared'

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
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
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
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/checkout/Index.vue'),
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
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/Detail.vue'),
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('@/views/user/Center.vue'),
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/user/Favorites.vue'),
  },
  {
    path: '/coupons',
    name: 'Coupons',
    component: () => import('@/views/user/Coupons.vue'),
  },
  {
    path: '/points',
    name: 'Points',
    component: () => import('@/views/user/Points.vue'),
  },
  {
    path: '/aftersales',
    name: 'AftersalesList',
    component: () => import('@/views/aftersales/List.vue'),
  },
  {
    path: '/aftersales/apply',
    name: 'AftersalesApply',
    component: () => import('@/views/aftersales/Apply.vue'),
  },
  {
    path: '/aftersales/:id',
    name: 'AftersalesDetail',
    component: () => import('@/views/aftersales/Detail.vue'),
  },
  {
    path: '/ticket',
    name: 'TicketList',
    component: () => import('@/views/ticket/List.vue'),
  },
  {
    path: '/ticket/create',
    name: 'TicketCreate',
    component: () => import('@/views/ticket/Create.vue'),
  },
  {
    path: '/ticket/:id',
    name: 'TicketDetail',
    component: () => import('@/views/ticket/Detail.vue'),
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/user/Messages.vue'),
  },
  {
    path: '/invoices',
    name: 'Invoices',
    component: () => import('@/views/invoice/List.vue'),
  },
  {
    path: '/browse-history',
    name: 'BrowseHistory',
    component: () => import('@/views/user/BrowseHistory.vue'),
  },
  {
    path: '/compare',
    name: 'ProductCompare',
    component: () => import('@/views/product/Compare.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const authRoutes = ['/cart', '/checkout', '/order', '/user', '/favorites', '/coupons', '/points', '/aftersales', '/ticket', '/messages', '/invoices', '/browse-history']

router.beforeEach((to) => {
  if (authRoutes.some((r) => to.path.startsWith(r)) && !getToken()) {
    return '/login'
  }
})

export default router
