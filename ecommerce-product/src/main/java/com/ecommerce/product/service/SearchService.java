package com.ecommerce.product.service;

import com.ecommerce.product.dto.HotSearchDTO;
import com.ecommerce.product.dto.SearchResultDTO;

import java.math.BigDecimal;
import java.util.List;

public interface SearchService {

    SearchResultDTO search(String keyword, Long categoryId, Long brandId,
                           BigDecimal minPrice, BigDecimal maxPrice,
                           String sortBy, int pageNum, int pageSize, Long userId);

    /**
     * Return up to 10 keyword suggestions matching the given prefix.
     */
    List<String> getSuggestions(String prefix);

    /**
     * Return top 10 hot searches.
     */
    List<HotSearchDTO> getHotSearches();

    /**
     * Return the user's recent 10 search keywords.
     */
    List<String> getUserHistory(Long userId);

    void clearUserHistory(Long userId);

    /**
     * Save keyword to search history and increment its hot search count.
     */
    void recordSearch(String keyword, Long userId);
}
