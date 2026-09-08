package com.ecommerce.marketing.controller;

import com.ecommerce.api.marketing.dto.PromotionDTO;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.marketing.service.PromotionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marketing")
@RequiredArgsConstructor
@Tag(name = "促销管理", description = "Promotion management APIs")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("/admin/promotion")
    public Result<Void> createPromotion(@RequestBody PromotionDTO dto) {
        promotionService.createPromotion(dto);
        return Result.success();
    }

    @PutMapping("/admin/promotion")
    public Result<Void> updatePromotion(@RequestBody PromotionDTO dto) {
        promotionService.updatePromotion(dto);
        return Result.success();
    }

    @DeleteMapping("/admin/promotion/{id}")
    public Result<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return Result.success();
    }

    @PutMapping("/admin/promotion/{id}/status")
    public Result<Void> updatePromotionStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        promotionService.updatePromotionStatus(id, body.get("status"));
        return Result.success();
    }

    @GetMapping("/admin/promotion/list")
    public Result<PageResult<PromotionDTO>> listPromotions(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        return Result.success(promotionService.listPromotions(pageNum, pageSize, type, status));
    }

    @GetMapping("/admin/promotion/{id}")
    public Result<PromotionDTO> getPromotion(@PathVariable Long id) {
        return Result.success(promotionService.getPromotion(id));
    }

    @GetMapping("/promotion/active")
    public Result<List<PromotionDTO>> listActivePromotions() {
        return Result.success(promotionService.listActivePromotions());
    }
}
