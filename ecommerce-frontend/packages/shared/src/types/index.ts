export interface Result<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = any> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface PageQuery {
  pageNum?: number
  pageSize?: number
}

export interface SkuDTO {
  skuId: number
  spuId: number
  skuName: string
  price: number
  stock: number
  image: string
  specs: string
}

export interface SpuDTO {
  spuId: number
  name: string
  categoryId: number
  brandId: number
  description: string
  images: string
  status: number
  skuList?: SkuDTO[]
}

export interface UserDTO {
  userId: number
  phone: string
  nickname: string
  avatar: string
}

export interface UserAddressDTO {
  addressId: number
  userId: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: number
}

export interface OrderDTO {
  orderId: number
  orderNo: string
  userId: number
  totalAmount: number
  status: number
  createTime: string
  orderItems?: OrderItemDTO[]
}

export interface OrderItemDTO {
  orderItemId: number
  orderId: number
  skuId: number
  skuName: string
  price: number
  quantity: number
  totalPrice: number
  image: string
}
