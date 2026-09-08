import { get, post } from './request'
import type { PageResult, UserFavoriteDTO } from '../types/index'

export const favoriteApi = {
  toggle: (spuId: number) =>
    post<boolean>(`/user/user/favorite/toggle?spuId=${spuId}`),

  check: (spuId: number) =>
    get<boolean>('/user/user/favorite/check', { spuId }),

  list: (params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<UserFavoriteDTO>>('/user/user/favorite/list', params),
}
