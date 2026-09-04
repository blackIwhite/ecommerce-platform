package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.product.dto.SkuDTO;
import com.ecommerce.product.dto.SpuCreateRequest;
import com.ecommerce.product.dto.SpuDTO;
import com.ecommerce.product.dto.SpuPageRequest;
import com.ecommerce.product.entity.Brand;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Sku;
import com.ecommerce.product.entity.Spu;
import com.ecommerce.product.mapper.BrandMapper;
import com.ecommerce.product.mapper.CategoryMapper;
import com.ecommerce.product.mapper.SkuMapper;
import com.ecommerce.product.mapper.SpuMapper;
import com.ecommerce.product.service.SkuService;
import com.ecommerce.product.service.SpuEsService;
import com.ecommerce.product.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpuServiceImpl implements SpuService {

    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final SkuService skuService;
    private final SpuEsService spuEsService;

    @Override
    public PageResult<SpuDTO> pageSpu(SpuPageRequest request) {
        if (StringUtils.hasText(request.getKeyword())) {
            PageResult<Long> esResult = spuEsService.searchSpuIds(request);
            if (esResult.getList().isEmpty()) {
                return PageResult.of(Collections.emptyList(), esResult.getTotal(), request.getPageNum(), request.getPageSize());
            }
            List<SpuDTO> dtoList = fetchBySpuIds(esResult.getList());
            return PageResult.of(dtoList, esResult.getTotal(), request.getPageNum(), request.getPageSize());
        }

        Page<Spu> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();
        if (request.getCategoryId() != null) {
            wrapper.eq(Spu::getCategoryId, request.getCategoryId());
        }
        if (request.getBrandId() != null) {
            wrapper.eq(Spu::getBrandId, request.getBrandId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Spu::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(Spu::getCreateTime);
        Page<Spu> spuPage = spuMapper.selectPage(page, wrapper);

        List<SpuDTO> dtoList = enrichSpuDTOList(spuPage.getRecords());

        return PageResult.of(dtoList, spuPage.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public SpuDTO getSpuDetail(Long spuId) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        SpuDTO dto = enrichSpuDTOList(List.of(spu)).get(0);
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
        spuEsService.indexSpu(spu.getId());
        return spu.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpu(SpuCreateRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ResultCode.PRODUCT_ID_REQUIRED);
        }
        Spu spu = spuMapper.selectById(request.getId());
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        spu.setName(request.getName());
        spu.setCategoryId(request.getCategoryId());
        spu.setBrandId(request.getBrandId());
        spu.setDescription(request.getDescription());
        spu.setImages(request.getImages());
        spuMapper.updateById(spu);

        if (request.getSkuList() != null) {
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
        spuEsService.indexSpu(request.getId());
    }

    @Override
    public void updateStatus(Long spuId, Integer status) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        spu.setStatus(status);
        spuMapper.updateById(spu);
        spuEsService.indexSpu(spuId);
    }

    private List<SpuDTO> enrichSpuDTOList(List<Spu> spuList) {
        if (spuList.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> categoryIds = spuList.stream()
                .map(Spu::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> brandIds = spuList.stream()
                .map(Spu::getBrandId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, Category> categoryMap = categoryIds.isEmpty()
                ? Collections.<Long, Category>emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Function.identity()));

        Map<Long, Brand> brandMap = brandIds.isEmpty()
                ? Collections.<Long, Brand>emptyMap()
                : brandMapper.selectBatchIds(brandIds).stream()
                        .collect(Collectors.toMap(Brand::getId, Function.identity()));

        return spuList.stream().map(spu -> {
            SpuDTO dto = toSpuDTO(spu);
            Category category = categoryMap.get(spu.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
            Brand brand = brandMap.get(spu.getBrandId());
            if (brand != null) {
                dto.setBrandName(brand.getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private List<SpuDTO> fetchBySpuIds(List<Long> spuIds) {
        List<Spu> spuList = spuMapper.selectBatchIds(spuIds);
        Map<Long, Spu> spuMap = spuList.stream().collect(Collectors.toMap(Spu::getId, Function.identity()));
        List<Spu> ordered = spuIds.stream()
                .map(spuMap::get)
                .filter(Objects::nonNull)
                .toList();
        return enrichSpuDTOList(ordered);
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
