import { get, post, put, del } from './request'
import type { PageResult } from '../types/index'

export interface ShippingTemplateDTO {
  id: number
  name: string
  chargeType: number
  defaultFee: number
  freeThreshold: number
  status: number
  createTime: string
  rules?: ShippingTemplateRuleDTO[]
}

export interface ShippingTemplateRuleDTO {
  id: number
  templateId: number
  regionCodes: string
  regionNames: string
  startThreshold: number
  startFee: number
  additionalThreshold: number
  additionalFee: number
}

export interface ShippingFeeCalculateResult {
  shippingFee: number
  freeShipping: boolean
  templateName: string
}

export const shippingApi = {
  page: (params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<ShippingTemplateDTO>>('/product/shipping/list', params),

  getDetail: (id: number) =>
    get<ShippingTemplateDTO>(`/product/shipping/${id}`),

  create: (data: any) =>
    post<void>('/product/shipping/admin', data),

  update: (data: any) =>
    put<void>('/product/shipping/admin', data),

  delete: (id: number) =>
    del<void>(`/product/shipping/admin/${id}`),

  updateStatus: (id: number, status: number) =>
    put<void>(`/product/shipping/admin/${id}/status?status=${status}`),

  calculate: (templateId: number, regionCode: string, quantity: number) =>
    get<ShippingFeeCalculateResult>('/product/shipping/calculate', { templateId, regionCode, quantity }),
}
