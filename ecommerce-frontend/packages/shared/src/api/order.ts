import { get, post, put } from './request'
import type { OrderDTO, PaymentDTO, LogisticsTraceDTO, PageResult } from '../types/index'

export interface OrderPageParams {
  pageNum?: number
  pageSize?: number
  status?: number
  orderNo?: string
}

export interface OrderSubmitParams {
  addressId: number
  remark?: string
  userCouponId?: number
  items: { skuId: number; quantity: number }[]
}

export interface OrderConfirmResult {
  totalAmount: number
  discountAmount: number
  payableAmount: number
  items: {
    skuId: number
    skuName: string
    price: number
    quantity: number
    totalPrice: number
    image: string
  }[]
}

export const orderApi = {
  confirm: (data: OrderSubmitParams) =>
    post<OrderConfirmResult>('/order/confirm', data),

  submit: (data: OrderSubmitParams) =>
    post<number>('/order/submit', data),

  list: (params: OrderPageParams) =>
    get<PageResult<OrderDTO>>('/order/list', params),

  getDetail: (orderId: number) =>
    get<OrderDTO>(`/order/${orderId}`),

  getStatus: (orderId: number) =>
    get<number>(`/order/status/${orderId}`),

  cancel: (orderId: number, reason?: string) =>
    put<void>(`/order/${orderId}/cancel`, { cancelReason: reason }),

  pay: (orderId: number) =>
    put<void>(`/order/${orderId}/pay`),

  receive: (orderId: number) =>
    put<void>(`/order/${orderId}/receive`),

  adminList: (params: OrderPageParams) =>
    get<PageResult<OrderDTO>>('/order/admin/list', params),

  adminDetail: (orderId: number) =>
    get<OrderDTO>(`/order/admin/${orderId}`),

  adminShip: (orderId: number, trackingNo: string) =>
    put<void>(`/order/admin/${orderId}/ship`, { trackingNo }),

  getPayment: (orderId: number) =>
    get<PaymentDTO>(`/order/payment/${orderId}`),

  getLogistics: (orderId: number) =>
    get<LogisticsTraceDTO>(`/order/${orderId}/logistics`),
}
