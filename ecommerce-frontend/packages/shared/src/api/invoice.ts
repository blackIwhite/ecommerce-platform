import { get, post, put } from './request'
import type { PageResult } from '../types/index'

export interface InvoiceDTO {
  id: number
  orderId: number
  orderNo?: string
  userId: number
  type: number
  title: string
  taxNo: string
  amount: number
  email: string
  phone: string
  status: number
  statusName: string
  invoiceNo: string
  invoiceUrl: string
  remark: string
  rejectReason: string
  applyTime: string
  issueTime: string
  createTime: string
}

export interface InvoiceApplyRequest {
  orderId: number
  type: number
  title: string
  taxNo?: string
  email?: string
  phone?: string
  remark?: string
}

export const invoiceApi = {
  apply: (data: InvoiceApplyRequest) =>
    post<void>('/order/order/invoice/apply', data),

  getMyInvoices: (params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<InvoiceDTO>>('/order/order/invoice/my', params),

  getInvoice: (id: number) =>
    get<InvoiceDTO>(`/order/order/invoice/${id}`),

  listAdmin: (params: { pageNum?: number; pageSize?: number; status?: number }) =>
    get<PageResult<InvoiceDTO>>('/order/order/admin/invoice/list', params),

  issue: (id: number, data: { invoiceNo: string; invoiceUrl: string }) =>
    put<void>(`/order/order/admin/invoice/${id}/issue`, data),

  reject: (id: number, data: { reason: string }) =>
    put<void>(`/order/order/admin/invoice/${id}/reject`, data),
}
