package com.ecommerce.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.inventory.dto.InventoryDeductRequest;
import com.ecommerce.api.inventory.dto.InventoryItem;
import com.ecommerce.api.inventory.dto.InventoryLockRequest;
import com.ecommerce.api.inventory.dto.InventoryUnlockRequest;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.redis.lock.RedisLock;
import com.ecommerce.common.redis.util.RedisUtils;
import com.ecommerce.inventory.dto.InventoryDTO;
import com.ecommerce.inventory.dto.InventoryLogDTO;
import com.ecommerce.inventory.dto.InventoryLogPageRequest;
import com.ecommerce.inventory.dto.InventoryPageRequest;
import com.ecommerce.inventory.dto.StockAdjustRequest;
import com.ecommerce.inventory.dto.StockBatchSetRequest;
import com.ecommerce.inventory.dto.StockSetRequest;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.InventoryLog;
import com.ecommerce.inventory.mapper.InventoryLogMapper;
import com.ecommerce.inventory.mapper.InventoryMapper;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final RedisLock redisLock;
    private final RedisUtils redisUtils;

    private static final String ORDER_LOCK_KEY_PREFIX = "inventory:lock:order:";
    private static final String STOCK_KEY_PREFIX = "inventory:stock:";
    private static final String SKU_LOCK_KEY_PREFIX = "inventory:sku:";
    private static final long LOCK_WAIT_SECONDS = 5;
    private static final long LOCK_LEASE_SECONDS = 30;

    @Override
    public Integer getStock(Long skuId) {
        String redisKey = STOCK_KEY_PREFIX + skuId;
        Object cached = redisUtils.get(redisKey);
        if (cached instanceof Number) {
            return ((Number) cached).intValue();
        }

        Inventory inventory = getInventoryBySkuId(skuId);
        if (inventory == null) {
            return 0;
        }

        redisUtils.set(redisKey, inventory.getAvailableStock(), 5, TimeUnit.MINUTES);
        return inventory.getAvailableStock();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean lockInventory(InventoryLockRequest request) {
        Long orderId = request.getOrderId();
        List<InventoryItem> items = request.getItems();
        List<LockedItem> processedItems = new ArrayList<>();

        try {
            for (InventoryItem item : items) {
                lockSingleSku(orderId, item, processedItems);
            }

            redisUtils.set(ORDER_LOCK_KEY_PREFIX + orderId, "LOCKED", 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            rollbackLockedItems(processedItems, orderId);
            throw e;
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlockInventory(InventoryUnlockRequest request) {
        Long orderId = request.getOrderId();

        Object lockStatus = redisUtils.get(ORDER_LOCK_KEY_PREFIX + orderId);
        if (lockStatus == null) {
            log.warn("No lock found for order: {}", orderId);
            return true;
        }

        List<InventoryLog> lockLogs = findLockLogsByOrderId(orderId);

        for (InventoryLog lockLog : lockLogs) {
            String skuLockKey = SKU_LOCK_KEY_PREFIX + lockLog.getSkuId();
            boolean locked = redisLock.tryLock(skuLockKey, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS);
            if (!locked) {
                throw new BusinessException(ResultCode.INVENTORY_LOCK_FAILED);
            }
            try {
                Inventory inventory = getInventoryBySkuId(lockLog.getSkuId());
                if (inventory == null) {
                    continue;
                }

                int beforeStock = inventory.getAvailableStock();
                int quantity = lockLog.getQuantity();
                inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
                inventory.setLockedStock(Math.max(0, inventory.getLockedStock() - quantity));

                inventoryMapper.updateById(inventory);

                syncStockToRedis(lockLog.getSkuId(), inventory.getAvailableStock());
                saveLog(lockLog.getSkuId(), orderId, "UNLOCK", quantity, beforeStock, inventory.getAvailableStock());
            } finally {
                redisLock.unlock(skuLockKey);
            }
        }

        redisUtils.delete(ORDER_LOCK_KEY_PREFIX + orderId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deductInventory(InventoryDeductRequest request) {
        Long orderId = request.getOrderId();

        List<InventoryLog> lockLogs = findLockLogsByOrderId(orderId);

        for (InventoryLog lockLog : lockLogs) {
            String skuLockKey = SKU_LOCK_KEY_PREFIX + lockLog.getSkuId();
            boolean locked = redisLock.tryLock(skuLockKey, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS);
            if (!locked) {
                throw new BusinessException(ResultCode.INVENTORY_LOCK_FAILED);
            }
            try {
                Inventory inventory = getInventoryBySkuId(lockLog.getSkuId());
                if (inventory == null) {
                    continue;
                }

                int beforeLocked = inventory.getLockedStock();
                int quantity = lockLog.getQuantity();
                inventory.setLockedStock(Math.max(0, inventory.getLockedStock() - quantity));

                inventoryMapper.updateById(inventory);

                syncStockToRedis(lockLog.getSkuId(), inventory.getAvailableStock());
                saveLog(lockLog.getSkuId(), orderId, "DEDUCT", quantity, beforeLocked, inventory.getLockedStock());
            } finally {
                redisLock.unlock(skuLockKey);
            }
        }

        redisUtils.delete(ORDER_LOCK_KEY_PREFIX + orderId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setStock(StockSetRequest request) {
        String skuLockKey = SKU_LOCK_KEY_PREFIX + request.getSkuId();
        boolean locked = redisLock.tryLock(skuLockKey, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS);
        if (!locked) {
            throw new BusinessException(ResultCode.INVENTORY_LOCK_FAILED);
        }
        try {
            Inventory inventory = getInventoryBySkuId(request.getSkuId());
            int beforeStock;

            if (inventory == null) {
                inventory = Inventory.builder()
                        .skuId(request.getSkuId())
                        .availableStock(request.getStock())
                        .lockedStock(0)
                        .version(0)
                        .build();
                inventoryMapper.insert(inventory);
                beforeStock = 0;
            } else {
                beforeStock = inventory.getAvailableStock();
                inventory.setAvailableStock(request.getStock());
                inventoryMapper.updateById(inventory);
            }

            syncStockToRedis(request.getSkuId(), request.getStock());
            saveLog(request.getSkuId(), null, "SET", request.getStock(), beforeStock, request.getStock());
        } finally {
            redisLock.unlock(skuLockKey);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean adjustStock(StockAdjustRequest request) {
        String skuLockKey = SKU_LOCK_KEY_PREFIX + request.getSkuId();
        boolean locked = redisLock.tryLock(skuLockKey, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS);
        if (!locked) {
            throw new BusinessException(ResultCode.INVENTORY_LOCK_FAILED);
        }
        try {
            Inventory inventory = getInventoryBySkuId(request.getSkuId());
            if (inventory == null) {
                throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND);
            }

            int beforeStock = inventory.getAvailableStock();
            int newStock = beforeStock + request.getAdjustQuantity();
            if (newStock < 0) {
                throw new BusinessException(ResultCode.INVENTORY_ADJUST_INVALID);
            }

            inventory.setAvailableStock(newStock);
            inventoryMapper.updateById(inventory);

            syncStockToRedis(request.getSkuId(), newStock);
            saveLog(request.getSkuId(), null, "ADJUST", request.getAdjustQuantity(), beforeStock, newStock);
        } finally {
            redisLock.unlock(skuLockKey);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchSetStock(StockBatchSetRequest request) {
        for (StockSetRequest item : request.getItems()) {
            setStock(item);
        }
        return true;
    }

    @Override
    public PageResult<InventoryDTO> pageInventory(InventoryPageRequest request) {
        Page<Inventory> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        if (request.getSkuId() != null) {
            wrapper.eq(Inventory::getSkuId, request.getSkuId());
        }
        wrapper.orderByDesc(Inventory::getCreateTime);

        Page<Inventory> result = inventoryMapper.selectPage(page, wrapper);

        List<InventoryDTO> dtoList = result.getRecords().stream()
                .map(this::toInventoryDTO)
                .toList();

        return PageResult.of(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public PageResult<InventoryLogDTO> pageInventoryLogs(InventoryLogPageRequest request) {
        Page<InventoryLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        if (request.getSkuId() != null) {
            wrapper.eq(InventoryLog::getSkuId, request.getSkuId());
        }
        if (StringUtils.hasText(request.getType())) {
            wrapper.eq(InventoryLog::getType, request.getType());
        }
        if (request.getOrderId() != null) {
            wrapper.eq(InventoryLog::getOrderId, request.getOrderId());
        }
        wrapper.orderByDesc(InventoryLog::getCreateTime);

        Page<InventoryLog> result = inventoryLogMapper.selectPage(page, wrapper);

        List<InventoryLogDTO> dtoList = result.getRecords().stream()
                .map(this::toInventoryLogDTO)
                .toList();

        return PageResult.of(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    // ---- Private helpers ----

    private void lockSingleSku(Long orderId, InventoryItem item, List<LockedItem> processedItems) {
        String redisKey = STOCK_KEY_PREFIX + item.getSkuId();

        Object cached = redisUtils.get(redisKey);
        if (cached instanceof Number) {
            int cachedStock = ((Number) cached).intValue();
            if (cachedStock < item.getQuantity()) {
                throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH,
                        "Insufficient stock for SKU: " + item.getSkuId());
            }
        }

        Long afterDecr = redisUtils.decrement(redisKey, item.getQuantity());
        if (afterDecr != null && afterDecr < 0) {
            redisUtils.increment(redisKey, item.getQuantity());
            throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH,
                    "Insufficient stock for SKU: " + item.getSkuId());
        }

        String skuLockKey = SKU_LOCK_KEY_PREFIX + item.getSkuId();
        boolean locked = redisLock.tryLock(skuLockKey, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS);
        if (!locked) {
            redisUtils.increment(redisKey, item.getQuantity());
            throw new BusinessException(ResultCode.INVENTORY_LOCK_FAILED);
        }

        try {
            Inventory inventory = getInventoryBySkuId(item.getSkuId());
            if (inventory == null) {
                redisUtils.increment(redisKey, item.getQuantity());
                throw new BusinessException(ResultCode.INVENTORY_NOT_FOUND,
                        "Inventory record not found for SKU: " + item.getSkuId());
            }

            if (inventory.getAvailableStock() < item.getQuantity()) {
                redisUtils.increment(redisKey, item.getQuantity());
                throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH,
                        "Insufficient stock for SKU: " + item.getSkuId());
            }

            int beforeStock = inventory.getAvailableStock();
            inventory.setAvailableStock(beforeStock - item.getQuantity());
            inventory.setLockedStock(inventory.getLockedStock() + item.getQuantity());

            int rows = inventoryMapper.updateById(inventory);
            if (rows == 0) {
                redisUtils.increment(redisKey, item.getQuantity());
                throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH,
                        "Version conflict for SKU: " + item.getSkuId());
            }

            syncStockToRedis(item.getSkuId(), inventory.getAvailableStock());
            saveLog(item.getSkuId(), orderId, "LOCK", item.getQuantity(), beforeStock, inventory.getAvailableStock());

            processedItems.add(new LockedItem(item.getSkuId(), item.getQuantity(), beforeStock));
        } finally {
            redisLock.unlock(skuLockKey);
        }
    }

    private void rollbackLockedItems(List<LockedItem> processedItems, Long orderId) {
        for (int i = processedItems.size() - 1; i >= 0; i--) {
            LockedItem item = processedItems.get(i);
            try {
                String skuLockKey = SKU_LOCK_KEY_PREFIX + item.skuId;
                boolean locked = redisLock.tryLock(skuLockKey, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS);
                if (!locked) {
                    log.error("Failed to acquire lock during rollback for SKU: {}", item.skuId);
                    continue;
                }
                try {
                    Inventory inventory = getInventoryBySkuId(item.skuId);
                    if (inventory != null) {
                        inventory.setAvailableStock(inventory.getAvailableStock() + item.quantity);
                        inventory.setLockedStock(Math.max(0, inventory.getLockedStock() - item.quantity));
                        inventoryMapper.updateById(inventory);
                        syncStockToRedis(item.skuId, inventory.getAvailableStock());
                    }
                } finally {
                    redisLock.unlock(skuLockKey);
                }

                redisUtils.increment(STOCK_KEY_PREFIX + item.skuId, item.quantity);
                log.info("Rolled back lock for SKU: {}, orderId: {}", item.skuId, orderId);
            } catch (Exception e) {
                log.error("Rollback failed for SKU: {}, orderId: {}", item.skuId, orderId, e);
            }
        }
    }

    private Inventory getInventoryBySkuId(Long skuId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getSkuId, skuId);
        return inventoryMapper.selectOne(wrapper);
    }

    private List<InventoryLog> findLockLogsByOrderId(Long orderId) {
        LambdaQueryWrapper<InventoryLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(InventoryLog::getOrderId, orderId)
                .eq(InventoryLog::getType, "LOCK");
        return inventoryLogMapper.selectList(logWrapper);
    }

    private void syncStockToRedis(Long skuId, int availableStock) {
        redisUtils.set(STOCK_KEY_PREFIX + skuId, availableStock);
    }

    private void saveLog(Long skuId, Long orderId, String type, Integer quantity, Integer beforeStock, Integer afterStock) {
        InventoryLog inventoryLog = InventoryLog.builder()
                .skuId(skuId)
                .orderId(orderId)
                .type(type)
                .quantity(quantity)
                .beforeStock(beforeStock)
                .afterStock(afterStock)
                .build();
        inventoryLogMapper.insert(inventoryLog);
    }

    private InventoryDTO toInventoryDTO(Inventory entity) {
        return InventoryDTO.builder()
                .id(entity.getId())
                .skuId(entity.getSkuId())
                .availableStock(entity.getAvailableStock())
                .lockedStock(entity.getLockedStock())
                .version(entity.getVersion())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private InventoryLogDTO toInventoryLogDTO(InventoryLog entity) {
        return InventoryLogDTO.builder()
                .id(entity.getId())
                .skuId(entity.getSkuId())
                .orderId(entity.getOrderId())
                .type(entity.getType())
                .quantity(entity.getQuantity())
                .beforeStock(entity.getBeforeStock())
                .afterStock(entity.getAfterStock())
                .createTime(entity.getCreateTime())
                .build();
    }

    private record LockedItem(Long skuId, int quantity, int beforeStock) {}
}
