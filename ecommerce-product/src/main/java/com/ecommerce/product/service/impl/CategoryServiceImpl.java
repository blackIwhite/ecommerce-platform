package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.redis.util.RedisUtils;
import com.ecommerce.product.dto.CategoryCreateRequest;
import com.ecommerce.product.dto.CategoryDTO;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Spu;
import com.ecommerce.product.mapper.CategoryMapper;
import com.ecommerce.product.mapper.SpuMapper;
import com.ecommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final SpuMapper spuMapper;
    private final RedisUtils redisUtils;

    private static final String CATEGORY_TREE_CACHE_KEY = "product:category:tree";
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    public List<CategoryDTO> getCategoryTree() {
        try {
            Object cached = redisUtils.get(CATEGORY_TREE_CACHE_KEY);
            if (cached instanceof List) {
                return (List<CategoryDTO>) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to read category cache, falling back to database", e);
        }

        List<Category> allCategories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
        List<CategoryDTO> tree = buildTree(allCategories, 0L);

        try {
            redisUtils.set(CATEGORY_TREE_CACHE_KEY, tree, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to write category cache", e);
        }
        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CategoryCreateRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .parentId(request.getParentId())
                .level(request.getLevel() != null ? request.getLevel() : 1)
                .sort(request.getSort() != null ? request.getSort() : 0)
                .icon(request.getIcon())
                .status(1)
                .build();
        categoryMapper.insert(category);
        evictCache();
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CategoryCreateRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Category id is required");
        }
        Category existing = categoryMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND, "Category not found");
        }
        existing.setName(request.getName());
        existing.setParentId(request.getParentId());
        if (request.getLevel() != null) {
            existing.setLevel(request.getLevel());
        }
        if (request.getSort() != null) {
            existing.setSort(request.getSort());
        }
        if (request.getIcon() != null) {
            existing.setIcon(request.getIcon());
        }
        categoryMapper.updateById(existing);
        evictCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        LambdaQueryWrapper<Category> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(Category::getParentId, id);
        Long childCount = categoryMapper.selectCount(childWrapper);
        if (childCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Cannot delete category with children");
        }

        LambdaQueryWrapper<Spu> spuWrapper = new LambdaQueryWrapper<>();
        spuWrapper.eq(Spu::getCategoryId, id);
        Long spuCount = spuMapper.selectCount(spuWrapper);
        if (spuCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Cannot delete category bound to products");
        }

        categoryMapper.deleteById(id);
        evictCache();
    }

    private List<CategoryDTO> buildTree(List<Category> categories, Long parentId) {
        Map<Long, List<Category>> grouped = categories.stream()
                .collect(Collectors.groupingBy(Category::getParentId));

        return toDTOList(grouped.getOrDefault(parentId, Collections.emptyList()), grouped);
    }

    private List<CategoryDTO> toDTOList(List<Category> categories, Map<Long, List<Category>> grouped) {
        List<CategoryDTO> result = new ArrayList<>();
        for (Category cat : categories) {
            CategoryDTO dto = CategoryDTO.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .parentId(cat.getParentId())
                    .level(cat.getLevel())
                    .sort(cat.getSort())
                    .icon(cat.getIcon())
                    .status(cat.getStatus())
                    .children(toDTOList(grouped.getOrDefault(cat.getId(), Collections.emptyList()), grouped))
                    .build();
            result.add(dto);
        }
        return result;
    }

    private void evictCache() {
        redisUtils.delete(CATEGORY_TREE_CACHE_KEY);
    }
}
