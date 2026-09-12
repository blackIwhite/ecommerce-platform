import { get, post, del } from './request'
import type { PageResult, SpuDTO } from '../types/index'

export interface SearchResultDTO {
  keyword: string
  total: number
  items: SpuDTO[]
  suggestions: string[]
}

export interface HotSearchDTO {
  id: number
  keyword: string
  searchCount: number
}

export const searchApi = {
  search: (params: {
    keyword?: string; categoryId?: number; brandId?: number
    minPrice?: number; maxPrice?: number; sortBy?: string
    pageNum?: number; pageSize?: number
  }) => get<PageResult<SpuDTO>>('/product/search', params),

  getSuggestions: (prefix: string) =>
    get<string[]>('/product/search/suggestions', { prefix }),

  getHotSearches: () =>
    get<HotSearchDTO[]>('/product/search/hot'),

  getHistory: () =>
    get<string[]>('/product/search/history'),

  clearHistory: () =>
    del<void>('/product/search/history'),

  record: (keyword: string) =>
    post<void>('/product/search/record', { keyword }),
}
