package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.product.dto.SearchResultDTO;
import com.ecommerce.product.entity.Brand;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.HotSearch;
import com.ecommerce.product.entity.SearchHistory;
import com.ecommerce.product.entity.Spu;
import com.ecommerce.product.mapper.BrandMapper;
import com.ecommerce.product.mapper.CategoryMapper;
import com.ecommerce.product.mapper.HotSearchMapper;
import com.ecommerce.product.mapper.SearchHistoryMapper;
import com.ecommerce.product.mapper.SpuMapper;
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
class SearchServiceImplTest {

    @Mock
    private SpuMapper spuMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private BrandMapper brandMapper;
    @Mock
    private SearchHistoryMapper searchHistoryMapper;
    @Mock
    private HotSearchMapper hotSearchMapper;

    @InjectMocks
    private SearchServiceImpl searchService;

    private Spu sampleSpu;
    private Category sampleCategory;
    private Brand sampleBrand;

    @BeforeEach
    void setUp() {
        sampleSpu = Spu.builder()
                .name("Test Phone")
                .categoryId(10L)
                .brandId(20L)
                .minPrice(new BigDecimal("999"))
                .status(1)
                .build();
        sampleSpu.setId(1L);

        sampleCategory = Category.builder().name("Electronics").parentId(0L).level(1).sort(0).status(1).build();
        sampleCategory.setId(10L);

        sampleBrand = Brand.builder().name("Apple").logo("apple.png").status(1).build();
        sampleBrand.setId(20L);
    }

    // ---- search ----

    @Test
    void search_withKeywordAndFilters_shouldReturnEnrichedItemsAndSuggestions() {
        Page<Spu> page = new Page<>(1, 10);
        page.setRecords(List.of(sampleSpu));
        page.setTotal(1);

        when(spuMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(List.of(sampleCategory));
        when(brandMapper.selectBatchIds(anyCollection())).thenReturn(List.of(sampleBrand));

        HotSearch hot1 = HotSearch.builder().keyword("phone case").searchCount(30).build();
        HotSearch hot2 = HotSearch.builder().keyword("phone holder").searchCount(12).build();
        when(hotSearchMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(hot1, hot2));

        SearchResultDTO result = searchService.search(" phone ", 10L, 20L,
                new BigDecimal("100"), new BigDecimal("2000"), "price_asc", 1, 10, 100L);

        assertEquals(" phone ", result.getKeyword());
        assertEquals(1, result.getTotal());
        assertEquals("Test Phone", result.getItems().getFirst().getName());
        assertEquals("Electronics", result.getItems().getFirst().getCategoryName());
        assertEquals("Apple", result.getItems().getFirst().getBrandName());
        assertEquals(List.of("phone case", "phone holder"), result.getSuggestions());
    }

    @Test
    void search_withoutKeyword_shouldSkipSuggestionsAndEnrichment() {
        Page<Spu> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        when(spuMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        SearchResultDTO result = searchService.search(null, null, null,
                null, null, null, 1, 10, null);

        assertEquals(0, result.getTotal());
        assertTrue(result.getItems().isEmpty());
        assertTrue(result.getSuggestions().isEmpty());
        verify(hotSearchMapper, never()).selectList(any(LambdaQueryWrapper.class));
        verify(categoryMapper, never()).selectBatchIds(anyCollection());
        verify(brandMapper, never()).selectBatchIds(anyCollection());
    }

    // ---- getSuggestions ----

    @Test
    void getSuggestions_blankPrefix_shouldReturnEmptyWithoutQuery() {
        assertTrue(searchService.getSuggestions("  ").isEmpty());
        verify(hotSearchMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void getSuggestions_withPrefix_shouldReturnHotKeywords() {
        HotSearch hot = HotSearch.builder().keyword("phone case").searchCount(30).build();
        when(hotSearchMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(hot));

        List<String> suggestions = searchService.getSuggestions("phone");

        assertEquals(List.of("phone case"), suggestions);
    }

    // ---- recordSearch ----

    @Test
    void recordSearch_existingHotKeyword_shouldIncrementCount() {
        HotSearch existing = HotSearch.builder().keyword("phone").searchCount(5).build();
        existing.setId(3L);
        when(hotSearchMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(hotSearchMapper.updateById(any(HotSearch.class))).thenReturn(1);

        searchService.recordSearch(" phone ", 100L);

        ArgumentCaptor<SearchHistory> historyCaptor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryMapper).insert(historyCaptor.capture());
        assertEquals("phone", historyCaptor.getValue().getKeyword());
        assertEquals(100L, historyCaptor.getValue().getUserId());
        assertNotNull(historyCaptor.getValue().getSearchTime());

        ArgumentCaptor<HotSearch> hotCaptor = ArgumentCaptor.forClass(HotSearch.class);
        verify(hotSearchMapper).updateById(hotCaptor.capture());
        assertEquals(6, hotCaptor.getValue().getSearchCount());
        verify(hotSearchMapper, never()).insert(any(HotSearch.class));
    }

    @Test
    void recordSearch_newKeyword_shouldInsertHotSearch() {
        when(hotSearchMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(hotSearchMapper.insert(any(HotSearch.class))).thenReturn(1);

        searchService.recordSearch("tablet", null);

        verify(searchHistoryMapper).insert(any(SearchHistory.class));
        ArgumentCaptor<HotSearch> captor = ArgumentCaptor.forClass(HotSearch.class);
        verify(hotSearchMapper).insert(captor.capture());
        HotSearch inserted = captor.getValue();
        assertEquals("tablet", inserted.getKeyword());
        assertEquals(1, inserted.getSearchCount());
        assertEquals(0, inserted.getIsManual());
        assertEquals(1, inserted.getStatus());
        verify(hotSearchMapper, never()).updateById(any(HotSearch.class));
    }

    @Test
    void recordSearch_blankKeyword_shouldDoNothing() {
        searchService.recordSearch("   ", 100L);

        verifyNoInteractions(searchHistoryMapper, hotSearchMapper);
    }

    // ---- getUserHistory ----

    @Test
    void getUserHistory_nullUserId_shouldReturnEmpty() {
        assertTrue(searchService.getUserHistory(null).isEmpty());
        verifyNoInteractions(searchHistoryMapper);
    }

    @Test
    void getUserHistory_withUserId_shouldReturnKeywords() {
        SearchHistory h1 = SearchHistory.builder().userId(100L).keyword("phone").build();
        SearchHistory h2 = SearchHistory.builder().userId(100L).keyword("tablet").build();
        when(searchHistoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(h1, h2));

        List<String> history = searchService.getUserHistory(100L);

        assertEquals(List.of("phone", "tablet"), history);
    }
}
