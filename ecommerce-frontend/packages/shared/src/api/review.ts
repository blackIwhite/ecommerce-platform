import { get, post } from './request'
import type { PageResult, ReviewDTO, ReviewStatsDTO, ReviewCreateRequest } from '../types/index'

export const reviewApi = {
  create: (data: ReviewCreateRequest) =>
    post<void>('/product/product/review', data),

  list: (params: { spuId: number; pageNum?: number; pageSize?: number }) =>
    get<PageResult<ReviewDTO>>('/product/product/review/list', params),

  stats: (spuId: number) =>
    get<ReviewStatsDTO>('/product/product/review/stats', { spuId }),
}
