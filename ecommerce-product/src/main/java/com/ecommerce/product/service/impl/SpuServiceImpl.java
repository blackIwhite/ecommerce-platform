package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.product.dto.SkuDTO;
import com.ecommerce.product.dto.SpuCreateRequest;
import com.ecommerce.product.dto.SpuDTO;
import com.ecommerce.product.dto.SpuPageRequest;
import com.ecommerce.product.entity.Sku;
import com.ecommerce.product.entity.Spu;
import com.ecommerce.product.mapper.SkuMapper;
import com.ecommerce.product.mapper.SpuMapper;
import com.ecommerce.product.service.SkuService;
import com.ecommerce.product.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpuServiceImpl implements SpuService {

    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final SkuService skuService;

    @Override
    public Page<SpuDTO> pageSpu(SpuPageRequest request) {
        Page<Spu> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();
        if (request.getCategoryId() != null) {
            wrapper.eq(Spu::getCategoryId, request.getCategoryId());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Spu::getName, request.getKeyword());
        }
        wrapper.orderByDesc(Spu::getCreateTime);
        Page<Spu> spuPage = spuMapper.selectPage(page, wrapper);

        Page<SpuDTO> resultPage = new Page<>(spuPage.getCurrent(), spuPage.getSize(), spuPage.getTotal());
        resultPage.setRecords(spuPage.getRecords().stream()
                .map(this::toSpuDTO)
                .collect(Collectors.toList()));
        return resultPage;
    }

    @Override
    public SpuDTO getSpuDetail(Long spuId) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("SPU not found");
        }
        SpuDTO dto = toSpuDTO(spu);
        dto.setSkuList(skuService.getSkuListBySpuId(spuId));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSpu(SpuCreateRequest request) {
        Spu spu = Spu.builder()
                .name(request.getName())
                .categoryId(request.getCategoryId())
                .brandId(request.getBrandId())
                .description(request.getDescription())
                .images(request.getImages())
                .status(0)
                .build();
        spuMapper.insert(spu);

        if (request.getSkuList() != null) {
            for (SpuCreateRequest.SkuCreateItem item : request.getSkuList()) {
                Sku sku = Sku.builder()
                        .spuId(spu.getId())
                        .skuName(item.getSkuName())
                        .price(item.getPrice())
                        .stock(item.getStock())
                        .image(item.getImage())
                        .specs(item.getSpecs())
                        .build();
                skuMapper.insert(sku);
            }
        }
        return spu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpu(SpuCreateRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("SPU id is required for update");
        }
        Spu spu = spuMapper.selectById(request.getId());
        if (spu == null) {
            throw new BusinessException("SPU not found");
        }
        spu.setName(request.getName());
        spu.setCategoryId(request.getCategoryId());
        spu.setBrandId(request.getBrandId());
        spu.setDescription(request.getDescription());
        spu.setImages(request.getImages());
        spuMapper.updateById(spu);

        if (request.getSkuList() != null) {
            // Delete old SKUs and recreate
            LambdaQueryWrapper<Sku> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(Sku::getSpuId, request.getId());
            skuMapper.delete(deleteWrapper);

            for (SpuCreateRequest.SkuCreateItem item : request.getSkuList()) {
                Sku sku = Sku.builder()
                        .spuId(request.getId())
                        .skuName(item.getSkuName())
                        .price(item.getPrice())
                        .stock(item.getStock())
                        .image(item.getImage())
                        .specs(item.getSpecs())
                        .build();
                skuMapper.insert(sku);
            }
        }
    }

    @Override
    public void updateStatus(Long spuId, Integer status) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException("SPU not found");
        }
        spu.setStatus(status);
        spuMapper.updateById(spu);
    }

    private SpuDTO toSpuDTO(Spu spu) {
        return SpuDTO.builder()
                .spuId(spu.getId())
                .name(spu.getName())
                .categoryId(spu.getCategoryId())
                .brandId(spu.getBrandId())
                .description(spu.getDescription())
                .images(spu.getImages())
                .status(spu.getStatus())
                .createTime(spu.getCreateTime())
                .build();
    }
}
