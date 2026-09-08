package com.ecommerce.product.controller;

import com.ecommerce.common.core.result.Result;
import com.ecommerce.product.dto.HotSearchDTO;
import com.ecommerce.product.dto.SearchResultDTO;
import com.ecommerce.product.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/search")
@RequiredArgsConstructor
@Tag(name = "商品搜索", description = "Product search APIs")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "搜索商品")
    @GetMapping
    public Result<SearchResultDTO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(searchService.search(keyword, categoryId, brandId,
                minPrice, maxPrice, sortBy, pageNum, pageSize, userId));
    }

    @Operation(summary = "搜索联想词")
    @GetMapping("/suggestions")
    public Result<List<String>> getSuggestions(@RequestParam(required = false) String prefix) {
        return Result.success(searchService.getSuggestions(prefix));
    }

    @Operation(summary = "热门搜索")
    @GetMapping("/hot")
    public Result<List<HotSearchDTO>> getHotSearches() {
        return Result.success(searchService.getHotSearches());
    }

    @Operation(summary = "用户搜索历史")
    @GetMapping("/history")
    public Result<List<String>> getUserHistory(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(searchService.getUserHistory(userId));
    }

    @Operation(summary = "清空用户搜索历史")
    @DeleteMapping("/history")
    public Result<Void> clearUserHistory(@RequestHeader("X-User-Id") Long userId) {
        searchService.clearUserHistory(userId);
        return Result.success();
    }

    @Operation(summary = "记录搜索")
    @PostMapping("/record")
    public Result<Void> recordSearch(@RequestBody Map<String, String> body,
                                     @RequestHeader("X-User-Id") Long userId) {
        searchService.recordSearch(body.get("keyword"), userId);
        return Result.success();
    }
}
