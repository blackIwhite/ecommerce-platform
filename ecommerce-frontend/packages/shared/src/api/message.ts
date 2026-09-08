import { get, post } from './request'
import type { PageResult, UserMessageDTO, UnreadCountDTO } from '../types/index'

export const messageApi = {
  listMessages: (params: { pageNum?: number; pageSize?: number; type?: number; isRead?: number }) =>
    get<PageResult<UserMessageDTO>>('/user/user/message/list', params),

  getUnreadCount: () =>
    get<UnreadCountDTO>('/user/user/message/unread-count'),

  markAsRead: (id: number) =>
    post<void>(`/user/user/message/read/${id}`),

  markAllAsRead: () =>
    post<void>('/user/user/message/read-all'),
}
