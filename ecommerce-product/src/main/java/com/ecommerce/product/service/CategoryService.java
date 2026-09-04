package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryCreateRequest;
import com.ecommerce.product.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {

    List<CategoryDTO> getCategoryTree();

    Long createCategory(CategoryCreateRequest request);

    void updateCategory(CategoryCreateRequest request);

    void deleteCategory(Long id);
}
