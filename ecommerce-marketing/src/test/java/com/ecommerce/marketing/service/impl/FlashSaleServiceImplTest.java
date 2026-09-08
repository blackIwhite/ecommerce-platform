package com.ecommerce.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.api.marketing.dto.PromotionDTO.FlashSaleItemDTO;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.marketing.entity.FlashSaleItem;
import com.ecommerce.marketing.entity.Promotion;
import com.ecommerce.marketing.mapper.FlashSaleItemMapper;
import com.ecommerce.marketing.mapper.PromotionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashSaleServiceImplTest {

    @Mock
    private FlashSaleItemMapper flashSaleItemMapper;
    @Mock
    private PromotionMapper promotionMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private FlashSaleServiceImpl flashSaleService;

    private FlashSaleItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = FlashSaleItem.builder()
                .promotionId(5L)
                .spuId(1L)
                .skuId(100L)
                .flashPrice(new BigDecimal("99"))
                .totalStock(10)
                .availableStock(8)
                .limitPerUser(1)
                .build();
        sampleItem.setId(7L);
    }

    // ---- deductStock ----

    @Test
    void deductStock_enoughStock_shouldDecrementRedisAndDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement("flash:stock:7", 2L)).thenReturn(6L);
        when(flashSaleItemMapper.selectById(7L)).thenReturn(sampleItem);
        when(flashSaleItemMapper.updateById(any(FlashSaleItem.class))).thenReturn(1);

        boolean result = flashSaleService.deductStock(7L, 2);

        assertTrue(result);
        verify(valueOperations).decrement("flash:stock:7", 2L);
        verify(valueOperations, never()).increment(anyString(), anyLong());
        ArgumentCaptor<FlashSaleItem> captor = ArgumentCaptor.forClass(FlashSaleItem.class);
        verify(flashSaleItemMapper).updateById(captor.capture());
        assertEquals(6, captor.getValue().getAvailableStock());
    }

    @Test
    void deductStock_oversold_shouldRollbackRedisAndReturnFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement("flash:stock:7", 3L)).thenReturn(-1L);

        boolean result = flashSaleService.deductStock(7L, 3);

        assertFalse(result);
        verify(valueOperations).increment("flash:stock:7", 3L);
        verify(flashSaleItemMapper, never()).selectById(anyLong());
        verify(flashSaleItemMapper, never()).updateById(any(FlashSaleItem.class));
    }

    @Test
    void deductStock_nullRedisResult_shouldRollbackAndReturnFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement("flash:stock:7", 1L)).thenReturn(null);

        boolean result = flashSaleService.deductStock(7L, 1);

        assertFalse(result);
        verify(valueOperations).increment("flash:stock:7", 1L);
        verify(flashSaleItemMapper, never()).updateById(any(FlashSaleItem.class));
    }

    // ---- restoreStock ----

    @Test
    void restoreStock_existingItem_shouldIncrementRedisAndDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(flashSaleItemMapper.selectById(7L)).thenReturn(sampleItem);
        when(flashSaleItemMapper.updateById(any(FlashSaleItem.class))).thenReturn(1);

        flashSaleService.restoreStock(7L, 2);

        verify(valueOperations).increment("flash:stock:7", 2L);
        ArgumentCaptor<FlashSaleItem> captor = ArgumentCaptor.forClass(FlashSaleItem.class);
        verify(flashSaleItemMapper).updateById(captor.capture());
        assertEquals(10, captor.getValue().getAvailableStock());
    }

    @Test
    void restoreStock_missingItem_shouldOnlyIncrementRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(flashSaleItemMapper.selectById(999L)).thenReturn(null);

        flashSaleService.restoreStock(999L, 1);

        verify(valueOperations).increment("flash:stock:999", 1L);
        verify(flashSaleItemMapper, never()).updateById(any(FlashSaleItem.class));
    }

    // ---- getActiveItems ----

    @Test
    void getActiveItems_activePromotion_shouldReturnItems() {
        Promotion promotion = Promotion.builder()
                .name("Flash Sale")
                .type(2)
                .status(1)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
        promotion.setId(5L);
        when(promotionMapper.selectById(5L)).thenReturn(promotion);
        when(flashSaleItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(sampleItem));

        List<FlashSaleItemDTO> items = flashSaleService.getActiveItems(5L);

        assertEquals(1, items.size());
        assertEquals(7L, items.getFirst().getId());
        assertEquals(new BigDecimal("99"), items.getFirst().getFlashPrice());
    }

    @Test
    void getActiveItems_promotionOutsideTimeWindow_shouldThrow() {
        Promotion promotion = Promotion.builder()
                .type(2)
                .status(1)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .build();
        promotion.setId(5L);
        when(promotionMapper.selectById(5L)).thenReturn(promotion);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> flashSaleService.getActiveItems(5L));
        assertEquals(ResultCode.FLASH_SALE_NOT_ACTIVE.getCode(), ex.getCode());
        verify(flashSaleItemMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void getActiveItems_notFlashSaleType_shouldThrow() {
        Promotion promotion = Promotion.builder()
                .type(1)
                .status(1)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
        promotion.setId(5L);
        when(promotionMapper.selectById(5L)).thenReturn(promotion);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> flashSaleService.getActiveItems(5L));
        assertEquals(ResultCode.FLASH_SALE_NOT_ACTIVE.getCode(), ex.getCode());
    }

    // ---- getItemDetail ----

    @Test
    void getItemDetail_missingItem_shouldThrow() {
        when(flashSaleItemMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> flashSaleService.getItemDetail(999L));
        assertEquals(ResultCode.FLASH_SALE_ITEM_NOT_FOUND.getCode(), ex.getCode());
    }
}
