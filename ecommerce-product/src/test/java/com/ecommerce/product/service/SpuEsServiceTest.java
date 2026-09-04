package com.ecommerce.product.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.product.document.SpuDocument;
import com.ecommerce.product.dto.SkuDTO;
import com.ecommerce.product.dto.SpuPageRequest;
import com.ecommerce.product.entity.Brand;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Spu;
import com.ecommerce.product.mapper.BrandMapper;
import com.ecommerce.product.mapper.CategoryMapper;
import com.ecommerce.product.mapper.SpuMapper;
import com.ecommerce.product.repository.SpuEsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpuEsServiceTest {

    @Mock
    private SpuEsRepository spuEsRepository;
    @Mock
    private ElasticsearchOperations esOperations;
    @Mock
    private SpuMapper spuMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private BrandMapper brandMapper;
    @Mock
    private SkuService skuService;

    @InjectMocks
    private SpuEsService spuEsService;

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
        sampleSpu.setCreateTime(LocalDateTime.of(2026, 9, 1, 10, 0));

        sampleCategory = Category.builder().name("Electronics").parentId(0L).level(1).sort(0).status(1).build();
        sampleCategory.setId(10L);

        sampleBrand = Brand.builder().name("Apple").logo("apple.png").status(1).build();
        sampleBrand.setId(20L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchSpuIds_withKeyword_shouldReturnIdsFromEs() {
        SpuPageRequest request = SpuPageRequest.builder()
                .keyword("Phone")
                .status(1)
                .build();
        request.setPageNum(1);
        request.setPageSize(10);

        SpuDocument doc = SpuDocument.builder().id(1L).name("Test Phone").build();
        SearchHit<SpuDocument> hit = mock(SearchHit.class);
        when(hit.getContent()).thenReturn(doc);

        SearchHits<SpuDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(List.of(hit));
        when(searchHits.getTotalHits()).thenReturn(1L);

        when(esOperations.search(any(org.springframework.data.elasticsearch.core.query.Query.class), eq(SpuDocument.class))).thenReturn(searchHits);

        PageResult<Long> result = spuEsService.searchSpuIds(request);

        assertEquals(1, result.getList().size());
        assertEquals(1L, result.getList().getFirst());
        assertEquals(1, result.getTotal());
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchSpuIds_noResults_shouldReturnEmpty() {
        SpuPageRequest request = SpuPageRequest.builder()
                .keyword("NonExistent")
                .build();
        request.setPageNum(1);
        request.setPageSize(10);

        SearchHits<SpuDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(Collections.emptyList());
        when(searchHits.getTotalHits()).thenReturn(0L);

        when(esOperations.search(any(org.springframework.data.elasticsearch.core.query.Query.class), eq(SpuDocument.class))).thenReturn(searchHits);

        PageResult<Long> result = spuEsService.searchSpuIds(request);

        assertTrue(result.getList().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    void indexSpu_existingSpu_shouldBuildAndSave() {
        when(spuMapper.selectById(1L)).thenReturn(sampleSpu);
        when(categoryMapper.selectById(10L)).thenReturn(sampleCategory);
        when(brandMapper.selectById(20L)).thenReturn(sampleBrand);
        when(skuService.getSkuListBySpuId(1L)).thenReturn(List.of(
                SkuDTO.builder().skuId(100L).spuId(1L).price(new BigDecimal("999")).build(),
                SkuDTO.builder().skuId(101L).spuId(1L).price(new BigDecimal("1299")).build()
        ));

        spuEsService.indexSpu(1L);

        ArgumentCaptor<SpuDocument> captor = ArgumentCaptor.forClass(SpuDocument.class);
        verify(spuEsRepository).save(captor.capture());
        SpuDocument doc = captor.getValue();
        assertEquals(1L, doc.getId());
        assertEquals("Test Phone", doc.getName());
        assertEquals("Electronics", doc.getCategoryName());
        assertEquals("Apple", doc.getBrandName());
        assertEquals(new BigDecimal("999"), doc.getMinPrice());
        assertEquals(new BigDecimal("1299"), doc.getMaxPrice());
    }

    @Test
    void indexSpu_nonExistingSpu_shouldDeleteFromEs() {
        when(spuMapper.selectById(999L)).thenReturn(null);

        spuEsService.indexSpu(999L);

        verify(spuEsRepository).deleteById(999L);
        verify(spuEsRepository, never()).save(any());
    }

    @Test
    void deleteSpu_shouldRemoveFromEs() {
        spuEsService.deleteSpu(1L);

        verify(spuEsRepository).deleteById(1L);
    }
}
