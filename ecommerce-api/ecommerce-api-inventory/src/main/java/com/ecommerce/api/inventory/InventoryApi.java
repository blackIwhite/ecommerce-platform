package com.ecommerce.api.inventory;

import com.ecommerce.api.inventory.dto.InventoryDeductRequest;
import com.ecommerce.api.inventory.dto.InventoryLockRequest;
import com.ecommerce.api.inventory.dto.InventoryUnlockRequest;
import com.ecommerce.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ecommerce-inventory")
public interface InventoryApi {

    @PostMapping("/inventory/lock")
    Result<Boolean> lockInventory(@RequestBody InventoryLockRequest request);

    @PostMapping("/inventory/unlock")
    Result<Boolean> unlockInventory(@RequestBody InventoryUnlockRequest request);

    @PostMapping("/inventory/deduct")
    Result<Boolean> deductInventory(@RequestBody InventoryDeductRequest request);

    @GetMapping("/inventory/stock/{skuId}")
    Result<Integer> getStock(@PathVariable("skuId") Long skuId);
}
