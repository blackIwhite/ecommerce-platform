package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.product.dto.ArticleCategoryDTO;
import com.ecommerce.product.dto.ArticleDTO;
import com.ecommerce.product.entity.Article;
import com.ecommerce.product.entity.ArticleCategory;
import com.ecommerce.product.mapper.ArticleCategoryMapper;
import com.ecommerce.product.mapper.ArticleMapper;
import com.ecommerce.product.service.ArticleService;
import com.ecommerce.product.service.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper articleCategoryMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    /** 0=draft, 1=published, 2=archived */
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int CATEGORY_STATUS_ENABLED = 1;
    private static final long ROOT_PARENT_ID = 0L;

    @Override
    public PageResult<ArticleDTO> listArticles(int pageNum, int pageSize, Long categoryId, Integer status) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        wrapper.orderByAsc(Article::getSortOrder).orderByDesc(Article::getCreateTime);

        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);
        List<ArticleDTO> dtoList = enrichArticleList(articlePage.getRecords());
        return PageResult.of(dtoList, articlePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public ArticleDTO getArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        return enrichArticleList(List.of(article)).get(0);
    }

    @Override
    public ArticleDTO getArticleBySlug(String slug) {
        if (!StringUtils.hasText(slug)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Article slug is required");
        }
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getSlug, slug);
        Article article = articleMapper.selectList(wrapper).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.ARTICLE_NOT_FOUND));
        return enrichArticleList(List.of(article)).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createArticle(ArticleDTO dto) {
        validateCategoryExists(dto.getCategoryId());

        int status = dto.getStatus() != null ? dto.getStatus() : STATUS_DRAFT;
        String slug = StringUtils.hasText(dto.getSlug())
                ? normalizeSlug(dto.getSlug())
                : buildSlugFromTitle(dto.getTitle());
        if (StringUtils.hasText(slug)) {
            ensureSlugUnique(slug, null);
        }

        String content = dto.getContent();
        String summary = dto.getSummary();
        if (content != null && sensitiveWordFilter.containsSensitiveWord(content)) {
            content = sensitiveWordFilter.filter(content);
        }
        if (summary != null && sensitiveWordFilter.containsSensitiveWord(summary)) {
            summary = sensitiveWordFilter.filter(summary);
        }

        Article article = Article.builder()
                .title(dto.getTitle())
                .slug(slug)
                .content(content)
                .summary(summary)
                .coverImage(dto.getCoverImage())
                .categoryId(dto.getCategoryId())
                .author(dto.getAuthor())
                .status(status)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .viewCount(0)
                .publishTime(resolvePublishTime(status, dto.getPublishTime()))
                .build();
        articleMapper.insert(article);
        dto.setId(article.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Article id is required");
        }
        Article existing = articleMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        validateCategoryExists(dto.getCategoryId());

        if (StringUtils.hasText(dto.getSlug())) {
            String slug = normalizeSlug(dto.getSlug());
            if (!slug.equals(existing.getSlug())) {
                ensureSlugUnique(slug, dto.getId());
            }
            existing.setSlug(slug);
        }

        existing.setTitle(dto.getTitle());
        existing.setContent(dto.getContent());
        existing.setSummary(dto.getSummary());
        existing.setCoverImage(dto.getCoverImage());
        existing.setCategoryId(dto.getCategoryId());
        existing.setAuthor(dto.getAuthor());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        if (dto.getSortOrder() != null) {
            existing.setSortOrder(dto.getSortOrder());
        }
        LocalDateTime publishTime = dto.getPublishTime() != null ? dto.getPublishTime() : existing.getPublishTime();
        existing.setPublishTime(resolvePublishTime(existing.getStatus(), publishTime));

        articleMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        articleMapper.deleteById(id);
    }

    @Override
    public List<ArticleCategoryDTO> listCategories() {
        LambdaQueryWrapper<ArticleCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ArticleCategory::getSortOrder).orderByAsc(ArticleCategory::getId);
        List<ArticleCategory> allCategories = articleCategoryMapper.selectList(wrapper);

        Map<Long, List<ArticleCategory>> grouped = allCategories.stream()
                .collect(Collectors.groupingBy(category -> category.getParentId() == null
                        ? ROOT_PARENT_ID
                        : category.getParentId()));

        return toCategoryDTOList(grouped.getOrDefault(ROOT_PARENT_ID, Collections.emptyList()), grouped);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(ArticleCategoryDTO dto) {
        if (StringUtils.hasText(dto.getCode())) {
            ensureCodeUnique(normalizeCode(dto.getCode()), null);
        }
        validateParentCategoryExists(dto.getParentId());

        ArticleCategory category = ArticleCategory.builder()
                .name(dto.getName())
                .code(StringUtils.hasText(dto.getCode()) ? normalizeCode(dto.getCode()) : dto.getCode())
                .parentId(dto.getParentId() != null ? dto.getParentId() : ROOT_PARENT_ID)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status(dto.getStatus() != null ? dto.getStatus() : CATEGORY_STATUS_ENABLED)
                .build();
        articleCategoryMapper.insert(category);
        dto.setId(category.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(ArticleCategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Article category id is required");
        }
        ArticleCategory existing = articleCategoryMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.ARTICLE_CATEGORY_NOT_FOUND);
        }
        if (StringUtils.hasText(dto.getCode())) {
            String code = normalizeCode(dto.getCode());
            if (!code.equals(existing.getCode())) {
                ensureCodeUnique(code, dto.getId());
            }
            existing.setCode(code);
        }
        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(dto.getId())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "Article category cannot be its own parent");
            }
            validateParentCategoryExists(dto.getParentId());
            existing.setParentId(dto.getParentId());
        }
        existing.setName(dto.getName());
        if (dto.getSortOrder() != null) {
            existing.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        articleCategoryMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        ArticleCategory existing = articleCategoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.ARTICLE_CATEGORY_NOT_FOUND);
        }

        LambdaQueryWrapper<ArticleCategory> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(ArticleCategory::getParentId, id);
        if (articleCategoryMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Cannot delete article category with children");
        }

        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getCategoryId, id);
        if (articleMapper.selectCount(articleWrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Cannot delete article category bound to articles");
        }

        articleCategoryMapper.deleteById(id);
    }

    private List<ArticleDTO> enrichArticleList(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ArticleCategory> categoryMap = categoryIds.isEmpty()
                ? Collections.<Long, ArticleCategory>emptyMap()
                : articleCategoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(ArticleCategory::getId, Function.identity()));

        return articles.stream().map(article -> {
            ArticleDTO dto = toArticleDTO(article);
            ArticleCategory category = categoryMap.get(article.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private ArticleDTO toArticleDTO(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .content(article.getContent())
                .summary(article.getSummary())
                .coverImage(article.getCoverImage())
                .categoryId(article.getCategoryId())
                .author(article.getAuthor())
                .status(article.getStatus())
                .sortOrder(article.getSortOrder())
                .viewCount(article.getViewCount())
                .publishTime(article.getPublishTime())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();
    }

    private List<ArticleCategoryDTO> toCategoryDTOList(List<ArticleCategory> categories,
                                                       Map<Long, List<ArticleCategory>> grouped) {
        List<ArticleCategoryDTO> result = new ArrayList<>();
        for (ArticleCategory category : categories) {
            result.add(ArticleCategoryDTO.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .code(category.getCode())
                    .parentId(category.getParentId())
                    .sortOrder(category.getSortOrder())
                    .status(category.getStatus())
                    .createTime(category.getCreateTime())
                    .children(toCategoryDTOList(
                            grouped.getOrDefault(category.getId(), Collections.emptyList()), grouped))
                    .build());
        }
        return result;
    }

    private void validateCategoryExists(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        if (articleCategoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(ResultCode.ARTICLE_CATEGORY_NOT_FOUND);
        }
    }

    private void validateParentCategoryExists(Long parentId) {
        if (parentId == null || parentId == ROOT_PARENT_ID) {
            return;
        }
        if (articleCategoryMapper.selectById(parentId) == null) {
            throw new BusinessException(ResultCode.ARTICLE_CATEGORY_NOT_FOUND);
        }
    }

    private void ensureSlugUnique(String slug, Long excludeId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getSlug, slug);
        if (excludeId != null) {
            wrapper.ne(Article::getId, excludeId);
        }
        if (articleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Article slug already exists");
        }
    }

    private void ensureCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<ArticleCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleCategory::getCode, code);
        if (excludeId != null) {
            wrapper.ne(ArticleCategory::getId, excludeId);
        }
        if (articleCategoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Article category code already exists");
        }
    }

    private LocalDateTime resolvePublishTime(Integer status, LocalDateTime publishTime) {
        if (status != null && status == STATUS_PUBLISHED && publishTime == null) {
            return LocalDateTime.now();
        }
        return publishTime;
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String normalizeCode(String code) {
        return code.trim().toLowerCase(Locale.ROOT);
    }

    private String buildSlugFromTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return null;
        }
        String slug = normalizeSlug(title);
        return slug.isEmpty() ? "article-" + System.currentTimeMillis() : slug;
    }
}
