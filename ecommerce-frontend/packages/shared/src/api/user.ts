import request, { get } from './request'
import type { UserDTO, PageResult } from '../types/index'

export interface UserPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export const userApi = {
  page: (params: UserPageParams) =>
    get<PageResult<UserDTO>>('/user/page', params),

  updateStatus: (userId: number, status: number) =>
    request.put<void>(`/user/${userId}/status`, null, { params: { status } }),
}
