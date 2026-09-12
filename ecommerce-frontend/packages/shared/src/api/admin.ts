import { get, post, put, del } from './request'
import type { PageResult, AdminUserDTO, RoleDTO, MenuDTO, PermissionDTO } from '../types/index'

export const adminApi = {
  login: (data: { username: string; password: string }) =>
    post<{ accessToken: string; refreshToken: string; adminUser: AdminUserDTO }>(
      '/auth/admin/login', data),

  getInfo: () =>
    get<AdminUserDTO>('/auth/admin/info'),

  getMenus: () =>
    get<MenuDTO[]>('/auth/admin/menus'),

  listAdmins: (params: { pageNum?: number; pageSize?: number }) =>
    get<PageResult<AdminUserDTO>>('/auth/admin/users', params),

  createAdmin: (data: { username: string; password: string; realName: string; phone?: string; email?: string; roleIds: number[] }) =>
    post<void>('/auth/admin/users', data),

  updateAdminStatus: (id: number, status: number) =>
    put<void>(`/auth/admin/users/${id}/status`, { status }),

  resetPassword: (id: number, newPassword: string) =>
    put<void>(`/auth/admin/users/${id}/password`, { newPassword }),

  deleteAdmin: (id: number) =>
    del<void>(`/auth/admin/users/${id}`),

  listRoles: () =>
    get<RoleDTO[]>('/auth/admin/roles'),

  createRole: (data: Partial<RoleDTO>) =>
    post<void>('/auth/admin/roles', data),

  updateRole: (id: number, data: Partial<RoleDTO>) =>
    put<void>(`/auth/admin/roles/${id}`, data),

  deleteRole: (id: number) =>
    del<void>(`/auth/admin/roles/${id}`),

  listMenus: () =>
    get<MenuDTO[]>('/auth/admin/menus/tree'),

  listPermissions: () =>
    get<PermissionDTO[]>('/auth/admin/menus/permissions'),
}
