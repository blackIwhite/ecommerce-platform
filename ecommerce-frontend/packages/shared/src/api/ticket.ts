import { get, post, put } from './request'
import type { ServiceTicketDTO, TicketMessageDTO, PageResult } from '../types/index'

export const ticketApi = {
  create: (data: { type: number; subject: string; content: string; priority?: number; orderId?: number }) =>
    post<number>('/aftersales/ticket', data),

  list: (params: { status?: number; pageNum?: number; pageSize?: number }) =>
    get<PageResult<ServiceTicketDTO>>('/aftersales/ticket/list', params),

  getDetail: (id: number) =>
    get<ServiceTicketDTO>(`/aftersales/ticket/${id}`),

  sendMessage: (id: number, content: string) =>
    post<number>(`/aftersales/ticket/${id}/message`, { content }),

  getMessages: (id: number) =>
    get<TicketMessageDTO[]>(`/aftersales/ticket/${id}/messages`),

  adminList: (params: { type?: number; status?: number; assignedTo?: string; pageNum?: number; pageSize?: number }) =>
    get<PageResult<ServiceTicketDTO>>('/aftersales/ticket/admin/list', params),

  adminDetail: (id: number) =>
    get<ServiceTicketDTO>(`/aftersales/ticket/admin/${id}`),

  adminAssign: (id: number, agent: string) =>
    put<void>(`/aftersales/ticket/admin/${id}/assign`, { agent }),

  adminReply: (id: number, agent: string, content: string) =>
    post<number>(`/aftersales/ticket/admin/${id}/reply`, { agent, content }),

  adminResolve: (id: number, agent?: string) =>
    put<void>(`/aftersales/ticket/admin/${id}/resolve`, { agent: agent || 'admin' }),

  adminClose: (id: number, agent?: string) =>
    put<void>(`/aftersales/ticket/admin/${id}/close`, { agent: agent || 'admin' }),
}
