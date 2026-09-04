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
  categoryName: string
  brandId: number
  brandName: string
  description: string
  images: string
  status: number
  createTime: string
  skuList?: SkuDTO[]
}

export interface CategoryDTO {
  id: number
  name: string
  parentId: number
  level: number
  sort: number
  icon: string
  status: number
  children?: CategoryDTO[]
}

export interface BrandDTO {
  id: number
  name: string
  logo: string
  description: string
  status: number
}

export interface UserDTO {
  userId: number
  phone: string
  nickname: string
  avatar: string
  status: number
  createTime: string
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
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark: string
  createTime: string
  items?: OrderItemDTO[]
}

export interface OrderItemDTO {
  skuId: number
  skuName: string
  price: number
  quantity: number
  totalPrice: number
  image: string
}
