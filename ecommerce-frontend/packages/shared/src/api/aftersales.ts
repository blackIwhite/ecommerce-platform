import { get, post, put } from './request'
import type { AftersalesOrderDTO, PageResult } from '../types/index'

export interface AftersalesApplyParams {
  orderId: number
  type: number
  reason: string
  description?: string
  images?: string
  exchangeAddress?: string
}

export interface AftersalesPageParams {
  pageNum?: number
  pageSize?: number
  status?: number
  type?: number
}

export const aftersalesApi = {
  apply: (data: AftersalesApplyParams) =>
    post<number>('/aftersales/aftersales/apply', data),

  cancel: (id: number) =>
    put<void>(`/aftersales/aftersales/${id}/cancel`),

  fillTracking: (id: number, trackingNo: string, company: string) =>
    put<void>(`/aftersales/aftersales/${id}/tracking`, { trackingNo, company }),

  list: (params: AftersalesPageParams) =>
    get<PageResult<AftersalesOrderDTO>>('/aftersales/aftersales/list', params),

  getDetail: (id: number) =>
    get<AftersalesOrderDTO>(`/aftersales/aftersales/${id}`),

  getLogs: (id: number) =>
    get<any[]>(`/aftersales/aftersales/${id}/logs`),

  adminList: (params: AftersalesPageParams) =>
    get<PageResult<AftersalesOrderDTO>>('/aftersales/aftersales/admin/list', params),

  adminDetail: (id: number) =>
    get<AftersalesOrderDTO>(`/aftersales/aftersales/admin/${id}`),

  adminApprove: (id: number, handler?: string) =>
    put<void>(`/aftersales/aftersales/admin/${id}/approve`, { handler: handler || 'admin' }),

  adminReject: (id: number, remark: string, handler?: string) =>
    put<void>(`/aftersales/aftersales/admin/${id}/reject`, { handler: handler || 'admin', remark }),

  adminReceive: (id: number, handler?: string) =>
    put<void>(`/aftersales/aftersales/admin/${id}/receive`, { handler: handler || 'admin' }),

  adminRefund: (id: number, handler?: string) =>
    put<void>(`/aftersales/aftersales/admin/${id}/refund`, { handler: handler || 'admin' }),

  adminShipExchange: (id: number, trackingNo: string, company: string, handler?: string) =>
    put<void>(`/aftersales/aftersales/admin/${id}/ship-exchange`, { handler: handler || 'admin', trackingNo, company }),
}
