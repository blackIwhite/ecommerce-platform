import { get, post, put, del } from './request'
import type { PageResult } from '../types/index'

export interface DictTypeDTO {
  id: number
  typeCode: string
  typeName: string
  remark: string
  status: number
  createTime: string
}

export interface DictItemDTO {
  id: number
  typeCode: string
  itemValue: string
  itemLabel: string
  sortOrder: number
  status: number
  remark: string
}

export const dictApi = {
  pageTypes: (params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<DictTypeDTO>>('/user/dict/type/page', params),

  listAllTypes: () =>
    get<DictTypeDTO[]>('/user/dict/type/list'),

  createType: (data: Partial<DictTypeDTO>) =>
    post<void>('/user/dict/type', data),

  updateType: (data: Partial<DictTypeDTO>) =>
    put<void>('/user/dict/type', data),

  deleteType: (id: number) =>
    del<void>(`/user/dict/type/${id}`),

  listItems: (typeCode: string) =>
    get<DictItemDTO[]>('/user/dict/item/list', { typeCode }),

  createItem: (data: Partial<DictItemDTO>) =>
    post<void>('/user/dict/item', data),

  updateItem: (data: Partial<DictItemDTO>) =>
    put<void>('/user/dict/item', data),

  deleteItem: (id: number) =>
    del<void>(`/user/dict/item/${id}`),

  updateItemStatus: (id: number, status: number) =>
    put<void>(`/user/dict/item/${id}/status`, { status }),
}
