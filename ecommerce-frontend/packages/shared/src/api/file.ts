import { get, post, del } from './request'
import type { PageResult, FileDTO } from '../types/index'

export const fileApi = {
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return post<FileDTO>('/file/file/upload', formData)
  },

  delete: (id: number) =>
    del<void>(`/file/file/${id}`),

  getById: (id: number) =>
    get<FileDTO>(`/file/file/${id}`),

  list: (params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<FileDTO>>('/file/file/list', params),
}
