package com.ecommerce.product.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.product.dto.ArticleCategoryDTO;
import com.ecommerce.product.dto.ArticleDTO;

import java.util.List;

/**
 * CMS article and article category management.
 */
public interface ArticleService {

    PageResult<ArticleDTO> listArticles(int pageNum, int pageSize, Long categoryId, Integer status);

    ArticleDTO getArticle(Long id);

    ArticleDTO getArticleBySlug(String slug);

    void createArticle(ArticleDTO dto);

    void updateArticle(ArticleDTO dto);

    void deleteArticle(Long id);

    List<ArticleCategoryDTO> listCategories();

    void createCategory(ArticleCategoryDTO dto);

    void updateCategory(ArticleCategoryDTO dto);

    void deleteCategory(Long id);
}
