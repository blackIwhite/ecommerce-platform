package com.ecommerce.inventory.controller;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.inventory.dto.InventoryDTO;
import com.ecommerce.inventory.dto.InventoryLogDTO;
import com.ecommerce.inventory.dto.InventoryLogPageRequest;
import com.ecommerce.inventory.dto.InventoryPageRequest;
import com.ecommerce.inventory.dto.StockAdjustRequest;
import com.ecommerce.inventory.dto.StockBatchSetRequest;
import com.ecommerce.inventory.dto.StockSetRequest;
import com.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory/admin")
@RequiredArgsConstructor
@RequireLogin
public class InventoryAdminController {

    private final InventoryService inventoryService;

    @PostMapping("/stock/set")
    public Result<Boolean> setStock(@RequestBody @Valid StockSetRequest request) {
        return Result.success(inventoryService.setStock(request));
    }

    @PostMapping("/stock/adjust")
    public Result<Boolean> adjustStock(@RequestBody @Valid StockAdjustRequest request) {
        return Result.success(inventoryService.adjustStock(request));
    }

    @PostMapping("/stock/batch-set")
    public Result<Boolean> batchSetStock(@RequestBody @Valid StockBatchSetRequest request) {
        return Result.success(inventoryService.batchSetStock(request));
    }

    @GetMapping("/list")
    public Result<PageResult<InventoryDTO>> pageInventory(InventoryPageRequest request) {
        return Result.success(inventoryService.pageInventory(request));
    }

    @GetMapping("/logs")
    public Result<PageResult<InventoryLogDTO>> pageInventoryLogs(InventoryLogPageRequest request) {
        return Result.success(inventoryService.pageInventoryLogs(request));
    }
}
