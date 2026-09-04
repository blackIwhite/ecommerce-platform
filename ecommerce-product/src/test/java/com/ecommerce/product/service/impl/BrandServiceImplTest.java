package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.redis.util.RedisUtils;
import com.ecommerce.product.dto.BrandCreateRequest;
import com.ecommerce.product.dto.BrandDTO;
import com.ecommerce.product.dto.BrandPageRequest;
import com.ecommerce.product.entity.Brand;
import com.ecommerce.product.mapper.BrandMapper;
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
class BrandServiceImplTest {

    @Mock
    private BrandMapper brandMapper;
    @Mock
    private RedisUtils redisUtils;

    @InjectMocks
    private BrandServiceImpl brandService;

    private Brand sampleBrand;

    @BeforeEach
    void setUp() {
        sampleBrand = Brand.builder()
                .name("Apple").logo("apple.png").description("Think different").status(1).build();
        sampleBrand.setId(1L);
    }

    // ---- pageBrand ----

    @Test
    void pageBrand_withKeyword_shouldReturnFilteredPage() {
        BrandPageRequest request = BrandPageRequest.builder()
                .keyword("App").pageNum(1).pageSize(10).build();

        Page<Brand> page = new Page<>(1, 10);
        page.setRecords(List.of(sampleBrand));
        page.setTotal(1);

        when(brandMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<BrandDTO> result = brandService.pageBrand(request);

        assertEquals(1, result.getTotal());
        assertEquals("Apple", result.getList().getFirst().getName());
    }

    @Test
    void pageBrand_noKeyword_shouldReturnAll() {
        BrandPageRequest request = BrandPageRequest.builder().pageNum(1).pageSize(10).build();

        Page<Brand> page = new Page<>(1, 10);
        page.setRecords(List.of(sampleBrand));
        page.setTotal(1);

        when(brandMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<BrandDTO> result = brandService.pageBrand(request);

        assertEquals(1, result.getTotal());
    }

    @Test
    void pageBrand_emptyResult_shouldReturnEmpty() {
        BrandPageRequest request = BrandPageRequest.builder().pageNum(1).pageSize(10).build();

        Page<Brand> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(brandMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<BrandDTO> result = brandService.pageBrand(request);

        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    // ---- listAllEnabled ----

    @Test
    void listAllEnabled_cacheHit_shouldReturnCached() {
        List<BrandDTO> cached = List.of(BrandDTO.builder().id(1L).name("Cached").build());
        when(redisUtils.get("product:brand:all")).thenReturn(cached);

        List<BrandDTO> result = brandService.listAllEnabled();

        assertEquals(1, result.size());
        assertEquals("Cached", result.getFirst().getName());
        verify(brandMapper, never()).selectList(any());
    }

    @Test
    void listAllEnabled_cacheMiss_shouldQueryAndCache() {
        when(redisUtils.get("product:brand:all")).thenReturn(null);
        when(brandMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sampleBrand));

        List<BrandDTO> result = brandService.listAllEnabled();

        assertEquals(1, result.size());
        assertEquals("Apple", result.getFirst().getName());
        verify(redisUtils).set(eq("product:brand:all"), any(), eq(30L), eq(TimeUnit.MINUTES));
    }

    // ---- createBrand ----

    @Test
    void createBrand_shouldInsertAndEvictCache() {
        BrandCreateRequest request = BrandCreateRequest.builder()
                .name("Samsung").logo("samsung.png").description("Galaxy").build();

        when(brandMapper.insert(any(Brand.class))).thenAnswer(inv -> {
            Brand brand = inv.getArgument(0);
            brand.setId(2L);
            return 1;
        });

        Long id = brandService.createBrand(request);

        assertEquals(2L, id);
        verify(brandMapper).insert(any(Brand.class));
        verify(redisUtils).delete("product:brand:all");
    }

    // ---- updateBrand ----

    @Test
    void updateBrand_nullId_shouldThrow() {
        BrandCreateRequest request = BrandCreateRequest.builder().name("Updated").build();

        assertThrows(BusinessException.class, () -> brandService.updateBrand(request));
    }

    @Test
    void updateBrand_nonExisting_shouldThrow() {
        BrandCreateRequest request = BrandCreateRequest.builder().id(999L).name("Updated").build();
        when(brandMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> brandService.updateBrand(request));
    }

    @Test
    void updateBrand_existing_shouldUpdateAndEvictCache() {
        BrandCreateRequest request = BrandCreateRequest.builder()
                .id(1L).name("Updated Apple").logo("new.png").description("New desc").build();
        when(brandMapper.selectById(1L)).thenReturn(sampleBrand);
        when(brandMapper.updateById(any(Brand.class))).thenReturn(1);

        brandService.updateBrand(request);

        ArgumentCaptor<Brand> captor = ArgumentCaptor.forClass(Brand.class);
        verify(brandMapper).updateById(captor.capture());
        assertEquals("Updated Apple", captor.getValue().getName());
        verify(redisUtils).delete("product:brand:all");
    }

    // ---- deleteBrand ----

    @Test
    void deleteBrand_nonExisting_shouldThrow() {
        when(brandMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> brandService.deleteBrand(999L));
    }

    @Test
    void deleteBrand_existing_shouldDeleteAndEvictCache() {
        when(brandMapper.selectById(1L)).thenReturn(sampleBrand);
        when(brandMapper.deleteById(1L)).thenReturn(1);

        brandService.deleteBrand(1L);

        verify(brandMapper).deleteById(1L);
        verify(redisUtils).delete("product:brand:all");
    }
}
