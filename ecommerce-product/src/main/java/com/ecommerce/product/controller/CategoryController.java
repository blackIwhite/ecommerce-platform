package com.ecommerce.product.controller;

import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.product.dto.CategoryCreateRequest;
import com.ecommerce.product.dto.CategoryDTO;
import com.ecommerce.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/category/tree")
    public Result<List<CategoryDTO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @RequireLogin
    @PostMapping("/admin/category")
    public Result<Long> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return Result.success(categoryService.createCategory(request));
    }

    @RequireLogin
    @PutMapping("/admin/category")
    public Result<Void> updateCategory(@Valid @RequestBody CategoryCreateRequest request) {
        categoryService.updateCategory(request);
        return Result.success();
    }

    @RequireLogin
    @DeleteMapping("/admin/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
