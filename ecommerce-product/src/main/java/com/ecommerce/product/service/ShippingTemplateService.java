package com.ecommerce.product.service;

import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.product.dto.ShippingFeeCalculateResult;
import com.ecommerce.product.dto.ShippingTemplateCreateRequest;
import com.ecommerce.product.dto.ShippingTemplateDTO;

import java.math.BigDecimal;

public interface ShippingTemplateService {

    PageResult<ShippingTemplateDTO> page(Integer status, int pageNum, int pageSize);

    ShippingTemplateDTO getDetail(Long id);

    Long create(ShippingTemplateCreateRequest request);

    void update(ShippingTemplateCreateRequest request);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    ShippingFeeCalculateResult calculateFee(Long templateId, String regionCode, BigDecimal quantity);
}
