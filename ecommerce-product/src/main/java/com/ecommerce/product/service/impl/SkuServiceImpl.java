package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.product.dto.SkuDTO;
import com.ecommerce.product.entity.Sku;
import com.ecommerce.product.mapper.SkuMapper;
import com.ecommerce.product.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkuServiceImpl implements SkuService {

    private final SkuMapper skuMapper;

    @Override
    public SkuDTO getSkuById(Long skuId) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU not found");
        }
        return toSkuDTO(sku);
    }

    @Override
    public List<SkuDTO> getSkuListByIds(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        List<Sku> skus = skuMapper.selectBatchIds(skuIds);
        return skus.stream().map(this::toSkuDTO).collect(Collectors.toList());
    }

    @Override
    public List<SkuDTO> getSkuListBySpuId(Long spuId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSpuId, spuId);
        List<Sku> skus = skuMapper.selectList(wrapper);
        return skus.stream().map(this::toSkuDTO).collect(Collectors.toList());
    }

    private SkuDTO toSkuDTO(Sku sku) {
        return SkuDTO.builder()
                .skuId(sku.getId())
                .spuId(sku.getSpuId())
                .skuName(sku.getSkuName())
                .price(sku.getPrice())
                .stock(sku.getStock())
                .image(sku.getImage())
                .specs(sku.getSpecs())
                .build();
    }
}
