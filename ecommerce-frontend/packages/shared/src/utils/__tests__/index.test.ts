import { beforeEach, describe, expect, it } from 'vitest'
import { formatDate, formatPrice, getToken, removeToken, setToken } from '../index'

describe('utils', () => {
  describe('formatPrice', () => {
    it('formats an integer price with two decimals and the ¥ prefix', () => {
      expect(formatPrice(99)).toBe('¥99.00')
    })

    it('keeps exactly two decimal places', () => {
      expect(formatPrice(0.5)).toBe('¥0.50')
      expect(formatPrice(12.345)).toBe('¥12.35')
    })

    it('handles zero', () => {
      expect(formatPrice(0)).toBe('¥0.00')
    })
  })

  describe('formatDate', () => {
    it('returns a non-empty localized string containing the year', () => {
      const result = formatDate('2026-03-15T10:30:00Z')
      expect(typeof result).toBe('string')
      expect(result.length).toBeGreaterThan(0)
      expect(result).toContain('2026')
    })
  })

  describe('token helpers', () => {
    beforeEach(() => {
      localStorage.clear()
    })

    it('getToken() returns null when no token is stored', () => {
      expect(getToken()).toBeNull()
    })

    it('setToken() stores the token in localStorage', () => {
      setToken('jwt-token-value')
      expect(localStorage.getItem('token')).toBe('jwt-token-value')
      expect(getToken()).toBe('jwt-token-value')
    })

    it('removeToken() clears the stored token', () => {
      setToken('jwt-token-value')
      removeToken()
      expect(getToken()).toBeNull()
    })
  })
})
