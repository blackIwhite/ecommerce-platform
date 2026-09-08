import { get } from './request'

export interface AuditLogItem {
  id: number
  serviceName: string
  module: string
  operation: string
  description: string
  method: string
  requestParams: string
  responseCode: number
  userId: string
  ip: string
  duration: number
  createTime: string
}

export interface AuditLogQuery {
  serviceName?: string
  module?: string
  userId?: string
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

export const auditLogApi = {
  list: (params: AuditLogQuery) =>
    get<AuditLogItem[]>('/user/user/admin/audit-log/list', params),
  count: (params: { serviceName?: string; module?: string }) =>
    get<{ total: number }>('/user/user/admin/audit-log/count', params),
}
