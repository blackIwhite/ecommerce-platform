import { get, post, del } from './request'
import type { PageResult } from '../types/index'

export interface BrowseHistoryDTO {
  id: number
  spuId: number
  productName: string
  productImage: string
  price: number
  browseTime: string
  duration: number
}

export interface BehaviorLogDTO {
  id: number
  action: string
  actionName: string
  targetType: string
  targetId: number
  extraData: string
  createTime: string
}

export interface BehaviorRecordRequest {
  action: string
  targetType?: string
  targetId?: number
  extraData?: string
  spuId?: number
  duration?: number
}

export const behaviorApi = {
  record: (data: BehaviorRecordRequest) =>
    post<void>('/user/user/behavior/record', data),

  getBrowseHistory: (params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<BrowseHistoryDTO>>('/user/user/behavior/browse-history', params),

  clearBrowseHistory: () =>
    del<void>('/user/user/behavior/browse-history'),

  getBehaviorLogs: (params: { action?: string; pageNum?: number; pageSize?: number }) =>
    get<PageResult<BehaviorLogDTO>>('/user/user/behavior/logs', params),

  getBehaviorStats: () =>
    get<Record<string, number>>('/user/user/behavior/stats'),
}
