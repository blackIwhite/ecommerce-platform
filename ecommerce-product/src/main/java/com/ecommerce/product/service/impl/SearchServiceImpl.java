package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.product.dto.HotSearchDTO;
import com.ecommerce.product.dto.SearchResultDTO;
import com.ecommerce.product.dto.SpuDTO;
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
import com.ecommerce.product.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SpuMapper spuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final HotSearchMapper hotSearchMapper;

    @Override
    public SearchResultDTO search(String keyword, Long categoryId, Long brandId,
                                  BigDecimal minPrice, BigDecimal maxPrice,
                                  String sortBy, int pageNum, int pageSize, Long userId) {
        Page<Spu> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            // Spu has no subtitle column; match against name and description
            wrapper.and(w -> w.like(Spu::getName, kw).or().like(Spu::getDescription, kw));
        }
        if (categoryId != null) {
            wrapper.eq(Spu::getCategoryId, categoryId);
        }
        if (brandId != null) {
            wrapper.eq(Spu::getBrandId, brandId);
        }
        if (minPrice != null) {
            wrapper.ge(Spu::getMinPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Spu::getMinPrice, maxPrice);
        }
        applySort(wrapper, sortBy);

        Page<Spu> spuPage = spuMapper.selectPage(page, wrapper);
        List<SpuDTO> items = enrichSpuDTOList(spuPage.getRecords());
        List<String> suggestions = StringUtils.hasText(keyword)
                ? getSuggestions(keyword.trim())
                : Collections.emptyList();

        return SearchResultDTO.builder()
                .keyword(keyword)
                .total(spuPage.getTotal())
                .items(items)
                .suggestions(suggestions)
                .build();
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<HotSearch> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(HotSearch::getKeyword, prefix.trim())
                .orderByDesc(HotSearch::getSearchCount)
                .last("LIMIT 10");
        return hotSearchMapper.selectList(wrapper).stream()
                .map(HotSearch::getKeyword)
                .collect(Collectors.toList());
    }

    @Override
    public List<HotSearchDTO> getHotSearches() {
        LambdaQueryWrapper<HotSearch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotSearch::getStatus, 1)
                .orderByDesc(HotSearch::getSortOrder)
                .orderByDesc(HotSearch::getSearchCount)
                .last("LIMIT 10");
        return hotSearchMapper.selectList(wrapper).stream()
                .map(hot -> HotSearchDTO.builder()
                        .id(hot.getId())
                        .keyword(hot.getKeyword())
                        .searchCount(hot.getSearchCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserHistory(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getSearchTime)
                .orderByDesc(SearchHistory::getId)
                .last("LIMIT 10");
        return searchHistoryMapper.selectList(wrapper).stream()
                .map(SearchHistory::getKeyword)
                .collect(Collectors.toList());
    }

    @Override
    public void clearUserHistory(Long userId) {
        if (userId == null) {
            return;
        }
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId);
        searchHistoryMapper.delete(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordSearch(String keyword, Long userId) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String kw = keyword.trim();

        SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .keyword(kw)
                .searchTime(LocalDateTime.now())
                .build();
        searchHistoryMapper.insert(history);

        LambdaQueryWrapper<HotSearch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotSearch::getKeyword, kw).last("LIMIT 1");
        HotSearch hotSearch = hotSearchMapper.selectOne(wrapper);
        if (hotSearch != null) {
            hotSearch.setSearchCount((hotSearch.getSearchCount() != null ? hotSearch.getSearchCount() : 0) + 1);
            hotSearchMapper.updateById(hotSearch);
        } else {
            HotSearch newHot = HotSearch.builder()
                    .keyword(kw)
                    .searchCount(1)
                    .isManual(0)
                    .sortOrder(0)
                    .status(1)
                    .build();
            hotSearchMapper.insert(newHot);
        }
    }

    private void applySort(LambdaQueryWrapper<Spu> wrapper, String sortBy) {
        if (sortBy == null) {
            wrapper.orderByDesc(Spu::getCreateTime);
            return;
        }
        switch (sortBy) {
            case "price_asc" -> wrapper.orderByAsc(Spu::getMinPrice);
            case "price_desc" -> wrapper.orderByDesc(Spu::getMinPrice);
            case "sales_desc" -> wrapper.orderByDesc(Spu::getSalesCount);
            default -> wrapper.orderByDesc(Spu::getCreateTime);
        }
    }

    private List<SpuDTO> enrichSpuDTOList(List<Spu> spuList) {
        if (spuList.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> categoryIds = spuList.stream()
                .map(Spu::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> brandIds = spuList.stream()
                .map(Spu::getBrandId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, Category> categoryMap = categoryIds.isEmpty()
                ? Collections.<Long, Category>emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Function.identity()));

        Map<Long, Brand> brandMap = brandIds.isEmpty()
                ? Collections.<Long, Brand>emptyMap()
                : brandMapper.selectBatchIds(brandIds).stream()
                        .collect(Collectors.toMap(Brand::getId, Function.identity()));

        return spuList.stream().map(spu -> {
            SpuDTO dto = toSpuDTO(spu);
            Category category = categoryMap.get(spu.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
            Brand brand = brandMap.get(spu.getBrandId());
            if (brand != null) {
                dto.setBrandName(brand.getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private SpuDTO toSpuDTO(Spu spu) {
        return SpuDTO.builder()
                .spuId(spu.getId())
                .name(spu.getName())
                .categoryId(spu.getCategoryId())
                .brandId(spu.getBrandId())
                .description(spu.getDescription())
                .images(spu.getImages())
                .status(spu.getStatus())
                .salesCount(spu.getSalesCount())
                .viewCount(spu.getViewCount())
                .minPrice(spu.getMinPrice())
                .avgRating(spu.getAvgRating())
                .reviewCount(spu.getReviewCount())
                .createTime(spu.getCreateTime())
                .build();
    }
}
