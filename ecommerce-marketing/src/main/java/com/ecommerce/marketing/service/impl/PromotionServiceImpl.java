package com.ecommerce.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.api.marketing.dto.PromotionDTO;
import com.ecommerce.api.marketing.dto.PromotionDTO.FullReductionRule;
import com.ecommerce.api.marketing.dto.PromotionDTO.FlashSaleItemDTO;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.core.util.JsonUtils;
import com.ecommerce.marketing.entity.FlashSaleItem;
import com.ecommerce.marketing.entity.Promotion;
import com.ecommerce.marketing.mapper.FlashSaleItemMapper;
import com.ecommerce.marketing.mapper.PromotionMapper;
import com.ecommerce.marketing.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionMapper promotionMapper;
    private final FlashSaleItemMapper flashSaleItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPromotion(PromotionDTO dto) {
        String ruleJson = dto.getRules() != null ? JsonUtils.toJson(dto.getRules()) : dto.getRuleJson();
        Promotion promotion = Promotion.builder()
                .name(dto.getName())
                .type(dto.getType())
                .ruleJson(ruleJson)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .description(dto.getDescription())
                .build();
        promotionMapper.insert(promotion);

        if (dto.getType() == 2 && dto.getItems() != null) {
            for (FlashSaleItemDTO itemDto : dto.getItems()) {
                FlashSaleItem item = FlashSaleItem.builder()
                        .promotionId(promotion.getId())
                        .spuId(itemDto.getSpuId())
                        .skuId(itemDto.getSkuId())
                        .flashPrice(itemDto.getFlashPrice())
                        .totalStock(itemDto.getTotalStock())
                        .availableStock(itemDto.getTotalStock())
                        .limitPerUser(itemDto.getLimitPerUser() != null ? itemDto.getLimitPerUser() : 1)
                        .build();
                flashSaleItemMapper.insert(item);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePromotion(PromotionDTO dto) {
        Promotion promotion = promotionMapper.selectById(dto.getId());
        if (promotion == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        promotion.setName(dto.getName());
        promotion.setType(dto.getType());
        promotion.setStartTime(dto.getStartTime());
        promotion.setEndTime(dto.getEndTime());
        promotion.setDescription(dto.getDescription());
        if (dto.getRules() != null) {
            promotion.setRuleJson(JsonUtils.toJson(dto.getRules()));
        }
        promotionMapper.updateById(promotion);
    }

    @Override
    public void deletePromotion(Long id) {
        promotionMapper.deleteById(id);
    }

    @Override
    public void updatePromotionStatus(Long id, Integer status) {
        Promotion promotion = promotionMapper.selectById(id);
        if (promotion == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        promotion.setStatus(status);
        promotionMapper.updateById(promotion);
    }

    @Override
    public PageResult<PromotionDTO> listPromotions(int pageNum, int pageSize, Integer type, Integer status) {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        if (type != null) wrapper.eq(Promotion::getType, type);
        if (status != null) wrapper.eq(Promotion::getStatus, status);
        wrapper.orderByDesc(Promotion::getCreateTime);
        Page<Promotion> page = promotionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<PromotionDTO> records = page.getRecords().stream().map(this::toDTO).toList();
        return PageResult.of(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public PromotionDTO getPromotion(Long id) {
        Promotion promotion = promotionMapper.selectById(id);
        if (promotion == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toDTOWithItems(promotion);
    }

    @Override
    public List<PromotionDTO> listActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Promotion::getStatus, 1)
                .le(Promotion::getStartTime, now)
                .ge(Promotion::getEndTime, now)
                .orderByDesc(Promotion::getCreateTime);
        return promotionMapper.selectList(wrapper).stream().map(this::toDTOWithItems).toList();
    }

    @Override
    public BigDecimal calculateFullReduction(Long promotionId, BigDecimal orderAmount) {
        Promotion promotion = promotionMapper.selectById(promotionId);
        if (promotion == null || promotion.getType() != 1) {
            return BigDecimal.ZERO;
        }
        List<FullReductionRule> rules = JsonUtils.toList(promotion.getRuleJson(), FullReductionRule.class);
        if (rules == null || rules.isEmpty()) {
            return BigDecimal.ZERO;
        }
        rules.sort(Comparator.comparing(FullReductionRule::getMinAmount).reversed());
        for (FullReductionRule rule : rules) {
            if (orderAmount.compareTo(rule.getMinAmount()) >= 0) {
                return rule.getReduction().min(orderAmount);
            }
        }
        return BigDecimal.ZERO;
    }

    private PromotionDTO toDTO(Promotion p) {
        List<FullReductionRule> rules = p.getRuleJson() != null
                ? JsonUtils.toList(p.getRuleJson(), FullReductionRule.class) : Collections.emptyList();
        return PromotionDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .type(p.getType())
                .ruleJson(p.getRuleJson())
                .rules(rules)
                .startTime(p.getStartTime())
                .endTime(p.getEndTime())
                .status(p.getStatus())
                .description(p.getDescription())
                .createTime(p.getCreateTime())
                .build();
    }

    private PromotionDTO toDTOWithItems(Promotion p) {
        PromotionDTO dto = toDTO(p);
        if (p.getType() == 2) {
            List<FlashSaleItem> items = flashSaleItemMapper.selectList(
                    new LambdaQueryWrapper<FlashSaleItem>().eq(FlashSaleItem::getPromotionId, p.getId()));
            dto.setItems(items.stream().map(item -> FlashSaleItemDTO.builder()
                    .id(item.getId())
                    .spuId(item.getSpuId())
                    .skuId(item.getSkuId())
                    .flashPrice(item.getFlashPrice())
                    .totalStock(item.getTotalStock())
                    .availableStock(item.getAvailableStock())
                    .limitPerUser(item.getLimitPerUser())
                    .build()).toList());
        }
        return dto;
    }
}
