import { get, post } from './request'
import type { FlashSaleItemDTO } from '../types/index'

export const flashSaleApi = {
  getActiveItems: (promotionId: number) =>
    get<FlashSaleItemDTO[]>(`/marketing/marketing/flash-sale/active/${promotionId}`),

  getItemDetail: (itemId: number) =>
    get<FlashSaleItemDTO>(`/marketing/marketing/flash-sale/item/${itemId}`),

  // Admin APIs
  listItems: (promotionId: number) =>
    get<FlashSaleItemDTO[]>(`/marketing/marketing/admin/flash-sale/items/${promotionId}`),

  addItem: (data: Partial<FlashSaleItemDTO>) =>
    post<void>('/marketing/marketing/admin/flash-sale/items', data),

  updateItem: (id: number, data: Partial<FlashSaleItemDTO>) =>
    post<void>(`/marketing/marketing/admin/flash-sale/items/${id}`, data),

  deleteItem: (id: number) =>
    post<void>(`/marketing/marketing/admin/flash-sale/items/${id}/delete`),
}
