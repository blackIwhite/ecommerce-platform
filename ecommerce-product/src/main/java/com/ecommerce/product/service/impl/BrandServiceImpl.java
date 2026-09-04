package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.common.core.result.ResultCode;
import com.ecommerce.common.redis.util.RedisUtils;
import com.ecommerce.product.dto.BrandCreateRequest;
import com.ecommerce.product.dto.BrandDTO;
import com.ecommerce.product.dto.BrandPageRequest;
import com.ecommerce.product.entity.Brand;
import com.ecommerce.product.mapper.BrandMapper;
import com.ecommerce.product.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandMapper brandMapper;
    private final RedisUtils redisUtils;

    private static final String BRAND_ALL_CACHE_KEY = "product:brand:all";
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    public PageResult<BrandDTO> pageBrand(BrandPageRequest request) {
        Page<Brand> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Brand::getName, request.getKeyword());
        }
        wrapper.orderByDesc(Brand::getCreateTime);

        Page<Brand> result = brandMapper.selectPage(page, wrapper);

        List<BrandDTO> dtoList = result.getRecords().stream()
                .map(this::toBrandDTO)
                .toList();

        return PageResult.of(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public List<BrandDTO> listAllEnabled() {
        Object cached = redisUtils.get(BRAND_ALL_CACHE_KEY);
        if (cached instanceof List) {
            return (List<BrandDTO>) cached;
        }

        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getStatus, 1).orderByAsc(Brand::getName);
        List<BrandDTO> list = brandMapper.selectList(wrapper).stream()
                .map(this::toBrandDTO)
                .toList();

        redisUtils.set(BRAND_ALL_CACHE_KEY, list, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBrand(BrandCreateRequest request) {
        Brand brand = Brand.builder()
                .name(request.getName())
                .logo(request.getLogo())
                .description(request.getDescription())
                .status(1)
                .build();
        brandMapper.insert(brand);
        evictCache();
        return brand.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrand(BrandCreateRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Brand id is required");
        }
        Brand existing = brandMapper.selectById(request.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND, "Brand not found");
        }
        existing.setName(request.getName());
        existing.setLogo(request.getLogo());
        existing.setDescription(request.getDescription());
        brandMapper.updateById(existing);
        evictCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBrand(Long id) {
        Brand existing = brandMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND, "Brand not found");
        }
        brandMapper.deleteById(id);
        evictCache();
    }

    private BrandDTO toBrandDTO(Brand brand) {
        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .logo(brand.getLogo())
                .description(brand.getDescription())
                .status(brand.getStatus())
                .build();
    }

    private void evictCache() {
        redisUtils.delete(BRAND_ALL_CACHE_KEY);
    }
}
