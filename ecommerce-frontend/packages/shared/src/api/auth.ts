import { post, get } from './request'

export interface LoginParams {
  phone: string
  password: string
  smsCode?: string
  loginType: 'PASSWORD' | 'SMS'
}

export interface LoginResult {
  token: string
  refreshToken: string
  userId: number
  phone: string
  nickname: string
}

export interface UserInfo {
  userId: number
  phone: string
  nickname: string
  avatar: string
  roles?: string[]
}

export const authApi = {
  login: (data: LoginParams) => post<LoginResult>('/auth/login', data),

  register: (data: { phone: string; password: string; smsCode: string }) =>
    post<LoginResult>('/auth/register', data),

  logout: () => post<void>('/auth/logout'),

  getUserInfo: () => get<UserInfo>('/auth/info'),

  sendSms: (data: { phone: string }) => post<void>('/auth/sms/send', data),

  refresh: (refreshToken: string) =>
    post<LoginResult>('/auth/refresh', { refreshToken }),
}
