import request, { get, post, put } from './request'
import type { SpuDTO, PageResult, CategoryDTO, BrandDTO } from '../types/index'

export interface SpuPageParams {
  pageNum?: number
  pageSize?: number
  categoryId?: number
  brandId?: number
  status?: number
  keyword?: string
  sort?: string
}

export const productApi = {
  getSpuPage: (params: SpuPageParams) =>
    get<PageResult<SpuDTO>>('/product/spu/page', params),

  getSpuDetail: (spuId: number) =>
    get<SpuDTO>(`/product/spu/${spuId}`),

  getCategoryTree: () =>
    get<CategoryDTO[]>('/product/category/tree'),

  getBrandList: () =>
    get<BrandDTO[]>('/product/brand/list'),

  createSpu: (data: any) =>
    post<number>('/product/admin/spu', data),

  updateSpu: (data: any) =>
    put<void>('/product/admin/spu', data),

  updateSpuStatus: (spuId: number, status: number) =>
    request.put<void>(`/product/admin/spu/${spuId}/status`, null, { params: { status } }),

  reindexAll: () =>
    post<void>('/product/admin/spu/reindex'),
}
