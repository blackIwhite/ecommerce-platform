package com.ecommerce.marketing.controller;

import com.ecommerce.api.marketing.dto.CouponUseRequest;
import com.ecommerce.api.marketing.dto.CouponUseResponse;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.marketing.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/marketing/internal")
@RequiredArgsConstructor
public class CouponInternalController {

    private final CouponService couponService;

    @GetMapping("/coupon/{userCouponId}/check")
    public Result<CouponUseResponse> checkCoupon(@PathVariable("userCouponId") Long userCouponId,
                                                  @RequestParam("userId") Long userId,
                                                  @RequestParam("orderAmount") java.math.BigDecimal orderAmount) {
        return Result.success(couponService.checkCoupon(userCouponId, userId, orderAmount));
    }

    @PutMapping("/coupon/use")
    public Result<CouponUseResponse> useCoupon(@RequestBody CouponUseRequest request) {
        return Result.success(couponService.useCoupon(request));
    }

    @PutMapping("/coupon/release/{userCouponId}")
    public Result<Void> releaseCoupon(@PathVariable("userCouponId") Long userCouponId) {
        couponService.releaseCoupon(userCouponId);
        return Result.success();
    }
}
