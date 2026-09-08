import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { AxiosAdapter, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import service, { download, get, post } from '../request'

/**
 * Tests for the axios wrapper in request.ts.
 *
 * Strategy: instead of mocking the whole axios module, we install a fake
 * adapter on the real axios instance. This keeps the actual request/response
 * interceptors under test while guaranteeing no network traffic.
 */

let lastConfig: InternalAxiosRequestConfig | undefined
let responseData: unknown

const fakeAdapter: AxiosAdapter = async (config) => {
  lastConfig = config as InternalAxiosRequestConfig
  const response: AxiosResponse = {
    data: responseData,
    status: 200,
    statusText: 'OK',
    headers: {},
    config: config as InternalAxiosRequestConfig,
  }
  return response
}

describe('api/request', () => {
  beforeEach(() => {
    service.defaults.adapter = fakeAdapter
    lastConfig = undefined
    responseData = { code: 200, message: 'ok', data: null }
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  describe('response interceptor', () => {
    it('unwraps Result<T> and returns .data when code === 200', async () => {
      responseData = { code: 200, message: 'ok', data: { id: 1, name: 'widget' } }
      const result = await get<{ id: number; name: string }>('/demo')
      expect(result).toEqual({ id: 1, name: 'widget' })
    })

    it('passes raw response.data through for blob responseType', async () => {
      const blob = new Blob(['file-bytes'], { type: 'text/csv' })
      responseData = blob
      const result = await service.get('/export', { responseType: 'blob' })
      expect(result).toBe(blob)
    })

    it('rejects with Error(message) when code !== 200', async () => {
      responseData = { code: 500, message: 'boom', data: null }
      await expect(get('/demo')).rejects.toThrow('boom')
    })

    it('rejects with "Error" fallback when message is empty', async () => {
      responseData = { code: 500, message: '', data: null }
      await expect(get('/demo')).rejects.toThrow('Error')
    })

    it('clears the token and redirects to /login on code 401', async () => {
      localStorage.setItem('token', 'expired-token')
      // jsdom logs "Not implemented: navigation" for the location.href write;
      // silence it while asserting the observable side effects.
      vi.spyOn(console, 'error').mockImplementation(() => {})
      responseData = { code: 401, message: 'unauthorized', data: null }

      await expect(get('/demo')).rejects.toThrow('unauthorized')
      expect(localStorage.getItem('token')).toBeNull()
    })
  })

  describe('request interceptor', () => {
    it('attaches Authorization header when a token exists', async () => {
      localStorage.setItem('token', 'abc123')
      await get('/demo')
      expect(lastConfig?.headers?.Authorization).toBe('Bearer abc123')
    })

    it('does not attach Authorization header without a token', async () => {
      await get('/demo')
      expect(lastConfig?.headers?.Authorization).toBeUndefined()
    })
  })

  describe('http helpers', () => {
    it('post() sends the body and hits the given url', async () => {
      responseData = { code: 200, message: 'ok', data: { created: true } }
      const result = await post<{ created: boolean }>('/items', { name: 'x' })
      expect(result).toEqual({ created: true })
      expect(lastConfig?.url).toBe('/items')
      expect(lastConfig?.method).toBe('post')
      expect(JSON.parse(String(lastConfig?.data))).toEqual({ name: 'x' })
    })
  })

  describe('download()', () => {
    let createObjectURL: ReturnType<typeof vi.fn>
    let revokeObjectURL: ReturnType<typeof vi.fn>
    let clickedLink: HTMLAnchorElement | null

    beforeEach(() => {
      clickedLink = null
      createObjectURL = vi.fn(() => 'blob:mock-url')
      revokeObjectURL = vi.fn()
      Object.defineProperty(URL, 'createObjectURL', {
        value: createObjectURL,
        configurable: true,
        writable: true,
      })
      Object.defineProperty(URL, 'revokeObjectURL', {
        value: revokeObjectURL,
        configurable: true,
        writable: true,
      })
      vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (
        this: HTMLAnchorElement
      ) {
        clickedLink = this
      })
    })

    it('creates an object URL, triggers a click with the given filename, then revokes', async () => {
      const blob = new Blob(['report'], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })
      responseData = blob

      await download('/export/orders', { year: 2026 }, 'orders.xlsx')

      expect(lastConfig?.responseType).toBe('blob')
      expect(lastConfig?.url).toBe('/export/orders')
      expect(createObjectURL).toHaveBeenCalledWith(blob)
      expect(clickedLink).not.toBeNull()
      expect(clickedLink?.download).toBe('orders.xlsx')
      expect(clickedLink?.href).toBe('blob:mock-url')
      expect(revokeObjectURL).toHaveBeenCalledWith('blob:mock-url')
    })

    it('falls back to the default filename "download.xlsx"', async () => {
      responseData = new Blob(['x'])
      await download('/export/anything')
      expect(clickedLink?.download).toBe('download.xlsx')
    })

    it('wraps non-Blob responses into a Blob', async () => {
      responseData = 'plain-string-payload'
      await download('/export/text', undefined, 'out.txt')
      const arg = createObjectURL.mock.calls[0][0]
      expect(arg).toBeInstanceOf(Blob)
      // 'plain-string-payload' is 20 bytes; verifies the content was wrapped.
      expect((arg as Blob).size).toBe(20)
    })
  })
})
