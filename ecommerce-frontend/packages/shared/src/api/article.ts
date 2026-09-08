import { get, post, put, del } from './request'
import type { PageResult, ArticleDTO, ArticleCategoryDTO } from '../types/index'

export const articleApi = {
  listArticles: (params: { pageNum?: number; pageSize?: number; categoryId?: number; status?: number }) =>
    get<PageResult<ArticleDTO>>('/product/product/admin/article/list', params),

  getArticle: (id: number) =>
    get<ArticleDTO>(`/product/product/admin/article/${id}`),

  createArticle: (data: ArticleDTO) =>
    post<void>('/product/product/admin/article', data),

  updateArticle: (data: ArticleDTO) =>
    put<void>('/product/product/admin/article', data),

  deleteArticle: (id: number) =>
    del<void>(`/product/product/admin/article/${id}`),

  listPublished: (params: { pageNum?: number; pageSize?: number; categoryId?: number }) =>
    get<PageResult<ArticleDTO>>('/product/product/article/list', params),

  getBySlug: (slug: string) =>
    get<ArticleDTO>(`/product/product/article/slug/${slug}`),

  listCategories: () =>
    get<ArticleCategoryDTO[]>('/product/product/admin/article-category/list'),

  createCategory: (data: ArticleCategoryDTO) =>
    post<void>('/product/product/admin/article-category', data),

  updateCategory: (data: ArticleCategoryDTO) =>
    put<void>('/product/product/admin/article-category', data),

  deleteCategory: (id: number) =>
    del<void>(`/product/product/admin/article-category/${id}`),
}
