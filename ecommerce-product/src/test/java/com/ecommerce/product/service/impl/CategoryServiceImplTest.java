package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.redis.util.RedisUtils;
import com.ecommerce.product.dto.CategoryCreateRequest;
import com.ecommerce.product.dto.CategoryDTO;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.mapper.CategoryMapper;
import com.ecommerce.product.mapper.SpuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private SpuMapper spuMapper;
    @Mock
    private RedisUtils redisUtils;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category rootCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        rootCategory = Category.builder()
                .name("Electronics").parentId(0L).level(1).sort(1).icon("elec.png").status(1).build();
        rootCategory.setId(1L);

        childCategory = Category.builder()
                .name("Phones").parentId(1L).level(2).sort(1).status(1).build();
        childCategory.setId(2L);
    }

    // ---- getCategoryTree ----

    @Test
    void getCategoryTree_cacheHit_shouldReturnCached() {
        List<CategoryDTO> cached = List.of(CategoryDTO.builder().id(1L).name("Cached").build());
        when(redisUtils.get("product:category:tree")).thenReturn(cached);

        List<CategoryDTO> result = categoryService.getCategoryTree();

        assertEquals(1, result.size());
        assertEquals("Cached", result.getFirst().getName());
        verify(categoryMapper, never()).selectList(any());
    }

    @Test
    void getCategoryTree_cacheMiss_shouldBuildTreeAndCache() {
        when(redisUtils.get("product:category:tree")).thenReturn(null);
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(rootCategory, childCategory));

        List<CategoryDTO> result = categoryService.getCategoryTree();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.getFirst().getName());
        assertEquals(1, result.getFirst().getChildren().size());
        assertEquals("Phones", result.getFirst().getChildren().getFirst().getName());
        verify(redisUtils).set(eq("product:category:tree"), any(), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getCategoryTree_emptyCategories_shouldReturnEmptyTree() {
        when(redisUtils.get("product:category:tree")).thenReturn(null);
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<CategoryDTO> result = categoryService.getCategoryTree();

        assertTrue(result.isEmpty());
    }

    // ---- createCategory ----

    @Test
    void createCategory_shouldInsertAndEvictCache() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Tablets").parentId(1L).level(2).sort(2).build();

        when(categoryMapper.insert(any(Category.class))).thenAnswer(inv -> {
            Category cat = inv.getArgument(0);
            cat.setId(3L);
            return 1;
        });

        Long id = categoryService.createCategory(request);

        assertEquals(3L, id);
        verify(categoryMapper).insert(any(Category.class));
        verify(redisUtils).delete("product:category:tree");
    }

    @Test
    void createCategory_defaultLevelAndSort_shouldUseDefaults() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("New Cat").parentId(0L).build();

        when(categoryMapper.insert(any(Category.class))).thenAnswer(inv -> {
            Category cat = inv.getArgument(0);
            cat.setId(4L);
            return 1;
        });

        categoryService.createCategory(request);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getLevel());
        assertEquals(0, captor.getValue().getSort());
    }

    // ---- updateCategory ----

    @Test
    void updateCategory_nullId_shouldThrow() {
        CategoryCreateRequest request = CategoryCreateRequest.builder().name("Updated").parentId(0L).build();

        assertThrows(BusinessException.class, () -> categoryService.updateCategory(request));
    }

    @Test
    void updateCategory_nonExisting_shouldThrow() {
        CategoryCreateRequest request = CategoryCreateRequest.builder().id(999L).name("Updated").parentId(0L).build();
        when(categoryMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> categoryService.updateCategory(request));
    }

    @Test
    void updateCategory_existing_shouldUpdateAndEvictCache() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .id(1L).name("Updated Electronics").parentId(0L).level(1).sort(5).icon("new.png").build();
        when(categoryMapper.selectById(1L)).thenReturn(rootCategory);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        categoryService.updateCategory(request);

        verify(categoryMapper).updateById(any(Category.class));
        verify(redisUtils).delete("product:category:tree");
    }

    // ---- deleteCategory ----

    @Test
    void deleteCategory_withChildren_shouldThrow() {
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class, () -> categoryService.deleteCategory(1L));
        verify(categoryMapper, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_withBoundProducts_shouldThrow() {
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(spuMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThrows(BusinessException.class, () -> categoryService.deleteCategory(1L));
        verify(categoryMapper, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_noChildrenNoProducts_shouldDeleteAndEvict() {
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(spuMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.deleteById(1L)).thenReturn(1);

        categoryService.deleteCategory(1L);

        verify(categoryMapper).deleteById(1L);
        verify(redisUtils).delete("product:category:tree");
    }
}
