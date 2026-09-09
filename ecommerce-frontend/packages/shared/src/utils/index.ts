export function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`
}

export function formatDate(date: string): string {
  if (!date) return ''
  return new Date(date.replace(' ', 'T')).toLocaleString('zh-CN')
}

export function getToken(): string | null {
  return localStorage.getItem('token')
}

export function setToken(token: string): void {
  localStorage.setItem('token', token)
}

export function removeToken(): void {
  localStorage.removeItem('token')
}
