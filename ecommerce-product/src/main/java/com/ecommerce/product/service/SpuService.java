package com.ecommerce.product.service;

import com.ecommerce.api.product.dto.SalesIncrementItem;
import com.ecommerce.api.product.dto.SpuSimpleDTO;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.product.dto.SpuCreateRequest;
import com.ecommerce.product.dto.SpuDTO;
import com.ecommerce.product.dto.SpuPageRequest;

import java.util.List;

public interface SpuService {

    PageResult<SpuDTO> pageSpu(SpuPageRequest request);

    SpuDTO getSpuDetail(Long spuId);

    Long createSpu(SpuCreateRequest request);

    void updateSpu(SpuCreateRequest request);

    void updateStatus(Long spuId, Integer status);

    void incrementSales(List<SalesIncrementItem> items);

    List<SpuSimpleDTO> getSpuSimpleList(List<Long> spuIds);
}
