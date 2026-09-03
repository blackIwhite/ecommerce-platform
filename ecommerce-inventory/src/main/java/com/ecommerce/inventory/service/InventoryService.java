package com.ecommerce.inventory.service;

import com.ecommerce.api.inventory.dto.InventoryDeductRequest;
import com.ecommerce.api.inventory.dto.InventoryLockRequest;
import com.ecommerce.api.inventory.dto.InventoryUnlockRequest;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.inventory.dto.InventoryDTO;
import com.ecommerce.inventory.dto.InventoryLogDTO;
import com.ecommerce.inventory.dto.InventoryLogPageRequest;
import com.ecommerce.inventory.dto.InventoryPageRequest;
import com.ecommerce.inventory.dto.StockAdjustRequest;
import com.ecommerce.inventory.dto.StockBatchSetRequest;
import com.ecommerce.inventory.dto.StockSetRequest;

public interface InventoryService {

    Integer getStock(Long skuId);

    Boolean lockInventory(InventoryLockRequest request);

    Boolean unlockInventory(InventoryUnlockRequest request);

    Boolean deductInventory(InventoryDeductRequest request);

    Boolean setStock(StockSetRequest request);

    Boolean adjustStock(StockAdjustRequest request);

    Boolean batchSetStock(StockBatchSetRequest request);

    PageResult<InventoryDTO> pageInventory(InventoryPageRequest request);

    PageResult<InventoryLogDTO> pageInventoryLogs(InventoryLogPageRequest request);
}
