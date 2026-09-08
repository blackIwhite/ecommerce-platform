package com.ecommerce.marketing.controller;

import com.ecommerce.api.marketing.dto.PromotionDTO.FlashSaleItemDTO;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.marketing.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/marketing")
@RequiredArgsConstructor
public class FlashSaleAdminController {

    private final FlashSaleService flashSaleService;

    @GetMapping("/admin/flash-sale/list")
    public Result<PageResult<FlashSaleItemDTO>> listItems(
            @RequestParam(required = false) Long promotionId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(flashSaleService.listItems(promotionId, pageNum, pageSize));
    }

    @PostMapping("/admin/flash-sale")
    public Result<Void> addItem(@RequestBody FlashSaleItemDTO dto) {
        flashSaleService.addItem(dto);
        return Result.success();
    }

    @PutMapping("/admin/flash-sale")
    public Result<Void> updateItem(@RequestBody FlashSaleItemDTO dto) {
        flashSaleService.updateItem(dto);
        return Result.success();
    }

    @DeleteMapping("/admin/flash-sale/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        flashSaleService.deleteItem(id);
        return Result.success();
    }
}
