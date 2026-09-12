import request, { get, post, del } from './request'

export interface CartItemDTO {
  id: number
  skuId: number
  skuName: string
  price: number
  stock: number
  image: string
  specs: string
  quantity: number
}

export interface CartAddParams {
  skuId: number
  quantity?: number
}

export const cartApi = {
  list: () => get<CartItemDTO[]>('/cart/list'),

  add: (data: CartAddParams) => post<void>('/cart/add', data),

  updateQuantity: (cartItemId: number, quantity: number) =>
    request.put<void>(`/cart/cart/${cartItemId}/quantity`, null, { params: { quantity } }),

  remove: (cartItemId: number) => del<void>(`/cart/cart/${cartItemId}`),

  clear: () => del<void>('/cart/clear'),
}
