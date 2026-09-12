import { get, put } from './request'
import type { PageResult, PointsAccountDTO, PointsLogDTO, PointsRuleDTO } from '../types/index'

export const pointsApi = {
  getAccount: () =>
    get<PointsAccountDTO>('/marketing/points/account'),

  getLogs: (params: { type?: number; pageNum?: number; pageSize?: number }) =>
    get<PageResult<PointsLogDTO>>('/marketing/points/logs', params),

  // Admin APIs
  listRules: () =>
    get<PointsRuleDTO[]>('/marketing/admin/points/rules'),

  updateRule: (id: number, data: Partial<PointsRuleDTO>) =>
    put<void>(`/marketing/admin/points/rules/${id}`, data),
}
