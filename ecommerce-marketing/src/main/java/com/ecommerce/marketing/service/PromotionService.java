package com.ecommerce.marketing.service;

import com.ecommerce.api.marketing.dto.PromotionDTO;
import com.ecommerce.common.core.page.PageResult;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {

    void createPromotion(PromotionDTO dto);

    void updatePromotion(PromotionDTO dto);

    void deletePromotion(Long id);

    void updatePromotionStatus(Long id, Integer status);

    PageResult<PromotionDTO> listPromotions(int pageNum, int pageSize, Integer type, Integer status);

    PromotionDTO getPromotion(Long id);

    List<PromotionDTO> listActivePromotions();

    BigDecimal calculateFullReduction(Long promotionId, BigDecimal orderAmount);
}
