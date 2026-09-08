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
  salesCount: number
  viewCount: number
  minPrice: number
  avgRating?: number
  reviewCount?: number
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
  couponId?: number
  discountAmount?: number
  status: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark: string
  createTime: string
  logisticsCompany?: string
  trackingNo?: string
  items?: OrderItemDTO[]
  statusLogs?: StatusLogDTO[]
}

export interface OrderItemDTO {
  skuId: number
  spuId: number
  skuName: string
  price: number
  quantity: number
  totalPrice: number
  image: string
}

export interface StatusLogDTO {
  fromStatus: number
  toStatus: number
  operator: string
  remark: string
  createTime: string
}

export interface LogisticsTraceDTO {
  trackingNo: string
  logisticsCompany: string
  traces: { time: string; description: string }[]
}

export interface PaymentDTO {
  paymentId: number
  orderId: number
  paymentNo: string
  amount: number
  payMethod: string
  status: number
  payTime: string
  createTime: string
}

export interface UserFavoriteDTO {
  spuId: number
  spuName: string
  image: string
  minPrice: number
  createTime: string
}

export interface ReviewDTO {
  id: number
  spuId: number
  skuId: number
  skuName?: string
  userId: number
  nickname: string
  avatar: string
  rating: number
  content: string
  images: string
  createTime: string
}

export interface ReviewCreateRequest {
  spuId: number
  skuId: number
  orderId: number
  rating: number
  content?: string
  images?: string
}

export interface ReviewStatsDTO {
  spuId: number
  avgRating: number
  reviewCount: number
  ratingDistribution: Record<number, number>
}

export interface CouponTemplateDTO {
  id?: number
  name: string
  type: number
  discountValue: number
  minPurchase: number
  maxDiscount?: number
  totalCount: number
  claimedCount?: number
  perLimit: number
  startTime: string
  endTime: string
  status?: number
  description?: string
  createTime?: string
}

export interface UserCouponDTO {
  id: number
  userId: number
  templateId: number
  couponName: string
  type: number
  discountValue: number
  minPurchase: number
  maxDiscount?: number
  status: number
  orderId?: number
  useTime?: string
  expireTime: string
  createTime: string
}

export interface PromotionDTO {
  id?: number
  name: string
  type: number
  ruleJson?: string
  rules?: FullReductionRule[]
  startTime: string
  endTime: string
  status?: number
  description?: string
  createTime?: string
  items?: FlashSaleItemDTO[]
}

export interface FullReductionRule {
  minAmount: number
  reduction: number
}

export interface FlashSaleItemDTO {
  id?: number
  spuId: number
  skuId: number
  flashPrice: number
  totalStock: number
  availableStock?: number
  limitPerUser?: number
}

export interface PointsAccountDTO {
  userId: number
  totalPoints: number
  availablePoints: number
  usedPoints: number
  expiredPoints: number
  level: number
  levelName: string
}

export interface PointsLogDTO {
  id: number
  userId: number
  type: number
  typeName: string
  points: number
  balance: number
  source: string
  referenceId: number
  description: string
  createTime: string
}

export interface PointsRuleDTO {
  id: number
  ruleKey: string
  ruleValue: string
  description: string
  status: number
  createTime?: string
}

export interface AftersalesOrderDTO {
  id: number
  aftersalesNo: string
  orderId: number
  userId: number
  type: number
  typeName: string
  status: number
  statusName: string
  reason: string
  description: string
  images: string
  refundAmount: number
  returnTrackingNo: string
  returnCompany: string
  exchangeTrackingNo: string
  exchangeCompany: string
  exchangeAddress: string
  handler: string
  handleRemark: string
  createTime: string
  items?: AftersalesItemDTO[]
  logs?: AftersalesLogDTO[]
}

export interface AftersalesItemDTO {
  id: number
  spuId: number
  skuId: number
  productName: string
  skuName: string
  image: string
  price: number
  quantity: number
}

export interface AftersalesLogDTO {
  fromStatus: number
  toStatus: number
  toStatusName: string
  operator: string
  remark: string
  createTime: string
}

export interface ServiceTicketDTO {
  id: number
  ticketNo: string
  userId: number
  type: number
  typeName: string
  subject: string
  content: string
  priority: number
  priorityName: string
  status: number
  statusName: string
  assignedTo: string
  orderId: number
  createTime: string
  updateTime: string
  messages?: TicketMessageDTO[]
}

export interface TicketMessageDTO {
  id: number
  senderType: number
  senderId: number
  senderName: string
  content: string
  createTime: string
}

export interface ServiceHealthStatus {
  name: string
  displayName: string
  status: string
  host: string
  port: number
}
