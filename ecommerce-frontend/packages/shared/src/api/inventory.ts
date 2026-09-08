import request, { get } from './request'
import type { PageResult } from '../types/index'

export interface InventoryDTO {
  id: number
  skuId: number
  availableStock: number
  lockedStock: number
  version: number
  createTime: string
  updateTime: string
}

export interface InventoryPageParams {
  pageNum?: number
  pageSize?: number
  skuId?: number
}

export const inventoryApi = {
  list: (params: InventoryPageParams) =>
    get<PageResult<InventoryDTO>>('/inventory/inventory/admin/list', params),

  setStock: (skuId: number, stock: number) =>
    request.post<void>('/inventory/inventory/admin/stock/set', { skuId, stock }),

  adjustStock: (skuId: number, delta: number) =>
    request.post<void>('/inventory/inventory/admin/stock/adjust', { skuId, delta }),
}
