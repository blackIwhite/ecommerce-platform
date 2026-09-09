import { get, post, put, del } from './request'
import type { FlashSaleItemDTO } from '../types/index'

export const flashSaleApi = {
  getActiveItems: (promotionId: number) =>
    get<FlashSaleItemDTO[]>(`/marketing/marketing/flash-sale/active`, { promotionId }),

  getItemDetail: (itemId: number) =>
    get<FlashSaleItemDTO>(`/marketing/marketing/flash-sale/${itemId}`),

  // Admin APIs
  listItems: (promotionId: number) =>
    get<any>(`/marketing/marketing/admin/flash-sale/list`, { promotionId }).then(res => res.list ?? []),

  addItem: (data: Partial<FlashSaleItemDTO>) =>
    post<void>('/marketing/marketing/admin/flash-sale', data),

  updateItem: (id: number, data: Partial<FlashSaleItemDTO>) =>
    put<void>('/marketing/marketing/admin/flash-sale', { ...data, id }),

  deleteItem: (id: number) =>
    del<void>(`/marketing/marketing/admin/flash-sale/${id}`),
}
