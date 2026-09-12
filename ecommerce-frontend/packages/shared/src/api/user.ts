import request, { get, post, put, del } from './request'
import type { UserDTO, UserAddressDTO, PageResult } from '../types/index'

export interface UserPageParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  status?: number
}

export interface UpdateProfileRequest {
  nickname?: string
  avatar?: string
}

export interface AddressRequest {
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault?: number
}

export const userApi = {
  page: (params: UserPageParams) =>
    get<PageResult<UserDTO>>('/user/page', params),

  updateStatus: (userId: number, status: number) =>
    request.put<void>(`/user/${userId}/status`, null, { params: { status } }),

  getMe: () =>
    get<UserDTO>('/user/me'),

  updateProfile: (data: UpdateProfileRequest) =>
    put<UserDTO>('/user/user', data),

  getAddressList: () =>
    get<UserAddressDTO[]>('/user/address/list'),

  addAddress: (data: AddressRequest) =>
    post<UserAddressDTO>('/user/address', data),

  updateAddress: (data: AddressRequest & { addressId: number }) =>
    put<UserAddressDTO>('/user/address', data),

  deleteAddress: (addressId: number) =>
    del<void>(`/user/address/${addressId}`),
}
