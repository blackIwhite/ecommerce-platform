package com.ecommerce.marketing.service;

import com.ecommerce.api.marketing.dto.CouponTemplateDTO;
import com.ecommerce.api.marketing.dto.CouponUseRequest;
import com.ecommerce.api.marketing.dto.CouponUseResponse;
import com.ecommerce.api.marketing.dto.UserCouponDTO;
import com.ecommerce.common.core.page.PageResult;

import java.util.List;

public interface CouponService {

    void createTemplate(CouponTemplateDTO dto);

    void updateTemplate(CouponTemplateDTO dto);

    void deleteTemplate(Long id);

    void updateTemplateStatus(Long id, Integer status);

    PageResult<CouponTemplateDTO> listTemplates(int pageNum, int pageSize, Integer status);

    CouponTemplateDTO getTemplate(Long id);

    List<CouponTemplateDTO> listAvailableCoupons();

    Long claimCoupon(Long templateId);

    List<UserCouponDTO> getMyCoupons(Integer status);

    CouponUseResponse checkCoupon(Long userCouponId, Long userId, java.math.BigDecimal orderAmount);

    CouponUseResponse useCoupon(CouponUseRequest request);

    void releaseCoupon(Long userCouponId);

    int expireUnusedCoupons();
}
