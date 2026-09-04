package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.product.dto.SkuDTO;
import com.ecommerce.product.dto.SpuCreateRequest;
import com.ecommerce.product.dto.SpuDTO;
import com.ecommerce.product.dto.SpuPageRequest;
import com.ecommerce.product.entity.Brand;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Sku;
import com.ecommerce.product.entity.Spu;
import com.ecommerce.product.mapper.BrandMapper;
import com.ecommerce.product.mapper.CategoryMapper;
import com.ecommerce.product.mapper.SkuMapper;
import com.ecommerce.product.mapper.SpuMapper;
import com.ecommerce.product.service.SkuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpuServiceImplTest {

    @Mock
    private SpuMapper spuMapper;
    @Mock
    private SkuMapper skuMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private BrandMapper brandMapper;
    @Mock
    private SkuService skuService;

    @InjectMocks
    private SpuServiceImpl spuService;

    private Spu sampleSpu;
    private Category sampleCategory;
    private Brand sampleBrand;

    @BeforeEach
    void setUp() {
        sampleSpu = Spu.builder()
                .name("Test Phone")
                .categoryId(10L)
                .brandId(20L)
                .description("A great phone")
                .images("[\"img1.jpg\"]")
                .status(1)
                .build();
        sampleSpu.setId(1L);

        sampleCategory = Category.builder().name("Electronics").parentId(0L).level(1).sort(0).status(1).build();
        sampleCategory.setId(10L);

        sampleBrand = Brand.builder().name("Apple").logo("apple.png").status(1).build();
        sampleBrand.setId(20L);
    }

    // ---- pageSpu ----

    @Test
    void pageSpu_withFilters_shouldReturnEnrichedPage() {
        SpuPageRequest request = SpuPageRequest.builder()
                .categoryId(10L).brandId(20L).status(1).pageNum(1).pageSize(10).build();

        Page<Spu> page = new Page<>(1, 10);
        page.setRecords(List.of(sampleSpu));
        page.setTotal(1);

        when(spuMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(List.of(sampleCategory));
        when(brandMapper.selectBatchIds(anyCollection())).thenReturn(List.of(sampleBrand));

        Page<SpuDTO> result = spuService.pageSpu(request);

        assertEquals(1, result.getTotal());
        SpuDTO dto = result.getRecords().getFirst();
        assertEquals("Test Phone", dto.getName());
        assertEquals("Electronics", dto.getCategoryName());
        assertEquals("Apple", dto.getBrandName());
    }

    @Test
    void pageSpu_withKeyword_shouldFilterByName() {
        SpuPageRequest request = SpuPageRequest.builder()
                .keyword("Phone").pageNum(1).pageSize(10).build();

        Page<Spu> page = new Page<>(1, 10);
        page.setRecords(List.of(sampleSpu));
        page.setTotal(1);

        when(spuMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(List.of(sampleCategory));
        when(brandMapper.selectBatchIds(anyCollection())).thenReturn(List.of(sampleBrand));

        Page<SpuDTO> result = spuService.pageSpu(request);

        assertEquals(1, result.getRecords().size());
    }

    @Test
    void pageSpu_emptyResult_shouldReturnEmptyPage() {
        SpuPageRequest request = SpuPageRequest.builder().pageNum(1).pageSize(10).build();

        Page<Spu> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(spuMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<SpuDTO> result = spuService.pageSpu(request);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(categoryMapper, never()).selectBatchIds(anyCollection());
        verify(brandMapper, never()).selectBatchIds(anyCollection());
    }

    // ---- getSpuDetail ----

    @Test
    void getSpuDetail_existingSpu_shouldReturnWithSkuList() {
        when(spuMapper.selectById(1L)).thenReturn(sampleSpu);
        when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(List.of(sampleCategory));
        when(brandMapper.selectBatchIds(anyCollection())).thenReturn(List.of(sampleBrand));

        SkuDTO skuDTO = SkuDTO.builder().skuId(100L).spuId(1L).skuName("Default").build();
        when(skuService.getSkuListBySpuId(1L)).thenReturn(List.of(skuDTO));

        SpuDTO result = spuService.getSpuDetail(1L);

        assertEquals("Test Phone", result.getName());
        assertEquals("Electronics", result.getCategoryName());
        assertEquals("Apple", result.getBrandName());
        assertEquals(1, result.getSkuList().size());
    }

    @Test
    void getSpuDetail_nonExistingSpu_shouldThrow() {
        when(spuMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> spuService.getSpuDetail(999L));
        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getCode(), ex.getCode());
    }

    // ---- createSpu ----

    @Test
    void createSpu_withSkuList_shouldInsertSpuAndSkus() {
        SpuCreateRequest request = SpuCreateRequest.builder()
                .name("New Phone")
                .categoryId(10L)
                .brandId(20L)
                .description("Desc")
                .images("[]")
                .skuList(List.of(
                        SpuCreateRequest.SkuCreateItem.builder()
                                .skuName("128GB").price(new BigDecimal("999")).stock(100).build()
                ))
                .build();

        when(spuMapper.insert(any(Spu.class))).thenAnswer(inv -> {
            Spu spu = inv.getArgument(0);
            spu.setId(1L);
            return 1;
        });
        when(skuMapper.insert(any(Sku.class))).thenReturn(1);

        Long id = spuService.createSpu(request);

        assertEquals(1L, id);
        verify(spuMapper).insert(any(Spu.class));
        verify(skuMapper).insert(any(Sku.class));
    }

    @Test
    void createSpu_withoutSkuList_shouldInsertSpuOnly() {
        SpuCreateRequest request = SpuCreateRequest.builder()
                .name("New Phone").categoryId(10L).brandId(20L).build();

        when(spuMapper.insert(any(Spu.class))).thenAnswer(inv -> {
            Spu spu = inv.getArgument(0);
            spu.setId(2L);
            return 1;
        });

        Long id = spuService.createSpu(request);

        assertEquals(2L, id);
        verify(spuMapper).insert(any(Spu.class));
        verify(skuMapper, never()).insert(any(Sku.class));
    }

    // ---- updateSpu ----

    @Test
    void updateSpu_nullId_shouldThrow() {
        SpuCreateRequest request = SpuCreateRequest.builder().name("Updated").build();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> spuService.updateSpu(request));
        assertEquals(ResultCode.PRODUCT_ID_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    void updateSpu_nonExistingSpu_shouldThrow() {
        SpuCreateRequest request = SpuCreateRequest.builder().id(999L).name("Updated").build();
        when(spuMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> spuService.updateSpu(request));
        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void updateSpu_withSkuList_shouldDeleteOldAndInsertNew() {
        SpuCreateRequest request = SpuCreateRequest.builder()
                .id(1L).name("Updated Phone").categoryId(10L).brandId(20L)
                .skuList(List.of(
                        SpuCreateRequest.SkuCreateItem.builder()
                                .skuName("256GB").price(new BigDecimal("1099")).stock(50).build()
                ))
                .build();

        when(spuMapper.selectById(1L)).thenReturn(sampleSpu);
        when(spuMapper.updateById(any(Spu.class))).thenReturn(1);
        when(skuMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(skuMapper.insert(any(Sku.class))).thenReturn(1);

        spuService.updateSpu(request);

        verify(spuMapper).updateById(any(Spu.class));
        verify(skuMapper).delete(any(LambdaQueryWrapper.class));
        verify(skuMapper).insert(any(Sku.class));
    }

    // ---- updateStatus ----

    @Test
    void updateStatus_existingSpu_shouldUpdate() {
        when(spuMapper.selectById(1L)).thenReturn(sampleSpu);
        when(spuMapper.updateById(any(Spu.class))).thenReturn(1);

        spuService.updateStatus(1L, 2);

        ArgumentCaptor<Spu> captor = ArgumentCaptor.forClass(Spu.class);
        verify(spuMapper).updateById(captor.capture());
        assertEquals(2, captor.getValue().getStatus());
    }

    @Test
    void updateStatus_nonExistingSpu_shouldThrow() {
        when(spuMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> spuService.updateStatus(999L, 1));
    }
}
