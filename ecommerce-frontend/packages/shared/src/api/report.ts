import { get, download } from './request'
import type { OrderStatsDTO, SalesReportDTO, ProductSalesDTO } from '../types/index'

export const reportApi = {
  getOrderStats: (params: { startDate: string; endDate: string }) =>
    get<OrderStatsDTO>('/order/order/admin/report/stats', params),

  getSalesReport: (params: { startDate: string; endDate: string; groupBy?: string }) =>
    get<SalesReportDTO[]>('/order/order/admin/report/sales', params),

  getProductSalesReport: (params: { startDate: string; endDate: string }) =>
    get<ProductSalesDTO[]>('/order/order/admin/report/products', params),

  exportOrders: (params: { startDate: string; endDate: string }) =>
    download('/order/order/admin/report/export/orders', params, 'orders.xlsx'),

  exportProducts: (params: { startDate: string; endDate: string }) =>
    download('/order/order/admin/report/export/products', params, 'products.xlsx'),
}
