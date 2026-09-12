import { get, post, del } from './request'
import type { PageResult, FileDTO } from '../types/index'

export const fileApi = {
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return post<FileDTO>('/file/upload', formData)
  },

  delete: (id: number) =>
    del<void>(`/file/${id}`),

  getById: (id: number) =>
    get<FileDTO>(`/file/${id}`),

  list: (params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<FileDTO>>('/file/list', params),
}
