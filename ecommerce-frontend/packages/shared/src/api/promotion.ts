import { get, post, put, del } from './request'
import type { PageResult, PromotionDTO } from '../types/index'

export const promotionApi = {
  create: (data: PromotionDTO) =>
    post<void>('/marketing/marketing/admin/promotion', data),

  update: (data: PromotionDTO) =>
    put<void>('/marketing/marketing/admin/promotion', data),

  delete: (id: number) =>
    del<void>(`/marketing/marketing/admin/promotion/${id}`),

  updateStatus: (id: number, status: number) =>
    put<void>(`/marketing/marketing/admin/promotion/${id}/status`, { status }),

  list: (params: { pageNum?: number; pageSize?: number; type?: number; status?: number }) =>
    get<PageResult<PromotionDTO>>('/marketing/marketing/admin/promotion/list', params),

  get: (id: number) =>
    get<PromotionDTO>(`/marketing/marketing/admin/promotion/${id}`),

  listActive: () =>
    get<PromotionDTO[]>('/marketing/marketing/promotion/active'),
}
