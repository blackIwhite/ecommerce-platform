package com.ecommerce.api.marketing;

import com.ecommerce.api.marketing.dto.CouponUseRequest;
import com.ecommerce.api.marketing.dto.CouponUseResponse;
import com.ecommerce.api.marketing.dto.PointsEarnRequest;
import com.ecommerce.api.marketing.dto.PointsRedeemRequest;
import com.ecommerce.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "ecommerce-marketing")
public interface MarketingApi {

    @GetMapping("/marketing/internal/coupon/{userCouponId}/check")
    Result<CouponUseResponse> checkCoupon(@PathVariable("userCouponId") Long userCouponId,
                                          @RequestParam("userId") Long userId,
                                          @RequestParam("orderAmount") BigDecimal orderAmount);

    @PutMapping("/marketing/internal/coupon/use")
    Result<CouponUseResponse> useCoupon(@RequestBody CouponUseRequest request);

    @PutMapping("/marketing/internal/coupon/release/{userCouponId}")
    Result<Void> releaseCoupon(@PathVariable("userCouponId") Long userCouponId);

    @PostMapping("/marketing/internal/points/earn")
    Result<Void> earnPoints(@RequestBody PointsEarnRequest request);

    @PostMapping("/marketing/internal/points/redeem")
    Result<Void> redeemPoints(@RequestBody PointsRedeemRequest request);
}
