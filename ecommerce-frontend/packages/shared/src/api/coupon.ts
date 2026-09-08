import { get, post, put, del } from './request'
import type { PageResult, CouponTemplateDTO, UserCouponDTO } from '../types/index'

export const couponApi = {
  createTemplate: (data: CouponTemplateDTO) =>
    post<void>('/marketing/marketing/admin/template', data),

  updateTemplate: (data: CouponTemplateDTO) =>
    put<void>('/marketing/marketing/admin/template', data),

  deleteTemplate: (id: number) =>
    del<void>(`/marketing/marketing/admin/template/${id}`),

  updateTemplateStatus: (id: number, status: number) =>
    put<void>(`/marketing/marketing/admin/template/${id}/status`, { status }),

  listTemplates: (params: { pageNum?: number; pageSize?: number; status?: number }) =>
    get<PageResult<CouponTemplateDTO>>('/marketing/marketing/admin/template/list', params),

  getTemplate: (id: number) =>
    get<CouponTemplateDTO>(`/marketing/marketing/admin/template/${id}`),

  listAvailable: () =>
    get<CouponTemplateDTO[]>('/marketing/marketing/template/available'),

  claim: (templateId: number) =>
    post<number>(`/marketing/marketing/coupon/claim/${templateId}`),

  getMyCoupons: (status?: number) =>
    get<UserCouponDTO[]>('/marketing/marketing/coupon/my', { status }),
}
