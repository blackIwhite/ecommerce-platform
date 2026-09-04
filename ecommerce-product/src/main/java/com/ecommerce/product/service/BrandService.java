package com.ecommerce.product.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.product.dto.BrandCreateRequest;
import com.ecommerce.product.dto.BrandDTO;
import com.ecommerce.product.dto.BrandPageRequest;

import java.util.List;

public interface BrandService {

    PageResult<BrandDTO> pageBrand(BrandPageRequest request);

    List<BrandDTO> listAllEnabled();

    Long createBrand(BrandCreateRequest request);

    void updateBrand(BrandCreateRequest request);

    void deleteBrand(Long id);
}
