import { get, post, del } from './request'
import type { PageResult } from '../types/index'

export interface SensitiveWordDTO {
  id: number
  word: string
  createTime: string
}

export const sensitiveWordApi = {
  page: (params: { pageNum?: number; pageSize?: number; keyword?: string }) =>
    get<PageResult<SensitiveWordDTO>>('/product/sensitive-word/list', params),

  add: (word: string) =>
    post<void>('/product/sensitive-word', { word }),

  remove: (id: number) =>
    del<void>(`/product/sensitive-word/${id}`),

  batchAdd: (words: string) =>
    post<void>('/product/sensitive-word/batch', { words }),

  reload: () =>
    post<void>('/product/sensitive-word/reload'),

  test: (text: string) =>
    post<{ containsSensitiveWord: boolean; filteredText: string }>(
      '/product/sensitive-word/test', { text }
    ),
}
