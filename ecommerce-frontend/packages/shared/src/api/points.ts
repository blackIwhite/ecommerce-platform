import { get, put } from './request'
import type { PageResult, PointsAccountDTO, PointsLogDTO, PointsRuleDTO } from '../types/index'

export const pointsApi = {
  getAccount: () =>
    get<PointsAccountDTO>('/marketing/marketing/points/account'),

  getLogs: (params: { type?: number; pageNum?: number; pageSize?: number }) =>
    get<PageResult<PointsLogDTO>>('/marketing/marketing/points/logs', params),

  // Admin APIs
  listRules: () =>
    get<PointsRuleDTO[]>('/marketing/marketing/admin/points/rules'),

  updateRule: (id: number, data: Partial<PointsRuleDTO>) =>
    put<void>(`/marketing/marketing/admin/points/rules/${id}`, data),
}
