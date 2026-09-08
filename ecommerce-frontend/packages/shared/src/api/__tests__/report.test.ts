import { beforeEach, describe, expect, it, vi } from 'vitest'
import { download, get } from '../request'
import { reportApi } from '../report'

vi.mock('../request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn(),
  download: vi.fn(),
}))

describe('api/report', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getOrderStats() GETs the admin stats endpoint with a date range', async () => {
    const params = { startDate: '2026-08-01', endDate: '2026-08-31' }
    await reportApi.getOrderStats(params)
    expect(get).toHaveBeenCalledTimes(1)
    expect(get).toHaveBeenCalledWith('/order/order/admin/report/stats', params)
  })

  it('getSalesReport() forwards the optional groupBy parameter', async () => {
    const params = { startDate: '2026-08-01', endDate: '2026-08-31', groupBy: 'day' }
    await reportApi.getSalesReport(params)
    expect(get).toHaveBeenCalledWith('/order/order/admin/report/sales', params)
  })

  it('exportOrders() delegates to download() with the orders.xlsx filename', async () => {
    const params = { startDate: '2026-08-01', endDate: '2026-08-31' }
    await reportApi.exportOrders(params)
    expect(download).toHaveBeenCalledWith(
      '/order/order/admin/report/export/orders',
      params,
      'orders.xlsx'
    )
  })

  it('exportProducts() delegates to download() with the products.xlsx filename', async () => {
    const params = { startDate: '2026-07-01', endDate: '2026-07-31' }
    await reportApi.exportProducts(params)
    expect(download).toHaveBeenCalledWith(
      '/order/order/admin/report/export/products',
      params,
      'products.xlsx'
    )
  })
})
