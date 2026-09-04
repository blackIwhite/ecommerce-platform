package com.ecommerce.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.inventory.dto.InventoryDeductRequest;
import com.ecommerce.api.inventory.dto.InventoryItem;
import com.ecommerce.api.inventory.dto.InventoryLockRequest;
import com.ecommerce.api.inventory.dto.InventoryUnlockRequest;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.redis.lock.RedisLock;
import com.ecommerce.common.redis.util.RedisUtils;
import com.ecommerce.inventory.dto.InventoryPageRequest;
import com.ecommerce.inventory.dto.StockAdjustRequest;
import com.ecommerce.inventory.dto.StockSetRequest;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.InventoryLog;
import com.ecommerce.inventory.mapper.InventoryLogMapper;
import com.ecommerce.inventory.mapper.InventoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryMapper inventoryMapper;
    @Mock
    private InventoryLogMapper inventoryLogMapper;
    @Mock
    private RedisLock redisLock;
    @Mock
    private RedisUtils redisUtils;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory sampleInventory;

    @BeforeEach
    void setUp() {
        sampleInventory = Inventory.builder()
                .skuId(1001L)
                .availableStock(100)
                .lockedStock(10)
                .version(1)
                .build();
        sampleInventory.setId(1L);
    }

    // ---- getStock ----

    @Test
    void getStock_fromRedisCache_shouldReturnCachedValue() {
        when(redisUtils.get("inventory:stock:1001")).thenReturn(50);

        Integer stock = inventoryService.getStock(1001L);

        assertEquals(50, stock);
        verify(inventoryMapper, never()).selectOne(any());
    }

    @Test
    void getStock_cacheMiss_shouldQueryDbAndCache() {
        when(redisUtils.get("inventory:stock:1001")).thenReturn(null);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleInventory);

        Integer stock = inventoryService.getStock(1001L);

        assertEquals(100, stock);
        verify(redisUtils).set(eq("inventory:stock:1001"), eq(100), anyLong(), any());
    }

    @Test
    void getStock_noInventoryRecord_shouldReturnZero() {
        when(redisUtils.get("inventory:stock:9999")).thenReturn(null);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Integer stock = inventoryService.getStock(9999L);

        assertEquals(0, stock);
    }

    // ---- setStock ----

    @Test
    void setStock_existingInventory_shouldUpdate() {
        when(redisLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleInventory);
        when(inventoryMapper.updateById(any(Inventory.class))).thenReturn(1);

        StockSetRequest request = new StockSetRequest(1001L, 200);
        Boolean result = inventoryService.setStock(request);

        assertTrue(result);
        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryMapper).updateById(captor.capture());
        assertEquals(200, captor.getValue().getAvailableStock());
        verify(inventoryLogMapper).insert(any(InventoryLog.class));
        verify(redisLock).unlock(anyString());
    }

    @Test
    void setStock_newInventory_shouldInsert() {
        when(redisLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(inventoryMapper.insert(any(Inventory.class))).thenReturn(1);

        StockSetRequest request = new StockSetRequest(2002L, 50);
        Boolean result = inventoryService.setStock(request);

        assertTrue(result);
        verify(inventoryMapper).insert(any(Inventory.class));
        verify(redisLock).unlock(anyString());
    }

    @Test
    void setStock_lockFailed_shouldThrowException() {
        when(redisLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(false);

        StockSetRequest request = new StockSetRequest(1001L, 200);

        assertThrows(BusinessException.class, () -> inventoryService.setStock(request));
    }

    // ---- adjustStock ----

    @Test
    void adjustStock_positiveAdjustment_shouldIncreaseStock() {
        when(redisLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleInventory);
        when(inventoryMapper.updateById(any(Inventory.class))).thenReturn(1);

        StockAdjustRequest request = StockAdjustRequest.builder()
                .skuId(1001L).adjustQuantity(50).build();
        Boolean result = inventoryService.adjustStock(request);

        assertTrue(result);
        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryMapper).updateById(captor.capture());
        assertEquals(150, captor.getValue().getAvailableStock());
    }

    @Test
    void adjustStock_negativeResult_shouldThrowException() {
        when(redisLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleInventory);

        StockAdjustRequest request = StockAdjustRequest.builder()
                .skuId(1001L).adjustQuantity(-200).build();

        assertThrows(BusinessException.class, () -> inventoryService.adjustStock(request));
    }

    @Test
    void adjustStock_inventoryNotFound_shouldThrowException() {
        when(redisLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        StockAdjustRequest request = StockAdjustRequest.builder()
                .skuId(9999L).adjustQuantity(10).build();

        assertThrows(BusinessException.class, () -> inventoryService.adjustStock(request));
    }

    // ---- lockInventory ----

    @Test
    void lockInventory_success_shouldLockAndLog() {
        InventoryLockRequest request = InventoryLockRequest.builder()
                .orderId(100L)
                .items(List.of(InventoryItem.builder().skuId(1001L).quantity(5).build()))
                .build();

        when(redisUtils.get("inventory:stock:1001")).thenReturn(100);
        when(redisUtils.decrement(anyString(), anyLong())).thenReturn(95L);
        when(redisLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleInventory);
        when(inventoryMapper.updateById(any(Inventory.class))).thenReturn(1);

        Boolean result = inventoryService.lockInventory(request);

        assertTrue(result);
        ArgumentCaptor<Inventory> invCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryMapper).updateById(invCaptor.capture());
        assertEquals(95, invCaptor.getValue().getAvailableStock());
        assertEquals(15, invCaptor.getValue().getLockedStock());

        ArgumentCaptor<InventoryLog> logCaptor = ArgumentCaptor.forClass(InventoryLog.class);
        verify(inventoryLogMapper).insert(logCaptor.capture());
        assertEquals("LOCK", logCaptor.getValue().getType());
        assertEquals(5, logCaptor.getValue().getQuantity());
    }

    @Test
    void lockInventory_insufficientCachedStock_shouldThrow() {
        InventoryLockRequest request = InventoryLockRequest.builder()
                .orderId(100L)
                .items(List.of(InventoryItem.builder().skuId(1001L).quantity(200).build()))
                .build();

        when(redisUtils.get("inventory:stock:1001")).thenReturn(100);

        assertThrows(BusinessException.class, () -> inventoryService.lockInventory(request));
    }

    // ---- unlockInventory ----

    @Test
    void unlockInventory_withLockLogs_shouldRestoreStock() {
        InventoryUnlockRequest request = InventoryUnlockRequest.builder().orderId(100L).build();

        when(redisUtils.get("inventory:lock:order:100")).thenReturn("LOCKED");

        InventoryLog lockLog = InventoryLog.builder()
                .skuId(1001L).orderId(100L).type("LOCK").quantity(5)
                .beforeStock(100).afterStock(95).build();
        when(inventoryLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(lockLog));
        when(redisLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleInventory);
        when(inventoryMapper.updateById(any(Inventory.class))).thenReturn(1);

        Boolean result = inventoryService.unlockInventory(request);

        assertTrue(result);
        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryMapper).updateById(captor.capture());
        assertEquals(105, captor.getValue().getAvailableStock());
        assertEquals(5, captor.getValue().getLockedStock());
        verify(redisUtils).delete("inventory:lock:order:100");
    }

    @Test
    void unlockInventory_noLockStatus_shouldReturnTrue() {
        InventoryUnlockRequest request = InventoryUnlockRequest.builder().orderId(999L).build();
        when(redisUtils.get("inventory:lock:order:999")).thenReturn(null);

        Boolean result = inventoryService.unlockInventory(request);

        assertTrue(result);
        verify(inventoryMapper, never()).updateById(any(Inventory.class));
    }

    // ---- deductInventory ----

    @Test
    void deductInventory_shouldReduceLockedStock() {
        InventoryDeductRequest request = InventoryDeductRequest.builder().orderId(100L).build();

        InventoryLog lockLog = InventoryLog.builder()
                .skuId(1001L).orderId(100L).type("LOCK").quantity(5)
                .beforeStock(100).afterStock(95).build();
        when(inventoryLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(lockLog));
        when(redisLock.tryLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleInventory);
        when(inventoryMapper.updateById(any(Inventory.class))).thenReturn(1);

        Boolean result = inventoryService.deductInventory(request);

        assertTrue(result);
        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryMapper).updateById(captor.capture());
        assertEquals(5, captor.getValue().getLockedStock());
        verify(redisUtils).delete("inventory:lock:order:100");
    }

    // ---- pageInventory ----

    @Test
    void pageInventory_shouldReturnPageResult() {
        InventoryPageRequest request = new InventoryPageRequest(1001L, 1, 10);
        Page<Inventory> page = new Page<>(1, 10);
        page.setRecords(List.of(sampleInventory));
        page.setTotal(1);

        when(inventoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        var result = inventoryService.pageInventory(request);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(1001L, result.getList().getFirst().getSkuId());
    }

    // ---- pageInventoryLogs ----

    @Test
    void pageInventoryLogs_emptyResult_shouldReturnEmptyPage() {
        var request = new InventoryPageRequest(null, 1, 10);
        Page<Inventory> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(inventoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        var result = inventoryService.pageInventory(request);

        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }
}
