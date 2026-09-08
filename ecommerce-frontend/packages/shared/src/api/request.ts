import axios, { type AxiosInstance, type AxiosResponse } from 'axios'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    if (code === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(new Error(message || 'Error'))
  },
  (error) => {
    return Promise.reject(error)
  }
)

export default service

export const get = <T>(url: string, params?: any): Promise<T> =>
  service.get(url, { params }) as any

export const post = <T>(url: string, data?: any): Promise<T> =>
  service.post(url, data) as any

export const put = <T>(url: string, data?: any): Promise<T> =>
  service.put(url, data) as any

export const del = <T>(url: string, params?: any): Promise<T> =>
  service.delete(url, { params }) as any

export const download = async (url: string, params?: any, filename?: string): Promise<void> => {
  const response = await service.get(url, { params, responseType: 'blob' }) as unknown as Blob
  const blob = response instanceof Blob ? response : new Blob([response as any])
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename || 'download.xlsx'
  link.click()
  URL.revokeObjectURL(link.href)
}
