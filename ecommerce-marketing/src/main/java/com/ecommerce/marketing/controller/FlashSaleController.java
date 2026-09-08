package com.ecommerce.marketing.controller;

import com.ecommerce.api.marketing.dto.PromotionDTO.FlashSaleItemDTO;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.marketing.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/marketing")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @GetMapping("/flash-sale/active")
    public Result<List<FlashSaleItemDTO>> getActiveItems(@RequestParam Long promotionId) {
        return Result.success(flashSaleService.getActiveItems(promotionId));
    }

    @GetMapping("/flash-sale/{id}")
    public Result<FlashSaleItemDTO> getItemDetail(@PathVariable Long id) {
        return Result.success(flashSaleService.getItemDetail(id));
    }
}
