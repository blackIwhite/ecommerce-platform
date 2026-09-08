import { beforeEach, describe, expect, it, vi } from 'vitest'
import { del, get, post } from '../request'
import { searchApi } from '../search'

vi.mock('../request', () => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn(),
  download: vi.fn(),
}))

describe('api/search', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('search() GETs /product/product/search with all filter params', async () => {
    const params = {
      keyword: 'phone',
      categoryId: 3,
      minPrice: 100,
      maxPrice: 5000,
      sortBy: 'price_asc',
      pageNum: 1,
      pageSize: 20,
    }
    await searchApi.search(params)
    expect(get).toHaveBeenCalledTimes(1)
    expect(get).toHaveBeenCalledWith('/product/product/search', params)
  })

  it('getSuggestions() wraps the prefix into a params object', async () => {
    await searchApi.getSuggestions('lap')
    expect(get).toHaveBeenCalledWith('/product/product/search/suggestions', { prefix: 'lap' })
  })

  it('getHotSearches() hits the hot-search endpoint without params', async () => {
    await searchApi.getHotSearches()
    expect(get).toHaveBeenCalledWith('/product/product/search/hot')
  })

  it('record() POSTs the keyword', async () => {
    await searchApi.record('mechanical keyboard')
    expect(post).toHaveBeenCalledWith('/product/product/search/record', {
      keyword: 'mechanical keyboard',
    })
  })

  it('clearHistory() DELETEs the search history', async () => {
    await searchApi.clearHistory()
    expect(del).toHaveBeenCalledWith('/product/product/search/history')
  })
})
