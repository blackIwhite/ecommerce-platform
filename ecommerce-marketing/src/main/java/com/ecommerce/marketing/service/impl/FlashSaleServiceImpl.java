package com.ecommerce.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.marketing.dto.PromotionDTO.FlashSaleItemDTO;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.marketing.entity.FlashSaleItem;
import com.ecommerce.marketing.entity.Promotion;
import com.ecommerce.marketing.mapper.FlashSaleItemMapper;
import com.ecommerce.marketing.mapper.PromotionMapper;
import com.ecommerce.marketing.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleServiceImpl implements FlashSaleService {

    private final FlashSaleItemMapper flashSaleItemMapper;
    private final PromotionMapper promotionMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String FLASH_STOCK_KEY = "flash:stock:";
    private static final String FLASH_PURCHASE_KEY = "flash:purchase:";

    @Override
    public PageResult<FlashSaleItemDTO> listItems(Long promotionId, int pageNum, int pageSize) {
        LambdaQueryWrapper<FlashSaleItem> wrapper = new LambdaQueryWrapper<>();
        if (promotionId != null) {
            wrapper.eq(FlashSaleItem::getPromotionId, promotionId);
        }
        wrapper.orderByDesc(FlashSaleItem::getCreateTime);
        Page<FlashSaleItem> page = flashSaleItemMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<FlashSaleItemDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public FlashSaleItemDTO getItemDetail(Long itemId) {
        FlashSaleItem item = flashSaleItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ResultCode.FLASH_SALE_ITEM_NOT_FOUND);
        }
        return toDTO(item);
    }

    @Override
    public List<FlashSaleItemDTO> getActiveItems(Long promotionId) {
        Promotion promotion = promotionMapper.selectById(promotionId);
        if (promotion == null || promotion.getType() != 2 || promotion.getStatus() != 1) {
            throw new BusinessException(ResultCode.FLASH_SALE_NOT_ACTIVE);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promotion.getStartTime()) || now.isAfter(promotion.getEndTime())) {
            throw new BusinessException(ResultCode.FLASH_SALE_NOT_ACTIVE);
        }
        LambdaQueryWrapper<FlashSaleItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSaleItem::getPromotionId, promotionId)
                .gt(FlashSaleItem::getAvailableStock, 0);
        return flashSaleItemMapper.selectList(wrapper).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItem(FlashSaleItemDTO dto) {
        FlashSaleItem item = FlashSaleItem.builder()
                .promotionId(dto.getSpuId() != null ? dto.getSpuId() : 0L)
                .spuId(dto.getSpuId())
                .skuId(dto.getSkuId())
                .flashPrice(dto.getFlashPrice())
                .totalStock(dto.getTotalStock())
                .availableStock(dto.getTotalStock())
                .limitPerUser(dto.getLimitPerUser() != null ? dto.getLimitPerUser() : 1)
                .build();
        flashSaleItemMapper.insert(item);
        redisTemplate.opsForValue().set(FLASH_STOCK_KEY + item.getId(), String.valueOf(dto.getTotalStock()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(FlashSaleItemDTO dto) {
        FlashSaleItem item = flashSaleItemMapper.selectById(dto.getId());
        if (item == null) {
            throw new BusinessException(ResultCode.FLASH_SALE_ITEM_NOT_FOUND);
        }
        item.setFlashPrice(dto.getFlashPrice());
        if (dto.getTotalStock() != null) {
            int diff = dto.getTotalStock() - item.getTotalStock();
            item.setTotalStock(dto.getTotalStock());
            item.setAvailableStock(item.getAvailableStock() + diff);
            redisTemplate.opsForValue().set(FLASH_STOCK_KEY + item.getId(), String.valueOf(item.getAvailableStock()));
        }
        if (dto.getLimitPerUser() != null) {
            item.setLimitPerUser(dto.getLimitPerUser());
        }
        flashSaleItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long itemId) {
        FlashSaleItem item = flashSaleItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ResultCode.FLASH_SALE_ITEM_NOT_FOUND);
        }
        flashSaleItemMapper.deleteById(itemId);
        redisTemplate.delete(FLASH_STOCK_KEY + itemId);
        redisTemplate.delete(FLASH_PURCHASE_KEY + itemId);
    }

    @Override
    public boolean deductStock(Long itemId, int quantity) {
        String key = FLASH_STOCK_KEY + itemId;
        Long remaining = redisTemplate.opsForValue().decrement(key, quantity);
        if (remaining == null || remaining < 0) {
            redisTemplate.opsForValue().increment(key, quantity);
            return false;
        }
        FlashSaleItem item = flashSaleItemMapper.selectById(itemId);
        if (item != null) {
            item.setAvailableStock(item.getAvailableStock() - quantity);
            flashSaleItemMapper.updateById(item);
        }
        return true;
    }

    @Override
    public void restoreStock(Long itemId, int quantity) {
        String key = FLASH_STOCK_KEY + itemId;
        redisTemplate.opsForValue().increment(key, quantity);
        FlashSaleItem item = flashSaleItemMapper.selectById(itemId);
        if (item != null) {
            item.setAvailableStock(item.getAvailableStock() + quantity);
            flashSaleItemMapper.updateById(item);
        }
    }

    private FlashSaleItemDTO toDTO(FlashSaleItem item) {
        return FlashSaleItemDTO.builder()
                .id(item.getId())
                .spuId(item.getSpuId())
                .skuId(item.getSkuId())
                .flashPrice(item.getFlashPrice())
                .totalStock(item.getTotalStock())
                .availableStock(item.getAvailableStock())
                .limitPerUser(item.getLimitPerUser())
                .build();
    }
}
