package com.ecommerce.inventory.controller;

import com.ecommerce.api.inventory.dto.InventoryDeductRequest;
import com.ecommerce.api.inventory.dto.InventoryLockRequest;
import com.ecommerce.api.inventory.dto.InventoryUnlockRequest;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/stock/{skuId}")
    public Result<Integer> getStock(@PathVariable Long skuId) {
        return Result.success(inventoryService.getStock(skuId));
    }

    @PostMapping("/lock")
    public Result<Boolean> lockInventory(@RequestBody InventoryLockRequest request) {
        return Result.success(inventoryService.lockInventory(request));
    }

    @PostMapping("/unlock")
    public Result<Boolean> unlockInventory(@RequestBody InventoryUnlockRequest request) {
        return Result.success(inventoryService.unlockInventory(request));
    }

    @PostMapping("/deduct")
    public Result<Boolean> deductInventory(@RequestBody InventoryDeductRequest request) {
        return Result.success(inventoryService.deductInventory(request));
    }
}
