import { get } from './request'
import type { ServiceHealthStatus } from '../types'

export const monitorApi = {
  health: () => get<ServiceHealthStatus[]>('/monitor/health'),
}
