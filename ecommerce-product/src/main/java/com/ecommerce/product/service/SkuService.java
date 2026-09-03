package com.ecommerce.product.service;

import com.ecommerce.product.dto.SkuDTO;

import java.util.List;

public interface SkuService {

    SkuDTO getSkuById(Long skuId);

    List<SkuDTO> getSkuListByIds(List<Long> skuIds);

    List<SkuDTO> getSkuListBySpuId(Long spuId);
}
