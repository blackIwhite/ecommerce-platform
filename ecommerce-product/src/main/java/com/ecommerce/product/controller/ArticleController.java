package com.ecommerce.product.controller;

import com.ecommerce.common.core.annotation.AuditLog;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.product.dto.ArticleCategoryDTO;
import com.ecommerce.product.dto.ArticleDTO;
import com.ecommerce.product.service.ArticleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Tag(name = "内容管理", description = "CMS article and article category APIs")
public class ArticleController {

    private final ArticleService articleService;

    /** Published articles are visible without authentication. */
    private static final int STATUS_PUBLISHED = 1;

    // ==================== Public APIs ====================

    @GetMapping("/article/list")
    public Result<PageResult<ArticleDTO>> listPublishedArticles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(articleService.listArticles(pageNum, pageSize, categoryId, STATUS_PUBLISHED));
    }

    @GetMapping("/article/slug/{slug}")
    public Result<ArticleDTO> getArticleBySlug(@PathVariable String slug) {
        return Result.success(articleService.getArticleBySlug(slug));
    }

    @GetMapping("/article/{id}")
    public Result<ArticleDTO> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getArticle(id));
    }

    // ==================== Admin article APIs ====================

    @RequireLogin
    @GetMapping("/admin/article/list")
    public Result<PageResult<ArticleDTO>> listArticles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(articleService.listArticles(pageNum, pageSize, categoryId, status));
    }

    @RequireLogin
    @GetMapping("/admin/article/{id}")
    public Result<ArticleDTO> getArticleForAdmin(@PathVariable Long id) {
        return Result.success(articleService.getArticle(id));
    }

    @AuditLog(module = "内容管理", operation = "创建文章", description = "创建CMS文章")
    @RequireLogin
    @PostMapping("/admin/article")
    public Result<Void> createArticle(@Valid @RequestBody ArticleDTO dto) {
        articleService.createArticle(dto);
        return Result.success();
    }

    @AuditLog(module = "内容管理", operation = "更新文章", description = "更新CMS文章")
    @RequireLogin
    @PutMapping("/admin/article")
    public Result<Void> updateArticle(@Valid @RequestBody ArticleDTO dto) {
        articleService.updateArticle(dto);
        return Result.success();
    }

    @AuditLog(module = "内容管理", operation = "删除文章", description = "删除CMS文章")
    @RequireLogin
    @DeleteMapping("/admin/article/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    // ==================== Admin article category APIs ====================

    @RequireLogin
    @GetMapping("/admin/article-category/list")
    public Result<List<ArticleCategoryDTO>> listCategories() {
        return Result.success(articleService.listCategories());
    }

    @AuditLog(module = "内容管理", operation = "创建文章分类", description = "创建CMS文章分类")
    @RequireLogin
    @PostMapping("/admin/article-category")
    public Result<Void> createCategory(@Valid @RequestBody ArticleCategoryDTO dto) {
        articleService.createCategory(dto);
        return Result.success();
    }

    @AuditLog(module = "内容管理", operation = "更新文章分类", description = "更新CMS文章分类")
    @RequireLogin
    @PutMapping("/admin/article-category")
    public Result<Void> updateCategory(@Valid @RequestBody ArticleCategoryDTO dto) {
        articleService.updateCategory(dto);
        return Result.success();
    }

    @AuditLog(module = "内容管理", operation = "删除文章分类", description = "删除CMS文章分类")
    @RequireLogin
    @DeleteMapping("/admin/article-category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        articleService.deleteCategory(id);
        return Result.success();
    }
}
