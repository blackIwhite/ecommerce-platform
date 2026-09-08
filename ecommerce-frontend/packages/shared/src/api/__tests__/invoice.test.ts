import { beforeEach, describe, expect, it, vi } from 'vitest'
import { get, post, put } from '../request'
import { invoiceApi } from '../invoice'
import type { InvoiceApplyRequest } from '../invoice'

vi.mock('../request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn(),
  download: vi.fn(),
}))

describe('api/invoice', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('apply() POSTs the application to /order/order/invoice/apply', async () => {
    const payload: InvoiceApplyRequest = {
      orderId: 42,
      type: 1,
      title: 'Acme Ltd',
      taxNo: 'TAX-123',
      email: 'billing@acme.test',
    }
    await invoiceApi.apply(payload)
    expect(post).toHaveBeenCalledTimes(1)
    expect(post).toHaveBeenCalledWith('/order/order/invoice/apply', payload)
  })

  it('getMyInvoices() GETs /order/order/invoice/my with paging params', async () => {
    const params = { pageNum: 2, pageSize: 20 }
    await invoiceApi.getMyInvoices(params)
    expect(get).toHaveBeenCalledWith('/order/order/invoice/my', params)
  })

  it('getInvoice() GETs the invoice by id', async () => {
    await invoiceApi.getInvoice(7)
    expect(get).toHaveBeenCalledWith('/order/order/invoice/7')
  })

  it('issue() PUTs invoiceNo/invoiceUrl to the admin issue endpoint', async () => {
    const data = { invoiceNo: 'INV-001', invoiceUrl: 'https://cdn/inv-001.pdf' }
    await invoiceApi.issue(9, data)
    expect(put).toHaveBeenCalledWith('/order/order/admin/invoice/9/issue', data)
  })

  it('reject() PUTs the reason to the admin reject endpoint', async () => {
    await invoiceApi.reject(9, { reason: 'tax number mismatch' })
    expect(put).toHaveBeenCalledWith('/order/order/admin/invoice/9/reject', {
      reason: 'tax number mismatch',
    })
  })
})
